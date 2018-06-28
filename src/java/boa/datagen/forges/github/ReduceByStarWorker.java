package boa.datagen.forges.github;

import java.io.File;
import java.util.ArrayList;
import java.util.Map.Entry;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import boa.datagen.util.FileIO;
import gnu.trove.set.hash.THashSet;

public class ReduceByStarWorker implements Runnable {
	int index;
	private TokenList tokens;
	private final String output;
	private final String input;
	JsonArray javarepos;
	private final String repo_url_header = "https://api.github.com/repos/";
	String stateFile = "";
	int javaCounter = 1;
	final static int RECORDS_PER_FILE = 100;
	THashSet<Integer> ids = GitHubReduceByStars.ids;
	private boolean available = true;

	public boolean isAvailable() {
		return available;
	}

	public ReduceByStarWorker(String repoPath, String output, TokenList tokenList) {
		this.output = output;
		this.tokens = tokenList;
		this.input = repoPath;
		this.javarepos = new JsonArray();
	}

	public void downloadRepoMetaDataForRepoIn() {
		Token tok = this.tokens.getNextAuthenticToken("https://api.github.com/repositories");
		File[] files = new File(this.input).listFiles();
		File repoFile = files[index];

		if (!repoFile.getPath().contains(".json")) {
			System.err.println(repoFile + " isn't a json");
			return;
		}
		String content = FileIO.readFileContents(repoFile);
		Gson parser = new Gson();
		JsonArray repos = parser.fromJson(content, JsonElement.class).getAsJsonArray();
		MetadataCacher mc = null;
		int size = repos.size();
		for (int j = 0; j < size; j++) {
			JsonObject repo = repos.get(j).getAsJsonObject();
			String name = repo.get("full_name").getAsString();
			boolean fork = repo.get("fork").getAsBoolean();
			ArrayList<String> languages = new ArrayList<String>();
			if (repo.has("language_list")) {
				System.out.println(repo.get("language_list").toString());
			}
			// JsonObject ll = repo.get("language_list").getAsJsonObject();
			for (Entry<String, JsonElement> je : repo.get("language_list").getAsJsonObject().entrySet()) {
				languages.add(je.getKey());
			}
			if (fork || !(languages.contains("Java") || languages.contains("JavaScript")
					|| languages.contains("PHP"))) {
				addRepo(output, repo);
				continue;
			}

			String repourl = this.repo_url_header + name;
			if (tok.getNumberOfRemainingLimit() <= 0) {
				tok = this.tokens.getNextAuthenticToken("https://api.github.com/repositories");
			}
			mc = new MetadataCacher(repourl, tok.getUserName(), tok.getToken());
			boolean authnticationResult = mc.authenticate();
			if (authnticationResult) {
				mc.getResponse();
				String pageContent = mc.getContent();
				JsonObject repository = parser.fromJson(pageContent, JsonElement.class).getAsJsonObject();
				int stars = repository.get("stargazers_count").getAsInt();
				int repoSize = repository.get("size").getAsInt();
				/**
				 * String contrUrl = repourl + "/contributors"; mc = new
				 * MetadataCacher(contrUrl , tok.getUserName(), tok.getToken());
				 * authnticationResult = mc.authenticate(); if
				 * (authnticationResult) { mc.getResponse(); pageContent =
				 * mc.getContent(); JsonArray contributorsList =
				 * parser.fromJson(pageContent,
				 * JsonElement.class).getAsJsonArray(); int contributors =
				 * contributorsList.size(); if (contributors < 2) continue; }
				 **/
				repo.addProperty("size", repoSize);
				if (!repo.has("stargazers_count"))
					repo.addProperty("stargazers_count", stars);
				addRepo(output, repo);
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
					j--;
				}
			}
		}

	}

	private void addRepo(String output, JsonObject repo) {
		File fileToWriteJson = null;
		this.javarepos.add(repo);
		if (this.javarepos.size() % RECORDS_PER_FILE == 0) {
			fileToWriteJson = new File(
					output + "/Thread-" + Thread.currentThread().getId() + "-page-" + javaCounter + ".json");
			while (fileToWriteJson.exists()) {
				System.out.println("file scala/thread-" + Thread.currentThread().getId() + "-page-" + javaCounter
						+ " arleady exist");
				javaCounter++;
				fileToWriteJson = new File(
						output + "/Thread-" + Thread.currentThread().getId() + "-page-" + javaCounter + ".json");
			}
			FileIO.writeFileContents(fileToWriteJson, this.javarepos.toString());
			System.out.println(Thread.currentThread().getId() + " " + javaCounter++);
			this.javarepos = new JsonArray();
		}
	}

	public void writeRemainingRepos(String output) {
		File fileToWriteJson = null;
		if (this.javarepos.size() > 0) {
			fileToWriteJson = new File(
					output + "/Thread-" + Thread.currentThread().getId() + "-page-" + javaCounter + ".json");
			while (fileToWriteJson.exists()) {
				javaCounter++;
				fileToWriteJson = new File(
						output + "/Thread-" + Thread.currentThread().getId() + "-page-" + javaCounter + ".json");
			}
			FileIO.writeFileContents(fileToWriteJson, this.javarepos.toString());
			System.out.println(Thread.currentThread().getId() + " scala " + javaCounter++);
		}
	}

	@Override
	public void run() {
		this.available = false;
		this.downloadRepoMetaDataForRepoIn();
		this.available = true;
	}

	public void closeWorker() {
		this.writeRemainingRepos(output);
	}

	public void setIndex(int i) {
		this.index = i;

	}
}
