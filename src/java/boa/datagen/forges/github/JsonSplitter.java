package boa.datagen.forges.github;

import java.io.File;

import boa.datagen.util.FileIO;

public class JsonSplitter {

	final private int Num_Threads;
	private String outDir;
	private String inDir;

	JsonSplitter(int threads, String outDir, String inDir) {
		this.Num_Threads = threads;
		this.outDir = outDir;
		this.inDir = inDir;
	}

	public static void main(String[] args) {
		JsonSplitter JS = new JsonSplitter(Integer.parseInt(args[0]), args[1], args[2]);
		JS.split();
	}

	public void split() {
		for (int i = 0; i < Num_Threads; i++) {
			File dir = new File(outDir + i);
			if (!dir.exists())
				dir.mkdirs();
		}

		File[] files = new File(inDir).listFiles();
		for (int i = 0; i < files.length; i++) {
			if (!files[i].getPath().contains(".json"))
				continue;
			int index = i % Num_Threads;
			File file = new File(outDir + index + "/" + files[i].getName());
			String content = FileIO.readFileContents(files[i]);
			System.out.println("processing " + i + "/" + files.length + " " +file.getName());
			FileIO.writeFileContents(file, content);
		}
	}
}
