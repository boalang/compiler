package boa.test.datagen.java;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

public class TestEnumDeclaration extends Java8BaseTest {

	@Test
	public void enumDeclaration() throws IOException {
		assertEquals(
			load("test/datagen/boa/EnumDeclaration.boa").trim(),
			parseJava(load("test/datagen/java/EnumDeclaration.java")).trim()
		);
	}
	
}
