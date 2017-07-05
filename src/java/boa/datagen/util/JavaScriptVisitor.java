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

import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.mozilla.javascript.*;
import org.mozilla.javascript.ast.*;

import boa.types.Ast.Declaration;
import boa.types.Ast.Expression;
import boa.types.Ast.Method;
import boa.types.Ast.Namespace;
import boa.types.Ast.PositionInfo;
import boa.types.Ast.Statement;
import boa.types.Ast.Statement.StatementKind;
import boa.types.Ast.Type;
import boa.types.Ast.TypeKind;
import boa.types.Ast.Variable;
import boa.types.Ast.VariableOrBuilder;

/**
 * @author rdyer
 */
public class JavaScriptVisitor implements NodeVisitor {
	private HashMap<String, Integer> nameIndices;

	private AstRoot root = null;
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

	public Namespace getNamespaces(AstRoot node) {
		root = node;
		node.visit(this);
		return b.build();
	}

	public List<boa.types.Ast.Comment> getComments() {
		return comments;
	}

	public List<String> getImports() {
		return imports;
	}

	private void buildPosition(final AstNode node) {
		pos = PositionInfo.newBuilder();
		int start = node.getPosition();// getStartPosition();
		int length = node.getLength();// getLength();
		pos.setStartPos(start);
		pos.setLength(length);
		pos.setStartLine(root.getBaseLineno());
		// FIXME pos.setStartCol(root. getColumnNumber(start));
		pos.setEndLine(root.getEndLineno());
		// FIXME pos.setEndCol(root.getColumnNumber(start + length));
	}

	public boolean visit(AstRoot node) {
		String pkg = node.getSourceName(); // getPackage();
		b.setName(pkg);
		for (Object c : node.getComments())
			((Comment) c).visit(this);
			for (Object s : node.getStatements()) {
				if (s instanceof FunctionNode) {
					methods.push(new ArrayList<boa.types.Ast.Method>());
					((FunctionNode) s).visit(this);
					for (boa.types.Ast.Method m : methods.pop())
						b.addMethods(m);
				} else {
					statements.push(new ArrayList<boa.types.Ast.Statement>());
					((AstNode) s).visit(this);
					for (boa.types.Ast.Statement d : statements.pop())
						b.addStatements(d);
				}
			}
		return false;
	}

	public boolean visit(Comment node) {
		boa.types.Ast.Comment.Builder b = boa.types.Ast.Comment.newBuilder();
		buildPosition(node);
		b.setPosition(pos.build());
		if (node.getCommentType() == Token.CommentType.BLOCK_COMMENT) {
			b.setKind(boa.types.Ast.Comment.CommentKind.BLOCK);
		} else if (node.getCommentType() == Token.CommentType.LINE) {
			b.setKind(boa.types.Ast.Comment.CommentKind.LINE);
		} else if (node.getCommentType() == Token.CommentType.JSDOC) {
			b.setKind(boa.types.Ast.Comment.CommentKind.DOC);
		}
		b.setValue(src.substring(node.getLineno(), node.getLineno() + node.getLength()));
		comments.add(b.build());
		return false;
	}

	//////////////////////////////////////////////////////////////
	// Field/Method Declarations

	public boolean visit(VariableDeclaration node) {
		boa.types.Ast.Expression.Builder eb = boa.types.Ast.Expression.newBuilder();
		eb.setKind(boa.types.Ast.Expression.ExpressionKind.VARDECL);
		for (VariableInitializer f : node.getVariables()) {
			f.visit(this);
			eb.addExpressions(expressions.pop());
		} 
		expressions.push(eb.build());
		return false;
	}

	public boolean visit(LetNode node) {
		Statement.Builder sb = boa.types.Ast.Statement.newBuilder();
		sb.setKind(boa.types.Ast.Statement.StatementKind.EXPRESSION);
		List<boa.types.Ast.Statement> list = statements.peek();
		boa.types.Ast.Expression.Builder eb = boa.types.Ast.Expression.newBuilder();
		eb.setKind(boa.types.Ast.Expression.ExpressionKind.VARDECL);
		for (Node f : node.getVariables()) {
			((AstNode) f).visit(this);
			eb.addExpressions(expressions.pop());
		} 
		sb.setExpression(eb.build());
		if (node.getBody() != null){
			statements.push(new ArrayList<boa.types.Ast.Statement>());
			node.getBody().visit(this);
			for (boa.types.Ast.Statement d : statements.pop())
				sb.addStatements(d);
		}
		list.add(sb.build());
		return false;
	}

