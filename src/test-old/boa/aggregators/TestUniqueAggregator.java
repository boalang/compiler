package boa.aggregators;

import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mrunit.mapreduce.ReduceDriver;
import org.junit.Test;

import boa.io.EmitKey;
import boa.io.EmitValue;

public class TestUniqueAggregator {
	@Test
	public void testUniqueAggregatorCombineDistinct() {
		final List<EmitValue> values = new ArrayList<EmitValue>();
		values.add(new EmitValue("one"));
		values.add(new EmitValue("two"));
		values.add(new EmitValue("three"));
		values.add(new EmitValue("four"));

		final ReduceDriver<EmitKey, EmitValue, EmitKey, EmitValue> reduceDriver = new ReduceDriver<EmitKey, EmitValue, EmitKey, EmitValue>(
				new UniqueBoaCombiner());
		reduceDriver.setInput(new EmitKey("test", 0), values);
		reduceDriver.addOutput(new EmitKey("test", 0), new EmitValue("one"));
		reduceDriver.addOutput(new EmitKey("test", 0), new EmitValue("two"));
		reduceDriver.addOutput(new EmitKey("test", 0), new EmitValue("three"));
		reduceDriver.addOutput(new EmitKey("test", 0), new EmitValue("four"));
		reduceDriver.runTest();
	}

	@Test
	public void testUniqueAggregatorCombineIndistinct() {
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
				new UniqueBoaCombiner());
		reduceDriver.setInput(new EmitKey("test", 0), values);
		reduceDriver.addOutput(new EmitKey("test", 0), new EmitValue("one"));
		reduceDriver.addOutput(new EmitKey("test", 0), new EmitValue("two"));
		reduceDriver.addOutput(new EmitKey("test", 0), new EmitValue("three"));
		reduceDriver.addOutput(new EmitKey("test", 0), new EmitValue("four"));
		reduceDriver.runTest();
	}

	@Test
	public void testUniqueAggregatorReduceDistinct() {
		final List<EmitValue> values = new ArrayList<EmitValue>();
		values.add(new EmitValue("one"));
		values.add(new EmitValue("two"));
		values.add(new EmitValue("three"));
		values.add(new EmitValue("four"));

		final ReduceDriver<EmitKey, EmitValue, Text, NullWritable> reduceDriver = new ReduceDriver<EmitKey, EmitValue, Text, NullWritable>(
				new UniqueBoaReducer());
		reduceDriver.setInput(new EmitKey("test", 0), values);
		reduceDriver.addOutput(new Text("test[] = 4"), NullWritable.get());
		reduceDriver.runTest();
	}

	@Test
	public void testUniqueAggregatorReduceIndistinct() {
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
				new UniqueBoaReducer());
		reduceDriver.setInput(new EmitKey("test", 0), values);
		reduceDriver.addOutput(new Text("test[] = 4"), NullWritable.get());
		reduceDriver.runTest();
	}
}

class UniqueBoaCombiner extends boa.runtime.BoaCombiner {
	public UniqueBoaCombiner() {
		super();

		this.tables.put("test", new Table(new boa.aggregators.UniqueAggregator(10000)));
	}
}

class UniqueBoaReducer extends boa.runtime.BoaReducer {
	public UniqueBoaReducer() {
		super();

		this.tables.put("test", new Table(new boa.aggregators.UniqueAggregator(10000)));
	}
}
