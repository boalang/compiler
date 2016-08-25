/*
 * Copyright 2015, Hridesh Rajan, Robert Dyer, Hoan Nguyen
 *                 and Iowa State University of Science and Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package boa.datagen.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import org.eclipse.jdt.core.dom.MethodRef;
import org.eclipse.jdt.core.dom.MethodRefParameter;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.wst.jsdt.core.dom.ASTNode;
import org.eclipse.wst.jsdt.core.dom.ASTVisitor;
import org.eclipse.wst.jsdt.core.dom.ArrayAccess;
import org.eclipse.wst.jsdt.core.dom.ArrayCreation;
import org.eclipse.wst.jsdt.core.dom.ArrayInitializer;
import org.eclipse.wst.jsdt.core.dom.ArrayType;
import org.eclipse.wst.jsdt.core.dom.Assignment;
import org.eclipse.wst.jsdt.core.dom.Block;
import org.eclipse.wst.jsdt.core.dom.BlockComment;
import org.eclipse.wst.jsdt.core.dom.BooleanLiteral;
import org.eclipse.wst.jsdt.core.dom.BreakStatement;
import org.eclipse.wst.jsdt.core.dom.CatchClause;
import org.eclipse.wst.jsdt.core.dom.CharacterLiteral;
import org.eclipse.wst.jsdt.core.dom.ClassInstanceCreation;
import org.eclipse.wst.jsdt.core.dom.ConditionalExpression;
import org.eclipse.wst.jsdt.core.dom.ConstructorInvocation;
import org.eclipse.wst.jsdt.core.dom.ContinueStatement;
import org.eclipse.wst.jsdt.core.dom.DoStatement;
import org.eclipse.wst.jsdt.core.dom.EmptyStatement;
import org.eclipse.wst.jsdt.core.dom.EnhancedForStatement;
import org.eclipse.wst.jsdt.core.dom.ExpressionStatement;
import org.eclipse.wst.jsdt.core.dom.FieldAccess;
import org.eclipse.wst.jsdt.core.dom.FieldDeclaration;
import org.eclipse.wst.jsdt.core.dom.ForInStatement;
import org.eclipse.wst.jsdt.core.dom.ForStatement;
import org.eclipse.wst.jsdt.core.dom.FunctionDeclaration;
import org.eclipse.wst.jsdt.core.dom.FunctionExpression;
import org.eclipse.wst.jsdt.core.dom.FunctionInvocation;
import org.eclipse.wst.jsdt.core.dom.FunctionRef;
import org.eclipse.wst.jsdt.core.dom.FunctionRefParameter;
import org.eclipse.wst.jsdt.core.dom.IfStatement;
import org.eclipse.wst.jsdt.core.dom.ImportDeclaration;
import org.eclipse.wst.jsdt.core.dom.InferredType;
import org.eclipse.wst.jsdt.core.dom.InfixExpression;
import org.eclipse.wst.jsdt.core.dom.Initializer;
import org.eclipse.wst.jsdt.core.dom.InstanceofExpression;
import org.eclipse.wst.jsdt.core.dom.JSdoc;
import org.eclipse.wst.jsdt.core.dom.JavaScriptUnit;
import org.eclipse.wst.jsdt.core.dom.LabeledStatement;
import org.eclipse.wst.jsdt.core.dom.LineComment;
import org.eclipse.wst.jsdt.core.dom.ListExpression;
import org.eclipse.wst.jsdt.core.dom.MemberRef;
import org.eclipse.wst.jsdt.core.dom.Name;
import org.eclipse.wst.jsdt.core.dom.NullLiteral;
import org.eclipse.wst.jsdt.core.dom.NumberLiteral;
import org.eclipse.wst.jsdt.core.dom.ObjectLiteral;
import org.eclipse.wst.jsdt.core.dom.ObjectLiteralField;
import org.eclipse.wst.jsdt.core.dom.PackageDeclaration;
import org.eclipse.wst.jsdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.wst.jsdt.core.dom.ParenthesizedExpression;
import org.eclipse.wst.jsdt.core.dom.PostfixExpression;
import org.eclipse.wst.jsdt.core.dom.PrefixExpression;
import org.eclipse.wst.jsdt.core.dom.PrimitiveType;
import org.eclipse.wst.jsdt.core.dom.QualifiedName;
import org.eclipse.wst.jsdt.core.dom.QualifiedType;
import org.eclipse.wst.jsdt.core.dom.RegularExpressionLiteral;
import org.eclipse.wst.jsdt.core.dom.ReturnStatement;
import org.eclipse.wst.jsdt.core.dom.SimpleName;
import org.eclipse.wst.jsdt.core.dom.SimpleType;
import org.eclipse.wst.jsdt.core.dom.SingleVariableDeclaration;
import org.eclipse.wst.jsdt.core.dom.Statement;
import org.eclipse.wst.jsdt.core.dom.BodyDeclaration;
import org.eclipse.wst.jsdt.core.dom.SuperConstructorInvocation;
import org.eclipse.wst.jsdt.core.dom.SuperFieldAccess;
import org.eclipse.wst.jsdt.core.dom.SuperMethodInvocation;
import org.eclipse.wst.jsdt.core.dom.SwitchCase;
import org.eclipse.wst.jsdt.core.dom.SwitchStatement;
import org.eclipse.wst.jsdt.core.dom.TextElement;
import org.eclipse.wst.jsdt.core.dom.ThisExpression;
import org.eclipse.wst.jsdt.core.dom.ThrowStatement;
import org.eclipse.wst.jsdt.core.dom.TryStatement;
import org.eclipse.wst.jsdt.core.dom.TypeDeclaration;
import org.eclipse.wst.jsdt.core.dom.TypeDeclarationStatement;
import org.eclipse.wst.jsdt.core.dom.TypeLiteral;
import org.eclipse.wst.jsdt.core.dom.UndefinedLiteral;
import org.eclipse.wst.jsdt.core.dom.VariableDeclarationExpression;
import org.eclipse.wst.jsdt.core.dom.VariableDeclarationFragment;
import org.eclipse.wst.jsdt.core.dom.VariableDeclarationStatement;
import org.eclipse.wst.jsdt.core.dom.WhileStatement;
import org.eclipse.wst.jsdt.core.dom.WithStatement;

import boa.types.Ast.Declaration;
import boa.types.Ast.Method;
import boa.types.Ast.Namespace;
import boa.types.Ast.PositionInfo;
import boa.types.Ast.Statement.StatementKind;
import boa.types.Ast.Type;
import boa.types.Ast.TypeKind;
import boa.types.Ast.Variable;

/**
 * @author rdyer
 */
public class JavaScriptVisitor extends ASTVisitor {
	private HashMap<String, Integer> nameIndices;
	
	private JavaScriptUnit root = null;
	private PositionInfo.Builder pos = null;
	private String src = null;

	private Namespace.Builder b = Namespace.newBuilder();
	private List<boa.types.Ast.Comment> comments = new ArrayList<boa.types.Ast.Comment>();
	private List<String> imports = new ArrayList<String>();
	private Stack<List<boa.types.Ast.Declaration>> declarations = new Stack<List<boa.types.Ast.Declaration>>();
	private Stack<boa.types.Ast.Modifier> modifiers = new Stack<boa.types.Ast.Modifier>();
	private Stack<boa.types.Ast.Expression> expressions = new Stack<boa.types.Ast.Expression>();
	private Stack<List<boa.types.Ast.Variable>> fields = new Stack<List<boa.types.Ast.Variable>>();
	private Stack<List<boa.types.Ast.Method>> methods = new Stack<List<boa.types.Ast.Method>>();
	private Stack<List<boa.types.Ast.Statement>> statements = new Stack<List<boa.types.Ast.Statement>>();

