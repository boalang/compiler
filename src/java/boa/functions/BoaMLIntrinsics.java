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
import boa.types.ml.*;
import weka.classifiers.Classifier;
import weka.clusterers.Clusterer;
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
	@FunctionSpec(name = "load", returnType = "model", formalParameters = { "int", "model" })
	public static BoaModel load(final long jobId, BoaModel m, final String identifier, final Object o)
			throws Exception {
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
			p = getModelFilePath(output, (int) jobId, identifier);
			fs = FileSystem.get(conf);
			in = fs.open(p);

			final byte[] b = new byte[(int) fs.getFileStatus(p).getLen() + 1];
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

		if (unserializedObject instanceof Classifier) {
			// classifier
			Classifier clr = (Classifier) unserializedObject;
			String className = clr.getClass().toString();
			if (className.contains("LinearRegression")) {
				m = new BoaLinearRegression(clr, o);
			} else if (className.contains("AdaBoostM1")) {
				m = new BoaAdaBoostM1(clr, o);
			} else if (className.contains("ZeroR")) {
				m = new BoaZeroR(clr, o);
			} else if (className.contains("Vote")) {
				m = new BoaVote(clr, o);
			} else if (className.contains("SMO")) {
				m = new BoaSMO(clr, o);
			} else if (className.contains("RandomForest")) {
				m = new BoaRandomForest(clr, o);
			} else if (className.contains("AdditiveRegression")) {
				m = new BoaAdditiveRegression(clr, o);
			} else if (className.contains("AttributeSelectedClassifier")) {
				m = new BoaAttributeSelectedClassifier(clr, o);
			} else if (className.contains("PART")) {
				m = new BoaPART(clr, o);
			} else if (className.contains("OneR")) {
				m = new BoaOneR(clr, o);
			} else if (className.contains("NaiveBayesMultinomialUpdateable")) {
				m = new BoaNaiveBayesMultinomialUpdateable(clr, o);
			} else if (className.contains("BoaNaiveBayesMultinomial")) {
				// TODO BoaNaiveBayesMultinomial
				m = new BoaNaiveBayesMultinomial(clr, o);
			} else if (className.contains("NaiveBayes")) {
				m = new BoaNaiveBayes(clr, o);
			} else if (className.contains("MultiScheme")) {
				m = new BoaMultiScheme(clr, o);
			} else if (className.contains("MultiClassClassifier")) {
				m = new BoaMultiClassClassifier(clr, o);
			} else if (className.contains("MultilayerPerceptron")) {
				m = new BoaMultilayerPerceptron(clr, o);
			} else if (className.contains("Bagging")) {
				m = new BoaBagging(clr, o);
			} else if (className.contains("BayesNet")) {
				m = new BoaBayesNet(clr, o);
			} else if (className.contains("ClassificationViaRegression")) {
				m = new BoaClassificationViaRegression(clr, o);
			} else if (className.contains("LWL")) {
				m = new BoaLWL(clr, o);
			} else if (className.contains("LogitBoost")) {
				m = new BoaLogitBoost(clr, o);
			} else if (className.contains("LMT")) {
				m = new BoaLMT(clr, o);
			} else if (className.contains("Logistic")) {
				m = new BoaLogisticRegression(clr, o);
			} else if (className.contains("J48")) {
				m = new BoaJ48(clr, o);
			} else if (className.contains("JRip")) {
				m = new BoaJRip(clr, o);
			} else if (className.contains("KStar")) {
				m = new BoaKStar(clr, o);
			} else if (className.contains("CVParameterSelection")) {
				m = new BoaCVParameterSelection(clr, o);
			} else if (className.contains("DecisionStump")) {
				m = new BoaDecisionStump(clr, o);
			} else if (className.contains("DecisionTable")) {
				m = new BoaDecisionTable(clr, o);
			} else if (className.contains("FilteredClassifier")) {
				m = new BoaFilteredClassifier(clr, o);
			} else if (className.contains("GaussianProcesses")) {
				m = new BoaGaussianProcesses(clr, o);
			} else if (className.contains("InputMappedClassifier")) {
				m = new BoaInputMappedClassifier(clr, o);
			}
		} else if (unserializedObject instanceof Clusterer) {
			// Clusterer
			Clusterer clu = (Clusterer) unserializedObject;
			String className = clu.getClass().toString();
			if (className.contains("SimpleKMeans")) {
				m = new BoaSimpleKMeans(clu, o);
			}
		}

		// TODO attribute selection:
		// TODO PrincipalComponents
		// TODO LSA
		return m;
	}

	@FunctionSpec(name = "classify", returnType = "string", formalParameters = { "model", "array of int" })
	public static String classify(final BoaModel model, final long[] vector) throws Exception {
		int NumOfAttributes = vector.length + 1;
		ArrayList<Attribute> fvAttributes = getAttributes(model, vector);

		Instances testingSet = new Instances("Classifier", fvAttributes, 1);
		testingSet.setClassIndex(NumOfAttributes - 1);

		Instance instance = new DenseInstance(NumOfAttributes);
		for (int i = 0; i < NumOfAttributes - 1; i++)
			instance.setValue((Attribute) fvAttributes.get(i), vector[i]);
		testingSet.add(instance);

		Classifier classifier = (Classifier) model.getClassifier();
		double predval = classifier.classifyInstance(testingSet.instance(0));

		return testingSet.classAttribute().isNominal() ? testingSet.classAttribute().value((int) predval)
				: String.valueOf(predval);
	}

	@FunctionSpec(name = "cluster", returnType = "string", formalParameters = { "model", "array of int" })
	public static String cluster(final BoaModel model, final long[] vector) throws Exception {
		int NumOfAttributes = vector.length + 1;
		ArrayList<Attribute> fvAttributes = getAttributes(model, vector);

		Instances testingSet = new Instances("Clusterer", fvAttributes, 1);
		testingSet.setClassIndex(NumOfAttributes - 1);

		Instance instance = new DenseInstance(NumOfAttributes);
		for (int i = 0; i < NumOfAttributes - 1; i++)
			instance.setValue((Attribute) fvAttributes.get(i), vector[i]);
		testingSet.add(instance);

		Clusterer clusterer = (Clusterer) model.getClusterer();
		double predval = clusterer.clusterInstance(testingSet.instance(0));

		return testingSet.classAttribute().isNominal() ? testingSet.classAttribute().value((int) predval)
				: String.valueOf(predval);
	}

	private static ArrayList<Attribute> getAttributes(BoaModel model, long[] vector) {
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
		return fvAttributes;
	}

	@FunctionSpec(name = "classify", returnType = "string", formalParameters = { "model", "tuple" })
	public static String classify(final BoaModel model, final Tuple vector) throws Exception {
		ArrayList<Attribute> fvAttributes = getAttributes(model, vector);
		int NumOfAttributes = fvAttributes.size();

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

		return testingSet.classAttribute().isNominal() ? testingSet.classAttribute().value((int) predval)
				: String.valueOf(predval);
	}

	@FunctionSpec(name = "cluster", returnType = "string", formalParameters = { "model", "tuple" })
	public static String cluster(final BoaModel model, final Tuple vector) throws Exception {
		ArrayList<Attribute> fvAttributes = getAttributes(model, vector);
		int NumOfAttributes = fvAttributes.size();

		Instances testingSet = new Instances("Classifier", fvAttributes, 1);
		testingSet.setClassIndex(NumOfAttributes - 1);

		Instance instance = new DenseInstance(NumOfAttributes);

		for (int i = 0; i < NumOfAttributes - 1; i++)
			if (NumberUtils.isNumber(vector.getValues()[i]))
				instance.setValue((Attribute) fvAttributes.get(i), Double.parseDouble(vector.getValues()[i]));
			else
				instance.setValue((Attribute) fvAttributes.get(i), vector.getValues()[i]);
		testingSet.add(instance);

		Clusterer clusterer = (Clusterer) model.getClusterer();
		double predval = clusterer.clusterInstance(testingSet.instance(0));

		return testingSet.classAttribute().isNominal() ? testingSet.classAttribute().value((int) predval)
				: String.valueOf(predval);
	}

	private static ArrayList<Attribute> getAttributes(BoaModel model, Tuple vector) {
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
		} catch (Exception e) {
			e.printStackTrace();
		}
		return fvAttributes;
	}

}