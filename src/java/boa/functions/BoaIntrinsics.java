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
package boa.functions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Array;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FSDataInputStream;

import boa.types.Code.CodeRepository;
import boa.types.Code.Revision;
import boa.types.Diff.ChangedFile;
import boa.types.Toplevel.Project;
import boa.types.ml.*;

import weka.core.Attribute;
import weka.core.Instance;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.classifiers.Classifier;

/**
 * Boa domain-specific functions.
 * 
 * @author rdyer
 * @author ankuraga
 */
public class BoaIntrinsics {
	private final static String[] fixingRegex = {
		"\\bfix(s|es|ing|ed)?\\b",
		"\\b(error|bug|issue)(s)?\\b",
		//"\\b(bug|issue|fix)(s)?\\b\\s*(#)?\\s*[0-9]+",
		//"\\b(bug|issue|fix)\\b\\s*id(s)?\\s*(=)?\\s*[0-9]+"
	};

	private final static List<Matcher> fixingMatchers = new ArrayList<Matcher>();

	static {
		for (final String s : BoaIntrinsics.fixingRegex)
			fixingMatchers.add(Pattern.compile(s).matcher(""));
	}

	/**
	 * Is a Revision's log message indicating it is a fixing revision?
	 * 
	 * @param rev the revision to mine
	 * @return true if the revision's log indicates a fixing revision
	 */
	@FunctionSpec(name = "isfixingrevision", returnType = "bool", formalParameters = { "Revision" })
	public static boolean isfixingrevision(final Revision rev) {
		return isfixingrevision(rev.getLog());
	}

	/**
	 * Is a log message indicating it is a fixing revision?
	 * 
	 * @param log the revision's log message to mine
	 * @return true if the log indicates a fixing revision
	 */
	@FunctionSpec(name = "isfixingrevision", returnType = "bool", formalParameters = { "string" })
	public static boolean isfixingrevision(final String log) {
		final String lower = log.toLowerCase();
		for (final Matcher m : fixingMatchers)
			if (m.reset(lower).find())
				return true;

		return false;
	}

	/**
	 * Does a Project contain a file of the specified type? This compares based on file extension.
	 * 
	 * @param p the Project to examine
	 * @param ext the file extension to look for
	 * @return true if the Project contains at least 1 file with the specified extension
	 */
	@FunctionSpec(name = "hasfiletype", returnType = "bool", formalParameters = { "Project", "string" })
	public static boolean hasfile(final Project p, final String ext) {
		for (int i = 0; i < p.getCodeRepositoriesCount(); i++)
			if (hasfile(p.getCodeRepositories(i), ext))
				return true;
		return false;
	}

	/**
	 * Does a CodeRepository contain a file of the specified type? This compares based on file extension.
	 * 
	 * @param cr the CodeRepository to examine
	 * @param ext the file extension to look for
	 * @return true if the CodeRepository contains at least 1 file with the specified extension
	 */
	@FunctionSpec(name = "hasfiletype", returnType = "bool", formalParameters = { "CodeRepository", "string" })
	public static boolean hasfile(final CodeRepository cr, final String ext) {
		for (int i = 0; i < cr.getRevisionsCount(); i++)
			if (hasfile(cr.getRevisions(i), ext))
				return true;
		return false;
	}

	/**
	 * Does a Revision contain a file of the specified type? This compares based on file extension.
	 * 
	 * @param rev the Revision to examine
	 * @param ext the file extension to look for
	 * @return true if the Revision contains at least 1 file with the specified extension
	 */
	@FunctionSpec(name = "hasfiletype", returnType = "bool", formalParameters = { "Revision", "string" })
	public static boolean hasfile(final Revision rev, final String ext) {
		for (int i = 0; i < rev.getFilesCount(); i++)
			if (rev.getFiles(i).getName().toLowerCase().endsWith("." + ext.toLowerCase()))
				return true;
		return false;
	}

	/**
	 * Matches a FileKind enum to the given string.
	 * 
	 * @param s the string to match against
	 * @param kind the FileKind to match
	 * @return true if the string matches the given kind
	 */
	@FunctionSpec(name = "iskind", returnType = "bool", formalParameters = { "string", "FileKind" })
	public static boolean iskind(final String s, final ChangedFile.FileKind kind) {
		return kind.name().startsWith(s);
	}

