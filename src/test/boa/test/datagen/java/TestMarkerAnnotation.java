package boa.test.datagen.java;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

public class TestMarkerAnnotation extends Java8BaseTest {

	@Test
	public void markerAnnotation() throws IOException {
		assertEquals(
			load("test/datagen/boa/MarkerAnnotation.boa").trim(),
			parseJava("test/datagen/java/MarkerAnnotation.java").trim()
		);
		
	}
	
}