	public JavaScriptVisitor(String src) {
		super();
		this.src = src;
	}

	public Namespace getNamespaces(JavaScriptUnit node) {
		root = node;
		node.accept(this);
		return b.build();
	}

	public List<boa.types.Ast.Comment> getComments() {
		return comments;
	}

	public List<String> getImports() {
		return imports;
	}

/*
	public void preVisit(ASTNode node) {
		buildPosition(node);
	}
*/

	private void buildPosition(final ASTNode node) {
		pos = PositionInfo.newBuilder();
		int start = node.getStartPosition();
		int length = node.getLength();
		pos.setStartPos(start);
		pos.setLength(length);
		pos.setStartLine(root.getLineNumber(start));
		pos.setStartCol(root.getColumnNumber(start));
		pos.setEndLine(root.getLineNumber(start + length));
		pos.setEndCol(root.getColumnNumber(start + length));
	}

	@Override
	public boolean visit(JavaScriptUnit node) {
//		b.setPosition(pos.build());
		PackageDeclaration pkg = node.getPackage();
		if (pkg == null) {
			b.setName("");
		} else {
			b.setName(pkg.getName().getFullyQualifiedName());
		}
		for (Object i : node.imports()) {
			ImportDeclaration id = (ImportDeclaration)i;
			String imp = "";
			if (id.isStatic())
				imp += "static ";
			imp += id.getName().getFullyQualifiedName();
			if (id.isOnDemand())
				imp += ".*";
			imports.add(imp);
		}
		for (Object t : node.types()) {
			declarations.push(new ArrayList<boa.types.Ast.Declaration>());
			((AbstractTypeDeclaration)t).accept(this);
			for (boa.types.Ast.Declaration d : declarations.pop())
				b.addDeclarations(d);
		}
		for (Object c : node.getCommentList())
			((org.eclipse.wst.jsdt.core.dom.Comment)c).accept(this);
		
		if (!node.statements().isEmpty()) {
			Declaration.Builder db = Declaration.newBuilder();
			db.setName("Default");
			db.setKind(TypeKind.CLASS);
			Method.Builder mb = Method.newBuilder();
			mb.setName("default");
			Type.Builder tb = Type.newBuilder();
			tb.setKind(TypeKind.CLASS);
			tb.setName(getIndex("Default"));
			mb.setReturnType(tb);
			for(Object s:node.statements()){
				if (s instanceof FunctionDeclaration) {
					methods.push(new ArrayList<boa.types.Ast.Method>());
					((FunctionDeclaration) s).accept(this);
					for (boa.types.Ast.Method m : methods.pop())
						db.addMethods(m);
				}
				else {
					statements.push(new ArrayList<boa.types.Ast.Statement>());
					((Statement)s).accept(this);
					for (boa.types.Ast.Statement d : statements.pop())
						mb.addStatements(d);
				}
			}
			db.addMethods(mb);
			b.addDeclarations(db);
		}
		return false;
	}
	
	
	
	@Override
	public boolean visit(org.eclipse.wst.jsdt.core.dom.AnonymousClassDeclaration node) {
		boa.types.Ast.Declaration.Builder b = boa.types.Ast.Declaration.newBuilder();
//		b.setPosition(pos.build());
		b.setName("");
		b.setKind(boa.types.Ast.TypeKind.ANONYMOUS);
		for (Object d : node.bodyDeclarations()) {
			if (d instanceof FieldDeclaration) {
				fields.push(new ArrayList<boa.types.Ast.Variable>());
				((FieldDeclaration)d).accept(this);
				for (boa.types.Ast.Variable v : fields.pop())
					b.addFields(v);
			} else if (d instanceof FunctionDeclaration) {
				methods.push(new ArrayList<boa.types.Ast.Method>());
				((FunctionDeclaration)d).accept(this);
				for (boa.types.Ast.Method m : methods.pop())
					b.addMethods(m);
			} else if (d instanceof Initializer) {
				methods.push(new ArrayList<boa.types.Ast.Method>());
				((Initializer)d).accept(this);
				for (boa.types.Ast.Method m : methods.pop())
					b.addMethods(m);
			} else {
				declarations.push(new ArrayList<boa.types.Ast.Declaration>());
				((BodyDeclaration)d).accept(this);
				for (boa.types.Ast.Declaration nd : declarations.pop())
					b.addNestedDeclarations(nd);
			}
		}
		declarations.peek().add(b.build());
		return false;
	}

	@Override
	public boolean visit(BlockComment node) {
		boa.types.Ast.Comment.Builder b = boa.types.Ast.Comment.newBuilder();
		buildPosition(node);
		b.setPosition(pos.build());
		b.setKind(boa.types.Ast.Comment.CommentKind.BLOCK);
		b.setValue(src.substring(node.getStartPosition(), node.getStartPosition() + node.getLength()));
		comments.add(b.build());
		return false;
	}

	@Override
	public boolean visit(LineComment node) {
		boa.types.Ast.Comment.Builder b = boa.types.Ast.Comment.newBuilder();
		buildPosition(node);
		b.setPosition(pos.build());
		b.setKind(boa.types.Ast.Comment.CommentKind.LINE);
		b.setValue(src.substring(node.getStartPosition(), node.getStartPosition() + node.getLength()));
		comments.add(b.build());
		return false;
	}

	@Override
	public boolean visit(JSdoc node) {
		boa.types.Ast.Comment.Builder b = boa.types.Ast.Comment.newBuilder();
		buildPosition(node);
		b.setPosition(pos.build());
		b.setKind(boa.types.Ast.Comment.CommentKind.DOC);
		b.setValue(src.substring(node.getStartPosition(), node.getStartPosition() + node.getLength()));
		comments.add(b.build());
		return false;
	}

	//////////////////////////////////////////////////////////////
	// Type Declarations

	@Override
	public boolean visit(TypeDeclaration node) {
		boa.types.Ast.Declaration.Builder b = boa.types.Ast.Declaration.newBuilder();
		b.setName(node.getName().getFullyQualifiedName());

			b.setKind(boa.types.Ast.TypeKind.CLASS);
		for (Object m : node.modifiers()) {

				((org.eclipse.wst.jsdt.core.dom.Modifier)m).accept(this);
			b.addModifiers(modifiers.pop());
		}

		if (node.getSuperclassType() != null) {
			boa.types.Ast.Type.Builder tb = boa.types.Ast.Type.newBuilder();
			tb.setName(getIndex(typeName(node.getSuperclassType())));
			tb.setKind(boa.types.Ast.TypeKind.CLASS);
			b.addParents(tb.build());
		}

		for (Object d : node.bodyDeclarations()) {
			if (d instanceof FieldDeclaration) {
				fields.push(new ArrayList<boa.types.Ast.Variable>());
				((FieldDeclaration)d).accept(this);
				for (boa.types.Ast.Variable v : fields.pop())
					b.addFields(v);
			}  else if (d instanceof Initializer) {
				methods.push(new ArrayList<boa.types.Ast.Method>());
				((Initializer)d).accept(this);
				for (boa.types.Ast.Method m : methods.pop())
					b.addMethods(m);
			} else {
				declarations.push(new ArrayList<boa.types.Ast.Declaration>());
				((BodyDeclaration)d).accept(this);
				for (boa.types.Ast.Declaration nd : declarations.pop())
					b.addNestedDeclarations(nd);
			}
		}
		// TODO initializers
		// TODO enum constants
		// TODO annotation type members
		declarations.peek().add(b.build());
		return false;
	}
//
//	public boolean visit(AnonymousClassDeclaration node) {
//		boa.types.Ast.Declaration.Builder b = boa.types.Ast.Declaration.newBuilder();
////		b.setPosition(pos.build());
//		b.setName("");
//		b.setKind(boa.types.Ast.TypeKind.ANONYMOUS);
//		for (Object d : node.bodyDeclarations()) {
//			if (d instanceof FieldDeclaration) {
//				fields.push(new ArrayList<boa.types.Ast.Variable>());
//				((FieldDeclaration)d).accept(this);
//				for (boa.types.Ast.Variable v : fields.pop())
//					b.addFields(v);
//			}  else if (d instanceof Initializer) {
//				methods.push(new ArrayList<boa.types.Ast.Method>());
//				((Initializer)d).accept(this);
//				for (boa.types.Ast.Method m : methods.pop())
//					b.addMethods(m);
//			} else {
//				declarations.push(new ArrayList<boa.types.Ast.Declaration>());
//				((BodyDeclaration)d).accept(this);
//				for (boa.types.Ast.Declaration nd : declarations.pop())
//					b.addNestedDeclarations(nd);
//			}
//		}
//		declarations.peek().add(b.build());
//		return false;
//	}





