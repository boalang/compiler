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
import org.apache.hadoop.fs.PathFilter;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;

import com.google.protobuf.CodedInputStream;

import boa.datagen.util.Properties;
import boa.types.Code.CodeRepository;
import boa.types.Code.Revision;
import boa.types.Code.RevisionOrBuilder;
import boa.types.Diff.ChangedFile;
import boa.types.Toplevel.Project;

/**
 * @author hoan
 * @author hridesh
 */
public class SeqCombiner {

	public static void main(String[] args) throws IOException {
		Configuration conf = new Configuration();
//		conf.set("fs.default.name", "hdfs://boa-njt/");
		FileSystem fileSystem = FileSystem.get(conf);
		String base = Properties.getProperty("output.path", DefaultProperties.OUTPUT);

		SequenceFile.Writer projectWriter = SequenceFile.createWriter(fileSystem, conf, new Path(base+ "/projects.seq"), Text.class, BytesWritable.class);
		SequenceFile.Writer astWriter = SequenceFile.createWriter(fileSystem, conf, new Path(base+ "/ast.seq"), LongWritable.class, BytesWritable.class);
		
		FileStatus[] files = fileSystem.listStatus(new Path(base), new PathFilter() {
			
			@Override
			public boolean accept(Path path) {
				String name = path.getName();
				return name.startsWith("projects-") && name.endsWith(".seq") && name.substring("projects-".length()).contains("-");
			}
		});
		for (int i = 0; i < files.length; i++) {
			FileStatus file = files[i];
			String name = file.getPath().getName();
			long len = astWriter.getLength();
			System.out.println("Reading file " + (i+1) + " in " + files.length + ": " + name);
			SequenceFile.Reader r = new SequenceFile.Reader(fileSystem, file.getPath(), conf);
			Text textKey = new Text();
			BytesWritable value = new BytesWritable();
			try {
				while (r.next(textKey, value)) {
					Project p = Project.parseFrom(CodedInputStream.newInstance(value.getBytes(), 0, value.getLength()));
					Project.Builder pb = Project.newBuilder(p);
					for (CodeRepository.Builder crb : pb.getCodeRepositoriesBuilderList()) {
						for (Revision.Builder rb : crb.getRevisionsBuilderList()) {
							for (ChangedFile.Builder cfb : rb.getFilesBuilderList()) {
								cfb.setKey(len + cfb.getKey());
								long mappedKey = cfb.getMappedKey();
								if (mappedKey != -1)
									cfb.setMappedKey(len + mappedKey);
							}
						}
					}
					projectWriter.append(new Text(textKey), new BytesWritable(pb.build().toByteArray()));
				}
			} catch (Exception e) {
				System.err.println(name);
				e.printStackTrace();
			} finally {
				r.close();
			}
			r = new SequenceFile.Reader(fileSystem, new Path(file.getPath().getParent(), "ast-" + name.substring("projects-".length())), conf);
			LongWritable longKey = new LongWritable();
			value = new BytesWritable();
			try {
				while (r.next(longKey, value)) {
					astWriter.append(new LongWritable(longKey.get()), new BytesWritable(value.getBytes()));
				}
			} catch (Exception e) {
				System.err.println(name);
				e.printStackTrace();
			} finally {
				r.close();
			} 
		}
		projectWriter.close();
		astWriter.close();
		
		fileSystem.close();
	}

}
