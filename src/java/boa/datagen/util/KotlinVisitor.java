/*
 * Copyright 2021, Robert Dyer, Samuel W. Flint,
 *                 and University of Nebraska Board of Regents
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
import java.util.List;
import java.util.Stack;

import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import org.jetbrains.kotlin.lexer.KtModifierKeywordToken;
import org.jetbrains.kotlin.psi.*;

import boa.types.Ast.Declaration;
import boa.types.Ast.Expression;
import boa.types.Ast.Method;
import boa.types.Ast.Modifier;
import boa.types.Ast.Namespace;
import boa.types.Ast.Statement;
import boa.types.Ast.Type;
import boa.types.Ast.TypeKind;
import boa.types.Ast.Variable;


/**
 * @author rdyer
 * @author swflint
 */
public class KotlinVisitor extends KtVisitor<Void, Void> {
	protected Namespace.Builder b = Namespace.newBuilder();
	protected Stack<List<Declaration>> declarations = new Stack<List<Declaration>>();
	protected Stack<List<Modifier>> modifiers = new Stack<List<Modifier>>();
	protected Stack<List<Expression>> expressions = new Stack<List<Expression>>();
	protected Stack<List<Variable>> fields = new Stack<List<Variable>>();
	protected Stack<List<Method>> methods = new Stack<List<Method>>();
	protected Stack<List<Statement>> statements = new Stack<List<Statement>>();

	protected Stack<Boolean> expectExpression = new Stack<Boolean>();

	public static final int KLS10 = 10;
	public static final int KLS11 = 11;
	public static final int KLS12 = 12;
	public static final int KLS13 = 13;
	public static final int KLS14 = 14;
	public static final int KLS15 = 15;
	protected int astLevel = KLS10;

	public int getAstLevel() {
		return astLevel;
	}

	public Namespace getNamespace(final KtFile kt) {
		kt.accept(this);

		return b.build();
	}

	@Override
	public void visitElement(final PsiElement element) {
		if (element instanceof LeafPsiElement)
			visitLeaf((LeafPsiElement)element);
		else
			element.acceptChildren(this);
	}

	public void visitLeaf(final LeafPsiElement leaf) {
		if (leaf.getElementType() instanceof KtModifierKeywordToken)
			visitModifier((KtModifierKeywordToken)leaf.getElementType());
	}

	public void visitModifier(final KtModifierKeywordToken m) {
		final Modifier.Builder mb = Modifier.newBuilder();

		switch (m.getValue()) {
		case "abstract":
			mb.setKind(Modifier.ModifierKind.ABSTRACT);
			break;

		case "const":
			mb.setKind(Modifier.ModifierKind.STATIC);
			break;

		case "final":
			mb.setKind(Modifier.ModifierKind.FINAL);
			break;

		case "annotation":
			mb.setKind(Modifier.ModifierKind.ANNOTATION);
			break;

		case "open":
		case "value":
		case "inner":
		case "sealed":
			mb.setKind(Modifier.ModifierKind.OTHER);
			mb.setOther(m.getValue());
			break;

		case "public":
			mb.setKind(Modifier.ModifierKind.VISIBILITY);
			mb.setVisibility(Modifier.Visibility.PUBLIC);
			break;

		case "protected":
			mb.setKind(Modifier.ModifierKind.VISIBILITY);
			mb.setVisibility(Modifier.Visibility.PROTECTED);
			break;

		case "private":
			mb.setKind(Modifier.ModifierKind.VISIBILITY);
			mb.setVisibility(Modifier.Visibility.PRIVATE);
			break;

		case "internal":
			mb.setKind(Modifier.ModifierKind.VISIBILITY);
			mb.setVisibility(Modifier.Visibility.INTERNAL);
			break;

		case "data":
		case "enum":
		case "companion":
			// do nothing because these are already handled by the class
			return;

		default:
			System.err.println("===> UNKNOWN MODIFIER: " + m.getValue());
			mb.setKind(Modifier.ModifierKind.OTHER);
			mb.setOther(m.getValue());
			break;
		}

		modifiers.peek().add(mb.build());
	}

	@Override
	public Void visitKtFile(final KtFile f, final Void v) {
		modifiers.push(new ArrayList<Modifier>());
		declarations.push(new ArrayList<Declaration>());
		fields.push(new ArrayList<Variable>());
		methods.push(new ArrayList<Method>());
		statements.push(new ArrayList<Statement>());
		expectExpression.push(false);

		b.setName("");
		f.acceptChildren(this);

		expectExpression.pop();
		b.addAllStatements(statements.pop());
		b.addAllMethods(methods.pop());
		b.addAllVariables(fields.pop());
		b.addAllDeclarations(declarations.pop());
		b.addAllModifiers(modifiers.pop());

		return null;
	}

	@Override
	public Void visitPackageDirective(final KtPackageDirective pkg, final Void v) {
		b.setName(pkg.getQualifiedName());
		return null;
	}

	@Override
	public Void visitImportDirective(final KtImportDirective imprt, final Void v) {
		final StringBuilder sb = new StringBuilder();
		sb.append(imprt.getImportedFqName().toString());

		if (imprt.isAllUnder())
			sb.append(".*");

		if (imprt.getAliasName() != null)
			sb.append(" as " + imprt.getAliasName());

		b.addImports(sb.toString());
		return null;
	}

	@Override
	public Void visitAnnotationEntry(final KtAnnotationEntry entry, final Void v) {
		final Modifier.Builder mb = Modifier.newBuilder();

		mb.setKind(Modifier.ModifierKind.ANNOTATION);

		if (entry.getUseSiteTarget() != null)
			mb.setOther(entry.getUseSiteTarget().getText());
		mb.setAnnotationName(entry.getCalleeExpression().getText());

		if (entry.getValueArgumentList() != null) {
			expressions.push(new ArrayList<Expression>());
			for (final KtValueArgument a : entry.getValueArgumentList().getArguments())
				a.getArgumentExpression().accept(this, v);
			mb.addAllAnnotationValues(expressions.pop());

			for (final KtValueArgument a : entry.getValueArgumentList().getArguments()) {
				if (a.getArgumentName() != null)
					mb.addAnnotationMembers(a.getArgumentName().getText());
				else
					mb.addAnnotationMembers("");
			}
		}

		modifiers.peek().add(mb.build());
		return null;
	}

	@Override
	public Void visitAnnotation(final KtAnnotation annotation, final Void v) {
		// TODO
		annotation.acceptChildren(this, v);
		return null;
	}

	@Override
	public Void visitDeclaration(final KtDeclaration d, final Void v) {
		// TODO
		d.acceptChildren(this, v);
		return null;
	}