	//////////////////////////////////////////////////////////////
	// Field/Method Declarations





	@Override
	public boolean visit(FieldDeclaration node) {
		List<boa.types.Ast.Variable> list = fields.peek();
		for (Object o : node.fragments()) {
			VariableDeclarationFragment f = (VariableDeclarationFragment)o;
			Variable.Builder b = Variable.newBuilder();
//			b.setPosition(pos.build()); // FIXME
			b.setName(f.getName().getFullyQualifiedName());
			for (Object m : node.modifiers()) {

					((org.eclipse.wst.jsdt.core.dom.Modifier)m).accept(this);
				b.addModifiers(modifiers.pop());
			}
			boa.types.Ast.Type.Builder tb = boa.types.Ast.Type.newBuilder();
			String name = typeName(node.getType());
			for (int i = 0; i < f.getExtraDimensions(); i++)
				name += "[]";
			tb.setName(getIndex(name));
			tb.setKind(boa.types.Ast.TypeKind.OTHER);
			b.setVariableType(tb.build());
			if (f.getInitializer() != null) {
				f.getInitializer().accept(this);
				b.setInitializer(expressions.pop());
			}
			list.add(b.build());
		}
		return false;
	}

	//////////////////////////////////////////////////////////////
	// Modifiers and Annotations
	@Override
	public boolean visit(org.eclipse.wst.jsdt.core.dom.Modifier node) {
		boa.types.Ast.Modifier.Builder b = boa.types.Ast.Modifier.newBuilder();
//		b.setPosition(pos.build());
		if (node.isFinal())
			b.setKind(boa.types.Ast.Modifier.ModifierKind.FINAL);
		else if (node.isAbstract())
			b.setKind(boa.types.Ast.Modifier.ModifierKind.ABSTRACT);
		else if (node.isStatic())
			b.setKind(boa.types.Ast.Modifier.ModifierKind.STATIC);
		else if (node.isSynchronized())
			b.setKind(boa.types.Ast.Modifier.ModifierKind.SYNCHRONIZED);
		else if (node.isPublic()) {
			b.setKind(boa.types.Ast.Modifier.ModifierKind.VISIBILITY);
			b.setVisibility(boa.types.Ast.Modifier.Visibility.PUBLIC);
		} else if (node.isPrivate()) {
			b.setKind(boa.types.Ast.Modifier.ModifierKind.VISIBILITY);
			b.setVisibility(boa.types.Ast.Modifier.Visibility.PRIVATE);
		} else if (node.isProtected()) {
			b.setKind(boa.types.Ast.Modifier.ModifierKind.VISIBILITY);
			b.setVisibility(boa.types.Ast.Modifier.Visibility.PROTECTED);
		} else {
			b.setKind(boa.types.Ast.Modifier.ModifierKind.OTHER);
			b.setOther(node.getKeyword().toString());
		}
		modifiers.push(b.build());
		return false;
	}

	//////////////////////////////////////////////////////////////
	// Statements



	@Override
	public boolean visit(Block node) {
		boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
//		b.setPosition(pos.build());
		List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.BLOCK);
		for (Object s : node.statements()) {

			//TODO: NonJava
			if(s instanceof FunctionDeclaration){
				methods.push(new ArrayList<boa.types.Ast.Method>());
				((FunctionDeclaration)s).accept(this);
				b.setKind(StatementKind.OTHER);
				
			}else{
				statements.push(new ArrayList<boa.types.Ast.Statement>());
				((org.eclipse.wst.jsdt.core.dom.Statement)s).accept(this);
				for (boa.types.Ast.Statement st : statements.pop())
					b.addStatements(st);	
			}
		}
		list.add(b.build());
		return false;
	}

	@Override
	public boolean visit(BreakStatement node) {
		boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
//		b.setPosition(pos.build());
		List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.BREAK);
		if (node.getLabel() != null) {
			boa.types.Ast.Expression.Builder eb = boa.types.Ast.Expression.newBuilder();
//			eb.setPosition(pos.build()); // FIXME
			eb.setLiteral(node.getLabel().getFullyQualifiedName());
			eb.setKind(boa.types.Ast.Expression.ExpressionKind.LITERAL);
			b.setExpression(eb.build());
		}
		list.add(b.build());
		return false;
	}

	@Override
	public boolean visit(CatchClause node) {
		boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
//		b.setPosition(pos.build());
		List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.CATCH);
		org.eclipse.wst.jsdt.core.dom.SingleVariableDeclaration ex = node.getException();
		Variable.Builder vb = Variable.newBuilder();
//		vb.setPosition(pos.build());// FIXME
		vb.setName(ex.getName().getFullyQualifiedName());
		for (Object m : ex.modifiers()) {

				((org.eclipse.wst.jsdt.core.dom.Modifier)m).accept(this);
			vb.addModifiers(modifiers.pop());
		}
		boa.types.Ast.Type.Builder tb = boa.types.Ast.Type.newBuilder();
		String name = typeName(ex.getType());
		for (int i = 0; i < ex.getExtraDimensions(); i++)
			name += "[]";
		tb.setName(getIndex(name));
		tb.setKind(boa.types.Ast.TypeKind.OTHER);
		vb.setVariableType(tb.build());
		if (ex.getInitializer() != null) {
			ex.getInitializer().accept(this);
			vb.setInitializer(expressions.pop());
		}
		b.setVariableDeclaration(vb.build());
		statements.push(new ArrayList<boa.types.Ast.Statement>());
		for (Object s : node.getBody().statements())
			((org.eclipse.wst.jsdt.core.dom.Statement)s).accept(this);
		for (boa.types.Ast.Statement s : statements.pop())
			b.addStatements(s);
		list.add(b.build());
		return false;
	}

	@Override
	public boolean visit(ConstructorInvocation node) {
		boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
//		b.setPosition(pos.build());
		List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.EXPRESSION);
		boa.types.Ast.Expression.Builder eb = boa.types.Ast.Expression.newBuilder();
