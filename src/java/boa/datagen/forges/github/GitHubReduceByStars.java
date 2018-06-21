package boa.datagen.forges.github;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import boa.datagen.util.FileIO;
import gnu.trove.set.hash.THashSet;

public class GitHubReduceByStars {

	public final String inPutDir;
	public final String outPutDir;
	public final String tokenFile;
	public final static int MAX_NUM_THREADS = 5;
	public static THashSet<Integer> ids = new THashSet<Integer>();

	public GitHubReduceByStars(String input, String output, String tokenFile) {
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
		GitHubReduceByStars master = new GitHubReduceByStars(args[0], args[1], args[2]);
		System.out.println(master.inPutDir);
		master.orchastrate(new File(args[0]).listFiles().length);
	}

	public void orchastrate(int totalFiles) {
		int i;
		TokenList tokens = new TokenList(this.tokenFile);
		ReduceByStarWorker workers[] = new ReduceByStarWorker[MAX_NUM_THREADS];
		for (i = 0; i < MAX_NUM_THREADS; i++) {
			ReduceByStarWorker worker = new ReduceByStarWorker(this.inPutDir, this.outPutDir, tokens);
			workers[i] = worker;
		}
		File[] files = new File(this.inPutDir).listFiles();
		for (i = 0; i < files.length; i++) {
			boolean assigned = false;
			while (!assigned) {
				for (int j = 0; j < MAX_NUM_THREADS; j++) {
					if (workers[j].isAvailable()) {
						System.out.println("Assigning " + i +" to thread " + j);
						workers[j].setIndex(i);
						new Thread(workers[j]).start();
						assigned = true;
						try {
							Thread.sleep(10);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						break;
					}
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		
		for (i = 0; i< MAX_NUM_THREADS; i++) {
			workers[i].closeWorker();
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
