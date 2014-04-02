package boa.aggregators;

import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mrunit.mapreduce.ReduceDriver;
import org.junit.Test;

import boa.io.EmitKey;
import boa.io.EmitValue;

public class TestCollectionAggregator {
	@Test
	public void testCollectionAggregatorCombine() {
		final List<EmitValue> values = new ArrayList<EmitValue>();
		values.add(new EmitValue("one"));
		values.add(new EmitValue("two"));
		values.add(new EmitValue("three"));
		values.add(new EmitValue("four"));

		final ReduceDriver<EmitKey, EmitValue, EmitKey, EmitValue> reduceDriver = new ReduceDriver<EmitKey, EmitValue, EmitKey, EmitValue>(
				new CollectionboaCombiner());
		reduceDriver.setInput(new EmitKey("test", 0), values);
		reduceDriver.addOutput(new EmitKey("test", 0), new EmitValue("one"));
		reduceDriver.addOutput(new EmitKey("test", 0), new EmitValue("two"));
		reduceDriver.addOutput(new EmitKey("test", 0), new EmitValue("three"));
		reduceDriver.addOutput(new EmitKey("test", 0), new EmitValue("four"));
		reduceDriver.runTest();
	}

	@Test
	public void testCollectionAggregatorReduce() {
		final List<EmitValue> values = new ArrayList<EmitValue>();
		values.add(new EmitValue("one"));
		values.add(new EmitValue("two"));
		values.add(new EmitValue("three"));
		values.add(new EmitValue("four"));

		final ReduceDriver<EmitKey, EmitValue, Text, NullWritable> reduceDriver = new ReduceDriver<EmitKey, EmitValue, Text, NullWritable>(
				new CollectionboaReducer());
		reduceDriver.setInput(new EmitKey("test", 0), values);
		reduceDriver.addOutput(new Text("test[] = one"), NullWritable.get());
		reduceDriver.addOutput(new Text("test[] = two"), NullWritable.get());
		reduceDriver.addOutput(new Text("test[] = three"), NullWritable.get());
		reduceDriver.addOutput(new Text("test[] = four"), NullWritable.get());
		reduceDriver.runTest();
	}
}

class CollectionboaCombiner extends boa.runtime.boaCombiner {
	public CollectionboaCombiner() {
		super();

		this.tables.put("test", new Table(new boa.aggregators.CollectionAggregator()));
	}
}

class CollectionboaReducer extends boa.runtime.boaReducer {
	public CollectionboaReducer() {
		super();

		this.tables.put("test", new Table(new boa.aggregators.CollectionAggregator()));
	}
}
