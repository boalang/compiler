package boa.datagen.forges.github;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;

/**
 * Simple snippet which shows how to clone a repository from a remote source
 *
 * @author dominik.stadler at gmx.at
 */
public class RepositoryCloner {

	public static void clone(String[] args)
			throws IOException, InvalidRemoteException, TransportException, GitAPIException {
		// prepare a new folder for the cloned repository
		String localPath = args[1];
		String url = args[0];
		File localGitDir = new File(localPath + "/.git");
		// then clone
		Git result = null;
		int low = 1;
		int high = 1000;
		Random r = new Random();
		int pow = 1;

		java.lang.System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");
		boolean success = false;
		int trys = 0;
		while (success) {
			try {
				success = true;
				result = Git.cloneRepository().setURI(url).setBare(true).setDirectory(localGitDir).call();
				// Note: the call() returns an opened repository already which
				// needs
				// to be closed to avoid file handle leaks!
				// workaround for
				// https://bugs.eclipse.org/bugs/show_bug.cgi?id=474093
				result.getRepository().close();
			} catch (Exception e) {
				if (e.getMessage().contains("Authentication is required but no CredentialsProvider")) {
					continue;
				}
				if (trys < 5) {
					try {
						pow *= 2;
						Thread.sleep(((int) Math.round(pow * 1000) + (r.nextInt(high - low) + low)));
						success = false;
					} catch (InterruptedException ie) {
						// ie.printStackTrace();
					}
				}
				// e.printStackTrace();
			} finally {

				if (result != null && result.getRepository() != null)
					result.getRepository().close();
			}
		}
	}
}