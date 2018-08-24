package boa.test.datagen.queries;

import org.junit.Test;

public class TestFields extends QueryTest {

	@Test
	public void testFields() {
		String expected = "MeanInstanceFields[] = 0.0\n"
				+ "MeanPrivateFields[] = 0.0\n"
				+ "MeanStaticFields[] = 0.0\n"
				+ "NoFieldClasses[] = 62\n"
				+ "TotalClasses[] = 77\n"
				+ "TotalInstanceFields[] = 21\n"
				+ "TotalStaticFields[] = 5\n";
		queryTest("test/known-good/fields.boa", expected);
	}
}