	@Override
	public Void visitObjectDeclaration(final KtObjectDeclaration d, final Void v) {
		final Declaration.Builder db = Declaration.newBuilder();

		if (d.getName() != null)
			db.setName(d.getName());
		else
			db.setName("");

		db.setKind(TypeKind.SINGLETON);

		modifiers.push(new ArrayList<Modifier>());
		fields.push(new ArrayList<Variable>());
                methods.push(new ArrayList<Method>());
		declarations.push(new ArrayList<Declaration>());

		if (d.isCompanion()) {
                        modifiers.peek().add(Modifier.newBuilder()
					     .setKind(Modifier.ModifierKind.OTHER)
					     .setOther("COMPANION")
					     .build());
		}

		d.acceptChildren(this, v);

		db.addAllNestedDeclarations(declarations.pop());
		db.addAllMethods(methods.pop());
                db.addAllFields(fields.pop());
		db.addAllModifiers(modifiers.pop());

		declarations.peek().add(db.build());

		return null;
	}

	@Override
	public Void visitClassOrObject(final KtClassOrObject n, final Void v) {
		// TODO
		n.acceptChildren(this, v);
		return null;
	}

	@Override
	public Void visitDestructuringDeclaration(final KtDestructuringDeclaration d, final Void v) {
		final Expression.Builder eb = Expression.newBuilder();

		eb.setKind(Expression.ExpressionKind.VARDECL);

		fields.push(new ArrayList<Variable>());
		expressions.push(new ArrayList<Expression>());

		System.out.println(d.getEntries().size());
		for (final KtDestructuringDeclarationEntry entry : d.getEntries())
			entry.accept(this, v);
		if (d.hasInitializer())
			d.getInitializer().accept(this, v);

		eb.addAllExpressions(expressions.pop());
		eb.addAllVariableDecls(fields.pop());

		expressions.peek().add(eb.build());
		return null;
	}

	@Override
	public Void visitDestructuringDeclarationEntry(final KtDestructuringDeclarationEntry n, final Void v) {
		final Variable.Builder vb = Variable.newBuilder();

		vb.setName(n.getName());

		if (n.getTypeReference() != null)
			vb.setVariableType(typeFromTypeRef(n.getTypeReference()));

		if (n.getModifierList() != null) {
			modifiers.push(new ArrayList<Modifier>());
			n.getModifierList().accept(this, v);
			vb.addAllModifiers(modifiers.pop());
		}

		fields.peek().add(vb.build());
		return null;
	}

	@Override
	public Void visitTypeAlias(final KtTypeAlias ta, final Void v) {
		final Statement.Builder sb = Statement.newBuilder();

		sb.setKind(Statement.StatementKind.TYPEDECL);

		modifiers.push(new ArrayList<Modifier>());
		if (ta.getModifierList() != null)
			ta.getModifierList().accept(this, v);

		sb.setTypeDeclaration(Declaration.newBuilder()
				.setKind(TypeKind.ALIAS)
				.setName(ta.getName())
				.addParents(typeFromTypeRef(ta.getTypeReference()))
				.addAllModifiers(modifiers.pop())
				.build());

		// TODO type params
		ta.getTypeConstraints();
		ta.getTypeParameters();

		statements.peek().add(sb.build());
		return null;
	}

	@Override
	public Void visitScript(final KtScript n, final Void v) {
		// TODO
		n.acceptChildren(this, v);
		return null;
	}

	@Override
	public Void visitClassBody(final KtClassBody n, final Void v) {
		// TODO
		n.acceptChildren(this, v);
		return null;
	}

	@Override
	public Void visitConstructorCalleeExpression(final KtConstructorCalleeExpression expr, final Void v) {
		// TODO
		expr.acceptChildren(this, v);
		return null;
	}

	@Override
	public Void visitTypeParameterList(final KtTypeParameterList n, final Void v) {
		// TODO
		n.acceptChildren(this, v);
		return null;
	}

	@Override
	public Void visitTypeParameter(final KtTypeParameter n, final Void v) {
		// TODO
		n.acceptChildren(this, v);
		return null;
	}

	@Override
	public Void visitEnumEntry(final KtEnumEntry n, final Void v) {
		// TODO
		n.acceptChildren(this, v);
		return null;
	}

	@Override
	public Void visitParameterList(final KtParameterList n, final Void v) {
		// TODO
		n.acceptChildren(this, v);
		return null;
	}

	@Override
	public Void visitSuperTypeList(final KtSuperTypeList n, final Void v) {
		// TODO
		n.acceptChildren(this, v);
		return null;
	}

	@Override
	public Void visitSuperTypeListEntry(final KtSuperTypeListEntry n, final Void v) {
		// TODO
		n.acceptChildren(this, v);
		return null;
	}

	@Override
	public Void visitDelegatedSuperTypeEntry(final KtDelegatedSuperTypeEntry n, final Void v) {
		// TODO
		n.acceptChildren(this, v);
		return null;
	}

	@Override
	public Void visitSuperTypeCallEntry(final KtSuperTypeCallEntry n, final Void v) {
		// TODO
		n.acceptChildren(this, v);
		return null;
	}

	@Override
	public Void visitSuperTypeEntry(final KtSuperTypeEntry n, final Void v) {
		// TODO
		n.acceptChildren(this, v);
		return null;
	}

	@Override
	public Void visitConstructorDelegationCall(final KtConstructorDelegationCall n, final Void v) {
		// TODO
		n.acceptChildren(this, v);
		return null;
	}

	@Override
	public Void visitPropertyDelegate(final KtPropertyDelegate n, final Void v) {
		// TODO
		n.acceptChildren(this, v);
		return null;
	}

	@Override
	public Void visitTypeReference(final KtTypeReference n, final Void v) {
		// TODO
		n.acceptChildren(this, v);
		return null;
	}

	@Override
	public Void visitArgument(final KtValueArgument n, final Void v) {
		// TODO
		n.acceptChildren(this, v);
		return null;
	}

	@Override
	public Void visitSimpleNameExpression(final KtSimpleNameExpression expr, final Void v) {
		expressions.peek().add(Expression.newBuilder()
				.setKind(Expression.ExpressionKind.VARACCESS)
				.setVariable(expr.getReferencedName()).build());
		return null;
	}

	@Override
	public Void visitReferenceExpression(final KtReferenceExpression expr, final Void v) {
		// TODO
		expr.acceptChildren(this, v);
		return null;
	}

	@Override
	public Void visitLabeledExpression(final KtLabeledExpression expr, final Void v) {
		final Statement.Builder sb = Statement.newBuilder();

		sb.setKind(Statement.StatementKind.LABEL);

		sb.addExpressions(Expression.newBuilder()
				.setKind(Expression.ExpressionKind.LITERAL)
				.setLiteral(expr.getNameIdentifier().getText())
				.build());

		if (expr.getBaseExpression() != null) {
			final List<Statement> stmts = new ArrayList<Statement>();
			statements.push(stmts);

			final List<Expression> exprs = new ArrayList<Expression>();
			expressions.push(exprs);

			expr.getBaseExpression().accept(this, v);
			for (final Expression e: exprs) {
				stmts.add(Statement.newBuilder()
					.setKind(Statement.StatementKind.EXPRESSION)
					.addExpressions(e)
					.build());
			}
			exprs.clear();

			expressions.pop();
			sb.addAllStatements(statements.pop());
		}

		statements.peek().add(sb.build());
		return null;
	}

