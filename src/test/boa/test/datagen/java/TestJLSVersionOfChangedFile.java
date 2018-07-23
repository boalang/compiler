package boa.test.datagen.java;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.SequenceFile.CompressionType;
import org.eclipse.jgit.lib.Constants;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import boa.datagen.DefaultProperties;
import boa.datagen.forges.github.RepositoryCloner;
import boa.datagen.scm.AbstractCommit;
import boa.datagen.scm.GitConnector;
import boa.datagen.util.FileIO;
import boa.functions.BoaIntrinsics;
import boa.types.Code.CodeRepository;
import boa.types.Code.CodeRepository.RepositoryKind;
import boa.types.Code.Revision;
import boa.types.Diff.ChangedFile;

//FIXME autoboxing

@RunWith(Parameterized.class)
public class TestJLSVersionOfChangedFile {

    @Parameters(name = "{index}: {0}")
    public static List<ChangedFile[]> data() throws Exception {
    	List<ChangedFile[]> data = new ArrayList<ChangedFile[]>();
		CodeRepository cr = buildCodeRepository("boalang/compiler");
		String[][] commits = new String[][] {
			{"3a1e352cc63f94058ddb38341531d347f121c29a", "58"},
			{"b82810e725dbf8c7fde6e7fdc034c5676d270313", "1"},
		};
		
		Map<String, Integer> map = new HashMap<String, Integer>();
		for (int i = 0; i < commits.length; i++) {
			String[] commit = commits[i];
			map.put(commit[0], i);
		}
		for (Revision rev : cr.getRevisionsList()) {
			Integer index = map.get(rev.getId());
			if (index != null) {
				String[] commit = commits[index];
				List<ChangedFile> snapshot = rev.getFilesList();
				assertThat(snapshot.size(), Matchers.is(Integer.parseInt(commit[1])));
				
		    	for (ChangedFile cf : snapshot)
		    		data.add(new ChangedFile[]{cf});
			}
		}
    	return data;
    }

    private ChangedFile changedFile;
	
	private static Configuration conf = new Configuration();
	private static FileSystem fileSystem = null;
	
	private static SequenceFile.Writer projectWriter, astWriter, commitWriter, contentWriter;
	private static long astWriterLen = 0, commitWriterLen = 0, contentWriterLen = 0;
	
	public TestJLSVersionOfChangedFile(ChangedFile input) {
		DefaultProperties.DEBUG = true;
		this.changedFile = input;
	}
	
	@Test
	public void testJLSVersion() throws Exception {
		String kind = changedFile.getKind().name();
		String version = kind.substring(kind.lastIndexOf('_') + 1);
		assertThat(changedFile.getName(), Matchers.containsString("/" + version + "/"));
	}
	
