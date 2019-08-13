package boa.test.datagen.java;

import java.io.IOException;

import org.junit.Test;

public class TestArrayInitializer extends Java8BaseTest {

	@Test
	public void arrayInitializer() throws IOException {
		testWrapped(
			load("test/datagen/java/ArrayInitializer.java").trim(),
			load("test/datagen/boa/ArrayInitializer.boa").trim()
		);
	}
	
}
