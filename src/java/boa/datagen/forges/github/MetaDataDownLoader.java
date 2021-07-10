package boa.datagen.forges.github;

import java.io.File;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import boa.datagen.util.FileIO;
import gnu.trove.set.hash.THashSet;

public class MetaDataDownLoader {
	
	public final String repoNameDir;
	public final String langNameDir;
	public final String tokenFile;
	public final static int MAX_NUM_THREADS = 2;
	public static THashSet<String> names = new THashSet<String>();
	public MetaDataDownloadWorker[] workers = new MetaDataDownloadWorker[2];
	
	public MetaDataDownLoader(String input, String output, String tokenFile) {
		this.repoNameDir = input;
		this.langNameDir = output;
		this.tokenFile = tokenFile;
		File outputDir = new File(output);
		if (!outputDir.exists()) {
			outputDir.mkdirs();
		} else {
			addNames(output);
		}
	}
	
	
	
	public static void main(String[] args) {
		if (args.length < 3) {
			throw new IllegalArgumentException();
		}
		MetaDataDownLoader master = new MetaDataDownLoader(args[0], args[1], args[2]);
		System.out.println(master.repoNameDir);
		master.orchastrate(new File(master.repoNameDir).listFiles().length);
	}
	
	public void orchastrate(int totalFies) {
		TokenList tokens = new TokenList(this.tokenFile);
		for (int i = 0; i < MAX_NUM_THREADS ; i++) {
			MetaDataDownloadWorker worker = new MetaDataDownloadWorker(this.repoNameDir, this.langNameDir, tokens, i);
			workers[i] = worker;
		}
		File inDir = new File(repoNameDir);
		File[] files = inDir.listFiles();
		for (int i = 0; i < files.length; i++) {
			MetaDataDownloadWorker worker = null;
			while (worker == null){
				for (int j = 0; j < workers.length; j++){
					if(workers[j].isReady()){
						System.out.println("worker-" + j + " processing " + i);
						worker = workers[j];
						break;
					}
				//	System.out.println("waiting for a worker");
				}
			}
			worker.setFile(files[i]);
			new Thread(worker).start();
		}
		
		for (int i = 0; i < workers.length; i++) {
			int count = 0;
			while (!workers[i].isReady() && count < 10000){
				try {
					Thread.sleep(10000);
					count ++;
				} catch (final InterruptedException e) {
					e.printStackTrace();
				}
			}
			workers[i].writeRemainingRepos(langNameDir);
			System.out.println(i + " is finished");
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
			if(!(files[i].toString().contains(".json"))){
				System.out.println("skipping " + files[i].toString());
				continue;
			}
			System.out.println("proccessing page " + files[i].getName());
			content = FileIO.readFileContents(files[i]);
			repos = parser.fromJson(content, JsonElement.class).getAsJsonArray();
			for (JsonElement repoE : repos) {
				repo = repoE.getAsJsonObject();
				names.add(repo.get("full_name").getAsString());
			}
		}
	}

}
