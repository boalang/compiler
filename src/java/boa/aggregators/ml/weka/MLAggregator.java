/*
 * Copyright 2020, Hridesh Rajan, Robert Dyer, 
 *                 and Iowa State University of Science and Technology
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
package boa.aggregators.ml.weka;

import boa.aggregators.Aggregator;

import boa.datagen.DefaultProperties;
import boa.runtime.Tuple;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapreduce.JobContext;
import weka.core.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * A Boa ML aggregator to train models.
 *
 * @author ankuraga
 * @author hyj
 */
public abstract class MLAggregator extends Aggregator {
	protected ArrayList<Attribute> fvAttributes = new ArrayList<Attribute>();
	protected Instances instances;
	protected int NumOfAttributes;
	protected String[] options;
	protected boolean flag;
	protected boolean isClusterer;

	public boolean trainWithCombiner;
	public Path modelPath;

	public boolean incrementalLearning;

	public MLAggregator() {
	}

	public MLAggregator(final String s) {
		try {
			options = handleNonWekaOptions(Utils.splitOptions(s));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// this function is used to handle non-weka options (-s: split)
	private String[] handleNonWekaOptions(String[] opts) {
		List<String> others = new ArrayList<>();
		for (int i = 0; i < opts.length; i++) {
			String cur = opts[i];
			if (cur.equals("-en"))
				trainWithCombiner = true;
			else if (cur.equals("-el"))
				incrementalLearning = true;
			else
				others.add(opts[i]);
		}
		return others.toArray(new String[0]);
	}

	// create attributes with tuple input data
	protected void attributeCreation(Tuple data, final String name) {
		if (flag == true)
			return;
		fvAttributes.clear();
		try {
			String[] fieldNames = data.getFieldNames();
			int count = 0;
			for (int i = 0; i < fieldNames.length; i++) {
				if (data.getValue(fieldNames[i]).getClass().isEnum()) {
					ArrayList<String> fvNominalVal = new ArrayList<String>();
					for (Object obj : data.getValue(fieldNames[i]).getClass().getEnumConstants())
						fvNominalVal.add(obj.toString());
					fvAttributes.add(new Attribute("Nominal" + count, fvNominalVal));
					count++;
				} else if (data.getValue(fieldNames[i]).getClass().isArray()) {
					int l = Array.getLength(data.getValue(fieldNames[i])) - 1;
					for (int j = 0; j <= l; j++) {
						fvAttributes.add(new Attribute("Attribute" + count));
						count++;
					}
				} else {
					fvAttributes.add(new Attribute("Attribute" + count));
					count++;
				}
			}
			NumOfAttributes = count; // use NumOfAttributes for tuple data
			flag = true;
			instances = new Instances(name, fvAttributes, 1);
			if (!isClusterer) {
				instances.setClassIndex(NumOfAttributes - 1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// create instance this tuple input data
	protected void instanceCreation(Tuple data, Instances set) {
		try {
			int count = 0;
			Instance instance = new DenseInstance(NumOfAttributes);
			String[] fieldNames = data.getFieldNames();
			for (int i = 0; i < fieldNames.length; i++) {
				if (data.getValue(fieldNames[i]).getClass().isEnum()) {
					instance.setValue(fvAttributes.get(count), String.valueOf(data.getValue(fieldNames[i])));
					count++;
				} else if (data.getValue(fieldNames[i]).getClass().isArray()) {
					int x = Array.getLength(data.getValue(fieldNames[i])) - 1;
					Object o = data.getValue(fieldNames[i]);
					for (int j = 0; j <= x; j++) {
						instance.setValue(fvAttributes.get(count), Double.parseDouble(String.valueOf(Array.get(o, j))));
						count++;
					}
				} else {
					String val = String.valueOf(data.getValue(fieldNames[i]));
					if (NumberUtils.isNumber(val))
						instance.setValue(fvAttributes.get(count), Double.parseDouble(val));
					else
						instance.setValue(fvAttributes.get(count), val);
					count++;
				}
			}
			set.add(instance);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// create attributes with string[] input data
	protected void attributeCreation(String[] data, String name) {
		if (flag == true)
			return;
		fvAttributes.clear();
		try {
			for (int i = 0; i < data.length; i++)
				fvAttributes.add(new Attribute("Attribute" + i));
			instances = new Instances(name, fvAttributes, 1);
//			testingSet = new Instances(name, fvAttributes, 1);
			if (!isClusterer) {
				instances.setClassIndex(data.length - 1); // use data length for String[] data
//				testingSet.setClassIndex(data.length - 1);
			}
			flag = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// create instance this string[] input data
	protected void instanceCreation(String[] data, Instances set) {
		try {
			Instance instance = new DenseInstance(data.length);
			for (int i = 0; i < data.length; i++)
				instance.setValue(fvAttributes.get(i), Double.parseDouble(data[i]));
			set.add(instance);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void aggregate(final String[] data, final String metadata, String name)
			throws IOException, InterruptedException {
		attributeCreation(data, name);
		instanceCreation(data, instances);
	}

	protected void aggregate(final Tuple data, final String metadata, String name)
			throws IOException, InterruptedException {
		attributeCreation(data, name);
		instanceCreation(data, instances);
	}

	public abstract void aggregate(final Tuple data, final String metadata) throws IOException, InterruptedException;

	public abstract void aggregate(final String[] data, final String metadata) throws IOException, InterruptedException;

	public void saveModel(Object model) {
		FSDataOutputStream out = null;
		ObjectOutputStream objectOut = null;

		try {
			// serialize the model and write to the path
			ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream();
			objectOut = new ObjectOutputStream(byteOutStream);
			objectOut.writeObject(model);

			// make path
			JobContext context = (JobContext) getContext();
			Configuration conf = context.getConfiguration();
			int boaJobId = conf.getInt("boa.hadoop.jobid", 0);
			JobConf job = new JobConf(conf);
			Path outputPath = FileOutputFormat.getOutputPath(job);
			FileSystem fs = outputPath.getFileSystem(context.getConfiguration());

			String output = DefaultProperties.localOutput != null
					? new Path(DefaultProperties.localOutput).toString() + "/../"
					: conf.get("fs.default.name", "hdfs://boa-njt/");

			Path modelDirPath = new Path(output, new Path("model/job_" + boaJobId));
			fs.mkdirs(modelDirPath);

			modelPath = new Path(modelDirPath, new Path(getKey().getName() + ".model"));
			if (trainWithCombiner) {
				int idx = 0;
				do {
					String modelName = getKey().getName() + "_" + getContext().getTaskAttemptID().getTaskID().toString()
							+ "_" + idx++ + ".model";
					modelPath = new Path(modelDirPath, new Path(modelName));
				} while (fs.exists(modelPath));
			}
			out = fs.create(modelPath, true); // overwrite: true
			out.write(byteOutStream.toByteArray());

			System.out.println("Model is saved to: " + modelPath.toString());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (out != null)
					out.close();
				if (objectOut != null)
					objectOut.close();
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void collectToOutput(String s) {
		try {
			this.collect(s);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}