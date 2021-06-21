package boa.test.datagen.java;

import java.io.IOException;

import org.junit.Test;

public class TestAssertStatement extends Java8BaseTest {

	@Test
	public void assertStatement() throws IOException {
		testWrapped(
			"test/datagen/java/Assert.java",
			load("test/datagen/boa/Assert.boa").trim()
		);
	}
	
}
