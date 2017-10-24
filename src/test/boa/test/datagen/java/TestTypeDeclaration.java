package boa.test.datagen.java;

import java.io.IOException;

import org.junit.Test;

public class TestTypeDeclaration extends Java8BaseTest {

	@Test
	public void typeDeclaration() throws IOException {
		testWrapped(
			load("test/datagen/java/TypeDeclaration.java").trim(),
			load("test/datagen/boa/TypeDeclaration.boa").trim()
		);
	}
	
}
