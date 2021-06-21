package boa.test.datagen.java;

import java.io.IOException;

import org.junit.Test;

public class TestAssignment extends Java8BaseTest {

	@Test
	public void assignment() throws IOException {
		testWrapped(
			"test/datagen/java/Assignment.java",
			load("test/datagen/boa/Assignment.boa").trim()
		);
	}
	
	@Test
	public void bitAndAssignment() throws IOException {
		testWrapped(
			"test/datagen/java/BitAndAssignment.java",
			load("test/datagen/boa/BitAndAssignment.boa").trim()
		);
	}
	
	@Test
	public void bitOrAssignment() throws IOException {
		testWrapped(
			"test/datagen/java/BitOrAssignment.java",
			load("test/datagen/boa/BitOrAssignment.boa").trim()
		);
	}
	
	@Test
	public void bitXOrAssignment() throws IOException {
		testWrapped(
			"test/datagen/java/BitXorAssignment.java",
			load("test/datagen/boa/BitXorAssignment.boa").trim()
		);
	}
	
	@Test
	public void bitDivideAssignment() throws IOException {
		testWrapped(
			"test/datagen/java/DivideAssignment.java",
			load("test/datagen/boa/DivideAssignment.boa").trim()
		);
	}
	
	@Test
	public void leftShiftAssignment() throws IOException {
		testWrapped(
			"test/datagen/java/LeftShiftAssignment.java",
			load("test/datagen/boa/LeftShiftAssignment.boa").trim()
		);
	}
	
	@Test
	public void plusAssignment() throws IOException {
		testWrapped(
			"test/datagen/java/PlusAssignment.java",
			load("test/datagen/boa/PlusAssignment.boa").trim()
		);
	}
	
	@Test
	public void minusAssignment() throws IOException {
		testWrapped(
			"test/datagen/java/MinusAssignment.java",
			load("test/datagen/boa/MinusAssignment.boa").trim()
		);
	}
	
	@Test
	public void modAssignment() throws IOException {
		testWrapped(
			"test/datagen/java/ModAssignment.java",
			load("test/datagen/boa/ModAssignment.boa").trim()
		);
	}
	
	@Test
	public void rightShiftAssignment() throws IOException {
		testWrapped(
			"test/datagen/java/RightShiftSignedAssignment.java",
			load("test/datagen/boa/RightShiftSignedAssignment.boa").trim()
		);
	}
	
	@Test
	public void unsignedRShiftAssignment() throws IOException {
		testWrapped(
			"test/datagen/java/RightShiftUnsignedAssignment.java",
			load("test/datagen/boa/RightShiftUnsignedAssignment.boa").trim()
		);
	}
	
	@Test
	public void timesAssignment() throws IOException {
		testWrapped(
			"test/datagen/java/TimesAssignment.java",
			load("test/datagen/boa/TimesAssignment.boa").trim()
		);
	}
	
}
