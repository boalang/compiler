package boa.test.datagen.html;

import java.io.File;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;

import boa.datagen.util.FileIO;
import boa.datagen.util.HtmlVisitor;

public class HTMLParseTest {
	@Test
	public void parseTest(){
	String content = FileIO.readFileContents(new File("test/datagen/HTML/HtmlParseTest.dtd"));
	System.out.println(content);
	Document doc = Jsoup.parse(content);
	HtmlVisitor visitor = new HtmlVisitor();
	boa.types.Ast.Document document = visitor.getDocument(doc);
	System.out.println(document.toString());
	//System.out.println(doc.toString());
	}
	
}
