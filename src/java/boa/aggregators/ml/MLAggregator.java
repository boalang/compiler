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

import boa.BoaTup;
import boa.aggregators.Aggregator;
import boa.datagen.DefaultProperties;
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
    protected int NumOfAttributes;
    protected String[] options;
    protected boolean flag;
    protected int count;

    public MLAggregator() {
        this.fvAttributes = new ArrayList<Attribute>();
        this.vector = new ArrayList<String>();
    }

    public MLAggregator(final String s) {
        super(s);
        this.fvAttributes = new ArrayList<Attribute>();
        this.vector = new ArrayList<String>();
        try {
            options = Utils.splitOptions(s);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void evaluate(Classifier model, Instances trainingSet) {
        try {
            Evaluation evaluation = new Evaluation(trainingSet);
            evaluation.evaluateModel(model, trainingSet);
            this.collect("  Training set evaluation \n " + evaluation.toSummaryString());
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

            if (DefaultProperties.localOutput != null) {
                fileSystem.mkdirs(new Path(DefaultProperties.localOutput, new Path("" + boaJobId)));
                filePath = new Path(DefaultProperties.localOutput, new Path("" + boaJobId, new Path(("" + getKey()).split("\\[")[0] + System.currentTimeMillis() + "data")));
            } else {
                fileSystem.mkdirs(new Path(DefaultProperties.HADOOP_OUT_LOCATION, new Path("" + boaJobId)));
                filePath = new Path(DefaultProperties.HADOOP_OUT_LOCATION, new Path("" + boaJobId, new Path(("" + getKey()).split("\\[")[0] + System.currentTimeMillis() + "data")));
            }

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
        try {
            JobContext context = (JobContext) getContext();
            Configuration configuration = context.getConfiguration();
            int boaJobId = configuration.getInt("boa.hadoop.jobid", 0);
            JobConf job = new JobConf(configuration);
            Path outputPath = FileOutputFormat.getOutputPath(job);
            fileSystem = outputPath.getFileSystem(context.getConfiguration());

            if (DefaultProperties.localOutput != null) {
                fileSystem.mkdirs(new Path(DefaultProperties.localOutput, new Path("" + boaJobId)));
                filePath = new Path(DefaultProperties.localOutput, new Path("" + boaJobId, new Path(("" + getKey()).split("\\[")[0] + System.currentTimeMillis() + "ML.model")));
            } else {
                fileSystem.mkdirs(new Path(DefaultProperties.HADOOP_OUT_LOCATION, new Path("" + boaJobId)));
                filePath = new Path(DefaultProperties.HADOOP_OUT_LOCATION, new Path("" + boaJobId, new Path(("" + getKey()).split("\\[")[0] + System.currentTimeMillis() + "ML.model")));
            }


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
        while (totalUnfilteredInstances-- > 0) {
            unFilteredInstances.remove(0);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract void aggregate(final String data, final String metadata) throws NumberFormatException, IOException, InterruptedException;

    protected void attributeCreation(BoaTup data, final String name) {
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void instanceCreation(ArrayList<String> data) {
        try {
            Instance instance = new DenseInstance(this.NumOfAttributes);
        	
            for (int i = 0; i < this.NumOfAttributes; i++) {
                instance.setValue((Attribute) this.fvAttributes.get(i), Double.parseDouble(data.get(i)));
            }
            	
            trainingSet.add(instance);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void instanceCreation(BoaTup data) {
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
    	System.out.println(this.getVectorSize());
        fvAttributes.clear();
        NumOfAttributes = this.getVectorSize();
        try {
            for (int i = 0; i < NumOfAttributes; i++) {
                fvAttributes.add(new Attribute("Attribute" + i));
            }

            this.flag = true;
            trainingSet = new Instances(name, fvAttributes, 1);
            trainingSet.setClassIndex(NumOfAttributes - 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void aggregate(final String data, final String metadata, String
            name) throws IOException, InterruptedException {
        if (this.count != this.getVectorSize()) {

            this.vector.add(data);
            this.count++;
        }

        if (this.count == this.getVectorSize()) {
            if (this.flag != true)
                attributeCreation(name);
            instanceCreation(this.vector);
            this.vector = new ArrayList<String>();
            this.count = 0;
        }
    }

    protected void aggregate(final BoaTup data, final String metadata, String
            name) throws IOException, InterruptedException {
        if (this.flag != true)
            attributeCreation(data, name);
        instanceCreation(data);
    }
}
