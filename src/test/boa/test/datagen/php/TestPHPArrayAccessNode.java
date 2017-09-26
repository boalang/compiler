package boa.test.datagen.php;

import java.io.IOException;

import org.junit.Test;

public class TestPHPArrayAccessNode extends PHPBaseTest {

	@Test
	public void ArrayAccess() throws IOException, Exception{
		nodeTest(load("test/datagen/PHP/ArrayAccessNode.boa"),
				load("test/datagen/PHP/ArrayAccessNode.php"));
	}
	
}
