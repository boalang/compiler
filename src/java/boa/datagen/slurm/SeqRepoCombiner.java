package boa.datagen.slurm;

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

	public static void main(String[] args) {

		if (args.length < 1) {
			System.err.println("Need args:\n" + "OUTPUT_PATH");
			return;
		}

		String base = args[0];
		CompressionType compType = CompressionType.BLOCK;
		CompressionCodec compCode = new DefaultCodec();
		Configuration conf = new Configuration();

		try {
			FileSystem fs = FileSystem.get(conf);

			SequenceFile.Writer projectWriter = SequenceFile.createWriter(fs, conf, new Path(base + "/projects.seq"),
					Text.class, BytesWritable.class, compType, compCode);
			MapFile.Writer astWriter = new MapFile.Writer(conf, fs, base + "/ast", LongWritable.class,
					BytesWritable.class, compType, compCode, null);
			MapFile.Writer commitWriter = new MapFile.Writer(conf, fs, base + "/commit", LongWritable.class,
					BytesWritable.class, compType, compCode, null);

			int count = 0;
			long lastAstWriterKey = 0, lastCommitWriterKey = 0;
			HashSet<String> processedProjectNames = new HashSet<String>();
			// iterate each directory
			for (FileStatus file : fs.listStatus(new Path(base + "/project"))) {
				if (!file.isDir())
					continue;
				// iterate each seq file
				for (FileStatus seqFile : fs.listStatus(file.getPath())) {
					if (!seqFile.getPath().getName().endsWith(".seq"))
						continue;
					count++;
					String name = seqFile.getPath().getName();
					System.out.println("Reading file " + count + " : " + name);
					SequenceFile.Reader r = new SequenceFile.Reader(fs, seqFile.getPath(), conf);
					Text textKey = new Text();
					BytesWritable value = new BytesWritable();
					
					try {
						String projectName = null;
						// each seq file should contain only one project
						while (r.next(textKey, value)) {
							Project p = Project.parseFrom(CodedInputStream.newInstance(value.getBytes(), 0, value.getLength()));
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
							processedProjectNames.add(projectName);

							// update its corresponding  commit and ast seq
							lastCommitWriterKey = readAndAppendCommit(conf, fs, commitWriter, base + "/commit/" + projectName + ".seq", lastAstWriterKey, lastCommitWriterKey);
							lastAstWriterKey = readAndAppendAst(conf, fs, astWriter, base + "/ast/" + projectName + ".seq", lastAstWriterKey);

							System.out.println("Finish project " + projectName);
						}
					} catch (Exception e) {
						System.err.println(name);
						e.printStackTrace();
						continue;
					} finally {
						r.close();
					}
				}
			}
			
			projectWriter.close();
			astWriter.close();
			commitWriter.close();
			fs.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
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
