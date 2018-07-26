package boa.test.datagen;

import org.junit.Test;

import boa.datagen.DefaultProperties;

public class TestBuildSnapshot2 {
	
	@Test
	public void testBuildSnapshot() throws Exception {
		DefaultProperties.DEBUG = true;
		
		String[] repoNames = new String[]{"boalang/compiler"};
		for (String repoName : repoNames)
			TestBuildSnapshot.buildCodeRepository(repoName);
	}
	
}


