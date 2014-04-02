package boa.aggregators;

import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mrunit.mapreduce.ReduceDriver;
import org.junit.Test;

import boa.io.EmitKey;
import boa.io.EmitValue;

public class TestTopAggregator {
	@Test
	public void testTopAggregatorTopTenCombine() {
		final List<EmitValue> values = new ArrayList<EmitValue>();
		values.add(new EmitValue("two"));
		values.add(new EmitValue("twelve"));
		values.add(new EmitValue("eleven"));
		values.add(new EmitValue("fourteen"));
		values.add(new EmitValue("thirteen"));
		values.add(new EmitValue("twelve"));
		values.add(new EmitValue("thirteen"));
		values.add(new EmitValue("eleven"));
		values.add(new EmitValue("four"));
		values.add(new EmitValue("six"));
		values.add(new EmitValue("two"));
		values.add(new EmitValue("twelve"));
		values.add(new EmitValue("thirteen"));
		values.add(new EmitValue("six"));
		values.add(new EmitValue("eleven"));
		values.add(new EmitValue("twelve"));
		values.add(new EmitValue("thirteen"));
		values.add(new EmitValue("four"));
		values.add(new EmitValue("fourteen"));
		values.add(new EmitValue("twelve"));
		values.add(new EmitValue("six"));
		values.add(new EmitValue("seven"));
		values.add(new EmitValue("four"));
		values.add(new EmitValue("six"));
		values.add(new EmitValue("eleven"));
		values.add(new EmitValue("twelve"));
		values.add(new EmitValue("four"));
		values.add(new EmitValue("thirteen"));
		values.add(new EmitValue("six"));
		values.add(new EmitValue("twelve"));
		values.add(new EmitValue("eleven"));
		values.add(new EmitValue("seven"));
		values.add(new EmitValue("twelve"));
		values.add(new EmitValue("fourteen"));
		values.add(new EmitValue("six"));
		values.add(new EmitValue("eleven"));
		values.add(new EmitValue("ten"));
		values.add(new EmitValue("nine"));
		values.add(new EmitValue("thirteen"));
		values.add(new EmitValue("twelve"));
		values.add(new EmitValue("fourteen"));
		values.add(new EmitValue("twelve"));
		values.add(new EmitValue("nine"));
		values.add(new EmitValue("ten"));
		values.add(new EmitValue("seven"));
		values.add(new EmitValue("eight"));
		values.add(new EmitValue("nine"));
		values.add(new EmitValue("eleven"));
		values.add(new EmitValue("fourteen"));
		values.add(new EmitValue("eight"));
		values.add(new EmitValue("ten"));
		values.add(new EmitValue("fourteen"));
		values.add(new EmitValue("three"));
		values.add(new EmitValue("thirteen"));
		values.add(new EmitValue("nine"));
		values.add(new EmitValue("fourteen"));
		values.add(new EmitValue("seven"));
		values.add(new EmitValue("three"));
		values.add(new EmitValue("five"));
		values.add(new EmitValue("eight"));
		values.add(new EmitValue("fourteen"));
		values.add(new EmitValue("thirteen"));
		values.add(new EmitValue("ten"));
		values.add(new EmitValue("seven"));
		values.add(new EmitValue("ten"));
		values.add(new EmitValue("eight"));
		values.add(new EmitValue("fourteen"));
		values.add(new EmitValue("nine"));
		values.add(new EmitValue("three"));
		values.add(new EmitValue("five"));
		values.add(new EmitValue("eleven"));
		values.add(new EmitValue("seven"));
		values.add(new EmitValue("ten"));
		values.add(new EmitValue("nine"));
		values.add(new EmitValue("five"));
		values.add(new EmitValue("fourteen"));
		values.add(new EmitValue("twelve"));
		values.add(new EmitValue("nine"));
		values.add(new EmitValue("eleven"));
		values.add(new EmitValue("fourteen"));
		values.add(new EmitValue("five"));
		values.add(new EmitValue("thirteen"));
		values.add(new EmitValue("seven"));
		values.add(new EmitValue("fourteen"));
		values.add(new EmitValue("one"));
		values.add(new EmitValue("nine"));
		values.add(new EmitValue("eight"));
		values.add(new EmitValue("thirteen"));
		values.add(new EmitValue("nine"));
		values.add(new EmitValue("ten"));
		values.add(new EmitValue("ten"));
		values.add(new EmitValue("twelve"));
		values.add(new EmitValue("eight"));
		values.add(new EmitValue("thirteen"));
		values.add(new EmitValue("eleven"));
		values.add(new EmitValue("ten"));
		values.add(new EmitValue("fourteen"));
		values.add(new EmitValue("thirteen"));
		values.add(new EmitValue("eight"));
		values.add(new EmitValue("eleven"));
		values.add(new EmitValue("ten"));
		values.add(new EmitValue("thirteen"));
		values.add(new EmitValue("five"));
		values.add(new EmitValue("fourteen"));
		values.add(new EmitValue("eight"));

		final ReduceDriver<EmitKey, EmitValue, EmitKey, EmitValue> reduceDriver = new ReduceDriver<EmitKey, EmitValue, EmitKey, EmitValue>(
				new TopBoaCombiner());
		reduceDriver.setInput(new EmitKey("test", 0), values);
		reduceDriver.addOutput(new EmitKey("test", 0), new EmitValue("ten", 10));
		reduceDriver.addOutput(new EmitKey("test", 0), new EmitValue("five", 5));
		reduceDriver.addOutput(new EmitKey("test", 0), new EmitValue("one", 1));
		reduceDriver.addOutput(new EmitKey("test", 0), new EmitValue("eleven", 11));
		reduceDriver.addOutput(new EmitKey("test", 0), new EmitValue("nine", 9));
		reduceDriver.addOutput(new EmitKey("test", 0), new EmitValue("eight", 8));
		reduceDriver.addOutput(new EmitKey("test", 0), new EmitValue("six", 6));
		reduceDriver.addOutput(new EmitKey("test", 0), new EmitValue("two", 2));
		reduceDriver.addOutput(new EmitKey("test", 0), new EmitValue("seven", 7));
		reduceDriver.addOutput(new EmitKey("test", 0), new EmitValue("three", 3));
		reduceDriver.addOutput(new EmitKey("test", 0), new EmitValue("twelve", 12));
		reduceDriver.addOutput(new EmitKey("test", 0), new EmitValue("fourteen", 14));
		reduceDriver.addOutput(new EmitKey("test", 0), new EmitValue("four", 4));
		reduceDriver.addOutput(new EmitKey("test", 0), new EmitValue("thirteen", 13));
		reduceDriver.runTest();
	}

