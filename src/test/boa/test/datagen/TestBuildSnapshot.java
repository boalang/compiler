package boa.test.datagen;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jgit.lib.Constants;
import org.junit.Ignore;
import org.junit.Test;

import boa.datagen.DefaultProperties;
import boa.datagen.forges.github.RepositoryCloner;
import boa.datagen.scm.AbstractCommit;
import boa.datagen.scm.GitConnector;
import boa.types.Diff.ChangedFile;

public class TestBuildSnapshot {
	
	@Test
	public void testBuildSnapshot() throws Exception {
		DefaultProperties.DEBUG = true;
		
		String[] repoNames = new String[]{"candoia/candoia", "boalang/compiler", "junit-team/junit4"};
		for (String repoName : repoNames) {
			System.out.println("Repo: " + repoName);
			File gitDir = new File("dataset/repos/" + repoName);
			RepositoryCloner.clone(new String[]{"https://github.com/" + repoName + ".git", gitDir.getAbsolutePath()});
			GitConnector gc = new GitConnector(gitDir.getAbsolutePath());
			gc.setRevisions();
			System.out.println("Finish processing commits");
			List<ChangedFile> snapshot1 = new ArrayList<ChangedFile>();
			List<AbstractCommit> commits = new ArrayList<AbstractCommit>();
			gc.getSnapshot(gc.getHeadCommitOffset(), snapshot1, commits);
			System.out.println("Finish building head snapshot");
			List<String> snapshot2 = gc.getSnapshot(Constants.HEAD);
			Set<String> s1 = new HashSet<String>(), s2 = new HashSet<String>(snapshot2), s = new HashSet<String>(s2), in1 = new HashSet<String>(s1), in2 = new HashSet<String>(s2);
			for (ChangedFile cf : snapshot1)
				s1.add(cf.getName());
			if (!s1.equals(s2)) {
				s = new HashSet<String>(s2);
				in1 = new HashSet<String>(s1);
				in2 = new HashSet<String>(s2);
//				print(s1);
//				print(s2);
				s.retainAll(s1);
//				print(s);
				in1.removeAll(s2);
				print(in1, snapshot1, commits);
				in2.removeAll(s1);
				print(in2, new ArrayList<ChangedFile>(), commits);
				System.out.println("Head: " + s1.size() + " " + s2.size() + " " + s.size() + " " + in1.size() + " " + in2.size());
			} else 
				System.out.println("Head: " + s1.size());
			assertEquals(s2, s1);
			for (int i = gc.getRevisions().size()-1; i >= 0; i--) {
				AbstractCommit commit = gc.getRevisions().get(i);
				snapshot1 = new ArrayList<ChangedFile>();
				gc.getSnapshot(i, snapshot1, new ArrayList<AbstractCommit>());
				snapshot2 = gc.getSnapshot(commit.getId());
				s1 = new HashSet<String>();
				s2 = new HashSet<String>(snapshot2);
				for (ChangedFile cf : snapshot1)
					s1.add(cf.getName());
				if (!s1.equals(s2)) {
					s = new HashSet<String>(s2);
					in1 = new HashSet<String>(s1);
					in2 = new HashSet<String>(s2);
//					print(s1);
//					print(s2);
					s.retainAll(s1);
//					print(s);
					in1.removeAll(s2);
					print(in1, snapshot1, commits);
					in2.removeAll(s1);
					print(in2, new ArrayList<ChangedFile>(), commits);
					System.out.println("Commit " + commit.getId() + ": " + s1.size() + " " + s2.size() + " " + s.size() + " " + in1.size() + " " + in2.size());
				}/* else 
					System.out.println("Commit " + commit.getId() + ": " + s1.size());*/
				assertEquals(s2, s1);
			}
			gc.close();
		}
	}
	
	@Ignore
	@Test
	public void testBuildSnapshotWithTypes() throws Exception {
		DefaultProperties.DEBUG = true;
		
		File gitDir = new File("D:/Projects/Boa-compiler/dataset/repos/candoia/candoia");
//		File gitDir = new File("D:/Projects/Boa-compiler/dataset/repos/boalang/compiler");
//		File gitDir = new File("F:\\testrepos\\repos-test\\hoan\\test1");
		if (!gitDir.exists())
			return;
		GitConnector gc = new GitConnector(gitDir.getAbsolutePath());
		gc.setRevisions();
		System.out.println("Finish processing commits");
		List<ChangedFile> snapshot1 = gc.buildHeadSnapshot(new String[]{"java"}, null);
		System.out.println("Finish building head snapshot");
		List<String> snapshot2 = gc.getSnapshot(Constants.HEAD);
		gc.close();
		Set<String> s1 = new HashSet<String>(), s2 = new HashSet<String>(snapshot2), s = new HashSet<String>(s2), in2 = new HashSet<String>(s2);
		for (ChangedFile cf : snapshot1)
			s1.add(cf.getName());
//		print(s1);
//		print(s2);
//		s.retainAll(s1);
//		print(s);
//		in2.removeAll(s1);
//		print(in2);
		System.out.println(s1.size() + " " + s2.size() + " " + s.size() + " " + in2.size());
		assertEquals(s2,  s1);
	}

	public void print(Set<String> s, List<ChangedFile> snapshot, List<AbstractCommit> commits) {
		List<String> l = new ArrayList<String>(s);
		Collections.sort(l);
		for (String f : l)
			System.out.println(f + " " + commits.get(indexOf(snapshot, f)).getId());
		System.out.println("==========================================");
	}

	private int indexOf(List<ChangedFile> snapshot, String name) {
		for (int i = 0; i < snapshot.size(); i++)
			if (snapshot.get(i).getName().equals(name))
				return i;
		return -1;
	}
}
