package boa.test.datagen.java;

import java.io.IOException;

import org.junit.Test;

public class TestInstanceOfExpression extends Java8BaseTest {

	@Test
	public void instanceOfExpression() throws IOException {
		testWrapped(
			"test/datagen/java/InstanceOfExpression.java",
			load("test/datagen/boa/InstanceOfExpression.boa").trim()
		);
	}
	
}
