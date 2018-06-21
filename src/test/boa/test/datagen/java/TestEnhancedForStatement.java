package boa.test.datagen.java;

import java.io.IOException;

import org.junit.Test;

public class TestEnhancedForStatement extends Java8BaseTest {

	@Test
	public void enhancedForStatement() throws IOException {
		testWrapped(
			load("test/datagen/java/EnhancedForStatement.java").trim(),
			load("test/datagen/boa/EnhancedForStatement.boa").trim()
		);
	}
	
}
