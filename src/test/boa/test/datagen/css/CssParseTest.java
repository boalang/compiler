package boa.test.datagen.css;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;

import org.junit.Test;
import org.w3c.css.sac.InputSource;

import com.steadystate.css.dom.CSSStyleSheetImpl;
import com.steadystate.css.parser.*;

import boa.datagen.util.CssVisitor;
import boa.datagen.util.FileIO;

public class CssParseTest {

	@Test
	public void parseTest(){
	//	InputSource source = new InputSource(new StringReader("h1 { background: #ffcc44; }"));
	//	CSSOMParser parser = new CSSOMParser(new SACParserCSS3());
	//	CSSStyleSheet sheet = parser.parseStyleSheet(source, null, null);
		
		com.steadystate.css.parser.CSSOMParser parser = new com.steadystate.css.parser.CSSOMParser();
		String content = FileIO.readFileContents(new File("test/datagen/Css/ParseTest.css"));
		InputSource source = new InputSource(new StringReader(content));
		try {
			com.steadystate.css.dom.CSSStyleSheetImpl sSheet = (CSSStyleSheetImpl) parser.parseStyleSheet(source, null, null);
			CssVisitor visitor = new CssVisitor();
			boa.types.Ast.Element document = visitor.getStyleSheet(sSheet);
			System.out.println(document.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	
}
