package boa.test.datagen.queries;

import org.junit.Test;

public class TestCounts2 extends QueryTest {

	@Test
	public void testCounts2() {
		String expected = "AstNodes[] = 10375\n"
				+ "Files[] = 108\n"
				+ "JLS2Files[] = 25\n"
				+ "JLS3Files[] = 50\n"
				+ "JLS4Files[] = 11\n"
				+ "JLS8Files[] = 20\n"
				+ "JavaErrorFiles[] = 1\n"
				+ "JavaFiles[] = 107\n"
				+ "JavaSnapshots[] = 165\n"
				+ "NonEmptyJavaProjects[] = 1\n"
				+ "NonEmptyProjects[] = 1\n"
				+ "Projects[] = 1\n"
				+ "Repositories[] = 1\n"
				+ "Revisions[] = 49\n"
				+ "Snapshots[] = 167\n";
		queryTest("test/known-good/counts2.boa", expected);
	}
}
