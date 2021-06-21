package boa.test.datagen.java;

import java.io.IOException;

import org.junit.Test;

public class TestBooleanLiteral extends Java8BaseTest {

	@Test
	public void booleanLiteral() throws IOException {
		testWrapped(
			"test/datagen/java/BooleanLiteral.java",
			load("test/datagen/boa/BooleanLiteral.boa").trim()
		);
	}
	
}
