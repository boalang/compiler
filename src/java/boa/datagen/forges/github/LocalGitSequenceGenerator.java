package boa.datagen.forges.github;

import java.io.File;
import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.SequenceFile.CompressionType;

import boa.types.Code.CodeRepository;
import boa.types.Code.Revision;
import boa.types.Code.CodeRepository.RepositoryKind;
import boa.types.Toplevel.Project;
import boa.types.Toplevel.Project.ForgeKind;
import boa.datagen.scm.AbstractConnector;
import boa.datagen.scm.GitConnector;

public class LocalGitSequenceGenerator {
	private static SequenceFile.Writer projectWriter;
	private static SequenceFile.Writer astWriter;
	private static SequenceFile.Writer contentWriter;
	private static Configuration conf = null;
	private static FileSystem fileSystem = null;

	public static void localGitSequenceGenerate(String path, String outputPath) throws IOException {
		conf = new Configuration();
        fileSystem = FileSystem.get(conf);
		openWriters(outputPath);
		try {
			storeRepo(path);
		} catch (final IOException e) {
			e.printStackTrace();
		}
		closeWriters();
	}

	private static void openWriters(String outputPath) {
		while (true) {
			try {
				projectWriter = SequenceFile.createWriter(fileSystem, conf, new Path(outputPath + "/projects.seq"), Text.class, BytesWritable.class, CompressionType.BLOCK);
				astWriter = SequenceFile.createWriter(fileSystem, conf, new Path(outputPath + "/ast.seq"), LongWritable.class, BytesWritable.class, CompressionType.BLOCK);
				contentWriter = SequenceFile.createWriter(fileSystem, conf, new Path(outputPath + "/sources.seq"), LongWritable.class, BytesWritable.class, CompressionType.BLOCK);
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

	private static void closeWriters() {
		while (true) {
			try {
				projectWriter.close();
				astWriter.close();
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

	private static void storeRepo(final String path) throws IOException {
		final Project.Builder projBuilder = Project.newBuilder();
		final File gitDir = new File(path);
		projBuilder.setName(gitDir.getName());
		projBuilder.setId(path);
		projBuilder.setProjectUrl(path);
		projBuilder.setKind(ForgeKind.OTHER);
		
		AbstractConnector conn = null;
		try {
			conn = new GitConnector(gitDir.getAbsolutePath(), "");
			final CodeRepository.Builder repoBuilder = CodeRepository.newBuilder();
			repoBuilder.setUrl(path);
			repoBuilder.setKind(RepositoryKind.GIT);
			for (final Object rev : conn.getRevisions("")) {
				final Revision.Builder revBuilder = Revision.newBuilder((Revision) rev);
				repoBuilder.addRevisions(revBuilder);
			}
			if (repoBuilder.getRevisionsCount() > 0)
				repoBuilder.setHead(conn.getHeadCommitOffset());
			repoBuilder.addAllBranches(conn.getBranchIndices());
			repoBuilder.addAllBranchNames(conn.getBranchNames());
			repoBuilder.addAllTags(conn.getTagIndices());
			repoBuilder.addAllTagNames(conn.getTagNames());
			
			projBuilder.addCodeRepositories(repoBuilder);
		} catch (final Exception e) {
			e.printStackTrace();
		} finally {
			if (conn != null)
				try {
					conn.close();
				} catch (final Exception e) {
					e.printStackTrace();
				}
		}
		final Project project = projBuilder.build();
		// System.out.println(project);
		projectWriter.append(new Text(project.getId()), new BytesWritable(project.toByteArray()));
	}
}
