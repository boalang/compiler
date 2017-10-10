package boa.test.datagen.java;

import java.io.IOException;

import org.junit.Test;

public class TestInfixExpression extends Java8BaseTest {

	@Test
	public void infixExpressionAnd() throws IOException {
		testWrapped(
			load("test/datagen/java/InfixExpressionAnd.java").trim(),
			load("test/datagen/boa/InfixExpressionAnd.boa").trim()
		);
	}
	
	@Test
	public void infixExpressionLogicalAnd() throws IOException {
		testWrapped(
			load("test/datagen/java/InfixExpressionLogicalAnd.java").trim(),
			load("test/datagen/boa/InfixExpressionLogicalAnd.boa").trim()
		);
	}
	
	@Test
	public void infixExpressionLogicalOr() throws IOException {
		testWrapped(
			load("test/datagen/java/InfixExpressionLogicalOr.java").trim(),
			load("test/datagen/boa/InfixExpressionLogicalOr.boa").trim()
		);
	}
	
	@Test
	public void infixExpressionDivide() throws IOException {
		testWrapped(
			load("test/datagen/java/InfixExpressionDivide.java").trim(),
			load("test/datagen/boa/InfixExpressionDivide.boa").trim()
		);
	}
	
	@Test
	public void infixExpressionEquals() throws IOException {
		testWrapped(
			load("test/datagen/java/InfixExpressionEquals.java").trim(),
			load("test/datagen/boa/InfixExpressionEquals.boa").trim()
		);
	}
	
	@Test
	public void infixExpressionGreaterThen() throws IOException {
		testWrapped(
			load("test/datagen/java/InfixExpressionGreaterThen.java").trim(),
			load("test/datagen/boa/InfixExpressionGreaterThen.boa").trim()
		);
	}
	
	@Test
	public void infixExpressionGreaterEqual() throws IOException {
		testWrapped(
			load("test/datagen/java/InfixExpressionGreaterEqual.java").trim(),
			load("test/datagen/boa/InfixExpressionGreaterEqual.boa").trim()
		);
	}
	
	@Test
	public void infixExpressionLeftShift() throws IOException {
		testWrapped(
			load("test/datagen/java/InfixExpressionLeftShift.java").trim(),
			load("test/datagen/boa/InfixExpressionLeftShift.boa").trim()
		);
	}
	
	@Test
	public void infixExpressionLessThen() throws IOException {
		testWrapped(
			load("test/datagen/java/InfixExpressionLessThen.java").trim(),
			load("test/datagen/boa/InfixExpressionLessThen.boa").trim()
		);
	}
	
	@Test
	public void infixExpressionLessEqual() throws IOException {
		testWrapped(
			load("test/datagen/java/InfixExpressionLessEqual.java").trim(),
			load("test/datagen/boa/InfixExpressionLessEqual.boa").trim()
		);
	}
	
	@Test
	public void infixExpressionMinus() throws IOException {
		testWrapped(
			load("test/datagen/java/InfixExpressionMinus.java").trim(),
			load("test/datagen/boa/InfixExpressionMinus.boa").trim()
		);
	}
	
	@Test
	public void infixExpressionNotEqual() throws IOException {
		testWrapped(
			load("test/datagen/java/InfixExpressionNotEqual.java").trim(),
			load("test/datagen/boa/InfixExpressionNotEqual.boa").trim()
		);
	}
	
	@Test
	public void infixExpressionOr() throws IOException {
		testWrapped(
			load("test/datagen/java/InfixExpressionOr.java").trim(),
			load("test/datagen/boa/InfixExpressionOr.boa").trim()
		);
	}
	
	@Test
	public void infixExpressionPlus() throws IOException {
		testWrapped(
			load("test/datagen/java/InfixExpressionPlus.java").trim(),
			load("test/datagen/boa/InfixExpressionPlus.boa").trim()
		);
	}
	
	@Test
	public void infixExpressionRemainder() throws IOException {
		testWrapped(
			load("test/datagen/java/InfixExpressionRemainder.java").trim(),
			load("test/datagen/boa/InfixExpressionRemainder.boa").trim()
		);
	}
	
}