	@Test
	public void testTopAggregatorTopTenReduce() {
		final List<EmitValue> values = new ArrayList<EmitValue>();
		values.add(new EmitValue("two", 2));
		values.add(new EmitValue("thirteen", 13));
		values.add(new EmitValue("six", 6));
		values.add(new EmitValue("four", 4));
		values.add(new EmitValue("seven", 7));
		values.add(new EmitValue("fourteen", 14));
		values.add(new EmitValue("three", 3));
		values.add(new EmitValue("nine", 9));
		values.add(new EmitValue("one", 1));
		values.add(new EmitValue("twelve", 12));
		values.add(new EmitValue("ten", 10));
		values.add(new EmitValue("eleven", 11));
		values.add(new EmitValue("five", 5));
		values.add(new EmitValue("eight", 8));

		final ReduceDriver<EmitKey, EmitValue, Text, NullWritable> reduceDriver10 = new ReduceDriver<EmitKey, EmitValue, Text, NullWritable>(
				new TopBoaReducerTen());
		reduceDriver10.setInput(new EmitKey("test", 0), values);
		reduceDriver10.addOutput(new Text("test[] = fourteen, 14, 0"), NullWritable.get());
		reduceDriver10.addOutput(new Text("test[] = thirteen, 13, 0"), NullWritable.get());
		reduceDriver10.addOutput(new Text("test[] = twelve, 12, 0"), NullWritable.get());
		reduceDriver10.addOutput(new Text("test[] = eleven, 11, 0"), NullWritable.get());
		reduceDriver10.addOutput(new Text("test[] = ten, 10, 0"), NullWritable.get());
		reduceDriver10.addOutput(new Text("test[] = nine, 9, 0"), NullWritable.get());
		reduceDriver10.addOutput(new Text("test[] = eight, 8, 0"), NullWritable.get());
		reduceDriver10.addOutput(new Text("test[] = seven, 7, 0"), NullWritable.get());
		reduceDriver10.addOutput(new Text("test[] = six, 6, 0"), NullWritable.get());
		reduceDriver10.addOutput(new Text("test[] = five, 5, 0"), NullWritable.get());
		reduceDriver10.runTest();

		reduceDriver10.setInput(new EmitKey("[test]", "test", 0), new ArrayList<EmitValue>());
		reduceDriver10.resetOutput();
		reduceDriver10.runTest();

		final ReduceDriver<EmitKey, EmitValue, Text, NullWritable> reduceDriver1 = new ReduceDriver<EmitKey, EmitValue, Text, NullWritable>(
				new TopBoaReducerOne());
		reduceDriver1.setInput(new EmitKey("test", 0), values);
		reduceDriver1.addOutput(new Text("test[] = fourteen, 14, 0"), NullWritable.get());
		reduceDriver1.runTest();
	}

