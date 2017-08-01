package boa.datagen.forges.github;

import boa.datagen.util.FileIO;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;


/**
 * author: Nitin Mukesh Tiwari
 * GithubRepositoryNameDownloader: This class downloads all the repository names from github.com.
 * Pre-requisite: Class depends of github authentication tokens, which are provided from TokenList
 * Last used: This class was last used for May 2017 dataset creation
 */
public class GithubRepositoryNameDownloader {
	public static void main(String[] args) {
		downloadRepoNames(args);
	}

	/**
	 * @param args
	 */
	public static void downloadRepoNames(String[] args) {
		String outDir = Config.githubRepoListDir;
		if (args == null) {
			throw new IllegalArgumentException("Two argumetns: Output directory and file containing all the tokens is expected as input");
		}
		outDir = args[0];
		TokenList tokens = new TokenList(args[1]);
		String url = "https://api.github.com/repositories";
		String pageContent = "";
		MetadataCacher mc;
		int pageNumber = 0;
		String id = "";
		File dir = new File(outDir);
		if (!dir.exists())
			dir.mkdirs();
		File[] files = dir.listFiles();
		Arrays.sort(files, new Comparator<File>() {
			@Override
			public int compare(File f1, File f2) {
				int n1 = getNumber(f1.getName()), n2 = getNumber(f2.getName());
				return n1 - n2;
			}

			private int getNumber(String name) {
				String s = name.substring(5, name.length() - 5);
				return Integer.valueOf(s);
			}
		});
		pageNumber = files.length;
		Token tok = null;
		if (pageNumber > 0) {
			pageContent = FileIO.readFileContents(files[pageNumber - 1]);
			tok = tokens.getNextAuthenticToken(url + "?since=" + getLastId(pageContent));
		} else {
			tok = tokens.getNextAuthenticToken(url + "?since=" + 0);
		}

		mc = new MetadataCacher(url + "?since=" + getLastId(pageContent), tok.getUserName(), tok.getToken());
		System.out.println(mc.getUrl());
		if (mc.authenticate()) {
			// System.out.println("Authentication successful!");
			int numOfRemainingRequests = mc.getNumberOfRemainingLimit();
			long time = mc.getLimitResetTime();
			while (true) {
				Gson parser = new Gson();
				mc.getResponseJson();
				pageContent = mc.getContent();
				if (pageContent.equals("[]"))
					break;
				if (!pageContent.isEmpty()) {
					pageNumber++;
					JsonArray repos = parser.fromJson(pageContent, JsonElement.class).getAsJsonArray();
					JsonArray reducedRepos = new JsonArray();
					for (int i = 0; i < repos.size(); i++) {
						JsonObject repo = repos.get(i).getAsJsonObject();
						String name = repo.get("full_name").getAsString();
						String shortName = repo.get("name").getAsString();
						String idNum = repo.get("id").getAsString();
						String fork = repo.get("fork").getAsString();
						String homePage = repo.get("homepage").getAsString();
						String html_url = repo.get("html_url").getAsString();
						String description = repo.get("description").getAsString();
						JsonObject owner = repo.get("owner").getAsJsonObject();
						repo = new JsonObject();
						repo.addProperty("id", idNum);
						repo.addProperty("full_name", name);
						repo.addProperty("name", shortName);
						repo.add("owner", owner);
						repo.addProperty("fork", fork);
						repo.addProperty("homepage", homePage);
						repo.addProperty("html_url", html_url);
						repo.addProperty("description", description);
						reducedRepos.add(repo);
					}
					FileIO.writeFileContents(new File(outDir + "/page-" + pageNumber + ".json"),
							reducedRepos.getAsString());
					id = getLastId(pageContent);
				}
				numOfRemainingRequests--;
				int diff = (int) (System.currentTimeMillis() / 1000 - time);
				if (diff > 0) {
					mc = new MetadataCacher(url + "?since=" + id, tok.getUserName(), tok.getToken());
					if (!mc.authenticate())
						continue;
					numOfRemainingRequests = mc.getNumberOfRemainingLimit();
					;
					time = mc.getLimitResetTime();
				} else if (numOfRemainingRequests <= 0) {
					System.out.println("Current token got exhausted, going for next token");
					tok = tokens.getNextAuthenticToken(url + "?since=" + id);
					mc = new MetadataCacher(url + "?since=" + id, tok.getUserName(), tok.getToken());
					if (!mc.authenticate())
						continue;
					numOfRemainingRequests = mc.getNumberOfRemainingLimit();
					;
					time = mc.getLimitResetTime();
				} else {
					mc = new MetadataCacher(url + "?since=" + id, tok.getUserName(), tok.getToken());
					if (!mc.authenticate())
						System.out.println("Authentication Failed!");
				}
			}
		} else {
			System.out.println("Authentication failed!");
		}
	}

	private static String getLastId(String pageContent) {
		int start = pageContent.length();
		if (start == 0)
			return "0";
		while (true) {
			String p = "{\"id\":";
			start = pageContent.lastIndexOf(p, start);
			int end = pageContent.indexOf(',', start);
			String s = pageContent.substring(start + p.length(), end);
			try {
				Integer.valueOf(s);
				return s;
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
	}

}
