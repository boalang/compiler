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

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.lib.RepositoryCache;
import org.eclipse.jgit.util.FS;

import com.google.protobuf.InvalidProtocolBufferException;

import boa.datagen.forges.github.RepositoryCloner;
import boa.datagen.scm.AbstractConnector;
import boa.datagen.scm.GitConnector;
import boa.types.Code.CodeRepository;
import boa.types.Code.Revision;
import boa.types.Toplevel.Project;
import boa.datagen.util.FileIO;
import boa.datagen.util.Properties;

/**
 * @author hoan
 * @author rdyer
 * @author hridesh
 * 
 */
public class SeqRepoImporter {
	private final static boolean debug = Properties.getBoolean("debug", DefaultProperties.DEBUG);

	private final static String keyDelim = Properties.getProperty("hbase.delimiter", DefaultProperties.HBASE_DELIMITER);

	private static File jsonCacheDir = new File(Properties.getProperty("gh.json.cache.path", DefaultProperties.GH_JSON_CACHE_PATH));
	private final static File gitRootPath = new File(
			Properties.getProperty("gh.svn.path", DefaultProperties.GH_GIT_PATH));

	private final static ArrayList<byte[]> cacheOfProjects = new ArrayList<byte[]>();
	private final static HashSet<String> processedProjectIds = new HashSet<String>();

	private static Configuration conf = null;
	private static FileSystem fileSystem = null;
	private static String base = null;

	private final static int poolSize = Integer.parseInt(Properties.getProperty("num.threads", DefaultProperties.NUM_THREADS));
	private final static AtomicInteger numOfProcessedProjects = new AtomicInteger(0);

	public static void main(String[] args) throws IOException, InterruptedException {
		conf = new Configuration();
		// currently using the cachejson location as tempCache
		// conf.set("fs.default.name", "hdfs://boa-njt/");
		// conf.set("fs.default.name",
		// Properties.getProperty("gh.json.cache.path",
		// DefaultProperties.GH_JSON_CACHE_PATH));
		fileSystem = FileSystem.get(conf);
		base = Properties.getProperty("gh.json.cache.path", DefaultProperties.GH_JSON_CACHE_PATH);

		buildCacheOfProjects();
		getProcessedProjects();

		Thread [] workers = new Thread[poolSize];
		for (int i = 0; i < poolSize; i++){
			workers[i] =new Thread(new ImportTask(i));
			workers[i].start();
		}
		
		for(Thread t :workers){
			while(t.isAlive()){
				Thread.sleep(1000);
			}
		}
			
	}

	private static void getProcessedProjects() throws IOException {

		FileStatus[] files = fileSystem.listStatus(new Path(base));
		String hostname = InetAddress.getLocalHost().getHostName();
		for (int i = 0; i < files.length; i++) {
			FileStatus file = files[i];
			String prefix = "projects-" + hostname + "-";
			String name = file.getPath().getName();
			int index1 = name.indexOf(prefix);
			if (index1 > -1) {
				try {
					SequenceFile.Reader r = new SequenceFile.Reader(fileSystem, file.getPath(), conf);
					final Text key = new Text();
					while (r.next(key)) {
						processedProjectIds.add(key.toString());
					}
					r.close();
				} catch (EOFException e) {
					printError(e, "EOF Exception in " + file.getPath().getName());
					fileSystem.delete(file.getPath(), false);
				}
			}
		}
//		System.out.println("Got processed projects: " + processedProjectIds.size());
	}

	private static void buildCacheOfProjects() {
		cacheOfProjects.clear();
		for (File file : jsonCacheDir.listFiles()) {
			if (file.getName().endsWith("buf-map")) {
				@SuppressWarnings("unchecked")
				HashMap<String, byte[]> repos = (HashMap<String, byte[]>) FileIO
						.readObjectFromFile(file.getAbsolutePath());
				for (String key : repos.keySet()) {
					byte[] bs = repos.get(key);
					if (poolSize > 1)
						cacheOfProjects.add(bs);
					else {
						try {
							Project p = Project.parseFrom(bs);
							if (processedProjectIds.contains(p.getId()))
								continue;
							cacheOfProjects.add(bs);
						} catch (InvalidProtocolBufferException e) {
							e.printStackTrace();
						}
					}
				}
				repos.clear();
			}
		}
//		System.out.println("Got cached projects: " + cacheOfProjects.size());
	}

	@SuppressWarnings("unused")
	private static void print(String id, Project p) {
		System.out.print(id);
		System.out.print(" " + p.getId());
		System.out.print(" " + p.getName());
		System.out.print(" " + p.getHomepageUrl());
		if (p.getProgrammingLanguagesCount() > 0) {
			System.out.print(" Programming languages:" + p.getProgrammingLanguagesCount());
			for (int i = 0; i < p.getProgrammingLanguagesCount(); i++)
				System.out.print(" " + p.getProgrammingLanguages(i));
		}
		System.out.println();
	}

