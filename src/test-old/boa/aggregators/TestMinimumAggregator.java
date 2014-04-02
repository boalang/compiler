package boa.aggregators;

import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mrunit.mapreduce.ReduceDriver;
import org.junit.Test;

import boa.io.EmitKey;
import boa.io.EmitValue;

public class TestMinimumAggregator {
	@Test
	public void testMinimumAggregatorTopTenCombine() {
		final List<EmitValue> values = new ArrayList<EmitValue>();
		values.add(new EmitValue("two", 200));
		values.add(new EmitValue("thirteen", 1300));
		values.add(new EmitValue("six", 600));
		values.add(new EmitValue("four", 400));
		values.add(new EmitValue("seven", 700));
		values.add(new EmitValue("fourteen", 1400));
		values.add(new EmitValue("three", 300));
		values.add(new EmitValue("nine", 900));
		values.add(new EmitValue("one", 100));
		values.add(new EmitValue("twelve", 1200));
		values.add(new EmitValue("ten", 1000));
		values.add(new EmitValue("eleven", 1100));
		values.add(new EmitValue("five", 500));
		values.add(new EmitValue("eight", 800));

		final ReduceDriver<EmitKey, EmitValue, EmitKey, EmitValue> reduceDriver = new ReduceDriver<EmitKey, EmitValue, EmitKey, EmitValue>(
				new MinimumBoaCombiner());
		reduceDriver.setInput(new EmitKey("test", 0), values);
		reduceDriver.addOutput(new EmitKey("test", 0), new EmitValue("one", 100.0));
		reduceDriver.addOutput(new EmitKey("test", 0), new EmitValue("two", 200.0));
		reduceDriver.addOutput(new EmitKey("test", 0), new EmitValue("three", 300.0));
		reduceDriver.addOutput(new EmitKey("test", 0), new EmitValue("four", 400.0));
		reduceDriver.addOutput(new EmitKey("test", 0), new EmitValue("five", 500.0));
		reduceDriver.addOutput(new EmitKey("test", 0), new EmitValue("six", 600.0));
		reduceDriver.addOutput(new EmitKey("test", 0), new EmitValue("seven", 700.0));
		reduceDriver.addOutput(new EmitKey("test", 0), new EmitValue("eight", 800.0));
		reduceDriver.addOutput(new EmitKey("test", 0), new EmitValue("nine", 900.0));
		reduceDriver.addOutput(new EmitKey("test", 0), new EmitValue("ten", 1000.0));
		reduceDriver.runTest();
	}

	@Test
	public void testMinimumAggregatorTopTenReduce() {
		final List<EmitValue> values = new ArrayList<EmitValue>();
		values.add(new EmitValue("two", 200));
		values.add(new EmitValue("thirteen", 1300));
		values.add(new EmitValue("six", 600));
		values.add(new EmitValue("four", 400));
		values.add(new EmitValue("seven", 700));
		values.add(new EmitValue("fourteen", 1400));
		values.add(new EmitValue("three", 300));
		values.add(new EmitValue("nine", 900));
		values.add(new EmitValue("one", 100));
		values.add(new EmitValue("twelve", 1200));
		values.add(new EmitValue("ten", 1000));
		values.add(new EmitValue("eleven", 1100));
		values.add(new EmitValue("five", 500));
		values.add(new EmitValue("eight", 800));

		final ReduceDriver<EmitKey, EmitValue, Text, NullWritable> reduceDriver10 = new ReduceDriver<EmitKey, EmitValue, Text, NullWritable>(
				new MinimumBoaReducerTen());
		reduceDriver10.setInput(new EmitKey("test", 0), values);
		reduceDriver10.addOutput(new Text("test[] = one weight 100.0"), NullWritable.get());
		reduceDriver10.addOutput(new Text("test[] = two weight 200.0"), NullWritable.get());
		reduceDriver10.addOutput(new Text("test[] = three weight 300.0"), NullWritable.get());
		reduceDriver10.addOutput(new Text("test[] = four weight 400.0"), NullWritable.get());
		reduceDriver10.addOutput(new Text("test[] = five weight 500.0"), NullWritable.get());
		reduceDriver10.addOutput(new Text("test[] = six weight 600.0"), NullWritable.get());
		reduceDriver10.addOutput(new Text("test[] = seven weight 700.0"), NullWritable.get());
		reduceDriver10.addOutput(new Text("test[] = eight weight 800.0"), NullWritable.get());
		reduceDriver10.addOutput(new Text("test[] = nine weight 900.0"), NullWritable.get());
		reduceDriver10.addOutput(new Text("test[] = ten weight 1000.0"), NullWritable.get());
		reduceDriver10.runTest();

		final ReduceDriver<EmitKey, EmitValue, Text, NullWritable> reduceDriver1 = new ReduceDriver<EmitKey, EmitValue, Text, NullWritable>(
				new MinimumBoaReducerOne());
		reduceDriver1.setInput(new EmitKey("test", 0), values);
		reduceDriver1.addOutput(new Text("test[] = one weight 100.0"), NullWritable.get());
		reduceDriver1.runTest();
	}

