package boa.test.datagen.java;

import java.io.IOException;

import org.junit.Test;

public class TestTypeDeclarationStatement extends Java8BaseTest {

	@Test
	public void typeDeclarationStatement() throws IOException {
		testWrapped(
			"test/datagen/java/TypeDeclarationStatement.java",
			load("test/datagen/boa/TypeDeclarationStatement.boa").trim()
		);
	}
	
}
