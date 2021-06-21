package boa.test.datagen.java;

import java.io.IOException;

import org.junit.Test;

public class TestLabeledStatement extends Java8BaseTest {

	@Test
	public void labeledSatement() throws IOException {
		testWrapped(
			"test/datagen/java/LabeledStatement.java",
			load("test/datagen/boa/LabeledStatement.boa").trim()
		);
	}
	
}
