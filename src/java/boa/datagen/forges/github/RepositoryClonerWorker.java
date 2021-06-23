package boa.datagen.forges.github;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Scanner;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import boa.datagen.util.FileIO;




public class RepositoryClonerWorker implements Runnable {
	private String outPath = "";
	private String inPath = "";
	private int from;
	private int to;
	private File recovory;
	private HashSet<String> names = new HashSet<String>();
	

	public RepositoryClonerWorker(String out, String in, String recovory, int from, int to) throws FileNotFoundException {
		this.outPath = out;
		this.inPath = in;
		this.from = from;
		this.to = to;
		this.recovory = new File(recovory);
		if (this.recovory.exists()){
			Scanner sc = new Scanner(this.recovory);
			while (sc.hasNext())
				names.add(sc.next());
			sc.close();
		}
	}

	public void clone(int from, int to)
			throws InvalidRemoteException, TransportException, IOException, GitAPIException {
		String urlHeader = "https://github.com/";
		String urlFooter = ".git";
		String outFilePath = "";
		File dir = new File(inPath);
		File[] files = dir.listFiles();
		for (int i = from; i < to; i++) {
			// System.out.println("Processing file number " + i );
			if (!files[i].getName().endsWith(".json")) {
				continue;
			}
			String content = FileIO.readFileContents(files[i]);
			Gson parser = new Gson();
			JsonArray repos = parser.fromJson(content, JsonElement.class).getAsJsonArray();
			for (int j = 0; j < repos.size(); j++) {
				JsonObject repo = repos.get(j).getAsJsonObject();
				String name = repo.get("full_name").getAsString();
				boolean forked = repo.get("fork").getAsBoolean();
				if (forked)
					continue;
				outFilePath = outPath + "/" + name;
				//File file = new File(outFilePath);
				if (names.contains(name)){
					names.remove(name);
					continue;
				}
				String[] args = { urlHeader + name + urlFooter, outFilePath };
				RepositoryCloner.clone(args);
				FileIO.writeFileContents(recovory, name + "/n" , true);
			}
		}
	}

	@Override
	public void run() {
		try {
			clone(from, to);
		} catch (InvalidRemoteException e) {
			e.printStackTrace();
		} catch (TransportException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (GitAPIException e) {
			e.printStackTrace();
		}
	}
}
