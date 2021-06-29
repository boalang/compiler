package boa.test.datagen.java;

import java.io.IOException;

import org.junit.Test;

public class TestSwitchStatement extends Java8BaseTest {

	@Test
	public void switchStatement() throws IOException {
		testWrapped(
			"test/datagen/java/SwitchStatement.java",
			load("test/datagen/boa/SwitchStatement.boa").trim()
		);
	}
	
	@Test
	public void switchCase() throws IOException {
		testWrapped(
			"test/datagen/java/SwitchCase.java",
			load("test/datagen/boa/SwitchCase.boa").trim()
		);
	}
	
	@Test
	public void switchCaseMultExps() throws IOException {
		testWrapped(
			"test/datagen/java/SwitchCaseMultExps.java",
			load("test/datagen/boa/SwitchCaseMultExps.boa").trim()
		);
	}
}
