package boa.datagen;

import java.io.File;
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
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.SequenceFile.CompressionType;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.DefaultCodec;

import com.google.protobuf.CodedInputStream;

import boa.types.Code.CodeRepository;
import boa.types.Code.Revision;
import boa.types.Diff.ChangedFile;
import boa.types.Toplevel.Project;

public class SeqDirectoryCombiner {

	public static void main(String[] args) throws IOException {
		CompressionType compressionType = CompressionType.BLOCK;
		CompressionCodec compressionCode = new DefaultCodec();
		Configuration conf = new Configuration();
		// conf.set("fs.default.name", "hdfs://boa-njt/");
		FileSystem fileSystem = FileSystem.get(conf);
		String inputBase = args[0];
		String outputBase = args[1];

		SequenceFile.Writer projectWriter = SequenceFile.createWriter(fileSystem, conf,
				new Path(outputBase + "/projects.seq"), Text.class, BytesWritable.class, compressionType, compressionCode);
		MapFile.Writer astWriter = new MapFile.Writer(conf, fileSystem, outputBase + "/ast", LongWritable.class,
				BytesWritable.class, compressionType, compressionCode, null);
		MapFile.Writer commitWriter = new MapFile.Writer(conf, fileSystem, outputBase + "/commit", LongWritable.class,
				BytesWritable.class, compressionType, compressionCode, null);

		File[] dir = new File(inputBase).listFiles();
		File outputDir = new File(outputBase);
		if(!outputDir.exists())
			outputDir.mkdirs();
		File outputAstDir = new File(outputBase + "/ast");
		if(!outputAstDir.exists())
			outputAstDir.mkdirs();
		File outputCommitDir = new File(outputBase + "/commit");
		if(!outputCommitDir.exists())
			outputCommitDir.mkdirs();
		long lastAstWriterKey = 0, lastCommitWriterKey = 0;
		for (int j = 0; j < dir.length; j++) {
			FileStatus[] files = fileSystem.listStatus(new Path(dir[j].getPath() + "/project"), new PathFilter() {
				@Override
				public boolean accept(Path path) {
					String name = path.getName();
					return name.endsWith(".seq") && name.contains("-");
				}
			});
			for (int i = 0; i < files.length; i++) {
				FileStatus file = files[i];
				String name = file.getPath().getName();
				System.out.println("Reading file " + (i + 1) + " in " + files.length + ": " + name + " from " + (j+1) + " in " + dir.length);
				SequenceFile.Reader r = new SequenceFile.Reader(fileSystem, file.getPath(), conf);
				Text textKey = new Text() ;
				BytesWritable value = new BytesWritable();
				try {
					while (r.next(textKey, value)) {
						Project p = Project
								.parseFrom(CodedInputStream.newInstance(value.getBytes(), 0, value.getLength()));
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
								for (int k = 0; k < crb.getRevisionKeysCount(); k++) {
									crb.setRevisionKeys(k, lastCommitWriterKey + crb.getRevisionKeys(k));
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
				lastCommitWriterKey = readAndAppendCommit(conf, fileSystem, commitWriter, dir[j].getPath() + "/commit/" + name,
						lastAstWriterKey, lastCommitWriterKey);
				lastAstWriterKey = readAndAppendAst(conf, fileSystem, astWriter, dir[j].getPath() + "/ast/" + name,
						lastAstWriterKey);
			}
		}
		projectWriter.close();
		astWriter.close();
		commitWriter.close();
		fileSystem.close();
	}
	
	
	public static long readAndAppendCommit(Configuration conf, FileSystem fileSystem, MapFile.Writer writer,
			String fileName, long lastAstKey, long lastCommitKey) throws IOException {
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
