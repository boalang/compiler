package boa.test.datagen.java;

import java.io.IOException;

import org.junit.Test;

public class TestThrowStatement extends Java8BaseTest {

	@Test
	public void throwStatement() throws IOException {
		testWrapped(
			load("test/datagen/java/ThrowStatement.java").trim(),
			load("test/datagen/boa/ThrowStatement.boa").trim()
		);
	}
	
}
