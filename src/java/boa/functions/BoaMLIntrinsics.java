package boa.functions;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
//import org.deeplearning4j.models.embeddings.reader.impl.BasicModelUtils;
//import org.deeplearning4j.models.sequencevectors.SequenceVectors;
//import org.deeplearning4j.models.word2vec.VocabWord;
//import org.deeplearning4j.models.word2vec.Word2Vec;

import boa.aggregators.ml.util.KMeans;
import boa.aggregators.ml.util.Ensemble;
import boa.datagen.DefaultProperties;
import boa.runtime.Tuple;
import boa.types.ml.*;
import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

public class BoaMLIntrinsics {

	public static final Configuration conf = BoaAstIntrinsics.context.getConfiguration();

	/**
	 * Given the model URL, deserialize the model and return Model type
	 *
	 * @param Take URL for the model
	 * @return Model type after deserializing
	 */
	@FunctionSpec(name = "load", returnType = "model", formalParameters = { "int", "model" })
	public static BoaModel load(final long jobId, BoaModel m, final String identifier, final String type,
			final Object o) {

		Path p = getModelPath(jobId, identifier);
		Object object = p.getName().endsWith(".seq") ? new Ensemble(p) : deserialize(p);

//		if (object instanceof Word2Vec) {
//			Word2Vec word2Vec = (Word2Vec) object;
//			word2Vec.setModelUtils(new BasicModelUtils<>());
//			return new BoaWord2Vec(word2Vec, o);
//		} else if (object instanceof SequenceVectors) {
//			@SuppressWarnings("unchecked")
//			SequenceVectors<VocabWord> seq2vec = (SequenceVectors<VocabWord>) object;
//			seq2vec.setModelUtils(new BasicModelUtils<>());
//			return new BoaSequence2Vec(seq2vec, o);
//		} else 
		if (object instanceof Classifier) {
			// classifier
			Classifier clr = (Classifier) object;
			if (type.contains("LinearRegression")) {
				m = new BoaLinearRegression(clr, o);
			} else if (type.contains("AdaBoostM1")) {
				m = new BoaAdaBoostM1(clr, o);
			} else if (type.contains("ZeroR")) {
				m = new BoaZeroR(clr, o);
			} else if (type.contains("Vote")) {
				m = new BoaVote(clr, o);
			} else if (type.contains("SMO")) {
				m = new BoaSMO(clr, o);
			} else if (type.contains("RandomForest")) {
				m = new BoaRandomForest(clr, o);
			} else if (type.contains("AdditiveRegression")) {
				m = new BoaAdditiveRegression(clr, o);
			} else if (type.contains("AttributeSelectedClassifier")) {
				m = new BoaAttributeSelectedClassifier(clr, o);
			} else if (type.contains("PART")) {
				m = new BoaPART(clr, o);
			} else if (type.contains("OneR")) {
				m = new BoaOneR(clr, o);
			} else if (type.contains("NaiveBayesMultinomialUpdateable")) {
				m = new BoaNaiveBayesMultinomialUpdateable(clr, o);
			} else if (type.contains("BoaNaiveBayesMultinomial")) {
				// TODO BoaNaiveBayesMultinomial
				m = new BoaNaiveBayesMultinomial(clr, o);
			} else if (type.contains("NaiveBayes")) {
				m = new BoaNaiveBayes(clr, o);
			} else if (type.contains("MultiScheme")) {
				m = new BoaMultiScheme(clr, o);
			} else if (type.contains("MultiClassClassifier")) {
				m = new BoaMultiClassClassifier(clr, o);
			} else if (type.contains("MultilayerPerceptron")) {
				m = new BoaMultilayerPerceptron(clr, o);
			} else if (type.contains("Bagging")) {
				m = new BoaBagging(clr, o);
			} else if (type.contains("BayesNet")) {
				m = new BoaBayesNet(clr, o);
			} else if (type.contains("ClassificationViaRegression")) {
				m = new BoaClassificationViaRegression(clr, o);
			} else if (type.contains("LWL")) {
				m = new BoaLWL(clr, o);
			} else if (type.contains("LogitBoost")) {
				m = new BoaLogitBoost(clr, o);
			} else if (type.contains("LMT")) {
				m = new BoaLMT(clr, o);
			} else if (type.contains("Logistic")) {
				m = new BoaLogisticRegression(clr, o);
			} else if (type.contains("J48")) {
				m = new BoaJ48(clr, o);
			} else if (type.contains("JRip")) {
				m = new BoaJRip(clr, o);
			} else if (type.contains("KStar")) {
				m = new BoaKStar(clr, o);
			} else if (type.contains("CVParameterSelection")) {
				m = new BoaCVParameterSelection(clr, o);
			} else if (type.contains("DecisionStump")) {
				m = new BoaDecisionStump(clr, o);
			} else if (type.contains("DecisionTable")) {
				m = new BoaDecisionTable(clr, o);
			} else if (type.contains("FilteredClassifier")) {
				m = new BoaFilteredClassifier(clr, o);
			} else if (type.contains("GaussianProcesses")) {
				m = new BoaGaussianProcesses(clr, o);
			} else if (type.contains("InputMappedClassifier")) {
				m = new BoaInputMappedClassifier(clr, o);
			}
		} else if (object instanceof KMeans) {
			m = new BoaSimpleKMeans((KMeans) object, o);
		}

		// TODO attribute selection:
		// TODO PrincipalComponents
		// TODO LSA
		return m;
	}

