package boa.test.datagen.java;

import java.io.IOException;

import org.junit.Test;

public class TestAnonymousClassDeclaration extends Java8BaseTest {

	@Test
	public void anonymousClassDeclaration() throws IOException {
		testWrapped(
			load("test/datagen/java/AnonymousClassDeclaration.java").trim(),
			load("test/datagen/boa/AnonymousClassDeclaration.boa").trim()
		);
	}
	
}
