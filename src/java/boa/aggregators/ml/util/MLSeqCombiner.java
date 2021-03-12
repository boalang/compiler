package boa.aggregators.ml.util;

import java.io.IOException;
import org.apache.commons.io.IOUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.SequenceFile.CompressionType;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.DefaultCodec;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapreduce.Reducer;

import boa.datagen.DefaultProperties;
import boa.io.EmitKey;
import boa.io.EmitValue;

public class MLSeqCombiner {

	private FileSystem fs;
	private Configuration conf;
	private Path modelDirPath;
	private Path modelSeqPath;

	private Reducer<EmitKey, EmitValue, Text, NullWritable>.Context context;
	private Iterable<EmitValue> values;
	private EmitKey key;

	private int modelCount;
	private SequenceFile.Writer modelWriter;

	public MLSeqCombiner(EmitKey key, Iterable<EmitValue> values,
			Reducer<EmitKey, EmitValue, Text, NullWritable>.Context context) {
		this.key = key;
		this.values = values;
		this.context = context;
	}

	public String combine() {
		buildSeqFiles();
		System.out.println("finish combining " + modelCount + " models");
		StringBuilder sb = new StringBuilder();
		sb.append("\n====== Trained and Combined " + modelCount + " Models ======\n\n");
		return sb.toString();
	}

	private void buildSeqFiles() {
		setPaths();
		openWriters();
		modelCount = 0;
		for (final EmitValue value : values) {
			String meta = value.getMetadata();
			if (meta != null) {
				if (meta.equals("model_path")) {
					String modelPath = value.getData()[0];
					System.out.println(modelPath);
					try {
						BytesWritable bw = new BytesWritable(getBytes(modelPath));
						modelWriter.append(new Text(String.valueOf(modelCount++)), bw);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		closeWriters();
	}

	private byte[] getBytes(String modelPath) {
		Path path = new Path(modelPath);
		FSDataInputStream in = null;
		byte[] bytes = null;
		try {
			in = fs.open(path);
			bytes = IOUtils.toByteArray(in);
			fs.delete(path, true);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (in != null)
					in.close();
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}
		return bytes;
	}

	private void setPaths() {
		try {
			conf = context.getConfiguration();
			int boaJobId = conf.getInt("boa.hadoop.jobid", 0);
			JobConf job = new JobConf(conf);
			Path outputPath = FileOutputFormat.getOutputPath(job);
			fs = outputPath.getFileSystem(context.getConfiguration());
			String output = DefaultProperties.localOutput != null
					? new Path(DefaultProperties.localOutput).toString() + "/../"
					: conf.get("fs.default.name", "hdfs://boa-njt/");
			modelDirPath = new Path(output, new Path("model/job_" + boaJobId));
			modelSeqPath = new Path(modelDirPath, new Path(key.getName() + "_model.seq"));
			if (fs.exists(modelSeqPath))
				fs.delete(modelSeqPath, true);
			fs.mkdirs(modelDirPath);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void openWriters() {
		CompressionType compType = CompressionType.BLOCK;
		CompressionCodec compCode = new DefaultCodec();
		try {
			System.out.println("build writers");
			modelWriter = SequenceFile.createWriter(fs, conf, modelSeqPath, Text.class, BytesWritable.class, compType,
					compCode);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void closeWriters() {
		try {
			modelWriter.close();
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
}