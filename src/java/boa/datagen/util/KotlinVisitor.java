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
	public Void visitKtElement(final KtElement element, final Void v) {
		if (element instanceof KtNameReferenceExpression)
			visitNameReferenceExpression((KtNameReferenceExpression) element, v);
		else
			visitElement(element);
		return null;
	}

	public void visitLeaf(final LeafPsiElement leaf) {
		if (leaf.getElementType() instanceof KtModifierKeywordToken)
			visitModifier((KtModifierKeywordToken)leaf.getElementType());
	}

	public void visitModifier(final KtModifierKeywordToken m) {
		Modifier.Builder mb = Modifier.newBuilder();

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
		case "enum":
		case "value":
		case "inner":
		case "data":
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

		default:
			System.out.println("unknown modifier: " + m.getValue());
			mb.setKind(Modifier.ModifierKind.OTHER);
			mb.setOther(m.getValue());
			break;
		}

		modifiers.peek().add(mb.build());
	}

	@Override
	public void visitElement(final PsiElement element) {
		if (element instanceof LeafPsiElement)
			visitLeaf((LeafPsiElement)element);
		else
			element.acceptChildren(this);
	}

	@Override
	public Void visitKtFile(final KtFile f, final Void v) {
		modifiers.push(new ArrayList<Modifier>());
		declarations.push(new ArrayList<Declaration>());
		fields.push(new ArrayList<Variable>());
		methods.push(new ArrayList<Method>());
		statements.push(new ArrayList<Statement>());

		b.setName("");
		f.acceptChildren(this);

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
		// TODO
		d.acceptChildren(this, v);
		return null;
	}

	@Override
	public Void visitClassOrObject(final KtClassOrObject n, final Void v) {
		// TODO
		n.acceptChildren(this, v);
		return null;
	}

	@Override
	public Void visitSecondaryConstructor(final KtSecondaryConstructor constructor, final Void v) {
		final Method.Builder mb = Method.newBuilder();

		mb.setName("<init>");

		modifiers.push(new ArrayList<Modifier>());
		fields.push(new ArrayList<Variable>());
		expressions.push(new ArrayList<Expression>());
		constructor.acceptChildren(this, v);
		List<Expression> exprs = expressions.pop();
		if (exprs.size() != 0)
			mb.addStatements(Statement.newBuilder()
					 .setKind(Statement.StatementKind.BLOCK)
					 .addAllExpressions(exprs));
		mb.addAllArguments(fields.pop());
		mb.addAllModifiers(modifiers.pop());

		methods.peek().add(mb.build());
		return null;
	}

	@Override
	public Void visitDestructuringDeclaration(final KtDestructuringDeclaration d, final Void v) {
		// TODO
		d.acceptChildren(this, v);
		return null;
	}

	@Override
	public Void visitDestructuringDeclarationEntry(final KtDestructuringDeclarationEntry n, final Void v) {
		// TODO
		n.acceptChildren(this, v);
		return null;
	}

	@Override
	public Void visitTypeAlias(final KtTypeAlias n, final Void v) {
		// TODO
		n.acceptChildren(this, v);
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
	public Void visitModifierList(final KtModifierList n, final Void v) {
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
	public Void visitExpression(final KtExpression expr, final Void v) {
		// TODO
		expr.acceptChildren(this, v);
		return null;
	}

	@Override
	public Void visitLoopExpression(final KtLoopExpression expr, final Void v) {
		// TODO
		expr.acceptChildren(this, v);
		return null;
	}

	@Override
	public Void visitSimpleNameExpression(final KtSimpleNameExpression expr, final Void v) {
		// TODO
		expr.acceptChildren(this, v);
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
		// TODO
		expr.acceptChildren(this, v);
		return null;
	}

	@Override
	public Void visitPrefixExpression(final KtPrefixExpression expr, final Void v) {
		// TODO
		expr.acceptChildren(this, v);
		return null;
	}

	@Override
	public Void visitPostfixExpression(final KtPostfixExpression expr, final Void v) {
		// TODO
		expr.acceptChildren(this, v);
		return null;
	}

	@Override
	public Void visitUnaryExpression(final KtUnaryExpression expr, final Void v) {
		// TODO
		expr.acceptChildren(this, v);
		return null;
	}

	@Override
	public Void visitReturnExpression(final KtReturnExpression expr, final Void v) {
		// TODO
		expr.acceptChildren(this, v);
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
		// TODO
		expr.acceptChildren(this, v);
		return null;
	}

	@Override
	public Void visitIfExpression(final KtIfExpression expr, final Void v) {
		// TODO
		expr.acceptChildren(this, v);
		return null;
	}

	@Override
	public Void visitWhenExpression(final KtWhenExpression expr, final Void v) {
		// TODO
		expr.acceptChildren(this, v);
		return null;
	}

	@Override
	public Void visitCollectionLiteralExpression(final KtCollectionLiteralExpression expr, final Void v) {
		// TODO
		expr.acceptChildren(this, v);
		return null;
	}

	@Override
	public Void visitTryExpression(final KtTryExpression expr, final Void v) {
		// TODO
		expr.acceptChildren(this, v);
		return null;
	}

	@Override
	public Void visitForExpression(final KtForExpression expr, final Void v) {
		// TODO
		expr.acceptChildren(this, v);
		return null;
	}

	@Override
	public Void visitDoWhileExpression(final KtDoWhileExpression expr, final Void v) {
		// TODO
		expr.acceptChildren(this, v);
		return null;
	}

	@Override
	public Void visitLambdaExpression(final KtLambdaExpression expr, final Void v) {
		// TODO
		expr.acceptChildren(this, v);
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
		// TODO
		expr.acceptChildren(this, v);
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
		// TODO
		expr.acceptChildren(this, v);
		return null;
	}

	@Override
	public Void visitCallableReferenceExpression(final KtCallableReferenceExpression expr, final Void v) {
		// TODO
		expr.acceptChildren(this, v);
		return null;
	}

	@Override
	public Void visitDotQualifiedExpression(final KtDotQualifiedExpression expr, final Void v) {
		final Expression.Builder eb = Expression.newBuilder();

		eb.setKind(Expression.ExpressionKind.VARACCESS);
		eb.setVariable(expr.getText());

		expressions.peek().add(eb.build());
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
		// TODO
		expr.acceptChildren(this, v);
		return null;
	}

	@Override
	public Void visitCatchSection(final KtCatchClause n, final Void v) {
		// TODO
		n.acceptChildren(this, v);
		return null;
	}

	@Override
	public Void visitFinallySection(final KtFinallySection n, final Void v) {
		// TODO
		n.acceptChildren(this, v);
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
		// TODO
		expr.acceptChildren(this, v);
		return null;
	}

	@Override
	public Void visitSuperExpression(final KtSuperExpression expr, final Void v) {
		// TODO
		expr.acceptChildren(this, v);
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
		// TODO
		n.acceptChildren(this, v);
		return null;
	}

	@Override
	public Void visitPropertyAccessor(final KtPropertyAccessor prop, final Void v) {
		expressions.push(new ArrayList<Expression>());
		statements.push(new ArrayList<Statement>());
		prop.acceptChildren(this, v);
		// TODO
		expressions.pop();
		statements.pop();
		prop.getProperty();
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
	public Void visitStringTemplateExpression(final KtStringTemplateExpression expr, final Void v) {
		final StringBuilder sb = new StringBuilder();
		for (final KtStringTemplateEntry e : expr.getEntries())
			sb.append(e.getText());

		expressions.peek().add(Expression.newBuilder()
				.setKind(Expression.ExpressionKind.LITERAL)
				.setLiteral("\"" + sb.toString() + "\"")
				.build());
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
	public Void visitWhenEntry(final KtWhenEntry n, final Void v) {
		// TODO
		n.acceptChildren(this, v);
		return null;
	}

	@Override
	public Void visitIsExpression(final KtIsExpression expr, final Void v) {
		// TODO
		expr.acceptChildren(this, v);
		return null;
	}

	@Override
	public Void visitWhenConditionIsPattern(final KtWhenConditionIsPattern n, final Void v) {
		// TODO
		n.acceptChildren(this, v);
		return null;
	}

	@Override
	public Void visitWhenConditionInRange(final KtWhenConditionInRange n, final Void v) {
		// TODO
		n.acceptChildren(this, v);
		return null;
	}

	@Override
	public Void visitWhenConditionWithExpression(final KtWhenConditionWithExpression expr, final Void v) {
		// TODO
		expr.acceptChildren(this, v);
		return null;
	}

	@Override
	public Void visitStringTemplateEntry(final KtStringTemplateEntry n, final Void v) {
		// TODO
		n.acceptChildren(this, v);
		return null;
	}

	@Override
	public Void visitStringTemplateEntryWithExpression(final KtStringTemplateEntryWithExpression expr, final Void v) {
		// TODO
		expr.acceptChildren(this, v);
		return null;
	}

	@Override
	public Void visitBlockStringTemplateEntry(final KtBlockStringTemplateEntry n, final Void v) {
		// TODO
		n.acceptChildren(this, v);
		return null;
	}

	@Override
	public Void visitSimpleNameStringTemplateEntry(final KtSimpleNameStringTemplateEntry n, final Void v) {
		// TODO
		n.acceptChildren(this, v);
		return null;
	}

	@Override
	public Void visitLiteralStringTemplateEntry(final KtLiteralStringTemplateEntry n, final Void v) {
		// TODO
		n.acceptChildren(this, v);
		return null;
	}

	@Override
	public Void visitEscapeStringTemplateEntry(final KtEscapeStringTemplateEntry n, final Void v) {
		// TODO
		n.acceptChildren(this, v);
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
		expressions.push(new ArrayList<Expression>());
		final Expression.Builder eb = Expression.newBuilder();
		expr.acceptChildren(this, v);
		eb.addAllExpressions(expressions.pop());
		switch(expr.getOperationToken().toString()) {
		case "PLUS":
			eb.setKind(Expression.ExpressionKind.OP_ADD);
			break;
		case "MUL":
			eb.setKind(Expression.ExpressionKind.OP_MULT);
			break;
		case "SUB":
			eb.setKind(Expression.ExpressionKind.OP_SUB);
			break;
		case "DIV":
			eb.setKind(Expression.ExpressionKind.OP_DIV);
			break;
		case "MOD":
			eb.setKind(Expression.ExpressionKind.OP_MOD);
			break;
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
		default:
			eb.setKind(Expression.ExpressionKind.OP_ADD);
		}
		expressions.peek().add(eb.build());
		return null;
	}

	public Void visitNameReferenceExpression(final KtNameReferenceExpression nameRef, final Void v) {
		if (!expressions.isEmpty()) {
			final Expression.Builder eb = Expression.newBuilder();
			eb.setKind(Expression.ExpressionKind.VARACCESS);
			eb.setVariable(nameRef.getReferencedName());
			expressions.peek().add(eb.build());
		}
		return null;
	}

	@Override
	public Void visitParenthesizedExpression(final KtParenthesizedExpression expr, final Void v) {
		expressions.push(new ArrayList<Expression>());
		final Expression.Builder eb = Expression.newBuilder();
		expr.acceptChildren(this, v);
		eb.setKind(Expression.ExpressionKind.PAREN);
		eb.addAllExpressions(expressions.pop());
		expressions.peek().add(eb.build());
		return null;
	}

	@Override
	public Void visitProperty(final KtProperty prop, final Void v) {
		final Variable.Builder vb = Variable.newBuilder();

		vb.setName(prop.getNameIdentifier().getText());

		final KtTypeReference typeRef = prop.getTypeReference();
		if (typeRef != null)
			vb.setVariableType(typeFromTypeRef(typeRef));

		modifiers.push(new ArrayList<Modifier>());
		if (prop.getModifierList() != null)
			prop.getModifierList().accept(this, v);
		if (!prop.isVar()) {
			final Modifier.Builder mb = Modifier.newBuilder();
			mb.setKind(Modifier.ModifierKind.FINAL);
			modifiers.peek().add(mb.build());
		}
		vb.addAllModifiers(modifiers.pop());

		expressions.push(new ArrayList<Expression>());
		if (prop.hasInitializer())
			prop.getInitializer().accept(this, v);
		vb.addAllExpressions(expressions.pop());

		fields.peek().add(vb.build());

		if (prop.getGetter() != null)
			prop.getGetter().accept(this, v);
		if (prop.getSetter() != null)
			prop.getSetter().accept(this, v);

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

	@Override
	public Void visitPrimaryConstructor(final KtPrimaryConstructor constructor, final Void v) {
		final Method.Builder mb = Method.newBuilder();

		mb.setName("<init>");

		modifiers.push(new ArrayList<Modifier>());
		modifiers.peek().add(Modifier.newBuilder()
				     .setKind(Modifier.ModifierKind.OTHER)
				     .setOther("primary")
				     .build());
		fields.push(new ArrayList<Variable>());
		expressions.push(new ArrayList<Expression>());
		constructor.acceptChildren(this, v);
		List<Expression> exprs = expressions.pop();
		if (exprs.size() != 0)
			mb.addStatements(Statement.newBuilder()
					 .setKind(Statement.StatementKind.BLOCK)
					 .addAllExpressions(exprs));
                List<Variable> methodFields = fields.pop();
		mb.addAllArguments(methodFields);
		mb.addAllModifiers(modifiers.pop());

		for(Variable var: methodFields)
			fields.peek().add(var);

		methods.peek().add(mb.build());
		return null;
	}

	@Override
	public Void visitNamedFunction(final KtNamedFunction function, final Void v) {
		final Method.Builder mb = Method.newBuilder();

		mb.setName(function.getName());

		modifiers.push(new ArrayList<Modifier>());
		fields.push(new ArrayList<Variable>());
		expressions.push(new ArrayList<Expression>());
		function.acceptChildren(this, v);
		List<Expression> exprs = expressions.pop();
		if (exprs.size() != 0)
			mb.addStatements(Statement.newBuilder()
					 .setKind(Statement.StatementKind.BLOCK)
					 .addAllExpressions(exprs));
		mb.addAllArguments(fields.pop());
		mb.addAllModifiers(modifiers.pop());

		methods.peek().add(mb.build());
		return null;
	}

	@Override
	public Void visitParameter(final KtParameter param, final Void v) {
		final Variable.Builder vb = Variable.newBuilder();

		vb.setName(param.getName());

		if (param.getTypeReference() != null)
			vb.setVariableType(typeFromTypeRef(param.getTypeReference()));

		if (param.getModifierList() != null) {
			modifiers.push(new ArrayList<Modifier>());
			param.getModifierList().accept(this, v);
			vb.addAllModifiers(modifiers.pop());
		}

		fields.peek().add(vb.build());
		return null;
	}

	@Override
	public Void visitCallExpression(final KtCallExpression args, final Void v) {
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
	public Void visitWhileExpression(final KtWhileExpression whileExpr, final Void v) {
		final Statement.Builder sb = Statement.newBuilder();

		sb.setKind(Statement.StatementKind.WHILE);

		expressions.push(new ArrayList<Expression>());
		whileExpr.getCondition().accept(this, v);
		sb.addAllConditions(expressions.pop());

		expressions.push(new ArrayList<Expression>());
		statements.push(new ArrayList<Statement>());
		whileExpr.getBody().accept(this, v);
		sb.addAllExpressions(expressions.pop());
		sb.addAllStatements(statements.pop());

		statements.peek().add(sb.build());
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
