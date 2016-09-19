package boa.datagen.forges.github;

import boa.datagen.util.FileIO;
import com.google.gson.*;
import org.json.JSONArray;
import java.io.File;
import java.io.FileNotFoundException;

/**
 * Created by nmtiwari on 9/15/16. This class reads repository names from a
 * directory and downloads the languages for each repository available in the
 * directory. Point to note here is that repository names are read from github's
 * repository metadata json files.
 */
public class GithubRepositoryLanguageDownloader {
	private TokenList tokens;
	private String repository_location;
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

	public GithubRepositoryLanguageDownloader(String repoPath, String tokenFile) {
		this.tokens = new TokenList(tokenFile);
		this.repository_location = repoPath;
		this.javarepos = new JSONArray();
		this.jsrepos = new JSONArray();
		this.phprepos = new JSONArray();
		this.scalarepos = new JSONArray();
	}

	public static void main(String[] args) {
		if (args.length < 3) {
			throw new IllegalArgumentException();
		}
		File outputDir = new File(args[1] + "/java");
		if (!outputDir.exists()) {
			outputDir.mkdirs();
		}
		outputDir = new File(args[1] + "/js");
		if (!outputDir.exists()) {
			outputDir.mkdirs();
		}
		outputDir = new File(args[1] + "/php");
		if (!outputDir.exists()) {
			outputDir.mkdirs();
		}
		outputDir = new File(args[1] + "/scala");
		if (!outputDir.exists()) {
			outputDir.mkdirs();
		}

		if (args.length == 4) {
			GithubRepositoryLanguageDownloader downloader = new GithubRepositoryLanguageDownloader(args[0], args[3]);
			downloader.downloadLangForRepoIn(args[0], args[1], Integer.parseInt(args[2]));
		} else {
			GithubRepositoryLanguageDownloader downloader = new GithubRepositoryLanguageDownloader(args[0], args[4]);
			try {
				downloader.downloadLangForRepoIn(args[0], args[1], Integer.parseInt(args[2]),
						Integer.parseInt(args[3]));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
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

	public void downloadLangForRepoIn(String path, String output, int from, int to) throws FileNotFoundException {
		System.out.println("Responsible for processing: " + from + " and " + to);
		String fileHeader = path + "/page-";
		String fileFooter = ".json";
		int pageNumber = from;
		while (pageNumber <= to) {
			File repoFile = new File(fileHeader + pageNumber + fileFooter);
			String content = FileIO.readFileContents(repoFile);
			Gson parser = new Gson();
			JsonArray repos = parser.fromJson(content, JsonElement.class).getAsJsonArray();
			MetadataLangCacher mc = null;
			int size = repos.size();
			for (int i = 0; i < size; i++) {
				JsonObject repo = repos.get(i).getAsJsonObject();
				try {
					String detail = repo.get("full_name").getAsString();
					mc = this.tokens
							.getAuthenticLangCacher(this.language_url_header + detail + this.language_url_footer);
				} catch (IllegalArgumentException e) {
					System.out.println(" All Token Exhausted and code is stopping for page: " + pageNumber);
					e.printStackTrace();
					this.writeRemainingRepos(output);
					return;
				}
				if (mc == null) {
					System.out.println(repo.get("full_name").getAsString() + " is not a valid url");
					continue;
				}
				//mc.getResponseJson();
				String pageContent = mc.getContent();
				repo.addProperty("language_list", pageContent);
				addRepoInReleventLangList(pageContent, output, repo);
			}
			pageNumber++;
			System.out.println("processing: " + pageNumber);
		}
		this.writeRemainingRepos(output);
		System.out.print("others: " + this.other);
	}


	private void addRepoInReleventLangList(String pageContent, String output, JsonObject repo) {
		File fileToWriteJson = null;
		if (pageContent.contains("\"Java\":")) {
			this.javarepos.put(this.javarepos.length(), repo);
			if (this.javarepos.length() % RECORDS_PER_FILE == 0) {
				fileToWriteJson = new File(output + "/java/page-" + javaCounter + ".json");
				FileIO.writeFileContents(fileToWriteJson, this.javarepos.toString());
				System.out.println("java " + javaCounter++);
                this.javarepos = new JSONArray();
			}
		} else if (pageContent.contains("\"JavaScript\":")) {
			this.jsrepos.put(this.jsrepos.length(), repo);
			if (this.jsrepos.length() % RECORDS_PER_FILE == 0) {
				fileToWriteJson = new File(output + "/js/page-" + jsCounter + ".json");
				FileIO.writeFileContents(fileToWriteJson, this.jsrepos.toString());
				System.out.println("js " + jsCounter++);
                this.jsrepos = new JSONArray();
			}
		} else if (pageContent.contains("\"PhP\":")) {
			this.phprepos.put(this.phprepos.length(), repo);
			if (this.phprepos.length() % RECORDS_PER_FILE == 0) {
				fileToWriteJson = new File(output + "/php/page-" + phpCounter + ".json");
				FileIO.writeFileContents(fileToWriteJson, this.phprepos.toString());
				System.out.println("php " + phpCounter++);
                this.phprepos = new JSONArray();
			}
		} else if (pageContent.contains("\"Scala\":")) {
			this.scalarepos.put(this.scalarepos.length(), repo);
			if (this.scalarepos.length() % RECORDS_PER_FILE == 0) {
				fileToWriteJson = new File(output + "/scala/page-" + scalaCounter + ".json");
				FileIO.writeFileContents(fileToWriteJson, this.scalarepos.toString());
				System.out.println(" scala: " + scalaCounter++);
                this.scalarepos = new JSONArray();
			}
		} else {
			this.other++;
		}
	}

	public void writeRemainingRepos(String output) {
		File fileToWriteJson = null;
		if (this.javarepos.length() > 0) {
			fileToWriteJson = new File(output + "/java/page-" + javaCounter + ".json");
			FileIO.writeFileContents(fileToWriteJson, this.javarepos.toString());
			System.out.println("java " + javaCounter++);
		}
		if (this.jsrepos.length() > 0) {
			fileToWriteJson = new File(output + "/js/page-" + jsCounter + ".json");
			FileIO.writeFileContents(fileToWriteJson, this.jsrepos.toString());
			System.out.println("js " + jsCounter++);
		}
		if (this.phprepos.length() > 0) {
			fileToWriteJson = new File(output + "/php/page-" + phpCounter + ".json");
			FileIO.writeFileContents(fileToWriteJson, this.phprepos.toString());
			System.out.println("php " + phpCounter++);
		}
		if (this.scalarepos.length() > 0) {
			fileToWriteJson = new File(output + "/scala/page-" + scalaCounter + ".json");
			FileIO.writeFileContents(fileToWriteJson, this.scalarepos.toString());
			System.out.println(" scala:  " + scalaCounter++);
		}
		System.out.print("others: " + this.other);
	}
}
