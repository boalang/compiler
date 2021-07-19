package boa.test.datagen.java;

import java.io.IOException;

import org.junit.Test;

public class TestRecordDeclaration extends Java8BaseTest {

	@Test
	public void recordDeclaration() throws IOException {
		testWrapped(
			"test/datagen/java/RecordDeclaration.java",
			load("test/datagen/boa/RecordDeclaration.boa").trim()
		);
	}
	
}