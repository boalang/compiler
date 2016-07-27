/*
 * Copyright 2016, Hridesh Rajan, Robert Dyer, Hoan Nguyen, Farheen Sultana
 *                 Iowa State University of Science and Technology
 *                 and Bowling Green State University
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

import java.util.*;

import org.eclipse.jdt.core.dom.*;

import boa.types.Ast.*;

/**
 * @author rdyer
 */
public class Java7Visitor extends ASTVisitor {
	protected HashMap<String, Integer> nameIndices;
	
	protected CompilationUnit root = null;
	protected PositionInfo.Builder pos = null;
	protected String src = null;

	protected Namespace.Builder b = Namespace.newBuilder();
	protected List<boa.types.Ast.Comment> comments = new ArrayList<boa.types.Ast.Comment>();
	protected List<String> imports = new ArrayList<String>();
	protected Stack<List<boa.types.Ast.Declaration>> declarations = new Stack<List<boa.types.Ast.Declaration>>();
	protected Stack<boa.types.Ast.Modifier> modifiers = new Stack<boa.types.Ast.Modifier>();
	protected Stack<boa.types.Ast.Expression> expressions = new Stack<boa.types.Ast.Expression>();
	protected Stack<List<boa.types.Ast.Variable>> fields = new Stack<List<boa.types.Ast.Variable>>();
	protected Stack<List<boa.types.Ast.Method>> methods = new Stack<List<boa.types.Ast.Method>>();
	protected Stack<List<boa.types.Ast.Statement>> statements = new Stack<List<boa.types.Ast.Statement>>();

	public Java7Visitor(String src, HashMap<String, Integer> nameIndices) {
		super();
		this.src = src;
		this.nameIndices = nameIndices;
	}

