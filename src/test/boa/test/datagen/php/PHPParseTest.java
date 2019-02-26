package boa.test.datagen.php;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.eclipse.php.internal.core.PHPVersion;
import org.eclipse.php.internal.core.ast.nodes.ASTParser;
import org.eclipse.php.internal.core.ast.nodes.Program;
import org.junit.Test;
//import org.eclipse.dltk.core.ModelException;

import boa.datagen.util.FileIO;
import boa.datagen.util.PHPErrorCheckVisitor;
import boa.datagen.util.PHPVisitor;
import boa.types.Ast.ASTRoot;
import boa.types.Ast.Namespace;

public class PHPParseTest {
	
	@Test
	public void parseTest() throws Exception{
		ASTParser parser = org.eclipse.php.internal.core.ast.nodes.ASTParser.newParser(PHPVersion.PHP7_1);
		String content = FileIO.readFileContents(new File("test/datagen/PHP/PHPParseTest.php"));
		parser.setSource(content.toCharArray());
		Program ast = parser.createAST(null);
		PHPVisitor visitor = new PHPVisitor(content);
		PHPErrorCheckVisitor errorCheck = new PHPErrorCheckVisitor();
		ast.accept(errorCheck);
		assertFalse(errorCheck.hasError);
		final ASTRoot.Builder root = ASTRoot.newBuilder();
		root.addNamespaces(visitor.getNamespace(ast));
		System.out.println(root.build().toString());
	}
}
