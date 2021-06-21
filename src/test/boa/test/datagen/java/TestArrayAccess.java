package boa.test.datagen.java;

import java.io.IOException;

import org.junit.Test;

public class TestArrayAccess extends Java8BaseTest {

	@Test
	public void arrayAccess() throws IOException {
		testWrapped(
			"test/datagen/java/ArrayAccess.java",
			load("test/datagen/boa/ArrayAccess.boa").trim()
		);
	}
	
	
}
