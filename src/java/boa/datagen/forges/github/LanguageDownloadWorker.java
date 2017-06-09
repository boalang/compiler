package boa.datagen.forges.github;

import boa.datagen.util.FileIO;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.json.JSONArray;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.concurrent.Callable;

/**
 * Created by nmtiwari on 9/15/16. This class reads repository names from a
 * directory and downloads the languages for each repository available in the
 * directory. Point to note here is that repository names are read from github's
 * repository metadata json files.
 */
public class LanguageDownloadWorker implements Runnable{
	private TokenList tokens;
	private String repository_location;
	private final String output;
	JSONArray javarepos;
	JSONArray jsrepos;
	JSONArray phprepos;
	JSONArray scalarepos;
	String language_url_header = "https://api.github.com/repos/";
	String language_url_footer = "/languages";
	int javaCounter = 1;
	int jsCounter = 1;
	int phpCounter = 1;
	int scalaCounter = 1;
	int other = 0;
	final static int RECORDS_PER_FILE = 100;
	final int startFileNumber;
	final int endFileNumber;

	public LanguageDownloadWorker(String repoPath, String output,  TokenList tokenList, int start, int end) {
		this.output = output;
		this.tokens = tokenList;
		this.repository_location = repoPath;
		this.javarepos = new JSONArray();
		this.jsrepos = new JSONArray();
		this.phprepos = new JSONArray();
		this.scalarepos = new JSONArray();
		this.startFileNumber = start;
		this.endFileNumber = end;
	}


	public LanguageDownloadWorker(String repoPath, String output, TokenList tokenList, int start) {
		this.output = output;
		this.tokens = tokenList;
		this.repository_location = repoPath;
		this.javarepos = new JSONArray();
		this.jsrepos = new JSONArray();
		this.phprepos = new JSONArray();
		this.scalarepos = new JSONArray();
		this.startFileNumber = start;
		this.endFileNumber = -1;
	}


	/**
	 *
	 * @param path:
	 *            Path of the file Given a valid file, this function downloads
	 *            language_list for each repo in file at path path
	 * @param output:
	 *            place where augumented data should be stored
	 */
	public void downloadLangForRepoIn(String path, String output) throws FileNotFoundException {
		File repoFile = new File(path);
		if (!repoFile.exists()) {
			throw new FileNotFoundException();
		}
		String content = FileIO.readFileContents(repoFile);
		Gson parser = new Gson();
		JsonArray repos = parser.fromJson(content, JsonElement.class).getAsJsonArray();
		MetadataLangCacher mc = null;
		for (int i = 0; i < repos.size(); i++) {
			JsonObject repo = repos.get(i).getAsJsonObject();
			try {
				mc = this.tokens.getAuthenticLangCacher(
						this.language_url_header + repo.get("full_name") + this.language_url_footer);
			} catch (Exception e) {
				// none of the tokens authenticated... hence write whatever has
				// been collected to a file
				this.writeRemainingRepos(output);
				return;
			}
			// if url is incorrect or invalid
			if (mc == null) {
				continue;
			}
			mc.getResponseJson();
			String pageContent = mc.getContent();
			repo.addProperty("language_list", pageContent);
			addRepoInReleventLangList(pageContent, output, repo);
		}
	}

	public void downloadLangForRepoIn(String path, String output, int from) {
		while (true) {
			try {
				downloadLangForRepoIn(path + "/page-" + from + ".json", output);
				System.out.println(" From:" + from++);
			} catch (FileNotFoundException e) {
				this.writeRemainingRepos(output);
				System.out.print("others: " + this.other);
				return;
			}
		}
	}

	public void downloadLangForRepoIn(int from, int to) throws FileNotFoundException {
		System.out.println(Thread.currentThread().getId() + " Responsible for processing: " + from + " and " + to);
		String fileHeader = this.repository_location + "/page-";
		String fileFooter = ".json";
		int pageNumber = from;
		Token tok = this.tokens.getAuthenticatedToken(Thread.currentThread().getId());
		while (pageNumber <= to) {
			File repoFile = new File(fileHeader + pageNumber + fileFooter);
			String content = FileIO.readFileContents(repoFile);
			Gson parser = new Gson();
			JsonArray repos = parser.fromJson(content, JsonElement.class).getAsJsonArray();
			MetadataCacher mc = null;
			int size = repos.size();
			for (int i = 0; i < size; i++) {
				JsonObject repo = repos.get(i).getAsJsonObject();
				String langurl = this.language_url_header + repo.get("full_name").getAsString() + this.language_url_footer;
				if(tok.getNumberOfRemainingLimit() <= 0){
					System.out.print(Thread.currentThread().getId() + " freeing : " + tok.getId());
					tok.reset(this.tokens);
					tok = this.tokens.getAuthenticatedToken(Thread.currentThread().getId());
				}
				mc = new MetadataCacher(langurl, tok.getUserName(), tok.getToken());
				boolean authnticationResult = mc.authenticate();
				if(authnticationResult){
					mc.getResponse();
					String pageContent = mc.getContent();
					repo.addProperty("language_list", pageContent);
					addRepoInReleventLangList(pageContent, output, repo);
					tok.setLastResponseCode(mc.getResponseCode());
					tok.setnumberOfRemainingLimit(mc.getNumberOfRemainingLimit());
					tok.setResetTime(mc.getLimitResetTime());
				}else{
					final int responsecode = mc.getResponseCode();
					if(responsecode/100 == 4){
						continue;
					}
				}
			}
			pageNumber++;
			System.out.println(Thread.currentThread().getId() + " processing: " + pageNumber);
		}
		this.writeRemainingRepos(output);
		System.out.print(Thread.currentThread().getId() + "others: " + this.other);
	}


