package boa.test.datagen.html;

import static org.junit.Assert.assertEquals;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import boa.datagen.util.HtmlVisitor;
import boa.test.compiler.BaseTest;

public class HTMLBaseTest extends BaseTest {

	public void nodeTest(String expected, String html) throws Exception{
		assertEquals(expected, parseHTML(html));
	}

	private String parseHTML(String content) throws Exception {
		Document doc = Jsoup.parse(content);
		HtmlVisitor visitor = new HtmlVisitor();
		boa.types.Ast.Document document = visitor.getDocument(doc);
		return document.toString();
	}
	
	
}
