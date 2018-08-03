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

public class GitHubRepoDownloaderWorker implements Runnable {
	File repoFile;
	private TokenList tokens;
	private final String output;
	private final String input;
	JsonArray javarepos;
	private final String repo_url_header = "https://api.github.com/repos/";
	String stateFile = "";
	int javaCounter = 1;
	final static int RECORDS_PER_FILE = 100;
	THashSet<Integer> ids = GitHubRepoDownloader.ids;
	private boolean available = true;

	public boolean isAvailable() {
		return available;
	}

	public GitHubRepoDownloaderWorker(String repoPath, String output, TokenList tokenList) {
		this.output = output;
		this.tokens = tokenList;
		this.input = repoPath;
		this.javarepos = new JsonArray();
	}

	public void downloadRepoMetaDataForRepoIn() {
		Token tok = this.tokens.getNextAuthenticToken("https://api.github.com/repositories");
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
			int id = repo.get("id").getAsInt();
			if (ids.contains(id))
				continue;
			String name = repo.get("full_name").getAsString();
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
				addRepo(output, repository);
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
				System.out.println("file /thread-" + Thread.currentThread().getId() + "-page-" + javaCounter
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
		while (true) {
			while (this.available) {
				if (GitHubRepoDownloader.done)
					break;
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			if (GitHubRepoDownloader.done)
				break;
			this.downloadRepoMetaDataForRepoIn();
			this.available = true;
			
		}
		writeRemainingRepos(output);
	}

	public void closeWorker() {
		this.writeRemainingRepos(output);
	}

	public void availableFalse() {
		this.available = false;
	}
	
	public void setFile(File file) {
		this.repoFile = file;

	}
}
