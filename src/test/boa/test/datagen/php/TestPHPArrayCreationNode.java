package boa.test.datagen.php;

import java.io.IOException;

import org.junit.Test;

public class TestPHPArrayCreationNode extends PHPBaseTest {

	@Test
	public void arrayCreation() throws IOException, Exception{
		nodeTest(load("test/datagen/PHP/ArrayCreationNode.boa"),
				load("test/datagen/PHP/ArrayCreationNode.php"));
	}
	
	@Test
	public void arrayCreationWithElements() throws IOException, Exception{
		nodeTest(load("test/datagen/PHP/ArrayCreationNode1.boa"),
				load("test/datagen/PHP/ArrayCreationNode1.php"));
	}
	
}
