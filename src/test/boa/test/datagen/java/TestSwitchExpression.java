package boa.test.datagen.java;

import java.io.IOException;

import org.junit.Test;

public class TestSwitchExpression extends Java8BaseTest {

	@Test
	public void switchExpression() throws IOException {
		testWrapped(
			"test/datagen/java/SwitchExpression.java",
			load("test/datagen/boa/SwitchExpression.boa").trim()
		);
	}
	
}
