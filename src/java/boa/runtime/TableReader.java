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
import boa.runtime.EmptyTuple;

/**
 * SequenceFile Iterator
 *
 * @author hungc
 * @author rdyer
 */
public class TableReader {
	public static int jobId = 0;
	private static final Configuration conf = new Configuration();

	private Path filePath = null;
	private SequenceFile.Reader reader = null;
	private List<Object> indices = new ArrayList<Object>();
	private boolean preloaded = false;
	private Row row = null;
	private long position = 0;

	private NullWritable key = NullWritable.get();
	private BytesWritable value = new BytesWritable();

	public TableReader(final long position, final String ... path) {
		String filePath = "";

		for (int i = 0; i < path.length - 1; i++) {
			if (i > 0)
				filePath += "/";
			filePath += path[i];
		}
		this.filePath = new Path("/boa/" + jobId + "/" + filePath + "/output/" + path[path.length - 1] + ".seq");
		open();
		seek(position);
	}

	private TableReader(final Path filePath, final boolean preloaded, final long position, final Row row, final List<Object> indices) {
		this.filePath = filePath;
		this.preloaded = preloaded;
		this.row = row;
		this.indices = new ArrayList<Object>(indices);
		open();
		seek(position);
	}

	private void open() {
		try {
			reader = new SequenceFile.Reader(FileSystem.get(conf), this.filePath, conf);
			this.position = 0;
		} catch (final Exception e) {
			System.err.println(e);
			e.printStackTrace();
			reader = null;
		}
	}

	private void seek(final long position) {
		try {
			for (; this.position < position; this.position++)
				if (!reader.next(key, value)) {
					close();
					return;
				}
		} catch (final Exception e) {
			close();
		}
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

	public TableReader addIndex(final Object obj) {
		indices.add(obj);
		return this;
	}

	public void addIndices(final List<Object> objs) {
		for (final Object obj : objs)
			indices.add(obj);
	}

	public void setIndices(final List<Object> objs) {
		indices = objs;
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
					close();
					return false;
				}
				this.position++;
				row = Row.parseFrom(CodedInputStream.newInstance(value.getBytes(), 0, value.getLength()));

				final List<Value> rowValues = row.getColsList();

				for (int i = 0; i < indices.size() && i < rowValues.size(); i++) {
					final Object target = indices.get(i);
					if (target instanceof String && ((String)target).equals("_"))
						continue;
					if (!compareField(rowValues.get(i), target)) {
						filter = true;
						break;
					}
				}

				if (!filter && indices.size() > rowValues.size()) {
					final Object target = indices.get(indices.size() - 1);
					if (!(target instanceof String && ((String)target).equals("_")))
						if (!compareField(row.getVal(), target))
							filter = true;
				}
			} catch (final IOException e) {
				close();
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
		final TableReader tr = clone();
		while (tr.hasNext()) {
			tr.next();
			count++;
		}
		return count;
	}

	public void reset() {
		close();
		preloaded = false;
		row = null;
		open();
	}

	public EmptyTuple[] filterToArray() {
		final EmptyTuple t = new EmptyTuple();

		EmptyTuple[] et = new EmptyTuple[this.length()];
		for (int i = 0; i < et.length; i++)
			et[i] = t;

		return et;
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
		return new TableReader(this.filePath, this.preloaded, this.position, this.row, this.indices);
	}
}
