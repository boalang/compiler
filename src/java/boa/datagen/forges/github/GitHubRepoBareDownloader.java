package boa.datagen.forges.github;

import java.io.File;
import java.util.List;

import static boa.datagen.forges.github.GitHubRepoJsonUpdater.getUrls;

public class GitHubRepoBareDownloader {
	
	private static String INPUT_PATH;
	private static String OUTPUT_REPOS_PATH;
	private static int THREAD_NUM;
	private static boolean done = false;
	

	public static void main(String[] args) {
//		args = new String[] { "/Users/hyj/Desktop/updated_json", 
//				"/Users/hyj/Desktop/repos",
//				"2" };
		if (args.length < 3) {
			System.out.println("args: INPUT_NAMES_PATH, OUTPUT_REPOS_PATH, THREAD_NUM");
		} else {
			INPUT_PATH = args[0];
			OUTPUT_REPOS_PATH = args[1];
			THREAD_NUM = Integer.parseInt(args[2]);
//			String input = FileIO.readFileContents(new File(INPUT_NAMES_PATH));
//			String[] projectNames = input.split("\\r?\\n");
			File input = new File(INPUT_PATH);
			List<String> urls = getUrls(input);
			
			DownloadWorker[] workers = new DownloadWorker[THREAD_NUM];
			Thread[] threads = new Thread[THREAD_NUM];
			for (int i = 0; i < THREAD_NUM; i++) {
				workers[i] = new DownloadWorker(i);
				threads[i] = new Thread(workers[i]);
				threads[i].start();
			}
			
			// assign tasks to workers
			for (String url : urls) {
				boolean assigned = false;
				while (!assigned) {
					for (int j = 0; j < THREAD_NUM; j++) {
						if (workers[j].isReady()) {
							workers[j].setName(url.replace("https://github.com/",""));
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
				
				String projectName = getName();
				String url = "https://github.com/" + projectName + ".git";
				File gitDir = new File(OUTPUT_REPOS_PATH + "/" + projectName + "/.git");
				String[] args = { url, gitDir.getAbsolutePath() };
				try {
					System.err.println("worker " + getId() + " is cloning " + projectName);
					RepositoryCloner.clone(args);
					System.err.println("worker " + getId() + " finished cloning " + projectName);
				} catch (Throwable t) {
					System.err.println(t);
					System.err.println("Error cloning " + url);
					setReady(true);
				}
				
				setReady(true);
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
