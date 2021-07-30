package boa.test.datagen.java;

import java.io.IOException;

import org.junit.Test;

public class TestModuleDeclaration extends Java8BaseTest {

	@Test
	public void moduleDeclaration() throws IOException {
		testWrapped(
			"/boa/test/datagen/java/module-info.java",
			load("test/datagen/boa/ModuleDeclaration.boa").trim()
		);
	}
	
}