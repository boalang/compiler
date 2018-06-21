package boa.test.datagen.java;

import java.io.IOException;

import org.junit.Test;

public class TestVariableDeclarationStatement extends Java8BaseTest {

	@Test
	public void variableDeclarationStatement() throws IOException {
		testWrapped(
			load("test/datagen/java/VariableDeclarationStatement.java").trim(),
			load("test/datagen/boa/VariableDeclarationStatement.boa").trim()
		);
	}
	
}
