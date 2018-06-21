package boa.test.datagen.java;

import java.io.IOException;

import org.junit.Test;

public class TestCharacterLiteral extends Java8BaseTest {

	@Test
	public void characterLiteral() throws IOException {
		testWrapped(
			load("test/datagen/java/CharacterLiteral.java").trim(),
			load("test/datagen/boa/CharacterLiteral.boa").trim()
		);
	}
	
}
