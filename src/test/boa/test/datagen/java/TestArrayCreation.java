package boa.test.datagen.java;

import java.io.IOException;

import org.junit.Test;

public class TestArrayCreation extends Java8BaseTest {

	@Test
	public void arrayCreation() throws IOException {
		testWrapped(
			load("test/datagen/java/ArrayCreation.java").trim(),
			load("test/datagen/boa/ArrayCreation.boa").trim()
		);
	}
	
}
