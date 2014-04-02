package boa.aggregators;

import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mrunit.mapreduce.ReduceDriver;
import org.junit.Test;

import boa.io.EmitKey;
import boa.io.EmitValue;

public class TestIntMeanAggregator {
	@Test
	public void testIntMeanAggregatorCombine() {
		final List<EmitValue> values = new ArrayList<EmitValue>();
		values.add(new EmitValue("1"));
		values.add(new EmitValue("2.0"));
		values.add(new EmitValue(3));
		values.add(new EmitValue(4.0));

		new ReduceDriver<EmitKey, EmitValue, EmitKey, EmitValue>(new IntMeanBoaCombiner()).withInput(new EmitKey("test", 0), values)
				.withOutput(new EmitKey("test", 0), new EmitValue("10", "4")).runTest();
	}

	@Test
	public void testIntMeanAggregatorReduce() {
		final List<EmitValue> values = new ArrayList<EmitValue>();
		values.add(new EmitValue("1"));
		values.add(new EmitValue("2.0"));
		values.add(new EmitValue(3));
		values.add(new EmitValue(4.0));

		new ReduceDriver<EmitKey, EmitValue, Text, NullWritable>(new IntMeanBoaReducer()).withInput(new EmitKey("test", 0), values)
				.withOutput(new Text("test[] = 2.5"), NullWritable.get()).runTest();
	}
}

class IntMeanBoaCombiner extends boa.runtime.BoaCombiner {
	public IntMeanBoaCombiner() {
		super();

		this.tables.put("test", new Table(new boa.aggregators.IntMeanAggregator()));
	}
}

class IntMeanBoaReducer extends boa.runtime.BoaReducer {
	public IntMeanBoaReducer() {
		super();

		this.tables.put("test", new Table(new boa.aggregators.IntMeanAggregator()));
	}
}
