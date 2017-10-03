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
	public void bitAndAssignment() throws IOException {
		testWrapped(
			load("test/datagen/java/BitAndAssignment.java").trim(),
			load("test/datagen/boa/BitAndAssignment.boa").trim()
		);
	}
	
	@Test
	public void bitOrAssignment() throws IOException {
		testWrapped(
			load("test/datagen/java/BitOrAssignment.java").trim(),
			load("test/datagen/boa/BitOrAssignment.boa").trim()
		);
	}
	
	@Test
	public void bitXOrAssignment() throws IOException {
		testWrapped(
			load("test/datagen/java/BitXorAssignment.java").trim(),
			load("test/datagen/boa/BitXorAssignment.boa").trim()
		);
	}
	
	@Test
	public void bitDivideAssignment() throws IOException {
		testWrapped(
			load("test/datagen/java/DivideAssignment.java").trim(),
			load("test/datagen/boa/DivideAssignment.boa").trim()
		);
	}
	
	@Test
	public void lShiftAssignment() throws IOException {
		testWrapped(
			load("test/datagen/java/LeftShiftAssignment.java").trim(),
			load("test/datagen/boa/LeftShiftAssignment.boa").trim()
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
	
	@Test
	public void modAssignment() throws IOException {
		testWrapped(
			load("test/datagen/java/ModAssignment.java").trim(),
			load("test/datagen/boa/ModAssignment.boa").trim()
		);
	}
	
	@Test
	public void rShiftAssignment() throws IOException {
		testWrapped(
			load("test/datagen/java/RightShiftSignedAssignment.java").trim(),
			load("test/datagen/boa/RightShiftSignedAssignment.boa").trim()
		);
	}
	
	@Test
	public void UnsignedRShiftAssignment() throws IOException {
		testWrapped(
			load("test/datagen/java/RightShiftUnsignedAssignment.java").trim(),
			load("test/datagen/boa/RightShiftUnsignedAssignment.boa").trim()
		);
	}
	
	@Test
	public void TimesAssignment() throws IOException {
		testWrapped(
			load("test/datagen/java/TimesAssignment.java").trim(),
			load("test/datagen/boa/TimesAssignment.boa").trim()
		);
	}
	
}
