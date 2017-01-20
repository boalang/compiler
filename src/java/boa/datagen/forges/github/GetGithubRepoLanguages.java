package boa.datagen.forges.github;

import java.io.File;
import java.util.Scanner;

import boa.datagen.util.FileIO;

public class GetGithubRepoLanguages {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String outDir = "C:/github";
		int getterId = 1;
		if (args != null && args.length > 0) {
			outDir = args[0];
			if (args.length > 2) {
				for (int i = 1; i < args.length; i++) {
					 /*if (args[i].equals("-a")) {
						 username = args[i+1];
						 password = args[i+2];
					 }*/
					 if (args[i].equals("-g")) {
						 getterId = Integer.parseInt(args[i+1]);
					 }
				}
			}
		}
		String[] usernames = {};
		int userId = 0;
		String username = usernames[userId];
		String password = null;
		String url = "https://api.github.com";
		//String url = "https://github.com";
		//System.out.println(url);
		String pageContent = "";
		//MetadataCacher homeMc = new MetadataCacher(url, username, password);
		MetadataCacher homeMc = null;
		MetadataCacher mc = null;
		//if (homeMc.authenticate()) 
		{
			//System.out.println("Authentication successful!");
			File dir = new File(outDir + "/repo-languages");
			if (!dir.exists()) dir.mkdirs();
			int n = dir.listFiles().length;
			String content = FileIO.readFileContents(new File(outDir + "/repos.csv"));
			Scanner sc = new Scanner(content);
			for (int i = 0; i < n; i++)
				sc.nextLine();
			while (sc.hasNextLine()) {
				String line = sc.nextLine();
				System.out.println(line);
				String[] parts = line.split(",");
				int l = parts[1].length();
				while (true) {
					mc = new MetadataCacher(url + "/repos/" + parts[1].substring(1, l-1) + "/languages", username, password);
					if (!mc.authenticate()) {
						homeMc = new MetadataCacher(url, username, password);
						if (!homeMc.authenticate()) {
							//System.exit(1);
							userId = (++userId) % 10;
							username = usernames[userId];
						}
						else {
							pageContent = null;
						}
					}
					else {
						mc.getResponseJson();
						pageContent = mc.getContent();
					}
					if (pageContent == null) {
						FileIO.writeFileContents(new File(dir.getAbsolutePath() + "/" + parts[0] + ".json"), pageContent);
						break;
					}
					if (!pageContent.isEmpty()) {
						//System.out.println(pageContent);
						FileIO.writeFileContents(new File(dir.getAbsolutePath() + "/" + parts[0] + ".json"), pageContent);
						break;
					}
				}
			}
			sc.close();
		}
		/*else {
			System.out.println("Authentication failed!");
		}*/
	}
}
