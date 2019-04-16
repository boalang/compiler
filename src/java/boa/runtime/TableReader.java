/*
 * Copyright 2018, Che Shian Hung, Robert Dyer
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
package boa.runtime;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.SequenceFile.Reader;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.util.ReflectionUtils;
import org.apache.hadoop.util.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;

import com.google.protobuf.CodedInputStream;

import boa.output.Output.Value;
import boa.output.Output.Row;
import boa.runtime.Tuple;


/**
 * SequenceFile Iterator
 *
 * @author hungc
 * @author rdyer
 */
public class TableReader {
	private String[] path = null;
	private SequenceFile.Reader reader = null;
	private NullWritable key = null;
	private BytesWritable value = null;
	private boolean preloaded = false;
	private List<Object> indices = new ArrayList<Object>();
	private int columnIndex = -1;
	private Row row = null;

	public TableReader(long position, String ... path) {
		this.path = path.clone();
		String filePath = path.length > 0 ? path[0] : "";

		for (int i = 1; i < path.length; i++) {
			filePath += "/" + path[i];
		}

		final Configuration conf = new Configuration();
		try {
			reader = new SequenceFile.Reader(FileSystem.get(conf), new Path(filePath), conf);
			reader.seek(position);
		} catch (final Exception e) {
			reader = null;
		}

		key = (NullWritable) ReflectionUtils.newInstance(this.reader.getKeyClass(), conf);
		value = (BytesWritable) ReflectionUtils.newInstance(this.reader.getValueClass(), conf);
	}

	public boolean hasNext() {
		if (preloaded)
			return true;
		preloaded = readNext();
		return preloaded;
	}

	public Row next() {
		if (!preloaded)
			readNext();
		else
			preloaded = false;

		return row;
	}

	public boolean fetch(final Tuple t) {
		t.def = hasNext();
		if (!t.def)
			return false;
		if (columnIndex != -1)
			t.columnFromRow(next(), columnIndex);
		else
			t.fromRow(next(), getIndicesCount());
		return true;
	}

	public void close() {
		if (reader != null)
			try {
				reader.close();
			} catch (final Exception e) {
				// do nothing
			}
		reader = null;
	}

	public void addIndex(final Object obj) {
		indices.add(obj);
	}

	public void addIndices(final List<Object> objs) {
		for (final Object obj : objs)
			indices.add(obj);
	}

	public void setIndices(final List<Object> objs) {
		indices = objs;
	}

	public void setColumnIndex(final int index) {
		this.columnIndex = index;
	}

	public int getIndicesCount() {
		return indices.size();
	}

	private boolean readNext() {
		if (reader == null)
			return false;

		boolean filter = true;
		while (filter) {
			try {
				filter = false;
				if (!reader.next(key, value)) {
					return false;
				}
				final CodedInputStream stream = CodedInputStream.newInstance(value.getBytes(), 0, value.getLength());
				row = Row.parseFrom(stream);
				final List<Value> rowValues = row.getColsList();

				if (this.columnIndex == -1) {
					for (int i = 0; i < indices.size(); i++) {
						final Value value = rowValues.get(i);
						final Object index = indices.get(i);
						if ((index instanceof String && !((String)index).equals("_")) && !compareField(value, index)) {
							filter = true;
							break;
						}
					}
				}
				rowValues.add(row.getVal());
			} catch (final IOException e) {
				return false;
			}
		}

		return true;
	}

	private boolean compareField(final Value v, final Object index) {
		switch (v.getType()) {
			case INT:
				return v.getI() == (Integer) index;
			case FLOAT:
				return v.getF() == (Float) index;
			case BOOL:
				return v.getB() == (Boolean) index;
			case STRING:
				return v.getS().equals(index);
			case TUPLE:
				for (int i = 0; i < v.getTCount(); i++) {
					if (!compareField(v.getT(i), ((ArrayList)index).get(i)))
						return false;
				}
				return true;
			default:
				return false;
	  	}
	}

	public int length() {
		int count = 0;
		TableReader tr = new TableReader(0, path);
		while(tr.hasNext()) {
			tr.next();
			count++;
		}
		return count;
	}

	public void reset() {
		try {
			reader.seek(0);
		} catch (final Exception e) {
			reader = null;
		}
	}

	public static Long valToLong(final Value val) {
		return val.getI();
	}

	public static Double valToDouble(final Value val) {
		return val.getF();
	}

	public static String valToString(final Value val) {
		return val.getS();
	}

	public static Boolean valToBoolean(final Value val) {
		return val.getB();
	}

	public TableReader clone(){
		long pos = 0L;
		if (reader != null)
			try {
				pos = reader.getPosition();
			} catch (final Exception e) {
				// do nothing
			}
		final TableReader newSFI = new TableReader(pos, path);
		newSFI.preloaded = this.preloaded;
		newSFI.row = this.row;
		newSFI.columnIndex = this.columnIndex;
		for (final Object o : indices)
			newSFI.indices.add(o);
		return newSFI;
	}
}