	// No need for visitPostfixExpression/visitPrefixExpression, this handles them both quite easily.
	@Override
	public Void visitUnaryExpression(final KtUnaryExpression expr, final Void v) {
		final Expression.Builder eb = Expression.newBuilder();

		expressions.push(new ArrayList<Expression>());
		if (expr.getBaseExpression() != null)
			expr.getBaseExpression().accept(this, v);
		eb.addAllExpressions(expressions.pop());

		switch(expr.getOperationToken().toString()) {
		case "MINUSMINUS":
			eb.setKind(Expression.ExpressionKind.OP_DEC);
			break;
		case "PLUSPLUS":
			eb.setKind(Expression.ExpressionKind.OP_INC);
			break;
		case "MINUS":
			eb.setKind(Expression.ExpressionKind.OP_SUB);
			break;
		case "EXCL":
			eb.setKind(Expression.ExpressionKind.LOGICAL_NOT);
			break;
		default:
			eb.setKind(Expression.ExpressionKind.OP_INC);
			System.err.println("===> UNKNOWN OPERATOR: " + expr.getOperationToken().toString());
			break;
		}

		if (expr instanceof KtPostfixExpression)
			eb.setIsPostfix(true);

		expressions.peek().add(eb.build());

		return null;
	}

	@Override
	public Void visitReturnExpression(final KtReturnExpression expr, final Void v) {
		final Statement.Builder sb = Statement.newBuilder();

		sb.setKind(Statement.StatementKind.RETURN);

		expressions.push(new ArrayList<Expression>());
		expr.acceptChildren(this, v);
		sb.addAllExpressions(expressions.pop());

		statements.peek().add(sb.build());

		return null;
	}

	@Override
	public Void visitExpressionWithLabel(final KtExpressionWithLabel n, final Void v) {
		// TODO
		n.acceptChildren(this, v);
		return null;
	}

	@Override
	public Void visitThrowExpression(final KtThrowExpression expr, final Void v) {
		final Statement.Builder sb = Statement.newBuilder();

		sb.setKind(Statement.StatementKind.THROW);

		expressions.push(new ArrayList<Expression>());
		expr.acceptChildren(this, v);
		sb.addAllExpressions(expressions.pop());

		statements.peek().add(sb.build());
		return null;
	}

	@Override
	public Void visitIfExpression(final KtIfExpression expr, final Void v) {
		if (expectExpression.peek()) {
			final Expression.Builder eb = Expression.newBuilder();

			eb.setKind(Expression.ExpressionKind.CONDITIONAL);

			final List<Statement> stmts = new ArrayList<Statement>();
			statements.push(stmts);

			final List<Expression> exprs = new ArrayList<Expression>();
			expressions.push(exprs);

			if (expr.getCondition() != null)
				expr.getCondition().accept(this, v);

			if (expr.getThen() != null) {
				expr.getThen().accept(this, v);
				for (final Statement s: stmts) {
					exprs.add(Expression.newBuilder()
							.setKind(Expression.ExpressionKind.OTHER)
							.addStatements(s)
							.build());
				}
				stmts.clear();
			} else {
				exprs.add(Expression.newBuilder()
						.setKind(Expression.ExpressionKind.OTHER)
						.build());
			}

			if (expr.getElse() != null) {
				expr.getElse().accept(this, v);
				for (final Statement s: stmts) {
					exprs.add(Expression.newBuilder()
							.setKind(Expression.ExpressionKind.OTHER)
							.addStatements(s)
							.build());
				}
				stmts.clear();
			}

			eb.addAllExpressions(expressions.pop());
			statements.pop();

			expressions.peek().add(eb.build());
		} else {
			final Statement.Builder sb = Statement.newBuilder();

			sb.setKind(Statement.StatementKind.IF);

			final List<Statement> stmts = new ArrayList<Statement>();
			statements.push(stmts);

			final List<Expression> exprs = new ArrayList<Expression>();
			expressions.push(exprs);

			if (expr.getCondition() != null) {
				expr.getCondition().accept(this, v);
				sb.addAllConditions(exprs);
				exprs.clear();
			}

			if (expr.getThen() != null) {
				expr.getThen().accept(this, v);
				for (final Expression e: exprs) {
					stmts.add(Statement.newBuilder()
							.setKind(Statement.StatementKind.BLOCK)
							.addExpressions(e)
							.build());
				}
				exprs.clear();
			} else {
				stmts.add(Statement.newBuilder()
						.setKind(Statement.StatementKind.BLOCK)
						.build());
			}

			if (expr.getElse() != null) {
				expr.getElse().accept(this, v);
				for (final Expression e: exprs) {
					stmts.add(Statement.newBuilder()
							.setKind(Statement.StatementKind.BLOCK)
							.addExpressions(e)
							.build());
				}
				exprs.clear();
			}

			expressions.pop();
			sb.addAllStatements(statements.pop());

			statements.peek().add(sb.build());
		}

		return null;
	}

	@Override
	public Void visitCollectionLiteralExpression(final KtCollectionLiteralExpression expr, final Void v) {
                final Expression.Builder eb = Expression.newBuilder();

		eb.setKind(Expression.ExpressionKind.ARRAYLITERAL);

		expressions.push(new ArrayList<Expression>());

		expr.acceptChildren(this, v);

		eb.addAllExpressions(expressions.pop());

		expressions.peek().add(eb.build());

		return null;
	}

	@Override
	public Void visitForExpression(final KtForExpression expr, final Void v) {
		final Statement.Builder sb = Statement.newBuilder();

		sb.setKind(Statement.StatementKind.FOREACH);

		if (expr.getDestructuringDeclaration() != null) {
			expressions.push(new ArrayList<Expression>());
			expr.getDestructuringDeclaration().accept(this, v);
			sb.addAllInitializations(expressions.pop());
		}
		else if (expr.getLoopParameter() != null) {
			fields.push(new ArrayList<Variable>());
			expr.getLoopParameter().accept(this, v);
			sb.addAllVariableDeclarations(fields.pop());
			if (expr.getLoopRange() != null) {
				expressions.push(new ArrayList<Expression>());
				expr.getLoopRange().accept(this, v);
				sb.addAllInitializations(expressions.pop());
			}
		}

		if (expr.getBody() != null) {
			final List<Statement> stmt = new ArrayList<Statement>();
			statements.push(stmt);

			final List<Expression> exprs = new ArrayList<Expression>();
			expressions.push(exprs);
			expr.getBody().accept(this, v);
			for (final Expression e: exprs) {
				stmt.add(Statement.newBuilder()
					 .setKind(Statement.StatementKind.EXPRESSION)
					 .addExpressions(e)
					 .build());
			}

			exprs.clear();

			expressions.pop();
			sb.addAllStatements(statements.pop());
		}

		statements.peek().add(sb.build());

		return null;
	}

