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
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instances;

public class MLSeqCombiner {

	private FileSystem fs;
	private Configuration conf;
	private Path modelDirPath;
	private Path modelSeqPath;

	private Instances trainInstances;
	private Instances testInstances;

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
		setPaths();
		buildSeqFiles();
		System.out.println("finish combining " + modelCount + " models");

		StringBuilder sb = new StringBuilder();
		sb.append("\n====== Trained and Combined " + modelCount + " Models ======\n\n");
		MyVote v = new MyVote(modelSeqPath);
		sb.append(v.toString());

		if (trainInstances != null && trainInstances.numInstances() != 0) {
			System.out.println("start to evaluate train data " + trainInstances.numInstances());
			sb.append(evaluate(v, trainInstances));
			System.out.println("finish evaluating train data");			
		}

		if (testInstances != null && testInstances.numInstances() != 0) {
			System.out.println("start to evaluate train data " + testInstances.numInstances());
			sb.append(evaluate(v, testInstances));
			System.out.println("finish evaluating test data");			
		}

		return sb.toString();
	}

	private void buildSeqFiles() {
		openWriters();
		modelCount = 0;
		for (final EmitValue value : values) {
			String meta = value.getMetadata();
			if (meta != null) {
				if (meta.equals("model_path")) {
					String modelPath = value.getData()[0];
					try {
						BytesWritable bw = new BytesWritable(getBytes(modelPath));
						modelWriter.append(new Text(String.valueOf(modelCount++)), bw);
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else if (meta.equals("train")) {
					if (trainInstances == null)
						trainInstances = new Instances(value.getTrain());
					else
						trainInstances.addAll(value.getTrain());
				} else if (meta.equals("test")) {
					if (testInstances == null)
						testInstances = new Instances(value.getTest());
					else
						testInstances.addAll(value.getTest());
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

	public String evaluate(Classifier model, Instances set) {
		String res = "";
		try {
			Evaluation eval = new Evaluation(set);
			eval.evaluateModel(model, set);
			String s = trainInstances == set ? "Train" : "Test";
			res = eval.toSummaryString("\n====== " + s + " Dataset Evaluation ======\n", false);
			res += "\n" + eval.toClassDetailsString() + "\n";
		} catch (Exception e) {
			e.printStackTrace();
		}
		return res;
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