package boa.datagen.forges.github;

import java.io.File;
import org.eclipse.jgit.api.Git;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import boa.datagen.util.FileIO;

public class GitHubRepoBareDownloader {

	private static String INPUT_PATH; // The directory contains a list of repo json files
	private static String OUTPUT_REPOS_PATH;
	private static int THREAD_NUM;
	private static boolean done = false;

	public static void main(String[] args) {

		if (args.length < 3) {
			System.out.println("args: INPUT_NAMES_PATH, OUTPUT_REPOS_PATH, THREAD_NUM");
		} else {
			INPUT_PATH = args[0];
			OUTPUT_REPOS_PATH = args[1];
			THREAD_NUM = Integer.parseInt(args[2]);

			File input = new File(INPUT_PATH);

			DownloadWorker[] workers = new DownloadWorker[THREAD_NUM];
			Thread[] threads = new Thread[THREAD_NUM];
			for (int i = 0; i < THREAD_NUM; i++) {
				workers[i] = new DownloadWorker(i);
				threads[i] = new Thread(workers[i]);
				threads[i].start();
			}

			// assign tasks to workers
			for (File file : input.listFiles()) {
				if (!file.getName().endsWith(".json"))
					continue;
				JsonElement jsonTree = new JsonParser().parse(FileIO.readFileContents(file));
				for (JsonElement je : jsonTree.getAsJsonArray()) {
					String projectName = je.getAsJsonObject().get("html_url").getAsString()
							.replace("https://github.com/", "");

					boolean assigned = false;
					while (!assigned) {
						for (int j = 0; j < THREAD_NUM; j++) {
							if (workers[j].isReady()) {
								workers[j].setName(projectName);
								workers[j].setReady(false);
								assigned = true;
								break;
							}
						}
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}

			// wait for all done
			for (int j = 0; j < THREAD_NUM; j++) {
				while (!workers[j].isReady())
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
			}

			setDone(true);
		}

	}

	synchronized static boolean getDone() {
		return GitHubRepoBareDownloader.done;
	}

	synchronized static void setDone(boolean done) {
		GitHubRepoBareDownloader.done = done;
	}

	private static class DownloadWorker implements Runnable {
		private int id;
		private String name;
		private boolean ready = true;

		public DownloadWorker(int id) {
			setId(id);
		}

		@Override
		public void run() {
			// worker is working
			while (true) {

				// if worker is ready, then waiting for a new task
				while (isReady()) {
					// if all tasks are done, worker stop working
					if (getDone())
						break;
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

				// if all tasks are done, worker stop working
				if (getDone())
					break;

				runJob();
				setReady(true);
			}
		}

		private void runJob() {
			String projectName = getName();
			String url = "https://github.com/" + projectName + ".git";
			File gitDir = new File(OUTPUT_REPOS_PATH + "/" + projectName + "/.git").getAbsoluteFile();
			if (!gitDir.exists()) {
				Git result = null;
				try {
					System.out.println("worker " + getId() + " is cloning " + projectName);
					result = Git.cloneRepository().setURI(url).setBare(true).setDirectory(gitDir).call();
					System.out.println("worker " + getId() + " finished cloning " + projectName);
				} catch (Throwable t) {
					System.out.println(t);
					System.err.println("Error cloning " + url);
				} finally {
					if (result != null && result.getRepository() != null)
						result.getRepository().close();
				}
			} else {
				System.out.println("repo " + projectName + "already exists");
			}
		}

		synchronized boolean isReady() {
			return this.ready;
		}

		synchronized void setReady(boolean ready) {
			this.ready = ready;
		}

		synchronized void setId(int id) {
			this.id = id;
		}

		synchronized int getId() {
			return this.id;
		}

		synchronized String getName() {
			return name;
		}

		synchronized void setName(String name) {
			this.name = name;
		}
	}

}