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
import weka.clusterers.ClusterEvaluation;
import weka.clusterers.Clusterer;
import weka.core.*;
import weka.filters.Filter;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static boa.functions.BoaUtilIntrinsics.*;

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
	protected boolean isClusterer;
	protected ArrayList<String> nominalAttr;
	
	public boolean trainMultipleModels;
	public String taskId;
	protected int minTrainSize = -1;

	public MLAggregator() {
		fvAttributes = new ArrayList<Attribute>();
		nominalAttr = new ArrayList<String>();
		trainingPerc = 100;
	}

	public MLAggregator(final String s) {
		this();
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
			collect(eval.toSummaryString("\n" + setName + "Set Evaluation:\n", false));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void evaluate(Clusterer clusterer, Instances set) {
		try {
			ClusterEvaluation eval = new ClusterEvaluation();
			eval.setClusterer(clusterer);
			set.setClassIndex(set.numAttributes() - 1);
			eval.evaluateClusterer(set);
			String setName = set == trainingSet ? "Training" : "Testing";
			collect("\n" + setName + "Set Evaluation:\n" + eval.clusterResultsToString());
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
			Configuration conf = context.getConfiguration();
			int boaJobId = conf.getInt("boa.hadoop.jobid", 0);
			JobConf job = new JobConf(conf);
			Path outputPath = FileOutputFormat.getOutputPath(job);
			fileSystem = outputPath.getFileSystem(context.getConfiguration());

			
			String output = null;
			String subpath = "_" + trainingSet.attribute(0).name() + "_";
			if (DefaultProperties.localOutput != null)
				output = DefaultProperties.localOutput;
			else
				output = conf.get("fs.default.name", "hdfs://boa-njt/");
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

	
	public void saveAndUpdateTrainingSet(LinkedList<String> train) {
		FSDataOutputStream out = null;
		FileSystem fs = null;
		Path modelDirPath = null;
		Path trainDataPath = null;
		ObjectOutputStream objectOut = null;
		
		try {
			JobContext context = (JobContext) getContext();
			Configuration conf = context.getConfiguration();
			int boaJobId = conf.getInt("boa.hadoop.jobid", 0);
			JobConf job = new JobConf(conf);
			Path outputPath = FileOutputFormat.getOutputPath(job);
			fs = outputPath.getFileSystem(context.getConfiguration());
			
			String output = DefaultProperties.localOutput != null
					? new Path(DefaultProperties.localOutput).toString() + "/../"
					: conf.get("fs.default.name", "hdfs://boa-njt/");
			
			modelDirPath = new Path(output, new Path("model/job_" + boaJobId));
			fs.mkdirs(modelDirPath);
			trainDataPath = new Path(modelDirPath, new Path(getKey().getName() + ".train"));
			
			if (!fs.exists(trainDataPath)) {
				out = fs.create(trainDataPath);
			} else {
				// only works under real hadoop namenode and datanode
				out = fs.append(trainDataPath);
			}
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out));
			for (String line : train) {
				bw.write(line);
				bw.newLine();
			}
			bw.close();
			train.clear();
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
		FileSystem fs = null;
		Path modelDirPath = null;
		Path modelPath = null;
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
			fs = outputPath.getFileSystem(context.getConfiguration());

			String output = DefaultProperties.localOutput != null
					? new Path(DefaultProperties.localOutput).toString() + "/../"
					: conf.get("fs.default.name", "hdfs://boa-njt/");

			modelDirPath = new Path(output, new Path("model/job_" + boaJobId));
			fs.mkdirs(modelDirPath);
			
			modelPath = new Path(modelDirPath, new Path(getKey().getName() + ".model"));
			if (trainMultipleModels) {
				int idx = 0;
				do {
					String modelName = getKey().getName() + "_" + taskId + "_" + idx++ + ".model";
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
			NumOfAttributes = count; // use NumOfAttributes for tuple data
			flag = true;
			trainingSet = new Instances(name, fvAttributes, 1);
			testingSet = new Instances(name, fvAttributes, 1);
			if (!isClusterer) {
				trainingSet.setClassIndex(NumOfAttributes - 1);
				testingSet.setClassIndex(NumOfAttributes - 1);				
			}
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
			for (int i = 0; i < data.length; i++)
				fvAttributes.add(new Attribute("Attribute" + i));
			trainingSet = new Instances(name, fvAttributes, 1);
			testingSet = new Instances(name, fvAttributes, 1);
			if (!isClusterer) {
				trainingSet.setClassIndex(data.length - 1); // use data length for String[] data
				testingSet.setClassIndex(data.length - 1);				
			}
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
	
	public boolean passThrough(int dataSize) {
		if (dataSize < MAX_RECORDS_FOR_SPILL / 900)
			return true;
		if (minTrainSize != -1 && dataSize < minTrainSize)
			return true;
		return false;
	}

	public abstract void aggregate(final Tuple data, final String metadata) throws IOException, InterruptedException;

	public abstract void aggregate(final String[] data, final String metadata) throws IOException, InterruptedException;

}