	@Override
	public Void visitWhileExpression(final KtWhileExpression whileExpr, final Void v) {
		final Statement.Builder sb = Statement.newBuilder();

		sb.setKind(Statement.StatementKind.WHILE);

		expressions.push(new ArrayList<Expression>());
		whileExpr.getCondition().accept(this, v);
		sb.addAllConditions(expressions.pop());

		if (whileExpr.getBody() != null) {
			expressions.push(new ArrayList<Expression>());
			statements.push(new ArrayList<Statement>());
			whileExpr.getBody().accept(this, v);
			sb.addAllExpressions(expressions.pop());
			sb.addAllStatements(statements.pop());
		}

		statements.peek().add(sb.build());
		return null;
	}

	@Override
	public Void visitDoWhileExpression(final KtDoWhileExpression expr, final Void v) {
		final Statement.Builder sb = Statement.newBuilder();

		sb.setKind(Statement.StatementKind.DO);

		expressions.push(new ArrayList<Expression>());
		expr.getCondition().accept(this, v);
		sb.addAllConditions(expressions.pop());

		if (expr.getBody() != null) {
			expressions.push(new ArrayList<Expression>());
			statements.push(new ArrayList<Statement>());
			expr.getBody().accept(this, v);
			sb.addAllExpressions(expressions.pop());
			sb.addAllStatements(statements.pop());
		}

		statements.peek().add(sb.build());
		return null;
	}

	@Override
	public Void visitLambdaExpression(final KtLambdaExpression expr, final Void v) {
		final Expression.Builder eb = Expression.newBuilder();

		eb.setKind(Expression.ExpressionKind.LAMBDA);

		if (expr.hasDeclaredReturnType())
			eb.setReturnType(typeFromTypeRef(expr.getFunctionLiteral().getTypeReference()));

		fields.push(new ArrayList<Variable>());
		for (final KtParameter p : expr.getValueParameters())
			p.accept(this, v);
		eb.addAllVariableDecls(fields.pop());

		if (expr.getBodyExpression() != null) {
			expressions.push(new ArrayList<Expression>());
			expr.getBodyExpression().accept(this, v);
			eb.addStatements(Statement.newBuilder()
					.setKind(Statement.StatementKind.EXPRESSION)
					.addExpressions(expressions.pop().get(0))
					.build());
		}

		expressions.peek().add(eb.build());
		return null;
	}

	@Override
	public Void visitAnnotatedExpression(final KtAnnotatedExpression expr, final Void v) {
		// TODO
		expr.acceptChildren(this, v);
		return null;
	}

	@Override
	public Void visitArrayAccessExpression(final KtArrayAccessExpression expr, final Void v) {
		final Expression.Builder eb = Expression.newBuilder();

		eb.setKind(Expression.ExpressionKind.ARRAYACCESS);

		expressions.push(new ArrayList<Expression>());
		expr.acceptChildren(this, v);
		eb.addAllExpressions(expressions.pop());

		expressions.peek().add(eb.build());
		return null;
	}

	@Override
	public Void visitQualifiedExpression(final KtQualifiedExpression expr, final Void v) {
		// TODO
		expr.acceptChildren(this, v);
		return null;
	}

	@Override
	public Void visitDoubleColonExpression(final KtDoubleColonExpression expr, final Void v) {
		final Expression.Builder eb = Expression.newBuilder();

		System.err.println("DOUBLE COLON: " + expr.getText());
		eb.setKind(Expression.ExpressionKind.METHOD_REFERENCE);

		if (expr.getReceiverExpression() != null) {
			expressions.push(new ArrayList<Expression>());
			expr.getReceiverExpression().accept(this, v);
			eb.addAllExpressions(expressions.pop());
		}
		eb.setMethod(expr.getText());

		expressions.peek().add(eb.build());
		return null;
	}

	@Override
	public Void visitCallableReferenceExpression(final KtCallableReferenceExpression expr, final Void v) {
		System.err.println("CALLABLE REF EXP: " + expr.getText());
		final Expression.Builder eb = Expression.newBuilder();

		eb.setKind(Expression.ExpressionKind.METHOD_REFERENCE);

		if (expr.getReceiverExpression() != null) {
			expressions.push(new ArrayList<Expression>());
			expr.getReceiverExpression().accept(this, v);
			eb.addAllExpressions(expressions.pop());
		}
		eb.setMethod(expr.getCallableReference().getText());

		expressions.peek().add(eb.build());
		return null;
	}

	@Override
	public Void visitDotQualifiedExpression(final KtDotQualifiedExpression expr, final Void v) {
		final KtExpression rcvr = expr.getReceiverExpression();
		final KtExpression sel = expr.getSelectorExpression();

		if (sel instanceof KtCallExpression) {
			final KtCallExpression call = (KtCallExpression)sel;
			final Expression.Builder eb = Expression.newBuilder();

			eb.setKind(Expression.ExpressionKind.METHODCALL);

			expressions.push(new ArrayList<Expression>());
			rcvr.accept(this, v);
			eb.addAllExpressions(expressions.pop());

			eb.setMethod(call.getCalleeExpression().getText());

			if (call.getValueArgumentList() != null) {
				expressions.push(new ArrayList<Expression>());
				for (final KtValueArgument arg : call.getValueArgumentList().getArguments())
					arg.getArgumentExpression().accept(this, v);
				eb.addAllMethodArgs(expressions.pop());
			}

			expressions.peek().add(eb.build());
		} else {
			final Expression.Builder eb = Expression.newBuilder();

			eb.setKind(Expression.ExpressionKind.VARACCESS);
			eb.setVariable(expr.getText());

			expressions.peek().add(eb.build());
		}

		return null;
	}

	@Override
	public Void visitSafeQualifiedExpression(final KtSafeQualifiedExpression expr, final Void v) {
		// TODO
		expr.acceptChildren(this, v);
		return null;
	}

	@Override
	public Void visitObjectLiteralExpression(final KtObjectLiteralExpression expr, final Void v) {
		// TODO
		expr.acceptChildren(this, v);
		return null;
	}

	@Override
	public Void visitBlockExpression(final KtBlockExpression expr, final Void v) {
		final Statement.Builder sb = Statement.newBuilder();
		sb.setKind(Statement.StatementKind.BLOCK);

		final List<Statement> stmts = new ArrayList<Statement>();
		statements.push(stmts);

		final List<Expression> exprs = new ArrayList<Expression>();
		expressions.push(exprs);

		for (final KtExpression e : expr.getStatements()) {
			e.accept(this, v);
			for (final Expression ex : exprs)
				stmts.add(Statement.newBuilder()
						.setKind(Statement.StatementKind.EXPRESSION)
						.addExpressions(ex)
						.build());
			exprs.clear();
		}

		sb.addAllStatements(statements.pop());
		expressions.pop();

		statements.peek().add(sb.build());
		return null;
	}

