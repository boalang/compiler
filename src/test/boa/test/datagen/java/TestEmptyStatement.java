package boa.test.datagen.java;

import java.io.IOException;

import org.junit.Test;

public class TestEmptyStatement extends Java8BaseTest {
	
	@Test
	public void emptyStatement() throws IOException {
		testWrapped(
			load("test/datagen/java/EmptyStatement.java").trim(),
			load("test/datagen/boa/EmptyStatement.boa").trim()
		);
	}
}
