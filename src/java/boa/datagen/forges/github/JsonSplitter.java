package boa.datagen.forges.github;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class JsonSplitter {

	private int numProcesses = 24;
	private String outDir;
	private String inDir;

	public static void main(String[] args) throws IOException {
		JsonSplitter js = null;
		if (args.length > 2)
			js = new JsonSplitter(args[0], args[1], Integer.parseInt(args[2]));
		else
			js = new JsonSplitter(args[0], args[1]);
		js.split();
	}

	JsonSplitter(String inDir, String outDir, int processes) {
		this(inDir, outDir);
		this.numProcesses = processes;
	}

	public JsonSplitter(String inDir, String outDir) {
		this.inDir = inDir;
		this.outDir = outDir;
	}

	public void split() throws IOException {
		List<File> files = new ArrayList<File>();
		for (File file : new File(inDir).listFiles())
			if (file.getName().endsWith(".json"))
				files.add(file);
		int numFiles = files.size() / numProcesses;
		if (files.size() % numProcesses != 0)
			numFiles++;
		Random rand = new Random();
		for (int i = 0; i < numProcesses; i++) {
			File dir = new File(outDir + "/" + i);
			if (!dir.exists())
				dir.mkdirs();
			for (int j = 0; j < numFiles && !files.isEmpty(); j++) {
				int index = rand.nextInt(files.size());
				File file = files.remove(index);
				Files.copy(file.toPath(), new File(dir, file.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
			}
		}
	}
}
