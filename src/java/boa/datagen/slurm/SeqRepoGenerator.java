package boa.datagen.slurm;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

//Datagen Phase 2: generate seq files for each bare repository
public class SeqRepoGenerator {

	private static String SLURM_JOB_TEMPLATE_PATH;
	private static String JSON_INPUT_PATH; // The directory contains a list of repo json files
	private static String REPO_INPUT_PATH; // The directory contains a list of bare repositories
	private static String SPLIT_JSON_PATH;
	private static String DATAGEN_JAR_PATH;
	private static String OUTPUT_PATH;
	private static int FILE_NUM_PER_JOB;

	public static void main(String[] args) {

		if (args.length < 7) {
			System.err.println("Need args:\n" + "SLURM_JOB_TEMPLATE_PATH\n" + "JSON_INPUT_PATH\n" + "REPO_INPUT_PATH\n"
					+ "SPLIT_JSON_PATH\n" + "DATAGEN_JAR_PATH\n" + "OUTPUT_PATH\n" + "FILE_NUM_PER_JOB");
			return;
		}

		SLURM_JOB_TEMPLATE_PATH = args[0];
		JSON_INPUT_PATH = args[1];
		REPO_INPUT_PATH = args[2];
		SPLIT_JSON_PATH = args[3];
		DATAGEN_JAR_PATH = args[4];
		OUTPUT_PATH = args[5];
		FILE_NUM_PER_JOB = Integer.parseInt(args[6]);

		// check split directory
		File splitDir = new File(SPLIT_JSON_PATH);
		if (splitDir.exists()) {
			System.out.println("deleted " + splitDir.getAbsolutePath());
			org.apache.commons.io.FileUtils.deleteQuietly(splitDir);
		}
		if (!splitDir.mkdir())
			System.err.println("can't make directory " + splitDir.getAbsolutePath());
		
		// split json files
		File input = new File(JSON_INPUT_PATH);
		List<String> files = new ArrayList<String>();
		int count = 0;
		for (File file : input.listFiles()) {
			if (file.getName().endsWith(".json")) {
				files.add(file.getAbsolutePath());
				if (files.size() == FILE_NUM_PER_JOB) {
					write(files, count++);
					files = new ArrayList<String>();
				}
			}
		}
		if (files.size() != 0)
			write(files, count);
		
		// run slurm job
		input = new File(SPLIT_JSON_PATH);
		for (File file : input.listFiles())
			runSlurmJob(file.getAbsolutePath());
	}

	private static void write(List<String> files, int count) {
		StringBuilder sb = new StringBuilder();
		for (String s : files)
			sb.append(s + "\n");
	    BufferedWriter writer;
	    String path = SPLIT_JSON_PATH + "/" + count + ".txt";
		try {
			writer = new BufferedWriter(new FileWriter(path));
			writer.write(sb.toString());
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void runSlurmJob(String splitPath) {
		Process p;
		try {
			List<String> cmdList = new ArrayList<String>();
			cmdList.add("sbatch");
//			cmdList.add("sh");
			cmdList.add(SLURM_JOB_TEMPLATE_PATH);
			// args
			cmdList.add(DATAGEN_JAR_PATH); // 1st arg: datagen jar path
			cmdList.add(REPO_INPUT_PATH); // 2nd arg: bare repo path
			cmdList.add(splitPath); // 3rd arg: the file contain the paths of splited json files
			cmdList.add(OUTPUT_PATH); // 4th arg: output path
			ProcessBuilder pb = new ProcessBuilder(cmdList);
			p = pb.start();

			p.waitFor();
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line;
			while ((line = reader.readLine()) != null)
				System.out.println(line);

		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
