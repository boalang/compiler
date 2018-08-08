package boa.test.datagen.queries;

import org.junit.Test;

public class TestQ20 extends TestQueries {
	
	@Test
	public void testq20() {
		String expected = "TransientMax[] = 140492550, 3.0\n"
				+ "TransientMean[] = 0.0410958904109589\n"
				+ "TransientMin[] = 140492550, 0.0\n"
				+ "TransientTotal[] = 3\n"
				+ "VolatileMax[] = 140492550, 1.0\n"
				+ "VolatileMean[] = 0.0136986301369863\n"
				+ "VolatileMin[] = 140492550, 0.0\n"
				+ "VolatileTotal[] = 1\n";
		queryTest("test/known-good/q20.boa", expected);
	}
}
