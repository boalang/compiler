package boa.test.datagen.java;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

public class TestAnnotationTypeDeclaration extends Java8BaseTest {

	@Test
	public void annotationTypeDeclaration() throws IOException {
		assertEquals(
			load("test/datagen/boa/AnnotationTypeDeclaration.boa").trim(),
			parseJava("test/datagen/java/AnnotationTypeDeclaration.java").trim()
		);
		
	}
	
}
