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
		final List<Object[]> data = new ArrayList<Object[]>();
		final CodeRepository cr = buildCodeRepository("boalang/test-datagen");
		final String[][] commits = new String[][] {
			{"f38c17a051b24e901e163baf824409fa1e556c55", "62"},
			//{"15c9685cbd36edba1709637bb8f8c217c894bee6", "58"},
			//{"30e04b12074b7288b4d2cf166c5c7c98a243d1ee", "59"},
		};

		for (final String[] commit : commits) {
			final ChangedFile[] snapshot = BoaIntrinsics.getSnapshotById(cr, commit[0], new String[]{"SOURCE_JAVA_"});
			assertThat(snapshot.length, Matchers.is(Integer.parseInt(commit[1])));

			for (final ChangedFile cf : snapshot)
				data.add(new Object[]{cf.getName(), cf});
		}
		return data;
	}

	private ChangedFile changedFile;

	private static Configuration conf = new Configuration();
	private static FileSystem fileSystem = null;

	private static SequenceFile.Writer projectWriter;
	private static SequenceFile.Writer astWriter;
	private static SequenceFile.Writer commitWriter;
	private static SequenceFile.Writer contentWriter;
	private static long astWriterLen = 1;
	private static long commitWriterLen = 1;
	private static long contentWriterLen = 1;

	public TestJLSVersion(final String name, final ChangedFile input) {
		DefaultProperties.DEBUG = true;
		this.changedFile = input;
	}

	@Test
	public void testJLSVersion() throws Exception {
		final String kind = changedFile.getKind().name();
		final String version = kind.substring(kind.lastIndexOf('_') + 1);
		assertThat(changedFile.getName(), Matchers.containsString("src/" + version + "/"));
	}

	private static CodeRepository buildCodeRepository(final String repoName) throws Exception {
		fileSystem = FileSystem.get(conf);

		System.out.println("Repo: " + repoName);
		final File gitDir = new File("dataset/repos/" + repoName);
		openWriters(gitDir.getAbsolutePath());
		FileIO.DirectoryRemover filecheck = new FileIO.DirectoryRemover(gitDir.getAbsolutePath());
		filecheck.run();
		final String url = "https://github.com/" + repoName + ".git";
		RepositoryCloner.clone(new String[]{url, gitDir.getAbsolutePath()});
		final GitConnector conn = new GitConnector(gitDir.getAbsolutePath(), repoName, astWriter, astWriterLen, commitWriter, commitWriterLen, contentWriter, contentWriterLen);
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
		Set<String> s1 = new HashSet<String>();
		Set<String> s2 = new HashSet<String>(snapshot2);
		for (final ChangedFile cf : snapshot1)
			s1.add(cf.getName());
//		System.out.println("Test head snapshot");
		assertEquals(s2, s1);

		for (int i = conn.getRevisions().size()-1; i >= 0; i--) {
			final AbstractCommit commit = (AbstractCommit) conn.getRevisions().get(i);
			snapshot1 = new ArrayList<ChangedFile>();
			conn.getSnapshot(i, snapshot1);
			snapshot2 = conn.getSnapshot(commit.getId());
			s1 = new HashSet<String>();
			s2 = new HashSet<String>(snapshot2);
			for (final ChangedFile cf : snapshot1)
				s1.add(cf.getName());
//			System.out.println("Test snapshot at " + commit.getId());
			assertEquals(s2, s1);
		}

		final CodeRepository cr = repoBuilder.build();

		{
			final ChangedFile[] snapshot = BoaIntrinsics.getSnapshot(cr);
			final String[] fileNames = new String[snapshot.length];
			for (int i = 0; i < snapshot.length; i++)
				fileNames[i] = snapshot[i].getName();
			Arrays.sort(fileNames);
			final String[] expectedFileNames = conn.getSnapshot(Constants.HEAD).toArray(new String[0]);
			Arrays.sort(expectedFileNames);
//			System.out.println("Test head snapshot");
			assertArrayEquals(expectedFileNames, fileNames);
		}

		for (final Revision rev : cr.getRevisionsList()) {
			final ChangedFile[] snapshot = BoaIntrinsics.getSnapshotById(cr, rev.getId());
			final String[] fileNames = new String[snapshot.length];
			for (int i = 0; i < snapshot.length; i++)
				fileNames[i] = snapshot[i].getName();
			Arrays.sort(fileNames);
			final String[] expectedFileNames = conn.getSnapshot(rev.getId()).toArray(new String[0]);
			Arrays.sort(expectedFileNames);
//			System.out.println("Test snapshot at " + rev.getId());
			assertArrayEquals(expectedFileNames, fileNames);
		}

		new Thread(new FileIO.DirectoryRemover(gitDir.getAbsolutePath())).start();
		conn.close();

		return cr;
	}

	public static void openWriters(final String base) {
		final long time = System.currentTimeMillis();
		final String suffix = time + ".seq";
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
			} catch (final Throwable t) {
				t.printStackTrace();
				try {
					Thread.sleep(1000);
				} catch (final InterruptedException e) {
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
				} catch (final InterruptedException e) {
				}
				break;
			} catch (final Throwable t) {
				t.printStackTrace();
				try {
					Thread.sleep(1000);
				} catch (final InterruptedException e) {
				}
			}
		}
	}

	public void print(final Set<String> s, final List<ChangedFile> snapshot, final Map<String, AbstractCommit> commits) {
		final List<String> l = new ArrayList<String>(s);
		Collections.sort(l);
		for (final String f : l)
			System.out.println(f + " " + commits.get(f).getId());
		System.out.println("==========================================");
	}
}
