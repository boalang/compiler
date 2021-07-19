package boa.test.datagen.java;

import java.io.IOException;

import org.junit.Test;

public class TestTextBlock extends Java8BaseTest {

	@Test
	public void textBlock() throws IOException {
		testWrapped(
			"test/datagen/java/TextBlock.java",
			load("test/datagen/boa/TextBlock.boa").trim()
		);
	}
	
}

