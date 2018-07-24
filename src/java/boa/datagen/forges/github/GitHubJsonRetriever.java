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
		File outDir = new File(OutPutDir);
		if(!outDir.exists())
			outDir.mkdirs();
	}

	public static void main(String[] args) {
		GitHubJsonRetriever master = new GitHubJsonRetriever(args[0], args[1], args[2]);
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
		System.out.println(namesList.size() + " names");
	}

	public void orchastrate() {
		TokenList tokens = new TokenList(this.TokenFile);
		GitHubJsonRetrieverWorker workers[] = new GitHubJsonRetrieverWorker[MAX_NUM_THREADS];

		for (int i = 0; i < MAX_NUM_THREADS; i++) {
			GitHubJsonRetrieverWorker worker = new GitHubJsonRetrieverWorker(this.OutPutDir, tokens, i);
			workers[i] = worker;
		}
		int i = 0;

		while (i < namesList.size()) {
			ArrayList<String> names = new ArrayList<String>();
			for (int k = 0; k < 200 && i < namesList.size(); k++, i++) {
				names.add(namesList.get(i));
			}
			boolean nAssigned = true;
			while (nAssigned) {
				for (int j = 0; j < MAX_NUM_THREADS; j++) {
					if (workers[j].isReady()) {
						workers[j].setName(names);
						new Thread(workers[j]).start();
						nAssigned = false;
						try {
							Thread.sleep(10);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						break;
					}
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		for (i = 0; i < MAX_NUM_THREADS; i++){
			while (!workers[i].isReady()){
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			workers[i].writeRemainingRepos(OutPutDir);
		}
	}
}
