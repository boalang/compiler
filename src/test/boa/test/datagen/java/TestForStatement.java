package boa.test.datagen.java;

import java.io.IOException;

import org.junit.Test;

public class TestForStatement extends Java8BaseTest {

	@Test
	public void forStatement() throws IOException {
		testWrapped(
			load("test/datagen/java/ForStatement.java").trim(),
			load("test/datagen/boa/ForStatement.boa").trim()
		);
	}
	
}
