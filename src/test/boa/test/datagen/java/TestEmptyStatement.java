package boa.test.datagen.java;

import java.io.IOException;

import org.junit.Test;

public class TestEmptyStatement extends Java8BaseTest {
	
	@Test
	public void emptyStatement() throws IOException {
		testWrapped(
			"test/datagen/java/EmptyStatement.java",
			load("test/datagen/boa/EmptyStatement.boa").trim()
		);
	}
}
