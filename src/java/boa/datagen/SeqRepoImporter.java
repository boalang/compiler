/*
 * Copyright 2015, Hridesh Rajan, Robert Dyer, Hoan Nguyen
 *                 and Iowa State University of Science and Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package boa.datagen;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
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
import boa.datagen.forges.github.RepoMetadata;
import boa.datagen.forges.github.RepositoryCloner;
import boa.datagen.scm.AbstractConnector;
import boa.datagen.scm.GitConnector;
import boa.datagen.util.FileIO;
import boa.datagen.util.Properties;
import boa.types.Code.CodeRepository;
import boa.types.Code.Revision;
import boa.types.Toplevel.Project;

public class SeqRepoImporter {
	private final static boolean debug = Properties.getBoolean("debug", DefaultProperties.DEBUG);
	private final static boolean cache = Properties.getBoolean("cache", DefaultProperties.CACHE);

	private final static File gitRootPath = new File(Properties.getProperty("gh.svn.path", DefaultProperties.GH_GIT_PATH));
	final static String jsonPath = Properties.getProperty("gh.json.path", DefaultProperties.GH_JSON_PATH);
	final static String jsonCachePath = Properties.getProperty("output.path", DefaultProperties.OUTPUT);
	
	private final static HashSet<String> processedProjectIds = new HashSet<String>();

	private static Configuration conf = null;
	private static FileSystem fileSystem = null;
	private static String base = null;
	private static boolean done = false;

	private final static int POOL_SIZE = Integer.parseInt(Properties.getProperty("num.threads", DefaultProperties.NUM_THREADS));
	private final static int MAX_SIZE_FOR_PROJECT_WITH_COMMITS = Integer.valueOf(DefaultProperties.MAX_SIZE_FOR_PROJECT_WITH_COMMITS);
	private final static boolean STORE_COMMITS = DefaultProperties.STORE_COMMITS;
	
	public static void main(String[] args) throws IOException, InterruptedException {

		conf = new Configuration();
		fileSystem = FileSystem.get(conf);
		base = Properties.getProperty("output.path", DefaultProperties.OUTPUT);

		getProcessedProjects();

		// assign each thread with a worker
		ImportTask[] workers = new ImportTask[POOL_SIZE];
		Thread[] threads = new Thread[POOL_SIZE];
		for (int i = 0; i < POOL_SIZE; i++) {
			workers[i] = new ImportTask(i);
			threads[i] = new Thread(workers[i]);
			threads[i].start();
			Thread.sleep(10);
		}
		
		int counter = 0;
		File dir = new File(jsonPath);
		for (File file : dir.listFiles()) {
			if (file.getName().endsWith(".json")) {
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
				for (int i = 0; i < repoArray.size(); i++) {
					try {
						JsonObject rp = repoArray.get(i).getAsJsonObject();
						RepoMetadata repo = new RepoMetadata(rp);
						if (repo.id != null && repo.name != null && !processedProjectIds.contains(repo.id)) {
							Project project = repo.toBoaMetaDataProtobuf(); // current project instance only contains metadata 

							// System.out.println(jRepo.toString());
							boolean assigned = false;
							while (!assigned) {
								for (int j = 0; j < POOL_SIZE; j++) {
									if (workers[j].isReady()) {
										workers[j].setProject(project);
										workers[j].setReady(false);
										assigned = true;
										break;
									}
								}
//								Thread.sleep(100);
							}
							System.out.println("Assigned the " + (++counter) + "th project: " + repo.name + " with id: " + repo.id  
									+ " from the " + i + "th object of the json file: " + file.getPath());
						}
					} catch (Exception e) {
						System.err.println("Error proccessing item " + i + " of page " + file.getPath());
						e.printStackTrace();
					}
				}
			}
		}
		for (int j = 0; j < POOL_SIZE; j++) {
			while (!workers[j].isReady())
				Thread.sleep(100);
		}
		setDone(true);
		
		// wait for workers to close writers and finish
		for (Thread thread : threads)
			while (thread.isAlive())
				Thread.sleep(1000);
	}

	synchronized static boolean getDone() {
		return SeqRepoImporter.done;
	}

	synchronized static void setDone(boolean done) {
		SeqRepoImporter.done = done;
	}

	private static void getProcessedProjects() throws IOException {
		FileStatus[] files = fileSystem.listStatus(new Path(base + "/project"));
		for (int i = 0; i < files.length; i++) {
			FileStatus file = files[i];
			String name = file.getPath().getName();
			if (name.endsWith(".seq")) {
				SequenceFile.Reader r = null;
				try {
					// the project sequence file contain multiple projects
					r = new SequenceFile.Reader(fileSystem, file.getPath(), conf);
					final Text key = new Text();
					HashSet<String> projectIds = new HashSet<>();
					while (r.next(key))
						projectIds.add(key.toString());
					// update processed project set if only if all projects in the sequence file are not corrupted 
					processedProjectIds.addAll(projectIds); 
					r.close();
				} catch (IOException e) {
					if (r != null)
						r.close();
					// if one project is corrupted, then delete the project sequence file and its related sequence files
					for (String dir : new String[] { "project", "ast", "commit", "source" }) {
						fileSystem.delete(new Path(base + "/" + dir + "/" + name), false);
						System.out.println("remove " + base + "/" + dir + "/" + name);
					}
				}
			}
		}
		// processedProjects = processedProjectIds.size();
		System.out.println("Got processed projects: " + processedProjectIds.size());
	}

	public static class ImportTask implements Runnable {
		private int id;
		private int counter = 0, allCounter = 0;
		private String suffix;
		private SequenceFile.Writer projectWriter, astWriter, commitWriter, contentWriter;
		private long astWriterLen = 1, commitWriterLen = 1, contentWriterLen = 1;
		private volatile boolean ready = true;
		private volatile Project project;

		public ImportTask(int id) {
			setId(id);
		}

		public synchronized void openWriters() {
			long time = System.currentTimeMillis();
			suffix = getId() + "-" + time + ".seq";
			while (true) {
				try {
					System.out.println(Thread.currentThread().getName() + " " + getId() + " " + suffix + " starts!");
					projectWriter = SequenceFile.createWriter(fileSystem, conf, new Path(base + "/project/" + suffix),
							Text.class, BytesWritable.class, CompressionType.BLOCK);
					astWriter = SequenceFile.createWriter(fileSystem, conf, new Path(base + "/ast/" + suffix),
							LongWritable.class, BytesWritable.class, CompressionType.BLOCK);
					commitWriter = SequenceFile.createWriter(fileSystem, conf, new Path(base + "/commit/" + suffix),
							LongWritable.class, BytesWritable.class, CompressionType.BLOCK);
					contentWriter = SequenceFile.createWriter(fileSystem, conf, new Path(base + "/source/" + suffix),
							LongWritable.class, BytesWritable.class, CompressionType.BLOCK);
					astWriterLen = 1;
					commitWriterLen = 1;
					contentWriterLen = 1;
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

		public synchronized void closeWriters() {
			while (true) {
				try {
					projectWriter.close();
					astWriter.close();
					commitWriter.close();
					contentWriter.close();
					System.out.println(Thread.currentThread().getName() + " " + getId() + " " + suffix + " done!!!");
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

		@Override
		public void run() {
			openWriters();
			while (true) {
				
				while (isReady()) {
					if (getDone())
						break;
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				
				if (getDone())
					break;
				
				storeProject : try {
					final String name = project.getName();

					if (debug)
						System.out.println(Thread.currentThread().getName() + " id: " + Thread.currentThread().getId()
								+ " is processing the " + (allCounter + 1) + "th project: " + name + " with id: "+ project.getId());
					project = storeRepository(project, 0);
					
					if (project == null) // if the project is null then skip this process
						break storeProject;
					
					if (debug)
						System.out.println(Thread.currentThread().getName() + " id: " + Thread.currentThread().getId() 
								+ " is putting project " + project.getName() + " in sequence file");

					// store project into sequence file
					BytesWritable bw = new BytesWritable(project.toByteArray());
					if (bw.getLength() <= MAX_SIZE_FOR_PROJECT_WITH_COMMITS 
							|| (project.getCodeRepositoriesCount() > 0 && project.getCodeRepositories(0).getRevisionKeysCount() > 0)) {
						// Approach 1: if the Project size is acceptable, then directly append the Project instance into the sequence file
						try {
							projectWriter.append(new Text(project.getId()), bw);
						} catch (IOException e) {
							e.printStackTrace();
						}
					} else {
						// Approach 2: if the size is too large, extract Commit instances and append them into commit sequence file.
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
						try {
							projectWriter.append(new Text(pb.getId()), new BytesWritable(pb.build().toByteArray()));
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					counter++;
					allCounter++;
					if (counter >= Integer.parseInt(DefaultProperties.MAX_PROJECTS)) {
						closeWriters();
						openWriters();
						counter = 0;
					}
				} catch (Throwable e) {
					e.printStackTrace();
				}
				setReady(true);
			}
			closeWriters();
		}

		private synchronized Project storeRepository(final Project project, final int i) {
			final CodeRepository repo = project.getCodeRepositories(i);   // this is an empty code repo
			final Project.Builder projBuilder = Project.newBuilder(project);

			final String name = project.getName();
			File gitDir = new File(gitRootPath + "/" + name);
			
			if (isFiltered(project))
				return null; // return null to skip empty project

			// If repository is already cloned delete then re-clone, this should only happen during recover
			FileIO.DirectoryRemover filecheck = new FileIO.DirectoryRemover(gitRootPath + "/" + project.getName());
			filecheck.run();

			// clone repository
			String[] args = { repo.getUrl(), gitDir.getAbsolutePath() };
			try {
				RepositoryCloner.clone(args);
			} catch (Throwable t) {
				System.err.println("Error cloning " + repo.getUrl());
				t.printStackTrace();
				return null; // return null to skip empty project
			}

			if (debug)
				System.out.println(Thread.currentThread().getName() + " id: " + Thread.currentThread().getId() + " cloned repository: " + name);

			AbstractConnector conn = null;
			try {
				conn = new GitConnector(gitDir.getAbsolutePath(), project.getName(), astWriter, astWriterLen, commitWriter, commitWriterLen,
						contentWriter, contentWriterLen);
				final CodeRepository.Builder repoBuilder = CodeRepository.newBuilder(repo);
				if (STORE_COMMITS) {
					List<Object> revisions = conn.getRevisions(project.getName());
					if (!revisions.isEmpty()) {
						if (revisions.get(0) instanceof Revision) { // Approach 1: if the revision object is Revision, add it into the repoBuilder
							for (final Object rev : revisions) {
								final Revision.Builder revBuilder = Revision.newBuilder((Revision) rev);
								repoBuilder.addRevisions(revBuilder);
							}
						} else { // Approach 2: else save it as a key pointing to the Revision instance in the commit sequence file
							for (final Object rev : revisions)
								repoBuilder.addRevisionKeys((Long) rev);
						}
					}
				}

				if (debug)
					System.out.println(Thread.currentThread().getName() + " id: " + Thread.currentThread().getId() + " is building head snapshot for " + name);

				repoBuilder.setHead(conn.getHeadCommitOffset()); // head commit indicates the latest commit which may not be in the default branch
				repoBuilder.addAllHeadSnapshot(conn.buildHeadSnapshot());
				repoBuilder.addAllBranches(conn.getBranchIndices());
				repoBuilder.addAllBranchNames(conn.getBranchNames());
				repoBuilder.addAllTags(conn.getTagIndices());
				repoBuilder.addAllTagNames(conn.getTagNames());

				projBuilder.setCodeRepositories(i, repoBuilder);

				return projBuilder.build(); // return the completely builded project

			} catch (final Throwable e) {
				printError(e, "unknown error", project.getName());
			} finally {
				if (conn != null) {
					this.astWriterLen = conn.getAstWriterLen();
					this.commitWriterLen = conn.getCommitWriterLen();
					this.contentWriterLen = conn.getContentWriterLen();
					try {
						conn.close();
					} catch (Exception e) {
						printError(e, "Cannot close Git connector to " + gitDir.getAbsolutePath(), project.getName());
					}
				}
				if (!cache) {
					new Thread(new FileIO.DirectoryRemover(gitRootPath + "/" + name)).start();
				}
			}

			return null; // return null to skip error project
		}

		private synchronized boolean isFiltered(Project project) {
			if (project.getForked())
				return true;
//			if (project.getStars() < 2 && project.getSize() < 100)
//				return true;
			if (project.getProgrammingLanguagesList().contains("Java")
					|| project.getProgrammingLanguagesList().contains("JavaScript")
					|| project.getProgrammingLanguagesList().contains("Python")
					|| project.getProgrammingLanguagesList().contains("Jupyter Notebook")
					|| project.getProgrammingLanguagesList().contains("PHP"))
				return false;
			String lang = project.getMainLanguage();
			if (lang != null
					&& (lang.equals("Java")
						|| lang.equals("JavaScript")
						|| lang.equals("Python")
						|| lang.equals("Jupyter Notebook")
						|| lang.equals("PHP")))
				return false;
			return true;
		}

		public synchronized Project getProject() {
			return this.project;
		}
		
		public synchronized void setProject(Project project) {
			this.project = project;
		};

		public synchronized boolean isReady() {
			return this.ready;
		}

		public synchronized void setReady(boolean ready) {
			this.ready = ready;
		}

		public synchronized int getId() {
			return this.id;
		}

		public synchronized void setId(int id) {
			this.id = id;
		}
	}

	public synchronized static void printError(final Throwable e, final String message, String name) {
		System.err.println("ERR: " + message + " proccessing: " + name);
		if (debug) {
			e.printStackTrace();
			// System.exit(-1);
		} else
			System.err.println(e.getMessage());
	}
	

}
