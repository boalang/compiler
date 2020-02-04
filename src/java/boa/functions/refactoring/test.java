package boa.functions.refactoring;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.eclipse.jgit.api.Git;

public class test {
	public static void main(String[] args) {
//		Git result = null;
//		try {
//			String name = "darkdiplomat/CraftingReloaded";
//			String url = "https://github.com/" + name + ".git";
//			File gitDir = new File("/Users/hyj/git/BoaData/DataGenInputRepo/" + name + "/.git");
//			File localGitDir = new File(gitDir.getAbsolutePath());
//			result = Git.cloneRepository().setURI(url).setBare(true).setDirectory(localGitDir).call();
//		} catch (Throwable t) {
//			
//		} finally {
//			if (result != null && result.getRepository() != null) {
//				result.getRepository().close();
//			}
//		}
		
		String input = "/Users/hyj/hpc_repo_json/detect/output";
		HashSet<Integer> nums = new HashSet<Integer>();
		for (int i = 0; i < 300; i++)
			nums.add(i);
		File file = new File(input);
		for (File f : file.listFiles()) {
			if (f.getName().startsWith("processed")) {
				getLines(f, processedProjects);
				String num = f.getName().replace("processed_", "").replace(".txt", "");
				nums.remove(Integer.parseInt(num));
			}
			if (f.getName().startsWith("excepted"))
				getLines(f, exceptedCommits);
		}
		for (Integer n : nums)
			System.out.println(n);
	}
	
	private static List<String> processedProjects = new ArrayList<String>();
	private static List<String> exceptedCommits = new ArrayList<String>();
	
	
	public static void getLines(File file, List<String> list) {
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(file));
			String line = reader.readLine();
			while (line != null) {
				list.add(line);
				line = reader.readLine();
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
