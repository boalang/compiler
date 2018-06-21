package boa.test.datagen.java;

import java.io.IOException;

import org.junit.Test;

public class TestSwitchStatement extends Java8BaseTest {

	@Test
	public void switchStatement() throws IOException {
		testWrapped(
			load("test/datagen/java/SwitchStatement.java").trim(),
			load("test/datagen/boa/SwitchStatement.boa").trim()
		);
	}
	
	@Test
	public void switchCase() throws IOException {
		testWrapped(
			load("test/datagen/java/SwitchCase.java").trim(),
			load("test/datagen/boa/SwitchCase.boa").trim()
		);
	}
	
}
