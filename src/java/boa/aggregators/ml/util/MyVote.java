package boa.aggregators.ml.util;

import java.util.List;
import java.util.Vector;

import org.apache.hadoop.fs.Path;
import org.nd4j.shade.guava.collect.Lists;

import weka.classifiers.Classifier;
import weka.classifiers.meta.Vote;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;

public class MyVote extends Vote {

	private static final long serialVersionUID = 1L;

	private SeqCollection<Classifier> classifiers;

	private double[][] instanceProbs;
	private double[] instanceNumPredictions;
	private Instances dataset;

	public MyVote(Path path) {
		classifiers = new SeqCollection<Classifier>(path);
	}

	public MyVote(Path path, Instances dataset, String rule) {
		classifiers = new SeqCollection<Classifier>(path);
		this.dataset = dataset;
		this.setCombinationRule(rule);
		instanceProbs = new double[dataset.numInstances()][dataset.numClasses()];
		instanceNumPredictions = new double[dataset.numInstances()];
		try {
			preprocess();
			postprocess();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String[] getResult(int instanceIdx) throws Exception {
		double predictedDoubleVal, expectedDoubleVal;
		double[] dist = instanceProbs[instanceIdx];
		Instance instance = dataset.instance(instanceIdx);
		int index;

		switch (m_CombinationRule) {
		case AVERAGE_RULE:
		case PRODUCT_RULE:
		case MAJORITY_VOTING_RULE:
		case MIN_RULE:
		case MAX_RULE:
			if (instance.classAttribute().isNominal()) {
				index = Utils.maxIndex(dist);
				if (dist[index] == 0) {
					predictedDoubleVal = Utils.missingValue();
				} else {
					predictedDoubleVal = index;
				}
			} else if (instance.classAttribute().isNumeric()) {
				predictedDoubleVal = dist[0];
			} else {
				predictedDoubleVal = Utils.missingValue();
			}
			break;
		case MEDIAN_RULE:
			predictedDoubleVal = classifyInstanceMedian(instance);
			break;
		default:
			throw new IllegalStateException("Unknown combination rule '" + m_CombinationRule + "'!");
		}

		expectedDoubleVal = instance.classValue();
		String expected = instance.classAttribute().isNominal()
				? instance.classAttribute().value((int) expectedDoubleVal)
				: String.valueOf(expectedDoubleVal);
		String predicted = instance.classAttribute().isNominal()
				? instance.classAttribute().value((int) predictedDoubleVal)
				: String.valueOf(predictedDoubleVal);

		return new String[] { expected, predicted };
	}

	public double[][] getInstanceProbs() {
		return instanceProbs;
	}

	public double[] getInstanceNumPredictions() {
		return instanceNumPredictions;
	}

	public Instances getDataset() {
		return dataset;
	}

	public void setCombinationRule(String rule) {
		switch (rule) {
		case "AVG":
			m_CombinationRule = AVERAGE_RULE;
			break;
		case "PROD":
			m_CombinationRule = PRODUCT_RULE;
			break;
		case "MAJ":
			m_CombinationRule = MAJORITY_VOTING_RULE;
			break;
		case "MIN":
			m_CombinationRule = MIN_RULE;
			break;
		case "MAX":
			m_CombinationRule = MAX_RULE;
			break;
		case "MED":
			m_CombinationRule = MEDIAN_RULE;
			break;
		default:
			break;
		}
	}

	private void preprocess() throws Exception {
		for (Classifier classifier : classifiers) {
			switch (m_CombinationRule) {
			case AVERAGE_RULE:
				preprocessDistributionForInstanceAverage(classifier);
				break;
			case PRODUCT_RULE:
				preprocessDistributionForInstanceProduct(classifier);
				break;
			case MAJORITY_VOTING_RULE:
				preprocessDistributionForInstanceMajorityVoting(classifier);
				break;
			case MIN_RULE:
				preprocessDistributionForInstanceMin(classifier);
				break;
			case MAX_RULE:
				preprocessDistributionForInstanceMax(classifier);
				break;
			case MEDIAN_RULE:
				preprocessClassifyInstance(classifier);
				break;
			default:
				throw new IllegalStateException("Unknown combination rule '" + m_CombinationRule + "'!");
			}
		}
	}

	private void postprocess() {
		switch (m_CombinationRule) {
		case AVERAGE_RULE:
			postprocessDistributionForInstanceAverage();
			break;
		case PRODUCT_RULE:
			postprocessDistributionForInstanceProduct();
			break;
		case MAJORITY_VOTING_RULE:
			postprocessDistributionForInstanceMajorityVoting();
			break;
		case MIN_RULE:
			postprocessDistributionForInstanceMin();
			break;
		case MAX_RULE:
			postprocessDistributionForInstanceMax();
			break;
		case MEDIAN_RULE:
			postprocessClassifyInstance();
			break;
		default:
			throw new IllegalStateException("Unknown combination rule '" + m_CombinationRule + "'!");
		}
	}

	@Override
	public double[] distributionForInstance(Instance instance) throws Exception {

		double[] result = new double[instance.numClasses()];

		switch (m_CombinationRule) {
		case AVERAGE_RULE:
			result = distributionForInstanceAverage(instance);
			break;
		case PRODUCT_RULE:
			result = distributionForInstanceProduct(instance);
			break;
		case MAJORITY_VOTING_RULE:
			result = distributionForInstanceMajorityVoting(instance);
			break;
		case MIN_RULE:
			result = distributionForInstanceMin(instance);
			break;
		case MAX_RULE:
			result = distributionForInstanceMax(instance);
			break;
		case MEDIAN_RULE:
			result[0] = classifyInstance(instance);
			break;
		default:
			throw new IllegalStateException("Unknown combination rule '" + m_CombinationRule + "'!");
		}

		if (!instance.classAttribute().isNumeric() && (Utils.sum(result) > 0)) {
			Utils.normalize(result);
		}

		return result;
	}

	public double[] distributionForInstance(int instanceIdx, Instance instance) throws Exception {
		double[] result = instanceProbs[instanceIdx];
		if (!instance.classAttribute().isNumeric() && (Utils.sum(result) > 0))
			Utils.normalize(result);
		return result;
	}

	@Override
	protected double classifyInstanceMedian(Instance instance) throws Exception {
		List<Double> results = Lists.newArrayList();

		for (Classifier classifier : classifiers) {
			double pred = classifier.classifyInstance(instance);
			if (!Utils.isMissingValue(pred)) {
				results.add(pred);
			}
		}

		if (results.size() == 0) {
			return Utils.missingValue();
		} else if (results.size() == 1) {
			return results.get(0);
		} else {
			double[] actualResults = new double[results.size()];
			System.arraycopy(results, 0, actualResults, 0, results.size());
			return Utils.kthSmallestValue(actualResults, actualResults.length / 2);
		}
	}

	private void preprocessClassifyInstance(Classifier classifier) {
		// TODO Auto-generated method stub
	}

	private void postprocessClassifyInstance() {
		// TODO Auto-generated method stub
	}

	@Override
	protected double[] distributionForInstanceAverage(Instance instance) throws Exception {

		double[] probs = new double[instance.numClasses()];

		double numPredictions = 0;

		// preProcess
		for (Classifier classifier : classifiers) {
			double[] dist = classifier.distributionForInstance(instance);
			if (!instance.classAttribute().isNumeric() || !Utils.isMissingValue(dist[0])) {
				for (int j = 0; j < dist.length; j++) {
					probs[j] += dist[j];
				}
				numPredictions++;
			}
		}

		// postProcess
		if (instance.classAttribute().isNumeric()) {
			if (numPredictions == 0) {
				probs[0] = Utils.missingValue();
			} else {
				for (int j = 0; j < probs.length; j++) {
					probs[j] /= numPredictions;
				}
			}
		} else {
			// Should normalize "probability" distribution
			if (Utils.sum(probs) > 0) {
				Utils.normalize(probs);
			}
		}

		return probs;
	}

	private void preprocessDistributionForInstanceAverage(Classifier classifier) throws Exception {
		for (int i = 0; i < instanceProbs.length; i++) {
			Instance instance = dataset.instance(i);
			double[] probs = instanceProbs[i];
			double[] dist = classifier.distributionForInstance(instance);
			if (!instance.classAttribute().isNumeric() || !Utils.isMissingValue(dist[0])) {
				for (int j = 0; j < dist.length; j++) {
					probs[j] += dist[j];
				}
				instanceNumPredictions[i]++;
			}
		}
	}

	private void postprocessDistributionForInstanceAverage() {
		for (int i = 0; i < instanceProbs.length; i++) {
			double[] probs = instanceProbs[i];
			if (dataset.classAttribute().isNumeric()) {
				if (instanceNumPredictions[i] == 0) {
					probs[0] = Utils.missingValue();
				} else {
					for (int j = 0; j < probs.length; j++) {
						probs[j] /= instanceNumPredictions[i];
					}
				}
			} else {
				// Should normalize "probability" distribution
				if (Utils.sum(probs) > 0) {
					Utils.normalize(probs);
				}
			}
		}
	}

	/* ---------------------------- Product ---------------------------- */
	@Override
	protected double[] distributionForInstanceProduct(Instance instance) throws Exception {

		double[] probs = new double[instance.numClasses()];
		for (int i = 0; i < probs.length; i++) {
			probs[i] = 1.0;
		}

		int numPredictions = 0;

		for (Classifier classifier : classifiers) {
			double[] dist = classifier.distributionForInstance(instance);
			if (Utils.sum(dist) > 0) {
				for (int j = 0; j < dist.length; j++) {
					probs[j] *= dist[j];
				}
				numPredictions++;
			}
		}

		// No predictions?
		if (numPredictions == 0) {
			return new double[instance.numClasses()];
		}

		// Should normalize to get "probabilities"
		if (Utils.sum(probs) > 0) {
			Utils.normalize(probs);
		}

		return probs;
	}

	private void preprocessDistributionForInstanceProduct(Classifier classifier) throws Exception {
		// TODO Auto-generated method stub
	}

	private void postprocessDistributionForInstanceProduct() {
		// TODO Auto-generated method stub
	}

	/* ---------------------------- Majority Voting ---------------------------- */
	@Override
	protected double[] distributionForInstanceMajorityVoting(Instance instance) throws Exception {

		double[] probs = new double[instance.classAttribute().numValues()];
		double[] votes = new double[probs.length];

		for (Classifier classifier : classifiers) {
			probs = classifier.distributionForInstance(instance);
			int maxIndex = 0;

			for (int j = 0; j < probs.length; j++) {
				if (probs[j] > probs[maxIndex]) {
					maxIndex = j;
				}
			}

			// Consider the cases when multiple classes happen to have the same
			// probability
			if (probs[maxIndex] > 0) {
				for (int j = 0; j < probs.length; j++) {
					if (probs[j] == probs[maxIndex]) {
						votes[j]++;
					}
				}
			}
		}

		int tmpMajorityIndex = 0;
		for (int k = 1; k < votes.length; k++) {
			if (votes[k] > votes[tmpMajorityIndex]) {
				tmpMajorityIndex = k;
			}
		}

		// No votes received
		if (votes[tmpMajorityIndex] == 0) {
			return new double[instance.numClasses()];
		}

		// Consider the cases when multiple classes receive the same amount of votes
		Vector<Integer> majorityIndexes = new Vector<Integer>();
		for (int k = 0; k < votes.length; k++) {
			if (votes[k] == votes[tmpMajorityIndex]) {
				majorityIndexes.add(k);
			}
		}
		int majorityIndex = tmpMajorityIndex;
		if (majorityIndexes.size() > 1) {
			// resolve ties by looking at the predicted distribution
			double[] distPreds = distributionForInstanceAverage(instance);
			majorityIndex = Utils.maxIndex(distPreds);
			// Resolve the ties according to a uniform random distribution
			// majorityIndex =
			// majorityIndexes.get(m_Random.nextInt(majorityIndexes.size()));
		}

		// set probs to 0
		probs = new double[probs.length];

		probs[majorityIndex] = 1; // the class that have been voted the most
									// receives 1

		return probs;
	}

	private void preprocessDistributionForInstanceMajorityVoting(Classifier classifier) {
		// TODO Auto-generated method stub
	}

	private void postprocessDistributionForInstanceMajorityVoting() {
		// TODO Auto-generated method stub
	}

	/* ---------------------------------- MAX ---------------------------------- */
	@Override
	protected double[] distributionForInstanceMax(Instance instance) throws Exception {

		double[] probs = new double[instance.numClasses()];

		double numPredictions = 0;

		for (Classifier classifier : classifiers) {
			double[] dist = classifier.distributionForInstance(instance);
			if (!instance.classAttribute().isNumeric() || !Utils.isMissingValue(dist[0])) {
				for (int j = 0; j < dist.length; j++) {
					if ((probs[j] < dist[j]) || (numPredictions == 0)) {
						probs[j] = dist[j];
					}
				}
				numPredictions++;
			}
		}

		if (instance.classAttribute().isNumeric()) {
			if (numPredictions == 0) {
				probs[0] = Utils.missingValue();
			}
		} else {

			// Should normalize "probability" distribution
			if (Utils.sum(probs) > 0) {
				Utils.normalize(probs);
			}
		}

		return probs;
	}

	private void preprocessDistributionForInstanceMax(Classifier classifier) {
		// TODO Auto-generated method stub

	}

	private void postprocessDistributionForInstanceMax() {
		// TODO Auto-generated method stub

	}

	/* ---------------------------------- Min ---------------------------------- */
	@Override
	protected double[] distributionForInstanceMin(Instance instance) throws Exception {

		double[] probs = new double[instance.numClasses()];

		double numPredictions = 0;

		for (Classifier classifier : classifiers) {
			double[] dist = classifier.distributionForInstance(instance);
			if (!instance.classAttribute().isNumeric() || !Utils.isMissingValue(dist[0])) {
				for (int j = 0; j < dist.length; j++) {
					if ((probs[j] > dist[j]) || (numPredictions == 0)) {
						probs[j] = dist[j];
					}
				}
				numPredictions++;
			}
		}

		if (instance.classAttribute().isNumeric()) {
			if (numPredictions == 0) {
				probs[0] = Utils.missingValue();
			}
		} else {

			// Should normalize "probability" distribution
			if (Utils.sum(probs) > 0) {
				Utils.normalize(probs);
			}
		}

		return probs;
	}

	private void preprocessDistributionForInstanceMin(Classifier classifier) {
		// TODO Auto-generated method stub
	}

	private void postprocessDistributionForInstanceMin() {
		// TODO Auto-generated method stub
	}

	@Override
	public String toString() {
		String result = this.getClass().getName() + " using the '";

		switch (m_CombinationRule) {
		case AVERAGE_RULE:
			result += "Average";
			break;

		case PRODUCT_RULE:
			result += "Product";
			break;

		case MAJORITY_VOTING_RULE:
			result += "Majority Voting";
			break;

		case MIN_RULE:
			result += "Minimum";
			break;

		case MAX_RULE:
			result += "Maximum";
			break;

		case MEDIAN_RULE:
			result += "Median";
			break;

		default:
			throw new IllegalStateException("Unknown combination rule '" + m_CombinationRule + "'!");
		}

		result += "' combination rule \n";

		return result;
	}
}