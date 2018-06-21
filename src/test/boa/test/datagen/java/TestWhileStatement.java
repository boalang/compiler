package boa.test.datagen.java;

import java.io.IOException;

import org.junit.Test;

public class TestWhileStatement extends Java8BaseTest {

	@Test
	public void whileStatement() throws IOException {
		testWrapped(
			load("test/datagen/java/WhileStatement.java").trim(),
			load("test/datagen/boa/WhileStatement.boa").trim()
		);
	}
	
}