	@Override
	public Void visitTryExpression(final KtTryExpression expr, final Void v) {
		final Statement.Builder sb = Statement.newBuilder();

		sb.setKind(Statement.StatementKind.TRY);

		statements.push(new ArrayList<Statement>());
		expr.acceptChildren(this, v);
		sb.addAllStatements(statements.pop());

		statements.peek().add(sb.build());
		return null;
	}

	@Override
	public Void visitCatchSection(final KtCatchClause n, final Void v) {
		final Statement.Builder sb = Statement.newBuilder();

		sb.setKind(Statement.StatementKind.CATCH);

		if (n.getParameterList() != null) {
			fields.push(new ArrayList<Variable>());
			n.getParameterList().accept(this, v);
			sb.addAllVariableDeclarations(fields.pop());
		}

		if (n.getCatchBody() != null) {
			final List<Statement> stmts = new ArrayList<Statement>();
			statements.push(stmts);

			final List<Expression> exprs = new ArrayList<Expression>();
			expressions.push(exprs);

			final KtExpression body = n.getCatchBody();

			if (body instanceof KtBlockExpression) {
				for (final KtExpression e: ((KtBlockExpression) body).getStatements()) {
					e.accept(this, v);
					for (final Expression ex: exprs)
						stmts.add(Statement.newBuilder()
								.setKind(Statement.StatementKind.EXPRESSION)
								.addExpressions(ex)
								.build());
					exprs.clear();
				}
			} else {
				body.accept(this, v);
				for (final Expression ex: exprs)
					stmts.add(Statement.newBuilder()
							.setKind(Statement.StatementKind.EXPRESSION)
							.addExpressions(ex)
							.build());
				exprs.clear();
			}

			sb.addAllStatements(statements.pop());
			expressions.pop();
		}

		statements.peek().add(sb.build());

		return null;
	}

	@Override
	public Void visitFinallySection(final KtFinallySection n, final Void v) {
		final Statement.Builder sb = Statement.newBuilder();

		sb.setKind(Statement.StatementKind.FINALLY);

		if (n.getFinalExpression() != null) {
			final List<Statement> stmts = new ArrayList<Statement>();
			statements.push(stmts);

			final List<Expression> exprs = new ArrayList<Expression>();
			expressions.push(exprs);

			for (final KtExpression e: n.getFinalExpression().getStatements()) {
				e.accept(this, v);
				for (final Expression ex: exprs)
					stmts.add(Statement.newBuilder()
							.setKind(Statement.StatementKind.EXPRESSION)
							.addExpressions(ex)
							.build());
				exprs.clear();
			}

			exprs.clear();

			sb.addAllStatements(statements.pop());
			expressions.pop();
		}

		statements.peek().add(sb.build());

		return null;
	}

	@Override
	public Void visitTypeArgumentList(final KtTypeArgumentList n, final Void v) {
		// TODO
		n.acceptChildren(this, v);
		return null;
	}

	@Override
	public Void visitThisExpression(final KtThisExpression expr, final Void v) {
		expressions.peek().add(Expression.newBuilder()
					 .setKind(Expression.ExpressionKind.VARACCESS)
					 .setVariable(expr.getText())
					 .build());
		return null;
	}

	@Override
	public Void visitSuperExpression(final KtSuperExpression expr, final Void v) {
		expressions.peek().add(Expression.newBuilder()
					 .setKind(Expression.ExpressionKind.VARACCESS)
					 .setVariable(expr.getText())
					 .build());
		return null;
	}

	@Override
	public Void visitInitializerList(final KtInitializerList n, final Void v) {
		// TODO
		n.acceptChildren(this, v);
		return null;
	}

	@Override
	public Void visitAnonymousInitializer(final KtAnonymousInitializer n, final Void v) {
		// TODO
		n.acceptChildren(this, v);
		return null;
	}

	@Override
	public Void visitScriptInitializer(final KtScriptInitializer n, final Void v) {
		// TODO
		n.acceptChildren(this, v);
		return null;
	}

	@Override
	public Void visitClassInitializer(final KtClassInitializer n, final Void v) {
		final Method.Builder mb = Method.newBuilder();

		mb.setName("<clinit>");

		final List<Statement> stmts = new ArrayList<Statement>();
		statements.push(stmts);

		final List<Expression> exprs = new ArrayList<Expression>();
		expressions.push(exprs);

		expectExpression.push(true);

		if (n.getBody() != null) {
                        n.getBody().accept(this, v);
			for (Expression e: exprs) {
				stmts.add(Statement.newBuilder()
					  .setKind(Statement.StatementKind.EXPRESSION)
					  .addExpressions(e)
					  .build());
			}
			exprs.clear();
		}

		expectExpression.push(true);

		expressions.pop();
		mb.addAllStatements(statements.pop());

                methods.peek().add(mb.build());
		return null;
	}

	@Override
	public Void visitTypeConstraintList(final KtTypeConstraintList n, final Void v) {
		// TODO
		n.acceptChildren(this, v);
		return null;
	}

	@Override
	public Void visitTypeConstraint(final KtTypeConstraint n, final Void v) {
		// TODO
		n.acceptChildren(this, v);
		return null;
	}

	@Override
	public Void visitUserType(final KtUserType n, final Void v) {
		// TODO
		n.acceptChildren(this, v);
		return null;
	}

	@Override
	public Void visitDynamicType(final KtDynamicType n, final Void v) {
		// TODO
		n.acceptChildren(this, v);
		return null;
	}

	@Override
	public Void visitFunctionType(final KtFunctionType n, final Void v) {
		// TODO
		n.acceptChildren(this, v);
		return null;
	}

	@Override
	public Void visitSelfType(final KtSelfType n, final Void v) {
		// TODO
		n.acceptChildren(this, v);
		return null;
	}

	@Override
	public Void visitBinaryWithTypeRHSExpression(final KtBinaryExpressionWithTypeRHS n, final Void v) {
		// TODO
		n.acceptChildren(this, v);
		return null;
	}

	@Override
	public Void visitNamedDeclaration(final KtNamedDeclaration d, final Void v) {
		// TODO
		d.acceptChildren(this, v);
		return null;
	}

	@Override
	public Void visitNullableType(final KtNullableType n, final Void v) {
		// TODO
		n.acceptChildren(this, v);
		return null;
	}

	@Override
	public Void visitTypeProjection(final KtTypeProjection n, final Void v) {
		// TODO
		n.acceptChildren(this, v);
		return null;
	}

