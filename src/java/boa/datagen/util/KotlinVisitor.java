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

	protected Stack<List<Expression>> arguments = new Stack<List<Expression>>();

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
		pkg.acceptChildren(this);
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
		annotation.acceptChildren(this, v);
		return null;
	}

	@Override
	public Void visitAnnotationUseSiteTarget(final KtAnnotationUseSiteTarget target, final Void v) {
		//System.err.println("ANN TARGET: " + target.getAnnotationUseSiteTarget().toString().toLowerCase());
		target.acceptChildren(this, v);
		return null;
	}

	// TODO
	// Generally visitor methods should be of the form:
	// 	public Void visitElementNameHere(final ElementType name, final Void v) {
	// 		doSomethingHere();
	// 		return null;
	// 	}
	// See also https://github.com/JetBrains/kotlin/blob/92d200e093c693b3c06e53a39e0b0973b84c7ec5/compiler/psi/src/org/jetbrains/kotlin/psi/KtVisitor.java
	// visitKtElement
	// visitDeclaration
	// visitObjectDeclaration
	// visitClassOrObject
	// visitSecondaryConstructor
	// visitPrimaryConstructor
	// visitNamedFunction
	// visitDestructuringDeclaration
	// visitDistructuringDeclarationEntry
	// visitTypeAlias
	// visitScript
	// visitClassBody
	// visitModifierList
	// visitConstructorCalleeExpression
	// visitTypeParameterList
	// visitTypeParameter
	// visitEnumEntry
	// visitParameterList
	// visitSuperTypeList
	// visitSuperTypeListEntry
	// visitDelegatedSuperTypeEntry
	// visitSuperTypeCallEntry
	// visitSuperTypeEntry
	// visitConstructorDelegationCall
	// visitPropertyDelegate
	// visitTypeReference
	// visitArgument
	// visitExpression
	// visitLoopExpression
	// visitSimpleNameExpression
	// visitReferenceExpression
	// visitLabeledExpression
	// visitPrefixExpression
	// visitPostfixExpression
	// visitUnaryExpression
	// visitReturnExpression
	// visitExpressionWithLabel
	// visitThrowExpression
	// visitIfExpression
	// visitWhenExpression
	// visitCollectionLiteralExpression
	// visitTryExpression
	// visitForExpression
	// visitWhileExpression
	// visitDoWhileExpression
	// visitLambdaExpression
	// visitAnnotatedExpression
	// visitCallExpression
	// visitArrayAccessExpression
	// visitQualifiedExpression
	// visitDoubleColonExpression
	// visitCallableReferenceExpression
	// visitDotQualifiedExpression
	// visitSafeQualifiedExpression
	// visitObjectLiteralExpression
	// visitBlockExpression
	// visitCatchSection
	// visitFinallySection
	// visitTypeArgumentList
	// visitThisExpression
	// visitSuperExpression
	// visitInitializerList
	// visitAnonymousInitializer
	// visitScriptInitializer
	// visitClassInitializer
	// visitPropertyAccessor
	// visitTypeConstraintList
	// visitTypeConstraint
	// visitTypeElement
	// visitUserType
	// visitDynamicType
	// visitFunctionType
	// visitSelfType
	// visitBinaryWithTypeRHSExpression
	// visitStringTemplateExpression
	// visitNamedDeclaration
	// visitNullableType
	// visitTypeProjection
	// visitWhenEntry
	// visitIsExpression
	// visitWhenConditionIsPattern
	// visitWhenConditionInRange
	// visitWhenConditionWithExpression
	// visitStringTemplateEntry
	// visitStringTemplateEntryWithExpression
	// visitBlockStringTemplateEntry
	// visitSimpleNameStringTemplateEntry
	// visitLiteralStringTemplateEntry
	// visitEscapeStringTemplateEntry

	@Override
	public Void visitBreakExpression(final KtBreakExpression expr, final Void v) {
		final Statement.Builder b = Statement.newBuilder();

		b.setKind(Statement.StatementKind.BREAK);

		final String label = expr.getLabelName();
		if (label != null) {
			final Expression.Builder eb = Expression.newBuilder();

			eb.setLiteral(label);
			eb.setKind(Expression.ExpressionKind.LITERAL);

			b.addExpressions(eb.build());
		}

		statements.peek().add(b.build());
		return null;
	}

	@Override
	public Void visitContinueExpression(final KtContinueExpression expr, final Void v) {
		final Statement.Builder b = Statement.newBuilder();

		b.setKind(Statement.StatementKind.CONTINUE);

		final String label = expr.getLabelName();
		if (label != null) {
			final Expression.Builder eb = Expression.newBuilder();

			eb.setLiteral(label);
			eb.setKind(Expression.ExpressionKind.LITERAL);

			b.addExpressions(eb.build());
		}

		statements.peek().add(b.build());
		return null;
	}

	@Override
	public Void visitConstantExpression(final KtConstantExpression expr, final Void v) {
		final Expression.Builder eb = Expression.newBuilder();
		eb.setKind(Expression.ExpressionKind.LITERAL);
		eb.setLiteral(expr.getText());
		expressions.peek().add(eb.build());
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

		mb.setName(constructor.getName());

		modifiers.push(new ArrayList<Modifier>());
		fields.push(new ArrayList<Variable>());
		expressions.push(new ArrayList<Expression>());
		constructor.acceptChildren(this, v);
		mb.addStatements(Statement.newBuilder()
				 .setKind(Statement.StatementKind.BLOCK)
				 .addAllExpressions(expressions.pop()));
		mb.addAllArguments(fields.pop());
		mb.addAllModifiers(modifiers.pop());

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
		mb.addStatements(Statement.newBuilder()
				 .setKind(Statement.StatementKind.BLOCK)
				 .addAllExpressions(expressions.pop()));
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

		modifiers.push(new ArrayList<Modifier>());
		param.getModifierList().accept(this, v);
		vb.addAllModifiers(modifiers.pop());

		fields.peek().add(vb.build());
		return null;
	}

	@Override
	public Void visitCallExpression(final KtCallExpression args, final Void v) {
		final Expression.Builder eb = Expression.newBuilder();

		eb.setKind(Expression.ExpressionKind.METHODCALL);

		arguments.push(new ArrayList<Expression>());
		expressions.push(new ArrayList<Expression>());
		args.acceptChildren(this, v);
		final List<Expression> exprs = expressions.pop();
		final Expression lastExpression = exprs.remove(exprs.size() - 1);
		eb.setMethod(lastExpression.getVariable());
		eb.addAllMethodArgs(arguments.pop());
		eb.addAllExpressions(exprs);

		expressions.peek().add(eb.build());
		return null;
	}

	@Override
	public Void visitValueArgumentList(final KtValueArgumentList args, final Void v) {
		expressions.push(new ArrayList<Expression>());
		args.acceptChildren(this, v);
		arguments.peek().addAll(expressions.pop());
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
