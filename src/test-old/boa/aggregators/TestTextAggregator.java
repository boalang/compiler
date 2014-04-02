package boa.aggregators;

import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mrunit.mapreduce.ReduceDriver;
import org.junit.Test;

import boa.io.EmitKey;
import boa.io.EmitValue;

public class TestTextAggregator {
	@Test
	public void testTextAggregatorCombine() {
		final List<EmitValue> values = new ArrayList<EmitValue>();
		values.add(new EmitValue("one"));
		values.add(new EmitValue("two"));
		values.add(new EmitValue("three"));
		values.add(new EmitValue("four"));

		final ReduceDriver<EmitKey, EmitValue, EmitKey, EmitValue> reduceDriver = new ReduceDriver<EmitKey, EmitValue, EmitKey, EmitValue>(
				new TextBoaCombiner());
		reduceDriver.setInput(new EmitKey("test", 0), values);
		reduceDriver.addOutput(new EmitKey("test", 0), new EmitValue("one"));
		reduceDriver.addOutput(new EmitKey("test", 0), new EmitValue("two"));
		reduceDriver.addOutput(new EmitKey("test", 0), new EmitValue("three"));
		reduceDriver.addOutput(new EmitKey("test", 0), new EmitValue("four"));
		reduceDriver.runTest();
	}

	@Test
	public void testTextAggregatorReduce() {
		final List<EmitValue> values = new ArrayList<EmitValue>();
		values.add(new EmitValue("one"));
		values.add(new EmitValue("two"));
		values.add(new EmitValue("three"));
		values.add(new EmitValue("four"));

		final ReduceDriver<EmitKey, EmitValue, Text, NullWritable> reduceDriver = new ReduceDriver<EmitKey, EmitValue, Text, NullWritable>(
				new TextBoaReducer());
		reduceDriver.setInput(new EmitKey("test", 0), values);
		reduceDriver.addOutput(new Text("one"), NullWritable.get());
		reduceDriver.addOutput(new Text("two"), NullWritable.get());
		reduceDriver.addOutput(new Text("three"), NullWritable.get());
		reduceDriver.addOutput(new Text("four"), NullWritable.get());
		reduceDriver.runTest();
	}
}

class TextBoaCombiner extends boa.runtime.BoaCombiner {
	public TextBoaCombiner() {
		super();

		this.tables.put("test", new Table(new boa.aggregators.TextAggregator()));
	}
}

class TextBoaReducer extends boa.runtime.BoaReducer {
	public TextBoaReducer() {
		super();

		this.tables.put("test", new Table(new boa.aggregators.TextAggregator()));
	}
}
