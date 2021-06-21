package boa.test.datagen.java;

import java.io.IOException;

import org.junit.Test;

public class TestSynchronizedStatement extends Java8BaseTest {

	@Test
	public void synchronizedStatement() throws IOException {
		testWrapped(
			"test/datagen/java/SynchronizedStatement.java",
			load("test/datagen/boa/SynchronizedStatement.boa").trim()
		);
	}
	
}
