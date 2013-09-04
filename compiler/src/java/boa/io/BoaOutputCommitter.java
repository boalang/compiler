package boa.io;

import java.io.*;
import java.sql.*;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
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
		storeOutput(context, jobId);
		updateStatus(false, jobId);
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
		FSDataOutputStream out = null;

		try {
			fileSystem = outputPath.getFileSystem(context.getConfiguration());

			con = DriverManager.getConnection(url, user, password);

			PreparedStatement ps = null;
			try {
				ps = con.prepareStatement("INSERT INTO boa_output (id, length) VALUES (" + jobId + ", 0)");
				ps.executeUpdate();
			} catch (final Exception e) {
			} finally {
				try { if (ps != null) ps.close(); } catch (final Exception e) { e.printStackTrace(); }
			}

			fileSystem.mkdirs(new Path("/boa", new Path("" + jobId)));
			out = fileSystem.create(new Path("/boa", new Path("" + jobId, new Path("output.txt"))));

			int partNum = 0;

			final byte[] b = new byte[67108864];
			int length = 0;

			while (true) {
				final Path path = new Path(outputPath, "part-r-" + String.format("%05d", partNum++));
				if (!fileSystem.exists(path))
					break;

				if (in != null)
					try { in.close(); } catch (final Exception e) { e.printStackTrace(); }
				in = fileSystem.open(path);

				int numBytes = 0;

				while ((numBytes = in.read(b)) > 0) {
					out.write(b, 0, numBytes);
					length += numBytes;

					this.context.progress();
				}
			}

			try {
				ps = con.prepareStatement("UPDATE boa_output SET length=? WHERE id=" + jobId);
				ps.setLong(1, length);
				ps.executeUpdate();
			} finally {
				try { if (ps != null) ps.close(); } catch (final Exception e) { e.printStackTrace(); }
			}
		} catch (final Exception e) {
			e.printStackTrace();
		} finally {
			try { if (con != null) con.close(); } catch (final Exception e) { e.printStackTrace(); }
			try { if (in != null) in.close(); } catch (final Exception e) { e.printStackTrace(); }
			try { if (out != null) out.close(); } catch (final Exception e) { e.printStackTrace(); }
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
