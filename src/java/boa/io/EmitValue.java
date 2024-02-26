/*
 * Copyright 2015, Anthony Urso, Hridesh Rajan, Robert Dyer,
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
package boa.io;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;

import org.apache.commons.lang.SerializationUtils;

import boa.functions.BoaCasts;

/**
 * A {@link Writable} that contains a datum and an optional metadatum to be
 * emitted to a Boa table.
 * 
 * @author anthonyu
 * @author rdyer
 */
public class EmitValue implements Writable {
	private String[] data;
	private String metadata;
	private HashMap<String, Long> hmdata;

	/**
	 * Construct an EmitValue.
	 */
	public EmitValue() {
		// default constructor for Writable
	}

	/**
	 * Construct an EmitValue.
	 * 
	 * @param data
	 *            An array of {@link String} containing the data to be emitted
	 * @param metadata
	 *            A {@link String} containing the metadata to be emitted
	 */
	public EmitValue(final String[] data, final String metadata) {
		this.data = data;
		this.metadata = metadata;
	}

	/**
	 * Construct an EmitValue.
	 * 
	 * @param data
	 *            An array of {@link String} containing the data to be emitted
	 */
	public EmitValue(final String[] data) {
		this(data, null);
	}

	public EmitValue(final HashMap<String, Long> data, final String metadata) {
		this.data = new String[0];
		this.hmdata = data;
		this.metadata = metadata;
	}

	public EmitValue(final HashMap<String, Long> data) {
		this(data, null);
	}

	/**
	 * Construct an EmitValue.
	 * 
	 * @param data
	 *            An array of {@link Object} containing the data to be emitted
	 * @param metadata
	 *            A {@link String} containing the metadata to be emitted
	 */
	public EmitValue(final Object[] data, final String metadata) {
		final String[] strings = new String[data.length];

		for (int i = 0; i < data.length; i++)
			strings[i] = data[i].toString();

		this.data = strings;
		this.metadata = metadata;
	}

	/**
	 * Construct an EmitValue.
	 * 
	 * @param data
	 *            An array of {@link String} containing the data to be emitted
	 */
	public EmitValue(final Object[] data) {
		this(data, null);
	}

	/**
	 * Construct an EmitValue.
	 * 
	 * @param data
	 *            A {@link String} containing the data to be emitted
	 */
	public EmitValue(final String data) {
		this(new String[] { data }, null);
	}

	/**
	 * Construct an EmitValue.
	 * 
	 * @param data
	 *            A {@link String} containing the data to be emitted
	 * @param metadata
	 *            A {@link String} containing the metadata to be emitted
	 */
	public EmitValue(final String data, final String metadata) {
		this(new String[] { data }, metadata);
	}

	/**
	 * Construct an EmitValue.
	 * 
	 * @param data
	 *            A {@link String} containing the data to be emitted
	 * @param metadata
	 *            A long representing the metadata to be emitted
	 */
	public EmitValue(final String data, final long metadata) {
		this(new String[] { data }, BoaCasts.longToString(metadata));
	}

	/**
	 * Construct an EmitValue.
	 * 
	 * @param data
	 *            A {@link String} containing the data to be emitted
	 * @param metadata
	 *            A double representing the metadata to be emitted
	 */
	public EmitValue(final String data, final double metadata) {
		this(new String[] { data }, BoaCasts.doubleToString(metadata));
	}

	/**
	 * Construct an EmitValue.
	 * 
	 * @param data
	 *            A long representing the data to be emitted
	 */
	public EmitValue(final long data) {
		this(new String[] { BoaCasts.longToString(data) }, null);
	}

	/**
	 * Construct an EmitValue.
	 * 
	 * @param data
	 *            A long representing the data to be emitted
	 * @param metadata
	 *            A {@link String} containing the metadata to be emitted
	 */
	public EmitValue(final long data, final String metadata) {
		this(new String[] { BoaCasts.longToString(data) }, metadata);
	}

	/**
	 * Construct an EmitValue.
	 * 
	 * @param data
	 *            A long representing the data to be emitted
	 * @param metadata
	 *            A long representing the metadata to be emitted
	 */
	public EmitValue(final long data, final long metadata) {
		this(new String[] { BoaCasts.longToString(data) }, BoaCasts.longToString(metadata));
	}

	/**
	 * Construct an EmitValue.
	 * 
	 * @param data
	 *            A long representing the data to be emitted
	 * @param metadata
	 *            A double representing the metadata to be emitted
	 */
	public EmitValue(final long data, final double metadata) {
		this(new String[] { BoaCasts.longToString(data) }, BoaCasts.doubleToString(metadata));
	}

	/**
	 * Construct an EmitValue.
	 * 
	 * @param data
	 *            A double representing the data to be emitted
	 */
	public EmitValue(final double data) {
		this(new String[] { BoaCasts.doubleToString(data) }, null);
	}

