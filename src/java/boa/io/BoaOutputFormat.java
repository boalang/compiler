package boa.io;

import org.apache.hadoop.mapreduce.OutputCommitter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.output.FileOutputCommitter;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

public class BoaOutputFormat<K, V> extends TextOutputFormat<K, V> {
	private FileOutputCommitter committer = null;

	@Override
	public synchronized OutputCommitter getOutputCommitter(TaskAttemptContext context) throws java.io.IOException {
		if (committer == null)
			committer = new BoaOutputCommitter(getOutputPath(context), context);
		return committer;
	}
}
