package boa.io;

import java.io.*;
import java.sql.*;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.lib.output.FileOutputCommitter;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.JobStatus;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

public class BoaOutputCommitter extends FileOutputCommitter {
	private final Path outputPath;
	private final TaskAttemptContext context;

	public BoaOutputCommitter(Path output, TaskAttemptContext context) throws java.io.IOException {
		super(output, context);
		this.context = context;
		this.outputPath = output;
	}

	@Override
	public void commitJob(JobContext context) throws java.io.IOException {
		super.commitJob(context);
		int jobId = context.getConfiguration().getInt("boa.hadoop.jobid", 0);
		updateStatus(false, jobId);
		storeOutput(context, jobId);
	}

	@Override
	public void abortJob(JobContext context, JobStatus.State runState) throws java.io.IOException {
		super.abortJob(context, runState);
		int jobId = context.getConfiguration().getInt("boa.hadoop.jobid", 0);
		updateStatus(true, jobId);
	}

	private final static String url = "jdbc:mysql://boa-head:3306/drupal";
	private final static String user = "drupal";
	private final static String password = "";

	private void updateStatus(final boolean error, final int jobId) {
		if (jobId == 0)
			return;

		Connection con = null;
		try {
			con = DriverManager.getConnection(url, user, password);
			PreparedStatement ps = null;
			try {
				ps = con.prepareStatement("UPDATE boa_jobs SET hadoop_end=CURRENT_TIMESTAMP(), hadoop_status=? WHERE id=" + jobId);
				ps.setInt(1, error ? -1 : 2);
				ps.executeUpdate();
			} finally {
				try { if (ps != null) ps.close(); } catch (final Exception e) { e.printStackTrace(); }
			}
		} catch (final Exception e) {
			e.printStackTrace();
		} finally {
			try { if (con != null) con.close(); } catch (final Exception e) { e.printStackTrace(); }
		}
	}

	private void storeOutput(final JobContext context, final int jobId) {
		if (jobId == 0)
			return;

		Connection con = null;
		FileSystem fileSystem = null;
		FSDataInputStream in = null;
		ByteArrayOutputStream out = null;

		try {
			fileSystem = outputPath.getFileSystem(context.getConfiguration());

			con = DriverManager.getConnection(url, user, password);

			int partNum = 0;

			final byte[] b = new byte[4096];
			long pos = 1;
			out = new ByteArrayOutputStream();

			while (true) {
				final Path path = new Path(outputPath, "part-r-" + String.format("%05d", partNum++));
				if (!fileSystem.exists(path))
					break;

				if (in != null)
					try { in.close(); } catch (final Exception e) { e.printStackTrace(); }
				in = fileSystem.open(path);

				PreparedStatement ps = null;
				try {
					ps = con.prepareStatement("INSERT INTO boa_output (id, result) VALUES (" + jobId + ", '')");
					ps.executeUpdate();
				} catch (final Exception e) {
				} finally {
					try { if (ps != null) ps.close(); } catch (final Exception e) { e.printStackTrace(); }
				}

				try {
					ps = con.prepareStatement("UPDATE boa_output SET result=INSERT(result, ?, ?, ?) WHERE id=" + jobId);

					int numBytes = 0;

					while ((numBytes = in.read(b)) > 0) {
						out.write(b, 0, numBytes);
						if (out.size() >= 4194304) {
							ps.setLong(1, pos);
							ps.setInt(2, out.size());
							ps.setString(3, out.toString());
							ps.executeUpdate();
							this.context.progress();

							pos += out.size();
							out.reset();
						}
					}

					if (out.size() > 0) {
						ps.setLong(1, pos);
						ps.setInt(2, out.size());
						ps.setString(3, out.toString());
						ps.executeUpdate();
						this.context.progress();

						pos += out.size();
					}
				} finally {
					try { if (ps != null) ps.close(); } catch (final Exception e) { e.printStackTrace(); }
				}
			}

			PreparedStatement ps = null;
			try {
				ps = con.prepareStatement("UPDATE boa_output SET length=? WHERE id=" + jobId);
				ps.setLong(1, pos - 1);
				ps.executeUpdate();
			} finally {
				try { if (ps != null) ps.close(); } catch (final Exception e) { e.printStackTrace(); }
			}
		} catch (final Exception e) {
			e.printStackTrace();
		} finally {
			try { if (con != null) con.close(); } catch (final Exception e) { e.printStackTrace(); }
			try { if (out != null) out.close(); } catch (final Exception e) { e.printStackTrace(); }
			try { if (in != null) in.close(); } catch (final Exception e) { e.printStackTrace(); }
			try { if (fileSystem != null) fileSystem.close(); } catch (final Exception e) { e.printStackTrace(); }
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
				try { if (ps != null) ps.close(); } catch (final Exception e) { e.printStackTrace(); }
			}
		} catch (final Exception e) {
			e.printStackTrace();
		} finally {
			try { if (con != null) con.close(); } catch (final Exception e) { e.printStackTrace(); }
		}
	}
}
