package boa.test.datagen;

import org.junit.Test;

import boa.datagen.DefaultProperties;

public class TestBuildSnapshot3 {
	
	@Test
	public void testBuildSnapshot() throws Exception {
		DefaultProperties.DEBUG = true;
		
		String[] repoNames = new String[]{"junit-team/junit4"};
		for (String repoName : repoNames)
			TestBuildSnapshot.buildCodeRepository(repoName);
	}
	
}


