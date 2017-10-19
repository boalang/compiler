package boa.test.datagen;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import boa.datagen.scm.GitConnector;
import boa.datagen.util.FileIO;
import boa.test.datagen.java.Java8BaseTest;

public class TestCountChangedFiles extends Java8BaseTest {
	
	private GitConnector gc;
		
	public TestCountChangedFiles() throws IOException {
		gc = new GitConnector("D:/Projects/Boa-compiler/dataset/repos/dmlloyd/openjdk");
	}
	
	@Test
	public void countChangedFilesTest() throws IOException {
		final Map<String, Integer> counts = gc.countChangedFiles();
		List<String> l = new ArrayList<String>(counts.keySet());
		Collections.sort(l, new Comparator<String>() {
			@Override
			public int compare(String s1, String s2) {
				return counts.get(s2) - counts.get(s1);
			}
		});
		StringBuilder sb = new StringBuilder();
		for (String key : l)
			sb.append(key + "," + counts.get(key) + "\n");
		FileIO.writeFileContents(new File("dataset/changed-files.csv"), sb.toString());
	}
}
