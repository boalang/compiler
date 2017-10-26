package boa.test.datagen.java;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

public class TestAnnotationTypeMemberDeclaration extends Java8BaseTest {

	@Test
	public void annotationTypeMemberDeclaration() throws IOException {
		assertEquals(
			load("test/datagen/boa/AnnotationTypeMemberDeclaration.boa").trim(),
			parseJava(load("test/datagen/java/AnnotationTypeMemberDeclaration.java")).trim()
		);
		
	}
	
}
