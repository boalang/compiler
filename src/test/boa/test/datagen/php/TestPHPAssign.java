package boa.test.datagen.php;

import java.io.IOException;

import org.junit.Test;

public class TestPHPAssign extends PHPBaseTest {

	@Test
	public void assignConcat() throws IOException, Exception{
		nodeTest(load("test/datagen/PHP/ASSIGN_CONCATNode.boa"),
				load("test/datagen/PHP/ASSIGN_CONCATNode.php"));
	}
	
	@Test
	public void assignPower() throws IOException, Exception{
		nodeTest(load("test/datagen/PHP/ASSIGN_POWNode.boa"),
				load("test/datagen/PHP/ASSIGN_POWNode.php"));
	}
	
}
