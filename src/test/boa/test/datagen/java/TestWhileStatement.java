package boa.test.datagen.java;

import java.io.IOException;

import org.junit.Test;

public class TestWhileStatement extends Java8BaseTest {

	@Test
	public void whileStatement() throws IOException {
		testWrapped(
			"test/datagen/java/WhileStatement.java",
			load("test/datagen/boa/WhileStatement.boa").trim()
		);
	}
	
}