	@Override
	public Void visitWhenExpression(final KtWhenExpression expr, final Void v) {
		final Statement.Builder sb = Statement.newBuilder();

		sb.setKind(Statement.StatementKind.SWITCH);

		statements.push(new ArrayList<Statement>());

		final List<Expression> exprs = new ArrayList<Expression>();
		expressions.push(exprs);

		if (expr.getSubjectVariable() != null) {
			fields.push(new ArrayList<Variable>());
			expr.getSubjectVariable().accept(this, v);
			sb.addAllVariableDeclarations(fields.pop());
		} else if (expr.getSubjectExpression() != null) {
			expr.getSubjectExpression().accept(this, v);
			sb.addAllExpressions(exprs);
			exprs.clear();
		}

		for (final KtWhenEntry entry: expr.getEntries())
			entry.accept(this, v);
		sb.addAllStatements(statements.pop());
		expressions.pop();

		statements.peek().add(sb.build());
		return null;
	}

	@Override
	public Void visitWhenEntry(final KtWhenEntry n, final Void v) {
		final Statement.Builder sb = Statement.newBuilder();

		final List<Statement> stmts = new ArrayList<Statement>();
		statements.push(stmts);

		final List<Expression> exprs = new ArrayList<Expression>();
		expressions.push(exprs);

		if (n.isElse()) {
			sb.setKind(Statement.StatementKind.DEFAULT);
		} else {
			sb.setKind(Statement.StatementKind.CASE);
			if (n.getConditions().length != 0) {
				for (final KtWhenCondition cond : n.getConditions())
					cond.accept(this, v);
				sb.addAllExpressions(exprs);
				exprs.clear();
			}
		}

		if (n.getExpression() != null) {
			n.getExpression().accept(this, v);
			for (final Expression e : exprs) {
				stmts.add(Statement.newBuilder()
						.setKind(Statement.StatementKind.EXPRESSION)
						.addExpressions(e)
						.build());
			}
			exprs.clear();
		}

		statements.pop();
		expressions.pop();
		statements.peek().add(sb.build());

		for (final Statement s: stmts)
			statements.peek().add(s);

		return null;
	}

	@Override
	public Void visitIsExpression(final KtIsExpression expr, final Void v) {
		Expression.Builder eb = Expression.newBuilder();

		eb.setKind(Expression.ExpressionKind.TYPECOMPARE);

		expressions.push(new ArrayList<Expression>());
		expr.getLeftHandSide().accept(this, v);
		eb.addAllExpressions(expressions.pop());

		eb.setNewType(typeFromTypeRef(expr.getTypeReference()));

		if (expr.isNegated()) {
			final Expression isExpr = eb.build();

			eb = Expression.newBuilder();
			eb.setKind(Expression.ExpressionKind.LOGICAL_NOT);
			eb.addExpressions(isExpr);
		}

		expressions.peek().add(eb.build());
		return null;
	}

	@Override
	public Void visitWhenConditionIsPattern(final KtWhenConditionIsPattern n, final Void v) {
		Expression.Builder eb = Expression.newBuilder();

		eb.setKind(Expression.ExpressionKind.TYPECOMPARE);
		eb.setNewType(typeFromTypeRef(n.getTypeReference()));

		if (n.isNegated()) {
			final Expression isExpr = eb.build();

			eb = Expression.newBuilder();
			eb.setKind(Expression.ExpressionKind.LOGICAL_NOT);
			eb.addExpressions(isExpr);
		}

		expressions.peek().add(eb.build());
		return null;
	}

	@Override
	public Void visitWhenConditionInRange(final KtWhenConditionInRange n, final Void v) {
		Expression.Builder eb = Expression.newBuilder();

		eb.setKind(Expression.ExpressionKind.IN);

		expressions.push(new ArrayList<Expression>());
		n.getRangeExpression().accept(this, v);
		eb.addAllExpressions(expressions.pop());

		if (n.isNegated()) {
			final Expression expr = eb.build();

			eb = Expression.newBuilder();
			eb.setKind(Expression.ExpressionKind.LOGICAL_NOT);
			eb.addExpressions(expr);
		}

		expressions.peek().add(eb.build());
		return null;
	}

	@Override
	public Void visitWhenConditionWithExpression(final KtWhenConditionWithExpression expr, final Void v) {
		expr.getExpression().accept(this, v);
		return null;
	}

	@Override
	public Void visitStringTemplateExpression(final KtStringTemplateExpression expr, final Void v) {
		final KtStringTemplateEntry[] entries = expr.getEntries();

		if (entries.length == 1 && entries[0] instanceof KtLiteralStringTemplateEntry) {
			expressions.peek().add(Expression.newBuilder()
					.setKind(Expression.ExpressionKind.LITERAL)
					.setLiteral("\"" + entries[0].getText() + "\"")
					.build());
		} else {
			final List<Expression> exprs = new ArrayList<Expression>();
			expressions.push(exprs);

			final StringBuilder sb = new StringBuilder();
			sb.append("\"");
			for (final KtStringTemplateEntry e : entries) {
				e.accept(this, v);
				sb.append(e.getText());
			}
			sb.append("\"");
			expressions.pop();

			expressions.peek().add(Expression.newBuilder()
					.setKind(Expression.ExpressionKind.TEMPLATE)
					// add the whole thing as a single string literal
					.setLiteral(sb.toString())
					// then add each individual part
					.addAllExpressions(exprs)
					.build());
		}

		return null;
	}

	@Override
	public Void visitStringTemplateEntry(final KtStringTemplateEntry st, final Void v) {
		expressions.peek().add(Expression.newBuilder()
				.setKind(Expression.ExpressionKind.TEMPLATE)
				.setLiteral("\"" + st.getText() + "\"")
				.build());
		return null;
	}

	@Override
	public Void visitStringTemplateEntryWithExpression(final KtStringTemplateEntryWithExpression st, final Void v) {
		final List<Expression> exprs = new ArrayList<Expression>();

		expressions.push(exprs);
		st.getExpression().accept(this, v);
		expressions.pop();

		expressions.peek().add(Expression.newBuilder()
				.setKind(Expression.ExpressionKind.TEMPLATE)
				.setLiteral("\"" + st.getText() + "\"")
				.addAllExpressions(exprs)
				.build());
		return null;
	}

	@Override
	public Void visitBreakExpression(final KtBreakExpression expr, final Void v) {
		final Statement.Builder b = Statement.newBuilder();

		b.setKind(Statement.StatementKind.BREAK);

		final String label = expr.getLabelName();
		if (label != null)
			b.addExpressions(Expression.newBuilder()
					.setLiteral(label)
					.setKind(Expression.ExpressionKind.LITERAL)
					.build());

		statements.peek().add(b.build());
		return null;
	}

	@Override
	public Void visitContinueExpression(final KtContinueExpression expr, final Void v) {
		final Statement.Builder b = Statement.newBuilder();

		b.setKind(Statement.StatementKind.CONTINUE);

		final String label = expr.getLabelName();
		if (label != null)
			b.addExpressions(Expression.newBuilder()
					.setLiteral(label)
					.setKind(Expression.ExpressionKind.LITERAL)
					.build());

		statements.peek().add(b.build());
		return null;
	}

	@Override
	public Void visitConstantExpression(final KtConstantExpression expr, final Void v) {
		expressions.peek().add(Expression.newBuilder()
				.setKind(Expression.ExpressionKind.LITERAL)
				.setLiteral(expr.getText())
				.build());
		return null;
	}

