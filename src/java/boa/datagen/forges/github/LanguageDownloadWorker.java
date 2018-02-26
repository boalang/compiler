package boa.datagen.forges.github;

import boa.datagen.util.FileIO;
import gnu.trove.set.hash.THashSet;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.File;


/**
 * Created by nmtiwari on 9/15/16. This class reads repository names from a
 * directory and downloads the languages for each repository available in the
 * directory. Point to note here is that repository names are read from github's
 * repository metadata json files.
 */
public class LanguageDownloadWorker implements Runnable {
	private TokenList tokens;
	private String repository_location;
	private final String output;
	JsonArray javarepos;
	JsonArray jsrepos;
	JsonArray phprepos;
	JsonArray scalarepos;
	JsonArray otherrepos;
	String language_url_header = "https://api.github.com/repos/";
	String language_url_footer = "/languages";
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
	String namesFilePath = "";

	public LanguageDownloadWorker(String repoPath, String output, TokenList tokenList, int start, int end, int index) {
		this.output = output;
		this.tokens = tokenList;
		this.repository_location = repoPath;
		this.javarepos = new JsonArray();
		this.jsrepos = new JsonArray();
		this.phprepos = new JsonArray();
		this.scalarepos = new JsonArray();
		this.otherrepos = new JsonArray();
		this.startFileNumber = start;
		this.endFileNumber = end;
		this.index = index;
	}

