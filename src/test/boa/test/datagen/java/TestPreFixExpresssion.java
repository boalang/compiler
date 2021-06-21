package boa.test.datagen.java;

import java.io.IOException;

import org.junit.Test;

public class TestPreFixExpresssion extends Java8BaseTest {

	@Test
	public void preFixExpression() throws IOException {
		testWrapped(
			"test/datagen/java/PreFixIncExpression.java",
			load("test/datagen/boa/PreFixIncExpression.boa").trim()
		);
	}
	
	@Test
	public void preFixDecExpression() throws IOException {
		testWrapped(
			"test/datagen/java/PreFixDecExpression.java",
			load("test/datagen/boa/PreFixDecExpression.boa").trim()
		);
	}
	
	@Test
	public void preFixPlusExpression() throws IOException {
		testWrapped(
			"test/datagen/java/PreFixPlusExpression.java",
			load("test/datagen/boa/PreFixPlusExpression.boa").trim()
		);
	}
	
	@Test
	public void preFixSubExpression() throws IOException {
		testWrapped(
			"test/datagen/java/PreFixSubExpression.java",
			load("test/datagen/boa/PreFixSubExpression.boa").trim()
		);
	}
	
	@Test
	public void preFixComplimentExpression() throws IOException {
		testWrapped(
			"test/datagen/java/PreFixComplimentExpression.java",
			load("test/datagen/boa/PreFixComplimentExpression.boa").trim()
		);
	}
	
	@Test
	public void preFixLogicalNotExpression() throws IOException {
		testWrapped(
			"test/datagen/java/PreFixLogicalNotExpression.java",
			load("test/datagen/boa/PreFixLogicalNotExpression.boa").trim()
		);
	}
}
