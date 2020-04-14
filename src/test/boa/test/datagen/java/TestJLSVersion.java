package boa.test.datagen.java;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
public class TestJLSVersion {

    @Parameters(name = "{index}: {0}")
    public static List<Object[]> data() throws Exception {
    	List<Object[]> data = new ArrayList<Object[]>();
		CodeRepository cr = buildCodeRepository("boalang/test-datagen");
		String[][] commits = new String[][] {
			{"15c9685cbd36edba1709637bb8f8c217c894bee6", "58"},
			{"30e04b12074b7288b4d2cf166c5c7c98a243d1ee", "59"},
		};
		
		for (String[] commit : commits) {
			ChangedFile[] snapshot = BoaIntrinsics.getSnapshotById(cr, commit[0], new String[]{"SOURCE_JAVA_"});
			assertThat(snapshot.length, Matchers.is(Integer.parseInt(commit[1])));
			
	    	for (ChangedFile cf : snapshot)
	    		data.add(new Object[]{cf.getName(), cf});
		}
    	return data;
    }
    
    private ChangedFile changedFile;
	
	private static Configuration conf = new Configuration();
	private static FileSystem fileSystem = null;
	
	private static SequenceFile.Writer projectWriter, astWriter, commitWriter, contentWriter;
	private static long astWriterLen = 1, commitWriterLen = 1, contentWriterLen = 1;
	
	public TestJLSVersion(String name, ChangedFile input) {
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
		GitConnector conn = new GitConnector(gitDir.getAbsolutePath(), repoName, astWriter, astWriterLen, commitWriter, commitWriterLen, contentWriter, contentWriterLen);
		final CodeRepository.Builder repoBuilder = CodeRepository.newBuilder();
		repoBuilder.setKind(RepositoryKind.GIT);
		repoBuilder.setUrl(url);
		for (final Object rev : conn.getRevisions(repoName)) {
			final Revision.Builder revBuilder = Revision.newBuilder((Revision) rev);
			repoBuilder.addRevisions(revBuilder);
		}
		if (repoBuilder.getRevisionsCount() > 0) {
//			System.out.println("Build head snapshot");
			repoBuilder.setHead(conn.getHeadCommitOffset());
			repoBuilder.addAllHeadSnapshot(conn.buildHeadSnapshot());
		}
		repoBuilder.addAllBranches(conn.getBranchIndices());
		repoBuilder.addAllBranchNames(conn.getBranchNames());
		repoBuilder.addAllTags(conn.getTagIndices());
		repoBuilder.addAllTagNames(conn.getTagNames());
		
		closeWriters();

		List<ChangedFile> snapshot1 = new ArrayList<ChangedFile>();
		conn.getSnapshot(conn.getHeadCommitOffset(), snapshot1);
//		System.out.println("Finish building head snapshot");
		List<String> snapshot2 = conn.getSnapshot(Constants.HEAD);
		Set<String> s1 = new HashSet<String>(), s2 = new HashSet<String>(snapshot2);
		for (ChangedFile cf : snapshot1)
			s1.add(cf.getName());
//		System.out.println("Test head snapshot");
		assertEquals(s2, s1);

		for (int i = conn.getRevisions().size()-1; i >= 0; i--) {
			AbstractCommit commit = (AbstractCommit) conn.getRevisions().get(i);
			snapshot1 = new ArrayList<ChangedFile>();
			conn.getSnapshot(i, snapshot1);
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
			ChangedFile[] snapshot = BoaIntrinsics.getSnapshotById(cr, rev.getId());
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
