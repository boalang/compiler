package boa.datagen.forges.github;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import boa.datagen.DefaultProperties;
import boa.datagen.util.FileIO;
import boa.datagen.util.Properties;

public class MetaDataMaster {
	private final boolean debug = Properties.getBoolean("debug", DefaultProperties.DEBUG);
	private final int poolSize = Integer
			.parseInt(Properties.getProperty("num.threads", DefaultProperties.NUM_THREADS));
	MetaDataWorker[] workers;

	public MetaDataMaster() {
	}

	public void downloadRepoNames(String tokenList, String outPath) {
		String outDir = Config.githubRepoListDir;
		outDir = outPath;
		TokenList tokens = new TokenList(tokenList);
		workers = new MetaDataWorker[poolSize];
		for (int i = 0; i < poolSize; i++) {
			MetaDataWorker worker = new MetaDataWorker(tokens, i);
			workers[i] = worker;
		}
		String url = "https://api.github.com/repositories";
		String pageContent = "";
		MetadataCacher mc;
		int pageNumber = 0;
		String id = "";
		File dir = new File(outDir + "/jsons");
		if (!dir.exists())
			dir.mkdirs();
		File[] files = dir.listFiles();
		Arrays.sort(files, new Comparator<File>() {
			@Override
			public int compare(File f1, File f2) {
				int n1 = getNumber(f1.getName());
				int n2 = getNumber(f2.getName());
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
					JsonArray parsedRepos = new JsonArray();
					for (int i = 0; i < repos.size(); i++) {
						JsonObject repo = repos.get(i).getAsJsonObject();
						parsedRepos.add(repo);
					}
					FileIO.writeFileContents(new File(outDir + "/jsons/page-" + pageNumber + ".json"), parsedRepos.toString());

					processMetadata(repos);
					id = getLastId(pageContent);
				}
				numOfRemainingRequests--;
				int diff = (int) (System.currentTimeMillis() / 1000 - time);
				if (diff > 0) {
					mc = new MetadataCacher(url + "?since=" + id, tok.getUserName(), tok.getToken());
					if (!mc.authenticate())
						continue;
					numOfRemainingRequests = mc.getNumberOfRemainingLimit();
					time = mc.getLimitResetTime();
				} else if (numOfRemainingRequests <= 0) {
					System.out.println("Current token got exhausted, going for next token");
					tok = tokens.getNextAuthenticToken(url + "?since=" + id);
					mc = new MetadataCacher(url + "?since=" + id, tok.getUserName(), tok.getToken());
					if (!mc.authenticate())
						continue;
					numOfRemainingRequests = mc.getNumberOfRemainingLimit();
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

		for (MetaDataWorker t : workers) {
			while (!t.isAvailable()) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					if(debug)
					e.printStackTrace();
				}
			}
			t.close(); //close writers for this worker.

			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				if(debug)
				e.printStackTrace();
			}
		}
	}

	private void processMetadata(JsonArray repos) {
		boolean assigned = false;
		while (!assigned) {
			for (int j = 0; j < poolSize; j++) {
				if (workers[j].isAvailable()) {
					workers[j].setRepos(repos);
					new Thread(workers[j]).start();
					assigned = true;
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					break;
				}

				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	private  String getLastId(String pageContent) {
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
