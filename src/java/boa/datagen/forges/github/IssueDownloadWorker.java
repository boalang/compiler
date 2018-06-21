package boa.datagen.forges.github;

import java.io.File;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import boa.datagen.util.FileIO;

public class IssueDownloadWorker implements Runnable {
	private final String TOKEN_PATH;
	private final String IN_PATH;
	private final String OUT_PATH;
	private final int from;
	private final int to;
	
	
	IssueDownloadWorker(String in, String out, String token, int from, int to) {
		this.IN_PATH = in;
		this.OUT_PATH = out;
		this.TOKEN_PATH = token;
		this.from = from;
		this.to = to;
	}
	
	public void downLoadIssues() {
		File[] files = new File(IN_PATH).listFiles();
		GitHubIssuesDownloader issue = new GitHubIssuesDownloader();
		for (int i = from; i < to; i++){
			String content = FileIO.readFileContents(files[i]);
			Gson parser = new Gson();
			JsonArray repos = parser.fromJson(content, JsonElement.class).getAsJsonArray();
			for (int j = 0; j < repos.size(); i++) {
				JsonObject repo = repos.get(i).getAsJsonObject();
				String name = repo.get("full_name").getAsString();
				String id = repo.get("id").getAsString();
				String[] fullName = name.split("/");
				String projName = fullName[1];
				if ((new File(OUT_PATH + "/" + id +"-issues.json")).exists())
					continue;
				issue.issueDownloader(name, id, OUT_PATH, TOKEN_PATH);
			}
		}
	}
	
	@Override
	public void run() {
		downLoadIssues();
	}
}
