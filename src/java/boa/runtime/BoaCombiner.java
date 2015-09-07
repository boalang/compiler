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
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.log4j.Logger;

import boa.aggregators.Aggregator;
import boa.aggregators.FinishedException;
import boa.io.EmitKey;
import boa.io.EmitValue;


/**
 * A {@link Reducer} that pre-reduces the outputs from a single mapper node in
 * order to save I/O.
 * 
 * @author anthonyu
 * @author rdyer
 */
public abstract class BoaCombiner extends Reducer<EmitKey, EmitValue, EmitKey, EmitValue> implements Configurable {
	/**
	 * A {@link Logger} that log entries can be written to.
	 * 
	 */
	protected static final Logger LOG = Logger.getLogger(BoaCombiner.class);

	/**
	 * A {@link Map} from {@link String} to {@link Aggregator} indexing instantiated
	 * aggregators to their Boa identifiers.
	 */
	protected Map<String, Aggregator> aggregators;

	private Configuration conf;
	private boolean robust;

	/**
	 * Construct a {@link BoaCombiner}.
	 */
	protected BoaCombiner() {
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
		this.robust = conf.getBoolean("boa.runtime.robust", false);
	}

	/** {@inheritDoc} */
	@Override
	protected void reduce(final EmitKey key, final Iterable<EmitValue> values, final Context context) throws IOException, InterruptedException {
		// if we can't combine, just pass the output through
		// TODO: find away to avoid combiner entirely when non-associative
		if (!this.aggregators.containsKey(key.getKey())) {
			for (final EmitValue value : values)
				context.write(key, value);

			return;
		}

		// get the aggregator named by the emit key
		final Aggregator a = this.aggregators.get(key.getKey());

		a.setCombining(true);
		a.start(key);
		a.setContext(context);

		for (final EmitValue value : values)
			try {
				for (final String s : value.getData())
					a.aggregate(s, value.getMetadata());
			} catch (final FinishedException e) {
				// we are done
				return;
			} catch (final Throwable e) {
				throw new RuntimeException(e);
			}

		a.finish();
	}
}
