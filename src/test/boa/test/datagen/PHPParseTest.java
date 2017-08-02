package boa.test.datagen;

import java.io.File;
import java.io.IOException;

import org.eclipse.php.internal.core.PHPVersion;
import org.eclipse.php.internal.core.ast.nodes.ASTParser;
import org.eclipse.php.internal.core.ast.nodes.Program;
import org.junit.Test;
//import org.eclipse.dltk.core.ModelException;

import boa.datagen.util.FileIO;
import boa.datagen.util.PHPVisitor;

public class PHPParseTest {
	
	@Test
	public void parseTest() throws Exception{
		ASTParser parser = org.eclipse.php.internal.core.ast.nodes.ASTParser.newParser(PHPVersion.PHP5);
		String content = FileIO.readFileContents(new File("test/datagen/PHP/PHPParseTest.php"));
		parser.setSource(content.toCharArray());
		Program ast = parser.createAST(null);
		PHPVisitor visitor = new PHPVisitor(content);
		boa.types.Ast.ASTRoot root = visitor.getRoot(ast);
		System.out.println(root);
	}
}
