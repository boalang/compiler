package boa.datagen.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import com.steadystate.css.dom.*;
import com.steadystate.css.parser.media.MediaQuery;

import boa.types.Ast;
import boa.types.Ast.Element;

public class CssVisitor {

	private CSSStyleSheetImpl root;
	protected Ast.Element.Builder b = Ast.Element.newBuilder();
	protected List<boa.types.Ast.Comment> comments = new ArrayList<boa.types.Ast.Comment>();
	protected Stack<List<boa.types.Ast.Element>> elements = new Stack<List<boa.types.Ast.Element>>();
	protected Stack<List<boa.types.Ast.Atribute>> atributes = new Stack<List<boa.types.Ast.Atribute>>();

	public CssVisitor() {

	}

	public Ast.Element getStyleSheet(com.steadystate.css.dom.CSSStyleSheetImpl node) {
		this.root = node;
		elements.push(new ArrayList<Element>());
		visitRoot();
		visit(node);
		return b.build();
	}

	private void visitRoot() {
		Element.Builder eb = Element.newBuilder();
		b.setKind(Element.ElementKind.STYLE_SHEET);
		b.setTag("");//FIXME
		CSSRuleListImpl l = (CSSRuleListImpl) root.getCssRules();
		if (root.getTitle() != null)
			eb.setTitle(root.getTitle());
		elements.push(new ArrayList<boa.types.Ast.Element>());
		for (Object o : l.getRules()) {
			if (o instanceof CSSFontFaceRuleImpl)
				visit((CSSFontFaceRuleImpl) o);
			else if (o instanceof CSSPageRuleImpl)
				visit((CSSPageRuleImpl) o);
			else if (o instanceof CSSStyleRuleImpl)
				visit((CSSStyleRuleImpl) o);
			else if (o instanceof CSSImportRuleImpl)
				visit((CSSImportRuleImpl) o);
			else if (o instanceof CSSMediaRuleImpl)
				visit((CSSMediaRuleImpl) o);

		}
	//	MediaListImpl ml = (MediaListImpl) root.getMedia(); FIXME
	//	for (int i = 0; i < ml.getLength(); i++) {
	//		visit(ml.mediaQuery(i));
	//	}
		for (boa.types.Ast.Element el : elements.pop())
			b.addElements(el);
	}

	private void visit(CSSStyleSheetImpl node) {
		Element.Builder b = Element.newBuilder();
		b.setKind(Element.ElementKind.STYLE_SHEET);
		b.setTag("");//FIXME
		CSSRuleListImpl l = (CSSRuleListImpl) node.getCssRules();
		if (node.getTitle() != null)
			b.setTitle(node.getTitle());
		elements.push(new ArrayList<boa.types.Ast.Element>());
		for (Object o : l.getRules()) {
			if (o instanceof CSSFontFaceRuleImpl)
				visit((CSSFontFaceRuleImpl) o);
			else if (o instanceof CSSPageRuleImpl)
				visit((CSSPageRuleImpl) o);
			else if (o instanceof CSSStyleRuleImpl)
				visit((CSSStyleRuleImpl) o);
			else if (o instanceof CSSImportRuleImpl)
				visit((CSSImportRuleImpl) o);
			else if (o instanceof CSSMediaRuleImpl)
				visit((CSSMediaRuleImpl) o);

		}
		MediaListImpl ml = (MediaListImpl) node.getMedia();
		for (int i = 0; i < ml.getLength(); i++) {
			visit(ml.mediaQuery(i));
		}
		for (boa.types.Ast.Element el : elements.pop())
			b.addElements(el);
		elements.peek().add(b.build());
	}

