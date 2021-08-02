package boa.test.datagen.queries;

import org.junit.Test;

public class TestAnnotNames extends QueryTest{
	
	@Test
	public void testAnnot_names() {
		String expected = "AnnotUse[FunctionalInterface] = 3\n"
				+ "AnnotUse[Override] = 11\n"
				+ "AnnotUse[Retention] = 1\n"
				+ "AnnotUse[SafeVarargs] = 1\n"
				+ "AnnotUse[SuppressWarnings] = 1\n"
				+ "AnnotUse[Target] = 2\n"
				+ "AnnotUse[ThreadSafe] = 1\n"
				+ "AnnotUse[ToDo] = 17\n";
		queryTest("test/known-good/annot-names.boa", expected);
	}
}
