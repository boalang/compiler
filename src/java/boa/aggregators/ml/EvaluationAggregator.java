package boa.aggregators.ml;

import java.io.IOException;
import java.util.HashMap;
import org.nd4j.shade.protobuf.common.collect.Maps;

import boa.aggregators.AggregatorSpec;
import boa.aggregators.FinishedException;
import boa.runtime.Tuple;

/**
 * A Boa aggregator for evaluating actual and predicted value.
 *
 * @author hyj
 */
@AggregatorSpec(name = "evaluation", formalParameters = { "string" })
public class EvaluationAggregator extends MLAggregator {

	private HashMap<String, Integer> classes;

	private long[][] matrix;

	private Results results;

	public EvaluationAggregator() {
		initialize();
	}

	public EvaluationAggregator(String s) {
		super(s);
		initialize();
	}

	private void initialize() {
		updateClasses();
		int size = classes.size();
		matrix = new long[size][size];
		results = new Results(classes);
	}

	private void updateClasses() {
		for (int i = 0; i < options.length; i++) {
			String cur = options[i];
			if (cur.equals("-class")) {
				classes = Maps.newHashMap();
				for (String c : options[++i].split(":"))
					classes.put(c, classes.size());
			}
		}
	}

	@Override
	public void aggregate(String data, String metadata) throws IOException, InterruptedException, FinishedException {
	}

	@Override
	public void aggregate(Tuple data, String metadata) throws IOException, InterruptedException {
	}

	@Override
	public void aggregate(String data[], String metadata) throws IOException, InterruptedException {
//		System.out.println(data[0]);
		update(matrix, data, metadata);
	}

	private void update(long[][] m, String[] data, String metadata) {
		int predicted = classes.get(data[0]);
		int expected = classes.get(data[1]);
		m[predicted][expected]++;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void finish() throws IOException, InterruptedException {
		calculate(matrix, results);

		StringBuilder sb = new StringBuilder();
		sb.append("Precision   Recall   F1-Score   Support\n").append(results);
		this.collect(sb.toString());
	}

	private void calculate(long[][] m, Results results) {
		long[] rowSums = new long[m.length];
		long[] colSums = new long[m.length];
		for (int row = 0; row < m.length; row++) {
			for (int col = 0; col < m.length; col++) {
				rowSums[row] += m[row][col];
				colSums[col] += m[row][col];
			}
		}

		int correct = 0;
		for (int i = 0; i < m.length; i++) {
			long colSum = colSums[i];
			long rowSum = rowSums[i];
			double val = m[i][i];
			Result r = results.get(i);

			results.total += colSum;
			correct += val;

			r.precision = rowSum == 0 ? 0 : val / rowSum;
			r.recall = colSum == 0 ? 0 : val / colSum;
			r.f1_score = (r.precision + r.recall) == 0 ? 0 : 2 * (r.precision * r.recall) / (r.precision + r.recall);
			r.support = colSum;

			results.macro[0] += r.precision;
			results.macro[1] += r.recall;
			results.macro[2] += r.f1_score;

			results.weighted[0] += r.precision * r.support;
			results.weighted[1] += r.recall * r.support;
			results.weighted[2] += r.f1_score * r.support;
		}

		results.accuracy = results.total == 0 ? 0 : (double) correct / results.total;

		for (int i = 0; i < results.macro.length; i++)
			results.macro[i] /= 3;

		for (int i = 0; i < results.weighted.length; i++)
			results.weighted[i] /= results.total == 0 ? 1 : results.total;
	}

}

class Results {
	double accuracy;
	long total;
	// precison, recall, f1-score
	double[] macro;
	double[] weighted;
	Result[] results;
	String[] classes;

	public Results(HashMap<String, Integer> classes) {
		int size = classes.size();
		macro = new double[3];
		weighted = new double[3];
		results = new Result[size];
		this.classes = new String[size];
		for (String c : classes.keySet())
			this.classes[classes.get(c)] = c;
		for (int i = 0; i < size; i++)
			results[i] = new Result();
	}

	public Result get(int i) {
		return results[i];
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < classes.length; i++) {
			Result r = results[i];
			sb.append(classes[i]).append(" " + str(r.precision)).append(" " + str(r.recall))
					.append(" " + str(r.f1_score)).append(" " + r.support + "\n");
		}

		sb.append("accuracy ").append(str(accuracy)).append(" " + total + "\n");

		sb.append("macro avg ");
		for (int i = 0; i < 3; i++)
			if (i == 0)
				sb.append(str(macro[i]));
			else
				sb.append(" " + str(macro[i]));
		sb.append("\n");

		sb.append("weighted avg ");
		for (int i = 0; i < 3; i++)
			if (i == 0)
				sb.append(str(weighted[i]));
			else
				sb.append(" " + str(weighted[i]));
		sb.append("\n");

		return sb.toString();
	}

	public String str(double d) {
		return String.format("%.1f", d * 100);
	}
}

class Result {
	double precision;
	double recall;
	double f1_score;
	double accuracy;
	long support;
}