	@Test
	public void testMinimumAggregatorAllEqual() {
		final List<EmitValue> values = new ArrayList<EmitValue>();
		values.add(new EmitValue("two", 100));
		values.add(new EmitValue("thirteen", 100));
		values.add(new EmitValue("six", 100));
		values.add(new EmitValue("four", 100));
		values.add(new EmitValue("seven", 100));
		values.add(new EmitValue("fourteen", 100));
		values.add(new EmitValue("three", 100));
		values.add(new EmitValue("nine", 100));
		values.add(new EmitValue("one", 100));
		values.add(new EmitValue("twelve", 100));
		values.add(new EmitValue("ten", 100));
		values.add(new EmitValue("eleven", 100));
		values.add(new EmitValue("five", 100));
		values.add(new EmitValue("eight", 100));

		final ReduceDriver<EmitKey, EmitValue, Text, NullWritable> reduceDriver10 = new ReduceDriver<EmitKey, EmitValue, Text, NullWritable>(
				new MinimumBoaReducerTen());
		reduceDriver10.setInput(new EmitKey("test", 0), values);
		reduceDriver10.addOutput(new Text("test[] = eight weight 100.0"), NullWritable.get());
		reduceDriver10.addOutput(new Text("test[] = eleven weight 100.0"), NullWritable.get());
		reduceDriver10.addOutput(new Text("test[] = five weight 100.0"), NullWritable.get());
		reduceDriver10.addOutput(new Text("test[] = four weight 100.0"), NullWritable.get());
		reduceDriver10.addOutput(new Text("test[] = fourteen weight 100.0"), NullWritable.get());
		reduceDriver10.addOutput(new Text("test[] = nine weight 100.0"), NullWritable.get());
		reduceDriver10.addOutput(new Text("test[] = one weight 100.0"), NullWritable.get());
		reduceDriver10.addOutput(new Text("test[] = seven weight 100.0"), NullWritable.get());
		reduceDriver10.addOutput(new Text("test[] = six weight 100.0"), NullWritable.get());
		reduceDriver10.addOutput(new Text("test[] = ten weight 100.0"), NullWritable.get());
		reduceDriver10.runTest();

		final ReduceDriver<EmitKey, EmitValue, Text, NullWritable> reduceDriver1 = new ReduceDriver<EmitKey, EmitValue, Text, NullWritable>(
				new MinimumBoaReducerOne());
		reduceDriver1.setInput(new EmitKey("test", 0), values);
		reduceDriver1.addOutput(new Text("test[] = eight weight 100.0"), NullWritable.get());
		reduceDriver1.runTest();
	}
}

class MinimumBoaCombiner extends boa.runtime.BoaCombiner {
	public MinimumBoaCombiner() {
		super();

		this.tables.put("test", new Table(new boa.aggregators.MinimumAggregator(10)));
	}
}

class MinimumBoaReducerTen extends boa.runtime.BoaReducer {
	public MinimumBoaReducerTen() {
		super();

		this.tables.put("test", new Table(new boa.aggregators.MinimumAggregator(10)));
	}
}

class MinimumBoaReducerOne extends boa.runtime.BoaReducer {
	public MinimumBoaReducerOne() {
		super();

		this.tables.put("test", new Table(new boa.aggregators.MinimumAggregator(1)));
	}
}