	@FunctionSpec(name = "load", returnType = "model", formalParameters = { "int", "model", "string" })
	public static BoaModel load(final long jobId, BoaModel m, final String rule, final String identifier,
			final String type, final Object o) {
		m = load(jobId, m, identifier, type, o);
		Classifier c = m.getClassifier();
		if (c instanceof Ensemble)
			((Ensemble) c).setCombinationRule(rule);
		return m;
	}

	public static Path getModelPath(long jobId, String identifier) {
		String output = DefaultProperties.localOutput != null
				? new Path(DefaultProperties.localOutput).toString() + "/../"
				: conf.get("fs.default.name", "hdfs://boa-njt/");

		Path modelDirPath = new Path(output, new Path("model/job_" + jobId));
		Path singleModelPath = new Path(modelDirPath, new Path(identifier + ".model"));
		Path ensembleModelPath = new Path(modelDirPath, new Path(identifier + "_model.seq"));
		try {
			final FileSystem fs = FileSystem.get(conf);
			if (fs.exists(singleModelPath))
				return singleModelPath;
			if (fs.exists(ensembleModelPath))
				return ensembleModelPath;
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return null;
	}

	public static Object deserialize(Path p) {
		Object object = null;
		FSDataInputStream in = null;
		ObjectInputStream dataIn = null;
		ByteArrayOutputStream bo = null;
		ByteArrayInputStream bin = null;
		try {
			final FileSystem fs = FileSystem.get(conf);
			in = fs.open(p);
			final byte[] b = new byte[(int) fs.getFileStatus(p).getLen() + 1];
			int c = 0;
			bo = new ByteArrayOutputStream();
			while ((c = in.read(b)) != -1)
				bo.write(b, 0, c);
			bin = new ByteArrayInputStream(bo.toByteArray());
			object = new ObjectInputStream(bin).readObject();
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
		return object;
	}

	@FunctionSpec(name = "classify", returnType = "string", formalParameters = { "model", "array of int" })
	public static String classify(final BoaModel model, final long[] vector) throws Exception {
		return model.classify(vector);
	}

	@FunctionSpec(name = "classify", returnType = "string", formalParameters = { "model", "tuple" })
	public static String classify(final BoaModel model, final Tuple vector) throws Exception {
		return model.classify(vector);
	}

	@FunctionSpec(name = "classify", returnType = "string", formalParameters = { "model", "array of string" })
	public static String classify(final BoaModel model, final String[] vector) throws Exception {
		BoaSimpleKMeans m = (BoaSimpleKMeans) model;
		ArrayList<Attribute> fvAttributes = m.getAttributes();
		Instances testingSet = new Instances("Classifier", fvAttributes, 1);

		Instance instance = new DenseInstance(vector.length);
		for (int i = 0; i < vector.length; i++)
			if (NumberUtils.isNumber(vector[i]))
				instance.setValue(fvAttributes.get(i), Double.parseDouble(vector[i]));
			else
				instance.setValue(fvAttributes.get(i), vector[i]);
		testingSet.add(instance);

		int res = m.getClusterer().clusterInstance(testingSet.instance(0));
		return String.valueOf(res);
	}

	/* ------------------------------- Word2Vec ------------------------------- */
	// for more methods please check:
	// https://github.com/eclipse/deeplearning4j/blob/43fd64358cd96413063a06e63b7d34402555a3ec/deeplearning4j/deeplearning4j-nlp-parent/deeplearning4j-nlp/src/main/java/org/deeplearning4j/models/embeddings/wordvectors/WordVectorsImpl.java
	// https://github.com/eclipse/deeplearning4j/blob/43fd64358cd96413063a06e63b7d34402555a3ec/deeplearning4j/deeplearning4j-nlp-parent/deeplearning4j-nlp/src/main/java/org/deeplearning4j/models/embeddings/reader/impl/BasicModelUtils.java

//	@FunctionSpec(name = "sim", returnType = "float", formalParameters = { "Word2Vec", "string", "string" })
//	public static double sim(final BoaWord2Vec m, final String w1, final String w2) {
//		return m.getW2v().similarity(w1, w2);
//	}
//
//	@FunctionSpec(name = "nearest", returnType = "array of string", formalParameters = { "Word2Vec", "string", "int" })
//	public static String[] nearest(final BoaWord2Vec m, final String w, final long num) {
//		return m.getW2v().wordsNearest(w, (int) num).toArray(new String[0]);
//	}
//
//	@FunctionSpec(name = "vector", returnType = "array of float", formalParameters = { "Word2Vec", "string" })
//	public static double[] vector(final BoaWord2Vec m, final String w) {
//		return m.getW2v().getWordVector(w);
//	}
//
//	@FunctionSpec(name = "arith", returnType = "array of string", formalParameters = { "Word2Vec", "string", "int" })
//	public static String[] arith(final BoaWord2Vec m, final String exp, final long num) {
//		List<String> plus = new LinkedList<String>();
//		List<String> minus = new LinkedList<String>();
//		String[] tokens = exp.split("\\s+");
//		for (int i = 0; i < tokens.length; i++) {
//			String token = tokens[i];
//			if (i == 0 && !token.equals("-") && !token.equals("+"))
//				plus.add(token);
//			else if (token.equals("+"))
//				plus.add(tokens[++i]);
//			else if (token.equals("-"))
//				minus.add(tokens[++i]);
//		}
//		return m.getW2v().wordsNearest(plus, minus, (int) num).toArray(new String[0]);
//	}

	/* ------------------------------- Seq2Vec ------------------------------- */

//	// TODO: ensemble
//	@FunctionSpec(name = "sim", returnType = "float", formalParameters = { "Seq2Vec", "string", "string" })
//	public static double sim(final BoaSequence2Vec m, final String w1, final String w2) {
//		return m.getSeq2Vec().similarity(w1, w2);
//	}
//
//	// TODO: ensemble
//	@FunctionSpec(name = "nearest", returnType = "array of string", formalParameters = { "Seq2Vec", "string", "int" })
//	public static String[] nearest(final BoaSequence2Vec m, final String w, final long num) {
//		return m.getSeq2Vec().wordsNearest(w, (int) num).toArray(new String[0]);
//	}
//
//	// TODO: ensemble
//	@FunctionSpec(name = "vector", returnType = "array of float", formalParameters = { "Seq2Vec", "string" })
//	public static double[] vector(final BoaSequence2Vec m, final String w) {
//		return m.getSeq2Vec().getWordVector(w);
//	}
//
//	@FunctionSpec(name = "vector", returnType = "array of float", formalParameters = { "Seq2Vec", "array of string" })
//	public static double[] vector(final BoaSequence2Vec m, final String[] seq) {
//
//		if (m.getSeq2Vec() != null)
//			return m.getSeq2Vec().getWordVectorsMean(Arrays.asList(seq)).toDoubleVector();
//
//		if (m.getPaths().length != 0)
//			return m.vector(seq);
//
//		return null;
//	}
//
//	@FunctionSpec(name = "vector", returnType = "array of float", formalParameters = { "Seq2Vec", "queue of string" })
//	public static double[] vector(final BoaSequence2Vec m, final LinkedList<String> seq) {
//
//		if (m.getSeq2Vec() != null)
//			return m.getSeq2Vec().getWordVectorsMean(seq).toDoubleVector();
//
//		if (m.getPaths().length != 0)
//			return m.vector(seq);
//
//		return null;
//	}
//
//	// TODO: ensemble
//	@FunctionSpec(name = "arith", returnType = "array of string", formalParameters = { "Seq2Vec", "string", "int" })
//	public static String[] arith(final BoaSequence2Vec m, final String exp, final long num) {
//		List<String> plus = new LinkedList<String>();
//		List<String> minus = new LinkedList<String>();
//		String[] tokens = exp.split("\\s+");
//		for (int i = 0; i < tokens.length; i++) {
//			String token = tokens[i];
//			if (i == 0 && !token.equals("-") && !token.equals("+"))
//				plus.add(token);
//			else if (token.equals("+"))
//				plus.add(tokens[++i]);
//			else if (token.equals("-"))
//				minus.add(tokens[++i]);
//		}
//		return m.getSeq2Vec().wordsNearest(plus, minus, (int) num).toArray(new String[0]);
//	}

}