package boa.datagen.forges.github;

import java.io.File;
import java.io.IOException;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;


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

		java.lang.System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");
		try {
			final CredentialsProvider cp = new UsernamePasswordCredentialsProvider("user", "password");
			result = Git.cloneRepository().setCredentialsProvider(cp).setURI(url).setBare(true).setDirectory(localGitDir).call();
			// Note: the call() returns an opened repository already which
			// needs
			// to be closed to avoid file handle leaks!
			// workaround for
			// https://bugs.eclipse.org/bugs/show_bug.cgi?id=474093
			result.getRepository().close();
			/*
		} catch (Exception e) {
			System.err.println("Error cloning " + url);
			e.printStackTrace();
			*/
		} finally {
			if (result != null && result.getRepository() != null) {
//				System.out.println("Cloned repo " + url);
				result.getRepository().close();
			}
			try {
				// fall back to only TLSv1 to avoid a bug where TLS wont fall back, so servers only supports TLSv1 refuse to work
				java.lang.System.setProperty("https.protocols", "TLSv1");
				final CredentialsProvider cp = new UsernamePasswordCredentialsProvider("user", "password");
				result = Git.cloneRepository().setCredentialsProvider(cp).setURI(url).setBare(true).setDirectory(localGitDir).call();
				result.getRepository().close();
			} finally {
				if (result != null && result.getRepository() != null) {
//					System.out.println("Cloned repo " + url);
					result.getRepository().close();
				}
			}
		} 
	}
}
