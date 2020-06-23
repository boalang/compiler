package boa.datagen.slurm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.SequenceFile.CompressionType;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import boa.datagen.DefaultProperties;
import boa.datagen.forges.github.RepoMetadata;
import boa.datagen.scm.AbstractConnector;
import boa.datagen.scm.GitConnector;
import boa.datagen.util.FileIO;
import boa.types.Code.CodeRepository;
import boa.types.Code.Revision;
import boa.types.Toplevel.Project;

//Datagen Phase 2: DATAGEN_JAR used for each slurm job 
public class SeqRepoBuilder {

	private static String REPO_PATH;
	private static String JSON_FILES_PATH;
	private static String OUTPUT_PATH;

	private static Configuration conf = null;
	private static FileSystem fileSystem = null;

	private static String suffix;
	private static SequenceFile.Writer projectWriter, astWriter, commitWriter, contentWriter;
	private static long astWriterLen = 1, commitWriterLen = 1, contentWriterLen = 1;

	private static int MAX_COMMITS = Integer.valueOf(DefaultProperties.MAX_SIZE_FOR_PROJECT_WITH_COMMITS);;

	public static void main(String[] args) throws IOException {

		if (args.length < 3) {
			System.err.println("Need args:\n" + "REPO_PATH\n" + "JSON_FILES_PATH\n" + "OUTPUT_PATH\n");
			return;
		}

		REPO_PATH = args[0];
		JSON_FILES_PATH = args[1];
		OUTPUT_PATH = args[2];

		conf = new Configuration();
		fileSystem = FileSystem.get(conf);
		boa.datagen.DefaultProperties.DEBUG = true;

		int counter = 0;
		for (String jsonFilePath : getJsonFilePaths()) {
			File file = new File(jsonFilePath);
			String content = FileIO.readFileContents(file);
			Gson parser = new Gson();
			JsonArray repoArray = null;
			try {
				repoArray = parser.fromJson(content, JsonElement.class).getAsJsonArray();
			} catch (Exception e) {
				System.err.println("Error proccessing page: " + file.getPath());
				e.printStackTrace();
				continue;
			}
			// iterate each json object (project metadata) in the json array
			for (int i = 0; i < repoArray.size(); i++) {
				JsonObject rp = repoArray.get(i).getAsJsonObject();
				RepoMetadata repo = new RepoMetadata(rp);
				if (repo.id != null && repo.name != null) {
					System.out.println("Processing the " + (++counter) + "th project: " + repo.name + " with id: "
							+ repo.id + " from the " + i + "th object of the json file: " + file.getPath());
					// generate seq files for this project
					Project project = repo.toBoaMetaDataProtobuf();
					process(project);
				}
			}
		}

		// done
	}

	private static void process(Project project) {
		String projectName = project.getName();
		String[] writerPaths = openWriters(projectName);
		
		// if writerPaths is null, then the project is processed.
		if (writerPaths == null)
			return;

		try {
			project = storeRepository(project, 0);
			// if the project is null then skip this project
			if (project == null) {
				System.err.println(projectName + " is null skip this");
				clear(writerPaths);
				return;
			}

			// store project into sequence file
			BytesWritable bw = new BytesWritable(project.toByteArray());
			if (bw.getLength() <= MAX_COMMITS || (project.getCodeRepositoriesCount() > 0
					&& project.getCodeRepositories(0).getRevisionKeysCount() > 0)) {
				// Approach 1: if the Project size is acceptable, then directly append the
				// Project instance into the sequence file
				projectWriter.append(new Text(project.getId()), bw);
			} else {
				// Approach 2: if the size is too large, extract Commit instances and append
				// them into commit sequence file.
				Project.Builder pb = Project.newBuilder(project);
				for (CodeRepository.Builder cb : pb.getCodeRepositoriesBuilderList()) {
					for (Revision.Builder rb : cb.getRevisionsBuilderList()) {
						cb.addRevisionKeys(commitWriterLen);
						bw = new BytesWritable(rb.build().toByteArray());
						commitWriter.append(new LongWritable(commitWriterLen), bw);
						commitWriterLen += bw.getLength();
					}
					cb.clearRevisions();
				}
				projectWriter.append(new Text(pb.getId()), new BytesWritable(pb.build().toByteArray()));
			}
		} catch (Throwable e) {
			e.printStackTrace();
			clear(writerPaths);
			return;
		}

		closeWriters();
	}

	private static void clear(String[] writerPaths) {
		closeWriters();
		// remove sequence files
		for (String path : writerPaths) {
			File file = new File(path);
			if (file.exists())
				org.apache.commons.io.FileUtils.deleteQuietly(file);
		}
	}