	private void addRepoInReleventLangList(String pageContent, String output, JsonObject repo) {
		File fileToWriteJson = null;
		if (pageContent.contains("\"Java\":")) {
			this.javarepos.put(this.javarepos.length(), repo);
			if (this.javarepos.length() % RECORDS_PER_FILE == 0) {
				fileToWriteJson = new File(output + "/java/Thread- "+ Thread.currentThread().getId() + "-page-" + javaCounter + ".json");
				FileIO.writeFileContents(fileToWriteJson, this.javarepos.toString());
				System.out.println(Thread.currentThread().getId() + " java " + javaCounter++);
                this.javarepos = new JSONArray();
			}
		} else if (pageContent.contains("\"JavaScript\":")) {
			this.jsrepos.put(this.jsrepos.length(), repo);
			if (this.jsrepos.length() % RECORDS_PER_FILE == 0) {
				fileToWriteJson = new File(output + "/js/Thread- "+ Thread.currentThread().getId() + "-page-" + jsCounter + ".json");
				FileIO.writeFileContents(fileToWriteJson, this.jsrepos.toString());
				System.out.println(Thread.currentThread().getId() + " js " + jsCounter++);
                this.jsrepos = new JSONArray();
			}
		} else if (pageContent.contains("\"PhP\":")) {
			this.phprepos.put(this.phprepos.length(), repo);
			if (this.phprepos.length() % RECORDS_PER_FILE == 0) {
				fileToWriteJson = new File(output + "/php/Thread- "+ Thread.currentThread().getId() + "-page-"  + phpCounter + ".json");
				FileIO.writeFileContents(fileToWriteJson, this.phprepos.toString());
				System.out.println(Thread.currentThread().getId() + " php " + phpCounter++);
                this.phprepos = new JSONArray();
			}
		} else if (pageContent.contains("\"Scala\":")) {
			this.scalarepos.put(this.scalarepos.length(), repo);
			if (this.scalarepos.length() % RECORDS_PER_FILE == 0) {
				fileToWriteJson = new File(output + "/scala/Thread- "+ Thread.currentThread().getId() + "-page-"  + scalaCounter + ".json");
				FileIO.writeFileContents(fileToWriteJson, this.scalarepos.toString());
				System.out.println(Thread.currentThread().getId() + " scala: " + scalaCounter++);
                this.scalarepos = new JSONArray();
			}
		} else {
			this.other++;
		}
	}

	public void writeRemainingRepos(String output) {
		File fileToWriteJson = null;
		if (this.javarepos.length() > 0) {
			fileToWriteJson = new File(output + "/java/Thread- "+ Thread.currentThread().getId() + "-page-" + javaCounter + ".json");
			FileIO.writeFileContents(fileToWriteJson, this.javarepos.toString());
			System.out.println(Thread.currentThread().getId() + " java " + javaCounter++);
		}
		if (this.jsrepos.length() > 0) {
			fileToWriteJson = new File(output + "/js/Thread- "+ Thread.currentThread().getId() + "-page-" + jsCounter + ".json");
			FileIO.writeFileContents(fileToWriteJson, this.jsrepos.toString());
			System.out.println(Thread.currentThread().getId() + " js " + jsCounter++);
		}
		if (this.phprepos.length() > 0) {
			fileToWriteJson = new File(output + "/php/Thread- "+ Thread.currentThread().getId() + "-page-" + phpCounter + ".json");
			FileIO.writeFileContents(fileToWriteJson, this.phprepos.toString());
			System.out.println(Thread.currentThread().getId() + " php " + phpCounter++);
		}
		if (this.scalarepos.length() > 0) {
			fileToWriteJson = new File(output + "/scala/Thread- "+ Thread.currentThread().getId() + "-page-" + scalaCounter + ".json");
			FileIO.writeFileContents(fileToWriteJson, this.scalarepos.toString());
			System.out.println(Thread.currentThread().getId() + " scala:  " + scalaCounter++);
		}
		System.out.println("Thread- "+ Thread.currentThread().getId() + " others: " + this.other);
	}

	@Override
	public void run(){
		try {
			this.downloadLangForRepoIn(this.startFileNumber, this.endFileNumber);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}
