package boa.datagen.forges.github;

public class Token {
    private String userName;
    private String token;

    public Token(String usr, String tok) {
        this.userName = usr;
        this.token = tok;
    }

    public String getUserName() {
        return this.userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getToken() {
        return this.token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}