	@Override
	public Void visitBinaryExpression(final KtBinaryExpression expr, final Void v) {
		final Expression.Builder eb = Expression.newBuilder();

		switch (expr.getOperationToken().toString()) {
		// arithmetic expressions
		case "PLUS":
			eb.setKind(Expression.ExpressionKind.OP_ADD);
			break;
		case "MUL":
			eb.setKind(Expression.ExpressionKind.OP_MULT);
			break;
		case "MINUS":
		case "SUB":
			eb.setKind(Expression.ExpressionKind.OP_SUB);
			break;
		case "DIV":
			eb.setKind(Expression.ExpressionKind.OP_DIV);
			break;
		case "PERC":
		case "MOD":
			eb.setKind(Expression.ExpressionKind.OP_MOD);
			break;

		// Comparisons
		case "LTEQ":
			eb.setKind(Expression.ExpressionKind.LTEQ);
			break;
		case "GTEQ":
			eb.setKind(Expression.ExpressionKind.GTEQ);
			break;
		case "GT":
			eb.setKind(Expression.ExpressionKind.GT);
			break;
		case "LT":
			eb.setKind(Expression.ExpressionKind.LT);
			break;
		case "EQEQ":
			eb.setKind(Expression.ExpressionKind.EQ);
			break;
		case "EXCLEQ":
			eb.setKind(Expression.ExpressionKind.NEQ);
			break;

		// Logical operators
		case "ANDAND":
			eb.setKind(Expression.ExpressionKind.LOGICAL_AND);
			break;
		case "OROR":
			eb.setKind(Expression.ExpressionKind.LOGICAL_OR);
			break;

		// Sets
		case "in":
			eb.setKind(Expression.ExpressionKind.IN);
			break;
		case "RANGE":
			eb.setKind(Expression.ExpressionKind.ARRAY_COMPREHENSION);
			break;

		// Assignment & assignment-like
		case "EQ":
			eb.setKind(Expression.ExpressionKind.ASSIGN);
			break;
		case "PLUSEQ":
			eb.setKind(Expression.ExpressionKind.ASSIGN_ADD);
			break;
		case "MINUSEQ":
			eb.setKind(Expression.ExpressionKind.ASSIGN_SUB);
			break;
		case "MULTEQ":
			eb.setKind(Expression.ExpressionKind.ASSIGN_MULT);
			break;
		case "DIVEQ":
			eb.setKind(Expression.ExpressionKind.ASSIGN_DIV);
			break;
		case "PERCEQ":
			eb.setKind(Expression.ExpressionKind.ASSIGN_MOD);
			break;

		// When we use an identifier for the operation
		case "IDENTIFIER":
			switch (expr.getOperationReference().getText()) {
			case "and":
				eb.setKind(Expression.ExpressionKind.BIT_AND);
				break;
			case "or":
				eb.setKind(Expression.ExpressionKind.BIT_OR);
				break;
			case "shl":
				eb.setKind(Expression.ExpressionKind.BIT_LSHIFT);
				break;
			case "shr":
				eb.setKind(Expression.ExpressionKind.BIT_RSHIFT);
				break;
			case "xor":
				eb.setKind(Expression.ExpressionKind.BIT_XOR);
				break;
			case "ushr":
				eb.setKind(Expression.ExpressionKind.BIT_UNSIGNEDRSHIFT);
				break;
			default:
				eb.setKind(Expression.ExpressionKind.OTHER);
				eb.setLiteral(expr.getOperationReference().getText());
				System.err.println("===> UNKNOWN OPERATOR ID: " + expr.getOperationReference().getText());
				break;
			}
			break;

		default:
			eb.setKind(Expression.ExpressionKind.OTHER);
			eb.setLiteral(expr.getOperationToken().toString());
			System.err.println("===> UNKNOWN OPERATOR: " + expr.getOperationToken().toString());
			break;
		}

		expressions.push(new ArrayList<Expression>());
		expr.getLeft().accept(this, v);
		expr.getRight().accept(this, v);
		eb.addAllExpressions(expressions.pop());

		expressions.peek().add(eb.build());
		return null;
	}

	@Override
	public Void visitParenthesizedExpression(final KtParenthesizedExpression expr, final Void v) {
		final Expression.Builder eb = Expression.newBuilder();

		eb.setKind(Expression.ExpressionKind.PAREN);

		expressions.push(new ArrayList<Expression>());
		expr.acceptChildren(this, v);
		eb.addAllExpressions(expressions.pop());

		expressions.peek().add(eb.build());
		return null;
	}

	@Override
	public Void visitPropertyAccessor(final KtPropertyAccessor acc, final Void v) {
		final Method.Builder mb = Method.newBuilder();
		final KtProperty prop = acc.getProperty();

		modifiers.push(new ArrayList<Modifier>());
		if (acc.getModifierList() != null)
			acc.getModifierList().accept(this, v);
		mb.addAllModifiers(modifiers.pop());

		final String propName;
		if (prop.getReceiverTypeReference() != null)
			propName = prop.getReceiverTypeReference().getText() + "." + prop.getNameIdentifier().getText();
		else
			propName = prop.getNameIdentifier().getText();
		mb.setName(propName + "." + (acc.isGetter() ? "<get>" : "<set>"));

		fields.push(new ArrayList<Variable>());
		for (final KtParameter p : acc.getValueParameters())
			p.accept(this, v);
		mb.addAllArguments(fields.pop());

		if (acc.getReturnTypeReference() != null) {
			mb.setReturnType(typeFromTypeRef(acc.getReturnTypeReference()));
		} else {
			if (acc.isSetter()) {
				mb.setReturnType(Type.newBuilder()
						.setName("Unit")
						.setKind(TypeKind.OTHER)
						.build());
			} else {
				final KtTypeReference typeRef = prop.getTypeReference();
				if (typeRef != null)
					mb.setReturnType(typeFromTypeRef(typeRef));
			}
		}

		if (acc.getBodyBlockExpression() != null) {
			statements.push(new ArrayList<Statement>());
			acc.getBodyBlockExpression().accept(this, v);
			mb.addAllStatements(statements.pop());
		} else if (acc.getBodyExpression() != null) {
			expressions.push(new ArrayList<Expression>());
			acc.getBodyExpression().accept(this, v);
			mb.addStatements(Statement.newBuilder()
					.setKind(Statement.StatementKind.EXPRESSION)
					.addExpressions(expressions.pop().get(0))
					.build());
		}

		methods.peek().add(mb.build());
		return null;
	}

