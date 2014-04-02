package boa.aggregators;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mrunit.mapreduce.ReduceDriver;
import org.apache.hadoop.mrunit.types.Pair;
import org.junit.Assert;
import org.junit.Test;

import boa.io.EmitKey;
import boa.io.EmitValue;

public class TestFloatQuantileAggregator {
	@Test
	public void testFloatQuantileAggregatorCombine() throws IOException {
		final List<EmitValue> values = new ArrayList<EmitValue>();

		final BufferedReader r = new BufferedReader(new FileReader("test/normals"));
		try {
			String line = null;
			while ((line = r.readLine()) != null)
				values.add(new EmitValue(Long.parseLong(line)));
		} finally {
			r.close();
		}

		final ReduceDriver<EmitKey, EmitValue, EmitKey, EmitValue> reduceDriver = new ReduceDriver<EmitKey, EmitValue, EmitKey, EmitValue>(
				new FloatQuartileBoaCombiner());
		reduceDriver.setInput(new EmitKey("test", 0), values);
		final List<Pair<EmitKey, EmitValue>> output = reduceDriver.run();

		Assert.assertEquals("size is wrong", 575, output.size());

		Assert.assertEquals("outputs are wrong", new EmitKey("test", 0), output.get(14).getFirst());
		Assert.assertEquals("outputs are wrong", new EmitValue("-292.0", 1), output.get(14).getSecond());

		Assert.assertEquals("outputs are wrong", new EmitKey("test", 0), output.get(256).getFirst());
		Assert.assertEquals("outputs are wrong", new EmitValue("-32.0", 42), output.get(256).getSecond());
	}

	@Test
	public void testFloatQuantileAggregator() {
		final List<EmitValue> values = new ArrayList<EmitValue>();
		values.add(new EmitValue(3));
		values.add(new EmitValue(6));
		values.add(new EmitValue(7));
		values.add(new EmitValue(8));
		values.add(new EmitValue(8));
		values.add(new EmitValue(10));
		values.add(new EmitValue(13));
		values.add(new EmitValue(15));
		values.add(new EmitValue(16));
		values.add(new EmitValue(20));

		final ReduceDriver<EmitKey, EmitValue, Text, NullWritable> reduceDriver = new ReduceDriver<EmitKey, EmitValue, Text, NullWritable>(
				new FloatQuartileBoaReducer());
		reduceDriver.setInput(new EmitKey("test", 0), values);
		reduceDriver.addOutput(new Text("test[] = [7.0, 8.0, 15.0, 20.0]"), NullWritable.get());
		reduceDriver.runTest();
	}

	@Test
	public void testFloatQuantileAggregatorBig() throws IOException {
		final List<EmitValue> values = new ArrayList<EmitValue>();

		final BufferedReader r = new BufferedReader(new FileReader("test/normals"));
		try {
			String line = null;
			while ((line = r.readLine()) != null)
				values.add(new EmitValue(Long.parseLong(line)));
		} finally {
			r.close();
		}

		final ReduceDriver<EmitKey, EmitValue, Text, NullWritable> reduceDriver = new ReduceDriver<EmitKey, EmitValue, Text, NullWritable>(
				new FloatQuartileBoaReducer());
		reduceDriver.setInput(new EmitKey("test", 0), values);
		reduceDriver.addOutput(new Text("test[] = [-66.0, 1.0, 69.0, 356.0]"), NullWritable.get());
		reduceDriver.runTest();
	}
}

class FloatQuartileBoaCombiner extends boa.runtime.BoaCombiner {
	public FloatQuartileBoaCombiner() {
		super();

		this.tables.put("test", new Table(new boa.aggregators.FloatQuantileAggregator(5)));
	}
}

class FloatQuartileBoaReducer extends boa.runtime.BoaReducer {
	public FloatQuartileBoaReducer() {
		super();

		this.tables.put("test", new Table(new boa.aggregators.FloatQuantileAggregator(5)));
	}
}
