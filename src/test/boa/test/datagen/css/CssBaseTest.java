package boa.test.datagen.css;

import static org.junit.Assert.assertEquals;

import java.io.StringReader;

import org.w3c.css.sac.InputSource;
import com.steadystate.css.dom.CSSStyleSheetImpl;

import boa.datagen.util.CssVisitor;
import boa.test.compiler.BaseTest;
import boa.types.Ast.Element;

public class CssBaseTest extends BaseTest  {

	public void nodeTest(String expected, String css) throws Exception{
		assertEquals(expected, parseCSS(css));
	}

	private String parseCSS(String content) throws Exception {
		com.steadystate.css.parser.CSSOMParser parser = new com.steadystate.css.parser.CSSOMParser();
		InputSource source = new InputSource(new StringReader(content));
		com.steadystate.css.dom.CSSStyleSheetImpl sSheet = (CSSStyleSheetImpl) parser.parseStyleSheet(source, null, null);
		CssVisitor visitor = new CssVisitor();
		Element sheet = visitor.getStyleSheet(sSheet);
		return sheet.toString();
	}
	
}
