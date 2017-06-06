package boa.datagen.forges.github;

public class TokenTest {

	public static void main(String[] args){
		MetadataCacher mc;
		TokenList tokens = new TokenList(args[0]);
		while(tokens.size() > 0){
			Token tok = tokens.getAuthenticatedToken(Thread.currentThread().getId());
			mc = new MetadataCacher( "https://api.github.com/repositories"+ "?since=" + 0, tok.getUserName(), tok.getToken());
			mc.authenticate();
			System.out.println("Remaining requests for " + tok.getUserName() + ": " + mc.getNumberOfRemainingLimit());
			System.out.println("Time till reset: " + (mc.getLimitResetTime() * 1000 - System.currentTimeMillis()) / 1000 + " sec");
		}
	}
}