	private void visit(CSSMediaRuleImpl node) {
		Element.Builder b = Element.newBuilder();
		b.setKind(Element.ElementKind.MEDIA_RULE);
		b.setTag("");//FIXME
		CSSRuleListImpl l = (CSSRuleListImpl) node.getCssRules();
		elements.push(new ArrayList<boa.types.Ast.Element>());
		for (Object o : l.getRules()) {
			if (o instanceof CSSFontFaceRuleImpl)
				visit((CSSFontFaceRuleImpl) o);
			else if (o instanceof CSSPageRuleImpl)
				visit((CSSPageRuleImpl) o);
			else if (o instanceof CSSStyleRuleImpl)
				visit((CSSStyleRuleImpl) o);
			else if (o instanceof CSSImportRuleImpl)
				visit((CSSImportRuleImpl) o);
			else if (o instanceof CSSMediaRuleImpl)
				visit((CSSMediaRuleImpl) o);

		}
		MediaListImpl ml = (MediaListImpl) node.getMedia();
		for (int i = 0; i < ml.getLength(); i++) {
			visit(ml.mediaQuery(i));
		}
		for (boa.types.Ast.Element el : elements.pop())
			b.addElements(el);
		elements.peek().add(b.build());
	}

	private void visit(CSSImportRuleImpl node) {
		Element.Builder b = Element.newBuilder();
		b.setKind(Element.ElementKind.IMPORT_RULE);
		b.setTag("");//FIXME
		b.addData(node.getHref());
		MediaListImpl ml = (MediaListImpl) node.getMedia();
		elements.push(new ArrayList<Element>());
		for (int i = 0; i < ml.getLength(); i++) {
			visit(ml.mediaQuery(i));
		}
		for (Element e : elements.pop())
			b.addElements(e);
		elements.peek().add(b.build());
	}

	private void visit(MediaQuery node) {
		Element.Builder b = Element.newBuilder();
		b.setKind(Element.ElementKind.MEDIA_QUERY);
		b.setTag("");//FIXME
		b.addData(node.getMedia());
		for (Property p : node.getProperties()) {
			boa.types.Ast.Atribute.Builder a = boa.types.Ast.Atribute.newBuilder();
			a.setKey(p.getName());
			a.setValue(p.getValue().getCssText());
			b.addAtributes(a.build());
		}
		elements.peek().add(b.build());
	}

	private void visit(CSSStyleRuleImpl node) {
		Element.Builder b = Element.newBuilder();
		b.setKind(Element.ElementKind.STYLE_RULE);
		b.setTag(node.getSelectorText());
		CSSStyleDeclarationImpl style = (CSSStyleDeclarationImpl) node.getStyle();
		atributes.push(new ArrayList<boa.types.Ast.Atribute>());
		visit(style);
		for (boa.types.Ast.Atribute a : atributes.pop())
			b.addAtributes(a);
		elements.peek().add(b.build());
	}

	private void visit(CSSStyleDeclarationImpl node) {
		for (Property p : node.getProperties()) {
			boa.types.Ast.Atribute.Builder a = boa.types.Ast.Atribute.newBuilder();
			a.setKey(p.getName());
			a.setValue(p.getValue().getCssText());
			atributes.peek().add(a.build());
		}
	}

	private void visit(CSSPageRuleImpl node) {
		Element.Builder b = Element.newBuilder();
		b.setKind(Element.ElementKind.PAGE_RULE);
		b.setTag("");//FIXME
		b.addData(node.getSelectorText());
		CSSStyleDeclarationImpl style = (CSSStyleDeclarationImpl) node.getStyle();
		atributes.push(new ArrayList<boa.types.Ast.Atribute>());
		visit(style);
		for (boa.types.Ast.Atribute a : atributes.pop())
			b.addAtributes(a);
		elements.peek().add(b.build());
	}

	private void visit(CSSFontFaceRuleImpl node) {
		Element.Builder b = Element.newBuilder();
		b.setKind(Element.ElementKind.FONT_FACE_RULE);
		b.setTag("");//FIXME
		CSSStyleDeclarationImpl style = (CSSStyleDeclarationImpl) node.getStyle();
		atributes.push(new ArrayList<boa.types.Ast.Atribute>());
		visit(style);
		for (boa.types.Ast.Atribute a : atributes.pop())
			b.addAtributes(a);
		elements.peek().add(b.build());
	}

}
