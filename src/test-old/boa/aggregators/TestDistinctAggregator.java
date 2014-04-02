package boa.aggregators;

import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mrunit.mapreduce.ReduceDriver;
import org.junit.Test;

import boa.io.EmitKey;
import boa.io.EmitValue;

public class TestDistinctAggregator {
	@Test
	public void testDistinctAggregatorCombineDistinct() {
		final List<EmitValue> values = new ArrayList<EmitValue>();
		values.add(new EmitValue("one"));
		values.add(new EmitValue("two"));
		values.add(new EmitValue("three"));
		values.add(new EmitValue("four"));

		final ReduceDriver<EmitKey, EmitValue, EmitKey, EmitValue> reduceDriver = new ReduceDriver<EmitKey, EmitValue, EmitKey, EmitValue>(
				new DistinctBoaCombiner());
		reduceDriver.setInput(new EmitKey("test", 0), values);
		reduceDriver.addOutput(new EmitKey("test", 0), new EmitValue("one"));
		reduceDriver.addOutput(new EmitKey("test", 0), new EmitValue("two"));
		reduceDriver.addOutput(new EmitKey("test", 0), new EmitValue("three"));
		reduceDriver.addOutput(new EmitKey("test", 0), new EmitValue("four"));
		reduceDriver.runTest();
	}

	@Test
	public void testDistinctAggregatorCombineIndistinct() {
		final List<EmitValue> values = new ArrayList<EmitValue>();
		values.add(new EmitValue("one"));
		values.add(new EmitValue("one"));
		values.add(new EmitValue("one"));
		values.add(new EmitValue("one"));
		values.add(new EmitValue("two"));
		values.add(new EmitValue("two"));
		values.add(new EmitValue("two"));
		values.add(new EmitValue("two"));
		values.add(new EmitValue("three"));
		values.add(new EmitValue("three"));
		values.add(new EmitValue("three"));
		values.add(new EmitValue("three"));
		values.add(new EmitValue("four"));
		values.add(new EmitValue("four"));
		values.add(new EmitValue("four"));
		values.add(new EmitValue("four"));

		final ReduceDriver<EmitKey, EmitValue, EmitKey, EmitValue> reduceDriver = new ReduceDriver<EmitKey, EmitValue, EmitKey, EmitValue>(
				new DistinctBoaCombiner());
		reduceDriver.setInput(new EmitKey("test", 0), values);
		reduceDriver.addOutput(new EmitKey("test", 0), new EmitValue("one"));
		reduceDriver.addOutput(new EmitKey("test", 0), new EmitValue("two"));
		reduceDriver.addOutput(new EmitKey("test", 0), new EmitValue("three"));
		reduceDriver.addOutput(new EmitKey("test", 0), new EmitValue("four"));
		reduceDriver.runTest();
	}

	@Test
	public void testDistinctAggregatorReduceDistinct() {
		final List<EmitValue> values = new ArrayList<EmitValue>();
		values.add(new EmitValue("one"));
		values.add(new EmitValue("two"));
		values.add(new EmitValue("three"));
		values.add(new EmitValue("four"));

		final ReduceDriver<EmitKey, EmitValue, Text, NullWritable> reduceDriver = new ReduceDriver<EmitKey, EmitValue, Text, NullWritable>(
				new DistinctBoaReducer());
		reduceDriver.setInput(new EmitKey("test", 0), values);
		reduceDriver.addOutput(new Text("test[] = one"), NullWritable.get());
		reduceDriver.addOutput(new Text("test[] = two"), NullWritable.get());
		reduceDriver.addOutput(new Text("test[] = three"), NullWritable.get());
		reduceDriver.addOutput(new Text("test[] = four"), NullWritable.get());
		reduceDriver.runTest();
	}

	@Test
	public void testDistinctAggregatorReduceIndistinct() {
		final List<EmitValue> values = new ArrayList<EmitValue>();
		values.add(new EmitValue("one"));
		values.add(new EmitValue("one"));
		values.add(new EmitValue("one"));
		values.add(new EmitValue("one"));
		values.add(new EmitValue("two"));
		values.add(new EmitValue("two"));
		values.add(new EmitValue("two"));
		values.add(new EmitValue("two"));
		values.add(new EmitValue("three"));
		values.add(new EmitValue("three"));
		values.add(new EmitValue("three"));
		values.add(new EmitValue("three"));
		values.add(new EmitValue("four"));
		values.add(new EmitValue("four"));
		values.add(new EmitValue("four"));
		values.add(new EmitValue("four"));

		final ReduceDriver<EmitKey, EmitValue, Text, NullWritable> reduceDriver = new ReduceDriver<EmitKey, EmitValue, Text, NullWritable>(
				new DistinctBoaReducer());
		reduceDriver.setInput(new EmitKey("test", 0), values);
		reduceDriver.addOutput(new Text("test[] = one"), NullWritable.get());
		reduceDriver.addOutput(new Text("test[] = two"), NullWritable.get());
		reduceDriver.addOutput(new Text("test[] = three"), NullWritable.get());
		reduceDriver.addOutput(new Text("test[] = four"), NullWritable.get());
		reduceDriver.runTest();
	}
}

class DistinctBoaCombiner extends boa.runtime.BoaCombiner {
	public DistinctBoaCombiner() {
		super();

		this.tables.put("test", new Table(new boa.aggregators.DistinctAggregator(10000)));
	}
}

class DistinctBoaReducer extends boa.runtime.BoaReducer {
	public DistinctBoaReducer() {
		super();

		this.tables.put("test", new Table(new boa.aggregators.DistinctAggregator(10000)));
	}
}