//		eb.setPosition(pos.build());//FIXME
		eb.setKind(boa.types.Ast.Expression.ExpressionKind.METHODCALL);
		eb.setMethod("<init>");
		for (Object a : node.arguments()) {
			((org.eclipse.wst.jsdt.core.dom.Statement)a).accept(this);
			eb.addMethodArgs(expressions.pop());
		}
		for (Object t : node.typeArguments()) {
			boa.types.Ast.Type.Builder tb = boa.types.Ast.Type.newBuilder();
			tb.setName(getIndex(typeName((org.eclipse.wst.jsdt.core.dom.Type)t)));
			tb.setKind(boa.types.Ast.TypeKind.GENERIC);
			eb.addGenericParameters(tb.build());
		}
		b.setExpression(eb.build());
		list.add(b.build());
		return false;
	}

	@Override
	public boolean visit(ContinueStatement node) {
		boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
//		b.setPosition(pos.build());
		List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.CONTINUE);
		if (node.getLabel() != null) {
			boa.types.Ast.Expression.Builder eb = boa.types.Ast.Expression.newBuilder();
//			eb.setPosition(pos.build());//FIXME
			eb.setLiteral(node.getLabel().getFullyQualifiedName());
			eb.setKind(boa.types.Ast.Expression.ExpressionKind.LITERAL);
			b.setExpression(eb.build());
		}
		list.add(b.build());
		return false;
	}

	@Override
	public boolean visit(DoStatement node) {
		boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
//		b.setPosition(pos.build());
		List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.DO);
		node.getExpression().accept(this);
		b.setExpression(expressions.pop());
		statements.push(new ArrayList<boa.types.Ast.Statement>());
		node.getBody().accept(this);
		for (boa.types.Ast.Statement s : statements.pop())
			b.addStatements(s);
		list.add(b.build());
		return false;
	}

	@Override
	public boolean visit(EmptyStatement node) {
		boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
//		b.setPosition(pos.build());
		List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.EMPTY);
		list.add(b.build());
		return false;
	}

	@Override
	public boolean visit(EnhancedForStatement node) {
		boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
//		b.setPosition(pos.build());
		List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.FOR);
		org.eclipse.wst.jsdt.core.dom.SingleVariableDeclaration ex = node.getParameter();
		Variable.Builder vb = Variable.newBuilder();
//		vb.setPosition(pos.build());//FIXME
		vb.setName(ex.getName().getFullyQualifiedName());
		for (Object m : ex.modifiers()) {

				((org.eclipse.wst.jsdt.core.dom.Modifier)m).accept(this);
			vb.addModifiers(modifiers.pop());
		}
		boa.types.Ast.Type.Builder tb = boa.types.Ast.Type.newBuilder();
		String name = typeName(ex.getType());
		for (int i = 0; i < ex.getExtraDimensions(); i++)
			name += "[]";
		tb.setName(getIndex(name));
		tb.setKind(boa.types.Ast.TypeKind.OTHER);
		vb.setVariableType(tb.build());
		if (ex.getInitializer() != null) {
			ex.getInitializer().accept(this);
			vb.setInitializer(expressions.pop());
		}
		b.setVariableDeclaration(vb.build());
		node.getExpression().accept(this);
		b.setExpression(expressions.pop());
		statements.push(new ArrayList<boa.types.Ast.Statement>());
		node.getBody().accept(this);
		for (boa.types.Ast.Statement s : statements.pop())
			b.addStatements(s);
		list.add(b.build());
		return false;
	}

	
	@Override
	public boolean visit(ForInStatement node){
		boa.types.Ast.Statement.Builder s = boa.types.Ast.Statement.newBuilder();
		List<boa.types.Ast.Statement> list = statements.peek();
		s.setKind(boa.types.Ast.Statement.StatementKind.FOR);
		statements.push(new ArrayList<boa.types.Ast.Statement>());
		node.getBody().accept(this);
		for (boa.types.Ast.Statement x : statements.pop())
			s.addStatements(x);
		
		statements.push(new ArrayList<boa.types.Ast.Statement>());
		node.getIterationVariable().accept(this);
		for (boa.types.Ast.Statement x : statements.pop())
			s.addStatements(x);
		
		node.getCollection().accept(this);
		s.setExpression(expressions.pop());
		list.add(s.build());
		return false;
	}
	
	
	@Override
	public boolean visit(FunctionInvocation node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
//		b.setPosition(pos.build());
		b.setKind(boa.types.Ast.Expression.ExpressionKind.METHODCALL);
		if(node.getName() != null)
		b.setMethod(node.getName().getFullyQualifiedName());
		if (node.getExpression() != null) {
			node.getExpression().accept(this);
			b.addExpressions(expressions.pop());
		}
		
		for (Object a : node.arguments()) {
			((org.eclipse.wst.jsdt.core.dom.Expression)a).accept(this);
			b.addMethodArgs(expressions.pop());
		}
		for (Object t : node.typeArguments()) {
			boa.types.Ast.Type.Builder tb = boa.types.Ast.Type.newBuilder();
			tb.setName(getIndex(typeName((org.eclipse.wst.jsdt.core.dom.Type)t)));
			tb.setKind(boa.types.Ast.TypeKind.GENERIC);
			b.addGenericParameters(tb.build());
		}
		expressions.push(b.build());
		return false;
	}
	
	@Override
	public boolean visit(ListExpression node){
		boa.types.Ast.Expression.Builder bui = boa.types.Ast.Expression.newBuilder();
//		b.setPosition(pos.build());
		bui.setKind(boa.types.Ast.Expression.ExpressionKind.OTHER);		
		for (Object a : node.expressions()) {
			((org.eclipse.wst.jsdt.core.dom.Expression)a).accept(this);
			bui.addExpressions(expressions.pop());
		}		
		expressions.push(bui.build());
		return false;
	}
	
	public boolean visit(Name node){
		boa.types.Ast.Expression.Builder bui = boa.types.Ast.Expression.newBuilder();
//		b.setPosition(pos.build());
		bui.setVariable(node.getFullyQualifiedName());
		bui.setKind(boa.types.Ast.Expression.ExpressionKind.OTHER);		
		((org.eclipse.wst.jsdt.core.dom.Expression)node).accept(this);
		bui.addExpressions(expressions.pop());
		expressions.push(bui.build());
		return false;
	}
	
	@Override
	public boolean visit(ExpressionStatement node) {
		boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
//		b.setPosition(pos.build());
		List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.EXPRESSION);
		node.getExpression().accept(this);
		b.setExpression(expressions.pop());
		list.add(b.build());
		return false;
	}
	
	@Override
	public boolean visit(WithStatement node) {
		boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
//		b.setPosition(pos.build());
		List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.OTHER);
		node.getExpression().accept(this);
		b.setExpression(expressions.pop());
		node.getBody().accept(this);
		for (boa.types.Ast.Statement s : statements.pop())
			b.addStatements(s);
		list.add(b.build());
		return false;
	}

	
	@Override
	public boolean visit(TextElement node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
//		b.setPosition(pos.build());
		b.setKind(boa.types.Ast.Expression.ExpressionKind.OTHER);
		b.setLiteral(node.getText());
		expressions.push(b.build());
		return false;
	}
	
	
	@Override
	public boolean visit(ForStatement node) {
		boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
//		b.setPosition(pos.build());
		List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.FOR);
		for (Object e : node.initializers()) {
			((org.eclipse.wst.jsdt.core.dom.Expression)e).accept(this);
			b.addInitializations(expressions.pop());
		}
		for (Object e : node.updaters()) {
			((org.eclipse.wst.jsdt.core.dom.Expression)e).accept(this);
			b.addUpdates(expressions.pop());
		}
		if (node.getExpression() != null) {
			node.getExpression().accept(this);
			b.setExpression(expressions.pop());
		}
		statements.push(new ArrayList<boa.types.Ast.Statement>());
		node.getBody().accept(this);
		for (boa.types.Ast.Statement s : statements.pop())
			b.addStatements(s);
		list.add(b.build());
		return false;
	}
	


	@Override
	public boolean visit(IfStatement node) {
		boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
//		b.setPosition(pos.build());
		List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.IF);
		node.getExpression().accept(this);
		b.setExpression(expressions.pop());
		statements.push(new ArrayList<boa.types.Ast.Statement>());
		node.getThenStatement().accept(this);
		for (boa.types.Ast.Statement s : statements.pop())
			b.addStatements(s);
		// FIXME
		if (node.getElseStatement() != null) {
			statements.push(new ArrayList<boa.types.Ast.Statement>());
			node.getElseStatement().accept(this);
			for (boa.types.Ast.Statement s : statements.pop())
				b.addStatements(s);
		}
		list.add(b.build());
		return false;
	}

	@Override
	public boolean visit(Initializer node) {
		List<boa.types.Ast.Method> list = methods.peek();
		Method.Builder b = Method.newBuilder();
//		b.setPosition(pos.build());
		b.setName("<clinit>");
		for (Object m : node.modifiers()) {
				((org.eclipse.wst.jsdt.core.dom.Modifier)m).accept(this);
			b.addModifiers(modifiers.pop());
		}
		boa.types.Ast.Type.Builder tb = boa.types.Ast.Type.newBuilder();
		tb.setName(getIndex("void"));
		tb.setKind(boa.types.Ast.TypeKind.OTHER);
		b.setReturnType(tb.build());
		if (node.getBody() != null) {
			statements.push(new ArrayList<boa.types.Ast.Statement>());
			node.getBody().accept(this);
			for (boa.types.Ast.Statement s : statements.pop())
				b.addStatements(s);
		}
		list.add(b.build());
		return false;
	}
	
	
	

	@Override
	public boolean visit(LabeledStatement node) {
		boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
//		b.setPosition(pos.build());
		List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.LABEL);
		statements.push(new ArrayList<boa.types.Ast.Statement>());
		node.getBody().accept(this);
		for (boa.types.Ast.Statement s : statements.pop())
			b.addStatements(s);
		boa.types.Ast.Expression.Builder eb = boa.types.Ast.Expression.newBuilder();