	private static CodeRepository buildCodeRepository(String repoName) throws Exception {
		fileSystem = FileSystem.get(conf);
		
		System.out.println("Repo: " + repoName);
		File gitDir = new File("dataset/repos/" + repoName);
		openWriters(gitDir.getAbsolutePath());
		FileIO.DirectoryRemover filecheck = new FileIO.DirectoryRemover(gitDir.getAbsolutePath());
		filecheck.run();
		String url = "https://github.com/" + repoName + ".git";
		RepositoryCloner.clone(new String[]{url, gitDir.getAbsolutePath()});
		GitConnector conn = new GitConnector(gitDir.getAbsolutePath(), repoName, astWriter, astWriterLen, contentWriter, contentWriterLen);
		final CodeRepository.Builder repoBuilder = CodeRepository.newBuilder();
		repoBuilder.setKind(RepositoryKind.GIT);
		repoBuilder.setUrl(url);
		for (final Revision rev : conn.getCommits(true, repoName)) {
			final Revision.Builder revBuilder = Revision.newBuilder(rev);
			repoBuilder.addRevisions(revBuilder);
		}
		if (repoBuilder.getRevisionsCount() > 0) {
//			System.out.println("Build head snapshot");
			repoBuilder.setHead(conn.getHeadCommitOffset());
			repoBuilder.addAllHeadSnapshot(conn.buildHeadSnapshot(new String[] { "java" }, repoName));
		}
		repoBuilder.addAllBranches(conn.getBranchIndices());
		repoBuilder.addAllBranchNames(conn.getBranchNames());
		repoBuilder.addAllTags(conn.getTagIndices());
		repoBuilder.addAllTagNames(conn.getTagNames());
		
		closeWriters();

		List<ChangedFile> snapshot1 = new ArrayList<ChangedFile>();
		Map<String, AbstractCommit> commits = new HashMap<String, AbstractCommit>();
		conn.getSnapshot(conn.getHeadCommitOffset(), snapshot1, commits);
//		System.out.println("Finish building head snapshot");
		List<String> snapshot2 = conn.getSnapshot(Constants.HEAD);
		Set<String> s1 = new HashSet<String>(), s2 = new HashSet<String>(snapshot2);
		for (ChangedFile cf : snapshot1)
			s1.add(cf.getName());
//		System.out.println("Test head snapshot");
		assertEquals(s2, s1);

		for (int i = conn.getRevisions().size()-1; i >= 0; i--) {
			AbstractCommit commit = conn.getRevisions().get(i);
			snapshot1 = new ArrayList<ChangedFile>();
			conn.getSnapshot(i, snapshot1, new HashMap<String, AbstractCommit>());
			snapshot2 = conn.getSnapshot(commit.getId());
			s1 = new HashSet<String>();
			s2 = new HashSet<String>(snapshot2);
			for (ChangedFile cf : snapshot1)
				s1.add(cf.getName());
//			System.out.println("Test snapshot at " + commit.getId());
			assertEquals(s2, s1);
		}
		
		CodeRepository cr = repoBuilder.build();
		
		{
			ChangedFile[] snapshot = BoaIntrinsics.getSnapshot(cr);
			String[] fileNames = new String[snapshot.length];
			for (int i = 0; i < snapshot.length; i++)
				fileNames[i] = snapshot[i].getName();
			Arrays.sort(fileNames);
			String[] expectedFileNames = conn.getSnapshot(Constants.HEAD).toArray(new String[0]);
			Arrays.sort(expectedFileNames);
//			System.out.println("Test head snapshot");
			assertArrayEquals(expectedFileNames, fileNames);
		}
		
		for (Revision rev : cr.getRevisionsList()) {
			ChangedFile[] snapshot = BoaIntrinsics.getSnapshot(cr, rev.getId());
			String[] fileNames = new String[snapshot.length];
			for (int i = 0; i < snapshot.length; i++)
				fileNames[i] = snapshot[i].getName();
			Arrays.sort(fileNames);
			String[] expectedFileNames = conn.getSnapshot(rev.getId()).toArray(new String[0]);
			Arrays.sort(expectedFileNames);
//			System.out.println("Test snapshot at " + rev.getId());
			assertArrayEquals(expectedFileNames, fileNames);
		}
		
		new Thread(new FileIO.DirectoryRemover(gitDir.getAbsolutePath())).start();
		conn.close();
		
		return cr;
	}

	public static void openWriters(String base) {
		long time = System.currentTimeMillis();
		String suffix = time + ".seq";
		while (true) {
			try {
				projectWriter = SequenceFile.createWriter(fileSystem, conf, new Path(base + "/project/" + suffix),
						Text.class, BytesWritable.class, CompressionType.BLOCK);
				astWriter = SequenceFile.createWriter(fileSystem, conf, new Path(base + "/ast/" + suffix),
						LongWritable.class, BytesWritable.class, CompressionType.BLOCK);
				commitWriter = SequenceFile.createWriter(fileSystem, conf, new Path(base + "/commit/" + suffix),
						LongWritable.class, BytesWritable.class, CompressionType.BLOCK);
				contentWriter = SequenceFile.createWriter(fileSystem, conf, new Path(base + "/source/" + suffix),
						LongWritable.class, BytesWritable.class, CompressionType.BLOCK);
				break;
			} catch (Throwable t) {
				t.printStackTrace();
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
			}
		}
	}

	public static void closeWriters() {
		while (true) {
			try {
				projectWriter.close();
				astWriter.close();
				commitWriter.close();
				contentWriter.close();
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
				break;
			} catch (Throwable t) {
				t.printStackTrace();
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
			}
		}
	}

	public void print(Set<String> s, List<ChangedFile> snapshot, Map<String, AbstractCommit> commits) {
		List<String> l = new ArrayList<String>(s);
		Collections.sort(l);
		for (String f : l)
			System.out.println(f + " " + commits.get(f).getId());
		System.out.println("==========================================");
	}
}
