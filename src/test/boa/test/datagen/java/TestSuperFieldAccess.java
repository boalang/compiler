package boa.test.datagen.java;

import java.io.IOException;

import org.junit.Test;

public class TestSuperFieldAccess extends Java8BaseTest {

	@Test
	public void superFieldAccess() throws IOException {
		testWrapped(
			load("test/datagen/java/SuperFieldAccess.java").trim(),
			load("test/datagen/boa/SuperFieldAccess.boa").trim()
		);
	}
	
}
