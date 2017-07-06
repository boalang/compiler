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

import boa.types.Code.CodeRepository;
import boa.types.Code.Revision;
import boa.types.Code.CodeRepository.RepositoryKind;
import boa.types.Toplevel.Project;
import boa.types.Toplevel.Project.ForgeKind;
import boa.datagen.scm.AbstractConnector;
import boa.datagen.scm.GitConnector;

public class LocalGitSequenceGenerator {

	private static SequenceFile.Writer projectWriter, astWriter;
	private static Configuration conf = null;
	private static FileSystem fileSystem = null;

	public LocalGitSequenceGenerator() {
		// TODO Auto-generated constructor stub
	}

	public static void localGitSequenceGenerate(String path, String outputPath) throws IOException {
		conf = new Configuration();
        fileSystem = FileSystem.get(conf);
		openWriters(outputPath);
		try {
			storeRepo(path);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		closeWriters();
	}

	private static void openWriters(String outputPath) {
		while (true) {
			try {
				projectWriter = SequenceFile.createWriter(fileSystem, conf,
						new Path(outputPath+"/projects.seq"), Text.class,
						BytesWritable.class);
				astWriter = SequenceFile.createWriter(fileSystem, conf,
						new Path(outputPath+"/ast.seq"), LongWritable.class, BytesWritable.class);
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

	private static void storeRepo(String path) throws IOException {
		final Project.Builder projBuilder = Project.newBuilder();
		final File gitDir = new File(path);
		projBuilder.setName(gitDir.getName());
		projBuilder.setId(path);
		projBuilder.setProjectUrl(path);
		projBuilder.setKind(ForgeKind.OTHER);
		
		AbstractConnector conn = null;
		try {
			conn = new GitConnector(gitDir.getAbsolutePath());
			final CodeRepository.Builder repoBuilder = CodeRepository.newBuilder();
			repoBuilder.setUrl(path);
			repoBuilder.setKind(RepositoryKind.GIT);
			for (final Revision rev : conn.getCommits(true, astWriter)) {
				final Revision.Builder revBuilder = Revision.newBuilder(rev);
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
		Project project = projBuilder.build();
		// System.out.println(project);
		projectWriter.append(new Text(project.getId()), new BytesWritable(project.toByteArray()));
	}
}
