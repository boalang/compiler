package boa.datagen.forges.github;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import boa.datagen.util.FileIO;

public class ListSplitter {
	private static final int MAX_LIST_SIZE = 9999;

	public static void main(String[] args) {
		int numOfDuplicates = 0;
		HashSet<String> names = new HashSet<String>();
		String in = args[0];
		String out = args[1];
		int listId = Integer.parseInt(args[2]);
		File inDir = new File(in);
		File[] files = inDir.listFiles();
		System.out.println(files.length + " files");
		Gson parser = new Gson();
		JsonArray repos;
		JsonObject repo;
		for (int i = 1; i < files.length; i++) {
			System.out.println("Proccessing page " + files[i].getName());
			String content = FileIO.readFileContents(files[i]);
			repos = parser.fromJson(content, JsonElement.class).getAsJsonArray();
			for (JsonElement repoE : repos) {
				repo = repoE.getAsJsonObject();
				String name = repo.get("full_name").getAsString();
				if (names.contains(name)) {
					System.err.println("Duplicate: " + name);
					numOfDuplicates++;
				} else
					names.add(name);
			}
		}
		System.out.println("Names: " + names.size());
		System.out.println("Duplicates: " + numOfDuplicates);

		ArrayList<String> l = new ArrayList<String>(names);
		Random rand = new Random();
		int i = 0;
		StringBuilder sb = new StringBuilder();
		while (!l.isEmpty()) {
			int index = rand.nextInt(l.size());
			String name = l.remove(index);
			sb.append(name + "\n");
			i++;
			if (i >= MAX_LIST_SIZE || l.isEmpty()) {
				FileIO.writeFileContents(new File(out + "/list-" + listId + ".txt"), sb.toString());
				i = 0;
				sb = new StringBuilder();
				listId++;
			}
		}
	}
}
