package boa.aggregators;

import java.util.*;
import java.io.IOException;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;

import boa.io.EmitKey;
import boa.io.EmitValue;


/**
 * A Boa aggregator to output graph data in CSV format as an adjacency list.
 * 
 * @author rdyer
 */
@AggregatorSpec(name = "graph")
public class GraphCSVAggregator extends GraphAggregator {
	/** {@inheritDoc} */
	@Override
	public void finish() throws IOException, InterruptedException {
		this.collect(toString(this.neighbors));
	}

	private String toString(final Set<String> set) {
		String str = "";
		for (final String s : set) {
			if (!str.isEmpty())
				str += ",";
			str += s;
		}
		return str;
	}

	/** {@inheritDoc} */
	@Override
	protected String format(final String idx, final String data, final String metadata) {
		return "\"" + idx.substring(1, idx.length() - 1)  + "\"," + data + "";
	}
}
