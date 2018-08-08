package boa.test.datagen.queries;

import org.junit.Test;

public class TestCatBp4 extends QueryTest {

	@Test
	public void testcatBp4() {
		String expected = "counts[] = 4.346938775510204\n";
		queryTest("test/known-good/catBp4.boa", expected);
	}
}
