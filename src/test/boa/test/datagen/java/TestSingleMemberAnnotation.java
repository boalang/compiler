package boa.test.datagen.java;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

public class TestSingleMemberAnnotation extends Java8BaseTest {

	@Test
	public void singleMemberAnnotation() throws IOException {
		assertEquals(
			load("test/datagen/boa/SingleMemberAnnotation.boa").trim(),
			parseJava("test/datagen/java/SingleMemberAnnotation.java").trim()
		);
		
	}
	
}
