package boa.datagen.forges.github;

import java.io.File;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import boa.datagen.util.FileIO;
import gnu.trove.set.hash.THashSet;

public class DataDownloadWorker implements Runnable {
	private TokenList tokens;
	private String repository_location;
	private final String output;
	JsonArray javarepos;
	private final String repo_url_header = "https://api.github.com/repos/";
	String stateFile = "";
	int javaCounter = 1;
	int jsCounter = 1;
	int phpCounter = 1;
	int scalaCounter = 1;
	int other = 1;
	int index = 0;
	final static int RECORDS_PER_FILE = 100;
	final int startFileNumber;
	final int endFileNumber;
	THashSet<String> names = GithubLanguageDownloadMaster.names;

	public DataDownloadWorker(String repoPath, String output, TokenList tokenList, int start, int end, int index) {
		this.output = output;
		this.tokens = tokenList;
		this.repository_location = repoPath;
		this.javarepos = new JsonArray();
		this.startFileNumber = start;
		this.endFileNumber = end;
		this.index = index;
	}

	public void downloadRepoMetaDataForRepoIn(int from, int to) {
		System.out.println(Thread.currentThread().getId() + " Responsible for processing: " + from + " and " + to);
		int pageNumber = from;
		Token tok = this.tokens.getNextAuthenticToken("https://api.github.com/repositories");
		File inDir = new File(repository_location);
		File[] files = inDir.listFiles();
		for (int i = from; i < to; i++) {
			File repoFile = files[i];
			String content = FileIO.readFileContents(repoFile);
			Gson parser = new Gson();
			JsonArray repos = parser.fromJson(content, JsonElement.class).getAsJsonArray();
			MetadataCacher mc = null;
			int size = repos.size();
			for (int j = 0; j < size; j++) {
				JsonObject repo = repos.get(j).getAsJsonObject();
				String name = repo.get("full_name").getAsString();
				if (names.contains(name)) {
					names.remove(name);
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
					int stars = repository.getAsJsonPrimitive("stargazers_count").getAsInt();
					if (stars <= 0)
						continue;
					boolean forked = repository.getAsJsonPrimitive("fork").getAsBoolean();
					if(forked)
						continue;
					repo.addProperty("stargazers_count", stars);
					String created = repository.getAsJsonPrimitive("created_at").getAsString();
					repo.addProperty("created_at", created);
					addRepo(output, repo);
					tok.setLastResponseCode(mc.getResponseCode());
					tok.setnumberOfRemainingLimit(mc.getNumberOfRemainingLimit());
					tok.setResetTime(mc.getLimitResetTime());
				} else {
					final int responsecode = mc.getResponseCode();
					System.err.println("authentication error " + responsecode);
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
			pageNumber++;
			System.out.println(Thread.currentThread().getId() + " processing: " + pageNumber);
		}
		this.writeRemainingRepos(output);
		System.out.print(Thread.currentThread().getId() + "finished");
	}

	private void addRepo(String output, JsonObject repo) {
		File fileToWriteJson = null;
		this.javarepos.add(repo);
		if (this.javarepos.size() % RECORDS_PER_FILE == 0) {
			fileToWriteJson = new File(output + "/java/Thread-" + Thread.currentThread().getId() + "-page-" + javaCounter + ".json");
			while (fileToWriteJson.exists()) {
				System.out.println("file java/thread-" + Thread.currentThread().getId() + "-page-" + javaCounter + " arleady exist");
				javaCounter++;
				fileToWriteJson = new File(output + "/java/Thread-" + Thread.currentThread().getId() + "-page-" + javaCounter + ".json");
			}
			FileIO.writeFileContents(fileToWriteJson, this.javarepos.toString());
			System.out.println(Thread.currentThread().getId() + " java " + javaCounter++);
			this.javarepos = new JsonArray();
		}
	}

	public void writeRemainingRepos(String output) {
		File fileToWriteJson = null;
		if (this.javarepos.size() > 0) {
			fileToWriteJson = new File(output + "/java/Thread-" + Thread.currentThread().getId() + "-page-" + javaCounter + ".json");
			while (fileToWriteJson.exists()) {
				javaCounter++;
				fileToWriteJson = new File(output + "/java/Thread-" + Thread.currentThread().getId() + "-page-" + javaCounter + ".json");
			}
			FileIO.writeFileContents(fileToWriteJson, this.javarepos.toString());
			System.out.println(Thread.currentThread().getId() + " java " + javaCounter++);
		}
	}

	@Override
	public void run() {
			this.downloadRepoMetaDataForRepoIn(this.startFileNumber, this.endFileNumber);
	}
}