	public boolean visit(FunctionNode node) {
		List<boa.types.Ast.Method> list = methods.peek();
		Method.Builder b = Method.newBuilder();
		b.setName(node.getName());
		//node.getFunctionName().visit(this);
		for (AstNode p : node.getParams()) {
			Variable.Builder vb = Variable.newBuilder();
			vb.setName(Token.typeToName(p.getType()));
			boa.types.Ast.Type.Builder tp = boa.types.Ast.Type.newBuilder();
			String name = p.toSource();
			tp.setName(name);
			tp.setKind(boa.types.Ast.TypeKind.OTHER);
			vb.setVariableType(tp.build());
			if (((VariableDeclaration) p).getVariables() != null) {
				for (VariableInitializer v : ((VariableDeclaration) p).getVariables()) {
					v.visit(this);
					vb.setInitializer(expressions.pop());
				}
			}
			b.addArguments(vb.build());
		}

		
		statements.push(new ArrayList<boa.types.Ast.Statement>());
		node.getBody().visit(this);
		for (boa.types.Ast.Statement s : statements.pop())
			b.addStatements(s);
		if (!node.isExpressionClosure()){
			if (node.getMemberExprNode() != null){
				node.getMemberExprNode().visit(this);
			//	b.setExpression(expressions.pop());
			}
		}
		list.add(b.build());
		return false;
	}

	//////////////////////////////////////////////////////////////
	// Statements

