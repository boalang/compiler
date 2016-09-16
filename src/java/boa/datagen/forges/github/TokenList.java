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
                this.lastUsedToken = tokenNumber;
                System.out.print("Returning Token: "+ tokenNumber);
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
            mc = new MetadataCacher(url, token.getUserName(), token.getToken());
            tokenNumber++;
            if(mc.authenticate()) {
                System.out.print("Last used token: "+ tokenNumber);
                return mc;
            }
        }
        throw new IllegalArgumentException();
    }
}
