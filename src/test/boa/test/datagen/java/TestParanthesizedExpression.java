package boa.test.datagen.java;

import java.io.IOException;

import org.junit.Test;

public class TestParanthesizedExpression extends Java8BaseTest {

	@Test
	public void paranthesizedExpression() throws IOException {
		testWrapped(
			"test/datagen/java/ParanthesizedExpression.java",
			load("test/datagen/boa/ParanthesizedExpression.boa").trim()
		);
	}
	
}
