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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import org.jetbrains.kotlin.psi.*;

import boa.types.Ast.Declaration;
import boa.types.Ast.Expression;
import boa.types.Ast.Method;
import boa.types.Ast.Modifier;
import boa.types.Ast.Namespace;
import boa.types.Ast.PositionInfo;
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

	protected Stack<Expression.ExpressionKind> exprType = new Stack<Expression.ExpressionKind>();

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

	private int indent = 0;

	private void indent() {
		for (int i = 0; i < indent * 2; i++)
			System.out.print(" ");
	}

	@Override
	public Void visitKtElement(final KtElement element, final Void v) {
		if (element instanceof KtOperationReferenceExpression)
			visitOperationReferenceExpression((KtOperationReferenceExpression) element, v);
		else
			visitElement(element);
		return null;
	}

	@Override
	public void visitElement(final PsiElement element) {
		indent();
		System.out.print(element);
		if (element instanceof org.jetbrains.kotlin.psi.KtConstantExpression)
			System.out.print("(" + ((org.jetbrains.kotlin.psi.KtConstantExpression)element).getText() + ")");
		else if (element instanceof org.jetbrains.kotlin.psi.KtBinaryExpression)
			System.out.print("(" + ((org.jetbrains.kotlin.psi.KtBinaryExpression)element).getOperationToken() + ")");
		else if (element instanceof com.intellij.psi.impl.source.tree.LeafPsiElement)
			System.out.print("(" + ((com.intellij.psi.impl.source.tree.LeafPsiElement)element).getText() + ")");
		System.out.println(" - " + element.getClass());
		indent++;
		element.acceptChildren(this);
		indent--;
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
	public Void visitPackageDirective(final KtPackageDirective directive, final Void v) {
		b.setName(directive.getQualifiedName());
		return null;
	}

	@Override
	public Void visitImportDirective(final KtImportDirective directive, final Void v) {
		StringBuilder sb = new StringBuilder();
		sb.append(directive.getImportedFqName().toString());
		if (directive.isAllUnder()) {
			sb.append(".*");
		}
		if (directive.getAliasName() != null) {
			sb.append(" as " + directive.getAliasName());
		}
		b.addImports(sb.toString());
		return null;
	}

	@Override
	public Void visitAnnotationEntry(final KtAnnotationEntry entry, final Void v) {
		System.out.println("ANN ENTRY");
		entry.acceptChildren(this, v);
		return null;
	}

	@Override
	public Void visitAnnotation(final KtAnnotation annotation, final Void v) {
		System.out.println("ANN");
		annotation.acceptChildren(this, v);
		return null;
	}

	@Override
	public Void visitAnnotationUseSiteTarget(final KtAnnotationUseSiteTarget target, final Void v) {
		System.out.println("ANN TARGET: " + target.getAnnotationUseSiteTarget().toString().toLowerCase());
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
	// visitParameter
	// visitSuperTypeList
	// visitSuperTypeListEntry
	// visitDelegatedSuperTypeEntry
	// visitSuperTypeCallEntry
	// visitSuperTypeEntry
	// visitConstructorDelegationCall
	// visitPropertyDelegate
	// visitTypeReference
	// visiytValueArgumentList
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
	// visitBreakExpression
	// visitContinueExpression
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
		eb.setKind(exprType.pop());
		expressions.peek().add(eb.build());
		return null;
	}

	public Void visitOperationReferenceExpression(final KtOperationReferenceExpression opRef, final Void v) {
		switch (opRef.getReferencedNameElement().getText()) {
		case "+":
			exprType.push(Expression.ExpressionKind.OP_ADD);
			break;
		case "*":
			exprType.push(Expression.ExpressionKind.OP_MULT);
			break;
		case "-":
			exprType.push(Expression.ExpressionKind.OP_SUB);
			break;
		case "/":
			exprType.push(Expression.ExpressionKind.OP_DIV);
			break;
		case "%":
			exprType.push(Expression.ExpressionKind.OP_MOD);
			break;
			// TODO: Check if there are other options
		default:
			exprType.push(Expression.ExpressionKind.OP_ADD);
			break;
		}
		return null;
	}

	public Void visitNameReferenceExpression(final KtNameReferenceExpression nameRef, final Void v) {
		final Expression.Builder eb = Expression.newBuilder();
		eb.setKind(Expression.ExpressionKind.VARACCESS);
		eb.setVariable(nameRef.getReferencedName());
		expressions.peek().add(eb.build());
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
		expressions.push(new ArrayList<Expression>());
		modifiers.push(new ArrayList<Modifier>());
		if (!prop.isVar()) {
			final Modifier.Builder mb = Modifier.newBuilder();
			mb.setKind(Modifier.ModifierKind.CONSTANT);
			modifiers.peek().add(mb.build());
		}
		final Variable.Builder vb = Variable.newBuilder();

		if (prop.hasInitializer()) {
			prop.getInitializer().accept(this, v);
		}

		KtTypeReference typeRef = prop.getTypeReference();
		if (typeRef != null)
			vb.setVariableType(typeFromTypeRef(typeRef));

		vb.setName(prop.getNameIdentifier().getText());
		vb.addAllModifiers(modifiers.pop());
		vb.addAllExpressions(expressions.pop());

		fields.peek().add(vb.build());
		return null;
	}

	@Override
	public Void visitClass(final KtClass klass, final Void v) {
		Declaration.Builder db = Declaration.newBuilder();
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

	// Things to ignore/pass through
	@Override
	public void visitWhiteSpace(final PsiWhiteSpace space) {
	}

	// Utility methods
	private Type typeFromTypeRef(final KtTypeReference type) {
		Type.Builder tb = Type.newBuilder();
		tb.setName(type.getText());
		tb.setKind(TypeKind.OTHER);
		return tb.build();
	}
}
