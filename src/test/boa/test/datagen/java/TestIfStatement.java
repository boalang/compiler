package boa.test.datagen.java;

import java.io.IOException;

import org.junit.Test;

public class TestIfStatement extends Java8BaseTest {

	@Test
	public void ifStatement() throws IOException {
		testWrapped(
			"test/datagen/java/IfStatement.java",
			load("test/datagen/boa/IfStatement.boa").trim()
		);
	}
	
}
