package boa.test.datagen.java;

import java.io.IOException;

import org.junit.Test;

public class TestReturnStatement extends Java8BaseTest {

	@Test
	public void returnNull() throws IOException {
		testWrapped(
			"test/datagen/java/ReturnNull.java",
			load("test/datagen/boa/ReturnNull.boa").trim()
		);
	}
	
	@Test
	public void returnNumberLiteral() throws IOException {
		testWrapped(
			"test/datagen/java/ReturnNumberLiteral.java",
			load("test/datagen/boa/ReturnNumberLiteral.boa").trim()
		);
	}
	
}
