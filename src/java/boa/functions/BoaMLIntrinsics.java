package boa.functions;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import boa.datagen.DefaultProperties;
import boa.types.ml.BoaLinearRegression;
import boa.types.ml.BoaModel;
import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

import static boa.aggregators.ml.MLAggregator.getModelFilePath;

public class BoaMLIntrinsics {
	
	/**
	 * Given the model URL, deserialize the model and return Model type
	 *
	 * @param Take URL for the model
	 * @return Model type after deserializing
	 */
	@FunctionSpec(name = "load", returnType = "LinearRegression", formalParameters = {"int", "string"})
	public static BoaModel load(final long jobId, final String modelVar, final Object o) throws Exception {
		Object unserializedObject = null;
		FSDataInputStream in = null;
		ObjectInputStream dataIn = null;
		ByteArrayOutputStream bo = null;
		try {
			final Configuration conf = BoaAstIntrinsics.context.getConfiguration();
			final FileSystem fs;
			final Path p;
			String output = DefaultProperties.localOutput != null ? new Path(DefaultProperties.localOutput).toString() + "/../"
					: conf.get("fs.default.name", "hdfs://boa-njt/");
			p = getModelFilePath(output, (int) jobId, modelVar);
			fs = FileSystem.get(conf);
			in = fs.open(p);
			
			final byte[] b = new byte[(int)fs.getLength(p) + 1];
			int c = 0;
			bo = new ByteArrayOutputStream();
			while((c = in.read(b)) != -1){
				bo.write(b, 0, c);
			}
			ByteArrayInputStream bin = new ByteArrayInputStream(bo.toByteArray());
			dataIn = new ObjectInputStream(bin);
			unserializedObject = dataIn.readObject();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		finally {
			try {
				if (in != null) in.close();
				if (dataIn != null) dataIn.close();
				if (bo != null) bo.close();
			} catch (final Exception e) { e.printStackTrace(); }
		}

		Classifier clr = (Classifier)unserializedObject;
		BoaModel m = null;
		if(clr.toString().contains("Linear Regression")){
			m = new BoaLinearRegression(clr, o);
		}
		return m;
	}
	
	
	@FunctionSpec(name = "classify", returnType = "string", formalParameters = { "Model","array of int"})
	public static String classify(final BoaModel model, final long[] vector) throws Exception {
		int NumOfAttributes = vector.length + 1;
		ArrayList<Attribute> fvAttributes = new ArrayList<Attribute>();
	
		for(int i=0; i < NumOfAttributes - 1; i++) {
			fvAttributes.add(new Attribute("Attribute" + i));
		}

		Instances testingSet = new Instances("Classifier", fvAttributes, 1);
		testingSet.setClassIndex(testingSet.numAttributes() - 1);
		Instance instance = new DenseInstance(NumOfAttributes - 1);
		for(int i=0; i<NumOfAttributes-1; i++) {
			instance.setValue((Attribute)fvAttributes.get(i), vector[i]);
		}
		testingSet.add(instance);

		Classifier classifier = (Classifier) model.getClassifier();
		double predval = classifier.classifyInstance(testingSet.instance(0));
		
		if(testingSet.classAttribute().isNominal())
			return testingSet.classAttribute().value((int) predval);
		else
			return predval + "";
	}
	
	@FunctionSpec(name = "train_split", returnType = "int", formalParameters = { "int"})
	public static Long trainSplit(final long percent) throws Exception {
		return (long) (Math.random() > (1 - percent / 100.0) ? 1 : 0);
	}

}