	public static class ImportTask implements Runnable {
		private static final int MAX_COUNTER = 10000;
		private int id;
		private int counter = 0;
		SequenceFile.Writer projectWriter, astWriter;

		public ImportTask(int id) throws IOException {
			this.id = id;
		}

		public void openWriters() {
			long time = System.currentTimeMillis() / 1000;
			String hostname = "" + time;
			for (int i = 0; i < 3; i++) {
				try {
					hostname = InetAddress.getLocalHost().getHostName();
					break;
				} catch (UnknownHostException e) {
					e.printStackTrace();
				}
			}
//			String suffix = hostname + "-" + id + "-" + time + ".seq";
			String suffix = ".seq";
			while (true) {
				try {
					projectWriter = SequenceFile.createWriter(fileSystem, conf,
							new Path(base + "/projects" + suffix), Text.class, BytesWritable.class);
					astWriter = SequenceFile.createWriter(fileSystem, conf,
							new Path(base + "/ast" + suffix), Text.class, BytesWritable.class);
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
				try {
					int pid = numOfProcessedProjects.getAndIncrement();
					if (pid >= cacheOfProjects.size()) {
						break;
					}
					byte[] bs = cacheOfProjects.get(pid);
					Project cachedProject = null;
					try {
						cachedProject = Project.parseFrom(bs);
						if (processedProjectIds.contains(cachedProject.getId()))
							continue;
					} catch (InvalidProtocolBufferException e) {
						e.printStackTrace();
						continue;
					}
					cacheOfProjects.set(pid, null);
					bs = null;

					final String name = cachedProject.getName();

					if (debug)
						System.out.println("Processing " + pid + " / " + cacheOfProjects.size() + " "
								+ cachedProject.getId() + " " + name);

					Project project = storeRepository(cachedProject, 0);

					if (debug)
						System.out.println("Putting in sequence file: " + project.getId());

					// store the project metadata
					try {
						projectWriter.append(new Text(project.getId()), new BytesWritable(project.toByteArray()));
					} catch (IOException e) {
						e.printStackTrace();
					}
					counter++;
					if (counter >= MAX_COUNTER) {
						closeWriters();
						openWriters();
						counter = 0;
					}
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}

			closeWriters();
		}

		private Project storeRepository(final Project project, final int i) {
			final CodeRepository repo = project.getCodeRepositories(i);
			final Project.Builder projBuilder = Project.newBuilder(project);

			final String name = project.getName();
			File gitDir = null;
			if(BoaGenerator.localCloning){
				String path = gitRootPath.getAbsolutePath();
				gitDir = new File(path);
			}else	
				gitDir = new File(gitRootPath + "/" + name);

			// make sure the given directory exists else create a new one
			if (!gitDir.exists()) {
				gitDir.mkdirs();
			}

			if (!RepositoryCache.FileKey.isGitRepository(gitDir, FS.DETECTED)) {
				String[] args = { repo.getUrl(), gitRootPath + "/" + name };
				try {
					RepositoryCloner.clone(args);
				} catch (InvalidRemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (TransportException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (GitAPIException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}catch (org.eclipse.jgit.api.errors.JGitInternalException e) {
					//e.printStackTrace();
				}

			}

			if (debug)
				System.out.println("Has repository: " + name);
			AbstractConnector conn = null;
			try {
				conn = new GitConnector(gitDir.getAbsolutePath());
				final CodeRepository.Builder repoBuilder = CodeRepository.newBuilder(repo);
				final String repoKey = "g:" + project.getId() + keyDelim + repo.getKind().getNumber();
				for (final Revision rev : conn.getCommits(true, astWriter, repoKey, keyDelim)) {
					
					  if (debug) System.out.println("Storing '" + name + "' revision: " + rev.getId());
					// build new rev w/ no namespaces
					final Revision.Builder revBuilder = Revision.newBuilder(rev);
					repoBuilder.addRevisions(revBuilder);
				}

				projBuilder.setCodeRepositories(i, repoBuilder);
				return projBuilder.build();
			} catch (final Exception e) {
				printError(e, "unknown error");
			} finally {
				if (conn != null) {
					try {
						conn.close();
					} catch (Exception e) {
						printError(e, "Cannot close Git connector to " + gitDir.getAbsolutePath());
					}
				}
			}

			return project;
		}
	}

	private static void printError(final Throwable e, final String message) {
		System.err.println("ERR: " + message);
		if (debug) {
			e.printStackTrace();
			// System.exit(-1);
		} else
			System.err.println(e.getMessage());
	}
}
