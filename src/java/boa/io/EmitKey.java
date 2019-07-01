/*
 * Copyright 2019, Anthony Urso, Hridesh Rajan, Robert Dyer,
 *                 Iowa State University of Science and Technology
 *                 and Bowling Green State University
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
import java.io.Serializable;

import com.google.protobuf.CodedInputStream;
import org.apache.hadoop.io.RawComparator;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableComparator;
import org.apache.hadoop.io.WritableUtils;

import boa.output.Output.Value;
import boa.runtime.Tuple;

/**
 * A {@link WritableComparable} that contains a low resolution key which is the
 * name of the table this value is being emitted to, and a high resolution key
 * which is an index into that table.
 *
 * @author anthonyu
 * @author rdyer
 */
public class EmitKey implements WritableComparable<EmitKey>, RawComparator<EmitKey>, Serializable {
	private static final long serialVersionUID = -6302400030199718829L;

	private String name;
	private Value[] indices;

	/**
	 * Construct an EmitKey.
	 */
	public EmitKey() {
		// default constructor for Writable
	}

	/**
	 * Construct an EmitKey.
	 *
	 * @param name
	 *            A {@link String} containing the name of the table this was
	 *            emitted to
	 */
	public EmitKey(final String name) {
		this.name = name;
		this.indices = new Value[0];
	}

	/**
	 * Construct an EmitKey.
	 *
	 * @param name
	 *            A {@link String} containing the name of the table this was
	 *            emitted to
	 *
	 * @param indices
	 *            A {@link Value[]} containing the indices into the table this was
	 *            emitted to
	 */
	public EmitKey(final String name, final Value... indices) {
		this.name = name;
		this.indices = new Value[indices.length];
		for (int i = 0; i < indices.length; i++)
			this.indices[i] = indices[i];
	}

	/** {@inheritDoc} */
	@Override
	public void readFields(final DataInput in) throws IOException {
		this.name = Text.readString(in);
		final int count = in.readInt();
		this.indices = new Value[count];
		for (int i = 0; i < count; i++) {
			final int len = in.readInt();
			final byte[] b = new byte[len];
			for (int j = 0; j < len; j++)
				b[j] = in.readByte();
			this.indices[i] = Value.parseFrom(CodedInputStream.newInstance(b, 0, len));
		}
	}

	/** {@inheritDoc} */
	@Override
	public void write(final DataOutput out) throws IOException {
		Text.writeString(out, this.name);
		out.writeInt(this.indices.length);
		for (final Value idx : this.indices) {
			final byte[] b = idx.toByteArray();
			out.write(b.length);
			out.write(b);
		}
	}

	/** {@inheritDoc} */
	@Override
	public int compare(final byte[] b1, final int s1, final int l1, final byte[] b2, final int s2, final int l2) {
		return WritableComparator.compareBytes(b1, s1, l1, b2, s2, l2);
	}

	/** {@inheritDoc} */
	@Override
	public int compare(final EmitKey k1, final EmitKey k2) {
		return k1.compareTo(k2);
	}

	/** {@inheritDoc} */
	@Override
	public int compareTo(final EmitKey that) {
		// compare the names
		int c = this.name.compareTo(that.name);
		if (c != 0)
			return c;

		// compare the indices
		for (int i = 0; i < this.indices.length && i < that.indices.length; i++) {
			c = EmitKey.valueToString(this.indices[i]).compareTo(EmitKey.valueToString(that.indices[i]));
			if (c != 0)
				return c;
		}

		return this.indices.length - that.indices.length;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (this.name == null ? 0 : this.name.hashCode());
		for (int i = 0; i < this.indices.length; i++)
			result = prime * result + this.indices[i].hashCode();
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (this.getClass() != obj.getClass())
			return false;
		final EmitKey other = (EmitKey) obj;
		if (this.name == null) {
			if (other.name != null)
				return false;
		} else if (!this.name.equals(other.name))
			return false;
		if (this.indices == null) {
			if (other.indices != null)
				return false;
		}
		return java.util.Arrays.equals(this.indices, other.indices);
	}

	/**
	 * Get the indices into the table this key was emitted to.
	 *
	 * @return A {@link Value[]} containing the indices into the table this key was
	 *         emitted to
	 */
	public Value[] getIndices() {
		return this.indices;
	}

	/**
	 * Get the indices into the table this key was emitted to as a string.
	 *
	 * @return A {@link String} containing the indices into the table this key was
	 *         emitted to
	 */
	public String getIndex() {
		String s = "[";
		for (int i = 0; i < this.indices.length; i++)
			s += EmitKey.valueToString(this.indices[i]);
		return s + "]";
	}

	public static String valueToString(final Value v) {
		switch (v.getType()) {
			case INT:
				return String.valueOf(v.getI());
			case FLOAT:
				return String.valueOf(v.getF());
			case STRING:
				return v.getS();
			case BOOL:
				return String.valueOf(v.getB());
			case TUPLE:
				String s = "{ ";
				for (int i = 0; i < v.getTCount(); i++) {
					if (i > 0)
						s += ", ";
					s += EmitKey.valueToString(v.getT(i));
				}
				return s + " }";
			default:
				return "";
		}
	}

	/**
	 * Get the name of the table this key was emitted to.
	 *
	 * @return A {@link String} containing the name of the table this key was
	 *         emitted to
	 */
	public String getName() {
		return this.name;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return this.name + this.getIndex();
	}

	public static Value toValue(final String data) {
		if (data == null) return null;
		final Value.Builder v = Value.newBuilder();
		v.setType(Value.Type.STRING);
		v.setS(data);
		return v.build();
	}

	public static Value toValue(final long data) {
		final Value.Builder v = Value.newBuilder();
		v.setType(Value.Type.INT);
		v.setI(data);
		return v.build();
	}

	public static Value toValue(final double data) {
		final Value.Builder v = Value.newBuilder();
		v.setType(Value.Type.FLOAT);
		v.setF(data);
		return v.build();
	}

	public static Value toValue(final boolean data) {
		final Value.Builder v = Value.newBuilder();
		v.setType(Value.Type.BOOL);
		v.setB(data);
		return v.build();
	}

	public static Value toValue(final Tuple data) {
		return data.toValue();
	}
}
