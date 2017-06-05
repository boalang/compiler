package boa.datagen.forges.github;


import boa.datagen.util.FileIO;

import java.io.File;
import java.util.PriorityQueue;
import java.util.Timer;
import java.util.TimerTask;


public class TokenList {
    //    private ArrayList<Token> tokens = new ArrayList();
    private int lastUsedToken = 0;
    public PriorityQueue<Token> tokens = new PriorityQueue<Token>();

    public TokenList(String path) {
        String tokenDetails = FileIO.readFileContents(new File(path));
        String[] allTokens = tokenDetails.split("\n");
        String[] usrNameAndToken = null;
        int i = 0;
        for (String token : allTokens) {
            usrNameAndToken = token.split(",");
            this.tokens.add(new Token(usrNameAndToken[0], usrNameAndToken[1], i));
            i++;
        }
    }
    public Token getNextAuthenticToken(String url) {
        MetadataCacher mc = null;
        int tokenNumber = 0;

        for(Token token: tokens){
            System.out.println("Trying token: "+ tokenNumber);
            mc = new MetadataCacher(url, token.getUserName(), token.getToken());
            if(mc.authenticate()) {
                if(this.lastUsedToken != tokenNumber){
                    this.lastUsedToken = tokenNumber;
                    System.out.print("now using token: "+ tokenNumber);
                }
                return token;
            }
            tokenNumber++;
        }
        throw new IllegalArgumentException();
    }


    public MetadataLangCacher getAuthenticLangCacher(String url) throws  IllegalArgumentException{
        MetadataLangCacher mc = null;
        int tokenNumber = 0;
        for(Token token: tokens){
            System.out.println("trying: "+ tokenNumber);
            mc = new MetadataLangCacher(url, token.getUserName(), token.getToken());
            int result = mc.authenticate();
            int response = mc.getResponseCode();
            mc.getResponse();
           // System.out.println(mc.getContent());
            if(response == 403 || response == 404){
                // error in url
            	System.out.println(url + ": is not valid");
                return null;
            }else if(response/100 == 2) {
            	// successful
                    if(this.lastUsedToken != tokenNumber){
                        System.out.println("Now using Token: "+ tokenNumber);
                        this.lastUsedToken = tokenNumber;
                    }
                    return mc;
                }
            tokenNumber++;
        }
        // none of the tokens authenticated
        throw new IllegalArgumentException();
    }



    public synchronized Token getAuthenticatedToken(long threadId){
        Token tok = this.tokens.peek();
        if(this.tokens.size() <= 0){
            try{
                Thread.sleep(10000);
            }catch (InterruptedException e) {
                e.printStackTrace();
            }
            return getAuthenticatedToken(threadId);
        }
        System.out.println(threadId + " using : " + tok.getId());
        tok.setThread_id(threadId);
        this.tokens.remove(tok);
        return tok;
    }

    public synchronized void removeToken(Token tok){
        this.tokens.remove(tok);
    }

    public synchronized void addToken(Token tok){
        this.tokens.add(tok);
    }
}
