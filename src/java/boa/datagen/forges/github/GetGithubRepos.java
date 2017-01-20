package boa.datagen.forges.github;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

import boa.datagen.util.FileIO;

public class GetGithubRepos {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String outDir = Config.githubRepoListDir;
		String username = "isu1000000";
		String password = "isu123456";
		if (args != null && args.length > 0) {
			outDir = args[0];
			if (args.length > 2) {
				for (int i = 1; i < args.length; i++) {
					 if (args[i].equals("-a")) {
						 username = args[i+1];
						 password = args[i+2];
					 }
				}
			}
		}
		String url = "https://api.github.com/repositories";
		//System.out.println(url);
		String pageContent = "";
		//MetadataCacher mc = new MetadataCacher(url + "?since=532681", username , password);
		MetadataCacher mc = new MetadataCacher(url, username, password);
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
		if (pageNumber > 0)
			pageContent = FileIO.readFileContents(files[pageNumber-1]);
		mc = new MetadataCacher(url + "?since=" + getLastId(pageContent), username, password);
		System.out.println(mc.getUrl());
		if (mc.authenticate()) {
			System.out.println("Authentication successful!");
			int numOfRemainingRequests = mc.getNumberOfRemainingLimit();
			long time = mc.getLimitResetTime();
			while (true) {
				System.out.println(mc.getUrl());
				mc.getResponseJson();
				pageContent = mc.getContent();
				if (pageContent.equals("[]"))
					break;
				if (!pageContent.isEmpty()) {
					pageNumber++;
					//System.out.println(pageContent);
					FileIO.writeFileContents(new File(outDir + "/page-" + pageNumber + ".json"), pageContent);
					id = getLastId(pageContent);
				}
				numOfRemainingRequests--;
				int diff = (int) (System.currentTimeMillis()/1000 - time);
				if (diff > 0) {
					mc = new MetadataCacher(url + "?since=" + id, username, password);
					if (mc.authenticate())
						System.out.println("Authentication successful!");
					else
						continue;
					numOfRemainingRequests = mc.getNumberOfRemainingLimit();;
					time = mc.getLimitResetTime();
				}
				else if (numOfRemainingRequests <= 0) {
					System.out.println("Waiting " + (1-diff) + " seconds for resetting limit");
					try {
						Thread.sleep((1 - diff) * 1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					mc = new MetadataCacher(url + "?since=" + id, username, password);
					if (mc.authenticate())
						System.out.println("Authentication successful!");
					else
						continue;
					numOfRemainingRequests = mc.getNumberOfRemainingLimit();;
					time = mc.getLimitResetTime();
				}
				else {
					mc = new MetadataCacher(url + "?since=" + id, username, password);
					if (mc.authenticate())
						System.out.println("Authentication successful!");
				}
			}
		}
		else {
			System.out.println("Authentication failed!");
		}
	}

	private static String getLastId(String pageContent) {
		int start = pageContent.length();
		if (start == 0) return "0";
		while (true) {
			String p = "{\"id\":";
			start = pageContent.lastIndexOf(p, start);
			int end = pageContent.indexOf(',', start);
			String s = pageContent.substring(start + p.length(), end);
			try {
				Integer.valueOf(s);
				return s;
			} catch (NumberFormatException e) {
				// try next id
			}
		}
	}

}
