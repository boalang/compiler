package boa.test.datagen.java;

import java.io.IOException;

import org.junit.Test;

public class TestMethodInvocation extends Java8BaseTest {

	@Test
	public void methodInvocation() throws IOException {
		testWrapped(
			"test/datagen/java/MethodInvocation.java",
			load("test/datagen/boa/MethodInvocation.boa").trim()
		);
	}
	
}
