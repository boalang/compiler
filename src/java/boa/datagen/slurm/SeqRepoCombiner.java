package boa.datagen.slurm;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.MapFile;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.SequenceFile.CompressionType;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.DefaultCodec;

import com.google.protobuf.CodedInputStream;

import boa.types.Code.CodeRepository;
import boa.types.Code.Revision;
import boa.types.Diff.ChangedFile;
import boa.types.Toplevel.Project;

//Datagen Phase 3: combine sequence files
public class SeqRepoCombiner {

	private static String DATASET_PATH; // generated dataset in phase 2
	private static int PROJECT_NUM_IN_AST; // maximum number of projects in each ast map

	private static FileSystem fs;
	private static Configuration conf;
	private static SequenceFile.Writer projectWriter;
	private static MapFile.Writer astWriter;
	private static MapFile.Writer commitWriter;

	public static void main(String[] args) {

		if (args.length < 2) {
			System.err.println("Need args:\n" + "DATASET_PATH\n" + "PROJECT_NUM_IN_AST\n");
			return;
		}

		DATASET_PATH = args[0];
		PROJECT_NUM_IN_AST = Integer.parseInt(args[1]);

		try {
			conf = new Configuration();
			fs = FileSystem.get(conf);

			// remove combined seq files
			checkAndRemove(DATASET_PATH + "/combined");

			int astCount = 0, fileCount = 0, projectCount = 0;
			openWriters(astCount);

			long lastAstWriterKey = 0, lastCommitWriterKey = 0;
			HashSet<String> processedProjectNames = new HashSet<String>();
			// iterate each directory
			for (FileStatus file : fs.listStatus(new Path(DATASET_PATH + "/project"))) {
				if (!file.isDir())
					continue;
				// iterate each seq file
				for (FileStatus seqFile : fs.listStatus(file.getPath())) {
					if (!seqFile.getPath().getName().endsWith(".seq"))
						continue;
					fileCount++;
					String name = seqFile.getPath().getName();

					SequenceFile.Reader r = null;
					try {
						System.out.println("Reading file " + fileCount + " : " + name);
						r = new SequenceFile.Reader(fs, seqFile.getPath(), conf);
						Text textKey = new Text();
						BytesWritable value = new BytesWritable();

						String projectName = null;
						// each seq file should contain only one project
						while (r.next(textKey, value)) {
							Project p = Project
									.parseFrom(CodedInputStream.newInstance(value.getBytes(), 0, value.getLength()));
							if (processedProjectNames.contains(p.getName()))
								continue;
							projectName = p.getName();
							Project.Builder pb = Project.newBuilder(p);
							for (CodeRepository.Builder crb : pb.getCodeRepositoriesBuilderList()) {
								if (crb.getRevisionsCount() > 0) {
									for (Revision.Builder rb : crb.getRevisionsBuilderList()) {
										for (ChangedFile.Builder cfb : rb.getFilesBuilderList()) {
											long key = cfb.getKey();
											if (key > 0)
												cfb.setKey(lastAstWriterKey + key);
											cfb.setAstKey(astCount);
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
									cfb.setAstKey(astCount);
								}
							}
							projectWriter.append(textKey, new BytesWritable(pb.build().toByteArray()));
							processedProjectNames.add(projectName);

							// update its corresponding commit and ast seq
							lastCommitWriterKey = readAndAppendCommit(conf, fs, commitWriter,
									DATASET_PATH + "/commit/" + projectName + ".seq", lastAstWriterKey,
									lastCommitWriterKey, astCount);
							lastAstWriterKey = readAndAppendAst(conf, fs, astWriter,
									DATASET_PATH + "/ast/" + projectName + ".seq", lastAstWriterKey);

							System.out.println("Finish project " + projectName);

							projectCount++;
							astCount++;
							// open a new ast writer if current writer hits the maximum project number
							if (projectCount >= PROJECT_NUM_IN_AST) {
								astWriter.close();
								astWriter = new MapFile.Writer(conf, fs, DATASET_PATH + "/combined/ast" + astCount,
										LongWritable.class, BytesWritable.class, CompressionType.BLOCK,
										new DefaultCodec(), null);
								projectCount = 0;
							}
						}
					} catch (EOFException e) {
						e.printStackTrace();
						System.err.println("ingore project " + name);
						continue;
					} catch (Exception e) {
						e.printStackTrace();
						System.err.println("ingore project " + name);
						continue;
					} finally {
						if (r != null)
							r.close();
					}
				}
			}

			closeWriters();
			fs.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void openWriters(int astCount) {
		CompressionType compType = CompressionType.BLOCK;
		CompressionCodec compCode = new DefaultCodec();
		try {
			projectWriter = SequenceFile.createWriter(fs, conf, new Path(DATASET_PATH + "/combined/projects.seq"),
					Text.class, BytesWritable.class, compType, compCode);
			astWriter = new MapFile.Writer(conf, fs, DATASET_PATH + "/combined/ast" + astCount, LongWritable.class,
					BytesWritable.class, compType, compCode, null);
			commitWriter = new MapFile.Writer(conf, fs, DATASET_PATH + "/combined/commit", LongWritable.class,
					BytesWritable.class, compType, compCode, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void closeWriters() {
		try {
			projectWriter.close();
			astWriter.close();
			commitWriter.close();
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	private static void checkAndRemove(String path) {
		File file = new File(path);
		if (file.exists()) {
			System.out.println("remove file " + path);
			org.apache.commons.io.FileUtils.deleteQuietly(file);
		}
	}

	public static long readAndAppendCommit(Configuration conf, FileSystem fileSystem, MapFile.Writer writer,
			String fileName, long lastAstKey, long lastCommitKey, int astCount) throws IOException {
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
					cfb.setAstKey(astCount);
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

	public static long readAndAppendAst(Configuration conf, FileSystem fileSystem, MapFile.Writer writer,
			String fileName, long lastKey) throws IOException {
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
