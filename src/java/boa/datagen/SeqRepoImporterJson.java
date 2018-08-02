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
import com.google.protobuf.InvalidProtocolBufferException;

import boa.datagen.forges.github.RepoMetadata;
import boa.datagen.forges.github.RepositoryCloner;
import boa.datagen.scm.AbstractConnector;
import boa.datagen.scm.GitConnector;
import boa.datagen.util.FileIO;
import boa.datagen.util.Properties;
import boa.types.Code.CodeRepository;
import boa.types.Code.Revision;
import boa.types.Toplevel.Project;

public class SeqRepoImporterJson {
	private final static boolean debug = Properties.getBoolean("debug", DefaultProperties.DEBUG);
	private final static boolean cache = Properties.getBoolean("cache", DefaultProperties.CACHE);

	private final static File gitRootPath = new File(Properties.getProperty("gh.svn.path", DefaultProperties.GH_GIT_PATH));

	private final static HashSet<String> processedProjectIds = new HashSet<String>();

	private static Configuration conf = null;
	private static FileSystem fileSystem = null;
	private static String base = null;

	private final static int poolSize = Integer.parseInt(Properties.getProperty("num.threads", DefaultProperties.NUM_THREADS));
	public static final int MAX_SIZE_FOR_PROJECT_WITH_COMMITS = Integer.valueOf(DefaultProperties.MAX_SIZE_FOR_PROJECT_WITH_COMMITS);
	final static String jsonPath = Properties.getProperty("gh.json.path", DefaultProperties.GH_JSON_PATH);
	final static String jsonCachePath = Properties.getProperty("output.path", DefaultProperties.OUTPUT);
	private static boolean done = false;
	
	public static void main(String[] args) throws IOException, InterruptedException {

		conf = new Configuration();
		fileSystem = FileSystem.get(conf);
		base = Properties.getProperty("output.path", DefaultProperties.OUTPUT);
		
		getProcessedProjects();

		ImportTask[] workers = new ImportTask[poolSize];
		for (int i = 0; i < poolSize; i++) {
			workers[i] = new ImportTask(i);
			new Thread(workers[i]).start();
			Thread.sleep(10);
		}

		File dir = new File(jsonPath);
		for (File file : dir.listFiles()) {
			if (file.getName().endsWith(".json")) {
				String content = FileIO.readFileContents(file);
				Gson parser = new Gson();

				JsonArray repoArray = null;
				try {
					repoArray = parser.fromJson(content, JsonElement.class).getAsJsonArray();
				} catch (Exception e) {
					System.err.println("error proccessing page: " + file.getPath());
					e.printStackTrace();
					continue;
				}
				for (int i = 0; i < repoArray.size(); i++) {
					JsonObject rp = repoArray.get(i).getAsJsonObject();
					RepoMetadata repo = new RepoMetadata(rp);
					if (repo.id != null && repo.name != null) {
						try {
							Project protobufRepo = repo.toBoaMetaDataProtobuf();
							// System.out.println(jRepo.toString());
							boolean assigned = false;
							while (!assigned) {
								for (int j = 0; j < poolSize; j++) {
									if (workers[j].isReady()) {
										workers[j].setProject(protobufRepo.toByteArray());
										workers[j].ready = false;
										assigned = true;
										break;
									}
								}
								Thread.sleep(10);
							}
						} catch (Exception e) {
							System.err.println("error proccessing page: " + file.getPath());
							e.printStackTrace();
						}
						System.out.println(file.getPath() + ": " + i + ": " + repo.id + " " + repo.name);
					}
				}
			}
		}
		done = true;
		Thread.sleep(100);
		// wait for workers to close writers
		for (ImportTask worker : workers) {
			while (!worker.isReady()){
				Thread.sleep(100);
			}
		}
	}

	private static void getProcessedProjects() throws IOException {
		FileStatus[] files = fileSystem.listStatus(new Path(base + "/project"));
		for (int i = 0; i < files.length; i++) {
			FileStatus file = files[i];
			String name = file.getPath().getName();
			if (name.endsWith(".seq")) {
				SequenceFile.Reader r = null;
				try {
					r = new SequenceFile.Reader(fileSystem, file.getPath(), conf);
					final Text key = new Text();
					while (r.next(key)) {
						processedProjectIds.add(key.toString());
					}
					r.close();
				} catch (IOException e) {
					if (r != null)
						r.close();
					for (String dir : new String[] { "ast", "commit", "source" })
						fileSystem.delete(new Path(base + "/" + dir + "/" + name), false);
				}
			}
		}
		// processedProjects = processedProjectIds.size();
		System.out.println("Got processed projects: " + processedProjectIds.size());
	}

	public static class ImportTask implements Runnable {
		private int id;
		private int counter = 0;
		private String suffix;
		private SequenceFile.Writer projectWriter, astWriter, commitWriter, contentWriter;
		private long astWriterLen = 0, commitWriterLen = 0, contentWriterLen = 0;
		private boolean ready = true;
		byte[] bs;

		public ImportTask(int id) {
			this.id = id;
		}

