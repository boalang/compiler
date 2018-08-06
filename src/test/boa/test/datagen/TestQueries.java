package boa.test.datagen;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import boa.datagen.util.FileIO;
import boa.evaluator.BoaEvaluator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;


public class TestQueries {
	
	@Test
	public void testNullCheck() {
		
	String[] args = { "-i",  "/Users/roberts/git/compiler/test/known-good/bug-fix.boa", 
					  "-d", "/Users/roberts/git/compiler/test/datagen/test_datagen",
					  "-o", "/Users/roberts/git/compiler/test/datagen/output"};	
	BoaEvaluator.main(args);	
	String expected = "AddedNullCheck[] = 1\n";
	File outputDir = new File( "/Users/roberts/git/compiler/test/datagen/output");
	String actual =  getResults(outputDir);// evaluator.getResults();
	try {
		FileUtils.deleteDirectory(outputDir);
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	assertEquals(expected, actual);
	}
	
	public String getResults(File outputDir) {
		for (final File f :  outputDir.listFiles()) {
			if (f.getName().startsWith("part")) {
				return FileIO.readFileContents(f);
			}
		}
		return "";
	}
}
