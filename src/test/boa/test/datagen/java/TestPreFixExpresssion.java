package boa.test.datagen.java;

import java.io.IOException;

import org.junit.Test;

public class TestPreFixExpresssion extends Java8BaseTest {

	@Test
	public void preFixExpression() throws IOException {
		testWrapped(
			load("test/datagen/java/PreFixIncExpression.java").trim(),
			load("test/datagen/boa/PreFixIncExpression.boa").trim()
		);
	}
	
	@Test
	public void preFixDecExpression() throws IOException {
		testWrapped(
			load("test/datagen/java/PreFixDecExpression.java").trim(),
			load("test/datagen/boa/PreFixDecExpression.boa").trim()
		);
	}
	
	@Test
	public void preFixPlusExpression() throws IOException {
		testWrapped(
			load("test/datagen/java/PreFixPlusExpression.java").trim(),
			load("test/datagen/boa/PreFixPlusExpression.boa").trim()
		);
	}
	
	@Test
	public void preFixSubExpression() throws IOException {
		testWrapped(
			load("test/datagen/java/PreFixSubExpression.java").trim(),
			load("test/datagen/boa/PreFixSubExpression.boa").trim()
		);
	}
	
	@Test
	public void preFixComplimentExpression() throws IOException {
		testWrapped(
			load("test/datagen/java/PreFixComplimentExpression.java").trim(),
			load("test/datagen/boa/PreFixComplimentExpression.boa").trim()
		);
	}
	
	@Test
	public void preFixLogicalNotExpression() throws IOException {
		testWrapped(
			load("test/datagen/java/PreFixLogicalNotExpression.java").trim(),
			load("test/datagen/boa/PreFixLogicalNotExpression.boa").trim()
		);
	}
}
