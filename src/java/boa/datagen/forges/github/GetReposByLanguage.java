package boa.datagen.forges.github;

import java.io.File;
import java.util.HashSet;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import boa.datagen.util.FileIO;


public class GetReposByLanguage {

	public static void main(String[] args) {
		HashSet<String> names = new HashSet<>();
		String time = "2018-09-18T01:01:01Z";
		Gson parser = new Gson();
		while (true){
			String url = "https://api.github.com/search/repositories?q=language:java+stars:>1+pushed:<=" + time + "&sort=updated&order=desc&per_page=100";
			System.out.println(url);
			MetadataCacher mc = new MetadataCacher(url, "", "");
			mc.authenticate();
			//System.out.println(mc.isAuthenticated() + " - " + mc.getNumberOfMaxLimit() + " - " + mc.getNumberOfRemainingLimit() + " - " + (mc.getLimitResetTime() - System.currentTimeMillis()/1000));
			while (!mc.isAuthenticated() || mc.getNumberOfRemainingLimit() <= 0) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				mc = new MetadataCacher(url, "", "");
				mc.authenticate();
			}
			mc.getResponseJson();
			String content = mc.getContent();
			//System.out.println(content);
			
			JsonObject json = null;
			json = parser.fromJson(content, JsonElement.class).getAsJsonObject();
	        JsonArray items = json.getAsJsonArray("items");
	        if (items.size() > 0) {
		        for (int j = 0; j < items.size(); j++) {
		        	JsonObject item = items.get(j).getAsJsonObject();
		        	FileIO.writeFileContents(new File("/users/roberts/Documents/github/" + item.get("id").getAsString()), item.toString());
		        	String name = item.get("full_name").getAsString();
		        	//System.out.println(name);
		        	names.add(name);
		        	String pushed = item.get("pushed_at").getAsString();
		        	System.out.println(pushed);
		        	if (pushed.compareTo(time) < 0)
		        		time = pushed;
		        }
	        }
	        int count = json.get("total_count").getAsInt();
	        if (count == items.size())
	        	break;
			if (mc.getNumberOfRemainingLimit() <= 1) {
				long t = mc.getLimitResetTime() * 1000 - System.currentTimeMillis();
				if (t >= 0) {
					System.out.println("Waiting " + (t/1000) + " seconds for sending more requests.");
					try {
						Thread.sleep(t);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
		StringBuilder sb = new StringBuilder();
    	for (String name : names)
    		sb.append(name + "\n");
        FileIO.writeFileContents(new File("/users/roberts/Documents/github/Java-stars-1.txt"), sb.toString());
	}

}
