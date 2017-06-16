package boa.datagen.forges.github;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import boa.datagen.util.FileIO;

public class ListSplitter {
	public static void main(String[] args) {
		final int NUM_OF_LISTS = 8;
		ArrayList<String> names = new ArrayList<String>();
		Random rand = new Random();
		String in = args[0];
		String out = args[1];
		int shareSize;
		File inDir = new File(in);
		File[] files = inDir.listFiles();
		Gson parser = new Gson();
		JsonArray repos;
		JsonObject repo;
		for (int i = 1; i < files.length; i++) {
			System.out.println("proccessing page " + files[i].getName());
			String content = FileIO.readFileContents(files[i]);
			repos = parser.fromJson(content, JsonElement.class).getAsJsonArray();
			for (JsonElement repoE : repos) {
				repo = repoE.getAsJsonObject();
				names.add(repo.get("full_name").getAsString());
			}
		}
		shareSize = (names.size() - 1)/NUM_OF_LISTS;
		for(int i = 0; i < NUM_OF_LISTS; i++){
			for(int j = 0; j < shareSize; j++){
				int k = rand.nextInt(names.size());
				FileIO.writeFileContents( new File(out + "/list-" + i) , names.get(k) + "\n", true);
				names.remove(k);
			}
		}
	}
}
