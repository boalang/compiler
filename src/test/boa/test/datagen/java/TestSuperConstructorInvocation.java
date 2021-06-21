package boa.test.datagen.java;

import java.io.IOException;

import org.junit.Test;

public class TestSuperConstructorInvocation extends Java8BaseTest {

	@Test
	public void superConstructorInvocation() throws IOException {
		testWrapped(
			"test/datagen/java/SuperConstructorInvocation.java",
			load("test/datagen/boa/SuperConstructorInvocation.boa").trim()
		);
	}
	
}
