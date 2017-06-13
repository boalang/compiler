package boa.datagen.forges.github;

import java.util.Timer;
import java.util.TimerTask;

public class Token implements Comparable<Token> {
	private String userName;
	private String token;
	private int lastResponseCode;
	private long resetTime;
	private int numberOfRemainingLimit;
	private long thread_id;
	public final int id;

	public Token(String usr, String tok, int id) {
		this.userName = usr;
		this.token = tok;
		this.lastResponseCode = 202; // assigning success
		this.resetTime = 0; // there is no required reset time in start
		this.numberOfRemainingLimit = Integer.MAX_VALUE;
		this.thread_id = -1;
		this.id = id;
	}

	public int getId() {
		return this.id;
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

	public void setThread_id(long thread_id) {
		this.thread_id = thread_id;
	}

	public long getThread_id() {
		return this.thread_id;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public long getResetTime() {
		return this.resetTime;
	}

	public int getLastResponseCode() {
		return this.lastResponseCode;
	}

	public void setLastResponseCode(int code) {
		this.lastResponseCode = code;
	}

	public void setResetTime(long time) {
		this.resetTime = time;
	}

	public void setnumberOfRemainingLimit(int time) {
		this.numberOfRemainingLimit = time;
	}

	public int getNumberOfRemainingLimit() {
		return this.numberOfRemainingLimit;
	}

	class UnAvailableToken extends TimerTask {
		private Token token;
		private TokenList list;

		UnAvailableToken(Token tok, TokenList list) {
			this.token = tok;
			this.list = list;
		}

		@Override
		public void run() {
			
			list.addToken(this.token);
			System.out.println("Token-" + token.getId() + " is available");
		}
	}

	public void reset(TokenList list) {
		this.thread_id = -1;
		long time = (this.getResetTime() + 1) * 1000 - System.currentTimeMillis();
		if (time <= 0)
			time = 0;
		System.out
				.println("Waiting for limit for token number: " + this.getId() + " reset in " + (time / 1000) + "s");
		Timer timer = new Timer();
		timer.schedule(new UnAvailableToken(this, list), time);
	}

	@Override
	public int compareTo(Token t) {
		if (this.getResetTime() < t.getResetTime()) {
			return -1;
		} else if (this.getResetTime() > t.getResetTime()) {
			return 1;
		}
		return 0;
	}

}