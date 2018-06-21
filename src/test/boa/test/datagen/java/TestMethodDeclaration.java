package boa.test.datagen.java;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

public class TestMethodDeclaration extends Java8BaseTest {

	@Test
	public void methodDeclaration() throws IOException {
		assertEquals(
			load("test/datagen/boa/MethodDeclaration.boa").trim(),
			parseJava(load("test/datagen/java/MethodDeclaration.java")).trim()
		);
	}
	
	@Test
	public void methodDeclaration2() throws IOException {
		assertEquals(
			load("test/datagen/boa/MethodDeclaration2.boa").trim(),
			parseJava(load("test/datagen/java/MethodDeclaration2.java")).trim()
		);
	}
}
