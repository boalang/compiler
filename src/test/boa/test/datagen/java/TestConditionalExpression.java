package boa.test.datagen.java;

import java.io.IOException;

import org.junit.Test;

public class TestConditionalExpression extends Java8BaseTest {

	@Test
	public void conditionalExpression() throws IOException {
		testWrapped(
			load("test/datagen/java/ConditionalExpression.java").trim(),
			load("test/datagen/boa/ConditionalExpression.boa").trim()
		);
	}
	
}
