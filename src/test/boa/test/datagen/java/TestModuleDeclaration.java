package boa.test.datagen.java;

import java.io.IOException;

import org.junit.Test;

public class TestModuleDeclaration extends Java8BaseTest {

	@Test
	public void moduleDeclaration() throws IOException {
		testWrapped(
			"test/datagen/java/ModuleDeclaration.java",
			load("test/datagen/boa/ModuleDeclaration.boa").trim()
		);
	}
	
}