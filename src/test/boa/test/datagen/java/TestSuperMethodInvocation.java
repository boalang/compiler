package boa.test.datagen.java;

import java.io.IOException;

import org.junit.Test;

public class TestSuperMethodInvocation extends Java8BaseTest {

	@Test
	public void superMethodInvocation() throws IOException {
		testWrapped(
			"test/datagen/java/SuperMethodInvocation.java",
			load("test/datagen/boa/SuperMethodInvocation.boa").trim()
		);
	}
	
}
