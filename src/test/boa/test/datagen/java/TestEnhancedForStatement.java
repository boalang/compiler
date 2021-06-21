package boa.test.datagen.java;

import java.io.IOException;

import org.junit.Test;

public class TestEnhancedForStatement extends Java8BaseTest {

	@Test
	public void enhancedForStatement() throws IOException {
		testWrapped(
			"test/datagen/java/EnhancedForStatement.java",
			load("test/datagen/boa/EnhancedForStatement.boa").trim()
		);
	}
	
}
