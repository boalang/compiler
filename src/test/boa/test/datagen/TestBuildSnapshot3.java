package boa.test.datagen;

import org.junit.Test;

import boa.datagen.DefaultProperties;

public class TestBuildSnapshot3 extends BuildSnapshotBase {
	@Test
	public void testBuildSnapshot() throws Exception {
		DefaultProperties.DEBUG = true;

		buildCodeRepository("junit-team/junit4");
	}
}
