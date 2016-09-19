package boa.datagen.forges.github;

import java.io.File;

/**
 * Created by nmtiwari on 9/19/16.
 */
public class GithubLanguageDownloadMaster {
    public final String repoNameDir;
    public final String langNameDir;
    public final String tokenFile;
    public final static int MAX_NUM_THREADS = 3;

    public GithubLanguageDownloadMaster(String input, String output, String tokenFile){
        this.repoNameDir = input;
        this.langNameDir = output;
        this.tokenFile = tokenFile;
        File outputDir = new File(output + "/java");
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
        outputDir = new File(output + "/js");
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
        outputDir = new File(output + "/php");
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
        outputDir = new File(output + "/scala");
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
    }

    public static void main(String[] args){
        if (args.length < 3) {
            throw new IllegalArgumentException();
        }
        GithubLanguageDownloadMaster master = new GithubLanguageDownloadMaster(args[0], args[1], args[2]);
        master.orchastrate(12);
    }

    public void orchastrate(int totalFies){
        int shareSize = totalFies/this.MAX_NUM_THREADS;
        int start = 0;
        int end = 0;
        TokenList tokens = new TokenList(this.tokenFile);
        for(int i = 0 ; i < this.MAX_NUM_THREADS-1; i++){
            start = end + 1;
            end = start + shareSize;
            LanguageDownloadWorker worker = new LanguageDownloadWorker(this.repoNameDir, this.langNameDir, tokens, start, end);
            new Thread(worker).start();
        }
        start = end + 1; end = totalFies;
        LanguageDownloadWorker worker = new LanguageDownloadWorker(this.repoNameDir, this.langNameDir, tokens, start, end);
        new Thread(worker).start();
    }
}
