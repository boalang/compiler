package boa.test.datagen.queries;

import org.junit.Test;

public class TestBugFix extends QueryTest {
	
	@Test
	public void testBugFix() {
		String expected = "AddedNullCheck[] = 1\n";
		queryTest("test/known-good/bug-fix.boa", expected);
	}
}
