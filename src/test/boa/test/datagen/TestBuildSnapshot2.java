package boa.test.datagen;

import org.junit.Test;

import boa.datagen.DefaultProperties;

public class TestBuildSnapshot2 extends BuildSnapshotBase {
	@Test
	public void testBuildSnapshot() throws Exception {
		DefaultProperties.DEBUG = true;

		buildCodeRepository("boalang/compiler");
	}
}
