/*
 * Copyright 2018, Che Shian Hung, Robert Dyer
 *                 and BowlingGreen State University
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

import com.google.protobuf.CodedInputStream;


/**
 * SequenceFile Iterator
 *
 * @author hungc
 * @author rdyer
 */
public class SequenceFileIterator {
	private String path = null;
	private Configuration conf = null;
	private SequenceFile.Reader reader = null;
	private NullWritable key = null;
	private BytesWritable value = null;
	private boolean preloaded = false;
	private List<Object> indices = null;
	private CodedInputStream stream = null;
	private Row row = null;

	public SequenceFileIterator(String path, long position) {
		this.path = path;
		conf = new Configuration();

		reader = new SequenceFile.Reader(conf, Reader.file(new Path(path)), Reader.bufferSize(4096), Reader.start(0));
		reader.seek(position);
		key = (NullWritable) ReflectionUtils.newInstance(this.reader.getKeyClass(), conf);
		value = (BytesWritable) ReflectionUtils.newInstance(this.reader.getValueClass(), conf);

		indices = new ArrayList<Object>();
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

	public void close() {
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

	private boolean readNext() {
		boolean filter = true;
		while (filter) {
			filter = false;
			if (!reader.next(key, value)) {
				return false;
			}
			stream = CodedInputStream.newInstance(value.getBytes(), 0, value.getLength());
			row = Row.parseFrom(stream);
			List<Value> rowValues = row.getColsList();

			for (int i = 0; i < indices.size(); i++) {
				Value value = rowValues.get(i);
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

	private boolean compareField(Value v, Object index) {
		switch (v.getType()) {
			case Value.Type.INT:
				return v.getI() == (Integer) index;
			case Value.Type.FLOAT:
				return v.getF() == (Float) index;
			case Value.Type.BOOL:
				return v.getB() == (Boolean) index;
			case Value.Type.STR:
				return v.getS().equals(index);
			case Value.Type.TUPLE:
				for (int i = 0; i < getTCount(); i++) {
					if (!compareField(v.getTs(i), index.get(i))) // need to think how to get each field from tuple index
						return false;
				}
				return true;
			default:
				return false;
      	}
	}

	public SequenceFileIterator clone() {
		SequenceFileIterator newSFI = new SequenceFileIterator(path, reader.getPosition());
		newSFI.setIndices(((ArrayList) indices).clone());
		return newSFI;
	}
}