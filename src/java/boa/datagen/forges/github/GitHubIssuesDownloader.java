package boa.datagen.forges.github;

import java.io.File;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import boa.datagen.util.FileIO;

public class GitHubIssuesDownloader {

		private final static String issuesUrlHeader = "https://api.github.com/repos/";

		public static void main(String[] args) {
		//	GitHubIssuesDownloader issue = new GitHubIssuesDownloader();
		//	issue.IssueDownloader(args[0], args[1], args[2]);
			int start = 0;
			int end = 0; 
			File inDir = new File(args[0]);
			int totalFiles = inDir.listFiles().length;
			int numThreads = 5;
			int shareSize = totalFiles/numThreads;
			
			for (int i = 0; i < numThreads -1; i++ ){
				start = end;
				end = start + shareSize;
				IssueDownloadWorker worker = new IssueDownloadWorker(args[0], args[1], args[2], start, end);
				worker.run();
			}
			start = end;
			end = totalFiles;
			IssueDownloadWorker worker = new IssueDownloadWorker(args[0], args[1], args[2], start, end);
			worker.run();
		}

		public void IssueDownloader(String name, String outPath, String tokenPath) {
			JsonArray issuesRepos = new JsonArray();
			String outDir = outPath;
			TokenList tokens = new TokenList(tokenPath);
			String pageContent = "";
			MetadataCacher mc;
			String projName = name.replace("/", "-") ;
			System.out.println(projName);
			int pageNumber = 1;
			File dir = new File(outDir);
			if (!dir.exists())
				dir.mkdir();
			Token tok = null;
			tok = tokens.getNextAuthenticToken("https://api.github.com/repositories");
			mc = new MetadataCacher(issuesUrlHeader + name + "/issues?state=all&per_page=100&page=" + pageNumber, tok.getUserName(), tok.getToken());
			if (mc.authenticate()) {
				// System.out.println("Authentication successful!");
				int numOfRemainingRequests = mc.getNumberOfRemainingLimit();
				long time = mc.getLimitResetTime();
				while (true) {
					Gson parser = new Gson();
					//System.out.println(mc.getUrl());
					mc.getResponseJson();
					pageContent = mc.getContent();
					if (pageContent.equals("[]"))
						break;
					if (!pageContent.isEmpty()) {
						JsonArray repos = parser.fromJson(pageContent, JsonElement.class).getAsJsonArray();
						for (int i = 0; i < repos.size(); i++) {
							JsonObject repo = repos.get(i).getAsJsonObject();
							String title = repo.get("title").getAsString();
							int idNum = repo.get("id").getAsInt();
							int num = repo.get("number").getAsInt();
							String state = repo.get("state").getAsString();
							boolean locked = repo.get("locked").getAsBoolean();
							String user = repo.get("user").getAsJsonObject().get("login").getAsString();
							String assignee = null;
							if(!repo.get("assignee").isJsonNull()){
								assignee = repo.get("assignee").getAsJsonObject().get("login").getAsString();
							}		
							JsonArray assign = repo.get("assignees").getAsJsonArray();
							JsonArray assign2 = new JsonArray();
							for (JsonElement e: assign){
								JsonObject assignie =  new JsonObject();
								assignie.addProperty("user", ((JsonObject) e).get("login").getAsString());
								assign2.add(assignie);
							}
							JsonArray labels = repo.get("labels").getAsJsonArray();
							JsonObject milestone = new JsonObject();
							if (repo.get("milestone").isJsonNull()) {
								milestone = null;
							} else {
								milestone.addProperty("title", repo.get("milestone").getAsJsonObject().get("title").getAsString());
								milestone.addProperty("number", repo.get("milestone").getAsJsonObject().get("number").getAsInt());
							}
							JsonArray comments;
							if (repo.get("comments").getAsInt() <= 0){
								comments = null;
							}else{
								String commentsUrl = repo.get("comments_url").getAsString();
								comments = getComments(commentsUrl, tokens);
							}
							String created = repo.get("created_at").getAsString();
							String updated = repo.get("updated_at").getAsString();
							String closed;
							if (repo.get("closed_at").isJsonNull()) {
								closed = null;
							} else {
								closed = repo.get("closed_at").getAsString();
							}
							String body;
							if (repo.get("body").isJsonNull()) {
								body = null;
							} else {
								body = repo.get("body").getAsString();
							}
							String pullUrl = null;
							if(repo.has("pull_request"))
								pullUrl = repo.get("pull_request").getAsJsonObject().get("html_url").getAsString();
								
							repo = new JsonObject();
							repo.addProperty("title", title);
							repo.addProperty("id", idNum);
							repo.addProperty("number", num);
							repo.addProperty("state", state);
							repo.addProperty("locked", locked);
							repo.addProperty("user", user);
							repo.addProperty("assignee", assignee);
							repo.add("assignees", assign2);
							repo.add("comments", comments);
							repo.addProperty("created_at", created);
							repo.addProperty("updated_at", updated);
							repo.addProperty("closed_at", closed);
							repo.add("labels", labels);
							repo.add("milestone", milestone);
							repo.addProperty("body", body);
							repo.addProperty("pull_request", pullUrl);
							issuesRepos.add(repo);
						}
						pageNumber++;
					}
					numOfRemainingRequests--;
					int diff = (int) (System.currentTimeMillis() / 1000 - time);
					if (diff > 0) {
						mc = new MetadataCacher(issuesUrlHeader + name + "/issues?state=all&per_page=100&page=" + pageNumber, tok.getUserName(), tok.getToken());
						if (!mc.authenticate())
							continue;
						numOfRemainingRequests = mc.getNumberOfRemainingLimit();
						time = mc.getLimitResetTime();
					} else if (numOfRemainingRequests <= 0) {
						System.out.println("Current token got exhausted, going for next token");
						tok = tokens.getNextAuthenticToken("https://api.github.com/repositories");
						mc = new MetadataCacher(issuesUrlHeader + name + "/issues?state=all&per_page=100&page=" + pageNumber, tok.getUserName(), tok.getToken());
						if (!mc.authenticate())
							continue;
						numOfRemainingRequests = mc.getNumberOfRemainingLimit();
						time = mc.getLimitResetTime();
					} else {
						mc = new MetadataCacher(issuesUrlHeader + name + "/issues?state=all&per_page=100&page=" + pageNumber, tok.getUserName(), tok.getToken());
						if (!mc.authenticate())
							System.out.println("Authentication Failed!");
					}
				}
				FileIO.writeFileContents(new File(outDir + "/" + projName +"-issues.json"), issuesRepos.toString());
			} else {
				System.out.println("Authentication failed!");
			}
		}
		
		private JsonArray getComments(String commentUrl, TokenList tokens){
			Gson parser = new Gson();
			Token tok = tokens.getNextAuthenticToken("https://api.github.com/repositories");
			String content = "";
			MetadataCacher mc = new MetadataCacher(commentUrl, tok.getUserName(), tok.getToken());
			if(mc.authenticate()){
			mc.getResponseJson();
			content = mc.getContent();
			JsonArray response = parser.fromJson(content, JsonElement.class).getAsJsonArray();
			JsonArray comments = new JsonArray();
			for (int i = 0; i < response.size(); i++){
				JsonObject repo = response.get(i).getAsJsonObject();
				String user = repo.get("user").getAsJsonObject().get("login").getAsString();
				int id = repo.get("id").getAsInt();
				String created = repo.get("created_at").getAsString();
				String modified = repo.get("updated_at").getAsString();
				String body = repo.get("body").getAsString();
				
				repo = new JsonObject();
				repo.addProperty("user", user);
				repo.addProperty("id", id);
				repo.addProperty("created_at", created);
				repo.addProperty("updated_at", modified);
				repo.addProperty("body", body);
				comments.add(repo);
				}
				return comments;
			}
			return null;
		}
}
