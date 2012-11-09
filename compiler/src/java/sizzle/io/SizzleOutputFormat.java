package sizzle.io;

import org.apache.hadoop.mapreduce.OutputCommitter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.output.FileOutputCommitter;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

public class SizzleOutputFormat<K, V> extends TextOutputFormat<K, V> {
	private FileOutputCommitter committer = null;

	@Override
	public synchronized OutputCommitter getOutputCommitter(TaskAttemptContext context) throws java.io.IOException {
		if (committer == null)
			committer = new SizzleOutputCommitter(getOutputPath(context), context);
		return committer;
	}
}
