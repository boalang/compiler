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
package boa.aggregators;

import java.io.IOException;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer.Context;

import boa.functions.BoaCasts;
import boa.io.EmitKey;
import boa.io.EmitValue;

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
	 * 
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
	 * 
	 */
	public void start(final EmitKey key) {
		this.setKey(key);
	}

	public abstract void aggregate(String data, String metadata) throws IOException, InterruptedException, FinishedException;

	public void aggregate(final String data) throws IOException, InterruptedException, FinishedException {
		this.aggregate(data, null);
	}

	public void aggregate(final long data, final String metadata) throws IOException, InterruptedException, FinishedException {
		this.aggregate(BoaCasts.longToString(data), metadata);
	}

	public void aggregate(final long data) throws IOException, InterruptedException, FinishedException {
		this.aggregate(BoaCasts.longToString(data), null);
	}

	public void aggregate(final double data, final String metadata) throws IOException, InterruptedException, FinishedException {
		this.aggregate(BoaCasts.doubleToString(data), metadata);
	}

	public void aggregate(final double data) throws IOException, InterruptedException, FinishedException {
		this.aggregate(BoaCasts.doubleToString(data), null);
	}

	@SuppressWarnings("unchecked")
	protected void collect(final String data, final String metadata) throws IOException, InterruptedException {
		if (this.combining)
			this.getContext().write(this.getKey(), new EmitValue(data, metadata));
		else if (metadata != null)
			this.getContext().write(new Text(this.getKey() + " = " + data + " weight " + metadata), NullWritable.get());
		else
			this.getContext().write(new Text(this.getKey() + " = " + data), NullWritable.get());
	}

	protected void collect(final String data) throws IOException, InterruptedException {
		this.collect(data, null);
	}

	@SuppressWarnings("unchecked")
	protected void collect(final long data, final String metadata) throws IOException, InterruptedException {
		this.collect(BoaCasts.longToString(data), metadata);
	}

	protected void collect(final long data) throws IOException, InterruptedException {
		this.collect(BoaCasts.longToString(data), null);
	}

	@SuppressWarnings("unchecked")
	protected void collect(final double data, final String metadata) throws IOException, InterruptedException {
		this.collect(BoaCasts.doubleToString(data), metadata);
	}

	protected void collect(final double data) throws IOException, InterruptedException {
		this.collect(BoaCasts.doubleToString(data), null);
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
