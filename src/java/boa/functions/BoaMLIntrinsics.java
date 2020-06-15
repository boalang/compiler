package boa.functions;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import boa.datagen.DefaultProperties;
import boa.runtime.Tuple;
import boa.types.ml.BoaAdaBoostM1;
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
	@FunctionSpec(name = "load", returnType = "LinearRegression", formalParameters = { "int", "string" })
	public static BoaModel load(final long jobId, final String modelVar, final Object o) throws Exception {
		Object unserializedObject = null;
		FSDataInputStream in = null;
		ObjectInputStream dataIn = null;
		ByteArrayOutputStream bo = null;
		try {
			final Configuration conf = BoaAstIntrinsics.context.getConfiguration();
			final FileSystem fs;
			final Path p;
			String output = DefaultProperties.localOutput != null
					? new Path(DefaultProperties.localOutput).toString() + "/../"
					: conf.get("fs.default.name", "hdfs://boa-njt/");
			p = getModelFilePath(output, (int) jobId, modelVar);
			fs = FileSystem.get(conf);
			in = fs.open(p);

			final byte[] b = new byte[(int) fs.getLength(p) + 1];
			int c = 0;
			bo = new ByteArrayOutputStream();
			while ((c = in.read(b)) != -1) {
				bo.write(b, 0, c);
			}
			ByteArrayInputStream bin = new ByteArrayInputStream(bo.toByteArray());
			dataIn = new ObjectInputStream(bin);
			unserializedObject = dataIn.readObject();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (in != null)
					in.close();
				if (dataIn != null)
					dataIn.close();
				if (bo != null)
					bo.close();
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}

		Classifier clr = (Classifier) unserializedObject;
		BoaModel m = null;
		if (clr.toString().contains("Linear Regression")) {
			m = new BoaLinearRegression(clr, o);
		} else if (clr.toString().contains("AdaBoostM1")) {
			m = new BoaAdaBoostM1(clr, o);
		}
		return m;
	}

	@FunctionSpec(name = "classify", returnType = "string", formalParameters = { "model", "array of int" })
	public static String classify(final BoaModel model, final long[] vector) throws Exception {
		int NumOfAttributes = vector.length + 1;
		ArrayList<Attribute> fvAttributes = new ArrayList<Attribute>();
		for (int i = 0; i < NumOfAttributes - 1; i++)
			fvAttributes.add(new Attribute("Attribute" + i));

		try {
			Field[] fields = ((Tuple) model.getObject()).getClass().getDeclaredFields();
			Field lastfield = fields[fields.length - 1];
			if (lastfield.getType().isEnum()) {
				ArrayList<String> fvNominalVal = new ArrayList<String>();
				for (Object obj : lastfield.getType().getEnumConstants())
					fvNominalVal.add(obj.toString());
				fvAttributes.add(new Attribute("Nominal" + (NumOfAttributes - 1), fvNominalVal));
			} else {
				fvAttributes.add(new Attribute("Attribute" + (NumOfAttributes - 1)));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		Instances testingSet = new Instances("Classifier", fvAttributes, 1);
		testingSet.setClassIndex(NumOfAttributes - 1);

		Instance instance = new DenseInstance(NumOfAttributes);
		for (int i = 0; i < NumOfAttributes - 1; i++)
			instance.setValue((Attribute) fvAttributes.get(i), vector[i]);
		testingSet.add(instance);

		Classifier classifier = (Classifier) model.getClassifier();
		double predval = classifier.classifyInstance(testingSet.instance(0));

		if (testingSet.classAttribute().isNominal())
			return testingSet.classAttribute().value((int) predval);
		else
			return predval + "";
	}

	@FunctionSpec(name = "classify", returnType = "string", formalParameters = { "model", "tuple" })
	public static String classify(final BoaModel model, final Tuple vector) throws Exception {
		int NumOfAttributes = 0;
		ArrayList<Attribute> fvAttributes = new ArrayList<Attribute>();
		try {
			String[] fieldNames = vector.getFieldNames();
			int count = 0;
			for (int i = 0; i < fieldNames.length; i++) {
				if (vector.getValue(fieldNames[i]).getClass().isEnum()) {
					ArrayList<String> fvNominalVal = new ArrayList<String>();
					for (Object obj : vector.getValue(fieldNames[i]).getClass().getEnumConstants())
						fvNominalVal.add(obj.toString());
					fvAttributes.add(new Attribute("Nominal" + count, fvNominalVal));
					count++;
				} else if (vector.getValue(fieldNames[i]).getClass().isArray()) {
					int l = Array.getLength(vector.getValue(fieldNames[i])) - 1;
					for (int j = 0; j <= l; j++) {
						fvAttributes.add(new Attribute("Attribute" + count));
						count++;
					}
				} else {
					fvAttributes.add(new Attribute("Attribute" + count));
					count++;
				}
			}

			Field[] fields = ((Tuple) model.getObject()).getClass().getDeclaredFields();
			Field lastfield = fields[fields.length - 1];
			if (lastfield.getType().isEnum()) {
				ArrayList<String> fvNominalVal = new ArrayList<String>();
				for (Object obj : lastfield.getType().getEnumConstants())
					fvNominalVal.add(obj.toString());
				fvAttributes.add(new Attribute("Nominal" + count, fvNominalVal));
				count++;
			} else {
				fvAttributes.add(new Attribute("Attribute" + count));
				count++;
			}

			NumOfAttributes = count;
		} catch (Exception e) {
			e.printStackTrace();
		}

		Instances testingSet = new Instances("Classifier", fvAttributes, 1);
		testingSet.setClassIndex(NumOfAttributes - 1);

		Instance instance = new DenseInstance(NumOfAttributes);

		for (int i = 0; i < NumOfAttributes - 1; i++)
			if (NumberUtils.isNumber(vector.getValues()[i]))
				instance.setValue((Attribute) fvAttributes.get(i), Double.parseDouble(vector.getValues()[i]));
			else
				instance.setValue((Attribute) fvAttributes.get(i), vector.getValues()[i]);
		testingSet.add(instance);

		Classifier classifier = (Classifier) model.getClassifier();
		double predval = classifier.classifyInstance(testingSet.instance(0));

		if (testingSet.classAttribute().isNominal())
			return testingSet.classAttribute().value((int) predval);
		else
			return predval + "";
	}

}
