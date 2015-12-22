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
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;

import com.google.protobuf.InvalidProtocolBufferException;

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
	private final static boolean debug = Properties.getBoolean("debug", boa.datagen.DefaultProperties.DEBUG);

	private final static String keyDelim = Properties.getProperty("hbase.delimiter", boa.datagen.DefaultProperties.HBASE_DELIMITER);
	
	private final static File jsonCacheDir = new File(Properties.getProperty("gh.json.cache.path", boa.datagen.DefaultProperties.GH_JSON_CACHE_PATH));
	private final static File gitRootPath = new File(Properties.getProperty("gh.svn.path", boa.datagen.DefaultProperties.GH_GIT_PATH));
	
	private static final HashMap<String, String[]> repoInfo = new HashMap<String, String[]>();
	
	private final static ArrayList<byte[]> cacheOfProjects = new ArrayList<byte[]>();
	private final static HashSet<String> processedProjectIds = new HashSet<String>();
	
	private static Configuration conf = null;
	private static FileSystem fileSystem = null;
	private static String base = null;
	
	private final static int poolSize = Integer.parseInt(Properties.getProperty("num.threads", boa.datagen.DefaultProperties.NUM_THREADS));
	private final static AtomicInteger numOfProcessedProjects = new AtomicInteger(0), listId = new AtomicInteger(0);
	private final static int maxListId = 16;

	public static void main(String[] args) throws IOException, InterruptedException {
		conf = new Configuration();
		conf.set("fs.default.name", "hdfs://boa-njt/");
		fileSystem = FileSystem.get(conf);
		base = conf.get("fs.default.name", "");
		
		getProcessedProjects();
		getRepoInfo();
		
		for (int i = 0; i < poolSize; i++) 
			new Thread(new ImportTask(i)).start();
	}

	private static void getProcessedProjects() throws IOException {
		FileStatus[] files = fileSystem.listStatus(new Path(base + "tmprepcache/2015-08"));
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
		System.out.println("Got processed projects: " + processedProjectIds.size());
	}

	private static void buildCacheOfProjects(int listId) {
		cacheOfProjects.clear();
		for (File file : jsonCacheDir.listFiles()) {
			if (file.getName().endsWith("-" + listId + "-buf-map")) {
				@SuppressWarnings("unchecked")
				HashMap<String, byte[]> repos = (HashMap<String, byte[]>) FileIO.readObjectFromFile(file.getAbsolutePath());
				for (String key : repos.keySet()) {
					byte[] bs = repos.get(key);
					if (poolSize > 1)
						cacheOfProjects.add(bs);
					else {
						try {
							Project p = Project.parseFrom(bs);
							if (processedProjectIds.contains(p.getId())) continue;
							String name = p.getName();
							String[] info = repoInfo.get(name);
							if (info != null && exists(name, info[1]) != null)
								cacheOfProjects.add(bs);
						} catch (InvalidProtocolBufferException e) {
							e.printStackTrace();
						}
					}
				}
				repos.clear();
			}
		}
		System.out.println("Got cached projects: " + cacheOfProjects.size());
	}

	private static void getRepoInfo() {
		String content = FileIO.readFileContents(new File("repos-Java-org-commits.csv"));
		Scanner sc = new Scanner(content);
		while (sc.hasNextLine()) {
			String[] parts = sc.nextLine().split(",");
			repoInfo.put(parts[0], new String[]{parts[1], parts[3]});
		}
		sc.close();
	}
	
	private static File exists(String name, String listId) {
		for (int i = 2; i <= 4; i++) {
			File dir = new File("/hadoop" + i + "/" + gitRootPath + "/" + listId + "/" + name);
			if (dir.exists())
				return dir;
		}
		return null;
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
			String suffix = hostname + "-" + id + "-" + time + ".seq";
			while (true) {
				try {
					projectWriter = SequenceFile.createWriter(fileSystem, conf, new Path(base + "tmprepcache/2015-08/projects-" + suffix), Text.class, BytesWritable.class);
					astWriter = SequenceFile.createWriter(fileSystem, conf, new Path(base + "tmprepcache/2015-08/ast-" + suffix), Text.class, BytesWritable.class);
					break;
				} catch (Throwable t) {
					t.printStackTrace();
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {}
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
					} catch (InterruptedException e) {}
					break;
				} catch (Throwable t) {
					t.printStackTrace();
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {}
				}
			}
		}

		@Override
		public void run() {
			openWriters();
			while (true) {
				try {
					if (numOfProcessedProjects.get() == -1) break;
					int pid = numOfProcessedProjects.getAndIncrement();
					if (pid == cacheOfProjects.size()) {
						if (listId.get() > maxListId) {
							numOfProcessedProjects.set(-1);
							break;
						}
						buildCacheOfProjects(listId.getAndIncrement());
						numOfProcessedProjects.set(0);
						continue;
					}
					else if (pid > cacheOfProjects.size()) {
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
						}
						continue;
					}
					byte[] bs = cacheOfProjects.get(pid);
					Project cachedProject = null;
					try {
						cachedProject = Project.parseFrom(bs);
						if (processedProjectIds.contains(cachedProject.getId())) continue;
					} catch (InvalidProtocolBufferException e) {
						e.printStackTrace();
						continue;
					}
					cacheOfProjects.set(pid, null);
					bs = null;

					final String name = cachedProject.getName();

					if (debug)
						System.out.println("Processing list " + (listId.get()-1) + ": " + pid + " / " + cacheOfProjects.size()  + " " + cachedProject.getId() + " " + name);

					String[] info = repoInfo.get(name);
					Project project = cachedProject;
					if (info != null && exists(name, info[1]) != null)
						project = storeRepository(cachedProject, 0);

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
			final File gitDir = exists(name, repoInfo.get(name)[1]);

			if (debug)
				System.out.println("Has repository: " + name);

			final AbstractConnector conn = new GitConnector(gitDir.getAbsolutePath());
			try {
				final CodeRepository.Builder repoBuilder = CodeRepository.newBuilder(repo);
				final String repoKey = "g:" + project.getId() + keyDelim + repo.getKind().getNumber();
				for (final Revision rev : conn.getCommits(true, astWriter, repoKey, keyDelim)) {
					/*if (debug)
						System.out.println("Storing '" + name + "' revision: " + rev.getId());*/

					// build new rev w/ no namespaces
					final Revision.Builder revBuilder = Revision.newBuilder(rev);
					repoBuilder.addRevisions(revBuilder);
				}

				projBuilder.setCodeRepositories(i, repoBuilder);
				return projBuilder.build();
			} catch (final Exception e) {
				printError(e, "unknown error");
			} finally {
			    try {
				if(conn != null) conn.close();
			    } catch (Exception e) {
				printError(e, "error closing GitConnector");
			    }
			}

			return project;
		}
	}

	private static void printError(final Throwable e, final String message) {
		System.err.println("ERR: " + message);
		if (debug) {
			e.printStackTrace();
			//System.exit(-1);
		}
		else
			System.err.println(e.getMessage());
	}
}
