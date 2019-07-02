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
package boa.io;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;

import com.google.protobuf.CodedInputStream;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;

import boa.functions.BoaCasts;
import boa.output.Output.Value;
import boa.runtime.Tuple;

/**
 * A {@link Writable} that contains a datum and an optional metadatum to be
 * emitted to a Boa table.
 *
 * @author anthonyu
 * @author rdyer
 */
public class EmitValue implements Writable {
	private Value data;
	private Value metadata;

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
	 *            A {@link Value} containing the data to be emitted
	 * @param metadata
	 *            A {@link String} containing the metadata to be emitted
	 */
	public EmitValue(final Value data, final Value metadata) {
		this.data = data;
		this.metadata = metadata;
	}

	/**
	 * Construct an EmitValue.
	 *
	 * @param data
	 *            A {@link Value} containing the data to be emitted
	 */
	public EmitValue(final Value data) {
		this(data, null);
	}

	/** {@inheritDoc} */
	@Override
	public void readFields(final DataInput in) throws IOException {
		int len = in.readInt();
		byte[] b = new byte[len];
		for (int i = 0; i < len; i++)
			b[i] = in.readByte();
		this.data = Value.parseFrom(CodedInputStream.newInstance(b, 0, len));

		len = in.readInt();
		if (len == 0) {
			this.metadata = null;
		} else {
			b = new byte[len];
			for (int i = 0; i < len; i++)
				b[i] = in.readByte();
			this.metadata = Value.parseFrom(CodedInputStream.newInstance(b, 0, len));
		}
	}

	/** {@inheritDoc} */
	@Override
	public void write(final DataOutput out) throws IOException {
		byte[] b = this.data.toByteArray();
		out.writeInt(b.length);
		out.write(b);

		if (this.metadata == null) {
			out.writeInt(0);
		} else {
			b = this.metadata.toByteArray();
			out.writeInt(b.length);
			out.write(b);
		}
	}

	/**
	 * @return the data
	 */
	public Value getData() {
		return this.data;
	}

	/**
	 * @return the metadata
	 */
	public Value getMetadata() {
		return this.metadata;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + this.data.hashCode();
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
		if (!this.data.equals(other.data))
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
		if (this.metadata != null)
			return EmitKey.valueToString(this.data) + ", " + EmitKey.valueToString(this.metadata);
		return EmitKey.valueToString(this.data);
	}
}
