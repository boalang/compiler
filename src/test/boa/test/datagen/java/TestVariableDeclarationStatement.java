package boa.test.datagen.java;

import java.io.IOException;

import org.junit.Test;

public class TestVariableDeclarationStatement extends Java8BaseTest {

	@Test
	public void variableDeclarationStatement() throws IOException {
		testWrapped(
			"test/datagen/java/VariableDeclarationStatement.java",
			load("test/datagen/boa/VariableDeclarationStatement.boa").trim()
		);
	}
	
}
