package boa.datagen.forges.github;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import boa.datagen.util.FileIO;

public class GitHubRepoJsonUpdater {

	private static String INPUT_PATH;
	private static String OUTPUT_PATH;
	private static HashSet<String> processedRepos;

	public static void main(String[] args) {
		args = new String[] { "/Users/yijiahuang/hpc_repo_json/sutton_dataset_json", 
				"/Users/yijiahuang/hpc_repo_json/updated_json",
				"/Users/yijiahuang/hpc_repo_json/myToken.txt" };
		if (args.length < 3) {
			System.out.println("args: INPUT_PATH, OUTPUT_PATH, TOKEN_PATH");
		} else {
			INPUT_PATH = args[0];
			OUTPUT_PATH = args[1];
			tokens = new TokenList(args[2]);
			File input = new File(INPUT_PATH);
			List<String> urls = getUrls(input);
			// get processed urls
			File output = new File(OUTPUT_PATH);
			processedRepos = new HashSet<String>(getUrls(output));
			System.out.println("get processed repos: " + processedRepos.size());
			for (String url : urls) {
				if (!processedRepos.contains(url))
					updateRepoJson(url);
			}
			// write the rest
			writeWith(repos.size() > 0);
//			updateRepoJson("https://github.com/ebayopensource/turmeric-repository");
		}
	}

	public static List<String> getUrls(File input) {
		List<String> urls = new ArrayList<String>();
		for (File f : input.listFiles()) {
			if (!f.getName().endsWith(".json"))
				continue;
			JsonParser parser = new JsonParser();
			String content = FileIO.readFileContents(f);
			JsonElement jsonTree = parser.parse(content);
			for (JsonElement je : jsonTree.getAsJsonArray()) {
				JsonObject jo = je.getAsJsonObject();
				urls.add(jo.get("html_url").getAsString());
			}
		}
		return urls;
	}

	private static TokenList tokens;
	private static JsonArray repos = new JsonArray();
	private static final int RECORDS_PER_FILE = 50;
	private static int jsonFileCounter = 0;
	private static int repoCounter = 0;
	private static long start, stop = 0;

	private static void updateRepoJson(String url) {
		if (!checkExistance(url)) {
			System.err.println(url + " not exist");
			return;
		}
		System.out.println("start url: " + url);
		url = url.replaceFirst("github.com/", "api.github.com/repos/");
		MetadataCacher mc = tokens.getNextAuthenticMetadataCacher(url);
		// check 404
		if (mc == null)
			return;
		System.out.println("use token user: " + mc.getUserName() + " limit: " + mc.getNumberOfRemainingLimit());
		mc.getResponseJson();
		// get Repository Json
		String content = mc.getContent();
		Gson parser = new Gson();
		JsonObject repo = null;
		repo = parser.fromJson(content, JsonElement.class).getAsJsonObject();
		// double check if already downloaded
		if (processedRepos.contains(repo.get("html_url").getAsString()))
			return;
		// add language list
		addLanguageToRepo(repo, parser);
		repos.add(repo);
		System.out.println("Add repository " + ++repoCounter + " " + repo.get("full_name").getAsString());
		
		// write to output path
		writeWith(repos.size() % RECORDS_PER_FILE == 0);
		
		if (mc.getNumberOfRemainingLimit() <= 1) {
			long t = mc.getLimitResetTime() * 1000 - System.currentTimeMillis();
			if (t >= 0) {
				System.out.println("Waiting " + (t / 1000) + " seconds for sending more requests.");
				try {
					Thread.sleep(t);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	
	private static boolean checkExistance(String repo_url) {
		Token tok = tokens.getNextAuthenticToken("https://api.github.com/repositories");
		MetadataCacher mc = new MetadataCacher(repo_url, tok.getUserName(), tok.getToken());
		return mc.authenticate();
	}
	
	private static void writeWith(boolean condition) {
		File fileToWriteJson = null;
		if (condition) {
			fileToWriteJson = new File(OUTPUT_PATH + "/page-" + jsonFileCounter + ".json");
			while (fileToWriteJson.exists()) {
				System.out.println(fileToWriteJson.getAbsolutePath() + " arleady exist");
				jsonFileCounter++;
				fileToWriteJson = new File(OUTPUT_PATH + "/page-" + jsonFileCounter + ".json");
			}
			FileIO.writeFileContents(fileToWriteJson, repos.toString());
			repos = new JsonArray();
			stop = System.currentTimeMillis();
			System.out.println("JSON File Counter: " + jsonFileCounter++ + " Time taken: " + (stop - start) / 1000.0 + " seconds");
			start = stop;
		}
	}
	

	private static void addLanguageToRepo(JsonObject repo, Gson parser) {
		System.out.println("start to add lang to json object");
		String langurl = "https://api.github.com/repos/" + repo.get("full_name").getAsString() + "/languages";
		MetadataCacher mc = tokens.getNextAuthenticMetadataCacher(langurl);
		// check not found
		if (mc == null)
			return;
		mc.getResponse();
		String pageContent = mc.getContent();
		JsonObject languages = parser.fromJson(pageContent, JsonElement.class).getAsJsonObject();
		repo.add("language_list", languages);
	}

}
