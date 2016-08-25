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
 * A Boa aggregator to calculate the skewness of the values in a dataset.
 * 
 * @author rdyer
 */
@AggregatorSpec(name = "statistics", type = "int")
public class StatisticsAggregator extends Aggregator {
	private SortedMap<Long, Long> map;
	private long count;

	/** {@inheritDoc} */
	@Override
	public void start(final EmitKey key) {
		super.start(key);

		map = new TreeMap<Long, Long>();
		count = 0;
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
		count++;
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

		float median = 0;

		long medianPos = count / 2L;
		long curPos = 0;
		long prevPos = 0;
		long prevKey = 0;

		for (final Long key : map.keySet()) {
			curPos = prevPos + map.get(key);

			if (prevPos <= medianPos && medianPos < curPos) {
				if (curPos % 2 == 0 && prevPos == medianPos)
					median = (float) (key + prevKey) / 2.0f;
				else
					median = key;
				break;
			}

			prevKey = key;
			prevPos = curPos;
		}

		double s1 = 0;
		double s2 = 0;
		double s3 = 0;
		double s4 = 0;

		final SummaryStatistics summaryStatistics = new SummaryStatistics();

		for (final Long key : map.keySet()) {
			s1 += key * map.get(key);
			s2 += key * key * map.get(key);
			s3 += key * key * key * map.get(key);
			s4 += key * key * key * key * map.get(key);
			for (int i = 0; i < map.get(key); i++)
				summaryStatistics.addValue(key);
		}

		final double mean = s1 / (double)count;
		final double var = s2 / (double)(count - 1) - s1 * s1 / (double)(count * (count - 1));
		final double stdev = Math.sqrt(var);
		final double skewness = (s3 - 3 * s1 * s2 / (double)count + s1 * s1 * s1 * 2 / (count * count)) / (count * stdev * var);
		final double kurtosis = (s4 - s3 * s1 * 4 / count + s2 * s1 * s1 * 6 / (double)(count * count) - s1 * s1 * s1 * s1 * 3 / (double)(count * count * count)) / (count * var * var);

		double ci = 0.0;
		try {
			final TDistributionImpl tDist = new TDistributionImpl(summaryStatistics.getN() - 1);
			final double a = tDist.inverseCumulativeProbability(1.0 - 0.025);
			ci = a * summaryStatistics.getStandardDeviation() / Math.sqrt(summaryStatistics.getN());
		} catch (final MathException e) {
		}

		this.collect(s1 + ", " + mean + ", " + median + ", " + stdev + ", " + var + ", " + kurtosis + ", " + skewness + ", " + ci);
	}
}
