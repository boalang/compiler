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
	public final static int MAX_NUM_THREADS = 10;
	public static THashSet<String> names = new THashSet<String>();
	
	public MetaDataDownLoader(String input, String output, String tokenFile) {
		this.repoNameDir = input;
		this.langNameDir = output;
		this.tokenFile = tokenFile;
		File outputDir = new File(output + "/java");
		if (!outputDir.exists()) {
			outputDir.mkdirs();
		} else {
			addNames(output + "/java");
		}
	}
	
	
	
	public static void main(String[] args) {
		if (args.length < 3) {
			throw new IllegalArgumentException();
		}
		GitHubRepoMetaDataDownloader master = new GitHubRepoMetaDataDownloader(args[0], args[1], args[2]);
		System.out.println(master.repoNameDir);
		master.orchastrate(new File(master.repoNameDir).listFiles().length);
	}
	
	public void orchastrate(int totalFies) {
		int shareSize = totalFies / MAX_NUM_THREADS;
		int start = 0;
		int end = 0;
		int i;
		TokenList tokens = new TokenList(this.tokenFile);
		for (i = 0; i < MAX_NUM_THREADS - 1; i++) {
			start = end;
			end = start + shareSize;
			MetaDataDownloadWorker worker = new MetaDataDownloadWorker(this.repoNameDir, this.langNameDir, tokens, start, end, i);
			new Thread(worker).start();
		}
		start = end;
		end = totalFies;
		MetaDataDownloadWorker worker = new MetaDataDownloadWorker(this.repoNameDir, this.langNameDir, tokens, start, end, i);
		new Thread(worker).start();
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
