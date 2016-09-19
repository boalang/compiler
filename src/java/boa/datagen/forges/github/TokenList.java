package boa.datagen.forges.github;


import boa.datagen.util.FileIO;

import java.io.File;
import java.util.ArrayList;


public class TokenList {
    private ArrayList<Token> tokens = new ArrayList();
    private int lastUsedToken = 0;
    public ArrayList<Token> getTokens(){return this.tokens;}

    public TokenList(String path) {
        String tokenDetails = FileIO.readFileContents(new File(path));
        String[] allTokens = tokenDetails.split("\n");
        String[] usrNameAndToken = null;
        for(String token: allTokens){
            usrNameAndToken = token.split(",");
            this.tokens.add(new Token(usrNameAndToken[0], usrNameAndToken[1]));
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


    public MetadataCacher getAuthenticCacher(String url) {
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
                return mc;
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
}