	/**
	 * Given the model URL, deserialize the model and return Model type
	 *
	 * @param Take URL for the model
	 * @return Model type after deserializing
	 */
	@FunctionSpec(name = "load", returnType = "Model", formalParameters = {"string"})
	public static BoaModel load(final String URL, final Object o) throws Exception {
		Object unserializedObject = null;
		FSDataInputStream in = null;
		ObjectInputStream dataIn = null;
		ByteArrayOutputStream bo = null;
		try {
			final Configuration conf = new Configuration();
			final FileSystem fileSystem = FileSystem.get(conf);
			final Path path = new Path("hdfs://master" + URL);
			in = fileSystem.open(path);
			
			final byte[] b = new byte[(int)fileSystem.getLength(path) + 1];
		    
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

		if(clr.toString().contains("AdaBoostM1")){
			m = new BoaAdaBoostM1(clr, o);
		}
		else if(clr.toString().contains("Additive Regression")){
			m = new BoaAdditiveRegression(clr, o);
		}
		else if(clr.toString().contains("AttributeSelectedClassifier")){
			m = new BoaAttributeSelectedClassifier(clr, o);
		}
		else if(clr.toString().contains("Bagging")){
			m = new BoaBagging(clr, o);
		}
		else if(clr.toString().contains("Bayes Network Classifier")){
			m = new BoaBayesNet(clr, o);
		}
		else if(clr.toString().contains("Classification via Regression")){
			m = new BoaClassificationViaRegression(clr, o);
		}
		else if(clr.toString().contains("Cross-validated Parameter selection")){
			m = new BoaCVParameterSelection(clr, o);
		}
		else if(clr.toString().contains("Decision Stump")){
			m = new BoaDecisionStump(clr, o);
		}
		else if(clr.toString().contains("Decision Table")){
			m = new BoaDecisionTable(clr, o);
		}
		else if(clr.toString().contains("FilteredClassifier")){
			m = new BoaFilteredClassifier(clr, o);
		}
		else if(clr.toString().contains("Gaussian Processes")){
			m = new BoaGaussianProcesses(clr, o);
		}
		else if(clr.toString().contains("NB adaptive")){
			m = new BoaHoeffdingTree(clr, o);
		}
		else if(clr.toString().contains("IB1 instance-based classifier")){
			m = new BoaIBk(clr, o);
		}
		else if(clr.toString().contains("InputMappedClassifier")){
			m = new BoaInputMappedClassifier(clr, o);
		}
		else if(clr.toString().contains("LogitBoost: Base classifiers and their weights")){
			m = new BoaIterativeClassifierOptimizer(clr, o);
		}
		else if(clr.toString().contains("J48")){
			m = new BoaJ48(clr, o);
		}
		else if(clr.toString().contains("JRIP")){
			m = new BoaJRip(clr, o);
		}
		else if(clr.toString().contains("KStar")){
			m = new BoaKStar(clr, o);
		}
		else if(clr.toString().contains("Linear Regression")){
			m = new BoaLinearRegression(clr, o);
		}
		else if(clr.toString().contains("Logistic model tree")){
			m = new BoaLMT(clr, o);
		}
		else if(clr.toString().contains("Logistic Regression")){
			m = new BoaLogisticRegression(clr, o);
		}
		else if(clr.toString().contains("LogitBoost")){
			m = new BoaLogitBoost(clr, o);
		}
		else if(clr.toString().contains("Locally weighted learning")){
			m = new BoaLWL(clr, o);
		}
		else if(clr.toString().contains("MultiClassClassifier")){
			m = new BoaMultiClassClassifier(clr, o);
		}
		else if(clr.toString().contains("Sigmoid Node")){
			m = new BoaMultilayerPerceptron(clr, o);
		}
		else if(clr.toString().contains("MultiScheme")){
			m = new BoaMultiScheme(clr, o);
		}
		else if(clr.toString().contains("Naive Bayes Classifier")){
			m = new BoaNaiveBayes(clr, o);
		}
		else if(clr.toString().contains("The independent probability")){
			m = new BoaNaiveBayesMultinomial(clr, o);
		}
		else if(clr.toString().contains("The independent frequency")){
			m = new BoaNaiveBayesMultinomialUpdateable(clr, o);
		}
		else if(clr.toString().contains("PART")){
			m = new BoaPART(clr, o);
		}
		else if(clr.toString().contains("RandomForest")){
			m = new BoaRandomForest(clr, o);
		}
		else if(clr.toString().contains("SMO")){
			m = new BoaSMO(clr, o);
		}
		else if(clr.toString().contains("Vote")){
			m = new BoaVote(clr, o);
		}
		else if(clr.toString().contains("ZeroR")){
			m = new BoaZeroR(clr, o);
		}
		
		return m;
	}
	
	@FunctionSpec(name = "classify", returnType = "string", formalParameters = { "Model","tuple"})
	public static String classify(final BoaModel model, final boa.BoaTup vector) throws Exception {
		int NumOfAttributes = 0;
		ArrayList<Attribute> fvAttributes = new ArrayList<Attribute>();
		try {
			String[] fieldNames = vector.getFieldNames();
			int count = 0;
			for(int i = 0; i < fieldNames.length; i++) {
				if(vector.getValue(fieldNames[i]).getClass().isEnum()) {
					ArrayList<String> fvNominalVal = new ArrayList<String>();
					for(Object obj: vector.getValue(fieldNames[i]).getClass().getEnumConstants())
						fvNominalVal.add(obj.toString());
					fvAttributes.add(new Attribute("Nominal" + count, fvNominalVal));
					count++;
				}
				else if(vector.getValue(fieldNames[i]).getClass().isArray()) {
					int l = Array.getLength(vector.getValue(fieldNames[i])) - 1;
					for(int j = 0; j <= l; j++) {
						fvAttributes.add(new Attribute("Attribute" + count)); 
						count++;
					}
				}
				else {
					fvAttributes.add(new Attribute("Attribute" + count)); 
					count++;
				}
			}
			
			String[] fields = ((boa.BoaTup)model.getObject()).getFieldNames();
			Field lastfield = model.getObject().getClass().getField(fields[fields.length - 1]);
			if(lastfield.getType().isEnum()) {
				ArrayList<String> fvNominalVal = new ArrayList<String>();
				for(Object obj: lastfield.getType().getEnumConstants())
					fvNominalVal.add(obj.toString());
				fvAttributes.add(new Attribute("Nominal" + count, fvNominalVal));
				count++;
			}
			else {
				fvAttributes.add(new Attribute("Attribute" + count)); 
				count++;
			}
				
			NumOfAttributes = count;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		Instances testingSet = new Instances("Classifier", fvAttributes, 1);
		testingSet.setClassIndex(NumOfAttributes-1);

		Instance instance = new DenseInstance(NumOfAttributes);
		
		for(int i=0; i<NumOfAttributes-1; i++)
			if(NumberUtils.isNumber(vector.getValues()[i]))
				instance.setValue((Attribute)fvAttributes.get(i), Double.parseDouble(vector.getValues()[i]));
			else
				instance.setValue((Attribute)fvAttributes.get(i), vector.getValues()[i]);
		testingSet.add(instance);
		
		Classifier classifier = (Classifier) model.getClassifier();
		double predval = classifier.classifyInstance(testingSet.instance(0));
		
		if(testingSet.classAttribute().isNominal())
			return testingSet.classAttribute().value((int) predval);
		else
			return predval + "";
	}
	
	@FunctionSpec(name = "classify", returnType = "string", formalParameters = { "Model","array of int"})
	public static String classify(final BoaModel model, final long[] vector) throws Exception {
		int NumOfAttributes = vector.length + 1;
		ArrayList<Attribute> fvAttributes = new ArrayList<Attribute>();
		
		for(int i=0; i < NumOfAttributes - 1; i++) {
			fvAttributes.add(new Attribute("Attribute" + i));
		}
		
		try {
			String[] fields = ((boa.BoaTup)model.getObject()).getFieldNames();
			Field lastfield = model.getObject().getClass().getField(fields[fields.length - 1]);
			if(lastfield.getType().isEnum()) {
				ArrayList<String> fvNominalVal = new ArrayList<String>();
				for(Object obj: lastfield.getType().getEnumConstants())
					fvNominalVal.add(obj.toString());
				fvAttributes.add(new Attribute("Nominal" + (NumOfAttributes - 1), fvNominalVal));
			} else
				fvAttributes.add(new Attribute("Attribute" + (NumOfAttributes - 1))); 
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		Instances testingSet = new Instances("Classifier", fvAttributes, 1);
		testingSet.setClassIndex(NumOfAttributes - 1);

		Instance instance = new DenseInstance(NumOfAttributes);
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

	public static <T> T stack_pop(final java.util.Stack<T> s) {
		if (s.empty())
			return null;
		return s.pop();
	}

	public static <T> T stack_peek(final java.util.Stack<T> s) {
		if (s.empty())
			return null;
		return s.peek();
	}

	public static String protolistToString(final List<String> l) {
		String s = "";
		for (final String str : l)
			if (s.isEmpty())
				s += str;
			else
				s += ", " + str;
		return s;
	}

	public static <T> String arrayToString(final T[] arr) {
		String s = "";
		for (final T val : arr)
			if (s.isEmpty())
				s += val;
			else
				s += ", " + val;
		return s;
	}

	public static String arrayToString(final long[] arr) {
		String s = "";
		for (final long val : arr)
			if (s.isEmpty())
				s += val;
			else
				s += ", " + val;
		return s;
	}

	public static String arrayToString(final double[] arr) {
		String s = "";
		for (final double val : arr)
			if (s.isEmpty())
				s += val;
			else
				s += ", " + val;
		return s;
	}

	public static String arrayToString(final boolean[] arr) {
		String s = "";
		for (final boolean val : arr)
			if (s.isEmpty())
				s += val;
			else
				s += ", " + val;
		return s;
	}

	public static <T> T[] basic_array(final T[] arr) {
		return arr;
	}

	public static <T> long[] basic_array(final Long[] arr) {
		long[] arr2 = new long[arr.length];
		for (int i = 0; i < arr.length; i++)
			arr2[i] = arr[i];
		return arr2;
	}

	public static <T> double[] basic_array(final Double[] arr) {
		double[] arr2 = new double[arr.length];
		for (int i = 0; i < arr.length; i++)
			arr2[i] = arr[i];
		return arr2;
	}

	public static <T> boolean[] basic_array(final Boolean[] arr) {
		boolean[] arr2 = new boolean[arr.length];
		for (int i = 0; i < arr.length; i++)
			arr2[i] = arr[i];
		return arr2;
	}

	public static <T> T[] concat(final T[] first, @SuppressWarnings("unchecked") final T[]... rest) {
		int totalLength = first.length;
		for (T[] array : rest)
			totalLength += array.length;
		
		final T[] result = Arrays.copyOf(first, totalLength);
		int offset = first.length;
		for (T[] array : rest) {
			System.arraycopy(array, 0, result, offset, array.length);
			offset += array.length;
		}
		return result;
	}

	public static long[] concat(final long[] first, final long[]... rest) {
		int totalLength = first.length;
		for (long[] array : rest)
			totalLength += array.length;
		
		final long[] result = new long[totalLength];
		System.arraycopy(first, 0, result, 0, first.length);

		int offset = first.length;
		for (long[] array : rest) {
			System.arraycopy(array, 0, result, offset, array.length);
			offset += array.length;
		}
		return result;
	}

	public static double[] concat(final double[] first, final double[]... rest) {
		int totalLength = first.length;
		for (double[] array : rest)
			totalLength += array.length;
		
		final double[] result = new double[totalLength];
		System.arraycopy(first, 0, result, 0, first.length);

		int offset = first.length;
		for (double[] array : rest) {
			System.arraycopy(array, 0, result, offset, array.length);
			offset += array.length;
		}
		return result;
	}

	public static boolean[] concat(final boolean[] first, final boolean[]... rest) {
		int totalLength = first.length;
		for (boolean[] array : rest)
			totalLength += array.length;
		
		final boolean[] result = new boolean[totalLength];
		System.arraycopy(first, 0, result, 0, first.length);

		int offset = first.length;
		for (boolean[] array : rest) {
			System.arraycopy(array, 0, result, offset, array.length);
			offset += array.length;
		}
		return result;
	}
}
