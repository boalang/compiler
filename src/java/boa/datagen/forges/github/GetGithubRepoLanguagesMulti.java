package boa.datagen.forges.github;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;

import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONSerializer;
import boa.datagen.util.FileIO;

public class GetGithubRepoLanguagesMulti {
	private static final int WINDOW = 1000000;

	public static void main(String[] args) throws FileNotFoundException {
		int nid = 0, nusers[] = {0};
		if (args != null && args.length > 0) {
			Config.githubRepoLanguagesDir = args[0];
			if (args.length > 2) {
				for (int i = 1; i < args.length; i++) {
					 if (args[i].equals("-nid")) {
						 nid = Integer.parseInt(args[i+1]);
					 }
					 if (args[i].equals("-nuser")) {
						 String arg = args[i+1];
						 nusers = new int[arg.length()];
						 for (int j = 0; j < arg.length(); j++) {
							 nusers[j] = Integer.parseInt(arg.substring(j, j+1));
						 }
					 }
				}
			}
		}
		File outDir = new File(Config.githubRepoLanguagesDir);
		HashSet<String> ids = new HashSet<String>();
		for (File file : outDir.listFiles()) {
			String name = file.getName();
			ids.add(name.substring(0, name.length() - 5));
		}
		String[] usernames = new String[Config.githubUsernames[0].length * nusers.length];
		long[] times = new long[usernames.length];
		for (int i = 0; i < nusers.length; i++) {
			int nuser = nusers[i];
			String[] names = Config.githubUsernames[nuser];
			for (int j = 0; j < names.length; j++) {
				usernames[i * names.length + j] = names[j];
				times[i * names.length + j] = System.currentTimeMillis() / 1000;
			}
		}
		String url = "https://api.github.com/repos";
		String pageContent = "";
		int userid = 0;
		StringBuilder sb = new StringBuilder();
		Scanner sc = new Scanner(new File(outDir.getParentFile().getAbsolutePath() + "/list.csv"));
		int i = 0;
		while (sc.hasNextLine()) {
			i++;
			String line = sc.nextLine();
			String[] parts = line.split(",");
			System.out.println(i + "\t" + parts[0]);
			if (ids.contains(parts[0]))
				continue;
			int id = Integer.parseInt(parts[0]);
			if ((id / WINDOW) % 10 != nid) {
				continue;
			}
			int l = parts[1].length();
			int trial = 0;
			while (trial < 3) {
				MetadataCacher mc = new MetadataCacher(url + "/" + parts[1].substring(1, l-1) + "/languages", usernames[userid], Config.githubPassword);
				System.out.println(mc.getUrl());
				mc.authenticate();
				mc.getResponseJson();
				pageContent = mc.getContent();
				if (!pageContent.isEmpty()) {
					for (GithubLanguage lang : getLanguages(pageContent))
						line += "," + lang.name + "," + lang.count;
					FileIO.writeFileContents(new File(Config.githubRepoLanguagesDir + "/" + id + ".json"), pageContent);
					break;
				}
				if (mc.getNumberOfRemainingLimit() > 0)
					trial++;
				else {
					times[userid] = mc.getLimitResetTime();
					userid = (userid + 1) % usernames.length;
					if (times[userid] * 1000 > System.currentTimeMillis()) {
						try {
							long time = times[userid] * 1000 - System.currentTimeMillis();
							System.out.println("Waiting for limit reset in " + (time / 1000 + 1) + "s");
							Thread.sleep(time + 1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}
			sb.append(line + "\n");
		}
		sc.close();
		FileIO.writeFileContents(new File(outDir.getParent(), "list-repo-language.csv"), sb.toString());
	}

	private static ArrayList<GithubLanguage> getLanguages(String content) {
		ArrayList<GithubLanguage> languages = new ArrayList<GithubLanguage>();
		JSONArray repos = null;
        try {
        	repos = (JSONArray) JSONSerializer.toJSON(content);
        } catch (JSONException e) {
        }
        if (repos == null) {
        	System.err.println("Error parsing file\n" + content);
			return languages;
        }
		int status = 0, s = 0;
		String name = null, count;
		for (int i = 0; i < content.length(); i++) {
			if (status == 0 && content.charAt(i) == '\"') {
				status = 1;
				s = i + 1;
			}
			else if (status == 1 && content.charAt(i) == '\"') {
				status = 2;
				name = content.substring(s, i);
			}
			else if (status == 2 && content.charAt(i) == ':') {
				status = 3;
				s = i + 1;
			}
			else if (status == 3 && !Character.isDigit(content.charAt(i))) {
				status = 0;
				count = content.substring(s, i);
				languages.add(new GithubLanguage(name, count));
			}
		}
		return languages;
	}
}

class GithubLanguage {
	String name, count;
	
	public GithubLanguage(String name, String count) {
		this.name = name;
		this.count = count;
	}
}
