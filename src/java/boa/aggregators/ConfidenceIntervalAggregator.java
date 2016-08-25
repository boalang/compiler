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
	private double n;

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
	 *            A double representing the significance
	 */
	public ConfidenceIntervalAggregator(final double n) {
		this.n = n;
	}

	/** {@inheritDoc} */
	@Override
	public void start(final EmitKey key) {
		super.start(key);

		map = new TreeMap<Long, Long>();
	}

	/** {@inheritDoc} */
	@Override
	public void aggregate(final String data, final String metadata) throws IOException, InterruptedException {
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
	public void aggregate(final long data, final String metadata) {
		if (map.containsKey(data))
			map.put(data, map.get(data) + 1L);
		else
			map.put(data, 1L);
	}

	/** {@inheritDoc} */
	@Override
	public void aggregate(final double data, final String metadata) {
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

			final double a = new TDistributionImpl(summaryStatistics.getN() - 1).inverseCumulativeProbability(1.0 - n / 200.0);
	
			this.collect(a * summaryStatistics.getStandardDeviation() / Math.sqrt(summaryStatistics.getN()));
		} catch (final MathException e) {
		}
	}
}
