package boa.datagen.forges.github;

import java.util.Comparator;
import java.util.Timer;
import java.util.TimerTask;

public class Token implements Comparable{
    private String userName;
    private String token;
    private int lastResponseCode;
    private long resetTime;
    private int numberOfRemainingLimit;
    private long thread_id;
    public final int id;
    private boolean available;

    public Token(String usr, String tok, int id) {
        this.userName = usr;
        this.token = tok;
        this.lastResponseCode = 202; // assigning success
        this.resetTime = 0; // there is no required reset time in start
        this.numberOfRemainingLimit = Integer.MAX_VALUE;
        this.thread_id = -1;
        this.id = id;
        this.available = true;
    }

    public int getId(){ return this.id; }


    public String getUserName() {
        return this.userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getToken() {
        return this.token;
    }

    public void setThread_id(long thread_id){
        this.thread_id = thread_id;
    }

    public long getThread_id(){ return  this.thread_id; }

    public void setToken(String token) {
        this.token = token;
    }

    public long getResetTime(){ return this.resetTime;}

    public int getLastResponseCode(){ return this.lastResponseCode;}

    public void setLastResponseCode(int code){ this.lastResponseCode = code;}

    public void setResetTime(long time){ this.resetTime = time;}

    public void setnumberOfRemainingLimit(int time){ this.numberOfRemainingLimit = time;}

    public void setAvailable(){ this.available = true;}

    public void setUnAvailable(){ this.available = false;}

    public boolean isAvailable(){ return this.available;}

    public int getNumberOfRemainingLimit(){ return this.numberOfRemainingLimit;}

//    @Override
//    public int compare(Token o1, Token o2) {
//        if(o1.getResetTime() < o2.getResetTime()){
//            return -1;
//        }else if(o1.getResetTime() > o2.getResetTime()){
//            return 1;
//        }
//        return 0;
//    }


    class UnAvailableToken extends TimerTask {
        private Token token;
        private TokenList list;
        UnAvailableToken(Token tok, TokenList list){
            this.token = tok;
            this.list = list;
        }
        @Override
        public void run() {
            System.out.println("Waiting for token: "+ this.token.getId());
            token.setAvailable();
            list.addToken(this.token);
        }
    }

    public void reset(TokenList list){
        this.thread_id = -1;
        long time = (this.getResetTime()*1000) - System.currentTimeMillis();
        System.out.println("Waiting for limit for token number: "+ this.getId() +"reset in " + (time / 1000 + 1) + "s");
        Timer timer = new Timer();
        timer.schedule(new UnAvailableToken(this, list), time);
    }

    @Override
    public int compareTo(Object o) {
        Token o2 = (Token)o;
        if(this.getResetTime() < o2.getResetTime()){
            return -1;
        }else if(this.getResetTime() > o2.getResetTime()){
            return 1;
        }
        return 0;
    }

}