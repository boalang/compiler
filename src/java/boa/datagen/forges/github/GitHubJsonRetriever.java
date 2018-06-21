package boa.datagen.forges.github;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class GitHubJsonRetriever {
	private final int MAX_NUM_THREADS = 5;
	private String InputFile;
	private String TokenFile;
	private String OutPutDir;
	private ArrayList<String> namesList = new ArrayList<String>();

	public GitHubJsonRetriever(String inputFile, String tokenFile, String output) {
		InputFile = inputFile;
		TokenFile = tokenFile;
		OutPutDir = output;
	}

	public static void main(String[] args) {
		GitHubJsonRetriever master = new GitHubJsonRetriever(args[0],args[1],args[2]);
		master.buildNamesList();
		master.orchastrate();
	}

	private void buildNamesList() {
		Scanner sc = null;
		try {
			sc = new Scanner(new File(InputFile));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		while (sc.hasNextLine())
			namesList.add(sc.nextLine());
	}

	public void orchastrate() {
		TokenList tokens = new TokenList(this.TokenFile);
		GitHubJsonRetrieverWorker workers[] = new GitHubJsonRetrieverWorker[MAX_NUM_THREADS] ;
		for (int i = 0; i < MAX_NUM_THREADS; i++) {
			GitHubJsonRetrieverWorker worker = new GitHubJsonRetrieverWorker(this.OutPutDir, tokens);
			workers[i] = worker;
			new Thread(worker).start();
		}
		for (int i =0; i < namesList.size(); i++) {
			boolean nAssigned = true;
			while (nAssigned) {
				for ( int j = 0; j < MAX_NUM_THREADS; j ++) {
					if (workers[j].isReady()){
						workers[j].setName(namesList.get(i));
						new Thread(workers[j]).start();
						nAssigned = false;
						break;
					}
				}
			}
		}
	}
}
