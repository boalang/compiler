package boa.test.datagen.java;

import java.io.IOException;

import org.junit.Test;

public class TestThisStatement extends Java8BaseTest {

	@Test
	public void thisStatement() throws IOException {
		testWrapped(
			"test/datagen/java/ThisStatement.java",
			load("test/datagen/boa/ThisStatement.boa").trim()
		);
	}
	
}
