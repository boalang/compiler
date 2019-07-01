/*
 * Copyright 2019, Anthony Urso, Hridesh Rajan, Robert Dyer,
 *                 Bowling Green State University
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
package boa.aggregators;

import java.io.IOException;

import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer.Context;

import boa.functions.BoaCasts;
import boa.io.EmitKey;
import boa.io.EmitValue;
import boa.output.Output.Row;
import boa.output.Output.Value;

/**
 * The base class for all Boa aggregators.
 *
 * @author anthonyu
 * @author rdyer
 */
public abstract class Aggregator {
	private long arg;
	@SuppressWarnings("rawtypes")
	private Context context;
	private EmitKey key;
	private boolean combining;

	/**
	 * Construct an Aggregator.
	 *
	 */
	public Aggregator() {
		// default constructor
	}

	/**
	 * Construct an Aggregator.
	 *
	 * @param arg
	 *            A long (Boa int) containing the argument to the table
	 */
	public Aggregator(final long arg) {
		this();

		this.arg = arg;
	}

	/**
	 * Reset this aggregator for a new key.
	 *
	 * @param key
	 *            The {@link EmitKey} to aggregate for
	 */
	public void start(final EmitKey key) {
		this.setKey(key);
	}

	public void aggregate(final Value data, final Value metadata) throws IOException, InterruptedException, FinishedException {
		switch (data.getType()) {
			case INT:
				this.aggregate(data.getI(), metadata);
				break;
			case FLOAT:
				this.aggregate(data.getF(), metadata);
				break;
			case STRING:
				this.aggregate(data.getS(), metadata);
				break;
			case BOOL:
				this.aggregate(data.getB(), metadata);
				break;
			case TUPLE:
				// TODO FIXME
				//this.aggregate(data.getB(), metadata);
				break;
			default:
				break;
		}
	}

	public final void aggregate(final Value data) throws IOException, InterruptedException, FinishedException {
		this.aggregate(data, null);
	}

	public void aggregate(final String data, final Value metadata) throws IOException, InterruptedException, FinishedException {
		// intentionally blank
	}

	public void aggregate(final long data, final Value metadata) throws IOException, InterruptedException, FinishedException {
		// intentionally blank
	}

	public void aggregate(final double data, final Value metadata) throws IOException, InterruptedException, FinishedException {
		// intentionally blank
	}

	public void aggregate(final boolean data, final Value metadata) throws IOException, InterruptedException, FinishedException {
		// intentionally blank
	}

	@SuppressWarnings("unchecked")
	protected void collect(final Value data, final Value metadata) throws IOException, InterruptedException {
		if (this.combining)
			this.getContext().write(this.getKey(), new EmitValue(data, metadata));
		else
			this.getContext().write(NullWritable.get(), toRow(data, metadata));
	}

	protected void collect(final Value data) throws IOException, InterruptedException {
		this.collect(data, null);
	}

	@SuppressWarnings("unchecked")
	protected void collect(final String data, final Value metadata) throws IOException, InterruptedException {
		if (this.combining)
			this.getContext().write(this.getKey(), new EmitValue(EmitKey.toValue(data), metadata));
		else
			this.getContext().write(NullWritable.get(), toRow(EmitKey.toValue(data), metadata));
	}

	protected BytesWritable toRow(final Value data, final Value metadata) {
		final Row.Builder r = Row.newBuilder();
		for (final Value idx : this.getKey().getIndices())
			r.addCols(idx);
		if (metadata != null) {
			final Value.Builder v = Value.newBuilder();
			v.setType(Value.Type.TUPLE);
			v.addT(data);
			v.addT(metadata);
			v.setHasWeight(true);
			r.setVal(v.build());
		} else {
			r.setVal(data);
		}
		final byte[] arr = r.build().toByteArray();
		final BytesWritable b = new BytesWritable(arr);
		return b;
	}

	protected void collect(final String data) throws IOException, InterruptedException {
		this.collect(data, null);
	}

	@SuppressWarnings("unchecked")
	protected void collect(final long data, final Value metadata) throws IOException, InterruptedException {
		if (this.combining)
			this.getContext().write(this.getKey(), new EmitValue(EmitKey.toValue(data), metadata));
		else
			this.getContext().write(NullWritable.get(), toRow(EmitKey.toValue(data), metadata));
	}

	protected void collect(final long data) throws IOException, InterruptedException {
		this.collect(data, null);
	}

	@SuppressWarnings("unchecked")
	protected void collect(final double data, final Value metadata) throws IOException, InterruptedException {
		if (this.combining)
			this.getContext().write(this.getKey(), new EmitValue(EmitKey.toValue(data), metadata));
		else
			this.getContext().write(NullWritable.get(), toRow(EmitKey.toValue(data), metadata));
	}

	protected void collect(final double data) throws IOException, InterruptedException {
		this.collect(data, null);
	}

	public void finish() throws IOException, InterruptedException {
		// do nothing by default
	}

	public long getArg() {
		return this.arg;
	}

	public void setContext(@SuppressWarnings("rawtypes") final Context context) {
		this.context = context;
	}

	public boolean isCombining() {
		return this.combining;
	}

	public void setCombining(final boolean combining) {
		this.combining = combining;
	}

	@SuppressWarnings("rawtypes")
	public Context getContext() {
		return this.context;
	}

	public void setKey(final EmitKey key) {
		this.key = key;
	}

	public EmitKey getKey() {
		return this.key;
	}
}
