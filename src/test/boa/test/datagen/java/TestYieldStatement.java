package boa.test.datagen.java;

import java.io.IOException;

import org.junit.Test;

public class TestYieldStatement extends Java8BaseTest {

	@Test
	public void yieldStatement() throws IOException {
		testWrapped(
			"test/datagen/java/YieldStatement.java",
			load("test/datagen/boa/YieldStatement.boa").trim()
		);
	}
	
}
