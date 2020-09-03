package boa.aggregators.ml.util;

import weka.classifiers.Classifier;
import weka.classifiers.ConditionalDensityEstimator;
import weka.classifiers.IntervalEstimator;
import weka.classifiers.evaluation.output.prediction.AbstractOutput;
import weka.core.BatchPredictor;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;

public class MyEvaluation extends weka.classifiers.evaluation.Evaluation {

	private static final long serialVersionUID = 1L;

	public MyEvaluation(Instances data) throws Exception {
		super(data);
	}
	
	public MyEvaluation(MyVote v) throws Exception {
		super(v.getDataset());
		evaluateMyVote(v, v.getDataset());
	}

	public double[] evaluateMyVote(Classifier classifier, Instances data, Object... forPredictionsPrinting)
			throws Exception {
		// for predictions printing
		AbstractOutput classificationOutput = null;

		double predictions[] = new double[data.numInstances()];

		if (forPredictionsPrinting.length > 0) {
			classificationOutput = (AbstractOutput) forPredictionsPrinting[0];
		}

		if (classifier instanceof BatchPredictor
				&& ((BatchPredictor) classifier).implementsMoreEfficientBatchPrediction()) {
			// make a copy and set the class to missing
			Instances dataPred = new Instances(data);
			for (int i = 0; i < data.numInstances(); i++) {
				dataPred.instance(i).setClassMissing();
			}
			double[][] preds = ((BatchPredictor) classifier).distributionsForInstances(dataPred);
			for (int i = 0; i < data.numInstances(); i++) {
				double[] p = preds[i];

				predictions[i] = evaluationForSingleInstance(p, data.instance(i), true);

				if (classificationOutput != null) {
					classificationOutput.printClassification(p, data.instance(i), i);
				}
			}
		} else {
			// Need to be able to collect predictions if appropriate (for AUC)
			if (classifier instanceof MyVote) {
				MyVote v = (MyVote) classifier;
				for (int i = 0; i < data.numInstances(); i++) {
					predictions[i] = evaluationForSingleInstance(v, data.instance(i), i, true);
					if (classificationOutput != null) {
						classificationOutput.printClassification(classifier, data.instance(i), i);
					}
				}
			}

		}

		return predictions;
	}

	protected double evaluationForSingleInstance(Classifier classifier, Instance instance, int instanceIndex,
			boolean storePredictions) throws Exception {

		Instance classMissing = (Instance) instance.copy();
		classMissing.setDataset(instance.dataset());

		if (classifier instanceof weka.classifiers.misc.InputMappedClassifier) {
			instance = (Instance) instance.copy();
			instance = ((weka.classifiers.misc.InputMappedClassifier) classifier).constructMappedInstance(instance);
			// System.out.println("Mapped instance " + instance);
			int mappedClass = ((weka.classifiers.misc.InputMappedClassifier) classifier).getMappedClassIndex();
			classMissing.setMissing(mappedClass);
		} else {
			classMissing.setClassMissing();
		}

		// System.out.println("instance (to predict)" + classMissing);
		double pred = 0;
		if (classifier instanceof MyVote) {
			MyVote v = (MyVote) classifier;
			pred = evaluationForSingleInstance(v.distributionForInstance(instanceIndex), instance, storePredictions);
		} else {
			pred = evaluationForSingleInstance(classifier.distributionForInstance(classMissing), instance,
					storePredictions);
		}

		// We don't need to do the following if the class is nominal because in that
		// case
		// entropy and coverage statistics are always computed.
		if (!m_ClassIsNominal) {
			if (!instance.classIsMissing() && !Utils.isMissingValue(pred)) {
				if (classifier instanceof IntervalEstimator) {
					updateStatsForIntervalEstimator((IntervalEstimator) classifier, classMissing,
							instance.classValue());
				} else {
					m_CoverageStatisticsAvailable = false;
				}
				if (classifier instanceof ConditionalDensityEstimator) {
					updateStatsForConditionalDensityEstimator((ConditionalDensityEstimator) classifier, classMissing,
							instance.classValue());
				} else {
					m_ComplexityStatisticsAvailable = false;
				}
			}
		}
		return pred;
	}
}