		public void setProject(byte[] project) {
			this.bs = project;
		};

		public boolean isReady() {
			return this.ready;
		}

		public void openWriters() {
			long time = System.currentTimeMillis();
			suffix = id + "-" + time + ".seq";
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

		@Override
		public void run() {
			openWriters();
			while (true) {
				while (this.ready) {
					if (done)
						break;
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				if (done)
					break;
				try {
					Project cachedProject = null;
					try {
						cachedProject = Project.parseFrom(bs);
						if (processedProjectIds.contains(cachedProject.getId())) {
							this.ready = true;
							return;
						}
					} catch (InvalidProtocolBufferException e) {
						e.printStackTrace();
						this.ready = true;
						return;
					}
					bs = null;

					final String name = cachedProject.getName();

					if (debug)
						System.out.println(
								Thread.currentThread().getId() + " Processing " + cachedProject.getId() + " " + name);

					Project project = storeRepository(cachedProject, 0);

					if (debug)
						System.out.println(
								Thread.currentThread().getId() + " Putting in sequence file: " + project.getId());

					// store the project metadata
					BytesWritable bw = new BytesWritable(project.toByteArray());
					if (bw.getLength() < MAX_SIZE_FOR_PROJECT_WITH_COMMITS) {
						try {
							projectWriter.append(new Text(project.getId()), bw);
						} catch (IOException e) {
							e.printStackTrace();
						}
					} else {
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
					if (counter >= Integer.parseInt(DefaultProperties.MAX_PROJECTS)) {
						closeWriters();
						openWriters();
						counter = 0;
					}
				} catch (Throwable e) {
					e.printStackTrace();
				}
				this.ready = true;
				if (done)
					break;
			}
			this.ready = false;
			closeWriters();
			this.ready = true;
		}

		private Project storeRepository(final Project project, final int i) {
			final CodeRepository repo = project.getCodeRepositories(i);
			final Project.Builder projBuilder = Project.newBuilder(project);

			final String name = project.getName();
			File gitDir = new File(gitRootPath + "/" + name);

			if (project.getForked() || !(project.getProgrammingLanguagesList().contains("Java")
					|| project.getProgrammingLanguagesList().contains("JavaScript")
					|| project.getProgrammingLanguagesList().contains("PHP"))
			// || project.getStars() < 2 || project.getSize() < 100
			)
				return project;

			// If repository is already cloned delete then re-clone, this should
			// only happen during recover
			FileIO.DirectoryRemover filecheck = new FileIO.DirectoryRemover(gitRootPath + "/" + project.getName());
			filecheck.run();

			String[] args = { repo.getUrl(), gitDir.getAbsolutePath() };
			try {
				RepositoryCloner.clone(args);
			} catch (Throwable t) {
				System.err.println("Error cloning " + repo.getUrl());
				t.printStackTrace();
				return project;
			}

			if (debug)
				System.out.println(Thread.currentThread().getId() + " Has repository: " + name);
			AbstractConnector conn = null;
			try {
				conn = new GitConnector(gitDir.getAbsolutePath(), project.getName(), astWriter, astWriterLen,
						contentWriter, contentWriterLen);
				final CodeRepository.Builder repoBuilder = CodeRepository.newBuilder(repo);
				for (final Revision rev : conn.getCommits(project.getName())) {
					// build new rev w/ no namespaces
					final Revision.Builder revBuilder = Revision.newBuilder(rev);
					repoBuilder.addRevisions(revBuilder);
				}
				if (repoBuilder.getRevisionsCount() > 0) {
					if (debug)
						System.out.println(Thread.currentThread().getId() + " Build head snapshot");
					repoBuilder.setHead(conn.getHeadCommitOffset());
					repoBuilder.addAllHeadSnapshot(conn.buildHeadSnapshot(new String[] {}, project.getName()));
				}
				repoBuilder.addAllBranches(conn.getBranchIndices());
				repoBuilder.addAllBranchNames(conn.getBranchNames());
				repoBuilder.addAllTags(conn.getTagIndices());
				repoBuilder.addAllTagNames(conn.getTagNames());

				projBuilder.setCodeRepositories(i, repoBuilder);
				return projBuilder.build();
			} catch (final Throwable e) {
				printError(e, "unknown error", project.getName());
			} finally {
				if (conn != null) {
					this.astWriterLen = conn.getAstWriterLen();
					this.contentWriterLen = conn.getContentWriterLen();
					try {
						conn.close();
					} catch (Exception e) {
						printError(e, "Cannot close Git connector to " + gitDir.getAbsolutePath(), project.getName());
					}
				}
				if (!cache) {
					new Thread(new FileIO.DirectoryRemover(gitRootPath + "/" + project.getName())).start();
				}
			}

			return project;
		}
	}

	public static void printError(final Throwable e, final String message, String name) {
		System.err.println("ERR: " + message + " proccessing: " + name);
		if (debug) {
			e.printStackTrace();
			// System.exit(-1);
		} else
			System.err.println(e.getMessage());
	}
}
