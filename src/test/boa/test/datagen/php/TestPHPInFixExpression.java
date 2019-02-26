package boa.test.datagen.php;

import java.io.IOException;

import org.junit.Test;

public class TestPHPInFixExpression extends PHPBaseTest {

	@Test
	public void testInFixExpressionIdentical() throws IOException, Exception{
		nodeTest(load("test/datagen/PHP/InFixExpressionIdentical.boa"),
				load("test/datagen/PHP/InFixExpressionIdentical.php"));
	}
	
	@Test
	public void testInFixExpressionNotIdentical() throws IOException, Exception{
		nodeTest(load("test/datagen/PHP/InFixExpressionNotIdentical.boa"),
				load("test/datagen/PHP/InFixExpressionNotIdentical.php"));
	}
	
	@Test
	public void testInFixExpressionStringXor() throws IOException, Exception{
		nodeTest(load("test/datagen/PHP/InFixExpressionStringXor.boa"),
				load("test/datagen/PHP/InFixExpressionStringXor.php"));
	}
	
	@Test
	public void testInFixExpressionStringOr() throws IOException, Exception{
		nodeTest(load("test/datagen/PHP/InFixExpressionStringOr.boa"),
				load("test/datagen/PHP/InFixExpressionStringOr.php"));
	}
	
	@Test
	public void testInFixExpressionStringAnd() throws IOException, Exception{
		nodeTest(load("test/datagen/PHP/InFixExpressionStringAnd.boa"),
				load("test/datagen/PHP/InFixExpressionStringAnd.php"));
	}
	
	@Test
	public void testInFixExpressionPower() throws IOException, Exception{
		nodeTest(load("test/datagen/PHP/InFixExpressionPower.boa"),
				load("test/datagen/PHP/InFixExpressionPower.php"));
	}
	
	@Test
	public void testInFixExpressionConCat() throws IOException, Exception{
		nodeTest(load("test/datagen/PHP/InFixExpressionConCat.boa"),
				load("test/datagen/PHP/InFixExpressionConCat.php"));
	}
	
	@Test
	public void testInFixExpressionSpaceShip() throws IOException, Exception{
		nodeTest(load("test/datagen/PHP/InFixExpressionSpaceShip.boa"),
				load("test/datagen/PHP/InFixExpressionSpaceShip.php"));
	}
	
}
