package boa.test.datagen.js;


import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ast.*;

import static org.junit.Assert.assertEquals;

import boa.types.Ast.ASTRoot;
import boa.datagen.util.JavaScriptVisitor;
import boa.test.compiler.BaseTest;


public class JavaScriptBaseTest extends BaseTest {

	public void nodeTest(String expected, String JS){
		assertEquals(expected, parseJs(JS));
	}

	private String parseJs(String content) {
		CompilerEnvirons cp = new CompilerEnvirons();
		cp.setLanguageVersion(Context.VERSION_ES6);
		final org.mozilla.javascript.Parser parser = new org.mozilla.javascript.Parser(cp);
		AstRoot cu;
		try{
			cu =  parser.parse(content, null, 0);
		}catch(java.lang.IllegalArgumentException ex){
			return "Parse error";
		}catch(org.mozilla.javascript.EvaluatorException ex){
			return "Parse error";
		}
		JavaScriptVisitor visitor = new JavaScriptVisitor(content);
		final ASTRoot.Builder ast = ASTRoot.newBuilder();
		ast.addNamespaces(visitor.getNamespaces(cu));
		String boaString = ast.build().toString();
		return boaString;
	}

}
