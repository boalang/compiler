package boa.test.datagen.queries;

import java.io.File;

import org.junit.Test;

import boa.datagen.util.FileIO;

public class TestCatBp18 extends TestQueries {
	
	@Test
	public void testcatBp18() {
		String expected = "counts[] = 4.346938775510204\n";
		queryTest("test/known-good/catBp18.boa", expected);
	}
	
	
}
