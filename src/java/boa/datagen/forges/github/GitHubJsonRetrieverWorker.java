package boa.datagen.forges.github;

import java.io.File;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import boa.datagen.util.FileIO;
import gnu.trove.set.hash.THashSet;

public class GitHubJsonRetrieverWorker implements Runnable {
	int index;
	private TokenList tokens;
	private final String output;
	String repo;
	JsonArray javarepos;
	private final String repo_url_header = "https://api.github.com/repos/";
	private String language_url_footer = "/languages";
	int javaCounter = 1;
	final static int RECORDS_PER_FILE = 100;
	THashSet<Integer> ids = GitHubReduceByStars.ids;
	File repoFile;
	private boolean available = true;

	public GitHubJsonRetrieverWorker(String output, TokenList tokenList) {
		this.output = output;
		this.tokens = tokenList;
		this.javarepos = new JsonArray();
	}

	public boolean isReady() {
		return available;
	}

	public void downloadRepoMetaDataForRepoIn() {
		Token tok = this.tokens.getNextAuthenticToken("https://api.github.com/repositories");
		Gson parser = new Gson();
		MetadataCacher mc = null;
		JsonObject repository = null;
		String name = repo;
		String repourl = this.repo_url_header + name;
		String languageurl = repourl + language_url_footer;
		if (tok.getNumberOfRemainingLimit() <= 0) {
			tok = this.tokens.getNextAuthenticToken("https://api.github.com/repositories");
		}
		mc = new MetadataCacher(repourl, tok.getUserName(), tok.getToken());
		boolean authnticationResult = mc.authenticate();
		if (authnticationResult) {
			mc.getResponse();
			String pageContent = mc.getContent();
			repository = parser.fromJson(pageContent, JsonElement.class).getAsJsonObject();
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
			}
		}
		mc = new MetadataCacher(languageurl, tok.getUserName(), tok.getToken());
		authnticationResult = mc.authenticate();
		if (authnticationResult && repository != null) {
			mc.getResponse();
			String pageContent = mc.getContent();
			JsonObject languages = parser.fromJson(pageContent, JsonElement.class).getAsJsonObject();
			repository.add("language_list", languages);
		}
		if (repository != null) {
			addRepo(output, repository);
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

	public void setName(String repoName) {
		repo = repoName;
	}
}
