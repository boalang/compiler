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
	protected Stack<List<Type>> types = new Stack<List<Type>>();

	protected Stack<Boolean> expectExpression = new Stack<Boolean>();

	protected List<Expression> superClassInitExprs = new ArrayList<Expression>();

	public static final int KLS10 = 10;
	public static final int KLS11 = 11;
	public static final int KLS12 = 12;
	public static final int KLS13 = 13;
	public static final int KLS14 = 14;
	public static final int KLS15 = 15;
	protected int astLevel = KLS10;

	protected void reset() {
		b = Namespace.newBuilder();
		b.setName("");

		declarations.clear();
		modifiers.clear();
		expressions.clear();
		fields.clear();
		methods.clear();
		statements.clear();
		types.clear();

		expectExpression.clear();

		superClassInitExprs.clear();

		astLevel = KLS10;
	}

	public int getAstLevel() {
		return astLevel;
	}

	public Namespace getNamespace(final KtFile kt) {
		reset();
		if (kt != null)
			kt.accept(this);

		return b.build();
	}

	@Override
	public void visitElement(final PsiElement element) {
		if (element instanceof LeafPsiElement && ((LeafPsiElement)element).getElementType() instanceof KtModifierKeywordToken)
			visitModifier((KtModifierKeywordToken)((LeafPsiElement)element).getElementType());
		else
			element.acceptChildren(this);
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

		case "actual":
		case "impl":
		case "expect":
		case "header":
		case "fun":
		case "reified":
		case "open":
		case "value":
		case "inner":
		case "sealed":
		case "override":
		case "inline":
		case "noinline":
		case "operator":
		case "vararg":
		case "tailrec":
		case "suspend":
		case "in":
		case "out":
		case "external":
		case "infix":
		case "crossinline":
		case "lateinit":
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
	public Void visitScript(final KtScript s, final Void v) {
		modifiers.push(new ArrayList<Modifier>());
		declarations.push(new ArrayList<Declaration>());
		fields.push(new ArrayList<Variable>());
		methods.push(new ArrayList<Method>());
		statements.push(new ArrayList<Statement>());
		expectExpression.push(false);

		s.acceptChildren(this);

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
			entry.getValueArgumentList().accept(this, v);
			mb.addAllAnnotationValues(expressions.pop());
		}

		modifiers.peek().add(mb.build());
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

		for (final KtTypeParameter type_param : d.getTypeParameters())
			db.addGenericParameters(typeFromTypeParameter(type_param, TypeKind.GENERIC));

		expectExpression.push(false);

		modifiers.push(new ArrayList<Modifier>());
		fields.push(new ArrayList<Variable>());
		methods.push(new ArrayList<Method>());
		declarations.push(new ArrayList<Declaration>());
		types.push(new ArrayList<Type>());

		if (d.isCompanion())
			db.addModifiers(Modifier.newBuilder()
					.setKind(Modifier.ModifierKind.OTHER)
					.setOther("COMPANION")
					.build());

		d.acceptChildren(this, v);

		db.addAllParents(types.pop());
		db.addAllNestedDeclarations(declarations.pop());
		db.addAllMethods(methods.pop());
		db.addAllFields(fields.pop());
		db.addAllModifiers(modifiers.pop());

		expectExpression.pop();

		declarations.peek().add(db.build());
		return null;
	}

	@Override
	public Void visitDestructuringDeclaration(final KtDestructuringDeclaration d, final Void v) {
		if (expectExpression.peek()) {
			fields.push(new ArrayList<Variable>());
			expressions.push(new ArrayList<Expression>());
		}

		for (final KtDestructuringDeclarationEntry entry : d.getEntries())
			entry.accept(this, v);

		if (d.hasInitializer()) {
			expectExpression.push(true);
			d.getInitializer().accept(this, v);
			expectExpression.pop();
		}

		if (expectExpression.peek()) {
			final Expression.Builder eb = Expression.newBuilder();

			eb.setKind(Expression.ExpressionKind.VARDECL);

			eb.addAllExpressions(expressions.pop());
			eb.addAllVariableDecls(fields.pop());

			expressions.peek().add(eb.build());
		}

		return null;
	}

	@Override
	public Void visitDestructuringDeclarationEntry(final KtDestructuringDeclarationEntry n, final Void v) {
		final Variable.Builder vb = Variable.newBuilder();

		vb.setName(n.getName());

		if (n.getTypeReference() != null)
			vb.setVariableType(typeFromTypeRef(n.getTypeReference()));

		if (n.getValOrVarKeyword() == null)
			vb.addModifiers(Modifier.newBuilder()
					.setKind(Modifier.ModifierKind.IMPLICIT)
					.build());
		if (!n.isVar())
			vb.addModifiers(Modifier.newBuilder()
					.setKind(Modifier.ModifierKind.FINAL)
					.build());

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

		types.push(new ArrayList<Type>());
		for (final KtTypeParameter param : ta.getTypeParameters())
			types.peek().add(typeFromTypeParameter(param));

		sb.setTypeDeclaration(Declaration.newBuilder()
				.setKind(TypeKind.ALIAS)
				.setName(ta.getName())
				.addParents(typeFromTypeRef(ta.getTypeReference()))
				.addAllModifiers(modifiers.pop())
				.addAllGenericParameters(types.pop())
				.build());

		// TODO type params
		ta.getTypeConstraints();

		statements.peek().add(sb.build());
		return null;
	}

	@Override
	public Void visitConstructorCalleeExpression(final KtConstructorCalleeExpression expr, final Void v) {
		// FIXME remove?
		System.err.println(expr.getClass());
		// expr.acceptChildren(this, v);
		return null;
	}

	@Override
	public Void visitEnumEntry(final KtEnumEntry n, final Void v) {
		final Variable.Builder vb = Variable.newBuilder();
		vb.setName(n.getNameAsSafeName().asString());
		vb.addModifiers(Modifier.newBuilder()
				.setKind(Modifier.ModifierKind.IMPLICIT)
				.build());

		if (n.getModifierList() != null) {
			modifiers.push(new ArrayList<Modifier>());
			n.getModifierList().accept(this, v);
			vb.addAllModifiers(modifiers.pop());
		}

		if (n.hasInitializer()) {
			if (n.getInitializerList() != null) {
				for (final KtSuperTypeListEntry st : n.getInitializerList().getInitializers()) {
					types.push(new ArrayList<Type>());
					st.accept(this, v);
					vb.addExpressions(Expression.newBuilder()
							.setKind(Expression.ExpressionKind.NEW)
							.setNewType(types.pop().get(0))
							.addAllExpressions(superClassInitExprs));
					superClassInitExprs.clear();
				}
			}
		}

		if (n.getBody() != null) {
			final Declaration.Builder db = Declaration.newBuilder();

			db.setName("");
			db.setKind(TypeKind.OTHER);

			fields.push(new ArrayList<Variable>());
			methods.push(new ArrayList<Method>());
			modifiers.push(new ArrayList<Modifier>());
			declarations.push(new ArrayList<Declaration>());

			n.getBody().accept(this, v);

			db.addAllNestedDeclarations(declarations.pop());
			db.addAllModifiers(modifiers.pop());
			db.addAllMethods(methods.pop());
			db.addAllFields(fields.pop());

			vb.setInitializer(Expression.newBuilder()
					.setKind(Expression.ExpressionKind.NEW)
					.setAnonDeclaration(db.build())
					.build());
		}

		fields.peek().add(vb.build());
		return null;
	}

	@Override
	public Void visitSuperTypeListEntry(final KtSuperTypeListEntry n, final Void v) {
		// FIXME remove?
		System.err.println(n.getClass());
		// n.acceptChildren(this, v);
		return null;
	}

	@Override
	public Void visitDelegatedSuperTypeEntry(final KtDelegatedSuperTypeEntry n, final Void v) {
		final Type.Builder tb = Type.newBuilder();

		tb.setKind(TypeKind.DELEGATED);

		tb.setName(n.getTypeReference().getText());

		if (n.getDelegateExpression() != null) {
			expressions.push(new ArrayList<Expression>());
			n.getDelegateExpression().accept(this, v);
			tb.setDelegate(expressions.pop().get(0));
		}

		types.peek().add(tb.build());

		return null;
	}

	@Override
	public Void visitSuperTypeCallEntry(final KtSuperTypeCallEntry n, final Void v) {
		if (n.getTypeReference() == null)
			return null;

		types.peek().add(typeFromTypeRef(n.getTypeReference(), TypeKind.CLASS));

		final Expression.Builder eb = Expression.newBuilder();

		eb.setKind(Expression.ExpressionKind.METHODCALL);
		eb.setMethod(n.getTypeReference().getText());

		if (n.getValueArgumentList() != null) {
			expressions.push(new ArrayList<Expression>());
			n.getValueArgumentList().accept(this, v);
			eb.addAllMethodArgs(expressions.pop());
		}

		superClassInitExprs.add(eb.build());
		return null;
	}

	@Override
	public Void visitSuperTypeEntry(final KtSuperTypeEntry n, final Void v) {
		if (n.getTypeReference() != null)
			types.peek().add(typeFromTypeRef(n.getTypeReference(), TypeKind.CLASS));
		return null;
	}

	@Override
	public Void visitTypeReference(final KtTypeReference n, final Void v) {
		// FIXME remove?
		System.err.println(n.getClass());
		// n.acceptChildren(this, v);
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
		// FIXME remove?
		System.err.println(expr.getClass());
		// expr.acceptChildren(this, v);
		return null;
	}

	@Override
	public Void visitLabeledExpression(final KtLabeledExpression expr, final Void v) {
		final Statement.Builder sb = Statement.newBuilder();

		sb.setKind(Statement.StatementKind.LABEL);

		sb.addExpressions(Expression.newBuilder()
				.setKind(Expression.ExpressionKind.LABEL)
				.setLiteral(expr.getNameIdentifier().getText())
				.build());

		if (expr.getBaseExpression() != null) {
			final List<Statement> stmts = new ArrayList<Statement>();
			statements.push(stmts);

			final List<Expression> exprs = new ArrayList<Expression>();
			expressions.push(exprs);

			expr.getBaseExpression().accept(this, v);
			for (final Expression e : exprs)
				stmts.add(Statement.newBuilder()
					.setKind(Statement.StatementKind.EXPRESSION)
					.addExpressions(e)
					.build());
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
		case "PLUS":
			eb.setKind(Expression.ExpressionKind.OP_ADD);
			break;
		case "EXCLEXCL":
			eb.setKind(Expression.ExpressionKind.OP_NOTNULL);
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
			eb.setKind(Expression.ExpressionKind.OTHER);
			eb.setLiteral(expr.getOperationToken().toString());
			System.err.println("===> UNKNOWN UNARY OPERATOR: " + expr.getOperationToken().toString());
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

		if (expr.getLabelName() != null)
			sb.addExpressions(Expression.newBuilder()
					.setLiteral(expr.getLabelName())
					.setKind(Expression.ExpressionKind.LABEL)
					.build());

		pushStatementOrExpr(sb);
		return null;
	}

	@Override
	public Void visitThrowExpression(final KtThrowExpression expr, final Void v) {
		final Statement.Builder sb = Statement.newBuilder();

		sb.setKind(Statement.StatementKind.THROW);

		expressions.push(new ArrayList<Expression>());
		expr.acceptChildren(this, v);
		sb.addAllExpressions(expressions.pop());

		pushStatementOrExpr(sb);
		return null;
	}

	@Override
	public Void visitIfExpression(final KtIfExpression expr, final Void v) {
		final Statement.Builder sb = Statement.newBuilder();

		sb.setKind(Statement.StatementKind.IF);

		if (expr.getCondition() != null) {
			expressions.push(new ArrayList<Expression>());
			expr.getCondition().accept(this, v);
			sb.addAllConditions(expressions.pop());
		}

		statements.push(new ArrayList<Statement>());
		expressions.push(new ArrayList<Expression>());
		if (expr.getThen() != null)
			expr.getThen().accept(this, v);
		else
			pushEmpty();
		for (final Expression e : expressions.pop())
			sb.addStatements(Statement.newBuilder()
					.setKind(Statement.StatementKind.EXPRESSION)
					.addExpressions(e)
					.build());
		sb.addAllStatements(statements.pop());

		if (expr.getElse() != null || expr.getElseKeyword() != null) {
			statements.push(new ArrayList<Statement>());
			expressions.push(new ArrayList<Expression>());
			if (expr.getElse() != null)
				expr.getElse().accept(this, v);
			else
				pushEmpty();
			for (final Expression e : expressions.pop())
				sb.addStatements(Statement.newBuilder()
						.setKind(Statement.StatementKind.EXPRESSION)
						.addExpressions(e)
						.build());
			sb.addAllStatements(statements.pop());
		}

		pushStatementOrExpr(sb);
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

		expectExpression.push(false);
		if (expr.getDestructuringDeclaration() != null) {
			fields.push(new ArrayList<Variable>());
			expressions.push(new ArrayList<Expression>());
			expr.getDestructuringDeclaration().accept(this, v);
			sb.addAllInitializations(expressions.pop());
			sb.addAllVariableDeclarations(fields.pop());
		} else if (expr.getLoopParameter() != null) {
			fields.push(new ArrayList<Variable>());
			expr.getLoopParameter().accept(this, v);
			sb.addAllVariableDeclarations(fields.pop());
		}
		expectExpression.pop();

		if (expr.getLoopRange() != null) {
			expressions.push(new ArrayList<Expression>());
			expr.getLoopRange().accept(this, v);
			sb.addAllInitializations(expressions.pop());
		}

		expectExpression.push(false);
		statements.push(new ArrayList<Statement>());
		expressions.push(new ArrayList<Expression>());
		expr.getBody().accept(this, v);
		for (final Expression e : expressions.pop())
			sb.addStatements(Statement.newBuilder()
					.setKind(Statement.StatementKind.EXPRESSION)
					.addExpressions(e)
					.build());
		sb.addAllStatements(statements.pop());
		expectExpression.pop();

		pushStatementOrExpr(sb);
		return null;
	}

	@Override
	public Void visitWhileExpression(final KtWhileExpression expr, final Void v) {
		final Statement.Builder sb = Statement.newBuilder();

		sb.setKind(Statement.StatementKind.WHILE);

		expressions.push(new ArrayList<Expression>());
		expr.getCondition().accept(this, v);
		sb.addAllConditions(expressions.pop());

		if (expr.getBody() != null) {
			statements.push(new ArrayList<Statement>());
			expressions.push(new ArrayList<Expression>());
			expr.getBody().accept(this, v);
			for (final Expression e : expressions.pop())
				sb.addStatements(Statement.newBuilder()
						.setKind(Statement.StatementKind.EXPRESSION)
						.addExpressions(e)
						.build());
			sb.addAllStatements(statements.pop());
		}

		pushStatementOrExpr(sb);
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
			statements.push(new ArrayList<Statement>());
			expressions.push(new ArrayList<Expression>());
			expr.getBody().accept(this, v);
			for (final Expression e : expressions.pop())
				sb.addStatements(Statement.newBuilder()
						.setKind(Statement.StatementKind.EXPRESSION)
						.addExpressions(e)
						.build());
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
			expectExpression.push(true);
			expressions.push(new ArrayList<Expression>());
			for (final KtExpression e : expr.getBodyExpression().getStatements())
				e.accept(this, v);
			eb.addAllExpressions(expressions.pop());
			expectExpression.pop();
		}

		expressions.peek().add(eb.build());
		return null;
	}

	@Override
	public Void visitAnnotatedExpression(final KtAnnotatedExpression expr, final Void v) {
		modifiers.push(new ArrayList<Modifier>());
		for (final KtAnnotationEntry ann : expr.getAnnotationEntries())
			ann.accept(this, v);

		expressions.push(new ArrayList<Expression>());
		expectExpression.push(true);
		expr.getBaseExpression().accept(this, v);
		expectExpression.pop();

		final Expression.Builder eb = Expression.newBuilder(expressions.pop().get(0));
		eb.addAllModifiers(modifiers.pop());
		expressions.peek().add(eb.build());
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
		// FIXME remove?
		System.err.println(expr.getClass());
		// expr.acceptChildren(this, v);
		return null;
	}

	@Override
	public Void visitDoubleColonExpression(final KtDoubleColonExpression expr, final Void v) {
		final Expression.Builder eb = Expression.newBuilder();

		eb.setKind(Expression.ExpressionKind.METHOD_REFERENCE);
		eb.setMethod(expr.getText());

		if (expr.getReceiverExpression() != null) {
			expressions.push(new ArrayList<Expression>());
			expr.getReceiverExpression().accept(this, v);
			eb.addAllExpressions(expressions.pop());
		}

		expressions.peek().add(eb.build());
		return null;
	}

	@Override
	public Void visitCallableReferenceExpression(final KtCallableReferenceExpression expr, final Void v) {
		final Expression.Builder eb = Expression.newBuilder();

		eb.setKind(Expression.ExpressionKind.METHOD_REFERENCE);
		eb.setMethod(expr.getCallableReference().getText());

		if (expr.getReceiverExpression() != null) {
			expressions.push(new ArrayList<Expression>());
			expr.getReceiverExpression().accept(this, v);
			eb.addAllExpressions(expressions.pop());
		}

		expressions.peek().add(eb.build());
		return null;
	}

	@Override
	public Void visitArgument(final KtValueArgument arg, final Void v) {
		expectExpression.push(true);
		arg.getArgumentExpression().accept(this, v);
		expectExpression.pop();

		if (arg.isSpread()) {
			final List<Expression> list = expressions.peek();
			list.add(Expression.newBuilder()
					.setKind(Expression.ExpressionKind.OP_MULT)
					.addExpressions(list.remove(list.size() - 1))
					.build());
		}

		if (arg.isNamed()) {
			final List<Expression> list = expressions.peek();
			list.add(Expression.newBuilder()
					.setKind(Expression.ExpressionKind.ASSIGN)
					.addExpressions(Expression.newBuilder()
							.setKind(Expression.ExpressionKind.VARACCESS)
							.setVariable(arg.getArgumentName().getText())
							.build())
					.addExpressions(list.remove(list.size() - 1))
					.build());
		}

		return null;
	}

	@Override
	public Void visitDotQualifiedExpression(final KtDotQualifiedExpression expr, final Void v) {
		final Expression.Builder eb = Expression.newBuilder();

		final KtExpression rcvr = expr.getReceiverExpression();
		final KtExpression sel = expr.getSelectorExpression();

		if (sel instanceof KtCallExpression) {
			final KtCallExpression call = (KtCallExpression)sel;

			eb.setKind(Expression.ExpressionKind.METHODCALL);

			expressions.push(new ArrayList<Expression>());
			rcvr.accept(this, v);
			eb.addAllExpressions(expressions.pop());

			eb.setMethod(call.getCalleeExpression().getText());

			if (call.getValueArgumentList() != null) {
				expressions.push(new ArrayList<Expression>());
				call.getValueArgumentList().accept(this, v);
				eb.addAllMethodArgs(expressions.pop());
			}
		} else {
			eb.setKind(Expression.ExpressionKind.VARACCESS);
			eb.setVariable(expr.getText());
		}

		expressions.peek().add(eb.build());
		return null;
	}

	@Override
	public Void visitSafeQualifiedExpression(final KtSafeQualifiedExpression expr, final Void v) {
		final Expression.Builder eb = Expression.newBuilder();

		final KtExpression rcvr = expr.getReceiverExpression();
		final KtExpression sel = expr.getSelectorExpression();

		if (sel instanceof KtCallExpression) {
			final KtCallExpression call = (KtCallExpression)sel;

			eb.setKind(Expression.ExpressionKind.METHODCALL);

			expressions.push(new ArrayList<Expression>());
			rcvr.accept(this, v);
			eb.addAllExpressions(expressions.pop());
			// FIXME need to add the '?' into this somehow

			eb.setMethod(call.getCalleeExpression().getText());

			if (call.getValueArgumentList() != null) {
				expressions.push(new ArrayList<Expression>());
				call.getValueArgumentList().accept(this, v);
				eb.addAllMethodArgs(expressions.pop());
			}
		} else {
			eb.setKind(Expression.ExpressionKind.VARACCESS);
			eb.setVariable(expr.getText());
		}

		expressions.peek().add(eb.build());
		return null;
	}

	@Override
	public Void visitObjectLiteralExpression(final KtObjectLiteralExpression expr, final Void v) {
		final Expression.Builder eb = Expression.newBuilder();

		eb.setKind(Expression.ExpressionKind.NEW);

		declarations.push(new ArrayList<Declaration>());
		expr.getObjectDeclaration().accept(this, v);
		final Declaration objLiteral = declarations.pop().get(0);

		eb.setAnonDeclaration(objLiteral);

		if (objLiteral.getParentsCount() > 0)
			eb.setNewType(objLiteral.getParents(0));

		expressions.peek().add(eb.build());

		return null;
	}

	@Override
	public Void visitBlockExpression(final KtBlockExpression expr, final Void v) {
		final Statement.Builder sb = Statement.newBuilder();

		sb.setKind(Statement.StatementKind.BLOCK);

		for (final KtExpression e : expr.getStatements()) {
			statements.push(new ArrayList<Statement>());
			expressions.push(new ArrayList<Expression>());

			expectExpression.push(e instanceof KtProperty);
			e.accept(this, v);
			expectExpression.pop();

			for (final Expression ex : expressions.pop())
				sb.addStatements(Statement.newBuilder()
						.setKind(Statement.StatementKind.EXPRESSION)
						.addExpressions(ex)
						.build());
			sb.addAllStatements(statements.pop());
		}

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

		pushStatementOrExpr(sb);
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

		final KtExpression body = n.getCatchBody();

		if (body instanceof KtBlockExpression) {
			for (final KtExpression e : ((KtBlockExpression) body).getStatements()) {
				statements.push(new ArrayList<Statement>());
				expressions.push(new ArrayList<Expression>());
				e.accept(this, v);
				for (final Expression ex : expressions.pop())
					sb.addStatements(Statement.newBuilder()
							.setKind(Statement.StatementKind.EXPRESSION)
							.addExpressions(ex)
							.build());
				sb.addAllStatements(statements.pop());
			}
		} else {
			statements.push(new ArrayList<Statement>());
			expressions.push(new ArrayList<Expression>());
			body.accept(this, v);
			for (final Expression ex : expressions.pop())
				sb.addStatements(Statement.newBuilder()
						.setKind(Statement.StatementKind.EXPRESSION)
						.addExpressions(ex)
						.build());
			sb.addAllStatements(statements.pop());
		}

		statements.peek().add(sb.build());

		return null;
	}

	@Override
	public Void visitFinallySection(final KtFinallySection n, final Void v) {
		final Statement.Builder sb = Statement.newBuilder();

		sb.setKind(Statement.StatementKind.FINALLY);

		for (final KtExpression e : n.getFinalExpression().getStatements()) {
			statements.push(new ArrayList<Statement>());
			expressions.push(new ArrayList<Expression>());
			e.accept(this, v);
			for (final Expression ex : expressions.pop())
				sb.addStatements(Statement.newBuilder()
						.setKind(Statement.StatementKind.EXPRESSION)
						.addExpressions(ex)
						.build());
			sb.addAllStatements(statements.pop());
		}

		statements.peek().add(sb.build());

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
	public Void visitAnonymousInitializer(final KtAnonymousInitializer n, final Void v) {
		// FIXME remove?
		System.err.println(n.getClass());
		// n.acceptChildren(this, v);
		return null;
	}

	@Override
	public Void visitClassInitializer(final KtClassInitializer n, final Void v) {
		final Method.Builder mb = Method.newBuilder();

		mb.setName("<clinit>");

		statements.push(new ArrayList<Statement>());
		expressions.push(new ArrayList<Expression>());
		expectExpression.push(true);

		n.getBody().accept(this, v);

		expectExpression.pop();
		for (final Expression e : expressions.pop())
			mb.addStatements(Statement.newBuilder()
					.setKind(Statement.StatementKind.EXPRESSION)
					.addExpressions(e)
					.build());
		mb.addAllStatements(statements.pop());

		methods.peek().add(mb.build());
		return null;
	}

	@Override
	public Void visitTypeConstraint(final KtTypeConstraint n, final Void v) {
		// FIXME remove?
		System.err.println(n.getClass());
		// n.acceptChildren(this, v);
		return null;
	}

	@Override
	public Void visitBinaryWithTypeRHSExpression(final KtBinaryExpressionWithTypeRHS expr, final Void v) {
		final Expression.Builder eb = Expression.newBuilder();

		expressions.push(new ArrayList<Expression>());
		expr.getLeft().accept(this, v);
		eb.addAllExpressions(expressions.pop());

		eb.setNewType(typeFromTypeRef(expr.getRight()));

		switch (expr.getOperationReference().getText()) {
		case "as":
			eb.setKind(Expression.ExpressionKind.CAST);
			break;
		case "as?":
		default:
			// FIXME maybe different kind?
			eb.setKind(Expression.ExpressionKind.CAST);
			eb.setLiteral(expr.getOperationReference().getText());
			break;
		}

		expressions.peek().add(eb.build());
		return null;
	}

	@Override
	public Void visitTypeProjection(final KtTypeProjection n, final Void v) {
		if (n.getTypeReference() != null)
			types.peek().add(typeFromTypeRef(n.getTypeReference(), TypeKind.GENERIC));
		else
			types.peek().add(Type.newBuilder()
					.setName("*")
					.setKind(TypeKind.GENERIC)
					.build());
		return null;
	}

	@Override
	public Void visitWhenExpression(final KtWhenExpression expr, final Void v) {
		final Statement.Builder sb = Statement.newBuilder();

		sb.setKind(Statement.StatementKind.SWITCH);

		expressions.push(new ArrayList<Expression>());
		expectExpression.push(true);
		if (expr.getSubjectVariable() != null)
			expr.getSubjectVariable().accept(this, v);
		else if (expr.getSubjectExpression() != null)
			expr.getSubjectExpression().accept(this, v);
		expectExpression.pop();
		sb.addAllConditions(expressions.pop());

		statements.push(new ArrayList<Statement>());
		expectExpression.push(false);
		for (final KtWhenEntry entry : expr.getEntries())
			entry.accept(this, v);
		expectExpression.pop();
		sb.addAllStatements(statements.pop());

		pushStatementOrExpr(sb);
		return null;
	}

	@Override
	public Void visitWhenEntry(final KtWhenEntry n, final Void v) {
		final Statement.Builder sb = Statement.newBuilder();

		if (n.isElse()) {
			sb.setKind(Statement.StatementKind.DEFAULT);
		} else {
			sb.setKind(Statement.StatementKind.CASE);

			if (n.getConditions().length != 0) {
				expressions.push(new ArrayList<Expression>());
				for (final KtWhenCondition cond : n.getConditions())
					cond.accept(this, v);
				sb.addAllExpressions(expressions.pop());
			}
		}

		statements.peek().add(sb.build());

		if (n.getExpression() != null) {
			expressions.push(new ArrayList<Expression>());
			n.getExpression().accept(this, v);
			for (final Expression e : expressions.pop())
				statements.peek().add(Statement.newBuilder()
						.setKind(Statement.StatementKind.EXPRESSION)
						.addExpressions(e)
						.build());
		}

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
		// FIXME how is this possible???
		if (expr.getExpression() != null)
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
			expressions.push(new ArrayList<Expression>());

			final StringBuilder sb = new StringBuilder();
			sb.append("\"");
			for (final KtStringTemplateEntry e : entries) {
				e.accept(this, v);
				sb.append(e.getText());
			}
			sb.append("\"");
			final List<Expression> exprs = expressions.pop();

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
					.setKind(Expression.ExpressionKind.LABEL)
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
					.setKind(Expression.ExpressionKind.LABEL)
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
		case "EQEQEQ":
			eb.setKind(Expression.ExpressionKind.SHEQ);
			break;
		case "EXCLEQEQEQ":
			eb.setKind(Expression.ExpressionKind.SHNEQ);
			break;
		case "NOT_IN":
			eb.setKind(Expression.ExpressionKind.NOT_IN);
			break;
		case "ELVIS":
			eb.setKind(Expression.ExpressionKind.OP_ELVIS);
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
			case "downTo":
				eb.setKind(Expression.ExpressionKind.ARRAY_COMPREHENSION);
				eb.setLiteral("downTo");
				break;
			case "until":
				eb.setKind(Expression.ExpressionKind.ARRAY_COMPREHENSION);
				eb.setLiteral("until");
				break;
			case "step":
				expressions.push(new ArrayList<Expression>());
				expr.getLeft().accept(this, v);
				final Expression.Builder eb2 = Expression.newBuilder(expressions.pop().get(0));
				expressions.push(new ArrayList<Expression>());
				expr.getRight().accept(this, v);
				eb2.addExpressions(expressions.pop().get(0));
				expressions.peek().add(eb2.build());
				return null;
			default:
				eb.setKind(Expression.ExpressionKind.OTHER);
				eb.setLiteral(expr.getOperationReference().getText());
				// System.err.println("===> UNKNOWN BINARY OPERATOR ID: " + expr.getOperationReference().getText());
				break;
			}
			break;

		default:
			eb.setKind(Expression.ExpressionKind.OTHER);
			eb.setLiteral(expr.getOperationToken().toString());
			System.err.println("===> UNKNOWN BINARY OPERATOR: " + expr.getOperationToken().toString());
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
		expectExpression.push(false);
		for (final KtParameter p : acc.getValueParameters())
			p.accept(this, v);
		expectExpression.pop();
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
			expectExpression.push(false);
			statements.push(new ArrayList<Statement>());
			acc.getBodyBlockExpression().accept(this, v);
			mb.addAllStatements(statements.pop());
			expectExpression.pop();
		} else if (acc.getBodyExpression() != null) {
			expectExpression.push(true);
			expressions.push(new ArrayList<Expression>());
			acc.getBodyExpression().accept(this, v);
			// FIXME what if there is more than 1?
			mb.setExpression(expressions.pop().get(0));
			expectExpression.pop();
		}

		methods.peek().add(mb.build());
		return null;
	}

	@Override
	public Void visitProperty(final KtProperty prop, final Void v) {
		final Variable.Builder vb = Variable.newBuilder();

		expectExpression.push(true);

		if (prop.getReceiverTypeReference() != null)
			vb.setName(prop.getReceiverTypeReference().getText() + "." + prop.getName());
		else
			vb.setName(prop.getName());

		if (prop.getValOrVarKeyword() == null)
			vb.addModifiers(Modifier.newBuilder()
					.setKind(Modifier.ModifierKind.IMPLICIT)
					.build());

		final KtTypeReference typeRef = prop.getTypeReference();
		final KtPropertyDelegate propDelegate = prop.getDelegate();
		if ((typeRef != null) && (propDelegate != null)) {
			final Type.Builder tb = Type.newBuilder();

			tb.setKind(TypeKind.DELEGATED);
			tb.setName(typeRef.getText());

			if (propDelegate.getExpression() != null) {
				// FIXME
				expressions.push(new ArrayList<Expression>());
				propDelegate.getExpression().accept(this, v);
				tb.setDelegate(expressions.pop().get(0));
			}

			vb.setVariableType(tb.build());
		} else if (typeRef != null) {
			vb.setVariableType(typeFromTypeRef(typeRef));
		}

		modifiers.push(new ArrayList<Modifier>());
		if (!prop.isVar())
			modifiers.peek().add(Modifier.newBuilder()
					.setKind(Modifier.ModifierKind.FINAL)
					.build());
		if (prop.getModifierList() != null)
			prop.getModifierList().accept(this, v);
		vb.addAllModifiers(modifiers.pop());

		if (prop.hasInitializer()) {
			final List<Expression> exprs = new ArrayList<Expression>();
			expressions.push(exprs);
			prop.getInitializer().accept(this, v);
			if (exprs.size() == 1)
				vb.setInitializer(exprs.get(0));
			expressions.pop();
		}

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

		for (final KtTypeParameter type_param : klass.getTypeParameters())
			db.addGenericParameters(typeFromTypeParameter(type_param, TypeKind.GENERIC));

		if (klass.getSuperTypeList() != null) {
			types.push(new ArrayList<Type>());
			klass.getSuperTypeList().accept(this, v);
			db.addAllParents(types.pop());
		}

		if (klass.getModifierList() != null) {
			modifiers.push(new ArrayList<Modifier>());
			klass.getModifierList().accept(this, v);
			db.addAllModifiers(modifiers.pop());
		}

		fields.push(new ArrayList<Variable>());
		methods.push(new ArrayList<Method>());
		declarations.push(new ArrayList<Declaration>());

		if (klass.getPrimaryConstructor() != null)
			klass.getPrimaryConstructor().accept(this, v);

		if (klass.getBody() != null)
			klass.getBody().accept(this, v);

		db.addAllNestedDeclarations(declarations.pop());
		db.addAllMethods(methods.pop());
		db.addAllFields(fields.pop());

		declarations.peek().add(db.build());

		superClassInitExprs.clear();
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
		if (constructor.getModifierList() != null)
			constructor.getModifierList().accept(this, null);
		mb.addAllModifiers(modifiers.pop());

		statements.push(new ArrayList<Statement>());

		if (isPrimary) {
			final List<Statement> stmts = new ArrayList<Statement>();
			for (final Expression expr : superClassInitExprs)
				stmts.add(Statement.newBuilder()
						.setKind(Statement.StatementKind.EXPRESSION)
						.addExpressions(expr)
						.build());
			if (stmts.size() > 0)
				statements.peek().add(Statement.newBuilder()
						.setKind(Statement.StatementKind.BLOCK)
						.addAllStatements(stmts)
						.build());
		} else {
			if (!((KtSecondaryConstructor) constructor).hasImplicitDelegationCall()) {
				expressions.push(new ArrayList<Expression>());
				((KtSecondaryConstructor) constructor).getDelegationCall().accept(this, null);
				statements.peek().add(Statement.newBuilder()
						.setKind(Statement.StatementKind.EXPRESSION)
						.addExpressions(Expression.newBuilder()
								.setKind(Expression.ExpressionKind.METHODCALL)
								.setMethod(((KtSecondaryConstructor) constructor)
									.getDelegationCall()
									.getCalleeExpression()
									.isThis() ? "this" : "super") // FIXME TEMP
								.addAllExpressions(expressions.pop())
								.build())
						.build());
			}
		}

		if (constructor.getBodyExpression() != null)
			constructor.getBodyExpression().accept(this, null);

		mb.addAllStatements(statements.pop());

		fields.push(new ArrayList<Variable>());
		if (constructor.getValueParameterList() != null)
			constructor.getValueParameterList().accept(this, null);
		mb.addAllArguments(fields.pop());

		if (isPrimary && constructor.getValueParameterList() != null)
			for (final KtParameter p : constructor.getValueParameterList().getParameters())
				if (p.hasValOrVar())
					p.accept(this, null);

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

		mb.setName(function.getName() != null ? function.getName() : "");

		if (function.getModifierList() != null) {
			modifiers.push(new ArrayList<Modifier>());
			function.getModifierList().accept(this, v);
			mb.addAllModifiers(modifiers.pop());
		}

		if (function.hasDeclaredReturnType())
			mb.setReturnType(typeFromTypeRef(function.getTypeReference()));

		for (final KtTypeParameter p : function.getTypeParameters())
			mb.addGenericParameters(typeFromTypeParameter(p, TypeKind.GENERIC));

		fields.push(new ArrayList<Variable>());
		for (final KtParameter p : function.getValueParameters())
			p.accept(this, v);
		mb.addAllArguments(fields.pop());

		if (function.getBodyBlockExpression() != null) {
			expectExpression.push(false);
			statements.push(new ArrayList<Statement>());
			function.getBodyBlockExpression().accept(this, v);
			mb.addAllStatements(statements.pop());
			expectExpression.pop();
		} else if (function.getBodyExpression() != null) {
			expectExpression.push(true);
			expressions.push(new ArrayList<Expression>());
			function.getBodyExpression().accept(this, v);
			// FIXME what if there is more than 1?
			mb.setExpression(expressions.pop().get(0));
			expectExpression.pop();
		}

		methods.peek().add(mb.build());
		return null;
	}

	@Override
	public Void visitParameter(final KtParameter param, final Void v) {
		final Variable.Builder vb = Variable.newBuilder();

		expectExpression.push(true);

		if (param.getName() != null)
			vb.setName(param.getName());

		if (param.getTypeReference() != null)
			vb.setVariableType(typeFromTypeRef(param.getTypeReference()));

		if (!param.hasValOrVar())
			vb.addModifiers(Modifier.newBuilder()
					.setKind(Modifier.ModifierKind.IMPLICIT)
					.build());

		if (!param.hasValOrVar() || !param.isMutable())
			vb.addModifiers(Modifier.newBuilder()
					.setKind(Modifier.ModifierKind.FINAL)
					.build());

		if (param.getModifierList() != null) {
			modifiers.push(new ArrayList<Modifier>());
			param.getModifierList().accept(this, v);
			vb.addAllModifiers(modifiers.pop());
		}

		if (param.hasDefaultValue()) {
			final List<Expression> exprs = new ArrayList<Expression>();
			expressions.push(exprs);
			statements.push(new ArrayList<Statement>());
			param.getDefaultValue().accept(this, v);
			if (exprs.size() > 0)
				vb.setInitializer(exprs.get(0));
			statements.pop();
			expressions.pop();
		}

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
	public Void visitCallExpression(final KtCallExpression call, final Void v) {
		final Expression.Builder eb = Expression.newBuilder();

		eb.setKind(Expression.ExpressionKind.METHODCALL);

		eb.setMethod(call.getCalleeExpression().getText());

		if (call.getValueArgumentList() != null) {
			expressions.push(new ArrayList<Expression>());
			call.getValueArgumentList().accept(this, v);
			eb.addAllMethodArgs(expressions.pop());
		}

		if (call.getLambdaArguments() != null) {
			expressions.push(new ArrayList<Expression>());
			for (final KtLambdaArgument arg : call.getLambdaArguments())
				arg.accept(this, v);
			eb.addAllMethodArgs(expressions.pop());
		}

		if (call.getTypeArgumentList() != null) {
			types.push(new ArrayList<Type>());
			call.getTypeArgumentList().accept(this, v);
			eb.addAllGenericParameters(types.pop());
		}

		expressions.peek().add(eb.build());
		return null;
	}

	@Override
	public void visitWhiteSpace(final PsiWhiteSpace space) {
		// ignore
	}

	// Utility methods
	private Type typeFromTypeRef(final KtTypeReference type) {
		return typeFromTypeRef(type, TypeKind.OTHER);
	}

	private Type typeFromTypeRef(final KtTypeReference type, TypeKind kind) {
		final Type.Builder tb = Type.newBuilder();
		tb.setName(type.getText());
		if (type.getText().equals("dynamic") && (kind == TypeKind.OTHER))
			tb.setKind(TypeKind.DYNAMIC);
		else
			tb.setKind(kind);
		return tb.build();
	}

	private Type typeFromTypeParameter(final KtTypeParameter type) {
		return typeFromTypeParameter(type, TypeKind.OTHER);
	}

	private Type typeFromTypeParameter(final KtTypeParameter type, TypeKind kind) {
		final Type.Builder tb = Type.newBuilder();
		tb.setName(type.getText());
		tb.setKind(kind);
		return tb.build();
	}

	private void pushStatementOrExpr(final Statement.Builder sb) {
		if (expectExpression.peek()) {
			final Expression.Builder eb = Expression.newBuilder();
			eb.setKind(Expression.ExpressionKind.STATEMENT);
			eb.addStatements(sb.build());
			expressions.peek().add(eb.build());
		} else {
			statements.peek().add(sb.build());
		}
	}

	private void pushEmpty() {
		expressions.peek().add(Expression.newBuilder()
				.setKind(Expression.ExpressionKind.EMPTY)
				.build());
	}
}
