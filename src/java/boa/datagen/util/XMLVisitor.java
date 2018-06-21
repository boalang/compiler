package boa.datagen.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.dom4j.Document;
import org.dom4j.Namespace;
import org.dom4j.Node;
import org.dom4j.ProcessingInstruction;
import org.dom4j.dom.*;
import org.dom4j.tree.DefaultDocument;
import org.w3c.dom.NodeList;

import boa.types.Ast;
import boa.types.Ast.Element;
import boa.types.Ast.Element.ElementKind;

public class XMLVisitor {
	protected Ast.Document.Builder b = Ast.Document.newBuilder();
	protected DefaultDocument root;
	protected List<boa.types.Ast.Comment> comments = new ArrayList<boa.types.Ast.Comment>();
	protected Stack<boa.types.Ast.Attribute> Attributes = new Stack<boa.types.Ast.Attribute>();
	protected Stack<List<boa.types.Ast.Element>> elements = new Stack<List<boa.types.Ast.Element>>(); 
	
	public Ast.Document getDocument(DOMDocument node) {
		this.root =  node;
		visit(node);
		return b.build();
	}
	
	private void visit(DOMDocument node) {
		// b.setTitle(node.getName());//FIXME ?
		elements.push(new ArrayList<boa.types.Ast.Element>());
		NodeList children = node.getChildNodes();
		for (int i = 0; i < children.getLength(); i++){
			Node n = (Node) children.item(i);
			if (n instanceof DOMElement)
				visit((org.dom4j.dom.DOMElement)n);
			else if (n instanceof DOMDocumentType){
				elements.push(new ArrayList<boa.types.Ast.Element>());
				visit((DOMDocumentType) n);
				for (Ast.Element e : elements.pop())
					b.setDocType(e);
			} else if (n instanceof DOMProcessingInstruction){
				visit((DOMProcessingInstruction)n);
				b.addProcessingInstruction(Attributes.pop());
			}
		}
		for (Ast.Element e : elements.pop())
			b.addElements(e);
	}

	private void visit(DOMProcessingInstruction node) {
		Ast.Attribute.Builder b = Ast.Attribute.newBuilder();
		b.setKey(node.getName());
		b.setValue(node.getText());
		Attributes.push(b.build());
	}

	private void visit(DOMDocumentType node) {
		Ast.Element.Builder b = Ast.Element.newBuilder();
		b.setKind(ElementKind.DOC_TYPE);
		b.setTag("!DOCTYPE");
		DOMAttributeNodeMap attr = (DOMAttributeNodeMap) node.getAttributes();
		for (int i = 0; i < attr.getLength(); i++) {
			DOMAttribute a = (DOMAttribute) attr.item(i);
			visit(a);
			b.addAttributes(Attributes.pop());
		}
		elements.peek().add(b.build());
		
	}

	private void visit(DOMElement node) {
		Element.Builder b = Element.newBuilder();
		b.setKind(ElementKind.XML_ELEMENT);
		b.setTag(node.getQualifiedName());
		Namespace ns = node.getNamespace();
		if (!ns.getURI().equals("")){
			Ast.Attribute.Builder ab = Ast.Attribute.newBuilder();
			ab.setKey(ns.getPrefix() + " URI");
			ab.setValue(ns.getURI());
			b.addAttributes(ab.build());
		}
		DOMAttributeNodeMap attr = (DOMAttributeNodeMap) node.getAttributes();
		for (int i = 0; i < attr.getLength(); i++) {
			DOMAttribute a = (DOMAttribute) attr.item(i);
			visit(a);
			b.addAttributes(Attributes.pop());
		}
		elements.push(new ArrayList<boa.types.Ast.Element>());
		NodeList children = node.getChildNodes();
		for (int i = 0; i < children.getLength(); i++){
			Node n = (Node) children.item(i);
			switch (n.getNodeType()){
				case(org.dom4j.Node.ELEMENT_NODE):
					visit((org.dom4j.dom.DOMElement)n);
					break;
				case(org.dom4j.Node.TEXT_NODE):
					String t = ((DOMText) n).getText();
					String check = t.replaceAll(" ", "");
					if (!check.equals("") && !check.equals("\n") && !check.equals("\n\n") && !check.equals("\n  ") && !check.equals("\n    ") )
						b.addText(t);
					break;
			}
		}
		
		for (ProcessingInstruction pi : node.processingInstructions()){
			visit((DOMProcessingInstruction)pi);
			b.addProcessingInstruction(Attributes.pop());
		}
		
		
		for (Ast.Element e : elements.pop())
			b.addElements(e);
		elements.peek().add(b.build());
		
	}

	private void visit(DOMAttribute node) {
		Ast.Attribute.Builder b = Ast.Attribute.newBuilder();
		b.setKey(node.getQName().getQualifiedName());//FIXME
		b.setValue(node.getValue());
		Attributes.push(b.build());
	}
}
