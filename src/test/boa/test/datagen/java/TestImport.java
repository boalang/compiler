package boa.test.datagen.java;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

public class TestImport extends Java8BaseTest {

	@Test
	public void importStatement() throws IOException {
		assertEquals(
			load("test/datagen/boa/Import.boa").trim(),
			parseJava(load("test/datagen/java/Import.java")).trim()
		);
		
	}
	
}
