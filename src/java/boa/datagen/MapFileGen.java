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

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.MapFile;
import org.apache.hadoop.io.Text;

import boa.datagen.util.Properties;

/**
 * @author hoan
 * @author hridesh
 */
public class MapFileGen {
//	private final static String SEQ_FILE_PATH = Properties.getProperty("seq.file.path", "");
	private final static String SEQ_FILE_PATH = Properties.getProperty("gh.json.cache.path", DefaultProperties.GH_JSON_CACHE_PATH);
	public static void main(String[] args) throws Exception {
		System.out.println("generating data and index file");
		if (SEQ_FILE_PATH.isEmpty()) {
			System.out.println("Missing path to sequence file. Please specify it in the properties file.");
			return;
		}
		String base = "hdfs://boa-njt/";
		Configuration conf = new Configuration();
//		conf.set("fs.default.name", base);
		FileSystem fs = FileSystem.get(conf);
		Path path = new Path(SEQ_FILE_PATH);
		String name = path.getName();
		if (fs.isFile(path)) {
			if (path.getName().equals(MapFile.DATA_FILE_NAME)) {
				MapFile.fix(fs, path.getParent(), Text.class, BytesWritable.class, false, conf);
			}
			else {
				Path dataFile = new Path(path.getParent(), MapFile.DATA_FILE_NAME);
				fs.rename(path, dataFile);
				Path dir = new Path(path.getParent(), name);
				fs.mkdirs(dir);
				fs.rename(dataFile, new Path(dir, dataFile.getName()));
				MapFile.fix(fs, dir, Text.class, BytesWritable.class, false, conf);
			}
		}
		else {
			FileStatus[] files = fs.listStatus(path);
			for (FileStatus file : files) {
				path = file.getPath();
				if (fs.isFile(path)) {
					Path dataFile = new Path(path.getParent(), MapFile.DATA_FILE_NAME);
					fs.rename(path, dataFile);
					System.out.println("finxing data file");
					MapFile.fix(fs, dataFile.getParent(), Text.class, BytesWritable.class, false, conf);
					break;
				}
			}
		}
		fs.close();
	}

}
