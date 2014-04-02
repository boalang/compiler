package boa.aggregators;

import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mrunit.mapreduce.ReduceDriver;
import org.junit.Test;

import boa.io.EmitKey;
import boa.io.EmitValue;

public class TestFloatMeanAggregator {
	@Test
	public void testFloatMeanAggregatorCombine() {
		final List<EmitValue> values = new ArrayList<EmitValue>();
		values.add(new EmitValue("1"));
		values.add(new EmitValue("2.0"));
		values.add(new EmitValue(3));
		values.add(new EmitValue(4.0));

		new ReduceDriver<EmitKey, EmitValue, EmitKey, EmitValue>(new FloatMeanBoaCombiner()).withInput(new EmitKey("test", 0), values)
				.withOutput(new EmitKey("test", 0), new EmitValue("10.0", "4")).runTest();
	}

	@Test
	public void testFloatMeanAggregatorReduce() {
		final List<EmitValue> values = new ArrayList<EmitValue>();
		values.add(new EmitValue("1"));
		values.add(new EmitValue("2.0"));
		values.add(new EmitValue(3));
		values.add(new EmitValue(4.0));

		new ReduceDriver<EmitKey, EmitValue, Text, NullWritable>(new FloatMeanBoaReducer()).withInput(new EmitKey("test", 0), values)
				.withOutput(new Text("test[] = 2.5"), NullWritable.get()).runTest();
	}
}

class FloatMeanBoaCombiner extends boa.runtime.BoaCombiner {
	public FloatMeanBoaCombiner() {
		super();

		this.tables.put("test", new Table(new boa.aggregators.FloatMeanAggregator()));
	}
}

class FloatMeanBoaReducer extends boa.runtime.BoaReducer {
	public FloatMeanBoaReducer() {
		super();

		this.tables.put("test", new Table(new boa.aggregators.FloatMeanAggregator()));
	}
}
