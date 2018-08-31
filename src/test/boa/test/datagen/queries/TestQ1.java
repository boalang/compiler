package boa.test.datagen.queries;

import org.junit.Test;

public class TestQ1 extends QueryTest {

	@Test
	public void testQ1() {
		String expected = "AbstractMax[] = 140492550, 2.0\n"
				+ "AbstractMean[] = 2.0\n"
				+ "AbstractMin[] = 140492550, 2.0\n"
				+ "AbstractTotal[] = 2\n"
				+ "ClassMax[] = 140492550, 77.0\n"
				+ "ClassMean[] = 77.0\n"
				+ "ClassMin[] = 140492550, 77.0\n"
				+ "ClassTotal[] = 77\n"
				+ "Projects[] = 1\n";
		queryTest("test/known-good/q1.boa", expected);
	}
}