	private static Project storeRepository(final Project project, final int i) {
		final CodeRepository repo = project.getCodeRepositories(i); // this is an empty code repo
		final Project.Builder projBuilder = Project.newBuilder(project);

		final String name = project.getName();
		File gitDir = new File(REPO_PATH + "/" + name);

		// return null to skip empty project
		if (isFiltered(project)) {
			System.err.println(name + " is filtered");
			return null;
		}

		AbstractConnector conn = null;
		try {
			conn = new GitConnector(gitDir.getAbsolutePath(), project.getName(), astWriter, astWriterLen, commitWriter,
					commitWriterLen, contentWriter, contentWriterLen);
			final CodeRepository.Builder repoBuilder = CodeRepository.newBuilder(repo);

			List<Object> revisions = conn.getRevisions(project.getName());
			if (!revisions.isEmpty()) {
				if (revisions.get(0) instanceof Revision) {
					// Approach 1: if the revision object is Revision, add it into the repoBuilder
					for (final Object rev : revisions) {
						final Revision.Builder revBuilder = Revision.newBuilder((Revision) rev);
						repoBuilder.addRevisions(revBuilder);
					}
				} else {
					// Approach 2: else save it as a key pointing to the Revision instance in the
					// commit sequence file
					for (final Object rev : revisions)
						repoBuilder.addRevisionKeys((Long) rev);
				}
			}

			// head commit indicates the latest commit which may not be in the default
			// branch
			repoBuilder.setHead(conn.getHeadCommitOffset());
			repoBuilder.addAllHeadSnapshot(conn.buildHeadSnapshot());
			repoBuilder.addAllBranches(conn.getBranchIndices());
			repoBuilder.addAllBranchNames(conn.getBranchNames());
			repoBuilder.addAllTags(conn.getTagIndices());
			repoBuilder.addAllTagNames(conn.getTagNames());
			projBuilder.setCodeRepositories(i, repoBuilder);

			// return the completely builded project
			return projBuilder.build();

		} catch (final Throwable e) {
			System.err.println("unknown error " + project.getName());
			e.printStackTrace();
		} finally {
			if (conn != null) {
				astWriterLen = conn.getAstWriterLen();
				commitWriterLen = conn.getCommitWriterLen();
				contentWriterLen = conn.getContentWriterLen();
				try {
					conn.close();
				} catch (Exception e) {
					System.err.println("Cannot close Git connector to " + gitDir.getAbsolutePath());
					e.printStackTrace();
				}
			}
		}

		// return null to skip error project
		return null;
	}

	private synchronized static boolean isFiltered(Project project) {
		if (project.getForked())
			return true;
//		if (project.getStars() < 2 && project.getSize() < 100)
//			return true;
		if (project.getProgrammingLanguagesList().contains("Java")
				|| project.getProgrammingLanguagesList().contains("JavaScript")
				|| project.getProgrammingLanguagesList().contains("PHP"))
			return false;
		String lang = project.getMainLanguage();
		if (lang != null && (lang.equals("Java") || lang.equals("JavaScript") || lang.equals("PHP")))
			return false;
		return true;
	}

	public static String[] openWriters(String projectName) {
		suffix = projectName + ".seq";
		while (true) {
			try {
				System.out.println(suffix + " starts!");

				String projectWriterPath = OUTPUT_PATH + "/project/" + suffix;

				// if the project is already processed return null
				if (new File(projectWriterPath).exists())
					return null;

				projectWriter = SequenceFile.createWriter(fileSystem, conf, new Path(projectWriterPath), Text.class,
						BytesWritable.class, CompressionType.BLOCK);

				String astWriterPath = OUTPUT_PATH + "/ast/" + suffix;
				astWriter = SequenceFile.createWriter(fileSystem, conf, new Path(astWriterPath), LongWritable.class,
						BytesWritable.class, CompressionType.BLOCK);

				String commitWriterPath = OUTPUT_PATH + "/commit/" + suffix;
				commitWriter = SequenceFile.createWriter(fileSystem, conf, new Path(commitWriterPath),
						LongWritable.class, BytesWritable.class, CompressionType.BLOCK);

				String contentWriterPath = OUTPUT_PATH + "/source/" + suffix;
				contentWriter = SequenceFile.createWriter(fileSystem, conf, new Path(contentWriterPath),
						LongWritable.class, BytesWritable.class, CompressionType.BLOCK);

				astWriterLen = 1;
				commitWriterLen = 1;
				contentWriterLen = 1;

				return new String[] { projectWriterPath, astWriterPath, commitWriterPath, contentWriterPath };
			} catch (Throwable t) {
				t.printStackTrace();
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
				System.out.println(suffix + " done!!!");
				return;
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
	}

	private static List<String> getJsonFilePaths() {
		List<String> jsonFilePaths = new ArrayList<String>();
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(JSON_FILES_PATH));
			String line = reader.readLine();
			while (line != null) {
				jsonFilePaths.add(line);
				line = reader.readLine();
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return jsonFilePaths;
	}
}
