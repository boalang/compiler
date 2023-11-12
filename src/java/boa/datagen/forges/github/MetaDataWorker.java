/*
 * Copyright 2015-2021, Hridesh Rajan, Robert Dyer, Hoan Nguyen
 *                 Iowa State University of Science and Technology
 *                 and University of Nebraska Board of Regents
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
package boa.datagen.forges.github;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.SequenceFile.CompressionType;
import org.apache.hadoop.io.Text;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.protobuf.InvalidProtocolBufferException;

import boa.datagen.DefaultProperties;
import boa.datagen.scm.AbstractConnector;
import boa.datagen.scm.GitConnector;
import boa.datagen.util.FileIO;
import boa.datagen.util.Properties;
import boa.types.Code.CodeRepository;
import boa.types.Code.Revision;
import boa.types.Toplevel.Project;

public class MetaDataWorker implements Runnable {
	private boolean available = true;
	private TokenList tokens;
	private JsonArray repos;
	private final String language_url_header = "https://api.github.com/repos/";
	private final String language_url_footer = "/languages";
	private final String repo_url_header = "https://api.github.com/repos/";
	private HashMap<String, byte[]> projects = new HashMap<String, byte[]>();
	private SequenceFile.Writer projectWriter;
	private SequenceFile.Writer astWriter;
	private SequenceFile.Writer commitWriter;
	private SequenceFile.Writer contentWriter;
	private final static boolean debug = Properties.getBoolean("debug", DefaultProperties.DEBUG);
	private final static boolean cache = Properties.getBoolean("cache", DefaultProperties.CACHE);
	private final static File gitRootPath = new File(
			Properties.getProperty("gh.svn.path", DefaultProperties.GH_GIT_PATH));
	private long commitWriterLen = 0;
	private final int id;

	private static Configuration conf = null;
	private static FileSystem fileSystem = null;
	private static String base = null;

	MetaDataWorker(TokenList tokens, int id) {
		this.tokens = tokens;
		this.id = id;
	}

	public boolean isAvailable() {
		return this.available;
	}

	public void setRepos(JsonArray repos) {
		this.repos = repos;
		
	}

	@Override
	public void run() {
		this.available = false;
		proccessMetaData();
		this.available = true;

	}

	private void proccessMetaData() {
		int size = repos.size();
		for (int i = 0; i < size; i++) {
			
			JsonObject repo = repos.get(i).getAsJsonObject();
			if (debug)
				System.out.println(id + " proccessing " + repo.get("full_name"));
			getLanguageList(repo);
			getStarsAndCreated(repo);
			storeMetadata(repo);
		}
		try {
			importRepos();
		} catch (IOException e) {
			if (debug) {
				System.err.println("IOException");
				e.printStackTrace();
			}
		}
	}

	private void importRepos() throws IOException {
		int counter = 0;
		ArrayList<byte[]> cacheOfProjects = new ArrayList<byte[]>();
		conf = new Configuration();
		fileSystem = FileSystem.get(conf);
		base = Properties.getProperty("output.path", DefaultProperties.OUTPUT);
		cacheOfProjects.clear();
		for (String key : projects.keySet()) {
			byte[] bs = projects.get(key);
			cacheOfProjects.add(bs);
		}
		openWriters();
		for (int i = 0; i < cacheOfProjects.size(); i++) {
			try {
				// int pid = numOfProcessedProjects.getAndIncrement();
				/*
				 * if (pid >= cacheOfProjects.size()) { break; }
				 */
				byte[] bs = cacheOfProjects.get(i);
				Project cachedProject = null;
				try {
					cachedProject = Project.parseFrom(bs);
				} catch (InvalidProtocolBufferException e) {
					e.printStackTrace();
					continue;
				}
				cacheOfProjects.set(i, null);
				bs = null;

				final String name = cachedProject.getName();

				if (debug)
					System.out.println("Processing " + (i + 1) + " / " + cacheOfProjects.size() + " "
							+ cachedProject.getId() + " " + name);

				Project project = storeRepository(cachedProject, 0);

				if (debug)
					System.out.println("Putting in sequence file: " + project.getId());

				// store the project metadata
				BytesWritable bw = new BytesWritable(project.toByteArray());
				if (bw.getLength() < Integer.MAX_VALUE / 3) {
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
						projectWriter.append(new Text(pb.getId()), pb.build().toByteArray());
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
		}

	//	closeWriters(); use max_projects to determine number of projects per seq file
	}
	
	/**
	 * close writers
	 */
	public void close() {
		closeWriters();
	}

	private void getStarsAndCreated(JsonObject repo) {
		Gson parser = new Gson();
		Token tok = this.tokens.getNextAuthenticToken("https://api.github.com/repositories");
		MetadataCacher mc = null;
		if (tok.getNumberOfRemainingLimit() <= 0) {
			tok = this.tokens.getNextAuthenticToken("https://api.github.com/repositories");
		}
		String name = repo.get("full_name").getAsString();
		String repourl = this.repo_url_header + name;

		if (tok.getNumberOfRemainingLimit() <= 0) {
			tok = this.tokens.getNextAuthenticToken("https://api.github.com/repositories");
		}
		for (int i = 0; i < 1; i++) {
			mc = new MetadataCacher(repourl, tok.getUserName(), tok.getToken());
			boolean authnticationResult = mc.authenticate();
			if (authnticationResult) {
				mc.getResponse();
				String pageContent = mc.getContent();
				JsonObject repository = parser.fromJson(pageContent, JsonElement.class).getAsJsonObject();
				int stars = repository.get("stargazers_count").getAsInt();
				String contrUrl = repourl + "/contributors";
				mc = new MetadataCacher(contrUrl, tok.getUserName(), tok.getToken());
				authnticationResult = mc.authenticate();
				repo.addProperty("stargazers_count", stars);
				String created = repository.get("created_at").getAsString();
				repo.addProperty("created_at", created);
				tok.setLastResponseCode(mc.getResponseCode());
				tok.setnumberOfRemainingLimit(mc.getNumberOfRemainingLimit());
				tok.setResetTime(mc.getLimitResetTime());
			} else {
				final int responsecode = mc.getResponseCode();
				System.err.println("authentication error " + responsecode + " " + name);
				mc = new MetadataCacher("https://api.github.com/repositories", tok.getUserName(), tok.getToken());
				if (mc.authenticate()) {
					tok.setnumberOfRemainingLimit(mc.getNumberOfRemainingLimit());
				} else {
					System.out.println("token: " + tok.getId() + " exhausted");
					tok.setnumberOfRemainingLimit(0);
					i--;
				}
			}
		}

	}

	private void getLanguageList(JsonObject repo) {
		Gson parser = new Gson();
		String name = repo.get("full_name").getAsString();
		String langurl = this.language_url_header + name + this.language_url_footer;
		Token tok = this.tokens.getNextAuthenticToken("https://api.github.com/repositories");
		MetadataCacher mc = null;
		if (tok.getNumberOfRemainingLimit() <= 0) {
			tok = this.tokens.getNextAuthenticToken("https://api.github.com/repositories");
		}
		for (int i = 0; i < 1; i++) {
			mc = new MetadataCacher(langurl, tok.getUserName(), tok.getToken());
			boolean authnticationResult = mc.authenticate();
			if (authnticationResult) {
				mc.getResponse();
				String pageContent = mc.getContent();
				JsonObject languages = parser.fromJson(pageContent, JsonElement.class).getAsJsonObject();
				repo.add("language_list", languages);
				tok.setLastResponseCode(mc.getResponseCode());
				tok.setnumberOfRemainingLimit(mc.getNumberOfRemainingLimit());
				tok.setResetTime(mc.getLimitResetTime());
			} else {
				final int responsecode = mc.getResponseCode();
				System.err.println("authentication error " + responsecode);
				mc = new MetadataCacher("https://api.github.com/repositories", tok.getUserName(), tok.getToken());
				if (mc.authenticate()) {
					tok.setnumberOfRemainingLimit(mc.getNumberOfRemainingLimit());
				} else {
					System.out.println("token: " + tok.getId() + " exhausted");
					tok.setnumberOfRemainingLimit(0);
					i--;
				}
			}
		}

	}

	private void storeMetadata(JsonObject repo) {
		RepoMetadata rep = new RepoMetadata(repo);
		try {
			Project protobufRepo = rep.toBoaMetaDataProtobuf();
			// System.out.println(jRepo.toString());
			projects.put(rep.id, protobufRepo.toByteArray());
		} catch (Exception e) {
			System.err.println("error proccessing project: " + rep.name);
			e.printStackTrace();
		}
	}

	private Project storeRepository(final Project project, final int i) {
		final CodeRepository repo = project.getCodeRepositories(i);
		final Project.Builder projBuilder = Project.newBuilder(project);

		final String name = project.getName();
		final File gitDir;
		if (cache) {
			final String id = project.getId().toString();
			gitDir = new File(gitRootPath + "/" + id.charAt(0) + "/" + id.charAt(1) + "/" + id);
		} else {
			gitDir = new File(gitRootPath + "/" + name);
		}

		if (project.getForked() || !(project.getProgrammingLanguagesList().contains("Java") || project.getProgrammingLanguagesList().contains("Kotlin") || project.getProgrammingLanguagesList().contains("JavaScript") || project.getProgrammingLanguagesList().contains("PHP")))
			return project;

		if (!gitDir.exists()) {
			String[] args = { repo.getUrl(), gitDir.getAbsolutePath() };
			try {
				RepositoryCloner.clone(args);
			} catch (final Throwable t) {
				System.err.println("Error cloning " + repo.getUrl());
				t.printStackTrace();
				return project;
			}
		}

		if (debug)
			System.out.println("Has repository: " + name);
		AbstractConnector conn = null;
		try {
			conn = new GitConnector(gitDir.getAbsolutePath(), project.getName());
			final CodeRepository.Builder repoBuilder = CodeRepository.newBuilder(repo);
			for (final Object rev : conn.getRevisions(project.getName())) {
				final Revision.Builder revBuilder = Revision.newBuilder((Revision) rev);
				repoBuilder.addRevisions(revBuilder);
			}
			if (repoBuilder.getRevisionsCount() > 0) {
				if (debug)
					System.out.println("Build head snapshot");
				repoBuilder.setHead(conn.getHeadCommitOffset());
				repoBuilder.addAllHeadSnapshot(conn.buildHeadSnapshot());
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
				try {
					conn.close();
				} catch (Exception e) {
					printError(e, "Cannot close Git connector to " + gitDir.getAbsolutePath(), project.getName());
				}
			}
			if (!cache)
				new Thread(new FileIO.DirectoryRemover(gitDir)).start();
		}

		return project;
	}

	public void openWriters() {
		long time = System.currentTimeMillis();
		String suffix = id + "-" + time + ".seq";
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

	public static void printError(final Throwable e, final String message, String name) {
		System.err.println("ERR: " + message + " proccessing: " + name);
		if (debug) {
			e.printStackTrace();
			// System.exit(-1);
		} else
			System.err.println(e.getMessage());
	}

}
