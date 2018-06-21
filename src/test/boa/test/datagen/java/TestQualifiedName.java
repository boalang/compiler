package boa.test.datagen.java;

import java.io.IOException;

import org.junit.Test;

public class TestQualifiedName extends Java8BaseTest {

	@Test
	public void qualifiedName() throws IOException {
		testWrapped(
			load("test/datagen/java/QualifiedName.java").trim(),
			load("test/datagen/boa/QualifiedName.boa").trim()
		);
	}
	
}
