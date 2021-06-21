package boa.test.datagen.java;

import java.io.IOException;

import org.junit.Test;

public class TestCastExpression extends Java8BaseTest {

	@Test
	public void castExpression() throws IOException {
		testWrapped(
			"test/datagen/java/CastExpression.java",
			load("test/datagen/boa/CastExpression.boa").trim()
		);
	}
	
}
