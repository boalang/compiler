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

import boa.output.Output.*;

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

import boa.runtime.Tuple;


/**
 * SequenceFile Iterator
 *
 * @author hungc
 * @author rdyer
 */
public class TableReader {
	private String[] path = null;
	private Configuration conf = null;
	private SequenceFile.Reader reader = null;
	private NullWritable key = null;
	private BytesWritable value = null;
	private boolean preloaded = false;
	private List<Object> indices = null;
	private CodedInputStream stream = null;
	private boa.output.Output.Row row = null;

	public TableReader(long position, String ... path) throws Exception {
		this.path = path.clone();
		String filePath = path.length > 0 ? path[0] : "";

		for (int i = 1; i < path.length; i++) {
			filePath += "/" + path[i];
		}

		conf = new Configuration();
		FileSystem fs = FileSystem.get(conf);
		Path p = new Path(filePath);

		reader = new SequenceFile.Reader(fs, new Path(filePath), conf);
		reader.seek(position);
		key = (NullWritable) ReflectionUtils.newInstance(this.reader.getKeyClass(), conf);
		value = (BytesWritable) ReflectionUtils.newInstance(this.reader.getValueClass(), conf);

		indices = new ArrayList<Object>();
	}

	public boolean hasNext() throws Exception{
		if (preloaded)
			return true;
		preloaded = readNext();
		return preloaded;
	}

	public boa.output.Output.Row next() throws Exception{
		if (!preloaded)
			readNext();
		else
			preloaded = false;

		return row;
	}

	public boolean fetch(final Tuple t) throws Exception {
		t.def = hasNext();
		if (!t.def)
			return false;
		t.fromRow(next(), getIndicesCount());
		return true;
	}

	public void close() throws Exception{
		reader.close();
	}

	public void addIndex(Object obj) {
		indices.add(obj);
	}

	public void addIndices(List<Object> objs) {
		for (Object obj : objs)
			indices.add(obj);
	}

	public void setIndices(List<Object> objs) {
		indices = objs;
	}

	public int getIndicesCount() {
		return indices.size();
	}

	private boolean readNext() throws Exception{
		boolean filter = true;
		while (filter) {
			filter = false;
			if (!reader.next(key, value)) {
				return false;
			}
			stream = CodedInputStream.newInstance(value.getBytes(), 0, value.getLength());
			row = boa.output.Output.Row.parseFrom(stream);
			List<boa.output.Output.Value> rowValues = row.getColsList();

			for (int i = 0; i < indices.size(); i++) {
				boa.output.Output.Value value = rowValues.get(i);
				Object index = indices.get(i);
				if ((index instanceof String && !((String)index).equals("_")) && !compareField(value, index)) {
					filter = true;
					break;
				}
			}
			rowValues.add(row.getVal());
		}

		return true;
	}

	private boolean compareField(boa.output.Output.Value v, Object index) {
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

	public static Long valToLong(Value val) {
		return val.getI();
	}

	public static Double valToDouble(Value val) {
		return val.getF();
	}

	public static String valToString(Value val) {
		return val.getS();
	}

	public static Boolean valToBoolean(Value val) {
		return val.getB();
	}

	public TableReader clone(){
		TableReader newSFI = null;

		try {
			newSFI = new TableReader(reader.getPosition(), path);
			List<Object> newIndices = new ArrayList<Object>();
			for (Object o : indices)
				newIndices.add(o);
			newSFI.setIndices(newIndices);
		} catch (Exception e) {
			return null;
		}

		return newSFI;
	}
}