	@Test
	public void testTopAggregatorAllEqual() {
		final List<EmitValue> values = new ArrayList<EmitValue>();
		values.add(new EmitValue("two", 1));
		values.add(new EmitValue("thirteen", 1));
		values.add(new EmitValue("six", 1));
		values.add(new EmitValue("four", 1));
		values.add(new EmitValue("seven", 1));
		values.add(new EmitValue("fourteen", 1));
		values.add(new EmitValue("three", 1));
		values.add(new EmitValue("nine", 1));
		values.add(new EmitValue("one", 1));
		values.add(new EmitValue("twelve", 1));
		values.add(new EmitValue("ten", 1));
		values.add(new EmitValue("eleven", 1));
		values.add(new EmitValue("five", 1));
		values.add(new EmitValue("eight", 1));

		final ReduceDriver<EmitKey, EmitValue, Text, NullWritable> reduceDriver10 = new ReduceDriver<EmitKey, EmitValue, Text, NullWritable>(
				new TopBoaReducerTen());
		reduceDriver10.setInput(new EmitKey("test", 0), values);
		reduceDriver10.addOutput(new Text("test[] = eight, 1, 0"), NullWritable.get());
		reduceDriver10.addOutput(new Text("test[] = eleven, 1, 0"), NullWritable.get());
		reduceDriver10.addOutput(new Text("test[] = five, 1, 0"), NullWritable.get());
		reduceDriver10.addOutput(new Text("test[] = four, 1, 0"), NullWritable.get());
		reduceDriver10.addOutput(new Text("test[] = fourteen, 1, 0"), NullWritable.get());
		reduceDriver10.addOutput(new Text("test[] = nine, 1, 0"), NullWritable.get());
		reduceDriver10.addOutput(new Text("test[] = one, 1, 0"), NullWritable.get());
		reduceDriver10.addOutput(new Text("test[] = seven, 1, 0"), NullWritable.get());
		reduceDriver10.addOutput(new Text("test[] = six, 1, 0"), NullWritable.get());
		reduceDriver10.addOutput(new Text("test[] = ten, 1, 0"), NullWritable.get());
		reduceDriver10.runTest();

		final ReduceDriver<EmitKey, EmitValue, Text, NullWritable> reduceDriver1 = new ReduceDriver<EmitKey, EmitValue, Text, NullWritable>(
				new TopBoaReducerOne());
		reduceDriver1.setInput(new EmitKey("test", 0), values);
		reduceDriver1.addOutput(new Text("test[] = eight, 1, 0"), NullWritable.get());
		reduceDriver1.runTest();
	}
}

class TopBoaCombiner extends boa.runtime.BoaCombiner {
	public TopBoaCombiner() {
		super();

		this.tables.put("test", new Table(new boa.aggregators.TopAggregator(10)));
	}
}

class TopBoaReducerTen extends boa.runtime.BoaReducer {
	public TopBoaReducerTen() {
		super();

		this.tables.put("test", new Table(new boa.aggregators.TopAggregator(10)));
	}
}

class TopBoaReducerOne extends boa.runtime.BoaReducer {
	public TopBoaReducerOne() {
		super();

		this.tables.put("test", new Table(new boa.aggregators.TopAggregator(1)));
	}
}
