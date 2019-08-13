package boa.test.datagen.xml;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.dom4j.Document;
import org.dom4j.dom.DOMDocument;
import org.dom4j.io.SAXReader;

import boa.datagen.util.XMLVisitor;
import boa.test.compiler.BaseTest;

public class XMLBaseTest extends BaseTest{

	public void nodeTest(String expected, String xml) throws Exception{
		assertEquals(expected, parseXML(xml));
	}

	private String parseXML(String content) throws Exception {
		File file = new File(content);
		org.dom4j.dom.DOMDocumentFactory di = new org.dom4j.dom.DOMDocumentFactory();
		SAXReader reader = new SAXReader(di);
		Document doc = reader.read(file); 
		XMLVisitor visitor = new XMLVisitor();
		boa.types.Ast.Document document = visitor.getDocument((DOMDocument)doc);
		return document.toString();
	}
	
}