	public void downloadLangForRepoIn(int from, int to) {
		System.out.println(Thread.currentThread().getId() + " Responsible for processing: " + from + " and " + to);
		String fileHeader = this.repository_location + "/page-";
		String fileFooter = ".json";
		int pageNumber = from;
		Token tok = this.tokens.getNextAuthenticToken("https://api.github.com/repositories");
		// File namesFile = new File(namesFilePath);
		while (pageNumber <= to) {
			File repoFile = new File(fileHeader + pageNumber + fileFooter);
			String content = FileIO.readFileContents(repoFile);
			Gson parser = new Gson();
			JsonArray repos = parser.fromJson(content, JsonElement.class).getAsJsonArray();
			MetadataCacher mc = null;
			int size = repos.size();
			for (int i = 0; i < size; i++) {
				JsonObject repo = repos.get(i).getAsJsonObject();
				String name = repo.get("full_name").getAsString();
				if (names.contains(name)) {
					names.remove(name);
					continue;
				}
				String langurl = this.language_url_header + name + this.language_url_footer;
				if (tok.getNumberOfRemainingLimit() <= 0) {
					tok = this.tokens.getNextAuthenticToken("https://api.github.com/repositories");
				}
				mc = new MetadataCacher(langurl, tok.getUserName(), tok.getToken());
				boolean authnticationResult = mc.authenticate();
				if (authnticationResult) {
					mc.getResponse();
					String pageContent = mc.getContent();
					JsonObject languages = parser.fromJson(pageContent, JsonElement.class).getAsJsonObject();
					repo.add("language_list", languages);
					addRepoInReleventLangList(pageContent, output, repo);
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
						i--;
					}
				}
			}
			pageNumber++;
			System.out.println(Thread.currentThread().getId() + " processing: " + pageNumber);
		}
		this.writeRemainingRepos(output);
		System.out.print(Thread.currentThread().getId() + "finished");
	}

	private void addRepoInReleventLangList(String pageContent, String output, JsonObject repo) {
		File fileToWriteJson = null;
		if (pageContent.contains("\"Java\":")) {
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
		} else if (pageContent.contains("\"JavaScript\":")) {
			this.jsrepos.add(repo);
			if (this.jsrepos.size() % RECORDS_PER_FILE == 0) {
				fileToWriteJson = new File(output + "/js/Thread-" + Thread.currentThread().getId() + "-page-" + jsCounter + ".json");
				while (fileToWriteJson.exists()) {
					System.out.println("file js/thread-" + Thread.currentThread().getId() + "-page-" + jsCounter
							+ " arleady exist");
					jsCounter++;
					fileToWriteJson = new File(output + "/js/Thread-" + Thread.currentThread().getId() + "-page-" + jsCounter + ".json");
				}
				FileIO.writeFileContents(fileToWriteJson, this.jsrepos.toString());
				System.out.println(Thread.currentThread().getId() + " js " + jsCounter++);
				this.jsrepos = new JsonArray();
			}
		} else if (pageContent.contains("\"PhP\":")) {
			this.phprepos.add(repo);
			if (this.phprepos.size() % RECORDS_PER_FILE == 0) {
				fileToWriteJson = new File(output + "/php/Thread-" + Thread.currentThread().getId() + "-page-" + phpCounter + ".json");
				while (fileToWriteJson.exists()) {
					System.out.println("file php/thread-" + Thread.currentThread().getId() + "-page-" + phpCounter
							+ " arleady exist");
					phpCounter++;
					fileToWriteJson = new File(output + "/php/Thread-" + Thread.currentThread().getId() + "-page-" + phpCounter + ".json");
				}
				FileIO.writeFileContents(fileToWriteJson, this.phprepos.toString());
				System.out.println(Thread.currentThread().getId() + " php " + phpCounter++);
				this.phprepos = new JsonArray();
			}
		} else if (pageContent.contains("\"Scala\":")) {
			this.scalarepos.add(repo);
			if (this.scalarepos.size() % RECORDS_PER_FILE == 0) {
				fileToWriteJson = new File(output + "/scala/Thread-" + Thread.currentThread().getId() + "-page-" + scalaCounter + ".json");
				while (fileToWriteJson.exists()) {
					System.out.println("file scala/thread-" + Thread.currentThread().getId() + "-page-" + scalaCounter + " arleady exist");
					scalaCounter++;
					fileToWriteJson = new File(output + "/scala/Thread-" + Thread.currentThread().getId() + "-page-" + scalaCounter + ".json");
				}
				FileIO.writeFileContents(fileToWriteJson, this.scalarepos.toString());
				System.out.println(Thread.currentThread().getId() + " scala: " + scalaCounter++);
				this.scalarepos = new JsonArray();
			}
		} else {
			this.otherrepos.add(repo);
			if (this.otherrepos.size() % RECORDS_PER_FILE == 0) {
				fileToWriteJson = new File(output + "/other/Thread-" + Thread.currentThread().getId() + "-page-" + other + ".json");
				while (fileToWriteJson.exists()) {
					System.out.println("file other/thread-" + Thread.currentThread().getId() + "-page-" + other
							+ " arleady exist");
					other++;
					fileToWriteJson = new File(output + "/other/Thread-" + Thread.currentThread().getId() + "-page-" + other + ".json");
				}
				FileIO.writeFileContents(fileToWriteJson, this.otherrepos.toString());
				System.out.println(Thread.currentThread().getId() + " other: " + other++);
				this.otherrepos = new JsonArray();
			}
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
		if (this.jsrepos.size() > 0) {
			fileToWriteJson = new File(output + "/js/Thread-" + Thread.currentThread().getId() + "-page-" + jsCounter + ".json");
			while (fileToWriteJson.exists()) {
				jsCounter++;
				fileToWriteJson = new File(output + "/js/Thread-" + Thread.currentThread().getId() + "-page-" + jsCounter + ".json");
			}
			FileIO.writeFileContents(fileToWriteJson, this.jsrepos.toString());
			System.out.println(Thread.currentThread().getId() + " js " + jsCounter++);
		}
		if (this.phprepos.size() > 0) {
			fileToWriteJson = new File(output + "/php/Thread-" + Thread.currentThread().getId() + "-page-" + phpCounter + ".json");
			while (fileToWriteJson.exists()) {
				phpCounter++;
				fileToWriteJson = new File(output + "/php/Thread-" + Thread.currentThread().getId() + "-page-" + phpCounter + ".json");
			}
			FileIO.writeFileContents(fileToWriteJson, this.phprepos.toString());
			System.out.println(Thread.currentThread().getId() + " php " + phpCounter++);
		}
		if (this.scalarepos.size() > 0) {
			fileToWriteJson = new File(
					output + "/scala/Thread-" + Thread.currentThread().getId() + "-page-" + scalaCounter + ".json");
			while (fileToWriteJson.exists()) {
				scalaCounter++;
				fileToWriteJson = new File(output + "/scala/Thread-" + Thread.currentThread().getId() + "-page-" + scalaCounter + ".json");
			}
			FileIO.writeFileContents(fileToWriteJson, this.scalarepos.toString());
			System.out.println(Thread.currentThread().getId() + " scala:  " + scalaCounter++);
		}
		if (this.otherrepos.size() > 0) {
			fileToWriteJson = new File(output + "/other/Thread-" + Thread.currentThread().getId() + "-page-" + other + ".json");
			while (fileToWriteJson.exists()) {
				other++;
				fileToWriteJson = new File(output + "/other/Thread-" + Thread.currentThread().getId() + "-page-" + other + ".json");
			}
			FileIO.writeFileContents(fileToWriteJson, this.otherrepos.toString());
			System.out.println(Thread.currentThread().getId() + " other:  " + other++);
		}
	}

	@Override
	public void run() {
			this.downloadLangForRepoIn(this.startFileNumber, this.endFileNumber);
	}
}
