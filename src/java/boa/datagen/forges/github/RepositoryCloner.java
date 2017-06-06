package boa.datagen.forges.github;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Scanner;

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

    private static  String REMOTE_URL = "";
  //  private HttpURLConnection connection = null;

    
    public static void clone(String[] args) throws IOException, InvalidRemoteException, TransportException, GitAPIException {
    	// prepare a new folder for the cloned repository
    	String localpaths=args[1];
    	String url=args[0];
    	REMOTE_URL=url;
        File localPath = new File(localpaths);
        if(!localPath.exists())
        	localPath.mkdir();
        // then clone
        Git result = null;
        try {
        	result = Git.cloneRepository()
                .setURI(REMOTE_URL)
                .setDirectory(localPath)
                .call();
	        // Note: the call() returns an opened repository already which needs to be closed to avoid file handle leaks!

            // workaround for https://bugs.eclipse.org/bugs/show_bug.cgi?id=474093
	        result.getRepository().close();
        } catch (Exception e) {
        	e.printStackTrace();
		} finally {
			if (result != null && result.getRepository() != null)
				result.getRepository().close();
		}
    }
    
    public static void main(String[] args) throws IOException, InvalidRemoteException, TransportException, GitAPIException {
      String input = args[0];
      String output= args[1];
      String tf = args[2];
      
      
      int totalFiles = Integer.parseInt(tf);
      final int MAX_NUM_THREADS = 3;
      
      int shareSize = totalFiles/MAX_NUM_THREADS;
      int start = 0;
      int end = 0;
      int i;
      for(i = 0; i < MAX_NUM_THREADS-1; i++){
          start = end;
          end = start + shareSize;
          RepositoryClonerWorker worker = new RepositoryClonerWorker(output, input, start, end);
          new Thread(worker).start();
      }
      start = end; end = totalFiles;
      RepositoryClonerWorker worker = new RepositoryClonerWorker(output, input, start, end);
      new Thread(worker).start();
    }
    
}