	@Override
	public Void visitProperty(final KtProperty prop, final Void v) {
		final Variable.Builder vb = Variable.newBuilder();

		expectExpression.push(true);

		if (prop.getReceiverTypeReference() != null)
			vb.setName(prop.getReceiverTypeReference().getText() + "." + prop.getNameIdentifier().getText());
		else
			vb.setName(prop.getNameIdentifier().getText());

		final KtTypeReference typeRef = prop.getTypeReference();
		if (typeRef != null)
			vb.setVariableType(typeFromTypeRef(typeRef));

		modifiers.push(new ArrayList<Modifier>());
		if (!prop.isVar())
			modifiers.peek().add(Modifier.newBuilder()
					.setKind(Modifier.ModifierKind.FINAL)
					.build());
		if (prop.getModifierList() != null)
			prop.getModifierList().accept(this, v);
		vb.addAllModifiers(modifiers.pop());

		expressions.push(new ArrayList<Expression>());
		if (prop.hasInitializer())
			prop.getInitializer().accept(this, v);
		vb.addAllExpressions(expressions.pop());

		if (prop.getGetter() != null)
			prop.getGetter().accept(this, v);
		if (prop.getSetter() != null)
			prop.getSetter().accept(this, v);

		expectExpression.pop();

		if (expectExpression.peek())
			expressions.peek().add(Expression.newBuilder()
					       .setKind(Expression.ExpressionKind.VARDECL)
					       .addVariableDecls(vb.build())
					       .build());
		else
			fields.peek().add(vb.build());

		return null;
	}

	@Override
	public Void visitClass(final KtClass klass, final Void v) {
		final Declaration.Builder db = Declaration.newBuilder();

		db.setName(klass.getNameAsSafeName().asString());

		if (klass.isInterface())
			db.setKind(TypeKind.INTERFACE);
		else if (klass.isEnum())
			db.setKind(TypeKind.ENUM);
		else if (klass.isData())
			db.setKind(TypeKind.IMMUTABLE);
		else
			db.setKind(TypeKind.CLASS);

		modifiers.push(new ArrayList<Modifier>());
		fields.push(new ArrayList<Variable>());
		declarations.push(new ArrayList<Declaration>());
		methods.push(new ArrayList<Method>());

		klass.acceptChildren(this, v);

		db.addAllNestedDeclarations(declarations.pop());
		db.addAllModifiers(modifiers.pop());
		db.addAllMethods(methods.pop());
		db.addAllFields(fields.pop());

		declarations.peek().add(db.build());
		return null;
	}

	private <T extends KtConstructor<T>> void visitConstructor(final KtConstructor<T> constructor, final boolean isPrimary) {
		final Method.Builder mb = Method.newBuilder();

		mb.setName("<init>");

		modifiers.push(new ArrayList<Modifier>());
		if (isPrimary)
			modifiers.peek().add(Modifier.newBuilder()
						.setKind(Modifier.ModifierKind.OTHER)
						.setOther("primary")
						.build());
		fields.push(new ArrayList<Variable>());
		statements.push(new ArrayList<Statement>());

		constructor.acceptChildren(this, null);

		mb.addAllStatements(statements.pop());
		final List<Variable> methodFields = fields.pop();
		mb.addAllArguments(methodFields);
		mb.addAllModifiers(modifiers.pop());

		if (isPrimary)
			for (final Variable var : methodFields)
				fields.peek().add(var);

		methods.peek().add(mb.build());
	}

	@Override
	public Void visitPrimaryConstructor(final KtPrimaryConstructor constructor, final Void v) {
		visitConstructor(constructor, true);
		return null;
	}

	@Override
	public Void visitSecondaryConstructor(final KtSecondaryConstructor constructor, final Void v) {
		visitConstructor(constructor, false);
		return null;
	}

	@Override
	public Void visitNamedFunction(final KtNamedFunction function, final Void v) {
		final Method.Builder mb = Method.newBuilder();

		if (function.getName() != null)
			mb.setName(function.getName());
		else
			mb.setName("");

		if (function.getModifierList() != null) {
			modifiers.push(new ArrayList<Modifier>());
			function.getModifierList().accept(this, v);
			mb.addAllModifiers(modifiers.pop());
		}

		if (function.hasDeclaredReturnType())
			mb.setReturnType(typeFromTypeRef(function.getTypeReference()));

		fields.push(new ArrayList<Variable>());
		for (final KtParameter p : function.getValueParameters())
			p.accept(this, v);
		mb.addAllArguments(fields.pop());

                expectExpression.push(true);
		if (function.getBodyBlockExpression() != null) {
			statements.push(new ArrayList<Statement>());
			function.getBodyBlockExpression().accept(this, v);
			mb.addAllStatements(statements.pop());
		} else if (function.getBodyExpression() != null) {
			expressions.push(new ArrayList<Expression>());
			function.getBodyExpression().accept(this, v);
			mb.addStatements(Statement.newBuilder()
					.setKind(Statement.StatementKind.EXPRESSION)
					.addExpressions(expressions.pop().get(0))
					.build());
		}
		expectExpression.pop();

		methods.peek().add(mb.build());
		return null;
	}

	@Override
	public Void visitParameter(final KtParameter param, final Void v) {
		final Variable.Builder vb = Variable.newBuilder();

		expectExpression.push(true);

		vb.setName(param.getName());

		if (param.getTypeReference() != null)
			vb.setVariableType(typeFromTypeRef(param.getTypeReference()));

		if (param.getModifierList() != null) {
			modifiers.push(new ArrayList<Modifier>());
			param.getModifierList().accept(this, v);
			vb.addAllModifiers(modifiers.pop());
		}

		if (param.hasDefaultValue()) {
			List<Expression> exprs = new ArrayList<Expression>();
			expressions.push(exprs);
			statements.push(new ArrayList<Statement>());
			param.getDefaultValue().accept(this, v);
			if (exprs.size() > 0)
				vb.setInitializer(exprs.get(0));
			statements.pop();
			expressions.pop();
		}

		expectExpression.pop();

		fields.peek().add(vb.build());
		return null;
	}

	@Override
	public Void visitCallExpression(final KtCallExpression args, final Void v) {
		// FIXME this visit can maybe go away?
		final Expression.Builder eb = Expression.newBuilder();

		eb.setKind(Expression.ExpressionKind.METHODCALL);

		eb.setMethod(args.getCalleeExpression().getText());

		if (args.getValueArgumentList() != null) {
			expressions.push(new ArrayList<Expression>());
			for (final KtValueArgument arg : args.getValueArgumentList().getArguments())
				arg.getArgumentExpression().accept(this, v);
			eb.addAllMethodArgs(expressions.pop());
		}

		expressions.peek().add(eb.build());
		return null;
	}

	@Override
	public Void visitValueArgumentList(final KtValueArgumentList args, final Void v) {
		// TODO
		args.acceptChildren(this, v);
		return null;
	}

	@Override
	public void visitWhiteSpace(final PsiWhiteSpace space) {
		// ignore
	}

	// Utility methods
	private Type typeFromTypeRef(final KtTypeReference type) {
		final Type.Builder tb = Type.newBuilder();
		tb.setName(type.getText());
		tb.setKind(TypeKind.OTHER);
		return tb.build();
	}
}