	/**
	 * Construct an EmitValue.
	 * 
	 * @param data
	 *            A double representing the data to be emitted
	 * @param metadata
	 *            A {@link String} containing the metadata to be emitted
	 */
	public EmitValue(final double data, final String metadata) {
		this(new String[] { BoaCasts.doubleToString(data) }, metadata);
	}

	/**
	 * Construct an EmitValue.
	 * 
	 * @param data
	 *            A double representing the data to be emitted
	 * @param metadata
	 *            A long representing the metadata to be emitted
	 */
	public EmitValue(final double data, final long metadata) {
		this(new String[] { BoaCasts.doubleToString(data) }, BoaCasts.longToString(metadata));
	}

	/**
	 * Construct an EmitValue.
	 * 
	 * @param data
	 *            A double representing the data to be emitted
	 * @param metadata
	 *            A double representing the metadata to be emitted
	 */
	public EmitValue(final double data, final double metadata) {
		this(new String[] { BoaCasts.doubleToString(data) }, BoaCasts.doubleToString(metadata));
	}

	/**
	 * Construct an EmitValue.
	 * 
	 * @param data
	 *            A boolean representing the data to be emitted
	 */
	public EmitValue(final boolean data) {
		this(new String[] { BoaCasts.booleanToString(data) }, null);
	}

	/**
	 * Construct an EmitValue.
	 * 
	 * @param data
	 *            A boolean representing the data to be emitted
	 * @param metadata
	 *            A {@link String} containing the metadata to be emitted
	 */
	public EmitValue(final boolean data, final String metadata) {
		this(new String[] { BoaCasts.booleanToString(data) }, metadata);
	}

	/**
	 * Construct an EmitValue.
	 * 
	 * @param data
	 *            A boolean representing the data to be emitted
	 * @param metadata
	 *            A long representing the metadata to be emitted
	 */
	public EmitValue(final boolean data, final long metadata) {
		this(new String[] { BoaCasts.booleanToString(data) }, BoaCasts.longToString(metadata));
	}

	/**
	 * Construct an EmitValue.
	 * 
	 * @param data
	 *            A boolean representing the data to be emitted
	 * @param metadata
	 *            A double representing the metadata to be emitted
	 */
	public EmitValue(final boolean data, final double metadata) {
		this(new String[] { BoaCasts.booleanToString(data) }, BoaCasts.doubleToString(metadata));
	}

	/** {@inheritDoc} */
	@Override
	public void readFields(final DataInput in) throws IOException {
		final int count = in.readInt();

		this.data = new String[count];
		for (int i = 0; i < count; i++)
			this.data[i] = Text.readString(in);

		final String metadata = Text.readString(in);
		if (metadata.equals(""))
			this.metadata = null;
		else
			this.metadata = metadata;

		this.hmdata = null;
		final int length = in.readInt();

		if (length > 0) {
			final byte[] temp = new byte[length];

			try {
				in.readFully(temp, 0, length);
				this.hmdata = (HashMap<String, Long>)SerializationUtils.deserialize(temp);
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	public void write(final DataOutput out) throws IOException {
		out.writeInt(this.data.length);

		for (final String d : this.data)
			Text.writeString(out, d);

		if (this.metadata == null)
			Text.writeString(out, "");
		else
			Text.writeString(out, this.metadata);

		if (this.hmdata == null) {
			out.writeInt(0);
		} else {
			final byte[] temp = SerializationUtils.serialize(this.hmdata);
			out.writeInt(temp.length);
			out.write(temp);
		}
	}

	/**
	 * @return the data
	 */
	public String[] getData() {
		return this.data;
	}

	/**
	 * @param data
	 *            the data to set
	 */
	public void setData(final String[] data) {
		this.data = data;
	}

	/**
	 * @return the metadata
	 */
	public String getMetadata() {
		return this.metadata;
	}

	/**
	 * @param metadata
	 *            the metadatum to set
	 */
	public void setMetadata(final String metadata) {
		this.metadata = metadata;
	}

	public HashMap<String,Long> getMapData() {
		return this.hmdata;
	}

	public void setMapData(final HashMap<String,Long> hmdata) {
		this.hmdata = hmdata;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(this.data);
		result = prime * result + (this.metadata == null ? 0 : this.metadata.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (this.getClass() != obj.getClass())
			return false;
		final EmitValue other = (EmitValue) obj;
		if (!Arrays.equals(this.data, other.data))
			return false;
		if (this.metadata == null) {
			if (other.metadata != null)
				return false;
		} else if (!this.metadata.equals(other.metadata))
			return false;
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return Arrays.toString(this.data) + ":" + this.metadata;
	}
}
