package boa.datagen.slurm;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.SequenceFile.CompressionType;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.DefaultCodec;

import com.google.protobuf.CodedInputStream;

import boa.types.Code.CodeRepository;
import boa.types.Toplevel.Project;

//Datagen Phase 3: combine sequence files
public class SeqRepoProjectPartition {

	private static String DATASET_PATH; // generated dataset in phase 3

	private static FileSystem fs;
	private static Configuration conf;

	private static SequenceFile.Writer fullProjectWriter;
	private static SequenceFile.Writer mediumProjectWriter;
	private static SequenceFile.Writer smallProjectWriter;
	private static SequenceFile.Writer tinyProjectWriter;

	/*
	 * TODO: We should evenly distributed projects on the sequence file 
	 * based on commit count. This change can improve query performance
	 * by leveraging all maps.
	 */
	public static void main(String[] args) {

		if (args.length < 1) {
			System.err.println("Need args:\n" + "DATASET_PATH\n");
			return;
		}

		DATASET_PATH = args[0];

		try {
			conf = new Configuration();
			fs = FileSystem.get(conf);

			// remove combined seq files
			checkAndRemove(DATASET_PATH + "/combined/project");

			int astMapSuffix = 0, fileCount = 0, projectCount = 0;
			openWriters(astMapSuffix);

			String projectPath = DATASET_PATH + "/combined/projects.seq";

			SequenceFile.Reader r = null;
			System.out.println("Reading file " + projectPath);
			try {
				System.out.println("Reading file " + fileCount + " : " + projectPath);
				r = new SequenceFile.Reader(fs, new Path(projectPath), conf);
				Text textKey = new Text();
				BytesWritable value = new BytesWritable();

				while (r.next(textKey, value)) {
					Project p = Project.parseFrom(CodedInputStream.newInstance(value.getBytes(), 0, value.getLength()));

					if (!fullProjectFilter(p))
						fullProjectWriter.append(textKey, new BytesWritable(p.toByteArray()));

					if (!mediumProjectFilter(p))
						mediumProjectWriter.append(textKey, new BytesWritable(p.toByteArray()));
					
					if (!smallProjectFilter(p))
						smallProjectWriter.append(textKey, new BytesWritable(p.toByteArray()));
					
					if (!tinyProjectFilter(p))
						tinyProjectWriter.append(textKey, new BytesWritable(p.toByteArray()));

					System.out.println("Finish " + ++projectCount + "th project " + p.getName());
				}
			} catch (EOFException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (r != null)
					r.close();
			}

			closeWriters();
			fs.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static boolean fullProjectFilter(Project p) {

		// remove these projects
		switch (p.getName()) {
		case "lambdalab-mirror/jdk8u-jdk":
		case "frohoff/jdk7u":
		case "iDempiereOSGiERP/idempiere":
		case "luyu1567/CNiere":
		case "arodchen/maxine":
		case "ehrmann/thrift-task":
			return true;
		default:
			break;
		}
	
		// remove commits larger than 10,000
		int commitCount = 0;
		for (CodeRepository cr : p.getCodeRepositoriesList())
			commitCount = cr.getRevisionsCount() > 0 ? cr.getRevisionsCount() : cr.getRevisionKeysCount();
		if (commitCount > 10000)
			return true;

		return false;
	}

	private static boolean mediumProjectFilter(Project p) {
		if (fullProjectFilter(p))
			return true;
		if (new Random().nextDouble() > 0.1) // filter out 90%
			return true;
		return false;
	}
	
	private static boolean smallProjectFilter(Project p) {
		if (fullProjectFilter(p))
			return true;
		if (new Random().nextDouble() > 0.01) // filter out 99%
			return true;
		return false;
	}
	
	private static boolean tinyProjectFilter(Project p) {
		if (fullProjectFilter(p))
			return true;
		if (new Random().nextDouble() > 0.001) // filter out 99.9%
			return true;
		return false;
	}


	public static void openWriters(int astCount) {
		CompressionType compType = CompressionType.BLOCK;
		CompressionCodec compCode = new DefaultCodec();
		try {
			fullProjectWriter = SequenceFile.createWriter(fs, conf,
					new Path(DATASET_PATH + "/combined/project/full/projects.seq"), Text.class, BytesWritable.class,
					compType, compCode);
			mediumProjectWriter = SequenceFile.createWriter(fs, conf,
					new Path(DATASET_PATH + "/combined/project/medium/projects.seq"), Text.class, BytesWritable.class,
					compType, compCode);
			smallProjectWriter = SequenceFile.createWriter(fs, conf,
					new Path(DATASET_PATH + "/combined/project/small/projects.seq"), Text.class, BytesWritable.class,
					compType, compCode);
			tinyProjectWriter = SequenceFile.createWriter(fs, conf,
					new Path(DATASET_PATH + "/combined/project/tiny/projects.seq"), Text.class, BytesWritable.class,
					compType, compCode);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void closeWriters() {
		try {
			fullProjectWriter.close();
			mediumProjectWriter.close();
			smallProjectWriter.close();
			tinyProjectWriter.close();
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
}