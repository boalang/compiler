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
import boa.aggregators.FinishedException;
import boa.compiler.ast.statements.EmitStatement;
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
import java.util.Arrays;

/**
 * A Boa ML aggregator to train models.
 *
 * @author ankuraga
 */
public abstract class MLAggregator extends Aggregator {
	protected final ArrayList<Attribute> fvAttributes;
	protected Instances unFilteredInstances;
	protected ArrayList<String> vector;
	protected Instances trainingSet;
	protected Instances testingSet;
	protected int NumOfAttributes;
	protected String[] options;
	protected boolean flag;
	protected boolean classification = false;
	protected boolean regression = false;
	protected ArrayList<String> nominalAttr = new ArrayList<String>();
	protected int count;
	private int vectorSize;
	private String mlarg;


	public MLAggregator() {
		this.fvAttributes = new ArrayList<Attribute>();
		this.vector = new ArrayList<String>();
	}

	public MLAggregator(final String s) {
		this.mlarg = s;
		this.fvAttributes = new ArrayList<Attribute>();
		this.vector = new ArrayList<String>();
		try {
			options = Utils.splitOptions(s);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void evaluate(Classifier model, Instances set) {
		try {
			Evaluation eval = new Evaluation(set);
			eval.evaluateModel(model, set);
			String name = set == trainingSet ? "Training" : "Testing";
			this.collect(eval.toSummaryString("\n" + name + "Set Evaluation:\n", false));
//			System.out.println("Correct % = " + eval.pctCorrect());
//			System.out.println("Incorrect % = " + eval.pctIncorrect());
//			System.out.println("AUC % = " + eval.areaUnderROC(1));
//			System.out.println("Kappa % = " + eval.kappa());
//			System.out.println("MAE % = " + eval.meanAbsoluteError());
//			System.out.println("RMSE % = " + eval.rootMeanSquaredError());
//			System.out.println("RAE % = " + eval.relativeAbsoluteError());
//			System.out.println("RRSE % = " + eval.rootRelativeSquaredError());
//			System.out.println("Precision = " + eval.precision(1));
//			System.out.println("Recall = " + eval.recall(1));
//			System.out.println("fMeasure = " + eval.fMeasure(1));
//			System.out.println("Error Rate = " + eval.errorRate());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void saveTrainingSet(Object trainingSet) {
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
			String subpath = "_" + this.trainingSet.attribute(0).name()+ "_";
			if (DefaultProperties.localOutput != null)
				output = DefaultProperties.localOutput;
			else
				output = configuration.get("fs.default.name", "hdfs://boa-njt/");
			for (int i = 1; i < NumOfAttributes; i ++) {
				//System.out.println(this.trainingSet.attribute(0).name());
				subpath += "_" + this.trainingSet.attribute(i).name() + "_" ;
			}
			fileSystem.mkdirs(new Path(output, new Path("/model" + boaJobId)));
			filePath = new Path(output, new Path("/model" + boaJobId, new Path(("" + getKey()).split("\\[")[0] + subpath + "data")));

			if (fileSystem.exists(filePath))
				return;
			
			out = fileSystem.create(filePath);

			ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream();
			objectOut = new ObjectOutputStream(byteOutStream);
			objectOut.writeObject(trainingSet);
			byte[] serializedObject = byteOutStream.toByteArray();

			out.write(serializedObject);

			this.collect(filePath.toString());

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (out != null) out.close();
				if (objectOut != null) objectOut.close();
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
		EmitStatement n = null;

		try {
			JobContext context = (JobContext) getContext();
			Configuration conf = context.getConfiguration();
			int boaJobId = conf.getInt("boa.hadoop.jobid", 0);
			JobConf job = new JobConf(conf);
			Path outputPath = FileOutputFormat.getOutputPath(job);	
			fileSystem = outputPath.getFileSystem(context.getConfiguration());
			String output = DefaultProperties.localOutput != null ? new Path(DefaultProperties.localOutput).toString() + "/../"
					: conf.get("fs.default.name", "hdfs://boa-njt/");
			fileSystem.mkdirs(getModelDirPath(output, boaJobId));
			filePath = getModelFilePath(output, boaJobId, this.getKey().getName());

			if (fileSystem.exists(filePath))
				return;

			out = fileSystem.create(filePath);

			ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream();
			objectOut = new ObjectOutputStream(byteOutStream);
			objectOut.writeObject(model);
			byte[] serializedObject = byteOutStream.toByteArray();

			out.write(serializedObject);
			
			this.collect(filePath.toString());

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (out != null) out.close();
				if (objectOut != null) objectOut.close();
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

	/**
	 * {@inheritDoc}
	 */	
	@Override
	public abstract void aggregate(final String data, final String metadata) throws NumberFormatException, IOException, InterruptedException;

	protected void attributeCreation(Tuple data, final String name) {
		this.fvAttributes.clear();
		try {
			String[] fieldNames = data.getFieldNames();
			int count = 0;
			for (int i = 0; i < fieldNames.length; i++) {
				if (data.getValue(fieldNames[i]).getClass().isEnum()) {
					ArrayList<String> fvNominalVal = new ArrayList<String>();
					for (Object obj : data.getValue(fieldNames[i]).getClass().getEnumConstants())
						fvNominalVal.add(obj.toString());
					this.fvAttributes.add(new Attribute("Nominal" + count, fvNominalVal));
					count++;
				} else if (data.getValue(fieldNames[i]).getClass().isArray()) {
					int l = Array.getLength(data.getValue(fieldNames[i])) - 1;
					for (int j = 0; j <= l; j++) {
						this.fvAttributes.add(new Attribute("Attribute" + count));
						count++;
					}
				} else {
					this.fvAttributes.add(new Attribute("Attribute" + count));
					count++;
				}
			}
			this.NumOfAttributes = count;
			this.flag = true;
			this.trainingSet = new Instances(name, this.fvAttributes, 1);
			this.trainingSet.setClassIndex(this.NumOfAttributes - 1);
			this.testingSet = new Instances(name, this.fvAttributes, 1);
			this.testingSet.setClassIndex(this.NumOfAttributes - 1);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void instanceCreation(ArrayList<String> data, Instances set) {
		try {
			Instance instance = new DenseInstance(this.NumOfAttributes);
			for (int i = 0; i < this.NumOfAttributes; i++) {
				try {
					Attribute attr = (Attribute) this.fvAttributes.get(i);
					instance.setValue(attr, Double.parseDouble(data.get(i)));
				}
				catch(NumberFormatException e) {
					Attribute attr = (Attribute) this.fvAttributes.get(i);
					instance.setValue(attr, String.valueOf(data.get(i)));
				}
			}
			set.add(instance);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void instanceCreation(Tuple data) {
		try {
			int count = 0;
			Instance instance = new DenseInstance(this.NumOfAttributes);
			String[] fieldNames = data.getFieldNames();
			for (int i = 0; i < fieldNames.length; i++) {
				if (data.getValue(fieldNames[i]).getClass().isEnum()) {
					instance.setValue((Attribute) this.fvAttributes.get(count), String.valueOf(data.getValue(fieldNames[i])));
					count++;
				} else if (data.getValue(fieldNames[i]).getClass().isArray()) {
					int x = Array.getLength(data.getValue(fieldNames[i])) - 1;
					Object o = data.getValue(fieldNames[i]);
					for (int j = 0; j <= x; j++) {
						instance.setValue((Attribute) this.fvAttributes.get(count), Double.parseDouble(String.valueOf(Array.get(o, j))));
						count++;
					}
				} else {	
					if (NumberUtils.isNumber(String.valueOf(data.getValue(fieldNames[i]))))
						instance.setValue((Attribute) this.fvAttributes.get(count), Double.parseDouble(String.valueOf(data.getValue(fieldNames[i]))));
					else
						instance.setValue((Attribute) this.fvAttributes.get(count), String.valueOf(data.getValue(fieldNames[i])));
					count++;
				}
			}
			trainingSet.add(instance);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void attributeCreation(String name) {
		fvAttributes.clear();
		NumOfAttributes = this.getVectorSize() - 1;
		try {
			for (int i = 0; i < NumOfAttributes - 1; i++)
				fvAttributes.add(new Attribute("Attribute" + i));
			if (classification == true) 
				fvAttributes.add(new Attribute("nominal", nominalAttr));
			else
				fvAttributes.add(new Attribute("Attribute" + NumOfAttributes));
			this.flag = true;
			trainingSet = new Instances(name, fvAttributes, 1);
			trainingSet.setClassIndex(NumOfAttributes - 1);
			testingSet = new Instances(name, fvAttributes, 1);
			testingSet.setClassIndex(NumOfAttributes - 1);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void aggregate(final String data, final String metadata, String name) throws IOException, InterruptedException {
		if (name == "AdaBoostM1")
			classification = true;
		else if (name == "LinearRegression")
			regression = true;
		if (this.count < this.getVectorSize() - 1)
        	this.vector.add(data);
		if (this.mlarg != null) {
			String[] arrOfStr = this.mlarg.split("/"); 
			nominalAttr = new ArrayList<String>(Arrays.asList(arrOfStr));
		}
		count++;
		if (count == NumOfAttributes) 
			if (!nominalAttr.contains(data)) 
				nominalAttr.add(data);
			
        if (this.count == this.getVectorSize()) {
        	if (this.flag != true) 
            	attributeCreation(name);
        	
        	Instances set = data.equals("1") ? trainingSet : testingSet;
            instanceCreation(this.vector, set);
            this.vector = new ArrayList<String>();
            this.count = 0;
        }
    }

    protected void aggregate(final Tuple data, final String metadata, String name) throws IOException, InterruptedException {
    	if (this.flag != true)
        	attributeCreation(data, name);
        instanceCreation(data);
    }

	public void aggregate(final Tuple data, final String metadata) throws IOException, InterruptedException, FinishedException, IllegalAccessException {	
	}

	public void aggregate(final Tuple data) throws IOException, InterruptedException, FinishedException, IllegalAccessException {
		this.aggregate(data, null);
	}
	
	public int getVectorSize() {
		return this.vectorSize;
	}

	public void setVectorSize(int vectorSize) {
		this.vectorSize = vectorSize;
	}
}