//		eb.setPosition(pos.build());//FIXME
		eb.setKind(boa.types.Ast.Expression.ExpressionKind.LITERAL);
		eb.setLiteral(node.getLabel().getFullyQualifiedName());
		b.setExpression(eb.build());
		list.add(b.build());
		return false;
	}

	
	@Override
	public boolean visit(FunctionDeclaration node) {
		List<boa.types.Ast.Method> list = methods.peek();
		Method.Builder b = Method.newBuilder();
//		b.setPosition(pos.build());
		if (node.isConstructor())
			b.setName("<init>");
		else{
			if(node.getName() != null)
			b.setName(node.getName().getFullyQualifiedName());
		}
		for (Object m : node.modifiers()) {
				((org.eclipse.wst.jsdt.core.dom.Modifier)m).accept(this);
			b.addModifiers(modifiers.pop());
		}
		boa.types.Ast.Type.Builder tb = boa.types.Ast.Type.newBuilder();
		if (node.getReturnType2() != null) {
			String name = typeName(node.getReturnType2());
			for (int i = 0; i < node.getExtraDimensions(); i++)
				name += "[]";
			tb.setName(getIndex(name));
			tb.setKind(boa.types.Ast.TypeKind.OTHER);
			b.setReturnType(tb.build());
		} else {
			tb.setName(getIndex("void"));
			tb.setKind(boa.types.Ast.TypeKind.OTHER);
			b.setReturnType(tb.build());
		}

		for (Object o : node.parameters()) {
			SingleVariableDeclaration ex = (SingleVariableDeclaration)o;
			Variable.Builder vb = Variable.newBuilder();
//			vb.setPosition(pos.build()); // FIXME
			vb.setName(ex.getName().getFullyQualifiedName());
			for (Object m : ex.modifiers()) {
					((org.eclipse.wst.jsdt.core.dom.Modifier)m).accept(this);
				vb.addModifiers(modifiers.pop());
			}
			boa.types.Ast.Type.Builder tp = boa.types.Ast.Type.newBuilder();
			String name = typeName(ex.getType());
			for (int i = 0; i < ex.getExtraDimensions(); i++)
				name += "[]";
			if (ex.isVarargs())
				name += "...";
			tp.setName(getIndex(name));
			tp.setKind(boa.types.Ast.TypeKind.OTHER);
			vb.setVariableType(tp.build());
			if (ex.getInitializer() != null) {
				ex.getInitializer().accept(this);
				vb.setInitializer(expressions.pop());
			}
			b.addArguments(vb.build());
		}
		for (Object o : node.thrownExceptions()) {
			boa.types.Ast.Type.Builder tp = boa.types.Ast.Type.newBuilder();
			tp.setName(getIndex(((Name)o).getFullyQualifiedName()));
			tp.setKind(boa.types.Ast.TypeKind.CLASS);
			b.addExceptionTypes(tp.build());
		}
		if (node.getBody() != null) {
			statements.push(new ArrayList<boa.types.Ast.Statement>());
			node.getBody().accept(this);
			for (boa.types.Ast.Statement s : statements.pop())
				b.addStatements(s);
		}
		list.add(b.build());
		return false;
	}
	
	@Override
	public boolean visit(ReturnStatement node) {
		boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
//		b.setPosition(pos.build());
		List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.RETURN);
		if (node.getExpression() != null) {
			node.getExpression().accept(this);
			b.setExpression(expressions.pop());
		}
		list.add(b.build());
		return false;
	}

	@Override
	public boolean visit(SuperConstructorInvocation node) {
		boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
//		b.setPosition(pos.build());
		List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.EXPRESSION);
		boa.types.Ast.Expression.Builder eb = boa.types.Ast.Expression.newBuilder();
//		eb.setPosition(pos.build());//FIXME
		eb.setKind(boa.types.Ast.Expression.ExpressionKind.METHODCALL);
		eb.setMethod("super");
		if (node.getExpression() != null) {
			node.getExpression().accept(this);
			eb.addExpressions(expressions.pop());
		}
		for (Object a : node.arguments()) {
			((org.eclipse.wst.jsdt.core.dom.Expression)a).accept(this);
			eb.addMethodArgs(expressions.pop());
		}
		for (Object t : node.typeArguments()) {
			boa.types.Ast.Type.Builder tb = boa.types.Ast.Type.newBuilder();
			tb.setName(getIndex(typeName((org.eclipse.wst.jsdt.core.dom.Type)t)));
			tb.setKind(boa.types.Ast.TypeKind.GENERIC);
			eb.addGenericParameters(tb.build());
		}
		b.setExpression(eb.build());
		list.add(b.build());
		return false;
	}

	@Override
	public boolean visit(SwitchCase node) {
		boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
//		b.setPosition(pos.build());
		List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.CASE);
		if (node.getExpression() != null) {
			node.getExpression().accept(this);
			b.setExpression(expressions.pop());
		}
		list.add(b.build());
		return false;
	}

	@Override
	public boolean visit(SwitchStatement node) {
		boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
//		b.setPosition(pos.build());
		List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.SWITCH);
		node.getExpression().accept(this);
		b.setExpression(expressions.pop());
		statements.push(new ArrayList<boa.types.Ast.Statement>());
		for (Object s : node.statements())
			((org.eclipse.wst.jsdt.core.dom.Statement)s).accept(this);
		for (boa.types.Ast.Statement s : statements.pop())
			b.addStatements(s);
		list.add(b.build());
		return false;
	}


	@Override
	public boolean visit(ThrowStatement node) {
		boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
//		b.setPosition(pos.build());
		List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.THROW);
		node.getExpression().accept(this);
		b.setExpression(expressions.pop());
		list.add(b.build());
		return false;
	}

	@Override
	public boolean visit(TryStatement node) {
		boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
//		b.setPosition(pos.build());
		List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.TRY);
		statements.push(new ArrayList<boa.types.Ast.Statement>());
		node.getBody().accept(this);
		for (Object c : node.catchClauses())
			((CatchClause)c).accept(this);
		if (node.getFinally() != null)
			node.getFinally().accept(this);
		for (boa.types.Ast.Statement s : statements.pop())
			b.addStatements(s);

		list.add(b.build());
		return false;
	}

	@Override
	public boolean visit(TypeDeclarationStatement node) {
		boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
//		b.setPosition(pos.build());
		List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.TYPEDECL);
		declarations.push(new ArrayList<boa.types.Ast.Declaration>());
		node.getDeclaration().accept(this);
		for (boa.types.Ast.Declaration d : declarations.pop())
			b.setTypeDeclaration(d);
		list.add(b.build());
		return false;
	}

	@Override
	public boolean visit(VariableDeclarationStatement node) {
		boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
//		b.setPosition(pos.build());
		List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.EXPRESSION);
		boa.types.Ast.Expression.Builder eb = boa.types.Ast.Expression.newBuilder();
