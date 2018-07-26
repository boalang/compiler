package boa.test.datagen;

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
import org.junit.Ignore;
import org.junit.Test;

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

public class TestBuildSnapshot {
	
	@Test
	public void testBuildSnapshot() throws Exception {
		DefaultProperties.DEBUG = true;
		
		String[] repoNames = new String[]{"candoia/candoia", "boalang/compiler", "junit-team/junit4"};
		for (String repoName : repoNames)
			buildCodeRepository(repoName);
	}
	
	@Ignore
	@Test
	public void testBuildSnapshotWithTypes() throws Exception {
		DefaultProperties.DEBUG = true;
		
		File gitDir = new File("D:/Projects/Boa-compiler/dataset/repos/candoia/candoia");
//		File gitDir = new File("D:/Projects/Boa-compiler/dataset/repos/boalang/compiler");
//		File gitDir = new File("F:\\testrepos\\repos-test\\hoan\\test1");
		if (!gitDir.exists())
			return;
		GitConnector gc = new GitConnector(gitDir.getAbsolutePath(), "condoia");
		gc.setRevisions();
		System.out.println("Finish processing commits");
		List<ChangedFile> snapshot1 = gc.buildHeadSnapshot(new String[]{"java"}, "");
		System.out.println("Finish building head snapshot");
		List<String> snapshot2 = gc.getSnapshot(Constants.HEAD);
		gc.close();
		Set<String> s1 = new HashSet<String>(), s2 = new HashSet<String>(snapshot2), s = new HashSet<String>(s2), in2 = new HashSet<String>(s2);
		for (ChangedFile cf : snapshot1)
			s1.add(cf.getName());
//		print(s1);
//		print(s2);
//		s.retainAll(s1);
//		print(s);
//		in2.removeAll(s1);
//		print(in2);
		System.out.println(s1.size() + " " + s2.size() + " " + s.size() + " " + in2.size());
		assertEquals(s2,  s1);
	}

	private static Configuration conf = new Configuration();
	private static FileSystem fileSystem = null;
	
	private SequenceFile.Writer projectWriter, astWriter, commitWriter, contentWriter;
	private long astWriterLen = 0, contentWriterLen = 0;
	
	@Test
	public void testGetSnapshotFromProtobuf1() throws Exception {
		DefaultProperties.DEBUG = true;
		
		CodeRepository cr = buildCodeRepository("boalang/test-datagen");

		ChangedFile[] snapshot = BoaIntrinsics.getSnapshotById(cr, "8041f1281cf6b615861768631097e22127a1e32e", new String[]{"SOURCE_JAVA_JLS"});
		String[] fileNames = new String[snapshot.length];
		for (int i = 0; i < snapshot.length; i++)
			fileNames[i] = snapshot[i].getName();
		assertArrayEquals(new String[]{}, fileNames);
		
		snapshot = BoaIntrinsics.getSnapshotById(cr, "269424473466542fad9c426f7edf7d10a742e2be", new String[]{"SOURCE_JAVA_JLS"});
		fileNames = new String[snapshot.length];
		for (int i = 0; i < snapshot.length; i++)
			fileNames[i] = snapshot[i].getName();
		assertArrayEquals(new String[]{"src/Foo.java"}, fileNames);
		
		snapshot = BoaIntrinsics.getSnapshotById(cr, "5e9291c8e830754479bf836686734045faa5c021", new String[]{"SOURCE_JAVA_JLS"});
		fileNames = new String[snapshot.length];
		for (int i = 0; i < snapshot.length; i++)
			fileNames[i] = snapshot[i].getName();
		assertArrayEquals(new String[]{}, fileNames);
		
		snapshot = BoaIntrinsics.getSnapshotById(cr, "06288fd7cf36415629e3eafdce2448a5406a8c1e", new String[]{"SOURCE_JAVA_JLS"});
		fileNames = new String[snapshot.length];
		for (int i = 0; i < snapshot.length; i++)
			fileNames[i] = snapshot[i].getName();
		assertArrayEquals(new String[]{}, fileNames);
	}
	
	@Test
	public void testGetSnapshotFromProtobuf2() throws Exception {
		DefaultProperties.DEBUG = true;
		
		CodeRepository cr = buildCodeRepository("hyjorc1/my-example");

		ChangedFile[] snapshot = BoaIntrinsics.getSnapshot(cr);
		String[] fileNames = new String[snapshot.length];
		for (int i = 0; i < snapshot.length; i++)
			fileNames[i] = snapshot[i].getName();
//			assertArrayEquals(new String[]{}, fileNames);
		
		snapshot = BoaIntrinsics.getSnapshotById(cr, "d7a4aced37af672f9a55238a47bb0e4974193ebe");
		fileNames = new String[snapshot.length];
		for (int i = 0; i < snapshot.length; i++)
			fileNames[i] = snapshot[i].getName();
		assertThat(fileNames, Matchers.hasItemInArray("src/org/birds/Bird.java"));
		assertThat(fileNames, Matchers.not(Matchers.hasItemInArray("src/org/animals/Bird.java")));
	}

	private CodeRepository buildCodeRepository(String repoName) throws Exception {
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

	public void openWriters(String base) {
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

	public void closeWriters() {
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


