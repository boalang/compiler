package boa.datagen.forges.github;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;

import boa.datagen.util.FileIO;
import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

public class GetGitHubRepoNames {

	public static void main(String[] args) {
		File inDir = new File(Config.githubRepoListDir);
		StringBuilder sb = new StringBuilder();
		for (File file : inDir.listFiles()) {
			if (file.getName().endsWith(".json")) {
				String jsonTxt = "";
				try {
					BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
					byte[] bytes = new byte[(int) file.length()];
					in.read(bytes);
					in.close();
					jsonTxt = new String(bytes);
				}
				catch (Exception e) {
					System.err.println("Error reading file " + file.getAbsolutePath());
					continue;
				}
				if (jsonTxt.isEmpty()) {
		        	System.err.println("File is empty " + file.getAbsolutePath());
		        	continue;
				}
				//System.out.println(jsonTxt);
		        
		        JSONArray repos = null;
		        try {
		        	repos = (JSONArray) JSONSerializer.toJSON(jsonTxt);
		        } catch (JSONException e) {
		        }
		        if (repos == null) {
		        	System.err.println("Error parsing file " + file.getAbsolutePath());
					continue;
		        }
		        for (int i = 0; i < repos.size(); i++) {
		        	//System.out.println(repos.getJSONObject(i));
		        	JSONObject repo = repos.getJSONObject(i);
		        	String id = repo.getString("id");
		        	String name = repo.getString("full_name");
		        	System.out.println(id + ": " + name);
		        	sb.append(id + "," + name + "\n");
		        	//FileIO.writeFileContents(new File(Config.githubRepoMetadataDir + "/" + id + ".json"), repo.toString());
		        }
			}
		}
		FileIO.writeFileContents(new File(inDir.getParentFile().getAbsolutePath() + "/list.csv"), sb.toString());
	}

}
