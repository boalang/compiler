package boa.test.datagen;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.eclipse.jgit.lib.Constants;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.google.protobuf.CodedInputStream;

import boa.datagen.BoaGenerator;
import boa.datagen.DefaultProperties;
import boa.datagen.forges.github.RepositoryCloner;
import boa.datagen.scm.GitConnector;
import boa.datagen.util.FileIO;
import boa.functions.BoaAstIntrinsics;
import boa.functions.BoaIntrinsics;
import boa.types.Code.CodeRepository;
import boa.types.Diff.ChangedFile;
import boa.types.Toplevel.Project;

@RunWith(Parameterized.class)
public class TestBuildSnapshotFromSequenceFile {
	static String dataPath = "dataset/temp_data";
	static File dataFile = new File(dataPath);
	
    @Parameters(name = "{index}: repo {0} commit {2}")
    public static List<Object[]> data() throws Exception {
    	dataPath = dataFile.getAbsolutePath();
    	
    	DefaultProperties.DEBUG = true;
    	DefaultProperties.localDataPath = dataPath;
		
		new FileIO.DirectoryRemover(dataFile.getAbsolutePath()).run();
		
		String[] args = {	"-inputJson", "test/datagen/jsons", 
							"-inputRepo", "dataset/repos",
							"-output", dataPath,
							"-commits", "1",
							"-threads", "2"};
		BoaGenerator.main(args);
		
    	List<Object[]> data = new ArrayList<Object[]>();
    	
		Configuration conf = new Configuration();
		FileSystem fileSystem = FileSystem.get(conf);
		Path projectPath = new Path(dataPath, "projects.seq");
		SequenceFile.Reader pr = new SequenceFile.Reader(fileSystem, projectPath, conf);
		Writable key = new Text();
		BytesWritable val = new BytesWritable();
		while (pr.next(key, val)) {
			byte[] bytes = val.getBytes();
			Project project = Project.parseFrom(CodedInputStream.newInstance(bytes, 0, val.getLength()));
			String repoName = project.getName();
			File gitDir = new File("dataset/repos/" + repoName);
			new FileIO.DirectoryRemover(gitDir.getAbsolutePath()).run();
			String url = "https://github.com/" + repoName + ".git";
			RepositoryCloner.clone(new String[]{url, gitDir.getAbsolutePath()});
			GitConnector conn = new GitConnector(gitDir.getAbsolutePath(), repoName);
			
//			ChangedFile[] snapshot = getSnapshot(dataPath, repoName, -1);
//			String[] fileNames = new String[snapshot.length];
//			for (int i = 0; i < snapshot.length; i++)
//				fileNames[i] = snapshot[i].getName();
//			Arrays.sort(fileNames);
//			String[] expectedFileNames = conn.getSnapshot(Constants.HEAD).toArray(new String[0]);
//			Arrays.sort(expectedFileNames);
//			System.out.println("Test head snapshot");
//			assertArrayEquals(expectedFileNames, fileNames);
			
			List<String> commitIds = conn.logCommitIds();
			Random rand = new Random();
			for (int i = 0; i < commitIds.size(); i++) {
				if (rand.nextBoolean())
					continue;
				String cid = commitIds.get(i);
				data.add(new Object[] { repoName, i, cid });
			}
			conn.close();
		}
		pr.close();
    	
    	return data;
    }

    private static String repoName;
    private static int index;
    private static String commitId;
    
	public TestBuildSnapshotFromSequenceFile(String repoName, int index, String commitId) {
		super();
		TestBuildSnapshotFromSequenceFile.repoName = repoName;
		TestBuildSnapshotFromSequenceFile.index = index;
		TestBuildSnapshotFromSequenceFile.commitId = commitId;
	}

	@Test
	public void testBuildSnapshotFromSeq() throws Exception {
		File gitDir = new File("dataset/repos/" + repoName);
		if (!gitDir.exists()) {
			String url = "https://github.com/" + repoName + ".git";
			RepositoryCloner.clone(new String[]{url, gitDir.getAbsolutePath()});
		}
		GitConnector conn = new GitConnector(gitDir.getAbsolutePath(), repoName);
		ChangedFile[] snapshot = getSnapshot(dataPath, repoName, index);
		String[] fileNames = new String[snapshot.length];
		for (int j = 0; j < snapshot.length; j++) {
			fileNames[j] = snapshot[j].getName();
//			System.out.println(fileNames[j]);
		}
		Arrays.sort(fileNames);
		String[] expectedFileNames = conn.getSnapshot(commitId).toArray(new String[0]);
		Arrays.sort(expectedFileNames);
//		System.out.println("Test snapshot at " + commitId);
		assertArrayEquals(expectedFileNames, fileNames);
		conn.close();
	}

	public static ChangedFile[] getSnapshot(String dataPath, String repoName, int index) throws Exception {
		TestBuildSnapshotFromSequenceFile.repoName = repoName;
		TestBuildSnapshotFromSequenceFile.index = index;
		TestBuildSnapshotFromSequenceFile.snapshot = null;
		
		File outDir = new File("dataset/temp_output");
		if (outDir.exists())
			new FileIO.DirectoryRemover(outDir.getAbsolutePath()).run();
		
		Configuration conf = new Configuration();
		Job job = new Job(conf, "read sequence file");
		job.setJarByClass(TestBuildSnapshotFromSequenceFile.class);
		job.setMapperClass(SequenceFileReaderMapper.class);
		job.setCombinerClass(SequenceFileReaderReducer.class);
		job.setReducerClass(SequenceFileReaderReducer.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);
		job.setInputFormatClass(org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat.class);
		FileInputFormat.addInputPath(job, new Path(dataPath, "projects.seq"));
		FileOutputFormat.setOutputPath(job, new Path(outDir.getAbsolutePath()));
		boolean completed = job.waitForCompletion(false);
		assertEquals(completed, true);

		if (outDir.exists())
			new FileIO.DirectoryRemover(outDir.getAbsolutePath()).run();
		
		return snapshot;
	}

	static ChangedFile[] snapshot;

	public static class SequenceFileReaderMapper extends Mapper<Text, BytesWritable, Text, IntWritable> {
		
		@Override
		protected void setup(Mapper<Text, BytesWritable, Text, IntWritable>.Context context)
				throws IOException, InterruptedException {
			BoaAstIntrinsics.setup(context);
			super.setup(context);
		}

		@Override
		public void map(Text key, BytesWritable value, Context context) throws IOException, InterruptedException {
			Project project = Project.parseFrom(CodedInputStream.newInstance(value.getBytes(), 0, value.getLength()));
			if (project.getName().equals(repoName)) {
				for (CodeRepository cr : project.getCodeRepositoriesList()) {
					try {
						if (index == -1)
							snapshot = BoaIntrinsics.getSnapshot(cr);
						else
							snapshot = BoaIntrinsics.getSnapshotByIndex(cr, index);
						context.write(key, new IntWritable(1));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		
		@Override
		protected void cleanup(Mapper<Text, BytesWritable, Text, IntWritable>.Context context)
				throws IOException, InterruptedException {
			BoaAstIntrinsics.cleanup(context);
			super.cleanup(context);
		}
	}

	public static class SequenceFileReaderReducer extends Reducer<Text, IntWritable, Text, IntWritable> {

		@Override
		public void reduce(Text key, Iterable<IntWritable> values, Context context)
				throws IOException, InterruptedException {
			int sum = 0;
			for (IntWritable val : values) {
				sum += val.get();
			}
			assertThat(sum, Matchers.is(1));
			context.write(key, new IntWritable(sum));
		}
	}
	
}
