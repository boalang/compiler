package boa.test.datagen.php;

import java.io.IOException;

import org.junit.Test;

public class TestPHPInfixExpression extends PHPBaseTest {

	@Test
	public void testInfixExpressionIdentical() throws IOException, Exception{
		nodeTest(load("test/datagen/PHP/InfixExpressionIdentical.boa"),
				load("test/datagen/PHP/InfixExpressionIdentical.php"));
	}
	
	@Test
	public void testInfixExpressionNotIdentical() throws IOException, Exception{
		nodeTest(load("test/datagen/PHP/InfixExpressionNotIdentical.boa"),
				load("test/datagen/PHP/InfixExpressionNotIdentical.php"));
	}
	
	@Test
	public void testInfixExpressionStringXor() throws IOException, Exception{
		nodeTest(load("test/datagen/PHP/InfixExpressionStringXor.boa"),
				load("test/datagen/PHP/InfixExpressionStringXor.php"));
	}
	
	@Test
	public void testInfixExpressionStringOr() throws IOException, Exception{
		nodeTest(load("test/datagen/PHP/InfixExpressionStringOr.boa"),
				load("test/datagen/PHP/InfixExpressionStringOr.php"));
	}
	
	@Test
	public void testInfixExpressionStringAnd() throws IOException, Exception{
		nodeTest(load("test/datagen/PHP/InfixExpressionStringAnd.boa"),
				load("test/datagen/PHP/InfixExpressionStringAnd.php"));
	}
	
	@Test
	public void testInfixExpressionPower() throws IOException, Exception{
		nodeTest(load("test/datagen/PHP/InfixExpressionPower.boa"),
				load("test/datagen/PHP/InfixExpressionPower.php"));
	}
	
	@Test
	public void testInfixExpressionConCat() throws IOException, Exception{
		nodeTest(load("test/datagen/PHP/InfixExpressionConCat.boa"),
				load("test/datagen/PHP/InfixExpressionConCat.php"));
	}
	
	@Test
	public void testInfixExpressionSpaceShip() throws IOException, Exception{
		nodeTest(load("test/datagen/PHP/InfixExpressionSpaceShip.boa"),
				load("test/datagen/PHP/InfixExpressionSpaceShip.php"));
	}
	
}
