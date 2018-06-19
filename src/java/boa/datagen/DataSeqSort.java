/*
 * Copyright 2015, Hridesh Rajan, Robert Dyer, Hoan Nguyen
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

package boa.datagen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;

import com.google.protobuf.CodedInputStream;

import boa.types.Ast.ASTRoot;

/**
 * @author hoan
 * 
 */
public class DataSeqSort {

	public static void main(String[] args) throws Exception {
		Configuration conf = new Configuration();
		FileSystem fs = FileSystem.get(conf);
		
		String inPath = args[0];
		Map<Text, BytesWritable> map = new HashMap<Text, BytesWritable>();
		SequenceFile.Reader reader = new SequenceFile.Reader(fs, new Path(inPath + "/data"), conf);
		Text key = new Text();
		BytesWritable val = new BytesWritable();
		while (reader.next(key, val)) {
			map.put(key, new BytesWritable(val.getBytes()));
		}
		reader.close();
		
		List<Text> list = new ArrayList<Text>(map.keySet());
		Collections.sort(list, new Comparator<Text>() {
			@Override
			public int compare(Text t1, Text t2) {
				return t1.compareTo(t2);
			}
		});
		
		SequenceFile.Writer w = SequenceFile.createWriter(fs, conf, new Path(inPath + "/data.sorted"), Text.class, BytesWritable.class);
		for (Text k : list) {
			w.append(k, map.get(k));
		}
		w.close();
	}
}
