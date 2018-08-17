package boa.test.datagen.queries;

import org.junit.Test;

public class TestLen extends QueryTest {

	@Test
	public void testLen() {
		String expected = "len[140492550] = 11745\n";
		queryTest("test/known-good/len.boa", expected);
	}
}
