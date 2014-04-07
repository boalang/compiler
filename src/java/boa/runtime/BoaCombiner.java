/*
 * Copyright 2014, Anthony Urso, Hridesh Rajan, Robert Dyer, 
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

import boa.aggregators.FinishedException;
import boa.aggregators.Table;
import boa.io.EmitKey;
import boa.io.EmitValue;


/**
 * A {@link Reducer} that pre-reduces the outputs from a single mapper node in
 * order to save I/O.
 * 
 * @author anthonyu
 */
public abstract class BoaCombiner extends Reducer<EmitKey, EmitValue, EmitKey, EmitValue> implements Configurable {
	/**
	 * A {@link Logger} that log entries can be written to.
	 * 
	 */
	protected static final Logger LOG = Logger.getLogger(BoaCombiner.class);

	/**
	 * A {@link Map} from {@link String} to {@link Table} indexing instantiated
	 * tables to their Boa identifiers.
	 */
	protected Map<String, Table> tables;

	private Configuration conf;
	private boolean robust;

	/**
	 * Construct a {@link BoaCombiner}.
	 */
	protected BoaCombiner() {
		this.tables = new HashMap<String, Table>();
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
		if (!this.tables.containsKey(key.getKey())) {
			for (final EmitValue value : values)
				context.write(key, value);

			return;
		}

		// get the table named by the emit key
		final Table t = this.tables.get(key.getKey());

		t.setCombining(true);
		t.start(key);
		t.setContext(context);

		for (final EmitValue value : values)
			try {
				t.aggregate(value.getData(), value.getMetadata());
			} catch (final FinishedException e) {
				// we are done
				return;
			} catch (final IOException e) {
				// won't be robust to IOExceptions
				throw e;
			} catch (final InterruptedException e) {
				// won't be robust to InterruptedExceptions
				throw e;
			} catch (final RuntimeException e) {
				if (this.robust)
					LOG.error(e.getClass().getName() + " caught", e);
				else
					throw e;
			} catch (final Exception e) {
				if (this.robust)
					LOG.error(e.getClass().getName() + " caught", e);
				else
					throw new RuntimeException(e.getClass().getName() + " caught", e);
			}

		// finish it!
		t.finish();
	}
}
