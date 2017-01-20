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

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.SequenceFile.Reader;

import boa.datagen.util.Properties;

import org.apache.hadoop.io.Text;

/**
 * @author hoan
 * @author hridesh
 */
public class SeqSortMerge {
//	private static String base = "hdfs://boa-njt/";
	private static String base = Properties.getProperty("gh.json.cache.path", DefaultProperties.GH_JSON_CACHE_PATH);
	private static Configuration conf = new Configuration();
	private static final int NUM_FILES = 15;
	
	public static void main(String[] args) throws IOException {
//		conf.set("fs.default.name", base);
		FileSystem fs = FileSystem.get(conf);
		
		String inPath = "/tmprepcache/sorted";
		while (true) {
			FileStatus[] files = fs.listStatus(new Path(base + inPath));
			if (files.length < 2) break;
			Path path = new Path(inPath + System.currentTimeMillis());
			fs.mkdirs(path);
			SequenceFile.Writer w = SequenceFile.createWriter(fs, conf, new Path(inPath + path.getName() + "/part-00000"), Text.class, BytesWritable.class);
			FileStatus[] candidates = getCandidates(files);
			System.out.println("Merging " + candidates.length + " from " + files.length);
			SequenceFile.Reader[] readers = new SequenceFile.Reader[candidates.length];
			for (int i = 0; i < candidates.length; i++)
				readers[i] = new SequenceFile.Reader(fs, new Path(inPath + candidates[i].getPath().getName() + "/part-00000"), conf);
			Text[] keys = new Text[candidates.length];
			BytesWritable[] values = new BytesWritable[candidates.length];
			read(readers, keys, values);
			while (true) {
				int index = min(keys);
				if (keys[index].toString().isEmpty())
					break;
				w.append(keys[index], values[index]);
				read(readers[index], keys[index], values[index]);
			}
			for (int i = 0; i < readers.length; i++)
				readers[i].close();
			w.close();
			for (int i = 0; i < readers.length; i++)
				fs.delete(new Path(inPath + candidates[i].getPath().getName()), true);
		}
	}

	private static int min(Text[] keys) {
		int index = 0;
		Text min = keys[0];
		for (int i = 1; i < keys.length; i++) {
			Text key = keys[i];
			if (!key.toString().isEmpty() && key.compareTo(min) < 0) {
				index = i;
				min = key;
			}
		}
		return index;
	}

	private static void read(Reader[] readers, Text[] keys, BytesWritable[] values) {
		for (int i = 0; i < readers.length; i++)
			read(readers[i], keys[i], values[i]);
	}

	private static void read(Reader reader, Text key, BytesWritable val) {
		try {
			if (reader.next(key, val))
				return;
		} catch (Throwable t) {
			t.printStackTrace();
		}
		key.set("");
	}

	private static FileStatus[] getCandidates(FileStatus[] files) throws IOException {
		if (files.length <= NUM_FILES) return files;
		Arrays.sort(files, new Comparator<FileStatus>() {
			@Override
			public int compare(FileStatus f1, FileStatus f2) {
				long d = 0;
				try {
					d = getLen(f1) - getLen(f2);
				} catch (IOException e) {
					e.printStackTrace();
				}
				if (d < 0) return -1;
				if (d > 0) return 1;
				return 0;
			}
		});
		FileStatus[] candidates = new FileStatus[NUM_FILES];
		for (int i = 0; i < NUM_FILES; i++)
			candidates[i] = files[i];
		return candidates;
	}

	private static long getLen(FileStatus file) throws IOException {
		Path path = new Path(file.getPath(), "part-00000");
		long len = path.getFileSystem(conf).getFileStatus(path).getLen();
		return len;
	}

}