	public Namespace getNamespaces(CompilationUnit node) {
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

	protected void buildPosition(final ASTNode node) {
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
	public boolean visit(CompilationUnit node) {
//		b.setPosition(pos.build());
		PackageDeclaration pkg = node.getPackage();
		if (pkg == null) {
			b.setName("");
		} else {
			b.setName(pkg.getName().getFullyQualifiedName());
			for (Object a : pkg.annotations()) {
				((Annotation)a).accept(this);
				b.addModifiers(modifiers.pop());
			}
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
			((org.eclipse.jdt.core.dom.Comment)c).accept(this);
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
	public boolean visit(Javadoc node) {
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
//		b.setPosition(pos.build());
		b.setName(node.getName().getFullyQualifiedName());
		if (node.isInterface())
			b.setKind(boa.types.Ast.TypeKind.INTERFACE);
		else
			b.setKind(boa.types.Ast.TypeKind.CLASS);
		for (Object m : node.modifiers()) {
			if (((IExtendedModifier)m).isAnnotation())
				((Annotation)m).accept(this);
			else
				((org.eclipse.jdt.core.dom.Modifier)m).accept(this);
			b.addModifiers(modifiers.pop());
		}
		for (Object t : node.typeParameters()) {
			boa.types.Ast.Type.Builder tb = boa.types.Ast.Type.newBuilder();
			String name = ((TypeParameter)t).getName().getFullyQualifiedName();
			String bounds = "";
			for (Object o : ((TypeParameter)t).typeBounds()) {
				if (bounds.length() > 0)
					bounds += " & ";
				bounds += typeName((org.eclipse.jdt.core.dom.Type)o);
			}
			if (bounds.length() > 0)
				name = name + " extends " + bounds;
			tb.setName(getIndex(name));
			tb.setKind(boa.types.Ast.TypeKind.GENERIC);
			b.addGenericParameters(tb.build());
		}
		if (node.getSuperclassType() != null) {
			boa.types.Ast.Type.Builder tb = boa.types.Ast.Type.newBuilder();
			tb.setName(getIndex(typeName(node.getSuperclassType())));
			tb.setKind(boa.types.Ast.TypeKind.CLASS);
			b.addParents(tb.build());
		}
		for (Object t : node.superInterfaceTypes()) {
			boa.types.Ast.Type.Builder tb = boa.types.Ast.Type.newBuilder();
			tb.setName(getIndex(typeName((org.eclipse.jdt.core.dom.Type)t)));
			tb.setKind(boa.types.Ast.TypeKind.INTERFACE);
			b.addParents(tb.build());
		}
		for (Object d : node.bodyDeclarations()) {
			if (d instanceof FieldDeclaration) {
				fields.push(new ArrayList<boa.types.Ast.Variable>());
				((FieldDeclaration)d).accept(this);
				for (boa.types.Ast.Variable v : fields.pop())
					b.addFields(v);
			} else if (d instanceof MethodDeclaration) {
				methods.push(new ArrayList<boa.types.Ast.Method>());
				((MethodDeclaration)d).accept(this);
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
		// TODO initializers
		// TODO enum constants
		// TODO annotation type members
		declarations.peek().add(b.build());
		return false;
	}

	@Override
	public boolean visit(AnonymousClassDeclaration node) {
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
			} else if (d instanceof MethodDeclaration) {
				methods.push(new ArrayList<boa.types.Ast.Method>());
				((MethodDeclaration)d).accept(this);
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
	public boolean visit(EnumDeclaration node) {
		boa.types.Ast.Declaration.Builder b = boa.types.Ast.Declaration.newBuilder();
//		b.setPosition(pos.build());
		b.setName(node.getName().getFullyQualifiedName());
		b.setKind(boa.types.Ast.TypeKind.ENUM);
		// TODO
		declarations.peek().add(b.build());
		return false;
	}

	@Override
	public boolean visit(AnnotationTypeDeclaration node) {
		boa.types.Ast.Declaration.Builder b = boa.types.Ast.Declaration.newBuilder();
//		b.setPosition(pos.build());
		b.setName(node.getName().getFullyQualifiedName());
		b.setKind(boa.types.Ast.TypeKind.ANNOTATION);
		// TODO
		declarations.peek().add(b.build());
		return false;
	}

	//////////////////////////////////////////////////////////////
	// Field/Method Declarations

	@Override
	public boolean visit(MethodDeclaration node) {
		List<boa.types.Ast.Method> list = methods.peek();
		Method.Builder b = Method.newBuilder();
//		b.setPosition(pos.build());
		if (node.isConstructor())
			b.setName("<init>");
		else
			b.setName(node.getName().getFullyQualifiedName());
		for (Object m : node.modifiers()) {
			if (((IExtendedModifier)m).isAnnotation())
				((Annotation)m).accept(this);
			else
				((org.eclipse.jdt.core.dom.Modifier)m).accept(this);
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
		for (Object t : node.typeParameters()) {
			boa.types.Ast.Type.Builder tp = boa.types.Ast.Type.newBuilder();
			String name = ((TypeParameter)t).getName().getFullyQualifiedName();
			String bounds = "";
			for (Object o : ((TypeParameter)t).typeBounds()) {
				if (bounds.length() > 0)
					bounds += " & ";
				bounds += typeName((org.eclipse.jdt.core.dom.Type)o);
			}
			if (bounds.length() > 0)
				name = name + " extends " + bounds;
			tp.setName(getIndex(name));
			tp.setKind(boa.types.Ast.TypeKind.GENERIC);
			b.addGenericParameters(tp.build());
		}
		for (Object o : node.parameters()) {
			SingleVariableDeclaration ex = (SingleVariableDeclaration)o;
			Variable.Builder vb = Variable.newBuilder();
//			vb.setPosition(pos.build()); // FIXME
			vb.setName(ex.getName().getFullyQualifiedName());
			for (Object m : ex.modifiers()) {
				if (((IExtendedModifier)m).isAnnotation())
					((Annotation)m).accept(this);
				else
					((org.eclipse.jdt.core.dom.Modifier)m).accept(this);
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
	public boolean visit(AnnotationTypeMemberDeclaration node) {
		List<boa.types.Ast.Method> list = methods.peek();
		Method.Builder b = Method.newBuilder();
//		b.setPosition(pos.build());
		b.setName(node.getName().getFullyQualifiedName());
		for (Object m : node.modifiers()) {
			if (((IExtendedModifier)m).isAnnotation())
				((Annotation)m).accept(this);
			else
				((org.eclipse.jdt.core.dom.Modifier)m).accept(this);
			b.addModifiers(modifiers.pop());
		}
		boa.types.Ast.Type.Builder tb = boa.types.Ast.Type.newBuilder();
		tb.setName(getIndex(typeName(node.getType())));
		tb.setKind(boa.types.Ast.TypeKind.OTHER);
		b.setReturnType(tb.build());
		if (node.getDefault() != null) {
			boa.types.Ast.Statement.Builder sb = boa.types.Ast.Statement.newBuilder();
	//		sb.setPosition(pos.build());
			sb.setKind(boa.types.Ast.Statement.StatementKind.EXPRESSION);
			node.getDefault().accept(this);
			sb.setExpression(expressions.pop());
			b.addStatements(sb.build());
		}
		list.add(b.build());
		return false;
	}

	@Override
	public boolean visit(FieldDeclaration node) {
		List<boa.types.Ast.Variable> list = fields.peek();
		for (Object o : node.fragments()) {
			VariableDeclarationFragment f = (VariableDeclarationFragment)o;
			Variable.Builder b = Variable.newBuilder();
//			b.setPosition(pos.build()); // FIXME
			b.setName(f.getName().getFullyQualifiedName());
			for (Object m : node.modifiers()) {
				if (((IExtendedModifier)m).isAnnotation())
					((Annotation)m).accept(this);
				else
					((org.eclipse.jdt.core.dom.Modifier)m).accept(this);
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

	protected boa.types.Ast.Modifier.Builder getAnnotationBuilder(Annotation node) {
		boa.types.Ast.Modifier.Builder b = boa.types.Ast.Modifier.newBuilder();
//		b.setPosition(pos.build());
		b.setKind(boa.types.Ast.Modifier.ModifierKind.ANNOTATION);
		b.setAnnotationName(node.getTypeName().getFullyQualifiedName());
		return b;
	}

	@Override
	public boolean visit(MarkerAnnotation node) {
		modifiers.push(getAnnotationBuilder(node).build());
		return false;
	}

	@Override
	public boolean visit(SingleMemberAnnotation node) {
		boa.types.Ast.Modifier.Builder b = getAnnotationBuilder(node);
		node.getValue().accept(this);
		if (expressions.empty()) {
			boa.types.Ast.Expression.Builder eb = boa.types.Ast.Expression.newBuilder();
//			eb.setPosition(pos.build()); // FIXME
			eb.setKind(boa.types.Ast.Expression.ExpressionKind.ANNOTATION);
			eb.setAnnotation(modifiers.pop());
			b.addAnnotationMembers("value");
			b.addAnnotationValues(eb.build());
		} else {
			b.addAnnotationMembers("value");
			b.addAnnotationValues(expressions.pop());
		}
		modifiers.push(b.build());
		return false;
	}

	@Override
	public boolean visit(NormalAnnotation node) {
		boa.types.Ast.Modifier.Builder b = getAnnotationBuilder(node);
		for (Object v : node.values()) {
			MemberValuePair pair = (MemberValuePair)v;
			pair.getValue().accept(this);
			if (expressions.empty()) {
				boa.types.Ast.Expression.Builder eb = boa.types.Ast.Expression.newBuilder();
	//			eb.setPosition(pos.build()); // FIXME
				eb.setKind(boa.types.Ast.Expression.ExpressionKind.ANNOTATION);
				eb.setAnnotation(modifiers.pop());
				b.addAnnotationMembers(pair.getName().getFullyQualifiedName());
				b.addAnnotationValues(eb.build());
			} else {
				b.addAnnotationMembers(pair.getName().getFullyQualifiedName());
				b.addAnnotationValues(expressions.pop());
			}
		}
		modifiers.push(b.build());
		return false;
	}

	@Override
	public boolean visit(org.eclipse.jdt.core.dom.Modifier node) {
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
	public boolean visit(AssertStatement node) {
		boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
//		b.setPosition(pos.build());
		List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.ASSERT);
		node.getExpression().accept(this);
		b.setExpression(expressions.pop());
		if (node.getMessage() != null) {
			node.getMessage().accept(this);
			b.setExpression(expressions.pop());
		}
		list.add(b.build());
		return false;
	}

	@Override
	public boolean visit(Block node) {
		boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
//		b.setPosition(pos.build());
		List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.BLOCK);
		for (Object s : node.statements()) {
			statements.push(new ArrayList<boa.types.Ast.Statement>());
			((org.eclipse.jdt.core.dom.Statement)s).accept(this);
			for (boa.types.Ast.Statement st : statements.pop())
				b.addStatements(st);
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
		SingleVariableDeclaration ex = node.getException();
		Variable.Builder vb = Variable.newBuilder();
//		vb.setPosition(pos.build());// FIXME
		vb.setName(ex.getName().getFullyQualifiedName());
		for (Object m : ex.modifiers()) {
			if (((IExtendedModifier)m).isAnnotation())
				((Annotation)m).accept(this);
			else
				((org.eclipse.jdt.core.dom.Modifier)m).accept(this);
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
			((org.eclipse.jdt.core.dom.Statement)s).accept(this);
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
			((org.eclipse.jdt.core.dom.Expression)a).accept(this);
			eb.addMethodArgs(expressions.pop());
		}
		for (Object t : node.typeArguments()) {
			boa.types.Ast.Type.Builder tb = boa.types.Ast.Type.newBuilder();
			tb.setName(getIndex(typeName((org.eclipse.jdt.core.dom.Type)t)));
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
		SingleVariableDeclaration ex = node.getParameter();
		Variable.Builder vb = Variable.newBuilder();
//		vb.setPosition(pos.build());//FIXME
		vb.setName(ex.getName().getFullyQualifiedName());
		for (Object m : ex.modifiers()) {
			if (((IExtendedModifier)m).isAnnotation())
				((Annotation)m).accept(this);
			else
				((org.eclipse.jdt.core.dom.Modifier)m).accept(this);
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
	public boolean visit(ForStatement node) {
		boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
//		b.setPosition(pos.build());
		List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.FOR);
		for (Object e : node.initializers()) {
			((org.eclipse.jdt.core.dom.Expression)e).accept(this);
			b.addInitializations(expressions.pop());
		}
		for (Object e : node.updaters()) {
			((org.eclipse.jdt.core.dom.Expression)e).accept(this);
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
			if (((IExtendedModifier)m).isAnnotation())
				((Annotation)m).accept(this);
			else
				((org.eclipse.jdt.core.dom.Modifier)m).accept(this);
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
			((org.eclipse.jdt.core.dom.Expression)a).accept(this);
			eb.addMethodArgs(expressions.pop());
		}
		for (Object t : node.typeArguments()) {
			boa.types.Ast.Type.Builder tb = boa.types.Ast.Type.newBuilder();
			tb.setName(getIndex(typeName((org.eclipse.jdt.core.dom.Type)t)));
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
			((org.eclipse.jdt.core.dom.Statement)s).accept(this);
		for (boa.types.Ast.Statement s : statements.pop())
			b.addStatements(s);
		list.add(b.build());
		return false;
	}

	@Override
	public boolean visit(SynchronizedStatement node) {
		boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
//		b.setPosition(pos.build());
		List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.SYNCHRONIZED);
		node.getExpression().accept(this);
		b.setExpression(expressions.pop());
		statements.push(new ArrayList<boa.types.Ast.Statement>());
		for (Object s : node.getBody().statements())
			((org.eclipse.jdt.core.dom.Statement)s).accept(this);
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
		if (node.resources() != null)
			for (Object v : node.resources()) {
				((VariableDeclarationExpression)v).accept(this);
				b.addInitializations(expressions.pop());
			}
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
				if (((IExtendedModifier)m).isAnnotation())
					((Annotation)m).accept(this);
				else
					((org.eclipse.jdt.core.dom.Modifier)m).accept(this);
				vb.addModifiers(modifiers.pop());
			}
			boa.types.Ast.Type.Builder tb = boa.types.Ast.Type.newBuilder();
			String name = typeName(node.getType());
			for (int i = 0; i < f.getExtraDimensions(); i++)
				name += "[]";
			tb.setName(getIndex(name));
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
			((org.eclipse.jdt.core.dom.Expression)e).accept(this);
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
			((org.eclipse.jdt.core.dom.Expression)e).accept(this);
			if (expressions.empty()) {
				boa.types.Ast.Expression.Builder eb = boa.types.Ast.Expression.newBuilder();
	//			eb.setPosition(pos.build()); // FIXME
				eb.setKind(boa.types.Ast.Expression.ExpressionKind.ANNOTATION);
				eb.setAnnotation(modifiers.pop());
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
	public boolean visit(CastExpression node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
//		b.setPosition(pos.build());
		b.setKind(boa.types.Ast.Expression.ExpressionKind.CAST);
		boa.types.Ast.Type.Builder tb = boa.types.Ast.Type.newBuilder();
		tb.setName(getIndex(typeName(node.getType())));
		tb.setKind(boa.types.Ast.TypeKind.OTHER);
		b.setNewType(tb.build());
		node.getExpression().accept(this);
		b.addExpressions(expressions.pop());
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
			gtb.setName(getIndex(typeName((org.eclipse.jdt.core.dom.Type)t)));
			gtb.setKind(boa.types.Ast.TypeKind.GENERIC);
			b.addGenericParameters(gtb.build());
		}
		if (node.getExpression() != null) {
			node.getExpression().accept(this);
			b.addExpressions(expressions.pop());
		}
		for (Object a : node.arguments()) {
			((org.eclipse.jdt.core.dom.Expression)a).accept(this);
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
		node.getLeftOperand().accept(this);
		b.addExpressions(expressions.pop());
		node.getRightOperand().accept(this);
		b.addExpressions(expressions.pop());
		for (Object e : node.extendedOperands()) {
			((org.eclipse.jdt.core.dom.Expression)e).accept(this);
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
	public boolean visit(MethodInvocation node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
//		b.setPosition(pos.build());
		b.setKind(boa.types.Ast.Expression.ExpressionKind.METHODCALL);
		b.setMethod(node.getName().getFullyQualifiedName());
		if (node.getExpression() != null) {
			node.getExpression().accept(this);
			b.addExpressions(expressions.pop());
		}
		for (Object a : node.arguments()) {
			((org.eclipse.jdt.core.dom.Expression)a).accept(this);
			b.addMethodArgs(expressions.pop());
		}
		for (Object t : node.typeArguments()) {
			boa.types.Ast.Type.Builder tb = boa.types.Ast.Type.newBuilder();
			tb.setName(getIndex(typeName((org.eclipse.jdt.core.dom.Type)t)));
			tb.setKind(boa.types.Ast.TypeKind.GENERIC);
			b.addGenericParameters(tb.build());
		}
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
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
//		b.setPosition(pos.build());
		b.setKind(boa.types.Ast.Expression.ExpressionKind.PAREN);
		node.getExpression().accept(this);
		b.addExpressions(expressions.pop());
		expressions.push(b.build());
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
	public boolean visit(StringLiteral node) {
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
			((org.eclipse.jdt.core.dom.Expression)a).accept(this);
			b.addMethodArgs(expressions.pop());
		}
		for (Object t : node.typeArguments()) {
			boa.types.Ast.Type.Builder tb = boa.types.Ast.Type.newBuilder();
			tb.setName(getIndex(typeName((org.eclipse.jdt.core.dom.Type)t)));
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
				if (((IExtendedModifier)m).isAnnotation())
					((Annotation)m).accept(this);
				else
					((org.eclipse.jdt.core.dom.Modifier)m).accept(this);
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

	protected String typeName(final org.eclipse.jdt.core.dom.Type t) {
		if (t.isArrayType())
			return typeName((ArrayType)t);
		if (t.isParameterizedType())
			return typeName((ParameterizedType)t);
		if (t.isPrimitiveType())
			return typeName((PrimitiveType)t);
		if (t.isQualifiedType())
			return typeName((QualifiedType)t);
		if (t.isIntersectionType())
			return typeName((IntersectionType)t);
		if (t.isUnionType())
			return typeName((UnionType)t);
		if (t.isWildcardType())
			return typeName((WildcardType)t);
		return typeName((SimpleType)t);
	}

	protected String typeName(final ArrayType t) {
		return typeName(t.getComponentType()) + "[]";
	}

	protected String typeName(final ParameterizedType t) {
		String name = "";
		for (final Object o : t.typeArguments()) {
			if (name.length() > 0) name += ", ";
			name += typeName((org.eclipse.jdt.core.dom.Type)o);
		}
		return typeName(t.getType()) + "<" + name + ">";
	}

	protected String typeName(final PrimitiveType t) {
		return t.getPrimitiveTypeCode().toString();
	}

	protected String typeName(final QualifiedType t) {
		return typeName(t.getQualifier()) + "." + t.getName().getFullyQualifiedName();
	}

	protected String typeName(final IntersectionType t) {
		String name = "";
		for (final Object o : t.types()) {
			if (name.length() > 0) name += " & ";
			name += typeName((org.eclipse.jdt.core.dom.Type)o);
		}
		return name;
	}

	protected String typeName(final UnionType t) {
		String name = "";
		for (final Object o : t.types()) {
			if (name.length() > 0) name += " | ";
			name += typeName((org.eclipse.jdt.core.dom.Type)o);
		}
		return name;
	}

	protected String typeName(final WildcardType t) {
		String name = "?";
		if (t.getBound() != null) {
			name += " " + (t.isUpperBound() ? "extends" : "super");
			name += " " + typeName(t.getBound());
		}
		return name;
	}

	protected String typeName(final SimpleType t) {
		return t.getName().getFullyQualifiedName();
	}

	protected int getIndex(final String name) {
		Integer index = this.nameIndices.get(name);
		if (index == null) {
			index = this.nameIndices.size();
			this.nameIndices.put(name, index);
		}
		return index;
	}


	//////////////////////////////////////////////////////////////
	// Unused node types
	
	@Override
	public boolean visit(ArrayType node) {
		throw new RuntimeException("visited unused node " + node.getClass().getSimpleName());
	}

	@Override
	public boolean visit(ParameterizedType node) {
		throw new RuntimeException("visited unused node " + node.getClass().getSimpleName());
	}

	@Override
	public boolean visit(PrimitiveType node) {
		throw new RuntimeException("visited unused node " + node.getClass().getSimpleName());
	}

	@Override
	public boolean visit(QualifiedType node) {
		throw new RuntimeException("visited unused node " + node.getClass().getSimpleName());
	}

	@Override
	public boolean visit(SimpleType node) {
		throw new RuntimeException("visited unused node " + node.getClass().getSimpleName());
	}

	@Override
	public boolean visit(UnionType node) {
		throw new RuntimeException("visited unused node " + node.getClass().getSimpleName());
	}

	@Override
	public boolean visit(WildcardType node) {
		throw new RuntimeException("visited unused node " + node.getClass().getSimpleName());
	}

	@Override
	public boolean visit(EnumConstantDeclaration node) {
		throw new RuntimeException("visited unused node " + node.getClass().getSimpleName());
	}

	@Override
	public boolean visit(ImportDeclaration node) {
		throw new RuntimeException("visited unused node " + node.getClass().getSimpleName());
	}

	@Override
	public boolean visit(PackageDeclaration node) {
		throw new RuntimeException("visited unused node " + node.getClass().getSimpleName());
	}

	@Override
	public boolean visit(SingleVariableDeclaration node) {
		throw new RuntimeException("visited unused node " + node.getClass().getSimpleName());
	}

	@Override
	public boolean visit(MemberRef node) {
		throw new RuntimeException("visited unused node " + node.getClass().getSimpleName());
	}

	@Override
	public boolean visit(MemberValuePair node) {
		throw new RuntimeException("visited unused node " + node.getClass().getSimpleName());
	}

	@Override
	public boolean visit(TypeParameter node) {
		throw new RuntimeException("visited unused node " + node.getClass().getSimpleName());
	}

	@Override
	public boolean visit(VariableDeclarationFragment node) {
		throw new RuntimeException("visited unused node " + node.getClass().getSimpleName());
	}
	
	@Override
	public boolean visit(MethodRef node) {
		throw new RuntimeException("visited unused node " + node.getClass().getSimpleName());
	}
	
	@Override
	public boolean visit(MethodRefParameter node) {
		throw new RuntimeException("visited unused node " + node.getClass().getSimpleName());
	}
	
	@Override
	public boolean visit(TagElement node) {
		throw new RuntimeException("visited unused node " + node.getClass().getSimpleName());
	}
	
	@Override
	public boolean visit(TextElement node) {
		throw new RuntimeException("visited unused node " + node.getClass().getSimpleName());
	}
}
