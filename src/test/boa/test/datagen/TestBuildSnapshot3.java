package boa.test.datagen;

import org.junit.Test;

import boa.datagen.DefaultProperties;

public class TestBuildSnapshot3 {
	@Test
	public void testBuildSnapshot() throws Exception {
		DefaultProperties.DEBUG = true;

		TestBuildSnapshot.buildCodeRepository("junit-team/junit4");
	}
}
