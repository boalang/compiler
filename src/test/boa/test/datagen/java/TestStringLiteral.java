package boa.test.datagen.java;

import java.io.IOException;

import org.junit.Test;

public class TestStringLiteral extends Java8BaseTest {

	@Test
	public void stringLiteral() throws IOException {
		testWrapped(
			load("test/datagen/java/StringLiteral.java").trim(),
			load("test/datagen/boa/StringLiteral.boa").trim()
		);
	}
	
}