//		eb.setPosition(pos.build());//FIXME
		eb.setKind(boa.types.Ast.Expression.ExpressionKind.VARDECL);
		for (Object o : node.fragments()) {
			VariableDeclarationFragment f = (VariableDeclarationFragment)o;
			Variable.Builder vb = Variable.newBuilder();
//			vb.setPosition(pos.build());//FIXME
			vb.setName(f.getName().getFullyQualifiedName());
			for (Object m : node.modifiers()) {
					((org.eclipse.wst.jsdt.core.dom.Modifier)m).accept(this);
				vb.addModifiers(modifiers.pop());
			}
			boa.types.Ast.Type.Builder tb = boa.types.Ast.Type.newBuilder();
			if(node.getType() != null){
				String name = typeName(node.getType());
				for (int i = 0; i < f.getExtraDimensions(); i++)
					name += "[]";
				tb.setName(getIndex(name));
			}

			tb.setKind(boa.types.Ast.TypeKind.OTHER);
			vb.setVariableType(tb.build());
			if (f.getInitializer() != null) {
				f.getInitializer().accept(this);
				vb.setInitializer(expressions.pop());
			}
			eb.addVariableDecls(vb.build());
		}
		b.setExpression(eb.build());
		list.add(b.build());
		return false;
	}

	@Override
	public boolean visit(WhileStatement node) {
		boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
//		b.setPosition(pos.build());
		List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.WHILE);
		node.getExpression().accept(this);
		b.setExpression(expressions.pop());
		statements.push(new ArrayList<boa.types.Ast.Statement>());
		node.getBody().accept(this);
		for (boa.types.Ast.Statement s : statements.pop())
			b.addStatements(s);
		list.add(b.build());
		return false;
	}

	//////////////////////////////////////////////////////////////
	// Expressions

	@Override
	public boolean visit(ArrayAccess node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
//		b.setPosition(pos.build());
		b.setKind(boa.types.Ast.Expression.ExpressionKind.ARRAYINDEX);
		node.getArray().accept(this);
		b.addExpressions(expressions.pop());
		node.getIndex().accept(this);
		b.addExpressions(expressions.pop());
		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(ArrayCreation node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
//		b.setPosition(pos.build());
		b.setKind(boa.types.Ast.Expression.ExpressionKind.NEWARRAY);
		boa.types.Ast.Type.Builder tb = boa.types.Ast.Type.newBuilder();
		tb.setName(getIndex(typeName(node.getType())));
		tb.setKind(boa.types.Ast.TypeKind.OTHER);
		b.setNewType(tb.build());
		for (Object e : node.dimensions()) {
			((org.eclipse.wst.jsdt.core.dom.Expression)e).accept(this);
			b.addExpressions(expressions.pop());
		}
		if (node.getInitializer() != null) {
			node.getInitializer().accept(this);
			// FIXME
			b.addExpressions(expressions.pop());
		}
		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(ArrayInitializer node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
//		b.setPosition(pos.build());
		b.setKind(boa.types.Ast.Expression.ExpressionKind.ARRAYINIT);
		for (Object e : node.expressions()) {
			((org.eclipse.wst.jsdt.core.dom.Expression)e).accept(this);
			if (expressions.empty()) {
				boa.types.Ast.Expression.Builder eb = boa.types.Ast.Expression.newBuilder();
	//			eb.setPosition(pos.build()); // FIXME
				eb.setKind(boa.types.Ast.Expression.ExpressionKind.ANNOTATION);
	//			eb.setAnnotation(modifiers.pop());
				b.addExpressions(eb.build());
			} else {
				b.addExpressions(expressions.pop());
			}
		}
		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(Assignment node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
//		b.setPosition(pos.build());
		node.getLeftHandSide().accept(this);
		b.addExpressions(expressions.pop());
		node.getRightHandSide().accept(this);
		b.addExpressions(expressions.pop());
		if (node.getOperator() == Assignment.Operator.ASSIGN)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.ASSIGN);
		else if (node.getOperator() == Assignment.Operator.BIT_AND_ASSIGN)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.ASSIGN_BITAND);
		else if (node.getOperator() == Assignment.Operator.BIT_OR_ASSIGN)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.ASSIGN_BITOR);
		else if (node.getOperator() == Assignment.Operator.BIT_XOR_ASSIGN)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.ASSIGN_BITXOR);
		else if (node.getOperator() == Assignment.Operator.DIVIDE_ASSIGN)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.ASSIGN_DIV);
		else if (node.getOperator() == Assignment.Operator.LEFT_SHIFT_ASSIGN)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.ASSIGN_LSHIFT);
		else if (node.getOperator() == Assignment.Operator.MINUS_ASSIGN)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.ASSIGN_SUB);
		else if (node.getOperator() == Assignment.Operator.PLUS_ASSIGN)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.ASSIGN_ADD);
		else if (node.getOperator() == Assignment.Operator.REMAINDER_ASSIGN)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.ASSIGN_MOD);
		else if (node.getOperator() == Assignment.Operator.RIGHT_SHIFT_SIGNED_ASSIGN)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.ASSIGN_RSHIFT);
		else if (node.getOperator() == Assignment.Operator.RIGHT_SHIFT_UNSIGNED_ASSIGN)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.ASSIGN_UNSIGNEDRSHIFT);
		else if (node.getOperator() == Assignment.Operator.TIMES_ASSIGN)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.ASSIGN_MULT);
		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(BooleanLiteral node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
//		b.setPosition(pos.build());
		b.setKind(boa.types.Ast.Expression.ExpressionKind.LITERAL);
		if (node.booleanValue())
			b.setLiteral("true");
		else
			b.setLiteral("false");
		expressions.push(b.build());
		return false;
	}


	@Override
	public boolean visit(CharacterLiteral node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
//		b.setPosition(pos.build());
		b.setKind(boa.types.Ast.Expression.ExpressionKind.LITERAL);
		b.setLiteral(node.getEscapedValue());
		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(ClassInstanceCreation node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
//		b.setPosition(pos.build());
		b.setKind(boa.types.Ast.Expression.ExpressionKind.NEW);
		boa.types.Ast.Type.Builder tb = boa.types.Ast.Type.newBuilder();
		tb.setName(getIndex(typeName(node.getType())));
		tb.setKind(boa.types.Ast.TypeKind.CLASS);
		b.setNewType(tb.build());
		for (Object t : node.typeArguments()) {
			boa.types.Ast.Type.Builder gtb = boa.types.Ast.Type.newBuilder();
			gtb.setName(getIndex(typeName((org.eclipse.wst.jsdt.core.dom.Type)t)));
			gtb.setKind(boa.types.Ast.TypeKind.GENERIC);
			b.addGenericParameters(gtb.build());
		}
		if (node.getExpression() != null) {
			node.getExpression().accept(this);
			b.addExpressions(expressions.pop());
		}
		for (Object a : node.arguments()) {
			((org.eclipse.wst.jsdt.core.dom.Expression)a).accept(this);
			b.addExpressions(expressions.pop());
		}
		if (node.getAnonymousClassDeclaration() != null) {
			declarations.push(new ArrayList<boa.types.Ast.Declaration>());
			node.getAnonymousClassDeclaration().accept(this);
			for (boa.types.Ast.Declaration d : declarations.pop())
				b.setAnonDeclaration(d);
		}
		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(ConditionalExpression node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
//		b.setPosition(pos.build());
		b.setKind(boa.types.Ast.Expression.ExpressionKind.CONDITIONAL);
		node.getExpression().accept(this);
		b.addExpressions(expressions.pop());
		node.getThenExpression().accept(this);
		b.addExpressions(expressions.pop());
		node.getElseExpression().accept(this);
		b.addExpressions(expressions.pop());
		expressions.push(b.build());
		return false;
	}
	
	@Override
	public boolean visit(FunctionExpression node) {
		boa.types.Ast.Expression.Builder bui = boa.types.Ast.Expression.newBuilder();
//		b.setPosition(pos.build());
		Declaration.Builder db = Declaration.newBuilder();
		db.setName("Default");
		db.setKind(TypeKind.OTHER);
		bui.setKind(boa.types.Ast.Expression.ExpressionKind.OTHER);
		methods.push(new ArrayList<boa.types.Ast.Method>());
		node.getMethod().accept(this);
		for (boa.types.Ast.Method d : methods.pop())
			db.addMethods(d);
		//TODO: Expressions are missing 
//		node.getExpression().accept(this);
//		b.addExpressions(expressions.pop());
		b.addDeclarations(db);
		expressions.push(bui.build());
		return false;
	}

	@Override
	public boolean visit(FieldAccess node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
//		b.setPosition(pos.build());
		b.setKind(boa.types.Ast.Expression.ExpressionKind.VARACCESS);
		node.getExpression().accept(this);
		b.addExpressions(expressions.pop());
		b.setVariable(node.getName().getFullyQualifiedName());
		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(SimpleName node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
//		b.setPosition(pos.build());
		b.setKind(boa.types.Ast.Expression.ExpressionKind.VARACCESS);
		b.setVariable(node.getFullyQualifiedName());
		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(QualifiedName node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
//		b.setPosition(pos.build());
		b.setKind(boa.types.Ast.Expression.ExpressionKind.VARACCESS);
		b.setVariable(node.getFullyQualifiedName());
		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(InfixExpression node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
//		b.setPosition(pos.build());
		if (node.getOperator() == InfixExpression.Operator.AND)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.BIT_AND);
		else if (node.getOperator() == InfixExpression.Operator.CONDITIONAL_AND)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.LOGICAL_AND);
		else if (node.getOperator() == InfixExpression.Operator.CONDITIONAL_OR)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.LOGICAL_OR);
		else if (node.getOperator() == InfixExpression.Operator.DIVIDE)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.OP_DIV);
		else if (node.getOperator() == InfixExpression.Operator.EQUALS)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.EQ);
		else if (node.getOperator() == InfixExpression.Operator.GREATER)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.GT);
		else if (node.getOperator() == InfixExpression.Operator.GREATER_EQUALS)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.GTEQ);
		else if (node.getOperator() == InfixExpression.Operator.LEFT_SHIFT)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.BIT_LSHIFT);
		else if (node.getOperator() == InfixExpression.Operator.LESS)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.LT);
		else if (node.getOperator() == InfixExpression.Operator.LESS_EQUALS)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.LTEQ);
		else if (node.getOperator() == InfixExpression.Operator.MINUS)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.OP_SUB);
		else if (node.getOperator() == InfixExpression.Operator.NOT_EQUALS)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.NEQ);
		else if (node.getOperator() == InfixExpression.Operator.OR)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.BIT_OR);
		else if (node.getOperator() == InfixExpression.Operator.PLUS)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.OP_ADD);
		else if (node.getOperator() == InfixExpression.Operator.REMAINDER)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.OP_MOD);
		else if (node.getOperator() == InfixExpression.Operator.RIGHT_SHIFT_SIGNED)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.BIT_RSHIFT);
		else if (node.getOperator() == InfixExpression.Operator.RIGHT_SHIFT_UNSIGNED)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.BIT_UNSIGNEDRSHIFT);
		else if (node.getOperator() == InfixExpression.Operator.TIMES)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.OP_MULT);
		else if (node.getOperator() == InfixExpression.Operator.XOR)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.BIT_XOR);
		else if (node.getOperator() == InfixExpression.Operator.EQUAL_EQUAL_EQUAL)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.OTHER);
		else if (node.getOperator() == InfixExpression.Operator.LESS_EQUALS)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.OTHER);
		else if (node.getOperator() == InfixExpression.Operator.LESS)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.OTHER);
		else if (node.getOperator() == InfixExpression.Operator.NOT_EQUAL_EQUAL)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.OTHER);
		else if (node.getOperator() == InfixExpression.Operator.TIMES)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.OTHER);
		else if (node.getOperator() == InfixExpression.Operator.IN)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.OTHER);
		else
			b.setKind(boa.types.Ast.Expression.ExpressionKind.OTHER);
		node.getLeftOperand().accept(this);
		b.addExpressions(expressions.pop());
		node.getRightOperand().accept(this);
		b.addExpressions(expressions.pop());
		for (Object e : node.extendedOperands()) {
			((org.eclipse.wst.jsdt.core.dom.Expression)e).accept(this);
			b.addExpressions(expressions.pop());
		}
		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(InstanceofExpression node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
//		b.setPosition(pos.build());
		b.setKind(boa.types.Ast.Expression.ExpressionKind.TYPECOMPARE);
		node.getLeftOperand().accept(this);
		b.addExpressions(expressions.pop());
		boa.types.Ast.Type.Builder tb = boa.types.Ast.Type.newBuilder();
		tb.setName(getIndex(typeName(node.getRightOperand())));
		tb.setKind(boa.types.Ast.TypeKind.OTHER);
		b.setNewType(tb.build());
		expressions.push(b.build());
		return false;
	}


	@Override
	public boolean visit(NullLiteral node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
//		b.setPosition(pos.build());
		b.setKind(boa.types.Ast.Expression.ExpressionKind.LITERAL);
		b.setLiteral("null");
		expressions.push(b.build());
		return false;
	}
	
	@Override
	public boolean visit(ObjectLiteral node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
//		b.setPosition(pos.build());
		b.setKind(boa.types.Ast.Expression.ExpressionKind.LITERAL);
		b.setLiteral("object");
		expressions.push(b.build());
		return false;
	}
	
	@Override
	public boolean visit(ObjectLiteralField node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
//		b.setPosition(pos.build());
		b.setKind(boa.types.Ast.Expression.ExpressionKind.OTHER);
		node.getFieldName().accept(this);
		b.addExpressions(expressions.pop());
		node.getInitializer().accept(this);
		b.addExpressions(expressions.pop());
		expressions.push(b.build());
		return false;
	}
	
	@Override
	public boolean visit(NumberLiteral node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
//		b.setPosition(pos.build());
		b.setKind(boa.types.Ast.Expression.ExpressionKind.LITERAL);
		b.setLiteral(node.getToken());
		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(ParenthesizedExpression node) {
		// TODO maybe? or ignore...
		return true;
	}

	@Override
	public boolean visit(PostfixExpression node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
//		b.setPosition(pos.build());
		if (node.getOperator() == PostfixExpression.Operator.DECREMENT)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.OP_DEC);
		else if (node.getOperator() == PostfixExpression.Operator.INCREMENT)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.OP_INC);
		node.getOperand().accept(this);
		b.addExpressions(expressions.pop());
		b.setIsPostfix(true);
		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(RegularExpressionLiteral node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		b.setKind(boa.types.Ast.Expression.ExpressionKind.OTHER);
		b.setLiteral(node.getRegularExpression());
		expressions.push(b.build());
		return false;
	}
	@Override
	public boolean visit(PrefixExpression node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
//		b.setPosition(pos.build());
		if (node.getOperator() == PrefixExpression.Operator.DECREMENT)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.OP_DEC);
		else if (node.getOperator() == PrefixExpression.Operator.INCREMENT)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.OP_INC);
		else if (node.getOperator() == PrefixExpression.Operator.PLUS)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.OP_ADD);
		else if (node.getOperator() == PrefixExpression.Operator.MINUS)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.OP_SUB);
		else if (node.getOperator() == PrefixExpression.Operator.COMPLEMENT)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.BIT_NOT);
		else if (node.getOperator() == PrefixExpression.Operator.NOT)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.LOGICAL_NOT);
		node.getOperand().accept(this);
		b.addExpressions(expressions.pop());
		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(org.eclipse.wst.jsdt.core.dom.StringLiteral node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
//		b.setPosition(pos.build());
		b.setKind(boa.types.Ast.Expression.ExpressionKind.LITERAL);
		b.setLiteral(node.getEscapedValue());
		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(SuperFieldAccess node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
//		b.setPosition(pos.build());
		b.setKind(boa.types.Ast.Expression.ExpressionKind.VARACCESS);
		String name = "super." + node.getName().getFullyQualifiedName();
		if (node.getQualifier() != null)
			name = node.getQualifier().getFullyQualifiedName() + "." + name;
		b.setVariable(name);
		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(SuperMethodInvocation node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
//		b.setPosition(pos.build());
		b.setKind(boa.types.Ast.Expression.ExpressionKind.METHODCALL);
		String name = "super." + node.getName().getFullyQualifiedName();
		if (node.getQualifier() != null)
			name = node.getQualifier().getFullyQualifiedName() + "." + name;
		b.setMethod(name);
		for (Object a : node.arguments()) {
			((org.eclipse.wst.jsdt.core.dom.Expression)a).accept(this);
			b.addMethodArgs(expressions.pop());
		}
		for (Object t : node.typeArguments()) {
			boa.types.Ast.Type.Builder tb = boa.types.Ast.Type.newBuilder();
			tb.setName(getIndex(typeName((org.eclipse.wst.jsdt.core.dom.Type)t)));
			tb.setKind(boa.types.Ast.TypeKind.GENERIC);
			b.addGenericParameters(tb.build());
		}
		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(ThisExpression node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
//		b.setPosition(pos.build());
		String name = "";
		if (node.getQualifier() != null)
			name += node.getQualifier().getFullyQualifiedName() + ".";
		b.setKind(boa.types.Ast.Expression.ExpressionKind.LITERAL);
		b.setLiteral(name + "this");
		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(TypeLiteral node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
//		b.setPosition(pos.build());
		b.setKind(boa.types.Ast.Expression.ExpressionKind.LITERAL);
		b.setLiteral(typeName(node.getType()) + ".class");
		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(VariableDeclarationExpression node) {
		boa.types.Ast.Expression.Builder eb = boa.types.Ast.Expression.newBuilder();
//		eb.setPosition(pos.build());
		eb.setKind(boa.types.Ast.Expression.ExpressionKind.VARDECL);
		for (Object o : node.fragments()) {
			VariableDeclarationFragment f = (VariableDeclarationFragment)o;
			Variable.Builder b = Variable.newBuilder();
//			b.setPosition(pos.build());//FIXME
			b.setName(f.getName().getFullyQualifiedName());
			for (Object m : node.modifiers()) {
					((org.eclipse.wst.jsdt.core.dom.Modifier)m).accept(this);
				b.addModifiers(modifiers.pop());
			}
			boa.types.Ast.Type.Builder tb = boa.types.Ast.Type.newBuilder();
			String name = typeName(node.getType());
			for (int i = 0; i < f.getExtraDimensions(); i++)
				name += "[]";
			tb.setName(getIndex(name));
			tb.setKind(boa.types.Ast.TypeKind.OTHER);
			b.setVariableType(tb.build());
			if (f.getInitializer() != null) {
				f.getInitializer().accept(this);
				b.setInitializer(expressions.pop());
			}
			eb.addVariableDecls(b.build());
		}
		expressions.push(eb.build());
		return false;
	}

	//////////////////////////////////////////////////////////////
	// Utility methods

	private String typeName(org.eclipse.wst.jsdt.core.dom.Type t) {
		if(t == null)
				return "other";
		if (t.isArrayType())
			return typeName(((ArrayType)t).getComponentType()) + "[]";

		if (t.isPrimitiveType())
			return ((PrimitiveType)t).getPrimitiveTypeCode().toString();
		if (t.isQualifiedType())
			return typeName(((QualifiedType)t).getQualifier()) + "." + ((QualifiedType)t).getName().getFullyQualifiedName();
		if(t.isInferred())
			return (((InferredType)t).getType()) + "." + ((InferredType)t).getType();
		
		return ((SimpleType)t).getName().getFullyQualifiedName();
	}

	//////////////////////////////////////////////////////////////
	// Currently un-used node types

	@Override
	public boolean visit(ArrayType node) {
		throw new RuntimeException("visited unused node ArrayType");
	}



	@Override
	public boolean visit(PrimitiveType node) {
		throw new RuntimeException("visited unused node PrimitiveType");
	}

	@Override
	public boolean visit(QualifiedType node) {
		throw new RuntimeException("visited unused node QualifiedType");
	}

	@Override
	public boolean visit(SimpleType node) {
		throw new RuntimeException("visited unused node SimpleType");
	}



	@Override
	public boolean visit(ImportDeclaration node) {
		throw new RuntimeException("visited unused node ImportDeclaration");
	}

	@Override
	public boolean visit(PackageDeclaration node) {
		throw new RuntimeException("visited unused node PackageDeclaration");
	}

	@Override
	public boolean visit(SingleVariableDeclaration node) {
		// FIXME 
		//throw new RuntimeException("visited unused node SingleVariableDeclaration");
		return false;
	}

	@Override
	public boolean visit(MemberRef node) {
		throw new RuntimeException("visited unused node " + node.getClass().getSimpleName());
	}


	@Override
	public boolean visit(VariableDeclarationFragment node) {
		throw new RuntimeException("visited unused node " + node.getClass().getSimpleName());
	}
	
	@Override
	public boolean visit(FunctionRef node) {
		throw new RuntimeException("visited unused node " + node.getClass().getSimpleName());
	}
	
	@Override
	public boolean visit(FunctionRefParameter node) {
		throw new RuntimeException("visited unused node " + node.getClass().getSimpleName());
	}
	
	@Override
	public boolean visit(InferredType node) {
		throw new RuntimeException("visited unused node " + node.getClass().getSimpleName());
	}
	
	@Override
	public boolean visit(org.eclipse.wst.jsdt.core.dom.TagElement node) {
		throw new RuntimeException("visited unused node " + node.getClass().getSimpleName());
	}
	
	@Override
	public boolean visit(UndefinedLiteral node) {
		throw new RuntimeException("visited unused node " + node.getClass().getSimpleName());
	}

	private int getIndex(String name) {
		Integer index = this.nameIndices.get(name);
		if (index == null) {
			index = this.nameIndices.size();
			this.nameIndices.put(name, index);
		}
		return index;
	}
}