package boa.datagen.forges.github;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import boa.datagen.util.FileIO;
import gnu.trove.set.hash.THashSet;

public class GitHubJsonRetriever {
	public static THashSet<Integer> ids;
	public static boolean done;
	private final int MAX_NUM_THREADS = 5;
	private String InputFile;
	private String TokenFile;
	private String OutPutDir;
	private ArrayList<String> namesList = new ArrayList<String>();

	public GitHubJsonRetriever(String inputFile, String tokenFile, String output) {
		InputFile = inputFile;
		TokenFile = tokenFile;
		OutPutDir = output;
		File outDir = new File(OutPutDir);
		if(!outDir.exists())
			outDir.mkdirs();
		else 
			addNames(output);
	}

	public static void main(String[] args) {
		GitHubJsonRetriever master = new GitHubJsonRetriever(args[0], args[1], args[2]);
		master.buildNamesList();
		master.orchastrate();
	}

	private void buildNamesList() {
		Scanner sc = null;
		try {
			sc = new Scanner(new File(InputFile));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		while (sc.hasNextLine()) {
			String url = sc.nextLine();
			String[] split = url.split(" ");
			String name = split[0];
			System.out.println(name);
			namesList.add(name);
		}
		sc.close();
		System.out.println(namesList.size() + " names");
	}

	public void orchastrate() {
		TokenList tokens = new TokenList(this.TokenFile);
		GitHubJsonRetrieverWorker workers[] = new GitHubJsonRetrieverWorker[MAX_NUM_THREADS];
		Thread[] threads = new Thread[MAX_NUM_THREADS];
		for (int i = 0; i < MAX_NUM_THREADS; i++) {
			GitHubJsonRetrieverWorker worker = new GitHubJsonRetrieverWorker(this.OutPutDir, tokens, i);
			workers[i] = worker;
			threads[i] = new Thread(worker);
			threads[i].start();
		}
		

		for (int i = 0;i < namesList.size(); i++) {
			String name = namesList.get(i);
			boolean nAssigned = true;
			while (nAssigned) {
				for (int j = 0; j < MAX_NUM_THREADS; j++) {
					if (workers[j].isReady()) {
						workers[j].setName(name);
						System.out.println("Assigning " + name + " to " + j);
						workers[j].readyFalse();
						nAssigned = false;
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
		for (Thread thread : threads){
			while (thread.isAlive()){
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
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
