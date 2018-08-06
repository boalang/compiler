package boa.datagen.forges.github;

import java.io.File;
import java.io.FileNotFoundException;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import boa.datagen.util.FileIO;
import gnu.trove.set.hash.THashSet;

public class GitHubRepoDownloader {

	public final String inPutDir;
	public final String outPutDir;
	public final String tokenFile;
	public final static int MAX_NUM_THREADS = 5;
	public static THashSet<Integer> ids = new THashSet<Integer>();
	public static boolean done = false;

	public GitHubRepoDownloader(String input, String output, String tokenFile) {
		this.inPutDir = input;
		this.outPutDir = output;
		this.tokenFile = tokenFile;
		File outputDir = new File(output);
		if (!outputDir.exists()) {
			outputDir.mkdirs();
		} else {
			addNames(output);
		}
	}

	public static void main(String[] args) throws FileNotFoundException {
		if (args.length < 3) {
			throw new IllegalArgumentException();
		}
		GitHubRepoDownloader master = new GitHubRepoDownloader(args[0], args[1], args[2]);
		System.out.println(master.inPutDir);
		master.orchastrate(new File(args[0]).listFiles().length);
	}

	public void orchastrate(int totalFiles) {
		int i;
		TokenList tokens = new TokenList(this.tokenFile);
		GitHubRepoDownloaderWorker workers[] = new GitHubRepoDownloaderWorker[MAX_NUM_THREADS];
		Thread[] threads = new Thread[MAX_NUM_THREADS];
		for (i = 0; i < MAX_NUM_THREADS; i++) {
			GitHubRepoDownloaderWorker worker = new GitHubRepoDownloaderWorker(this.inPutDir, this.outPutDir, tokens);
			workers[i] = worker;
			threads[i] = new Thread(workers[i]);
			threads[i].start();
		}
		
		File[] files = new File(this.inPutDir).listFiles();
		for (i = 0; i < files.length; i++) {
			boolean assigned = false;
			while (!assigned) {
				for (int j = 0; j < MAX_NUM_THREADS; j++) {
					if (workers[j].isAvailable()) {
						System.out.println("Assigning " + i +" to thread " + j);
						workers[j].setFile(files[i]);
						workers[j].availableFalse();
						assigned = true;
						break;
					}
				}
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		done = true;
		
		for (Thread th : threads)
			while (th.isAlive())
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
	}

	private void addNames(String filePath) {
		System.out.println("adding " + filePath + " to names");
		File dir = new File(filePath);
		File[] files = dir.listFiles();
		String content;
		Gson parser = new Gson();
		JsonArray repos;
		JsonObject repo;
		for (int i = 0; i < files.length; i++) {
			if (!(files[i].toString().contains(".json"))) {
				System.out.println("skipping " + files[i].toString());
				continue;
			}
			System.out.println("proccessing page " + files[i].getName());
			content = FileIO.readFileContents(files[i]);
			repos = parser.fromJson(content, JsonElement.class).getAsJsonArray();
			for (JsonElement repoE : repos) {
				repo = repoE.getAsJsonObject();
				ids.add(repo.get("id").getAsInt());
			}
		}
	}
}
