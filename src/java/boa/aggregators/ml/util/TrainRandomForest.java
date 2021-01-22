package boa.aggregators.ml.util;

import java.io.*;
import java.util.*;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.trees.RandomForest;
import weka.core.*;

public class TrainRandomForest {
	
	public static void main(String[] args) {
		
		long startTime = System.nanoTime();
		
		ArrayList<Attribute> attributes = new ArrayList<Attribute>();
		for (int i = 0; i < 4; i++)
			attributes.add(new Attribute("Attribute" + i));
		ArrayList<String> nomialVals = new ArrayList<String>();
		nomialVals.add("notbuggy");
		nomialVals.add("buggy");
		attributes.add(new Attribute("Nominal4", nomialVals));
		Instances instances = new Instances("rf", attributes, 1);
		instances.setClassIndex(4);
		
		
		
		
		String input = "/Users/yijiahuang/boa-workspace/compiler/src/java/boa/aggregators/ml/util/random_forest_input.txt";
		try (BufferedReader br = new BufferedReader(new FileReader(input))) {
		    String line = null;
		    while ((line = br.readLine()) != null) {
		       String[] arr = line.substring(6).split(" ");
		       Instance instance = new DenseInstance(5);
		       for (int i = 0; i < 5; i++) {
		    	   if (i == 4)
		    		   instance.setValue(attributes.get(i), arr[i]);
		    	   else
		    		   instance.setValue(attributes.get(i), Double.parseDouble(arr[i]));
		       }
		       instances.add(instance);
		    }
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		RandomForest model = new RandomForest();
		try {
			model.buildClassifier(instances);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		String modelPath = "/Users/yijiahuang/boa-workspace/compiler/src/java/boa/aggregators/ml/util/rf.model";
		serialize(modelPath, model);
		
		long endTime   = System.nanoTime();
		long totalTime = endTime - startTime;
		System.out.println(totalTime * 1.66667e-11 + " mins");
		
//		startTime = System.nanoTime();
//		model = (RandomForest) deserialize(modelPath);
//		evaluate(model, instances);
//		endTime   = System.nanoTime();
//		totalTime = endTime - startTime;
//		System.out.println(totalTime * 1.66667e-11 + " mins");
	}
	
	public static void evaluate(Classifier model, Instances set) {
		try {
			Evaluation eval = new Evaluation(set);
			eval.evaluateModel(model, set);
			String res = eval.toSummaryString("\n=== Dataset Evaluation ===\n", false);
			res += "\n" + eval.toClassDetailsString() + "\n";
			System.out.println(res);
		} catch (Exception e) {
		}
	}

	public static void serialize(String path, Object o) {
		try {
			FileOutputStream file = new FileOutputStream(path);
			ObjectOutputStream out = new ObjectOutputStream(file);
			out.writeObject(o);
			out.close();
			file.close();
			System.out.println("Object has been serialized");
		} catch (IOException ex) {
			System.out.println("IOException is caught");
		}
	}

	public static Object deserialize(String path) {
		Object o = null;
		try {
			FileInputStream file = new FileInputStream(path);
			ObjectInputStream in = new ObjectInputStream(file);
			o = in.readObject();
			in.close();
			file.close();
		} catch (IOException ex) {
			System.out.println("IOException is caught");
		} catch (ClassNotFoundException ex) {
			System.out.println("ClassNotFoundException is caught");
		}
		return o;
	}
	
}
