/*
 * Copyright 2014, Hridesh Rajan, Robert Dyer, 
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

import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;

import boa.aggregators.Aggregator;
import boa.datagen.DefaultProperties;

import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.FileOutputFormat;

import weka.classifiers.Evaluation;
import weka.classifiers.Classifier;
import weka.core.Instances;

/**
 * A Boa ML aggregator to train models.
 * 
 * @author ankuraga
 * @author giang
 */
public abstract class MLAggregator extends Aggregator {
	
	public MLAggregator() {
	}
	
	public MLAggregator(final String s) {
		super(s);
	}
	
	public void evaluate(Classifier model, Instances trainingSet) {
		try {
		 Evaluation evaluation = new Evaluation(trainingSet);
		 evaluation.evaluateModel(model, trainingSet);
		 this.collect("  Training set evaluation \n " + evaluation.toSummaryString()); 
		}
		catch (Exception e) {
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

			fileSystem.mkdirs(new Path(DefaultProperties.HADOOP_OUT_LOCATION, new Path("" + boaJobId)));
			filePath = new Path(DefaultProperties.HADOOP_OUT_LOCATION, new Path("" + boaJobId, new Path(("" + getKey()).split("\\[")[0] + System.currentTimeMillis() + "data")));
			
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
			} catch (final Exception e) { e.printStackTrace(); }
		}
	}
	
	public void saveModel(Object model) {
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

			fileSystem.mkdirs(new Path(DefaultProperties.HADOOP_OUT_LOCATION, new Path("" + boaJobId)));
			filePath = new Path(DefaultProperties.HADOOP_OUT_LOCATION, new Path("" + boaJobId, new Path(("" + getKey()).split("\\[")[0] + System.currentTimeMillis() + "ML.model")));
			
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
			} catch (final Exception e) { e.printStackTrace(); }
		}
	}
	
	/** {@inheritDoc} */
	@Override
	public abstract void aggregate(final String data, final String metadata) throws NumberFormatException, IOException, InterruptedException;
	
}
