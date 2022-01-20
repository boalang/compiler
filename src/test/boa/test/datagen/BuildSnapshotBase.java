package boa.test.datagen;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;

import java.io.File;
import java.nio.file.Files;
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

public class BuildSnapshotBase {
	protected final Configuration conf = new Configuration();
	protected FileSystem fileSystem = null;

	protected SequenceFile.Writer projectWriter;
	protected SequenceFile.Writer astWriter;
	protected SequenceFile.Writer commitWriter;
	protected SequenceFile.Writer contentWriter;
	protected long astWriterLen = 1;
	protected long commitWriterLen = 1;
	protected long contentWriterLen = 1;

	protected CodeRepository buildCodeRepository(final String repoName) throws Exception {
		fileSystem = FileSystem.get(conf);

		final File gitDir = Files.createTempDirectory(repoName.replaceAll("/", "_")).toFile();
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
		List<String> snapshot2 = conn.getSnapshot(Constants.HEAD);
		Set<String> s1 = new HashSet<String>();
		Set<String> s2 = new HashSet<String>(snapshot2);
		for (final ChangedFile cf : snapshot1)
			s1.add(cf.getName());
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
			assertArrayEquals(expectedFileNames, fileNames);
		}

		for (final Revision rev : cr.getRevisionsList()) {
			final String[] expectedFileNames = conn.getSnapshot(rev.getId()).toArray(new String[0]);
			Arrays.sort(expectedFileNames);

			ChangedFile[] snapshot = BoaIntrinsics.getSnapshotById(cr, rev.getId());
			String[] fileNames = new String[snapshot.length];
			for (int i = 0; i < snapshot.length; i++)
				fileNames[i] = snapshot[i].getName();
			Arrays.sort(fileNames);
			assertArrayEquals(expectedFileNames, fileNames);

			snapshot = BoaIntrinsics.getSnapshot(cr, rev);
			fileNames = new String[snapshot.length];
			for (int i = 0; i < snapshot.length; i++)
				fileNames[i] = snapshot[i].getName();
			Arrays.sort(fileNames);
			assertArrayEquals(expectedFileNames, fileNames);
		}

		// test changed files for each commit
		for (final Revision rev : cr.getRevisionsList()) {
			final String[] expectedFileNames = conn.getDiffFiles(rev.getId()).toArray(new String[0]);
			final String[] actualFileNames = new String[rev.getFilesCount()];
			for (int i = 0; i < actualFileNames.length; i++)
				actualFileNames[i] = rev.getFiles(i).getName();
			Arrays.sort(expectedFileNames);
			Arrays.sort(actualFileNames);
			assertArrayEquals(expectedFileNames, actualFileNames);
		}

		new Thread(new FileIO.DirectoryRemover(gitDir.getAbsolutePath())).start();
		conn.close();

		return cr;
	}

	protected void openWriters(final String base) {
		final String suffix = System.currentTimeMillis() + ".seq";
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

	protected void closeWriters() {
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
}
