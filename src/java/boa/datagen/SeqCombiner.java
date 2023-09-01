/*
 * Copyright 2015-2022, Hridesh Rajan, Robert Dyer, Hoan Nguyen
 *                 Iowa State University of Science and Technology
 *                 and University of Nebraska Board of Regents
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
 * @author rdyer
 */
public class SeqCombiner {
	public static void main(final String[] args) throws IOException {
		final CompressionType compressionType = CompressionType.BLOCK;
		CompressionCodec compressionCode = new DefaultCodec();
		final Configuration conf = new Configuration();
		final FileSystem fileSystem = FileSystem.get(conf);
		String inputbase = Properties.getProperty("output.path", DefaultProperties.OUTPUT);
		String outputbase = Properties.getProperty("combiner.output.path", DefaultProperties.COMBINER_OUTPUT);

		if (args.length > 0) {
			inputbase = args[0];
		}
		if (args.length > 1) {
			outputbase = args[1];
		}
		if (args.length > 2) {
			if (args[2].toLowerCase().equals("d"))
				compressionCode = new DefaultCodec();
			else if (args[2].toLowerCase().equals("g"))
				compressionCode = new GzipCodec();
			else if (args[2].toLowerCase().equals("b"))
				compressionCode = new BZip2Codec();
			else if (args[2].toLowerCase().equals("s"))
				compressionCode = new SnappyCodec();
		}

		final SequenceFile.Writer projectWriter = SequenceFile.createWriter(fileSystem, conf, new Path(outputbase + "/projects.seq"), Text.class, BytesWritable.class, compressionType, compressionCode);
		final MapFile.Writer astWriter = new MapFile.Writer(conf, fileSystem, outputbase + "/ast", LongWritable.class, BytesWritable.class, compressionType, compressionCode, null);
		final MapFile.Writer commitWriter = new MapFile.Writer(conf, fileSystem, outputbase + "/commit", LongWritable.class, BytesWritable.class, compressionType, compressionCode, null);

		final FileStatus[] files = fileSystem.listStatus(new Path(inputbase + "/project"), new PathFilter() {
			@Override
			public boolean accept(Path path) {
				String name = path.getName();
				return name.endsWith(".seq") && name.contains("-");
			}
		});
		long lastAstWriterKey = 0;
		long lastCommitWriterKey = 0;
		long projCount = 0;
		for (int i = 0; i < files.length; i++) {
			final FileStatus file = files[i];
			final String name = file.getPath().getName();
			try {
				System.out.println("Reading file " + (i+1) + " in " + files.length + ": " + name);
				final SequenceFile.Reader r = new SequenceFile.Reader(fileSystem, file.getPath(), conf);
				final Text textKey = new Text();
				final BytesWritable value = new BytesWritable();
				try {
					while (r.next(textKey, value)) {
						final Project p = Project.parseFrom(CodedInputStream.newInstance(value.getBytes(), 0, value.getLength()));
						final Project.Builder pb = Project.newBuilder(p);
						for (final CodeRepository.Builder crb : pb.getCodeRepositoriesBuilderList()) {
							if (crb.getRevisionsCount() > 0) {
								for (final Revision.Builder rb : crb.getRevisionsBuilderList()) {
									for (final ChangedFile.Builder cfb : rb.getFilesBuilderList()) {
										final long key = cfb.getKey();
										if (key > 0)
											cfb.setKey(lastAstWriterKey + key);
									}
								}
							} else {
								for (int j = 0; j < crb.getRevisionKeysCount(); j++) {
									crb.setRevisionKeys(j, lastCommitWriterKey + crb.getRevisionKeys(j));
								}
							}
							for (final ChangedFile.Builder cfb : crb.getHeadSnapshotBuilderList()) {
								final long key = cfb.getKey();
								if (key > 0)
									cfb.setKey(lastAstWriterKey + key);
							}
						}
						projectWriter.append(textKey, new BytesWritable(pb.build().toByteArray()));
						projCount++;
					}
				} catch (final Exception e) {
					System.err.println(name);
					e.printStackTrace();
				} finally {
					try { r.close(); } catch (final Exception e) {}
				}
				lastCommitWriterKey = readAndAppendCommit(conf, fileSystem, commitWriter, inputbase + "/commit/" + name, lastAstWriterKey, lastCommitWriterKey);
				lastAstWriterKey = readAndAppendAst(conf, fileSystem, astWriter, inputbase + "/ast/" + name, lastAstWriterKey);
			} catch (final Exception e) {
				System.err.println(name);
				e.printStackTrace();
			}
		}
		try { projectWriter.close(); } catch (final Exception e) {}
		try { astWriter.close(); } catch (final Exception e) {}
		try { commitWriter.close(); } catch (final Exception e) {}

		try { fileSystem.close(); } catch (final Exception e) {}
		System.out.println("combined " + projCount + " projects!");
	}

	public static long readAndAppendCommit(final Configuration conf, final FileSystem fileSystem, final MapFile.Writer writer, final String fileName, final long lastAstKey, final long lastCommitKey) throws IOException {
		long newLastKey = lastCommitKey;
		try {
			final SequenceFile.Reader r = new SequenceFile.Reader(fileSystem, new Path(fileName), conf);
			final LongWritable longKey = new LongWritable();
			final BytesWritable value = new BytesWritable();
			try {
				while (r.next(longKey, value)) {
					newLastKey = longKey.get() + lastCommitKey;
					final Revision rev = Revision.parseFrom(CodedInputStream.newInstance(value.getBytes(), 0, value.getLength()));
					final Revision.Builder rb = Revision.newBuilder(rev);
					for (final ChangedFile.Builder cfb : rb.getFilesBuilderList()) {
						final long key = cfb.getKey();
						if (key > 0)
							cfb.setKey(lastAstKey + key);
					}
					writer.append(new LongWritable(newLastKey), new BytesWritable(rb.build().toByteArray()));
				}
			} catch (final Exception e) {
				System.err.println(fileName);
				e.printStackTrace();
			} finally {
				try { r.close(); } catch (final Exception e) {}
			}
		} catch (final Exception e) {
			System.err.println(fileName);
			e.printStackTrace();
		}
		return newLastKey;
	}

	public static long readAndAppendAst(final Configuration conf, final FileSystem fileSystem, final MapFile.Writer writer, final String fileName, final long lastKey) throws IOException {
		long newLastKey = lastKey;
		try {
			final SequenceFile.Reader r = new SequenceFile.Reader(fileSystem, new Path(fileName), conf);
			final LongWritable longKey = new LongWritable();
			final BytesWritable value = new BytesWritable();
			try {
				while (r.next(longKey, value)) {
					newLastKey = longKey.get() + lastKey;
					writer.append(new LongWritable(newLastKey), value);
				}
			} catch (final Exception e) {
				System.err.println(fileName);
				e.printStackTrace();
			} finally {
				try { r.close(); } catch (final Exception e) {}
			}
		} catch (final Exception e) {
			System.err.println(fileName);
			e.printStackTrace();
		}
		return newLastKey;
	}
}
