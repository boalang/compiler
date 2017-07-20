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

	public boolean accept(AstRoot node) {
		String pkg = "";
		if (node.getSourceName() != null)
			pkg = node.getSourceName(); // getPackage();
		b.setName(pkg);
		if (node.getComments() != null) {
			for (Object c : node.getComments())
				((Comment) c).visit(this);
		}
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

	public boolean accept(Comment node) {
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

	public boolean accept(VariableDeclaration node) {
		boa.types.Ast.Expression.Builder eb = boa.types.Ast.Expression.newBuilder();
		eb.setKind(boa.types.Ast.Expression.ExpressionKind.VARDECL);
		for (VariableInitializer f : node.getVariables()) {
			f.visit(this);
			eb.addExpressions(expressions.pop());
		} 
		expressions.push(eb.build());
		return false;
	}

	public boolean accept(LetNode node) {
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

	public boolean accept(FunctionNode node) {
		List<boa.types.Ast.Method> list = methods.peek();
		Method.Builder b = Method.newBuilder();
		b.setName(node.getName());
		Type.Builder tb = Type.newBuilder();
		tb.setName("");
		tb.setKind(TypeKind.OTHER);
		b.setReturnType(tb.build());
		for (AstNode p : node.getParams()) {
			Variable.Builder vb = Variable.newBuilder();
			vb.setName(Token.typeToName(p.getType()));
			boa.types.Ast.Type.Builder tp = boa.types.Ast.Type.newBuilder();
			String name = "";
			name = ((Name)p).getIdentifier();
			tp.setName(name);
			tp.setKind(boa.types.Ast.TypeKind.OTHER);
			vb.setVariableType(tp.build());
			b.addArguments(vb.build());
		}
		statements.push(new ArrayList<boa.types.Ast.Statement>());
		node.getBody().visit(this);
		for (boa.types.Ast.Statement s : statements.pop())
			b.addStatements(s);
		if (!node.isExpressionClosure()){
			if (node.getMemberExprNode() != null){
				node.getMemberExprNode().visit(this);
				Statement.Builder sb = Statement.newBuilder();
				sb.setKind(Statement.StatementKind.EXPRESSION);
				sb.setExpression(expressions.pop()); //FIXME
				b.addStatements(sb.build());
			}
		}
		list.add(b.build());
		return false;
	}

	//////////////////////////////////////////////////////////////
	// Statements

	public boolean accept(Block node) {
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

	public boolean accept(BreakStatement node) {
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

	public boolean accept(CatchClause node) {
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

	public boolean accept(ContinueStatement node) {
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

	public boolean accept(DoLoop node) {
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

	public boolean accept(EmptyStatement node) {
		boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
		List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.EMPTY);
		list.add(b.build());
		return false;
	}

	public boolean accept(EmptyExpression node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		b.setKind(boa.types.Ast.Expression.ExpressionKind.OTHER);//EMPTY); FIXME
		expressions.push(b.build());
		return false;
	}

	public boolean accept(ErrorNode node) {
		boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
		List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.OTHER);
		list.add(b.build());
		return false;
	}

	public boolean accept(ForInLoop node) {
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
	
	public boolean accept(ForLoop node){
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

	public boolean accept(FunctionCall node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		b.setKind(boa.types.Ast.Expression.ExpressionKind.METHODCALL);
		if (node.getTarget() != null) {
			AstNode target = node.getTarget();
			if (target instanceof Name)
				b.setMethod(((Name) target).getIdentifier());
			else {
				if (target instanceof FunctionNode) {
					methods.push(new ArrayList<Method>());
					target.visit(this);
					for (Method m : methods.pop())// FIXME add repeated Method methods to Expression?
						b.setLambda(m);
				} else {
					target.visit(this);
					b.addMethodArgs(expressions.pop());
				}
			}
		}
		for (AstNode a : node.getArguments()) {
			if (a instanceof FunctionNode) {
				methods.push(new ArrayList<Method>());
				a.visit(this);
				for (Method m : methods.pop())// FIXME add repeated Method methods to Expression?
					b.setLambda(m);
			} else {
				a.visit(this);
				b.addMethodArgs(expressions.pop());
			}
		}
		expressions.push(b.build());
		return false;
	}

	public boolean accept(ExpressionStatement node) {
		boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
		List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.EXPRESSION);
		node.getExpression().visit(this);
		b.setExpression(expressions.pop());
		list.add(b.build());
		return false;
	}

	public boolean accept(WithStatement node) {
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

	public boolean accept(IfStatement node) {
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

	public boolean accept(LabeledStatement node) {
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

	public boolean accept(ReturnStatement node) {
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

	public boolean accept(SwitchCase node) {
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

	public boolean accept(SwitchStatement node) {
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

	public boolean accept(ThrowStatement node) {
		boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
		List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.THROW);
		node.getExpression().visit(this);
		b.setExpression(expressions.pop());
		list.add(b.build());
		return false;
	}

	public boolean accept(TryStatement node) {
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

	public boolean accept(WhileLoop node) {
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

	public boolean accept(Label node) {
		boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
		List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.LABEL);
		list.add(b.build());
		return false;
	}
 
	public boolean accept(GeneratorExpression node){
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
	
	public boolean accept(GeneratorExpressionLoop node){
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
	

	public boolean accept(ElementGet node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		b.setKind(boa.types.Ast.Expression.ExpressionKind.ARRAYINDEX);
		node.getTarget().visit(this);
		b.addExpressions(expressions.pop());
		node.getElement().visit(this);
		b.addExpressions(expressions.pop());
		expressions.push(b.build());
		return false;
	}

	public boolean accept(ArrayLiteral node) {
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

	public boolean accept(ArrayComprehension node) {
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

	public boolean accept(ArrayComprehensionLoop node) {
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

	public boolean accept(VariableInitializer node) {
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

	public boolean accept(Assignment node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		node.getLeft().visit(this);
		b.addExpressions(expressions.pop());
		if (node.getRight() != null) {
			AstNode exp = node.getRight();
			if (exp instanceof FunctionNode) {
				methods.push(new ArrayList<Method>());
				exp.visit(this);
				for (Method m : methods.pop())// FIXME add repeated Method methods to Expression?
					b.setLambda(m);
			} else {
				exp.visit(this);
				b.addMethodArgs(expressions.pop());
			}
		}
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

	public boolean accept(KeywordLiteral node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		b.setKind(boa.types.Ast.Expression.ExpressionKind.LITERAL);
		b.setLiteral(node.toSource());
		expressions.push(b.build());
		return false;
	}

	public boolean accept(ConditionalExpression node) {
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

	public boolean accept(Name node) {
		boa.types.Ast.Expression.Builder bui = boa.types.Ast.Expression.newBuilder();
		bui.setVariable(node.getIdentifier());//FIXME
		bui.setKind(boa.types.Ast.Expression.ExpressionKind.OTHER);
		expressions.push(bui.build());
		return false;
	}

	public boolean accept(PropertyGet node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		b.setKind(boa.types.Ast.Expression.ExpressionKind.OTHER);
		node.getTarget().visit(this);;
		b.addExpressions(expressions.pop());
		node.getProperty().visit(this);
		b.addExpressions(expressions.pop());
		expressions.push(b.build());
		return false;
	}

	public boolean accept(InfixExpression node) {
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

	public boolean accept(NewExpression node) {
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

	public boolean accept(ObjectLiteral node) {
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

	public boolean accept(ObjectProperty node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		b.setKind(boa.types.Ast.Expression.ExpressionKind.OTHER);
		if (node.getLeft() != null) {
			node.getLeft().visit(this);
			b.addExpressions(expressions.pop());
		}
		if (node.getRight() != null) {
			AstNode exp = node.getRight();
			if (exp instanceof FunctionNode) {
				methods.push(new ArrayList<Method>());
				exp.visit(this);
				for (Method m : methods.pop())// FIXME add repeated Method methods to Expression?
					b.setLambda(m);
			} else {
				exp.visit(this);
				b.addMethodArgs(expressions.pop());
			}
		}
		expressions.push(b.build());
		return false;
	}

	public boolean accept(NumberLiteral node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		b.setKind(boa.types.Ast.Expression.ExpressionKind.LITERAL);
		b.setLiteral("" + node.getNumber());
		expressions.push(b.build());
		return false;
	}

	public boolean accept(ParenthesizedExpression node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		b.setKind(boa.types.Ast.Expression.ExpressionKind.PAREN);
		AstNode exp = node.getExpression();
		if (exp instanceof FunctionNode) {
			methods.push(new ArrayList<Method>());
			exp.visit(this);
			for (Method m : methods.pop())// FIXME add repeated Method methods to Expression?
				b.setLambda(m);
		} else {
			exp.visit(this);
			b.addMethodArgs(expressions.pop());
		}
		expressions.push(b.build());
		return true;
	}

	public boolean accept(UnaryExpression node) {
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
		else
			b.setKind(boa.types.Ast.Expression.ExpressionKind.OTHER);
		node.getOperand().visit(this);
		b.addExpressions(expressions.pop());
		expressions.push(b.build());
		return false;
	}

	public boolean accept(RegExpLiteral node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		b.setKind(boa.types.Ast.Expression.ExpressionKind.LITERAL);
		b.setLiteral(node.getValue());
		expressions.push(b.build());
		return false;
	}

	public boolean accept(StringLiteral node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		b.setKind(boa.types.Ast.Expression.ExpressionKind.LITERAL);
		b.setLiteral(node.getValue());
		expressions.push(b.build());
		return false;
	}

	public boolean accept(Yield node) {
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

	public boolean accept(XmlDotQuery node) {
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

	public boolean accept(XmlExpression node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		b.setKind(boa.types.Ast.Expression.ExpressionKind.OTHER);
		node.getExpression();
		b.addExpressions(expressions.pop());
		expressions.push(b.build());
		return false;
	}

	public boolean accept(XmlLiteral node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		b.setKind(boa.types.Ast.Expression.ExpressionKind.LITERAL);
		b.setLiteral("xml");
		for (XmlFragment frag : node.getFragments()) {
			frag.visit(this);
			b.addExpressions(expressions.pop());
		}
		return false;
	}

	public boolean accept(XmlMemberGet node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		b.setKind(boa.types.Ast.Expression.ExpressionKind.OTHER);
		node.getLeft().visit(this);
		b.addExpressions(expressions.pop());
		node.getRight().visit(this);
		b.addExpressions(expressions.pop());
		expressions.push(b.build());
		return false;
	}

	public boolean accept(XmlPropRef node) {
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

	public boolean accept(XmlString node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		b.setKind(boa.types.Ast.Expression.ExpressionKind.LITERAL);
		b.setLiteral(node.getXml());
		expressions.push(b.build());
		return false;
	}

	public boolean accept(XmlElemRef node) {
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

	public boolean accept(Scope node) {
		boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
		List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.OTHER);//FIXME 
		for (Node s : node) {
			if (s instanceof FunctionNode) {
				methods.push(new ArrayList<boa.types.Ast.Method>());
				((AstNode) s).visit(this);
			} else if (s instanceof VariableDeclaration){
				((AstNode) s).visit(this);
				b.addInitializations(expressions.pop());
			}else {
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
	//////////////////////////////////////////////////////////////
	// Currently un-used node types



	public boolean accept(ScriptNode node) {
		throw new RuntimeException("visited unused node ScriptNode");
	}

	public boolean accept(Jump node) {
		throw new RuntimeException("visited unused node JumpNode");
	}

	public boolean accept(XmlRef node) {
		throw new RuntimeException("visited unused node XmlRef");
	}

	
	@Override
	public boolean visit(AstNode node) {
		if (node instanceof AstRoot){
			this.accept((AstRoot) node);
			return false;
		}
		if (node instanceof ArrayComprehension){
			this.accept((ArrayComprehension)node);
			return false;
		}
		if (node instanceof ArrayComprehensionLoop){
			this.accept((ArrayComprehensionLoop)node);
			return false;
		}
		if (node instanceof ArrayLiteral){
			this.accept((ArrayLiteral)node);
			return false;
		}
		if (node instanceof Assignment){
			this.accept((Assignment) node);
			return false;
		}
		if (node instanceof Block){
			this.accept((Block) node);
			return false;
		}
		if (node instanceof BreakStatement){
			this.accept((BreakStatement)node);
			return false;
		}
		if (node instanceof CatchClause){
			this.accept((CatchClause)node);
			return false;
		}
		if (node instanceof Comment){
			this.accept((Comment)node);
			return false;
		}
		if (node instanceof ConditionalExpression){
			this.accept((ConditionalExpression) node);
			return false;
		}
		if (node instanceof ContinueStatement){
			this.accept((ContinueStatement) node);
			return false;
		}
		if (node instanceof DoLoop){
			this.accept((DoLoop)node);
			return false;
		}
		if (node instanceof ElementGet){
			this.accept((ElementGet)node);
			return false;
		}
		if (node instanceof EmptyExpression){
			this.accept((EmptyExpression)node);
			return false;
		}
		if (node instanceof EmptyStatement){
			this.accept((EmptyStatement) node);
			return false;
		}
		if (node instanceof ExpressionStatement){
			this.accept((ExpressionStatement) node);
			return false;
		}
		if (node instanceof GeneratorExpressionLoop){
			this.accept((GeneratorExpressionLoop)node);
			return false;
		}
		if (node instanceof ForInLoop){
			this.accept((ForInLoop)node);
			return false;
		}
		if (node instanceof NewExpression){
			this.accept((NewExpression)node);
			return false;
		}
		if (node instanceof FunctionCall){
			this.accept((FunctionCall)node);
			return false;
		}
		if (node instanceof FunctionNode){
			this.accept((FunctionNode)node);
			return false;
		}
		if (node instanceof IfStatement){
			this.accept((IfStatement) node);
			return false;
		}
		if (node instanceof GeneratorExpression){
			this.accept((GeneratorExpression) node);
			return false;
		}
		if (node instanceof ObjectProperty){
			this.accept((ObjectProperty)node);
			return false;
		}
		if (node instanceof PropertyGet){
			this.accept((PropertyGet)node);
			return false;
		}
		if (node instanceof XmlDotQuery){
			this.accept((XmlDotQuery) node);
			return false;
		}
		if (node instanceof XmlMemberGet){
			this.accept((XmlMemberGet) node);
			return false;
		}
		if (node instanceof InfixExpression){
			this.accept((InfixExpression)node);
			return false;
		}
		if (node instanceof KeywordLiteral){
			this.accept((KeywordLiteral)node);
			return false;
		}
		if (node instanceof Label){
			this.accept((Label) node);
			return false;
		}
		if (node instanceof LabeledStatement){
			this.accept((LabeledStatement) node);
			return false;
		}
		if (node instanceof LetNode){
			this.accept((LetNode) node);
			return false;
		}
		if (node instanceof Name){
			this.accept((Name)node);
			return false;
		}
		if (node instanceof NumberLiteral){
			this.accept((NumberLiteral)node);
			return false;
		}
		if (node instanceof ObjectLiteral){
			this.accept((ObjectLiteral) node);
			return false;
		}
		if (node instanceof ParenthesizedExpression){
			this.accept((ParenthesizedExpression) node);
			return false;
		}
		if (node instanceof RegExpLiteral){
			this.accept((RegExpLiteral)node);
			return false;
		}
		if (node instanceof ReturnStatement){
			this.accept((ReturnStatement)node);
			return false;
		}
		if (node instanceof StringLiteral){
			this.accept((StringLiteral)node);
			return false;
		}
		if (node instanceof SwitchStatement){
			this.accept((SwitchStatement) node);
			return false;
		}
		if (node instanceof SwitchCase){
			this.accept((SwitchCase) node);
			return false;
		}
		if (node instanceof ThrowStatement){
			this.accept((ThrowStatement) node);
			return false;
		}
		if (node instanceof TryStatement){
			this.accept((TryStatement) node);
			return false;
		}
		if (node instanceof UnaryExpression){
			this.accept((UnaryExpression)node);
			return false;
		}
		if (node instanceof VariableDeclaration){
			this.accept((VariableDeclaration)node);
			return false;
		}
		if (node instanceof VariableInitializer){
			this.accept((VariableInitializer)node);
			return false;
		}
		if (node instanceof WhileLoop){
			this.accept((WhileLoop) node);
			return false;
		}
		if (node instanceof WithStatement){
			this.accept((WhileLoop) node);
			return false;
		}
		if (node instanceof XmlElemRef){
			this.accept((XmlElemRef)node);
			return false;
		}
		if (node instanceof XmlPropRef){
			this.accept((XmlPropRef)node);
			return false;
		}
		if (node instanceof XmlString){
			this.accept((XmlString)node);
			return false;
		}
		if (node instanceof XmlRef){
			this.accept((XmlRef)node);
			return false;
		}
		if (node instanceof Yield){
			this.accept((Yield)node);
			return false;
		}
		if (node instanceof ScriptNode){
			this.accept((ScriptNode)node);
			return false;
		}
		if (node instanceof Scope){
			this.accept((Scope) node);
			return false;
		}
		if (node instanceof Jump){
			this.accept((Jump)node);
			return false;
		}
		System.err.println("visited unused node" + node.getClass());
		throw new RuntimeException("visited unused node");
	}
}