package boa.datagen.util;

import boa.types.Ast;
import boa.types.Ast.*;
import boa.types.Ast.Comment.CommentKind;
import boa.types.Ast.Expression.ExpressionKind;
import boa.types.Ast.ASTRoot;
import boa.types.Ast.Declaration;
import boa.types.Ast.Element.ElementKind;
import boa.types.Ast.Expression;
import boa.types.Ast.Method;
import boa.types.Ast.Modifier;
import boa.types.Ast.Namespace;
import boa.types.Ast.PositionInfo;
import boa.types.Ast.Statement;
import boa.types.Ast.Statement.StatementKind;
import boa.types.Ast.Type;
import boa.types.Ast.TypeKind;
import boa.types.Ast.Variable;
import boa.types.Ast.Modifier.ModifierKind;

import static org.junit.Assert.assertFalse;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.eclipse.php.internal.core.PHPVersion;
import org.eclipse.php.internal.core.ast.nodes.ASTParser;
import org.eclipse.php.internal.core.ast.nodes.Program;
import org.jsoup.Connection.KeyVal;
import org.jsoup.nodes.*;
import org.jsoup.nodes.Comment;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ast.AstRoot;

public class HtmlVisitor {
	protected Ast.Document.Builder b = Ast.Document.newBuilder();
	protected Document root;
	protected List<boa.types.Ast.Comment> comments = new ArrayList<boa.types.Ast.Comment>();
	protected Stack<boa.types.Ast.Atribute> atributes = new Stack<boa.types.Ast.Atribute>();
	protected Stack<List<boa.types.Ast.Element>> elements = new Stack<List<boa.types.Ast.Element>>();

	public Ast.Document getDocument(Document node) {
		this.root = node;
		visit(node);
		return b.build();
	}

	public void visit(Document node) {
		b.setTitle(node.title());
		elements.push(new ArrayList<boa.types.Ast.Element>());
		for (Node n : node.childNodes()){
			if (n instanceof org.jsoup.nodes.Element)
				visit((org.jsoup.nodes.Element)n);
		}
		for (Ast.Element e : elements.pop())
			b.addElements(e);
	}

	public void visit(org.jsoup.nodes.Element node) {
		Ast.Element.Builder b = Ast.Element.newBuilder();
		String tag = node.tagName();
		b.setTag(tag);
		if (node.isBlock())
			b.setKind(ElementKind.BLOCK);
		else
			b.setKind(ElementKind.IN_LINE);
		
		if (node instanceof FormElement){
			b.setKind(ElementKind.FORM);
			for (KeyVal d : ((FormElement)node).formData()) {
				Ast.Atribute.Builder ab = Ast.Atribute.newBuilder();
				ab.setKey(d.key());
				ab.setValue(d.value());
				b.addAtributes(ab.build());
			}
		}
		for (Attribute n : node.attributes()) {
			visit(n);
			b.addAtributes(atributes.pop());
		}
	
		String text = node.ownText();
		if (text != "")
	//		b.addText(text);
		for (Node n : node.childNodes()) {
			 if (n instanceof org.jsoup.nodes.Element) {
				elements.push(new ArrayList<Ast.Element>());
				visit((org.jsoup.nodes.Element) n);
				for (Ast.Element s : elements.pop())
					b.addElements(s);
			} else if (n instanceof Comment) {
				String comm = ((Comment) n).getData();
				if (comm.startsWith("?php")){
					String php = "<";
					php += comm;
					php += node.ownText();
					Ast.Namespace ns = this.parsePHP(php);
					if (ns != null)
						b.setPhp(ns); 
					else
						b.addData(php);
				} else {
			//	b.addText(comm);
				visit((Comment) n);
				}
			}else if (n instanceof TextNode) {
				String t = ((TextNode) n).text();
				String check = t.replaceAll(" ", "");
				if (!check.equals(""))
					b.addText(t);
			} else if (n instanceof DataNode) {
				if (tag.equals("script")) {
					Ast.Namespace ns = this.parseJs(((DataNode) n).getWholeData());
					if (ns != null)
						b.setScript(ns); 
					else
						b.addData(((DataNode) n).getWholeData());
				} else
					b.addData(((DataNode) n).getWholeData());
			} else if (n instanceof XmlDeclaration) {
				b.addData(((XmlDeclaration) n).getWholeDeclaration());
			}
		}
		elements.peek().add(b.build());
	}


	public void visit(org.jsoup.nodes.Comment node) {
		boa.types.Ast.Comment.Builder b = boa.types.Ast.Comment.newBuilder();
		b.setKind(CommentKind.OTHER);// FIXME
		b.setValue(node.getData());
		comments.add(b.build());
	}

	public void visit(DocumentType node) {
		Expression.Builder b = Expression.newBuilder();
		b.setKind(ExpressionKind.OTHER);// FIXME
		DataNode n;
		// FIXME
	}

	public void visit(org.jsoup.nodes.Attribute node) {
		Ast.Atribute.Builder b = Ast.Atribute.newBuilder();
		b.setKey(node.getKey());
		b.setValue(node.getValue());
		atributes.push(b.build());
	}

	public void visit(Node node) {
		throw new RuntimeException("visited unused node " + node.getClass());
	}

	private Namespace parseJs(String content) {
		CompilerEnvirons cp = new CompilerEnvirons();
		cp.setLanguageVersion(Context.VERSION_1_7);
		cp.setRecoverFromErrors(false);
		// cp.setAllowMemberExprAsFunctionName(true);
		final org.mozilla.javascript.Parser parser = new org.mozilla.javascript.Parser(cp);
		AstRoot cu;
		try {
			cu = parser.parse(content, null, 0);
		} catch (java.lang.IllegalArgumentException ex) {
			return null;
		} catch (org.mozilla.javascript.EvaluatorException ex) {
			return null;
		}
		JavaScriptVisitor visitor = new JavaScriptVisitor(content);
		return visitor.getNamespaces(cu);
	}

	private Namespace parsePHP(String content) {
		ASTParser parser = org.eclipse.php.internal.core.ast.nodes.ASTParser.newParser(PHPVersion.PHP7_1);
		try {
			parser.setSource(content.toCharArray());
		} catch (IOException e) {
			return null;
		}
		Program ast;
		try {
			ast = parser.createAST(null);
		} catch (Exception e) {
			return null;
		}
		if (ast == null)
			return null;
		PHPVisitor visitor = new PHPVisitor(content);
		PHPErrorCheckVisitor errorCheck = new PHPErrorCheckVisitor();
		ast.accept(errorCheck);
		if (errorCheck.hasError)
			return null;
		return visitor.getNamespace(ast);
	}
}
