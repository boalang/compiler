package boa.test.datagen.java;

import java.io.IOException;

import org.junit.Test;

public class TestPostFixExpression extends Java8BaseTest {

	@Test
	public void postFixExpression() throws IOException {
		testWrapped(
			load("test/datagen/java/PostFixIncExpression.java").trim(),
			load("test/datagen/boa/PostFixIncExpression.boa").trim()
		);
	}
	
	@Test
	public void postFixDecExpression() throws IOException {
		testWrapped(
			load("test/datagen/java/PostFixDecExpression.java").trim(),
			load("test/datagen/boa/PostFixDecExpression.boa").trim()
		);
	}
	
}
