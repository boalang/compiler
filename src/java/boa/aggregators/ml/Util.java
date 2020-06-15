package boa.aggregators.ml;

import weka.classifiers.Evaluation;

public class Util {

	public static String summary(Evaluation eval, String setName) {
//		System.out.println("Correct % = " + eval.pctCorrect());
//		System.out.println("Incorrect % = " + eval.pctIncorrect());
//		System.out.println("AUC % = " + eval.areaUnderROC(1));
//		System.out.println("Kappa % = " + eval.kappa());
//		System.out.println("MAE % = " + eval.meanAbsoluteError());
//		System.out.println("RMSE % = " + eval.rootMeanSquaredError());
//		System.out.println("RAE % = " + eval.relativeAbsoluteError());
//		System.out.println("RRSE % = " + eval.rootRelativeSquaredError());
//		System.out.println("Precision = " + eval.precision(1));
//		System.out.println("Recall = " + eval.recall(1));
//		System.out.println("fMeasure = " + eval.fMeasure(1));
//		System.out.println("Error Rate = " + eval.errorRate());
		return eval.toSummaryString("\n" + setName + "Set Evaluation:\n", false);
	}
	
	
}
