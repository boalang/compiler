package boa.datagen.forges.github;

import boa.datagen.util.FileIO;

import java.io.File;
import java.util.PriorityQueue;
import java.util.concurrent.PriorityBlockingQueue;

public class TokenList {
	private int lastUsedToken = 0;
	public PriorityQueue<Token> tokens = new PriorityQueue<Token>();

	public int size() {
		return tokens.size();
	}

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
		while (true) {
			for (Token token : tokens) {
			//	System.out.println("Trying token: " + token.getId());
				mc = new MetadataCacher(url, token.getUserName(), token.getToken());
				if (mc.authenticate()) {
					if (this.lastUsedToken != token.getId()) {
						this.lastUsedToken = token.getId();
						System.out.println("now using token: " + token.getId());
					}
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
