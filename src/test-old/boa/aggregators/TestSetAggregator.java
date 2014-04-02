package boa.aggregators;

import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mrunit.mapreduce.ReduceDriver;
import org.junit.Test;

import boa.io.EmitKey;
import boa.io.EmitValue;

public class TestSetAggregator {
	@Test
	public void testSetAggregatorCombineDistinct() {
		final List<EmitValue> values = new ArrayList<EmitValue>();
		values.add(new EmitValue("one"));
		values.add(new EmitValue("two"));
		values.add(new EmitValue("three"));

		final ReduceDriver<EmitKey, EmitValue, EmitKey, EmitValue> reduceDriver = new ReduceDriver<EmitKey, EmitValue, EmitKey, EmitValue>(
				new SetBoaCombiner());
		reduceDriver.setInput(new EmitKey("test", 0), values);
		reduceDriver.addOutput(new EmitKey("test", 0), new EmitValue("two"));
		reduceDriver.addOutput(new EmitKey("test", 0), new EmitValue("one"));
		reduceDriver.addOutput(new EmitKey("test", 0), new EmitValue("three"));
		reduceDriver.runTest();
	}

	@Test
	public void testSetAggregatorCombineIndistinct() {
		final List<EmitValue> values = new ArrayList<EmitValue>();
		values.add(new EmitValue("three"));
		values.add(new EmitValue("one"));
		values.add(new EmitValue("one"));
		values.add(new EmitValue("one"));
		values.add(new EmitValue("two"));
		values.add(new EmitValue("one"));
		values.add(new EmitValue("three"));
		values.add(new EmitValue("two"));
		values.add(new EmitValue("two"));
		values.add(new EmitValue("one"));
		values.add(new EmitValue("three"));
		values.add(new EmitValue("two"));
		values.add(new EmitValue("two"));
		values.add(new EmitValue("one"));
		values.add(new EmitValue("three"));
		values.add(new EmitValue("three"));

		final ReduceDriver<EmitKey, EmitValue, EmitKey, EmitValue> reduceDriver = new ReduceDriver<EmitKey, EmitValue, EmitKey, EmitValue>(
				new SetBoaCombiner());
		reduceDriver.setInput(new EmitKey("test", 0), values);
		reduceDriver.addOutput(new EmitKey("test", 0), new EmitValue("two"));
		reduceDriver.addOutput(new EmitKey("test", 0), new EmitValue("one"));
		reduceDriver.addOutput(new EmitKey("test", 0), new EmitValue("three"));
		reduceDriver.runTest();
	}

	@Test
	public void testSetAggregatorCombineLess() {
		final List<EmitValue> values = new ArrayList<EmitValue>();
		values.add(new EmitValue("one"));
		values.add(new EmitValue("two"));

		final ReduceDriver<EmitKey, EmitValue, EmitKey, EmitValue> reduceDriver = new ReduceDriver<EmitKey, EmitValue, EmitKey, EmitValue>(
				new SetBoaCombiner());
		reduceDriver.setInput(new EmitKey("test", 0), values);
		reduceDriver.addOutput(new EmitKey("test", 0), new EmitValue("two"));
		reduceDriver.addOutput(new EmitKey("test", 0), new EmitValue("one"));
		reduceDriver.runTest();
	}

	@Test
	public void testSetAggregatorCombineMore() {
		final List<EmitValue> values = new ArrayList<EmitValue>();
		values.add(new EmitValue("one"));
		values.add(new EmitValue("two"));
		values.add(new EmitValue("three"));
		values.add(new EmitValue("four"));

		final ReduceDriver<EmitKey, EmitValue, EmitKey, EmitValue> reduceDriver = new ReduceDriver<EmitKey, EmitValue, EmitKey, EmitValue>(
				new SetBoaCombiner());
		reduceDriver.setInput(new EmitKey("test", 0), values);
		reduceDriver.runTest();
	}