	public boolean visit(Block node) {
		boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
		List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.BLOCK);
		for (Node s : node) {
			if (s instanceof FunctionNode) {
				methods.push(new ArrayList<boa.types.Ast.Method>());
				((AstNode) s).visit(this);
			} else {
				statements.push(new ArrayList<boa.types.Ast.Statement>());
				((AstNode) s).visit(this);
				for (boa.types.Ast.Statement st : statements.pop())
					b.addStatements(st);
			}
			s = s.getNext();
		}
		list.add(b.build());
		return false;
	}

	public boolean visit(BreakStatement node) {
		boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
		List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.BREAK);
		if (node.getBreakLabel() != null) {
			node.getBreakLabel().visit(this);
			b.setExpression(expressions.pop());
		}
		list.add(b.build());
		return false;
	}

	public boolean visit(CatchClause node) {
		boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
		Expression.Builder eb = boa.types.Ast.Expression.newBuilder();
		List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.CATCH);
		node.getVarName().visit(this);
		eb.addExpressions(expressions.pop());
		if (node.getCatchCondition() != null){
			node.getCatchCondition().visit(this);
			eb.addExpressions(expressions.pop());
		}
		statements.push(new ArrayList<boa.types.Ast.Statement>());
		node.getBody().visit(this);
		for (boa.types.Ast.Statement d : statements.pop())
			b.addStatements(d);
		list.add(b.build());
		return false;
	}

	public boolean visit(ContinueStatement node) {
		boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
		List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.CONTINUE);
		if (node.getLabel() != null) {
			node.getLabel().visit(this);
			b.setExpression(expressions.pop());
		}
		list.add(b.build());
		return false;
	}

	public boolean visit(DoLoop node) {
		boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
		List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.DO);
		node.getCondition().visit(this);
		b.setExpression(expressions.pop());
		statements.push(new ArrayList<boa.types.Ast.Statement>());
		node.getBody().visit(this);
		for (boa.types.Ast.Statement s : statements.pop())
			b.addStatements(s);
		list.add(b.build());
		return false;
	}

	public boolean visit(EmptyStatement node) {
		boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
		List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.EMPTY);
		list.add(b.build());
		return false;
	}

	public boolean visit(EmptyExpression node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		b.setKind(boa.types.Ast.Expression.ExpressionKind.OTHER);//EMPTY); FIXME
		expressions.push(b.build());
		return false;
	}

	public boolean visit(ErrorNode node) {
		boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
		List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.OTHER);
		list.add(b.build());
		return false;
	}

	public boolean visit(ForInLoop node) {
		boa.types.Ast.Statement.Builder s = boa.types.Ast.Statement.newBuilder();
		List<boa.types.Ast.Statement> list = statements.peek();
		s.setKind(boa.types.Ast.Statement.StatementKind.FOR);
		statements.push(new ArrayList<boa.types.Ast.Statement>());
		node.getBody().visit(this);
		for (boa.types.Ast.Statement x : statements.pop())
			s.addStatements(x);
		node.getIterator().visit(this);
		s.addInitializations(expressions.pop());
		node.getIteratedObject().visit(this);
		s.addInitializations(expressions.pop());
		list.add(s.build());
		return false;
	}
	
	public boolean visit(ForLoop node){
		boa.types.Ast.Statement.Builder s = boa.types.Ast.Statement.newBuilder();
		List<boa.types.Ast.Statement> list = statements.peek();
		s.setKind(boa.types.Ast.Statement.StatementKind.FOR);
		node.getInitializer().visit(this);
		s.addInitializations(expressions.pop());
		node.getCondition().visit(this);
		s.setCondition(expressions.pop());
		node.getIncrement().visit(this);
		s.addUpdates(expressions.pop());
		statements.push(new ArrayList<boa.types.Ast.Statement>());
		node.getBody().visit(this);
		for (boa.types.Ast.Statement x : statements.pop())
			s.addStatements(x);
		list.add(s.build());
		return false;
	}

	public boolean visit(FunctionCall node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		b.setKind(boa.types.Ast.Expression.ExpressionKind.METHODCALL);
		if (node.shortName() != null)
			b.setMethod(node.toSource());
		if (node.getTarget() != null) {
			node.getTarget().visit(this);
			b.addExpressions(expressions.pop());
		}

		for (Object a : node.getArguments()) {
			((AstNode) a).visit(this);
			b.addMethodArgs(expressions.pop());
		}
		expressions.push(b.build());
		return false;
	}

	public boolean visit(ExpressionStatement node) {
		boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
		List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.EXPRESSION);
		node.getExpression().visit(this);
		b.setExpression(expressions.pop());
		list.add(b.build());
		return false;
	}

	public boolean visit(WithStatement node) {
		boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
		List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.OTHER);
		node.getExpression().visit(this);
		b.setExpression(expressions.pop());
		statements.push(new ArrayList<boa.types.Ast.Statement>());
		node.getStatement().visit(this);
		for (boa.types.Ast.Statement s : statements.pop())
			b.addStatements(s);
		list.add(b.build());
		return false;
	}

	public boolean visit(IfStatement node) {
		boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
		List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.IF);
		node.getCondition().visit(this);
		b.setCondition(expressions.pop());
		statements.push(new ArrayList<boa.types.Ast.Statement>());
		node.getThenPart().visit(this);
		for (boa.types.Ast.Statement s : statements.pop())
			b.addStatements(s);
		if (node.getElsePart() != null) {
			statements.push(new ArrayList<boa.types.Ast.Statement>());
			node.getElsePart().visit(this);
			for (boa.types.Ast.Statement s : statements.pop())
				b.addStatements(s);
		}
		list.add(b.build());
		return false;
	}

	public boolean visit(LabeledStatement node) {
		boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
		List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.LABEL);
		for (Label l : node.getLabels()) {
			statements.push(new ArrayList<boa.types.Ast.Statement>());
			l.visit(this);
			for (boa.types.Ast.Statement s : statements.pop())
				b.addStatements(s);
		}
		statements.push(new ArrayList<boa.types.Ast.Statement>());
		node.getStatement().visit(this);
		for (boa.types.Ast.Statement s : statements.pop())
			b.addStatements(s);
		list.add(b.build());
		return false;
	}

	public boolean visit(ReturnStatement node) {
		boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
		List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.RETURN);
		if (node.getReturnValue() != null) {
			node.getReturnValue().visit(this);
			b.setExpression(expressions.pop());
		}
		list.add(b.build());
		return false;
	}

	public boolean visit(SwitchCase node) {
		boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
		List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.CASE);
		if (node.getExpression() != null) {
			node.getExpression().visit(this);
			b.setExpression(expressions.pop());
		}
		if (node.getStatements() != null){
			for (AstNode s: node.getStatements()){
				statements.push(new ArrayList<boa.types.Ast.Statement>());
				s.visit(this);
				for (boa.types.Ast.Statement st : statements.pop())
					b.addStatements(st);
			}
		}
		list.add(b.build());
		return false;
	}

	public boolean visit(SwitchStatement node) {
		boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
		List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.SWITCH);
		node.getExpression().visit(this);
		b.setExpression(expressions.pop());
		statements.push(new ArrayList<boa.types.Ast.Statement>());
		for (SwitchCase c : node.getCases()) {
			statements.push(new ArrayList<boa.types.Ast.Statement>());
			c.visit(this);
			for (boa.types.Ast.Statement s : statements.pop())
				b.addStatements(s);
		}
		list.add(b.build());
		return false;
	}

	public boolean visit(ThrowStatement node) {
		boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
		List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.THROW);
		node.getExpression().visit(this);
		b.setExpression(expressions.pop());
		list.add(b.build());
		return false;
	}

	public boolean visit(TryStatement node) {
		boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
		List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.TRY);
		statements.push(new ArrayList<boa.types.Ast.Statement>());
		node.getTryBlock().visit(this);
		for (Object c : node.getCatchClauses())
			((CatchClause) c).visit(this);
		if (node.getFinallyBlock() != null)
			node.getFinallyBlock().visit(this);
		for (boa.types.Ast.Statement s : statements.pop())
			b.addStatements(s);
		list.add(b.build());
		return false;
	}

	public boolean visit(WhileLoop node) {
		boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
		List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.WHILE);
		node.getCondition().visit(this);
		b.setCondition(expressions.pop());
		statements.push(new ArrayList<boa.types.Ast.Statement>());
		node.getBody().visit(this);
		for (boa.types.Ast.Statement s : statements.pop())
			b.addStatements(s);
		list.add(b.build());
		return false;
	}

	public boolean visit(Label node) {
		boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
		List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.LABEL);
		list.add(b.build());
		return false;
	}
 
	public boolean visit(GeneratorExpression node){
		Statement.Builder b = boa.types.Ast.Statement.newBuilder();
		b.setKind(boa.types.Ast.Statement.StatementKind.OTHER);
		List<boa.types.Ast.Statement> list = statements.peek();
		node.getResult().visit(this);
		b.addUpdates(expressions.pop());
		for (Loop l: node.getLoops()){
			statements.push(new ArrayList<boa.types.Ast.Statement>());
			l.visit(this);
			for (boa.types.Ast.Statement d : statements.pop())
				b.addStatements(d);
		}
		if (node.getFilter() != null){
			node.getFilter().visit(this);
			b.addUpdates(expressions.pop());
		}
		list.add(b.build());	
		return false;
	}
	
	public boolean visit(GeneratorExpressionLoop node){
		Statement.Builder b = boa.types.Ast.Statement.newBuilder();
		b.setKind(boa.types.Ast.Statement.StatementKind.FOR);
		List<boa.types.Ast.Statement> list = statements.peek();
		node.getIterator().visit(this);
		b.addInitializations(expressions.pop());
		node.getIteratedObject().visit(this);
		b.addInitializations(expressions.pop());
		list.add(b.build());
		return false;
	}
	
	//////////////////////////////////////////////////////////////
	// Expressions
	

	public boolean visit(ElementGet node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		b.setKind(boa.types.Ast.Expression.ExpressionKind.ARRAYINDEX);
		node.getTarget().visit(this);
		b.addExpressions(expressions.pop());
		node.getElement().visit(this);
		b.addExpressions(expressions.pop());
		expressions.push(b.build());
		return false;
	}

	public boolean visit(ArrayLiteral node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		b.setKind(boa.types.Ast.Expression.ExpressionKind.LITERAL);
		boa.types.Ast.Type.Builder tb = boa.types.Ast.Type.newBuilder();
		tb.setName(node.toSource());// typeName(node.getType()));
		tb.setKind(boa.types.Ast.TypeKind.OTHER);
		b.setNewType(tb.build());
		for (Object e : node.getElements()) {
			((AstNode) e).visit(this);
			b.addExpressions(expressions.pop());
		}
		expressions.push(b.build());
		return false;
	}

	public boolean visit(ArrayComprehension node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		b.setKind(boa.types.Ast.Expression.ExpressionKind.NEWARRAY);
		boa.types.Ast.Type.Builder tb = boa.types.Ast.Type.newBuilder();
		tb.setName(Token.typeToName(node.getType()));
		tb.setKind(boa.types.Ast.TypeKind.OTHER);
		b.setNewType(tb.build());
		node.getResult().visit(this);
		b.addExpressions(expressions.pop());
		for (Object l : node.getLoops()) {
			((AstNode) l).visit(this);
			b.addExpressions(expressions.pop());
		}
		if (node.getFilter() != null){
			node.getFilter().visit(this);
			b.addExpressions(expressions.pop());
		}
		expressions.push(b.build());
		return false;
	}

	public boolean visit(ArrayComprehensionLoop node) {
		Statement.Builder s = boa.types.Ast.Statement.newBuilder();
		Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		List<boa.types.Ast.Statement> list = statements.peek();
		s.setKind(boa.types.Ast.Statement.StatementKind.FOR);
		b.setKind(boa.types.Ast.Expression.ExpressionKind.OTHER);
		node.getIterator().visit(this);
		b.addExpressions(expressions.pop());
		node.getIteratedObject();
		b.addExpressions(expressions.pop());
		s.setExpression(b.build());
		list.add(s.build());
		return false;
	}

	public boolean visit(VariableInitializer node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		b.setKind(boa.types.Ast.Expression.ExpressionKind.OTHER);
		node.getTarget().visit(this);
		b.addExpressions(expressions.pop());
		if (node.getInitializer() != null) {
			node.getInitializer().visit(this);
			b.addExpressions(expressions.pop());
		}
		expressions.push(b.build());
		return false;
	}

	public boolean visit(Assignment node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		node.getLeft().visit(this);
		b.addExpressions(expressions.pop());
		node.getRight().visit(this);
		b.addExpressions(expressions.pop());
		if (node.getOperator() == Token.ASSIGN)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.ASSIGN);
		else if (node.getOperator() == Token.ASSIGN_BITAND)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.ASSIGN_BITAND);
		else if (node.getOperator() == Token.ASSIGN_BITOR)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.ASSIGN_BITOR);
		else if (node.getOperator() == Token.ASSIGN_BITXOR)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.ASSIGN_BITXOR);
		else if (node.getOperator() == Token.ASSIGN_DIV)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.ASSIGN_DIV);
		else if (node.getOperator() == Token.ASSIGN_LSH)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.ASSIGN_LSHIFT);
		else if (node.getOperator() == Token.ASSIGN_SUB)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.ASSIGN_SUB);
		else if (node.getOperator() == Token.ASSIGN_ADD)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.ASSIGN_ADD);
		else if (node.getOperator() == Token.ASSIGN_MOD)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.ASSIGN_MOD);
		else if (node.getOperator() == Token.ASSIGN_RSH)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.ASSIGN_RSHIFT);
		else if (node.getOperator() == Token.ASSIGN_URSH)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.ASSIGN_UNSIGNEDRSHIFT);
		else if (node.getOperator() == Token.ASSIGN_MUL)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.ASSIGN_MULT);
		expressions.push(b.build());
		return false;
	}

	public boolean visit(KeywordLiteral node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		b.setKind(boa.types.Ast.Expression.ExpressionKind.LITERAL);
		b.setLiteral(node.toSource());
		expressions.push(b.build());
		return false;
	}

	public boolean visit(ConditionalExpression node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		b.setKind(boa.types.Ast.Expression.ExpressionKind.CONDITIONAL);
		node.getTestExpression().visit(this);
		b.addExpressions(expressions.pop());
		node.getTrueExpression().visit(this);
		b.addExpressions(expressions.pop());
		node.getFalseExpression().visit(this);
		b.addExpressions(expressions.pop());
		expressions.push(b.build());
		return false;
	}

	public boolean visit(Name node) {
		boa.types.Ast.Expression.Builder bui = boa.types.Ast.Expression.newBuilder();
		bui.setVariable(node.getIdentifier());
		bui.setKind(boa.types.Ast.Expression.ExpressionKind.OTHER);
		expressions.push(bui.build());
		return false;
	}

	public boolean visit(PropertyGet node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		b.setKind(boa.types.Ast.Expression.ExpressionKind.OTHER);
		node.getTarget().visit(this);;
		b.addExpressions(expressions.pop());
		node.getProperty().visit(this);
		b.addExpressions(expressions.pop());
		expressions.push(b.build());
		return false;
	}

	public boolean visit(InfixExpression node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		if (node.getOperator() == Token.BITAND)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.BIT_AND);
		else if (node.getOperator() == Token.AND)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.LOGICAL_AND);
		else if (node.getOperator() == Token.OR)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.LOGICAL_OR);
		else if (node.getOperator() == Token.DIV)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.OP_DIV);
		else if (node.getOperator() == Token.EQ)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.EQ);
		else if (node.getOperator() == Token.GT)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.GT);
		else if (node.getOperator() == Token.GE)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.GTEQ);
		else if (node.getOperator() == Token.LSH)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.BIT_LSHIFT);
		else if (node.getOperator() == Token.LT)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.LT);
		else if (node.getOperator() == Token.LE)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.LTEQ);
		else if (node.getOperator() == Token.SUB)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.OP_SUB);
		else if (node.getOperator() == Token.NE)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.NEQ);
		else if (node.getOperator() == Token.BITOR)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.BIT_OR);
		else if (node.getOperator() == Token.ADD)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.OP_ADD);
		else if (node.getOperator() == Token.MOD)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.OP_MOD);
		else if (node.getOperator() == Token.RSH)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.BIT_RSHIFT);
		else if (node.getOperator() == Token.URSH)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.BIT_UNSIGNEDRSHIFT);
		else if (node.getOperator() == Token.MUL)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.OP_MULT);
		else if (node.getOperator() == Token.BITXOR)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.BIT_XOR);
		else if (node.getOperator() == Token.SHEQ)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.OTHER);
		else if (node.getOperator() == Token.SHNE)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.OTHER);
		else if (node.getOperator() == Token.IN)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.OTHER);
		else
			b.setKind(boa.types.Ast.Expression.ExpressionKind.OTHER);
		node.getLeft().visit(this);
		b.addExpressions(expressions.pop());
		node.getRight().visit(this);
		b.addExpressions(expressions.pop());
		expressions.push(b.build());
		return false;
	}

	public boolean visit(NewExpression node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		b.setKind(boa.types.Ast.Expression.ExpressionKind.NEW);
		node.getTarget().visit(this);
		b.addExpressions(expressions.pop());
		for (AstNode arg: node.getArguments()){
			arg.visit(this);
			b.addMethodArgs(expressions.pop());
		}
		if (node.getInitializer() != null){
			node.getInitializer().visit(this);
			b.addExpressions(expressions.pop());
		}
		expressions.push(b.build());
		return false;
	}

	public boolean visit(ObjectLiteral node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		b.setKind(boa.types.Ast.Expression.ExpressionKind.LITERAL);
		b.setLiteral("object");
		for (ObjectProperty prop: node.getElements()){
			prop.visit(this);
			b.addExpressions(expressions.pop());
		}
		expressions.push(b.build());
		return false;
	}

	public boolean visit(ObjectProperty node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		b.setKind(boa.types.Ast.Expression.ExpressionKind.OTHER);
		if (node.getLeft() != null) {
			node.getLeft().visit(this);
			b.addExpressions(expressions.pop());
		}
		if (node.getRight() != null) {
			node.getRight().visit(this);
			b.addExpressions(expressions.pop());
		}
		expressions.push(b.build());
		return false;
	}

	public boolean visit(NumberLiteral node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		b.setKind(boa.types.Ast.Expression.ExpressionKind.LITERAL);
		b.setLiteral("" + node.getNumber());
		expressions.push(b.build());
		return false;
	}

	public boolean visit(ParenthesizedExpression node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		b.setKind(boa.types.Ast.Expression.ExpressionKind.PAREN);
		node.getExpression().visit(this);
		b.addExpressions(expressions.pop());
		expressions.push(b.build());
		return true;
	}

	public boolean visit(UnaryExpression node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		if (node.isPostfix()) {
			b.setIsPostfix(true);
		}
		if (node.getOperator() == Token.DEC)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.OP_DEC);
		else if (node.getOperator() == Token.INC)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.OP_INC);
		else if (node.getOperator() == Token.ADD)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.OP_ADD);
		else if (node.getOperator() == Token.SUB)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.OP_SUB);
		else if (node.getOperator() == Token.BITNOT)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.BIT_NOT);
		else if (node.getOperator() == Token.NOT)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.LOGICAL_NOT);
		node.getOperand().visit(this);
		b.addExpressions(expressions.pop());
		expressions.push(b.build());
		return false;
	}

	public boolean visit(RegExpLiteral node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		b.setKind(boa.types.Ast.Expression.ExpressionKind.LITERAL);
		b.setLiteral(node.getValue());
		expressions.push(b.build());
		return false;
	}

	public boolean visit(StringLiteral node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		b.setKind(boa.types.Ast.Expression.ExpressionKind.LITERAL);
		b.setLiteral(node.getValue());
		expressions.push(b.build());
		return false;
	}

	public boolean visit(Yield node) {
		boa.types.Ast.Expression.Builder eb = boa.types.Ast.Expression.newBuilder();
		eb.setKind(boa.types.Ast.Expression.ExpressionKind.OTHER);
		if (node.getValue() != null) {
			node.getValue().visit(this);
			eb.addExpressions(expressions.pop());
		}
		expressions.push(eb.build());
		return false;
	}

	//////////////////////////////////////////////////////////////
	// Xml nodes

	public boolean visit(XmlDotQuery node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		b.setKind(boa.types.Ast.Expression.ExpressionKind.OTHER);
		node.getLeft().visit(this);
		b.addExpressions(expressions.pop());
		if (node.getRight() != null) {
			node.getRight().visit(this);
			b.addExpressions(expressions.pop());
		}
		expressions.push(b.build());
		return false;
	}

	public boolean visit(XmlExpression node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		b.setKind(boa.types.Ast.Expression.ExpressionKind.OTHER);
		node.getExpression();
		b.addExpressions(expressions.pop());
		expressions.push(b.build());
		return false;
	}

	public boolean visit(XmlLiteral node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		b.setKind(boa.types.Ast.Expression.ExpressionKind.LITERAL);
		b.setLiteral("xml");
		for (XmlFragment frag : node.getFragments()) {
			frag.visit(this);
			b.addExpressions(expressions.pop());
		}
		return false;
	}

	public boolean visit(XmlMemberGet node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		b.setKind(boa.types.Ast.Expression.ExpressionKind.OTHER);
		node.getLeft().visit(this);
		b.addExpressions(expressions.pop());
		node.getRight().visit(this);
		b.addExpressions(expressions.pop());
		expressions.push(b.build());
		return false;
	}

	public boolean visit(XmlPropRef node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		b.setKind(boa.types.Ast.Expression.ExpressionKind.OTHER);
		if (node.getNamespace() != null){
			node.getNamespace().visit(this);
			b.addExpressions(expressions.pop());
		}
		node.getPropName().visit(this);
		b.addExpressions(expressions.pop());
		expressions.push(b.build());
		return false;
	}

	public boolean visit(XmlString node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		b.setKind(boa.types.Ast.Expression.ExpressionKind.LITERAL);
		b.setLiteral(node.getXml());
		expressions.push(b.build());
		return false;
	}

	public boolean visit(XmlElemRef node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		b.setKind(boa.types.Ast.Expression.ExpressionKind.OTHER);
		if (node.getNamespace() != null){
			node.getNamespace().visit(this);
			b.addExpressions(expressions.pop());
		}
		node.getExpression().visit(this);
		b.addExpressions(expressions.pop());
		expressions.push(b.build());
		return false;
	}

	//////////////////////////////////////////////////////////////
	// Currently un-used node types

	public boolean visit(Scope node) {
		throw new RuntimeException("visited unused node Scope");
	}

	public boolean visit(ScriptNode node) {
		throw new RuntimeException("visited unused node ScriptNode");
	}

	public boolean visit(Jump node) {
		throw new RuntimeException("visited unused node JumpNode");
	}

	public boolean visit(XmlRef node) {
		throw new RuntimeException("visited unused node XmlRef");
	}

	
	@Override
	public boolean visit(AstNode node) {
		throw new RuntimeException("visited unused node " + node.getClass());
	}
	
}