package sizzle.io;

import java.io.*;
import java.sql.*;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.lib.output.FileOutputCommitter;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.JobStatus;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

public class SizzleOutputCommitter extends FileOutputCommitter {
	public SizzleOutputCommitter(Path output, TaskAttemptContext context) throws java.io.IOException {
		super(output, context);
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
		updateStatus(true, context.getConfiguration().getInt("boa.hadoop.jobid", 0));
	}

	static String url = "jdbc:mysql://boa-head:3306/drupal";
	static String user = "drupal";
	static String password = "";

	private void updateStatus(boolean error, int jobId) {
		Connection con = null;
		try {
			con = DriverManager.getConnection(url, user, password);
			PreparedStatement ps = null;
			try {
				ps = con.prepareStatement("UPDATE boa_jobs SET hadoop_end=CURRENT_TIMESTAMP(), hadoop_status=? WHERE id=" + jobId);
				ps.setInt(1, error ? -1 : 2);
				ps.executeUpdate();
			} finally {
				try { if (ps != null) ps.close(); } catch (Exception e) { e.printStackTrace(); }
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try { if (con != null) con.close(); } catch (Exception e) { e.printStackTrace(); }
		}
	}

	private void storeOutput(JobContext context, int jobId) {
		Connection con = null;
		FileSystem fileSystem = null;
		FSDataInputStream in = null;
		ByteArrayOutputStream out = null;
		Configuration conf = context.getConfiguration();

		try {
			fileSystem = FileSystem.get(conf);

			String file = "hdfs://boa-nn1" + conf.get("boa.hadoop.output", "") + "/part-r-00000";
			Path path = new Path(file);
			if (!fileSystem.exists(path)) {
				System.out.println("File " + file + " does not exists");
				return;
			}

			in = fileSystem.open(path);
			out = new ByteArrayOutputStream();

			con = DriverManager.getConnection(url, user, password);
			PreparedStatement ps = null;
			try {
				ps = con.prepareStatement("INSERT INTO boa_output (id, result) VALUES (" + jobId + ", '')");
				ps.executeUpdate();
			} catch (Exception e) {
			} finally {
				try { if (ps != null) ps.close(); } catch (Exception e) { e.printStackTrace(); }
			}

			try {
				ps = con.prepareStatement("UPDATE boa_output SET result=INSERT(result, ?, ?, ?) WHERE id=" + jobId);

				byte[] b = new byte[4096];
				int numBytes = 0;
				int pos = 1;

				while ((numBytes = in.read(b)) > 0) {
					out.write(b, 0, numBytes);
					if (out.size() >= 4194304) {
						ps.setInt(1, pos);
						ps.setInt(2, out.size());
						ps.setString(3, out.toString());
						ps.executeUpdate();

						pos += out.size();
						out.reset();
					}
				}

				if (out.size() > 0) {
					ps.setInt(1, pos);
					ps.setInt(2, out.size());
					ps.setString(3, out.toString());
					ps.executeUpdate();

					pos += out.size();
				}

				ps = con.prepareStatement("UPDATE boa_output SET length=? WHERE id=" + jobId);
				ps.setInt(1, pos - 1);
				ps.executeUpdate();
			} finally {
				try { if (ps != null) ps.close(); } catch (Exception e) { e.printStackTrace(); }
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try { if (con != null) con.close(); } catch (Exception e) { e.printStackTrace(); }
			try { if (out != null) out.close(); } catch (Exception e) { e.printStackTrace(); }
			try { if (in != null) in.close(); } catch (Exception e) { e.printStackTrace(); }
			try { if (fileSystem != null) fileSystem.close(); } catch (Exception e) { e.printStackTrace(); }
		}
	}

	public static void setJobID(String id, int jobId) {
		Connection con = null;
		try {
			con = DriverManager.getConnection(url, user, password);
			PreparedStatement ps = null;
			try {
				ps = con.prepareStatement("UPDATE boa_jobs SET hadoop_id=? WHERE id=" + jobId);
				ps.setString(1, id);
				ps.executeUpdate();
			} finally {
				try { if (ps != null) ps.close(); } catch (Exception e) { e.printStackTrace(); }
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try { if (con != null) con.close(); } catch (Exception e) { e.printStackTrace(); }
		}
	}
}