	@Test
	public void testSetAggregatorReduceDistinct() {
		final List<EmitValue> values = new ArrayList<EmitValue>();
		values.add(new EmitValue("one"));
		values.add(new EmitValue("two"));
		values.add(new EmitValue("three"));

		final ReduceDriver<EmitKey, EmitValue, Text, NullWritable> reduceDriver = new ReduceDriver<EmitKey, EmitValue, Text, NullWritable>(
				new SetBoaReducer());
		reduceDriver.setInput(new EmitKey("test", 0), values);
		reduceDriver.addOutput(new Text("test[] = two"), NullWritable.get());
		reduceDriver.addOutput(new Text("test[] = one"), NullWritable.get());
		reduceDriver.addOutput(new Text("test[] = three"), NullWritable.get());
		reduceDriver.runTest();
	}

	@Test
	public void testSetAggregatorReduceInistinct() {
		final List<EmitValue> values = new ArrayList<EmitValue>();
		values.add(new EmitValue("three"));
		values.add(new EmitValue("one"));
		values.add(new EmitValue("one"));
		values.add(new EmitValue("one"));
		values.add(new EmitValue("two"));
		values.add(new EmitValue("one"));
		values.add(new EmitValue("three"));
		values.add(new EmitValue("two"));
		values.add(new EmitValue("two"));
		values.add(new EmitValue("one"));
		values.add(new EmitValue("three"));
		values.add(new EmitValue("two"));
		values.add(new EmitValue("two"));
		values.add(new EmitValue("one"));
		values.add(new EmitValue("three"));
		values.add(new EmitValue("three"));

		final ReduceDriver<EmitKey, EmitValue, Text, NullWritable> reduceDriver = new ReduceDriver<EmitKey, EmitValue, Text, NullWritable>(
				new SetBoaReducer());
		reduceDriver.setInput(new EmitKey("test", 0), values);
		reduceDriver.addOutput(new Text("test[] = two"), NullWritable.get());
		reduceDriver.addOutput(new Text("test[] = one"), NullWritable.get());
		reduceDriver.addOutput(new Text("test[] = three"), NullWritable.get());
		reduceDriver.runTest();
	}

	@Test
	public void testSetAggregatorReduceLess() {
		final List<EmitValue> values = new ArrayList<EmitValue>();
		values.add(new EmitValue("one"));
		values.add(new EmitValue("two"));

		final ReduceDriver<EmitKey, EmitValue, Text, NullWritable> reduceDriver = new ReduceDriver<EmitKey, EmitValue, Text, NullWritable>(
				new SetBoaReducer());
		reduceDriver.setInput(new EmitKey("test", 0), values);
		reduceDriver.addOutput(new Text("test[] = two"), NullWritable.get());
		reduceDriver.addOutput(new Text("test[] = one"), NullWritable.get());
		reduceDriver.runTest();
	}

	@Test
	public void testSetAggregatorReduceMore() {
		final List<EmitValue> values = new ArrayList<EmitValue>();
		values.add(new EmitValue("one"));
		values.add(new EmitValue("two"));
		values.add(new EmitValue("three"));
		values.add(new EmitValue("four"));

		final ReduceDriver<EmitKey, EmitValue, Text, NullWritable> reduceDriver = new ReduceDriver<EmitKey, EmitValue, Text, NullWritable>(
				new SetBoaReducer());
		reduceDriver.setInput(new EmitKey("test", 0), values);
		reduceDriver.runTest();
	}
}

class SetBoaCombiner extends boa.runtime.BoaCombiner {
	public SetBoaCombiner() {
		super();

		this.tables.put("test", new Table(new boa.aggregators.SetAggregator(3)));
	}
}

class SetBoaReducer extends boa.runtime.BoaReducer {
	public SetBoaReducer() {
		super();

		this.tables.put("test", new Table(new boa.aggregators.SetAggregator(3)));
	}
}
