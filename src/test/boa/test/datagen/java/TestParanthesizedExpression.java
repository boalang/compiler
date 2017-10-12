package boa.test.datagen.java;

import java.io.IOException;

import org.junit.Test;

public class TestParanthesizedExpression extends Java8BaseTest {

	@Test
	public void paranthesizedExpression() throws IOException {
		testWrapped(
			load("test/datagen/java/ParanthesizedExpression.java").trim(),
			load("test/datagen/boa/ParanthesizedExpression.boa").trim()
		);
	}
	
}
