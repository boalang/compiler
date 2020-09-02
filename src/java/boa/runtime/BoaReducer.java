/*
 * Copyright 2015, Anthony Urso, Hridesh Rajan, Robert Dyer,
 *                 and Iowa State University of Science and Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package boa.runtime;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.conf.Configurable;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.log4j.Logger;

import boa.aggregators.Aggregator;
import boa.aggregators.FinishedException;
import boa.aggregators.ml.MLAggregator;
import boa.aggregators.ml.util.MLSeqCombiner;
import boa.io.EmitKey;
import boa.io.EmitValue;
import static boa.functions.BoaUtilIntrinsics.*;

/**
 * A {@link Reducer} that reduces the outputs for a single {@link EmitKey}.
 * 
 * @author anthonyu
 * @author rdyer
 */
public abstract class BoaReducer extends Reducer<EmitKey, EmitValue, Text, NullWritable> implements Configurable {
	/**
	 * A {@link Logger} that log entries can be written to.
	 * 
	 */
	protected static final Logger LOG = Logger.getLogger(BoaReducer.class);

	/**
	 * A {@link Map} from {@link String} to {@link Aggregator} indexing instantiated
	 * aggregators to their Boa identifiers.
	 */
	protected Map<String, Aggregator> aggregators;

	private Configuration conf;

	/**
	 * Construct a {@link BoaReducer}.
	 */
	protected BoaReducer() {
		this.aggregators = new HashMap<String, Aggregator>();
	}

	/** {@inheritDoc} */
	@Override
	public Configuration getConf() {
		return this.conf;
	}

	/** {@inheritDoc} */
	@Override
	public void setConf(final Configuration conf) {
		this.conf = conf;
	}

	/** {@inheritDoc} */
	@Override
	protected void reduce(final EmitKey key, final Iterable<EmitValue> values, final Context context)
			throws IOException, InterruptedException {

		// get the aggregator named by the emit key
		final Aggregator a = this.aggregators.get(key.getName());

		a.setCombining(false);
		a.start(key);
		a.setContext(context);

		if (a instanceof MLAggregator) {
			handleMLAggregator(a, key, values, context);
		} else {
			handleRegularAggregator(a, key, values, context);
		}
	}

	private void handleMLAggregator(Aggregator a, EmitKey key, Iterable<EmitValue> values,
			Reducer<EmitKey, EmitValue, Text, NullWritable>.Context context) throws IOException, InterruptedException {

		// ml aggregator
		MLAggregator mla = (MLAggregator) a;

		if (mla.trainWithCombiner) {
			System.out.println("boa reducer train with combiner");
			MLSeqCombiner combiner = new MLSeqCombiner(key, values, context);
			mla.collectToOutput(combiner.combine());
			return;
		}

		// single model
		int processedData = 0;
		for (final EmitValue value : values) {
			processedData++;
			if (value.getTuple() != null)
				mla.aggregate(value.getTuple(), value.getMetadata());
			else if (value.getData() != null)
				mla.aggregate(value.getData(), value.getMetadata());
		}
		mla.finish();

		System.out.println("boa reducer for ML processed: " + processedData + " freemem: " + freemem());
	}

	private void handleRegularAggregator(Aggregator a, EmitKey key, Iterable<EmitValue> values,
			Reducer<EmitKey, EmitValue, Text, NullWritable>.Context context) throws IOException, InterruptedException {
		for (final EmitValue value : values) {
			try {
				for (final String s : value.getData())
					a.aggregate(s, value.getMetadata());
			} catch (final FinishedException e) {
				// we are done
				return;
			} catch (final Throwable e) {
				throw new RuntimeException(e);
			}
		}
		a.finish();
	}
}