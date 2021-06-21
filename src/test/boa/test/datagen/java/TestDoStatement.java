package boa.test.datagen.java;

import java.io.IOException;

import org.junit.Test;

public class TestDoStatement extends Java8BaseTest {

	@Test
	public void doStatement() throws IOException {
		testWrapped(
			"test/datagen/java/DoStatement.java",
			load("test/datagen/boa/DoStatement.boa").trim()
		);
	}
	
	@Test
	public void doStatementWithBreak() throws IOException {
		testWrapped(
			"test/datagen/java/DoStatementWithBreak.java",
			load("test/datagen/boa/DoStatementWithBreak.boa").trim()
		);
	}
	
	@Test
	public void doStatementWithContinue() throws IOException {
		testWrapped(
			"test/datagen/java/DoStatementWithContinue.java",
			load("test/datagen/boa/DoStatementWithContinue.boa").trim()
		);
	}
}
