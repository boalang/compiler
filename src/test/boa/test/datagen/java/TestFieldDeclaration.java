package boa.test.datagen.java;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

public class TestFieldDeclaration extends Java8BaseTest {

	@Test
	public void fieldDeclaration() throws IOException {
		assertEquals(
			load("test/datagen/boa/FieldDeclaration.boa").trim(),
			parseJava(load("test/datagen/java/FieldDeclaration.java")).trim()
		);
	}
	
}
