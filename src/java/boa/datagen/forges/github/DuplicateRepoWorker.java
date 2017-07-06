package boa.datagen.forges.github;

import java.io.File;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import boa.datagen.util.FileIO;
import gnu.trove.set.hash.THashSet;

public class DuplicateRepoWorker implements Runnable {
	private final String inPath;
	private final String outPath;
	private final int to;
	private final int from;
	THashSet<String> names = DuplicateRepoRemover.names;
	private JsonArray cleared = new JsonArray();
	private int counter = 0;
	
	
	public DuplicateRepoWorker(String inPath, String outPath, int from, int to ){
		this.inPath = inPath;
		this.outPath = outPath;
		this.to = to;
		this.from = from;
	}
	
	private void removeDuplicates(){
		System.out.println(Thread.currentThread().getId() + " Responsible for processing: " + from + " and " + to);
		File[] files = new File(inPath).listFiles(); 
		for (int i = from; i < to; i++){
			File repoFile = files[i];
			String content = FileIO.readFileContents(repoFile);
			Gson parser = new Gson();
			JsonArray repos = parser.fromJson(content, JsonElement.class).getAsJsonArray();
			int size = repos.size();
			for (int j = 0; j < size; j++) {
				JsonObject repo = repos.get(j).getAsJsonObject();
				String name = repo.get("full_name").getAsString();
				if (names.contains(name)) {
					System.out.println("found a duplicate " + name);
					continue;
				}
				names.add(name);
				int id = repo.get("id").getAsInt();
				JsonObject owner = repo.get("owner").getAsJsonObject();
				String created_at = null;
				if (repo.has("created_at"))
					created_at = repo.get("created_at").getAsString();
				boolean fork = repo.get("fork").getAsBoolean();
				String description = null;
				if (!repo.get("description").isJsonNull())
					description = repo.get("description").getAsString();
				JsonObject langList = new JsonObject();
				if (repo.has("language_list")){
					if(!repo.get("language_list").isJsonObject()){
						String brokenList = repo.get("language_list").getAsString();
						brokenList = brokenList.replace("\\", "");
						repo.remove("language_list");
						repo.addProperty("language_list", brokenList);
					}
					if (repo.get("language_list").isJsonObject())
						langList = repo.get("language_list").getAsJsonObject();
				}
				
				JsonObject reducedRepo = new JsonObject();
				reducedRepo.addProperty("id", id);
				reducedRepo.addProperty("full_name", name);
				reducedRepo.add("owner", owner);
				reducedRepo.addProperty("created_at", created_at);
				reducedRepo.addProperty("fork", fork);
				reducedRepo.addProperty("description", description);
				reducedRepo.add("language_list", langList);
				this.addRepo(reducedRepo);
			}
			System.out.println(Thread.currentThread().getId() + " processing: " + i);
		}
		this.writeRemainingRepos();
		System.out.print(Thread.currentThread().getId() + "finished");
	}
	
	private void writeRemainingRepos() {
		if (cleared.size() > 0){
			File fileToWriteJson = new File(outPath + "/" + Thread.currentThread().getId() + "-page" + counter + ".json");
			while (fileToWriteJson.exists()) {
				System.out.println("file thread-" + Thread.currentThread().getId() + "-page-" + counter + " arleady exist");
				counter++;
				fileToWriteJson = new File(outPath + "/" + Thread.currentThread().getId() + "-page" + counter + ".json");
			}
			FileIO.writeFileContents(fileToWriteJson, cleared.toString());
		}
	}

	private void addRepo(JsonObject repo) {
		cleared.add(repo);
		if (cleared.size() >= 100){
			File fileToWriteJson = new File(outPath + "/" + Thread.currentThread().getId() + "-page" + counter + ".json");
			while (fileToWriteJson.exists()) {
				System.out.println("file thread-" + Thread.currentThread().getId() + "-page-" + counter + " arleady exist");
				counter++;
				fileToWriteJson = new File(outPath + "/" + Thread.currentThread().getId() + "-page" + counter + ".json");
			}
			FileIO.writeFileContents(fileToWriteJson, cleared.toString());
			counter++;
			cleared = new JsonArray();
		}
	}

	@Override
	public void run() {
		this.removeDuplicates();
	}

}
