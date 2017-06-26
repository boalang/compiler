package boa.datagen.forges.github;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import boa.datagen.util.FileIO;
import gnu.trove.set.hash.THashSet;

public class ListSplitter {
	public static void main(String[] args) {
		int NUM_OF_LISTS = 125;
		ArrayList<String> names = new ArrayList<String>();
		Random rand = new Random();
		String in = args[0];
		String out = args[1];
		int shareSize;
		File inDir = new File(in);
		File[] files = inDir.listFiles();
		System.out.println(files.length + " files");
		Gson parser = new Gson();
		JsonArray repos;
		JsonObject repo;
		for (int i = 1; i < files.length; i++) {
			System.out.println("proccessing page " + files[i].getName());
			String content = FileIO.readFileContents(files[i]);
			repos = parser.fromJson(content, JsonElement.class).getAsJsonArray();
			for (JsonElement repoE : repos) {
				repo = repoE.getAsJsonObject();
				String name = repo.get("full_name").getAsString();
				names.add(name);
			}
		}
		System.out.println(names.size() + " names");
		shareSize = (names.size())/NUM_OF_LISTS;
		System.out.println("share size = " + shareSize);
		for (int i = 0; i < NUM_OF_LISTS -1; i++){
			System.out.println("writting list " + i);
			for (int j = 0; j < shareSize; j++){
				int k = rand.nextInt(names.size());
				String name = names. get(k);
				FileIO.writeFileContents(new File(out + "/list-" + i + ".txt") , name + "\n", true);
				names.remove(k);
			}
		}
		System.out.println("names remaning " + names.size());
		while (!names.isEmpty()){
			System.out.println("writting list " + (NUM_OF_LISTS -1));
			FileIO.writeFileContents( new File(out + "/list-" + (NUM_OF_LISTS - 1)) , names.get(0) + "\n", true);
			names.remove(0);
		}
	}
}
