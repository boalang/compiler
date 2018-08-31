package boa.test.datagen.queries;

import org.junit.Test;

public class Testq12 extends QueryTest{

	@Test
	public void testQ12() {
		String expected = "StaticMethodsMax[] = 140492550, 2.0\n"
				+ "StaticMethodsMean[] = 0.45454545454545453\n"
				+ "StaticMethodsMin[] = 140492550, 0.0\n"
				+ "StaticMethodsTotal[] = 35\n";
		queryTest("test/known-good/q12.boa", expected);
	}
}
