package boa.test.datagen.php;

import static org.junit.Assert.assertEquals;

import org.eclipse.php.internal.core.PHPVersion;
import org.eclipse.php.internal.core.ast.nodes.ASTParser;
import org.eclipse.php.internal.core.ast.nodes.Program;

import boa.datagen.util.PHPVisitor;
import boa.test.compiler.BaseTest;
import boa.types.Ast.ASTRoot;

public class PHPBaseTest extends BaseTest {
 
	public void nodeTest(String expected, String PHP) throws Exception{
		assertEquals(expected, parsePHP(PHP));
	}

	private String parsePHP(String content) throws Exception {
		ASTParser parser = org.eclipse.php.internal.core.ast.nodes.ASTParser.newParser(PHPVersion.PHP7_1);
		parser.setSource(content.toCharArray());
		Program cu = parser.createAST(null);
		PHPVisitor visitor = new PHPVisitor(content);
		final ASTRoot.Builder ast = ASTRoot.newBuilder();
		ast.addNamespaces(visitor.getNamespace(cu));
		String boaString = ast.build().toString();
		return boaString;
	}
	
}
