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

	private static String REMOTE_URL = "";
	// private HttpURLConnection connection = null;

	public static void clone(String[] args)
			throws IOException, InvalidRemoteException, TransportException, GitAPIException {
		// prepare a new folder for the cloned repository
		String localPath = args[1];
		String url = args[0];
		REMOTE_URL = url;
		File localGitDir = new File(localPath + "/.git");
		// then clone
		Git result = null;
		try {
			result = Git.cloneRepository().setURI(REMOTE_URL).setBare(true).setDirectory(localGitDir).call();
			// Note: the call() returns an opened repository already which needs
			// to be closed to avoid file handle leaks!
			// workaround for
			// https://bugs.eclipse.org/bugs/show_bug.cgi?id=474093
			result.getRepository().close();
		} catch (Exception e) {
		} finally {
			if (result != null && result.getRepository() != null)
				result.getRepository().close();
		}
	}

}