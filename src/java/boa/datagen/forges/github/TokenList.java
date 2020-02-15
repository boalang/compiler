package boa.datagen.forges.github;

import boa.datagen.util.FileIO;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Scanner;

public class TokenList {
	private int lastUsedToken = 0;
	public PriorityQueue<Token> tokens = new PriorityQueue<Token>();

	public int size() {
		return tokens.size();
	}

	public TokenList(String path) {
		String tokenDetails = FileIO.readFileContents(new File(path));
		List<String> lines = new ArrayList<String>();
		Scanner sc = new Scanner(tokenDetails);
		while (sc.hasNextLine())
			lines.add(sc.nextLine());
		sc.close();
		String[] allTokens = lines.toArray(new String[0]);
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
		while (true) {
			for (Token token : tokens) {
				mc = new MetadataCacher(url, token.getUserName(), token.getToken());
				if (mc.authenticate() && mc.getNumberOfRemainingLimit() >= 1) {
					if (this.lastUsedToken != token.getId()) {
						this.lastUsedToken = token.getId();
//						System.out.println("now using token: " + token.getId());
					}
//					System.out.println("Use authentic token: " + token.getId() + " user: " + token.getUserName());
					return token;
				}
			}
			try {
				System.out.println("waiting for token, going to sleep for 10s");
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		// throw new IllegalArgumentException();
	}
	
	public MetadataCacher getNextAuthenticMetadataCacher(String url) {
		MetadataCacher mc = null;
		while (true) {
			for (Token token : tokens) {
				System.out.println("Trying token " + token.getId());
				mc = new MetadataCacher(url, token.getUserName(), token.getToken());
				if (mc.authenticate()) {
					if (this.lastUsedToken != token.getId()) {
						this.lastUsedToken = token.getId();
//						System.out.println("now using token: " + token.getId());
					}
					System.out.println("Use authentic token: " + token.getId() + " user: " + token.getUserName());
					return mc;
				}
				// the web is 404
				if (mc.getNumberOfRemainingLimit() >= 1)
					return null;
			}
			try {
				System.out.println("waiting for token, going to sleep for 10s");
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		// throw new IllegalArgumentException();
	}
	
	public Token getNextAuthenticTokenM(String url) {
		MetadataCacher mc = null;
		while (true) {
			for (Token token : tokens) {
				mc = new MetadataCacher(url, token.getUserName(), token.getToken());
				System.out.println("Trying token: " + token.getUserName() + " for " + url.substring(0, Math.min(url.length(), 40)) + ((url.length() < 40) ? "" : "..."));
				mc.authenticate();
				System.out.println(mc.getUsername() + " " + mc.isAuthenticated());
				if (mc.getNumberOfRemainingLimit() >= 0) {
					System.out.println(mc.getNumberOfRemainingLimit());
					if (this.lastUsedToken != token.getId()) {
						this.lastUsedToken = token.getId();
						System.out.println("now using token: " + token.getUserName());
					}
					return token;
				}
			}
			try {
//				long t = mc.getLimitResetTime() * 1000 - System.currentTimeMillis();
//				if (t >= 0) { // could be useful if json is created too fast
//					System.out.println("Waiting " + (t / 1000) + " seconds for sending more requests.");
//					try {
//						Thread.sleep(t);
//					} catch (InterruptedException e) {
//						e.printStackTrace();
//					}
//				}
				System.out.println("waiting for token, going to sleep for 10s");
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		// throw new IllegalArgumentException();
	}
	
	
	public Token getNextAuthenticToken(String url, int minRateLimit) {
		MetadataCacher mc = null;
		while (true) {
			for (Token token : tokens) {
				System.out.println("Trying token: " + token.getUserName() + " ");
				mc = new MetadataCacher(url, token.getUserName(), token.getToken());
				mc.authenticate();
				if (mc.isAuthenticated()) {
					int limitRemaining = mc.getNumberOfRemainingLimit();
					if (limitRemaining < minRateLimit) {
						System.out.println("Authenticated but have " + limitRemaining + " than min rate limit of " + minRateLimit);
						continue;	
					}
					if (this.lastUsedToken != token.getId()) {
						this.lastUsedToken = token.getId();
						System.out.println("Now using token: " + token.getUserName() + " ");
					}
					System.out.println(mc.getNumberOfRemainingLimit());
					return token;
				} else {
					System.err.println("Can't authenticate, response code:" + mc.getResponseCode());
				}
			}
			try {
				System.out.println("waiting for token, going to sleep for 10s");
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		// throw new IllegalArgumentException();
	}

	public synchronized Token getAuthenticatedToken(long threadId) {
		while (true) {
			Token tok = this.tokens.poll();
			if (tok == null) {
				try {
					System.out.println("thread-" + threadId + " going to sleep for 10s");
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} else {
				System.out.println(threadId + " using : " + tok.getId());
				tok.setThread_id(threadId);
				return tok;
			}
		}
	}

	public synchronized void removeToken(Token tok) {
		this.tokens.remove(tok);
	}

	public synchronized void addToken(Token tok) {
		this.tokens.add(tok);
	}
}
