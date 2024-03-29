/*
 * Copyright 2019-2021, Hridesh Rajan, Robert Dyer, Che Shian Hung
 *                 Bowling Green State University
 *                 Iowa State University of Science and Technology
 *                 and University of Nebraska Board of Regents
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package boa.io;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.JobID;
import org.apache.hadoop.mapred.RunningJob;
import org.apache.hadoop.mapred.TaskCompletionEvent;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.JobStatus;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.output.FileOutputCommitter;

/**
 * A {@link FileOutputCommitter} that stores the job results into a database.
 *
 * @author rdyer
 * @author hungc
 */
public class BoaOutputCommitter extends FileOutputCommitter {
	private final Path outputPath;
	private final TaskAttemptContext context;
	public static Throwable lastSeenEx = null;

	public BoaOutputCommitter(final Path output, final TaskAttemptContext context) throws java.io.IOException {
		super(output, context);

		this.context = context;
		this.outputPath = output;
	}

	@Override
	public void commitJob(final JobContext context) throws java.io.IOException {
		super.commitJob(context);

		final int boaJobId = context.getConfiguration().getInt("boa.hadoop.jobid", 0);
		storeOutput(context, boaJobId);
		updateStatus(null, boaJobId);
	}

	@Override
	public void abortJob(final JobContext context, final JobStatus.State runState) throws java.io.IOException {
		super.abortJob(context, runState);

		final JobClient jobClient = new JobClient(new JobConf(context.getConfiguration()));
		final RunningJob job = jobClient.getJob((org.apache.hadoop.mapred.JobID) JobID.forName(context.getConfiguration().get("mapred.job.id")));
		String diag = "";
		int start = 0;
		while (true) {
			final TaskCompletionEvent[] events = job.getTaskCompletionEvents(start);
			if (events == null || events.length == 0)
				break;
			start += events.length;
			for (final TaskCompletionEvent event : events)
				switch (event.getTaskStatus()) {
					case KILLED:
						break;
					case SUCCEEDED:
						break;
					default:
						diag += "Diagnostics for: " + event.getTaskAttemptId() + "\n";
						for (final String s : job.getTaskDiagnostics(event.getTaskAttemptId()))
							diag += s + "\n";
						diag += "\n";
						break;
				}
		}
		updateStatus(diag, context.getConfiguration().getInt("boa.hadoop.jobid", 0));
	}

	private final static String url = "jdbc:mysql://head:3306/drupal";
	private final static String user = "drupal";
	private final static String password = "";

	private void updateStatus(final String error, final int jobId) {
		if (jobId == 0)
			return;

		Connection con = null;
		try {
			con = DriverManager.getConnection(url, user, password);
			PreparedStatement ps = null;
			PreparedStatement ps2 = null;
			try {
				ps = con.prepareStatement("UPDATE boa_jobs SET hadoop_end=CURRENT_TIMESTAMP(), hadoop_status=? WHERE id=" + jobId);
				ps.setInt(1, error != null ? -1 : 2);
				ps.executeUpdate();

				ps2 = con.prepareStatement("UPDATE boa_jobs SET hadoop_output=CONCAT(hadoop_output, ?) WHERE id=" + jobId);
				ps2.setString(1, error == null ? "" : error);
				ps2.executeUpdate();
			} finally {
				try {
					if (ps != null)
						ps.close();
				} catch (final Exception e) {
					e.printStackTrace();
				}
				try {
					if (ps2 != null)
						ps2.close();
				} catch (final Exception e) {
					e.printStackTrace();
				}
			}
		} catch (final Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (con != null)
					con.close();
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void storeOutput(final JobContext context, final int jobId) {
		if (jobId == 0)
			return;

		Connection con = null;
		FileSystem fileSystem = null;
		FSDataInputStream in = null;

		try {
			fileSystem = outputPath.getFileSystem(context.getConfiguration());

			con = DriverManager.getConnection(url, user, password);

			PreparedStatement ps = null;
			try {
				ps = con.prepareStatement("INSERT INTO boa_output (id, length, web_result) VALUES (" + jobId + ", 0, '')");
				ps.executeUpdate();
			} catch (final Exception e) {
			} finally {
				try {
					if (ps != null)
						ps.close();
				} catch (final Exception e) {
					e.printStackTrace();
				}
			}

			fileSystem.mkdirs(new Path("/boa", new Path("" + jobId)));

			int partNum = 0;

			final int MAX_OUTPUT = 64 * 1024 - 1;
			final byte[] b = new byte[MAX_OUTPUT];
			long length = 0;
			String output = "";

			// ensure the reducer class is initialized in the cleanup task
			try {
				context.getReducerClass().getConstructor().newInstance();
			} catch (final ReflectiveOperationException e) { }

			while (true) {
				final Path path = new Path(outputPath, "part-r-" + String.format("%05d", partNum));
				if (!fileSystem.exists(path))
					break;

				final Path newpath = new Path("/boa", new Path("" + jobId, new Path(boa.runtime.BoaPartitioner.getVariableFromPartition(partNum++) + ".txt")));
				if (fileSystem.exists(newpath))
					try {
						fileSystem.delete(newpath, false);
					} catch (final Exception e) {
						// do nothing
					}

				fileSystem.rename(path, newpath);

				if (in != null)
					try {
						in.close();
					} catch (final Exception e) {
						e.printStackTrace();
					}
				in = fileSystem.open(newpath);

				int numBytes = 0;

				while ((numBytes = in.read(b)) > 0) {
					if (output.length() < MAX_OUTPUT)
						output += new String(b, 0, numBytes);
					length += numBytes;

					this.context.progress();
				}
			}

			try {
				ps = con.prepareStatement("UPDATE boa_output SET length=?, web_result=?, hash=MD5(web_result) WHERE id=" + jobId);
				ps.setLong(1, length);
				if (output.length() < MAX_OUTPUT)
					ps.setString(2, output);
				else
					ps.setString(2, output.substring(0, MAX_OUTPUT));
				ps.executeUpdate();
			} finally {
				try {
					if (ps != null)
						ps.close();
				} catch (final Exception e) {
					e.printStackTrace();
				}
			}
		} catch (final Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (con != null)
					con.close();
			} catch (final Exception e) {
				e.printStackTrace();
			}
			try {
				if (in != null)
					in.close();
			} catch (final Exception e) {
				e.printStackTrace();
			}
			try {
				if (fileSystem != null)
					fileSystem.close();
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void setJobID(final String id, final int jobId) {
		if (jobId == 0)
			return;

		Connection con = null;
		try {
			con = DriverManager.getConnection(url, user, password);
			PreparedStatement ps = null;
			try {
				ps = con.prepareStatement("UPDATE boa_jobs SET hadoop_id=? WHERE id=" + jobId);
				ps.setString(1, id);
				ps.executeUpdate();
			} finally {
				try {
					if (ps != null)
						ps.close();
				} catch (final Exception e) {
					e.printStackTrace();
				}
			}
		} catch (final Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (con != null)
					con.close();
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}
	}
}
