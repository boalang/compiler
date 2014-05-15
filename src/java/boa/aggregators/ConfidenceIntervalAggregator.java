package boa.aggregators;

import java.io.IOException;
import java.util.TreeMap;
import java.util.SortedMap;

import org.apache.commons.math.MathException;
import org.apache.commons.math.distribution.TDistributionImpl;
import org.apache.commons.math.stat.descriptive.SummaryStatistics;

import boa.io.EmitKey;

/**
 * A Boa aggregator to calculate a confidence interval of the values in a dataset.
 * 
 * @author rdyer
 */
@AggregatorSpec(name = "confidence", formalParameters = {"float"}, type = "int")
public class ConfidenceIntervalAggregator extends Aggregator {
	private SortedMap<Long, Long> map;

	/**
	 * Construct a {@link ConfidenceIntervalAggregator}.
	 */
	public ConfidenceIntervalAggregator() {
		this(5);
	}

	/**
	 * Construct a {@link ConfidenceIntervalAggregator}.
	 * 
	 * @param n
	 *            A long representing the significance
	 */
	public ConfidenceIntervalAggregator(final long n) {
		super(n);
	}

	/** {@inheritDoc} */
	@Override
	public void start(EmitKey key) {
		super.start(key);

		map = new TreeMap<Long, Long>();
	}

	/** {@inheritDoc} */
	@Override
	public boolean isAssociative() {
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public boolean isCommutative() {
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public void aggregate(String data, String metadata) throws IOException, InterruptedException {
		for (final String s : data.split(";")) {
			final int idx = s.indexOf(":");
			if (idx > 0) {
				final long item = Long.valueOf(s.substring(0, idx));
				final long count = Long.valueOf(s.substring(idx + 1));
				for (int i = 0; i < count; i++)
					aggregate(item, metadata);
			} else
				aggregate(Long.valueOf(s), metadata);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void aggregate(long data, String metadata) {
		if (map.containsKey(data))
			map.put(data, map.get(data) + 1L);
		else
			map.put(data, 1L);
	}

	/** {@inheritDoc} */
	@Override
	public void aggregate(double data, String metadata) {
		this.aggregate(Double.valueOf(data).longValue(), metadata);
	}

	/** {@inheritDoc} */
	@Override
	public void finish() throws IOException, InterruptedException {
		if (this.isCombining()) {
			String s = "";
			for (final Long key : map.keySet())
				s += key + ":" + map.get(key) + ";";
			this.collect(s, null);
			return;
		}

		try {
			final SummaryStatistics summaryStatistics = new SummaryStatistics();
			
			for (final Long key : map.keySet())
				for (int i = 0; i < map.get(key); i++)
					summaryStatistics.addValue(key);

			final TDistributionImpl tDist = new TDistributionImpl(summaryStatistics.getN() - 1);
			final double a = tDist.inverseCumulativeProbability(1.0 - getArg() / 200.0);
	
			this.collect(a * summaryStatistics.getStandardDeviation() / Math.sqrt(summaryStatistics.getN()));
		} catch (final MathException e) {
		}
	}
}
