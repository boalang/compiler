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
import com.intellij.psi.PsiRecursiveElementVisitor;
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
		modifiers.push(new ArrayList<Modifier>());
		declarations.push(new ArrayList<Declaration>());
		fields.push(new ArrayList<Variable>());
		methods.push(new ArrayList<Method>());
		statements.push(new ArrayList<Statement>());
		expressions.push(new ArrayList<Expression>());

		b.setName("");

		kt.accept(this);

		b.addAllExpressions(expressions.pop());
		b.addAllStatements(statements.pop());
		b.addAllMethods(methods.pop());
		b.addAllVariables(fields.pop());
		b.addAllDeclarations(declarations.pop());
		b.addAllModifiers(modifiers.pop());

		return b.build();
	}

	private int indent = 0;

	private void indent() {
		for (int i = 0; i < indent * 2; i++)
			System.out.print(" ");
	}

	public Void visitKtElement(final KtElement element, final Void v) {
		if (element instanceof KtOperationReferenceExpression)
			visitOperationReferenceExpression((KtOperationReferenceExpression) element, v);
		else if (element instanceof KtNameReferenceExpression)
			visitNameReferenceExpression((KtNameReferenceExpression) element, v);
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

	public Void visitPackageDirective(final KtPackageDirective directive, final Void v) {
		b.setName(directive.getQualifiedName());
		return null;
	}

	// Generally visitor methods should be of the form:
	// See also https://github.com/JetBrains/kotlin/blob/92d200e093c693b3c06e53a39e0b0973b84c7ec5/compiler/psi/src/org/jetbrains/kotlin/psi/KtVisitor.java
	// public Void visitElementNameHere(final ElementType name, final Void v) {
	//		 doSomethingHere();
	// 	return null;
	// }

	public Void visitImportDirective(final KtImportDirective directive, final Void v) {
		b.addImports(directive.getImportedFqName().toString());
		return null;
	}

	// TODO
	// visitKtElement
	// visitDeclaration
	// visitClass
	// visitObjectDeclaration
	// visitClassOrObject
	// visitSecondaryConstructor
	// visitPrimaryConstructor
	// visitNamedFunction
	// visitDestructuringDeclaration
	// visitDistructuringDeclarationEntry
	// visitTypeAlias
	// visitKtFile
	// visitScript
	// visitImportAlias
	// visitFileAnnotationList
	// visitClassBody
	// visitModifierList
	// visitAnnotations
	// visitAnnotationEntry
	// visitAnnotationUseSiteTarget
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

	public Void visitConstantExpression(final KtConstantExpression expr, final Void v) {
		final Expression.Builder eb = Expression.newBuilder();
		eb.setKind(Expression.ExpressionKind.LITERAL);
		eb.setLiteral(expr.getText());
		expressions.peek().add(eb.build());
		return null;
	}

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

	public Void visitParenthesizedExpression(final KtParenthesizedExpression expr, final Void v) {
		expressions.push(new ArrayList<Expression>());
		final Expression.Builder eb = Expression.newBuilder();
		expr.acceptChildren(this, v);
		eb.setKind(Expression.ExpressionKind.PAREN);
		eb.addAllExpressions(expressions.pop());
		expressions.peek().add(eb.build());
		return null;
	}

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


	// Things to ignore/pass through
	@Override
	public void visitWhiteSpace(final PsiWhiteSpace space) {
	}

	// TODO: Remove when nolonger including printing
	public Void visitImportList(final KtImportList l, final Void v) {
		l.acceptChildren(this, v);
		return null;
	}


	// Utility methods
	private Type typeFromTypeRef(KtTypeReference type) {
		Type.Builder tb = Type.newBuilder();
		tb.setName(type.getText());
                tb.setKind(TypeKind.OTHER);
		return tb.build();
	}

}