package boa.datagen.forges.github;

import boa.datagen.util.FileIO;
import net.sf.json.JSON;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by nmtiwari on 9/15/16.
 * This class reads repository names from a directory and downloads the languages for each repository
 * available in the directory. Point to note here is that repository names are read from github's repository
 * metadata json files.
 */
public class GithubRepositoryLanguageDownloader {
    private TokenList tokens;
    private String repository_location;
    JSONArray javarepos;
    JSONArray jsrepos;
    JSONArray phprepos;
    JSONArray scalarepos;
    String language_url_header = "https://api.github.com/repos/";
    String language_url_footer = "/languages";
    int javaCounter = 1;
    int jsCounter = 1;
    int phpCounter = 1;
    int scalaCounter = 1;
    int other = 0;
    final static int RECORDS_PER_FILE= 100;

    public GithubRepositoryLanguageDownloader(String repoPath, String tokenFile){
        this.tokens = new TokenList(tokenFile);
        this.repository_location = repoPath;
        this.javarepos = new JSONArray();
        this.jsrepos = new JSONArray();
        this.phprepos = new JSONArray();
        this.scalarepos = new JSONArray();
    }


    public static void main(String[] args){
        if(args.length < 3){
            throw new IllegalArgumentException();
        }
        File outputDir = new File(args[1]+"/java");
        if(!outputDir.exists()){
            outputDir.mkdirs();
        }
        outputDir = new File(args[1]+"/js");
        if(!outputDir.exists()){
            outputDir.mkdirs();
        }
        outputDir = new File(args[1]+"/php");
        if(!outputDir.exists()){
            outputDir.mkdirs();
        }
        outputDir = new File(args[1]+"/scala");
        if(!outputDir.exists()){
            outputDir.mkdirs();
        }

        if(args.length == 4){
            GithubRepositoryLanguageDownloader downloader = new GithubRepositoryLanguageDownloader(args[0], args[3]);
            downloader.downloadLangForRepoIn(args[0], args[1], Integer.parseInt(args[2]));
        }else{
            GithubRepositoryLanguageDownloader downloader = new GithubRepositoryLanguageDownloader(args[0],args[4]);
            try{
                downloader.downloadLangForRepoIn(args[0], args[1], Integer.parseInt(args[2]), Integer.parseInt(args[3]));
            }catch(FileNotFoundException e){
                e.printStackTrace();
            }
        }
    }

    /**
     *
     * @param path: Path of the file
     *              Given a valid file, this function downloads language_list for each repo in file at path path
     *@param output: place where augumented data should be stored
     */
    public void downloadLangForRepoIn(String path, String output) throws FileNotFoundException{
        File repoFile = new File(path);
        if(!repoFile.exists()){
            throw new FileNotFoundException();
        }
        String content = FileIO.readFileContents(repoFile);
        JSONArray repos = new JSONArray(content);
        JSONObject repo = null;
        MetadataCacher mc = null;
        for(Object obj: repos){
            repo = (JSONObject) obj;
            mc = this.tokens.getAuthenticCacher(this.language_url_header+repo.getString("full_name")+this.language_url_footer);
            if(mc == null){
                this.writeRemainingRepos(output);
                return;
            }
            mc.getResponseJson();
            String pageContent = mc.getContent();
            repo.put("language_list", pageContent);
            addRepoInReleventLangList(pageContent, output, repo);
        }
    }


    public  void downloadLangForRepoIn(String path, String output, int from){
        while(true){
            try{
                downloadLangForRepoIn(path+"/page-"+from+".json", output);
                System.out.println(" From:" + from++);
            }catch(FileNotFoundException e){
                this.writeRemainingRepos(output);
                System.out.print("others: " + this.other);
                return;
            }
        }
    }

