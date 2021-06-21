package boa.test.datagen.java;

import java.io.IOException;

import org.junit.Test;

public class TestClassInstanceCreation extends Java8BaseTest {

	@Test
	public void classInstanceCreation() throws IOException {
		testWrapped(
			"test/datagen/java/ClassInstance.java",
			load("test/datagen/boa/ClassInstance.boa").trim()
		);
	}
	
}
