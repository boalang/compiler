package boa.datagen.forges.github;

import java.io.File;
import java.io.IOException;
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
		File localGitDir = new File(localPath);
		// then clone
		Git result = null;

		java.lang.System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");
		try {
			result = Git.cloneRepository().setURI(url).setBare(true).setDirectory(localGitDir).call();
			// Note: the call() returns an opened repository already which
			// needs
			// to be closed to avoid file handle leaks!
			// workaround for
			// https://bugs.eclipse.org/bugs/show_bug.cgi?id=474093
		} finally {
			if (result != null && result.getRepository() != null) {
				result.getRepository().close();
			}
		}
	}
}