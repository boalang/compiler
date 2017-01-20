package boa.datagen.forges.github;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Scanner;

import boa.datagen.util.FileIO;

public class GetGithubRepoMetadataMulti {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String outDir = "/remote/rs/tien/github";
		int nid = -1, nusers[] = null;
		if (args != null && args.length > 0) {
			outDir = args[0];
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
		File outSubDir = new File(outDir + "/" + "repos-1312-1506-metadata-" + nid);
		if (!outSubDir.exists()) outSubDir.mkdirs();
		String logFileName = "repos-1312-1506-metadata-" + nid + ".csv";
		PrintStream out = null;
		try {
			out = new PrintStream(new FileOutputStream(outDir + "/" + logFileName, true));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}
		HashSet<String> ids = new HashSet<String>();
		String content = FileIO.readFileContents(new File(outDir + "/" + logFileName));
		Scanner sc = new Scanner(content);
		while (sc.hasNextLine()) {
			String line = sc.nextLine();
			String[] parts = line.split(",");
			//if (parts.length > 3)
				ids.add(parts[0]);
		}
		sc.close();
		long[] times = {System.currentTimeMillis() / 1000};
		String[] usernames = {Config.githubUsername};
		if (nusers != null) {
			usernames = new String[Config.githubUsernames[0].length * nusers.length];
			times = new long[usernames.length];
			for (int i = 0; i < nusers.length; i++) {
				int nuser = nusers[i];
				String[] names = Config.githubUsernames[nuser];
				for (int j = 0; j < names.length; j++) {
					usernames[i * names.length + j] = names[j];
					times[i * names.length + j] = System.currentTimeMillis() / 1000;
				}
			}
		}
		String url = "https://api.github.com/repos";
		String pageContent = "";
		int userid = 0;
		content = FileIO.readFileContents(new File(outDir + "/repos-1312-1506-" + nid + ".csv"));
		sc = new Scanner(content);
		int i = 0;
		while (sc.hasNextLine()) {
			i++;
			String line = sc.nextLine();
			String[] parts = line.split(",");
			System.out.println(i + "\t" + parts[0]);
			if (ids.contains(parts[0]))
				continue;
			int l = parts[1].length();
			int trial = 0;
			while (trial < 3) {
				MetadataCacher mc = new MetadataCacher(url + "/" + parts[1].substring(1, l-1), usernames[userid], Config.githubPassword);
				System.out.println(mc.getUrl());
				mc.authenticate();
				mc.getResponseJson();
				pageContent = mc.getContent();
				if (!pageContent.isEmpty()) {
					FileIO.writeFileContents(new File(outSubDir.getAbsolutePath() + "/" + parts[0] + ".json"), pageContent);
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
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
			out.println(line + "," + trial);
		}
		sc.close();
		out.close();
	}
}
