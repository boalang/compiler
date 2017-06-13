package boa.datagen.forges.github;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import boa.datagen.util.FileIO;

/**
 * Created by nmtiwari on 9/19/16.
 */
public class GithubLanguageDownloadMaster {
    public final String repoNameDir;
    public final String langNameDir;
    public final String tokenFile;
    public final static int MAX_NUM_THREADS = 8;
    public static HashSet<String> names = new HashSet<String>();

    public GithubLanguageDownloadMaster(String input, String output, String tokenFile){
        this.repoNameDir = input;
        this.langNameDir = output;
        this.tokenFile = tokenFile;
        File outputDir = new File(output + "/java");
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }else{
        	addNames(output + "/java");
        }
        outputDir = new File(output + "/js");
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }else{
        	addNames(output + "/js");
        }
        outputDir = new File(output + "/php");
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }else{
        	addNames(output + "/php");
        }
        outputDir = new File(output + "/scala");
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }else{
        	addNames(output +"/scala");
        }
       outputDir = new File(output + "/other");
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }else{
        	addNames(output +"/other");
        }
        /*
        try {
			addNamesSingleFile(output+ "/names.txt");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
		*/
    }

    //when recovering use -Xmx4024m to increase heap size 
    public static void main(String[] args) throws FileNotFoundException {
        if (args.length < 3) {
            throw new IllegalArgumentException();
        }
        GithubLanguageDownloadMaster master = new GithubLanguageDownloadMaster(args[0], args[1], args[2]);
        master.orchastrate(new File(master.repoNameDir).listFiles().length);
    }

    public void orchastrate(int totalFies) throws FileNotFoundException{
        int shareSize = totalFies/MAX_NUM_THREADS;
        int start = 0;
        int end = 0;
        int i = 0;
        TokenList tokens = new TokenList(this.tokenFile);
        for(i = 0; i < MAX_NUM_THREADS-1; i++){
            start = end + 1;
            end = start + shareSize;
            LanguageDownloadWorker worker = new LanguageDownloadWorker(this.repoNameDir, this.langNameDir, tokens, start, end , i);
            new Thread(worker).start();
        }
        start = end + 1; end = totalFies;
        LanguageDownloadWorker worker = new LanguageDownloadWorker(this.repoNameDir, this.langNameDir, tokens, start, end, i);
        new Thread(worker).start();
    }

    private void addNames(String filePath){
    	System.out.println("adding " + filePath + " to names");
    	File dir = new File(filePath);
    	File[] files = dir.listFiles();
    	String content;
    	Gson parser = new Gson();
    	JsonArray repos;
    	JsonObject repo;
    	for(int i = 0; i < files.length; i++){
    		System.out.println("proccessing page " + files[i].getName());
    		content = FileIO.readFileContents(files[i]);
			repos = parser.fromJson(content, JsonElement.class).getAsJsonArray();
			for(JsonElement repoE: repos){
				repo = repoE.getAsJsonObject();
				names.add(repo.get("full_name").getAsString());
			}
    	}
    }
    
    public void addNamesSingleFile(String filePath) throws FileNotFoundException{
    	File namesFile = new File(filePath);
    	Scanner sc = new Scanner(namesFile);
    	String name = "";
    	while(sc.hasNext()){
    		name = sc.next();
    		names.add(name);
    	}
    	sc.close();
    }
}
