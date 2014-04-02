package boa.aggregators;

import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.CounterGroup;
import org.apache.hadoop.mapreduce.Counters;
import org.apache.hadoop.mrunit.mapreduce.ReduceDriver;
import org.junit.Assert;
import org.junit.Test;

import boa.io.EmitKey;
import boa.io.EmitValue;

public class TestMrcounterAggregator {
	@Test
	public void testMrcounterAggregatorCombineUnnamed() {
		final List<EmitValue> values = new ArrayList<EmitValue>();
		values.add(new EmitValue(1));
		values.add(new EmitValue(2));

		final ReduceDriver<EmitKey, EmitValue, EmitKey, EmitValue> reduceDriver = new ReduceDriver<EmitKey, EmitValue, EmitKey, EmitValue>(
				new MrcounterBoaCombiner());
		reduceDriver.setInput(new EmitKey("test", 0), values);
		reduceDriver.runTest();
		final Counters counters = reduceDriver.getCounters();
		final CounterGroup group = counters.getGroup("Boa Counters");

		Assert.assertEquals("counter value is wrong", 3, group.findCounter("Unnamed counter").getValue());
	}

	@Test
	public void testMrcounterAggregatorCombineNamed() {
		final List<EmitValue> values = new ArrayList<EmitValue>();
		values.add(new EmitValue(1));
		values.add(new EmitValue(2));

		final ReduceDriver<EmitKey, EmitValue, EmitKey, EmitValue> reduceDriver = new ReduceDriver<EmitKey, EmitValue, EmitKey, EmitValue>(
				new MrcounterBoaCombiner());
		reduceDriver.setInput(new EmitKey("[three]", "test", 0), values);
		reduceDriver.runTest();
		final Counters counters = reduceDriver.getCounters();
		final CounterGroup group = counters.getGroup("Boa Counters");

		Assert.assertEquals("counter value is wrong", 3, group.findCounter("three").getValue());
	}

	@Test
	public void testMrcounterAggregatorCombineGroup() {
		final List<EmitValue> values = new ArrayList<EmitValue>();
		values.add(new EmitValue(1));
		values.add(new EmitValue(2));

		final ReduceDriver<EmitKey, EmitValue, EmitKey, EmitValue> reduceDriver = new ReduceDriver<EmitKey, EmitValue, EmitKey, EmitValue>(
				new MrcounterBoaCombiner());
		reduceDriver.setInput(new EmitKey("[Job Statistics][Some counter]", "test", 0), values);
		reduceDriver.runTest();
		final Counters counters = reduceDriver.getCounters();
		final CounterGroup group = counters.getGroup("Job Statistics");

		Assert.assertEquals("counter value is wrong", 3, group.findCounter("Some counter").getValue());
	}

	@Test
	public void testMrcounterAggregatorCombineMultilevel() {
		final List<EmitValue> values = new ArrayList<EmitValue>();
		values.add(new EmitValue(1));
		values.add(new EmitValue(2));

		final ReduceDriver<EmitKey, EmitValue, EmitKey, EmitValue> reduceDriver = new ReduceDriver<EmitKey, EmitValue, EmitKey, EmitValue>(
				new MrcounterBoaCombiner());
		reduceDriver.setInput(new EmitKey("[Job Statistics][Some][counter]", "test", 0), values);
		reduceDriver.runTest();
		final Counters counters = reduceDriver.getCounters();
		final CounterGroup group = counters.getGroup("Job Statistics");

		Assert.assertEquals("counter value is wrong", 3, group.findCounter("Somecounter").getValue());
	}

	@Test
	public void testMrcounterAggregatorReduce() {
		final List<EmitValue> values = new ArrayList<EmitValue>();
		values.add(new EmitValue("one", 1));
		values.add(new EmitValue("two", 2));
		values.add(new EmitValue("three", 3));
		values.add(new EmitValue("four", 4));

		final ReduceDriver<EmitKey, EmitValue, Text, NullWritable> reduceDriver = new ReduceDriver<EmitKey, EmitValue, Text, NullWritable>(
				new MrcounterBoaReducer());
		reduceDriver.setInput(new EmitKey("test", 0), values);
		reduceDriver.runTest();
		final Counters counters = reduceDriver.getCounters();
		final CounterGroup group = counters.getGroup("test[]");

		Assert.assertEquals("counter value is wrong", 0, group.findCounter("one").getValue());
		Assert.assertEquals("counter value is wrong", 0, group.findCounter("two").getValue());
		Assert.assertEquals("counter value is wrong", 0, group.findCounter("three").getValue());
		Assert.assertEquals("counter value is wrong", 0, group.findCounter("four").getValue());
	}
}

class MrcounterBoaCombiner extends boa.runtime.BoaCombiner {
	public MrcounterBoaCombiner() {
		super();

		this.tables.put("test", new Table(new boa.aggregators.MrcounterAggregator()));
	}
}

class MrcounterBoaReducer extends boa.runtime.BoaReducer {
	public MrcounterBoaReducer() {
		super();

		this.tables.put("test", new Table(new boa.aggregators.MrcounterAggregator()));
	}
}
