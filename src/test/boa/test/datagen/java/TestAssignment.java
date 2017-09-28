package boa.test.datagen.java;

import java.io.IOException;

import org.junit.Test;

public class TestAssignment extends Java8BaseTest {

	@Test
	public void assignment() throws IOException {
		testWrapped(
			load("test/datagen/java/Assignment.java").trim(),
			load("test/datagen/boa/Assignment.boa").trim()
		);
	}
	
	@Test
	public void plusAssignment() throws IOException {
		testWrapped(
			load("test/datagen/java/PlusAssignment.java").trim(),
			load("test/datagen/boa/PlusAssignment.boa").trim()
		);
	}
	
	@Test
	public void minusAssignment() throws IOException {
		testWrapped(
			load("test/datagen/java/MinusAssignment.java").trim(),
			load("test/datagen/boa/MinusAssignment.boa").trim()
		);
	}
	
}
