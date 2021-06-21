package boa.test.datagen.java;

import java.io.IOException;

import org.junit.Test;

public class TestInfixExpression extends Java8BaseTest {

	@Test
	public void infixExpressionAnd() throws IOException {
		testWrapped(
			"test/datagen/java/InfixExpressionAnd.java",
			load("test/datagen/boa/InfixExpressionAnd.boa").trim()
		);
	}
	
	@Test
	public void infixExpressionLogicalAnd() throws IOException {
		testWrapped(
			"test/datagen/java/InfixExpressionLogicalAnd.java",
			load("test/datagen/boa/InfixExpressionLogicalAnd.boa").trim()
		);
	}
	
	@Test
	public void infixExpressionLogicalOr() throws IOException {
		testWrapped(
			"test/datagen/java/InfixExpressionLogicalOr.java",
			load("test/datagen/boa/InfixExpressionLogicalOr.boa").trim()
		);
	}
	
	@Test
	public void infixExpressionDivide() throws IOException {
		testWrapped(
			"test/datagen/java/InfixExpressionDivide.java",
			load("test/datagen/boa/InfixExpressionDivide.boa").trim()
		);
	}
	
	@Test
	public void infixExpressionEquals() throws IOException {
		testWrapped(
			"test/datagen/java/InfixExpressionEquals.java",
			load("test/datagen/boa/InfixExpressionEquals.boa").trim()
		);
	}
	
	@Test
	public void infixExpressionGreaterThen() throws IOException {
		testWrapped(
			"test/datagen/java/InfixExpressionGreaterThen.java",
			load("test/datagen/boa/InfixExpressionGreaterThen.boa").trim()
		);
	}
	
	@Test
	public void infixExpressionGreaterEqual() throws IOException {
		testWrapped(
			"test/datagen/java/InfixExpressionGreaterEqual.java",
			load("test/datagen/boa/InfixExpressionGreaterEqual.boa").trim()
		);
	}
	
	@Test
	public void infixExpressionLeftShift() throws IOException {
		testWrapped(
			"test/datagen/java/InfixExpressionLeftShift.java",
			load("test/datagen/boa/InfixExpressionLeftShift.boa").trim()
		);
	}
	
	@Test
	public void infixExpressionLessThen() throws IOException {
		testWrapped(
			"test/datagen/java/InfixExpressionLessThen.java",
			load("test/datagen/boa/InfixExpressionLessThen.boa").trim()
		);
	}
	
	@Test
	public void infixExpressionLessEqual() throws IOException {
		testWrapped(
			"test/datagen/java/InfixExpressionLessEqual.java",
			load("test/datagen/boa/InfixExpressionLessEqual.boa").trim()
		);
	}
	
	@Test
	public void infixExpressionMinus() throws IOException {
		testWrapped(
			"test/datagen/java/InfixExpressionMinus.java",
			load("test/datagen/boa/InfixExpressionMinus.boa").trim()
		);
	}
	
	@Test
	public void infixExpressionNotEqual() throws IOException {
		testWrapped(
			"test/datagen/java/InfixExpressionNotEqual.java",
			load("test/datagen/boa/InfixExpressionNotEqual.boa").trim()
		);
	}
	
	@Test
	public void infixExpressionOr() throws IOException {
		testWrapped(
			"test/datagen/java/InfixExpressionOr.java",
			load("test/datagen/boa/InfixExpressionOr.boa").trim()
		);
	}
	
	@Test
	public void infixExpressionPlus() throws IOException {
		testWrapped(
			"test/datagen/java/InfixExpressionPlus.java",
			load("test/datagen/boa/InfixExpressionPlus.boa").trim()
		);
	}
	
	@Test
	public void infixExpressionRemainder() throws IOException {
		testWrapped(
			"test/datagen/java/InfixExpressionRemainder.java",
			load("test/datagen/boa/InfixExpressionRemainder.boa").trim()
		);
	}
	
	@Test
	public void infixExpressionRightShiftSigned() throws IOException {
		testWrapped(
			"test/datagen/java/InfixExpressionRightShiftSigned.java",
			load("test/datagen/boa/InfixExpressionRightShiftSigned.boa").trim()
		);
	}
	
	@Test
	public void infixExpressionRightShiftUnsigned() throws IOException {
		testWrapped(
			"test/datagen/java/InfixExpressionRightShiftUnsigned.java",
			load("test/datagen/boa/InfixExpressionRightShiftUnsigned.boa").trim()
		);
	}
	
	@Test
	public void infixExpressionTimes() throws IOException {
		testWrapped(
			"test/datagen/java/InfixExpressionTimes.java",
			load("test/datagen/boa/InfixExpressionTimes.boa").trim()
		);
	}
	
	@Test
	public void infixExpressionXor() throws IOException {
		testWrapped(
			"test/datagen/java/InfixExpressionXor.java",
			load("test/datagen/boa/InfixExpressionXor.boa").trim()
		);
	}
	
}
