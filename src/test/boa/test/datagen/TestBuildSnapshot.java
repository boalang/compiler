package boa.test.datagen;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jgit.lib.Constants;
import org.junit.Test;

import boa.datagen.scm.GitConnector;
import boa.types.Diff.ChangedFile;

public class TestBuildSnapshot {
	@Test
	public void testBuildSnapshot() throws Exception {
		GitConnector gc = new GitConnector("D:/Projects/Boa-compiler/dataset/repos/candoia/candoia");
//		GitConnector gc = new GitConnector("D:/Projects/Boa-compiler/dataset/repos/boalang/compiler");
//		GitConnector gc = new GitConnector("F:\\testrepos\\repos-test\\hoan\\test1");
		gc.setRevisions();
		List<ChangedFile> snapshot1 = gc.buildHeadSnapshot();
		List<String> snapshot2 = gc.getSnapshot(Constants.HEAD);
		gc.close();
		Set<String> s1 = new HashSet<String>(), s2 = new HashSet<String>(snapshot2), s = new HashSet<String>(s2), in2 = new HashSet<String>(s2);
		for (ChangedFile cf : snapshot1)
			s1.add(cf.getName());
		print(s1);
		System.out.println("==========================================");
		print(s2);
		System.out.println("==========================================");
		s.retainAll(s1);
		print(s);
		System.out.println("==========================================");
		in2.removeAll(s1);
		print(in2);
		System.out.println(s1.size() + " " + s2.size() + " " + s.size() + " " + in2.size());
		assertEquals(s2,  s1);
	}

	public void print(Set<String> s) {
		List<String> l = new ArrayList<String>(s);
		Collections.sort(l);
		for (String f : l)
			System.out.println(f);
	}
}
