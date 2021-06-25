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
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.PathFilter;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.MapFile;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.SequenceFile.CompressionType;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.compress.BZip2Codec;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.DefaultCodec;
import org.apache.hadoop.io.compress.GzipCodec;
import org.apache.hadoop.io.compress.SnappyCodec;

import com.google.protobuf.CodedInputStream;

import boa.datagen.util.Properties;
import boa.types.Code.CodeRepository;
import boa.types.Code.Revision;
import boa.types.Diff.ChangedFile;
import boa.types.Toplevel.Project;

/**
 * @author hoan
 * @author hridesh
 */
public class SeqCombiner {
	public static void main(String[] args) throws IOException {
		CompressionType compressionType = CompressionType.BLOCK;
		CompressionCodec compressionCode = new DefaultCodec();
		Configuration conf = new Configuration();
		//conf.set("fs.default.name", "hdfs://boa-njt/");
		FileSystem fileSystem = FileSystem.get(conf);
		String base = Properties.getProperty("output.path", DefaultProperties.OUTPUT);

		if (args.length > 0) {
			base = args[0];
		}
		if (args.length > 1) {
			if (args[1].toLowerCase().equals("d"))
				compressionCode = new DefaultCodec();
			else if (args[1].toLowerCase().equals("g"))
				compressionCode = new GzipCodec();
			else if (args[1].toLowerCase().equals("b"))
				compressionCode = new BZip2Codec();
			else if (args[1].toLowerCase().equals("s"))
				compressionCode = new SnappyCodec();
		}

		SequenceFile.Writer projectWriter = SequenceFile.createWriter(fileSystem, conf, new Path(base + "/projects.seq"), Text.class, BytesWritable.class, compressionType, compressionCode);
		MapFile.Writer astWriter = new MapFile.Writer(conf, fileSystem, base + "/ast", LongWritable.class, BytesWritable.class, compressionType, compressionCode, null);
		MapFile.Writer commitWriter = new MapFile.Writer(conf, fileSystem, base + "/commit", LongWritable.class, BytesWritable.class, compressionType, compressionCode, null);

		FileStatus[] files = fileSystem.listStatus(new Path(base + "/project"), new PathFilter() {
			@Override
			public boolean accept(Path path) {
				String name = path.getName();
				return name.endsWith(".seq") && name.contains("-");
			}
		});
		long lastAstWriterKey = 0;
		long lastCommitWriterKey = 0;
		for (int i = 0; i < files.length; i++) {
			FileStatus file = files[i];
			String name = file.getPath().getName();
			System.out.println("Reading file " + (i+1) + " in " + files.length + ": " + name);
			SequenceFile.Reader r = new SequenceFile.Reader(fileSystem, file.getPath(), conf);
			Text textKey = new Text();
			BytesWritable value = new BytesWritable();
			try {
				while (r.next(textKey, value)) {
					Project p = Project.parseFrom(CodedInputStream.newInstance(value.getBytes(), 0, value.getLength()));
					Project.Builder pb = Project.newBuilder(p);
					for (CodeRepository.Builder crb : pb.getCodeRepositoriesBuilderList()) {
						if (crb.getRevisionsCount() > 0) {
							for (Revision.Builder rb : crb.getRevisionsBuilderList()) {
								for (ChangedFile.Builder cfb : rb.getFilesBuilderList()) {
									long key = cfb.getKey();
									if (key > 0)
										cfb.setKey(lastAstWriterKey + key);
								}
							}
						} else {
							for (int j = 0; j < crb.getRevisionKeysCount(); j++) {
								crb.setRevisionKeys(j, lastCommitWriterKey + crb.getRevisionKeys(j));
							}
						}
						for (ChangedFile.Builder cfb : crb.getHeadSnapshotBuilderList()) {
							long key = cfb.getKey();
							if (key > 0)
								cfb.setKey(lastAstWriterKey + key);
						}
					}
					projectWriter.append(textKey, new BytesWritable(pb.build().toByteArray()));
				}
			} catch (Exception e) {
				System.err.println(name);
				e.printStackTrace();
			} finally {
				r.close();
			}
			lastCommitWriterKey = readAndAppendCommit(conf, fileSystem, commitWriter, base + "/commit/" + name, lastAstWriterKey, lastCommitWriterKey);
			lastAstWriterKey = readAndAppendAst(conf, fileSystem, astWriter, base + "/ast/" + name, lastAstWriterKey);
		}
		projectWriter.close();
		astWriter.close();
		commitWriter.close();

		fileSystem.close();
	}

	public static long readAndAppendCommit(Configuration conf, FileSystem fileSystem, MapFile.Writer writer, String fileName, long lastAstKey, long lastCommitKey) throws IOException {
		long newLastKey = lastCommitKey;
		SequenceFile.Reader r = new SequenceFile.Reader(fileSystem, new Path(fileName), conf);
		LongWritable longKey = new LongWritable();
		BytesWritable value = new BytesWritable();
		try {
			while (r.next(longKey, value)) {
				newLastKey = longKey.get() + lastCommitKey;
				Revision rev = Revision.parseFrom(CodedInputStream.newInstance(value.getBytes(), 0, value.getLength()));
				Revision.Builder rb = Revision.newBuilder(rev);
				for (ChangedFile.Builder cfb : rb.getFilesBuilderList()) {
					long key = cfb.getKey();
					if (key > 0)
						cfb.setKey(lastAstKey + key);
				}
				writer.append(new LongWritable(newLastKey), new BytesWritable(rb.build().toByteArray()));
			}
		} catch (Exception e) {
			System.err.println(fileName);
			e.printStackTrace();
		} finally {
			r.close();
		}
		return newLastKey;
	}

	public static long readAndAppendAst(Configuration conf, FileSystem fileSystem, MapFile.Writer writer, String fileName, long lastKey) throws IOException {
		long newLastKey = lastKey;
		SequenceFile.Reader r = new SequenceFile.Reader(fileSystem, new Path(fileName), conf);
		LongWritable longKey = new LongWritable();
		BytesWritable value = new BytesWritable();
		try {
			while (r.next(longKey, value)) {
				newLastKey = longKey.get() + lastKey;
				writer.append(new LongWritable(newLastKey), value);
			}
		} catch (Exception e) {
			System.err.println(fileName);
			e.printStackTrace();
		} finally {
			r.close();
		}
		return newLastKey;
	}
}
