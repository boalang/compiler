package boa.datagen.forges.github;

import java.io.File;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import boa.datagen.util.FileIO;
import gnu.trove.set.hash.THashSet;

public class DuplicateRepoRemover {
	public static THashSet<String> names = new THashSet<String>();
	
	public DuplicateRepoRemover(String outPath){
		File outDir = new File(outPath);
		if (!outDir.exists())
			outDir.mkdirs();
		else
			addNames(outPath);
	}
	
	
	public static void main(String[] args) {
		File dir = new File(args[0]);
		File[] files = dir.listFiles();
		int totalFiles = files.length;
		int numThreads = 3;
		int from = 0, to = 0;
		int shareSize = totalFiles / numThreads;

		DuplicateRepoRemover dp = new DuplicateRepoRemover(args[1]);
		for (int i = 0; i < numThreads - 1; i++) {
			System.out.println(i);
				from = to;
				to = from + shareSize;
				DuplicateRepoWorker worker = new DuplicateRepoWorker(args[0], args[1], from, to);
				new Thread(worker).start();
		}
		from = to;
		to = totalFiles;
		DuplicateRepoWorker worker = new DuplicateRepoWorker(args[0], args[1], from, to);
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
		for (int i = 1; i < files.length; i++) {
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
