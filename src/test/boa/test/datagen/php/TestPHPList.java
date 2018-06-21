package boa.test.datagen.php;

import java.io.IOException;

import org.junit.Test;

public class TestPHPList extends PHPBaseTest {

	@Test
	public void testList() throws IOException, Exception{
		nodeTest(load("test/datagen/PHP/List.boa"),
				load("test/datagen/PHP/List.php"));
	}
	
}
