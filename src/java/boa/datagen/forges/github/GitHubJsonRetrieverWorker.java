package boa.datagen.forges.github;

import java.io.File;
import java.util.ArrayList;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import boa.datagen.util.FileIO;
import gnu.trove.set.hash.THashSet;

public class GitHubJsonRetrieverWorker implements Runnable {
	//int index;
	private TokenList tokens;
	private final String output;
	// String repo;
	JsonArray javarepos;
	private final String repo_url_header = "https://api.github.com/repos/";
	private String language_url_footer = "/languages";
	int javaCounter = 1;
	final static int RECORDS_PER_FILE = 100;
	THashSet<Integer> ids = GitHubJsonRetriever.ids;
	File repoFile;
	private boolean available = true;
	private String name;
	private final int index;

	public GitHubJsonRetrieverWorker(String output, TokenList tokenList, int i) {
		this.output = output;
		this.tokens = tokenList;
		this.javarepos = new JsonArray();
		this.index = i;
	}

	public boolean isReady() {return available;}

	public void readyFalse() {this.available = false;}
	
	public void downloadRepoMetaDataForRepoIn() {
		Token tok = this.tokens.getNextAuthenticToken("https://api.github.com/repositories");
		Gson parser = new Gson();
		MetadataCacher mc = null;
		JsonObject repository = null;
		String repourl = this.repo_url_header + name;
		String languageurl = repourl + language_url_footer;
		if (tok.getNumberOfRemainingLimit() <= 0)
			tok = this.tokens.getNextAuthenticToken("https://api.github.com/repositories");
		while (true) {
			mc = new MetadataCacher(repourl, tok.getUserName(), tok.getToken());
			boolean authnticationResult = mc.authenticate();
			if (authnticationResult) {
				mc.getResponse();
				String pageContent = mc.getContent();
				repository = parser.fromJson(pageContent, JsonElement.class).getAsJsonObject();
				tok.setLastResponseCode(mc.getResponseCode());
				tok.setnumberOfRemainingLimit(mc.getNumberOfRemainingLimit());
				tok.setResetTime(mc.getLimitResetTime());
				break;
			} else {
				final int responsecode = mc.getResponseCode();
				System.err.println("authentication error " + responsecode + " " + name);
				mc = new MetadataCacher("https://api.github.com/repositories", tok.getUserName(), tok.getToken());
				if (mc.authenticate()) {
					tok.setnumberOfRemainingLimit(mc.getNumberOfRemainingLimit());
				} else {
					System.out.println("token: " + tok.getId() + " exhausted");
					tok.setnumberOfRemainingLimit(0);
					break;
				}
			}
		}
		if (repository != null) {
			while (true) {
				mc = new MetadataCacher(languageurl, tok.getUserName(), tok.getToken());
				boolean authnticationResult = mc.authenticate();
				if (authnticationResult) {
					mc.getResponse();
					String pageContent = mc.getContent();
					JsonObject languages = parser.fromJson(pageContent, JsonElement.class).getAsJsonObject();
					repository.add("language_list", languages);
					break;
				}
				final int responsecode = mc.getResponseCode();
				System.err.println("authentication error getting languages " + responsecode + " " + name);
				mc = new MetadataCacher("https://api.github.com/repositories", tok.getUserName(), tok.getToken());
				if (mc.authenticate()) {
					tok.setnumberOfRemainingLimit(mc.getNumberOfRemainingLimit());
					continue;
				} else {
					System.out.println("token: " + tok.getId() + " exhausted");
					tok.setnumberOfRemainingLimit(0);
					break;
				}
			}
			addRepo(output, repository);
		}
	}

	private void addRepo(String output, JsonObject repo) {
		File fileToWriteJson = null;
		this.javarepos.add(repo);
		if (this.javarepos.size() % RECORDS_PER_FILE == 0) {
			fileToWriteJson = new File(
					output + "/Thread-" + this.index + "-page-" + javaCounter + ".json");
			while (fileToWriteJson.exists()) {
				System.out.println("file scala/thread-" + this.index + "-page-" + javaCounter
						+ " arleady exist");
				javaCounter++;
				fileToWriteJson = new File(
						output + "/Thread-" + this.index + "-page-" + javaCounter + ".json");
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
					output + "/Thread-" + this.index + "-page-" + javaCounter + ".json");
			while (fileToWriteJson.exists()) {
				javaCounter++;
				fileToWriteJson = new File(
						output + "/Thread-" + this.index + "-page-" + javaCounter + ".json");
			}
			FileIO.writeFileContents(fileToWriteJson, this.javarepos.toString());
			System.out.println(this.index  + javaCounter++);
		}
	}

	@Override
	public void run() {
		while (true) {
			while (this.available) {
				if (GitHubJsonRetriever.done)
					break;
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			if (GitHubJsonRetriever.done)
				break;
			this.downloadRepoMetaDataForRepoIn();
			this.available = true;
		}
		writeRemainingRepos(output);
	}

	public void setName(String names) {
		this.name = names;
	}
}
