package boa.test.datagen.java;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

public class TestSuperConstructorInvocation extends Java8BaseTest {

	@Test
	public void superConstructorInvocation() throws IOException {
		testWrapped(
			load("test/datagen/java/SuperConstructorInvocation.java").trim(),
			load("test/datagen/boa/SuperConstructorInvocation.boa").trim()
		);
	}
	
}
