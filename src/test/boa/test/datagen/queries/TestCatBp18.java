package boa.test.datagen.queries;

import org.junit.Test;

public class TestCatBp18 extends QueryTest {
	
	@Test
	public void testcatBp18() {
		String expected = "counts[] = 4.346938775510204\n";
		queryTest("test/known-good/catBp18.boa", expected);
	}
}
