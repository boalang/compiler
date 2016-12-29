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
import java.util.HashMap;
import java.util.HashSet;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;

import com.google.protobuf.CodedInputStream;

import boa.datagen.util.Properties;
import boa.types.Toplevel.Project;

/**
 * @author hoan
 * @author hridesh
 */
public class SeqProjectCombiner {

	public static void main(String[] args) throws IOException {
		Configuration conf = new Configuration();
//		conf.set("fs.default.name", "hdfs://boa-njt/");
		FileSystem fileSystem = FileSystem.get(conf);
//		String base = conf.get("fs.default.name", "");
		String base = Properties.getProperty("gh.json.cache.path", DefaultProperties.GH_JSON_CACHE_PATH);
		
		
		HashMap<String, String> sources = new HashMap<String, String>();
		HashSet<String> marks = new HashSet<String>();
		FileStatus[] files = fileSystem.listStatus(new Path(base + "tmprepcache"));
		for (int i = 0; i < files.length; i++) {
			FileStatus file = files[i];
			String name = file.getPath().getName();
			if (name.startsWith("projects-") && name.endsWith(".seq")) {
				System.out.println("Reading file " + i + " in " + files.length + ": " + name);
				SequenceFile.Reader r = new SequenceFile.Reader(fileSystem, file.getPath(), conf);
				final Text key = new Text();
				final BytesWritable value = new BytesWritable();
				try {
					while (r.next(key, value)) {
						String s = key.toString();
						if (marks.contains(s)) continue;
						Project p = Project.parseFrom(CodedInputStream.newInstance(value.getBytes(), 0, value.getLength()));
						if (p.getCodeRepositoriesCount() > 0 && p.getCodeRepositories(0).getRevisionsCount() > 0)
							marks.add(s);
						sources.put(s, name);
					}
				} catch (Exception e) {
					System.err.println(name);
					e.printStackTrace();
				}
				r.close();
			}
		}
		SequenceFile.Writer w = SequenceFile.createWriter(fileSystem, conf, new Path(base+ "/projects.seq"), Text.class, BytesWritable.class);
		for (int i = 0; i < files.length; i++) {
			FileStatus file = files[i];
			String name = file.getPath().getName();
			if (name.startsWith("projects-") && name.endsWith(".seq")) {
				System.out.println("Reading file " + i + " in " + files.length + ": " + name);
				SequenceFile.Reader r = new SequenceFile.Reader(fileSystem, file.getPath(), conf);
				final Text key = new Text();
				final BytesWritable value = new BytesWritable();
				try {
					while (r.next(key, value)) {
						String s = key.toString();
						if (sources.get(s).equals(name))
							w.append(key, value);
					}
				} catch (Exception e) {
					System.err.println(name);
					e.printStackTrace();
				}
				r.close();
			}
		}
		w.close();
		
		fileSystem.close();
	}

}
