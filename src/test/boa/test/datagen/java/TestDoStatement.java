package boa.test.datagen.java;

import java.io.IOException;

import org.junit.Test;

public class TestDoStatement extends Java8BaseTest {

	@Test
	public void doStatement() throws IOException {
		testWrapped(
			load("test/datagen/java/DoStatement.java").trim(),
			load("test/datagen/boa/DoStatement.boa").trim()
		);
	}
	
	@Test
	public void doStatementWithBreak() throws IOException {
		testWrapped(
			load("test/datagen/java/DoStatementWithBreak.java").trim(),
			load("test/datagen/boa/DoStatementWithBreak.boa").trim()
		);
	}
	
	@Test
	public void doStatementWithContinue() throws IOException {
		testWrapped(
			load("test/datagen/java/DoStatementWithContinue.java").trim(),
			load("test/datagen/boa/DoStatementWithContinue.boa").trim()
		);
	}
}
