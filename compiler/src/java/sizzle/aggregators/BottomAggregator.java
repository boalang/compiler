package sizzle.aggregators;

import java.io.IOException;
import java.util.Map.Entry;

import sizzle.io.EmitKey;

/**
 * A Sizzle aggregator to estimate the bottom <i>n</i> values in a dataset by
 * cardinality.
 * 
 * @author anthonyu
 */
@AggregatorSpec(name = "bottom", formalParameters = { "int" }, weightType = "int")
public class BottomAggregator extends Aggregator {
	private CountingSet<String> set;
	private final CountedString[] list;
	private final int last;

	/**
	 * Construct a {@link BottomAggregator}.
	 * 
	 * @param n A long representing the number of values to return
	 */
	public BottomAggregator(final long n) {
		super(n);

		// an array of weighted string of length n
		this.list = new CountedString[(int) n];
		// the index of the last entry in the list
		this.last = (int) (this.getArg() - 1);
	}

	/** {@inheritDoc} */
	@Override
	public void start(final EmitKey key) {
		super.start(key);

		this.set = new CountingSet<String>();

		// clear out the list
		for (int i = 0; i < this.getArg(); i++)
			this.list[i] = new CountedString("", Long.MAX_VALUE);
	}

	/** {@inheritDoc} */
	@Override
	public void aggregate(final String data, final String metadata) {
		if (metadata == null)
			this.set.add(data, 1);
		else
			this.set.add(data, Double.valueOf(metadata).longValue());
	}

	/** {@inheritDoc} */
	@Override
	public void finish() throws IOException, InterruptedException {
		if (this.isCombining()) {
			for (final Entry<String, Long> e : this.set.getEntries())
				this.collect(e.getKey().toString(), e.getValue().toString());
		} else {
			// TODO: replace this with the algorithm described in M. Charikar,
			// K. Chen, and M. Farach-Colton, Finding frequent items in data
			// streams, Proc 29th Intl. Colloq. on Automata, Languages and
			// Programming, 2002.

			for (final Entry<String, Long> e : this.set.getEntries()) {
				if (e.getValue() < this.list[this.last].getCount() || e.getValue() == this.list[this.last].getCount()
						&& this.list[this.last].getString().compareTo(e.getKey()) > 0) {
					// find this new item's position within the list
					for (int i = 0; i < this.getArg(); i++)
						if (e.getValue() < this.list[i].getCount() || e.getValue() == this.list[i].getCount()
								&& this.list[i].getString().compareTo(e.getKey()) > 0) {
							// here it is. move all subsequent items down one
							for (int j = (int) (this.getArg() - 2); j >= i; j--)
								this.list[j + 1] = this.list[j];

							// insert the item where it belongs
							this.list[i] = new CountedString(e.getKey(), e.getValue());

							break;
						}
				}
			}

			for (final CountedString c : this.list)
				if (c.getCount() < Long.MAX_VALUE)
					this.collect(c.toString());
		}
	}

	/** {@inheritDoc} */
	@Override
	public boolean isAssociative() {
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public boolean isCommutative() {
		return true;
	}
}
