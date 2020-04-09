package boa.datagen.forges.github;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class AddLanguageLlistToJson {
	
	private static final String R_PATH = "/Users/sumon/Research/PyDatagen/fix-metadata/json";
	private static final String W_PATH = "/Users/sumon/Research/PyDatagen/fix-metadata/json/new";
	
	private static TokenList tokens;

	public static void main(String[] args) {
		int projectCounter = 0;
		Gson parser = new Gson();
		
		tokens = new TokenList(args[0]);
		
		JSONArray poject_list = new JSONArray();
		File folder = new File(R_PATH);
		File[] listOfFiles = folder.listFiles(new FilenameFilter() {
		    public boolean accept(File dir, String name) {
		        return name.toLowerCase().endsWith(".json");
		    }
		});
		
		for (int i = 0; i < listOfFiles.length; i++) {
			String filename = listOfFiles[i].getName();
			
	        JSONParser jsonParser = new JSONParser();       
	        try (FileReader reader = new FileReader(R_PATH + "/" + filename)) {
	            Object obj = jsonParser.parse(reader);
	            JSONArray projects = (JSONArray) obj;
	            
	            for(Object o : projects) {
	            	JSONObject project = (JSONObject) o;
	            	com.google.gson.JsonObject pro = parser.fromJson(project.toJSONString(), JsonElement.class).getAsJsonObject();
	            	
	            	addLanguageToRepo(pro, parser);
	            	System.out.println("Lang");
	            	
	            	poject_list.add(pro);
	            	projectCounter ++;
	            	System.out.println("Added");

    	    		
	            	if(projectCounter % 100 == 0) {
        	    		try (FileWriter file = new FileWriter(W_PATH + projectCounter/100 + ".json", false)) {
        	    			JSONArray.writeJSONString(poject_list, file);
        	    			poject_list.clear();
        	            } catch (IOException e) {
        	                e.printStackTrace();
        	            }
        	    	}
	            	
	            }
	        } catch (FileNotFoundException e) {
	            e.printStackTrace();
	        } catch (IOException e) {
	            e.printStackTrace();
	        } catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		
		try (FileWriter file = new FileWriter(W_PATH + (projectCounter/100 + 1) + ".json", false)) {
			JSONArray.writeJSONString(poject_list, file);
        } catch (IOException e) {
            e.printStackTrace();
        }	
		System.out.println("****************** Done *********************");
	}
	
	private static void addLanguageToRepo(JsonObject repo, Gson parser) {
		String langurl = "https://api.github.com/repos/" + repo.get("full_name").getAsString() + "/languages";
		Token tok = tokens.getNextAuthenticToken("https://api.github.com/repositories");
		MetadataCacher mc = null;
		if (tok.getNumberOfRemainingLimit() <= 0) {
			tok = tokens.getNextAuthenticToken("https://api.github.com/repositories");
		}
		for (int i = 0; i < 1; i++) {
			mc = new MetadataCacher(langurl, tok.getUserName(), tok.getToken());
			boolean authnticationResult = mc.authenticate();
			if (authnticationResult) {
				mc.getResponse();
				String pageContent = mc.getContent();
				JsonObject languages = parser.fromJson(pageContent, JsonElement.class).getAsJsonObject();
				repo.add("language_list", languages);
				tok.setLastResponseCode(mc.getResponseCode());
				tok.setnumberOfRemainingLimit(mc.getNumberOfRemainingLimit());
				tok.setResetTime(mc.getLimitResetTime());
			} else {
				final int responsecode = mc.getResponseCode();
				System.err.println("authentication error " + responsecode);
				mc = new MetadataCacher("https://api.github.com/repositories", tok.getUserName(), tok.getToken());
				if (mc.authenticate()) {
					tok.setnumberOfRemainingLimit(mc.getNumberOfRemainingLimit());
				} else {
					System.out.println("token: " + tok.getId() + " exhausted");
					tok.setnumberOfRemainingLimit(0);
					i--;
				}
			}
		}

	}
}