    public  void downloadLangForRepoIn(String path, String output, int from, int to) throws FileNotFoundException {
        String fileHeader = path+"/page-";
        String fileFooter = ".json";
        int pageNumber = from;
        while(pageNumber <= to){
            File repoFile = new File(fileHeader + pageNumber + fileFooter);
            String content = FileIO.readFileContents(repoFile);
            JSONArray repos = new JSONArray(content);
            JSONObject repo = null;
            MetadataCacher mc = null;
            for(Object obj: repos){
                repo = (JSONObject) obj;
                mc = this.tokens.getAuthenticCacher(this.language_url_header+repo.getString("full_name")+this.language_url_footer);
                if(mc == null){
                    this.writeRemainingRepos(output);
                    return;
                }
                mc.getResponseJson();
                String pageContent = mc.getContent();
                repo.put("language_list", pageContent);
                addRepoInReleventLangList(pageContent, output, repo);
            }
            pageNumber++;
            System.out.println("processing: "+ pageNumber);
        }
        this.writeRemainingRepos(output);
        System.out.print("others: " + this.other);
    }

    private void addRepoInReleventLangList(String pageContent, String output, JSONObject repo){
        File fileToWriteJson = null;
        if(pageContent.contains("\"Java\":")){
            this.javarepos.put(this.javarepos.length(), repo);
            if(this.javarepos.length() % RECORDS_PER_FILE == 0){
                fileToWriteJson = new File(output+"/java/page-"+ javaCounter+".json");
                FileIO.writeFileContents(fileToWriteJson, this.javarepos.toString());
                System.out.println("java " + javaCounter++);
            }
        }else if(pageContent.contains("\"JavaScript\":")){
            this.jsrepos.put(this.jsrepos.length(), repo);
            if(this.jsrepos.length() % RECORDS_PER_FILE == 0){
                fileToWriteJson = new File(output+"/js/page-"+ jsCounter+".json");
                FileIO.writeFileContents(fileToWriteJson, this.jsrepos.toString());
                System.out.println("js " +   jsCounter++);
            }
        }else if(pageContent.contains("\"PhP\":")){
            this.phprepos.put(this.phprepos.length(), repo);
            if(this.phprepos.length() % RECORDS_PER_FILE == 0){
                fileToWriteJson = new File(output+"/php/page-"+ phpCounter+".json");
                FileIO.writeFileContents(fileToWriteJson, this.phprepos.toString());
                System.out.println("php " +  phpCounter++);
            }
        }else if(pageContent.contains("\"Scala\":")){
            this.scalarepos.put(this.scalarepos.length(), repo);
            if(this.scalarepos.length() % RECORDS_PER_FILE == 0){
                fileToWriteJson = new File(output+"/scala/page-"+ scalaCounter+".json");
                FileIO.writeFileContents(fileToWriteJson, this.scalarepos.toString());
                System.out.println(" scala: " +scalaCounter++);
            }
        }else{
            this.other++;
        }
    }

    public void writeRemainingRepos(String output){
        File fileToWriteJson = null;
        if(this.javarepos.length() > 0){
            fileToWriteJson = new File(output+"/java/page-"+ javaCounter+".json");
            FileIO.writeFileContents(fileToWriteJson, this.javarepos.toString());
            System.out.println("java " + javaCounter++);
        }
        if(this.jsrepos.length() > 0){
            fileToWriteJson = new File(output+"/js/page-"+ jsCounter+".json");
            FileIO.writeFileContents(fileToWriteJson, this.jsrepos.toString());
            System.out.println("js " +   jsCounter++);
        }
        if(this.phprepos.length() > 0){
            fileToWriteJson = new File(output+"/php/page-"+ phpCounter+".json");
            FileIO.writeFileContents(fileToWriteJson, this.phprepos.toString());
            System.out.println("php " +  phpCounter++);
        }
        if(this.scalarepos.length() > 0){
            fileToWriteJson = new File(output+"/scala/page-"+ scalaCounter+".json");
            FileIO.writeFileContents(fileToWriteJson, this.scalarepos.toString());
            System.out.println(" scala:  " +scalaCounter++);
        }
        System.out.print("others: " + this.other);
    }
}
