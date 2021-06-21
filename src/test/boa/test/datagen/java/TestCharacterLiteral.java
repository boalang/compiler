package boa.test.datagen.java;

import java.io.IOException;

import org.junit.Test;

public class TestCharacterLiteral extends Java8BaseTest {

	@Test
	public void characterLiteral() throws IOException {
		testWrapped(
			"test/datagen/java/CharacterLiteral.java",
			load("test/datagen/boa/CharacterLiteral.boa").trim()
		);
	}
	
}
