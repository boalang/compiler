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
package boa.aggregators.ml;

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
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.*;
import weka.filters.Filter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import static boa.aggregators.ml.Util.*;

/**
 * A Boa ML aggregator to train models.
 *
 * @author ankuraga
 */
public abstract class MLAggregator extends Aggregator {
	protected ArrayList<Attribute> fvAttributes;
	protected Instances unFilteredInstances;
	protected int trainingPerc;
	protected Instances trainingSet;
	protected Instances testingSet;
	protected int NumOfAttributes;
	protected String[] options;
	protected boolean flag;
	protected ArrayList<String> nominalAttr;

	public MLAggregator() {
		initialize();
	}

	public MLAggregator(final String s) {
		initialize();
		try {
			options = handleNonWekaOptions(Utils.splitOptions(s));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void initialize() {
		fvAttributes = new ArrayList<Attribute>();
		nominalAttr = new ArrayList<String>();
		trainingPerc = 100;
	}

	// this function is used to handle non-weka options (-s: split)
	private String[] handleNonWekaOptions(String[] opts) {
		List<String> others = new ArrayList<>();
		for (int i = 0; i < opts.length; i++) {
			String cur = opts[i];
			if (cur.equals("-s"))
				trainingPerc = Integer.parseInt(opts[++i]);
			else
				others.add(opts[i]);
		}
		return others.toArray(new String[0]);
	}

	public void evaluate(Classifier model, Instances set) {
		try {
			Evaluation eval = new Evaluation(set);
			eval.evaluateModel(model, set);
			String setName = set == trainingSet ? "Training" : "Testing";
			collect(summary(eval, setName));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// TODO: this is not fixed
	public void saveTrainingSet(Object set) {
		FSDataOutputStream out = null;
		FileSystem fileSystem = null;
		Path filePath = null;
		ObjectOutputStream objectOut = null;

		try {
			JobContext context = (JobContext) getContext();
			Configuration configuration = context.getConfiguration();
			int boaJobId = configuration.getInt("boa.hadoop.jobid", 0);
			JobConf job = new JobConf(configuration);
			Path outputPath = FileOutputFormat.getOutputPath(job);
			fileSystem = outputPath.getFileSystem(context.getConfiguration());
			String output = null;
			String subpath = "_" + trainingSet.attribute(0).name() + "_";
			if (DefaultProperties.localOutput != null)
				output = DefaultProperties.localOutput;
			else
				output = configuration.get("fs.default.name", "hdfs://boa-njt/");
			for (int i = 1; i < NumOfAttributes; i++) {
				// System.out.println(trainingSet.attribute(0).name());
				subpath += "_" + trainingSet.attribute(i).name() + "_";
			}
			fileSystem.mkdirs(new Path(output, new Path("/model" + boaJobId)));
			filePath = new Path(output,
					new Path("/model" + boaJobId, new Path(("" + getKey()).split("\\[")[0] + subpath + "data")));

			if (fileSystem.exists(filePath))
				return;

			out = fileSystem.create(filePath);

			ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream();
			objectOut = new ObjectOutputStream(byteOutStream);
			objectOut.writeObject(set);
			byte[] serializedObject = byteOutStream.toByteArray();
			out.write(serializedObject);

			collect(filePath.toString());

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

	public void saveModel(Object model) {
		FSDataOutputStream out = null;
		FileSystem fileSystem = null;
		Path filePath = null;
		ObjectOutputStream objectOut = null;

		try {
			JobContext context = (JobContext) getContext();
			Configuration conf = context.getConfiguration();
			int boaJobId = conf.getInt("boa.hadoop.jobid", 0);
			JobConf job = new JobConf(conf);
			Path outputPath = FileOutputFormat.getOutputPath(job);
			fileSystem = outputPath.getFileSystem(context.getConfiguration());
			String output = DefaultProperties.localOutput != null
					? new Path(DefaultProperties.localOutput).toString() + "/../"
					: conf.get("fs.default.name", "hdfs://boa-njt/");
			fileSystem.mkdirs(getModelDirPath(output, boaJobId));
			filePath = getModelFilePath(output, boaJobId, getKey().getName());

			// delete previous model
			if (fileSystem.exists(filePath) && fileSystem.delete(filePath, true))
				System.out.println("Deleted previous model");

			out = fileSystem.create(filePath);
			ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream();
			objectOut = new ObjectOutputStream(byteOutStream);
			objectOut.writeObject(model);
			out.write(byteOutStream.toByteArray());

			collect("Model is saved to: " + filePath.toString());
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

	public static Path getModelDirPath(String output, int boaJobId) {
		return new Path(output, new Path("model/job_" + boaJobId));
	}

	public static Path getModelFilePath(String output, int boaJobId, String modelVar) {
		return new Path(getModelDirPath(output, boaJobId), new Path(modelVar + ".model"));
	}

	protected void applyFilterToUnfilteredInstances(Filter filter) throws Exception {
		unFilteredInstances = Filter.useFilter(unFilteredInstances, filter);
	}

	protected void applyFilterToUnfilteredInstances(Filter filter, Instances filteredInstances) throws Exception {
		unFilteredInstances = Filter.useFilter(unFilteredInstances, filter);
		moveFromUnFilteredToFiltered(filteredInstances);
	}

	protected void moveFromUnFilteredToFiltered(Instances filteredInstances) {
		int totalUnfilteredInstances = unFilteredInstances.numInstances();
		filteredInstances.addAll(unFilteredInstances.subList(0, totalUnfilteredInstances));
		unFilteredInstances.delete();
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
			NumOfAttributes = count;
			flag = true;
			trainingSet = new Instances(name, fvAttributes, 1);
			trainingSet.setClassIndex(NumOfAttributes - 1);
			testingSet = new Instances(name, fvAttributes, 1);
			testingSet.setClassIndex(NumOfAttributes - 1);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// create instance this tuple input data
	private void instanceCreation(Tuple data, Instances set) {
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
					if (NumberUtils.isNumber(String.valueOf(data.getValue(fieldNames[i]))))
						instance.setValue(fvAttributes.get(count),
								Double.parseDouble(String.valueOf(data.getValue(fieldNames[i]))));
					else
						instance.setValue(fvAttributes.get(count), String.valueOf(data.getValue(fieldNames[i])));
					count++;
				}
			}
			set.add(instance);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// create attributes with string[] input data
	private void attributeCreation(String[] data, String name) {
		if (flag == true)
			return;
		fvAttributes.clear();
		try {
			int classIdx = data.length - 1;
			for (int i = 0; i < data.length; i++)
				fvAttributes.add(new Attribute("Attribute" + i));
			trainingSet = new Instances(name, fvAttributes, 1);
			trainingSet.setClassIndex(classIdx);
			testingSet = new Instances(name, fvAttributes, 1);
			testingSet.setClassIndex(classIdx);
			flag = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// create instance this string[] input data
	private void instanceCreation(String[] data, Instances set) {
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
		instanceCreation(data, isTrainData() ? trainingSet : testingSet);
	}

	protected void aggregate(final Tuple data, final String metadata, String name)
			throws IOException, InterruptedException {
		attributeCreation(data, name);
		instanceCreation(data, isTrainData() ? trainingSet : testingSet);
	}

	private boolean isTrainData() {
		return Math.random() > (1 - trainingPerc / 100.0);
	}

	public abstract void aggregate(final Tuple data, final String metadata) throws IOException, InterruptedException;

	public abstract void aggregate(final String[] data, final String metadata) throws IOException, InterruptedException;

}