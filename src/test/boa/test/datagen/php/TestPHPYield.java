package boa.test.datagen.php;

import java.io.IOException;

import org.junit.Test;

public class TestPHPYield extends PHPBaseTest {

	@Test
	public void testYield() throws IOException, Exception{
		nodeTest(load("test/datagen/PHP/Yield.boa"),
				load("test/datagen/PHP/Yield.php"));
	}
	
	@Test
	public void testYieldFrom() throws IOException, Exception{
		nodeTest(load("test/datagen/PHP/YieldFrom.boa"),
				load("test/datagen/PHP/YieldFrom.php"));
	}
}
