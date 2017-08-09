package boa.test.datagen.php;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.File;

import org.eclipse.php.internal.core.PHPVersion;
import org.eclipse.php.internal.core.ast.nodes.ASTParser;
import org.eclipse.php.internal.core.ast.nodes.Program;
import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ast.AstRoot;

import boa.datagen.util.FileIO;
import boa.datagen.util.JavaScriptVisitor;
import boa.datagen.util.PHPErrorCheckVisitor;
import boa.datagen.util.PHPVisitor;
import boa.test.compiler.BaseTest;
import boa.types.Ast.ASTRoot;
import boa.types.Ast.Namespace;

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
