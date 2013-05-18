package boa.aggregators;

import java.io.IOException;
import java.util.TreeMap;
import java.util.SortedMap;

import boa.io.EmitKey;

/**
 * A Boa aggregator to calculate the kurtosis of the values in a dataset.
 * 
 * @author rdyer
 */
@AggregatorSpec(name = "kurtosis", type = "int")
public class KurtosisAggregator extends Aggregator {
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
			for (Long key : map.keySet())
				s += key + ":" + map.get(key) + ";";
			this.collect(s, null);
			return;
		}


		double s1 = 0;
		double s2 = 0;
		double s3 = 0;
		double s4 = 0;

		for (final Long key : map.keySet()) {
			s1 += key * map.get(key);
			s2 += key * key * map.get(key);
			s3 += key * key * key * map.get(key);
			s4 += key * key * key * map.get(key);
		}

		final double var = s2 / (double)(count - 1) - s1 * s1 / (double)(count * (count - 1));

		this.collect((s4 - s3 * s1 * 4 / count + s2 * s1 * s1 * 6 / (double)(count * count) - s1 * s1 * s1 * s1 * 3 / (double)(count * count * count)) / (count * var * var));
	}
}
