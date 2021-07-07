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

import kotlinx.ast.common.ast.Ast;
import kotlinx.ast.common.ast.DefaultAstNode;
import kotlinx.ast.common.ast.DefaultAstTerminal;
import kotlinx.ast.common.klass.KlassIdentifier;
import kotlinx.ast.common.klass.KlassDeclaration;
import kotlinx.ast.common.klass.KlassModifier;
import kotlinx.ast.common.klass.KlassInheritance;
import kotlinx.ast.common.klass.KlassString;
import kotlinx.ast.common.klass.KlassAnnotation;
import kotlinx.ast.common.klass.StringComponent;
import kotlinx.ast.common.klass.StringComponentRaw;
import kotlinx.ast.common.klass.StringComponentAstNodeExpression;
import kotlinx.ast.grammar.kotlin.common.summary.Import;
import kotlinx.ast.grammar.kotlin.common.summary.PackageHeader;

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
public class KotlinVisitor {
	protected List<Ast> root = null;

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

	public Namespace getNamespaces(final List<Ast> n) {
		root = n;

		modifiers.push(new ArrayList<Modifier>());
		declarations.push(new ArrayList<Declaration>());
		fields.push(new ArrayList<Variable>());
		methods.push(new ArrayList<Method>());
		statements.push(new ArrayList<Statement>());
		expressions.push(new ArrayList<Expression>());

		startvisit(n);

        b.addAllExpressions(expressions.pop());
        b.addAllStatements(statements.pop());
        b.addAllMethods(methods.pop());
        b.addAllVariables(fields.pop());
        b.addAllDeclarations(declarations.pop());
        b.addAllModifiers(modifiers.pop());

		return b.build();
	}

	public void startvisit(final List<Ast> n) {
		for (final Ast ast : n)
			startvisit(ast);
	}

	public void startvisit(final Ast n) {
		if (n instanceof PackageHeader)
			visit((PackageHeader) n);
		else if (n instanceof Import)
			visit((Import) n);
		else if (n instanceof KlassDeclaration)
			visit((KlassDeclaration) n);
		else if (n instanceof KlassModifier)
			visit((KlassModifier) n);
		else if (n instanceof KlassInheritance)
			visit((KlassInheritance) n);
		else if (n instanceof KlassIdentifier)
			visit((KlassIdentifier) n);
		else if (n instanceof KlassString)
			visit((KlassString) n);
		else if (n instanceof KlassIdentifier)
			visit((KlassIdentifier) n);
		else if (n instanceof KlassAnnotation)
			visit((KlassAnnotation) n);
		else if (n instanceof DefaultAstNode)
			visit((DefaultAstNode) n);
		else if (n instanceof DefaultAstTerminal)
			visit((DefaultAstTerminal) n);
		else
			System.out.println("unknown kotlin node: " + n.getClass());
	}

	protected void visit(final PackageHeader n) {
		b.setName(getIdentifier(n.getIdentifier()));
	}

	protected void visit(final Import n) {
		b.addImports(getIdentifier(n.getIdentifier()));
	}

	protected void visit(final KlassIdentifier n) {

	}

	protected void visit(final KlassDeclaration n) {
		switch (n.getKeyword()) {
		case "var":
			visitDeclarationVar(n, false);
			break;
		case "val":
			visitDeclarationVar(n, true);
			break;
		case "class":
			visitDeclarationClass(n);
			break;
		case "interface":
			visitDeclarationInterface(n);
			break;
		case "companion object":
			visitDeclarationCompanionObject(n);
			break;
		case "object":
			visitDeclarationObject(n);
			break;
		case "constructor":
			visitDeclarationConstructor(n);
			break;
		case "argument":
			visitDeclarationArgument(n);
			break;
		case "parameter":
			visitDeclarationParameter(n);
			break;
		default:
			System.out.println("unknown kotlin declaration: " + n.getKeyword());
		}
	}

	protected void visitDeclarationVar(final KlassDeclaration n, final boolean isFinal) {
		final Variable.Builder vb = Variable.newBuilder();

		vb.setName(getIdentifier(n.getIdentifier()));

		if (n.getType().size() > 0)
			vb.setVariableType(buildType(n.getType()));

		// for val, mark it final/const
		if (isFinal)
			vb.addModifiers(Modifier.newBuilder()
									.setKind(Modifier.ModifierKind.CONSTANT)
									.build());

		modifiers.push(new ArrayList<Modifier>());
		for (final KlassModifier m : n.getModifiers())
			visit(m);
		vb.addAllModifiers(modifiers.pop());

		for (final Ast ex : n.getExpressions()) {
						expressions.push(new ArrayList<Expression>());
			startvisit(ex);
			vb.addAllExpressions(expressions.pop());
		}

		fields.peek().add(vb.build());
	}

	protected void visitDeclarationClass(final KlassDeclaration n) {
		final Declaration.Builder db = Declaration.newBuilder();

		db.setKind(TypeKind.CLASS);
		db.setName(getIdentifier(n.getIdentifier()));
		db.setFullyQualifiedName(fullyQualified(getIdentifier(n.getIdentifier())));

		modifiers.push(new ArrayList<Modifier>());
		for (final KlassModifier m : n.getModifiers())
			visit(m);
		db.addAllModifiers(modifiers.pop());

		declarations.push(new ArrayList<Declaration>());
		fields.push(new ArrayList<Variable>());

		startvisit(n.getExpressions());

		db.addAllFields(fields.pop());
		db.addAllNestedDeclarations(declarations.pop());
	/* TODO
	repeated Type generic_parameters = 4;
	repeated Type parents = 5;
	repeated Method methods = 6;
	repeated Declaration nested_declarations = 8;
	optional int32 declaring_type = 15;
	repeated Statement statements = 16;
	*/

		declarations.peek().add(db.build());
	}

	protected void visitDeclarationInterface(final KlassDeclaration n) {

	}

	protected void visitDeclarationCompanionObject(final KlassDeclaration n) {

	}

	protected void visitDeclarationObject(final KlassDeclaration n) {

	}

	protected void visitDeclarationConstructor(final KlassDeclaration n) {

	}

	protected void visitDeclarationArgument(final KlassDeclaration n) {

	}

	protected void visitDeclarationParameter(final KlassDeclaration n) {

	}

	protected void visit(final KlassModifier n) {
		Modifier.Builder mb = Modifier.newBuilder();

		switch (n.getGroup().getGroup()) {
		case "inheritanceModifier":
			switch (n.getModifier()) {
			case "abstract":
				mb.setKind(Modifier.ModifierKind.ABSTRACT);
				break;

			case "open":
				mb.setKind(Modifier.ModifierKind.OTHER);
				mb.setOther(n.getModifier());
				break;

			case "final":
				mb.setKind(Modifier.ModifierKind.FINAL);
				break;

			default:
				System.out.println("unknown visibility modifier: " + n.getModifier());
				break;
			}
			break;

		case "classModifier":
			switch (n.getModifier()) {
			case "annotation":
				mb.setKind(Modifier.ModifierKind.ANNOTATION);
				break;

			case "enum":
			case "value":
			case "inner":
			case "data":
			case "sealed":
				mb.setKind(Modifier.ModifierKind.OTHER);
				mb.setOther(n.getModifier());
				break;

			default:
				System.out.println("unknown visibility modifier: " + n.getModifier());
				break;
			}
			break;

		case "visibilityModifier":
			mb.setKind(Modifier.ModifierKind.VISIBILITY);
			switch (n.getModifier()) {
			case "public":
				mb.setVisibility(Modifier.Visibility.PUBLIC);
				break;

			case "protected":
				mb.setVisibility(Modifier.Visibility.PROTECTED);
				break;

			case "private":
				mb.setVisibility(Modifier.Visibility.PRIVATE);
				break;

			case "internal":
				mb.setVisibility(Modifier.Visibility.INTERNAL);
				break;

			default:
				System.out.println("unknown visibility modifier: " + n.getModifier());
				break;
			}
			break;

		case "typeModifier":
		case "memberModifier":
		case "varianceModifier":
		case "functionModifier":
		case "propertyModifier":
		case "parameterModifier":
		case "reificationModifier":
		case "platformModifier":
			mb.setKind(Modifier.ModifierKind.OTHER);
			mb.setOther(n.getModifier());
			break;

		default:
			System.out.println("unknown modifier group: " + n.getGroup().getGroup());
			break;
		}

		modifiers.peek().add(mb.build());
	}

	protected void visit(final KlassString n) {

	}

	protected void startvisitsc(final List<StringComponent> sc) {
		for (final StringComponent s : sc)
			visitsc(s);
	}

	protected void visitsc(final StringComponent sc) {
		if (sc instanceof StringComponentRaw)
			visitsc((StringComponentRaw) sc);
		else if (sc instanceof StringComponentAstNodeExpression)
			visitsc((StringComponentAstNodeExpression) sc);
		else
			System.out.println("unknown: "+ sc.getClass());
	}

	protected void visitsc(final StringComponentRaw sc) {

	}

	protected void visitsc(final StringComponentAstNodeExpression sc) {

	}

	protected void visit(final KlassInheritance n) {

	}

	protected void visit(final KlassAnnotation n) {

	}

	protected void visit(final DefaultAstNode n) {
		switch (n.getDescription()) {
		case "fileAnnotation":
			visitFileAnnot(n);
			break;
		case "typeAlias":
			visitTypeAlias(n);
			break;
		case "declaration":
			visitDeclaration(n);
			break;
		case "primaryConstructor":
			visitPrimaryConstructor(n);
			break;
		case "delegationSpecifiers":
			visitDelegationSpecifiers(n);
			break;
		case "delegationSpecifier":
			visitDelegationSpecifier(n);
			break;
		case "constructorInvocation":
			visitConstructorInvocation(n);
			break;
		case "annotatedDelegationSpecifier":
			visitAnnotatedDelegationSpecifier(n);
			break;
		case "explicitDelegation":
			visitExplicitDelegation(n);
			break;
		case "typeParameters":
			visitTypeParameters(n);
			break;
		case "typeParameter":
			visitTypeParameter(n);
			break;
		case "typeConstraints":
			visitTypeConstraints(n);
			break;
		case "typeConstraint":
			visitTypeConstraint(n);
			break;
		case "classMemberDeclarations":
			visitClassMemberDeclarations(n);
			break;
		case "classMemberDeclaration":
			visitClassMemberDeclaration(n);
			break;
		case "anonymousInitializer":
			visitAnonymousInitializer(n);
			break;
		case "companionObject":
			visitCompanionObject(n);
			break;
		case "functionValueParameters":
			visitFunctionValueParameters(n);
			break;
		case "functionValueParameter":
			visitFunctionValueParameter(n);
			break;
		case "functionDeclaration":
			visitFunctionDeclaration(n);
			break;
		case "functionBody":
			visitFunctionBody(n);
			break;
		case "variableDeclaration":
			visitVariableDeclaration(n);
			break;
		case "multiVariableDeclaration":
			visitMultiVariableDeclaration(n);
			break;
		case "propertyDeclaration":
			visitPropertyDeclaration(n);
			break;
		case "propertyDelegate":
			visitPropertyDelegate(n);
			break;
		case "getter":
			visitGetter(n);
			break;
		case "setter":
			visitSetter(n);
			break;
		case "parametersWithOptionalType":
			visitParametersWithOptionalType(n);
			break;
		case "parameterWithOptionalType":
			visitParameterWithOptionalType(n);
			break;
		case "parameter":
			visitParameter(n);
			break;
		case "objectDeclaration":
			visitObjectDeclaration(n);
			break;
		case "secondaryConstructor":
			visitSecondaryConstructor(n);
			break;
		case "constructorDelegationCall":
			visitConstructorDelegationCall(n);
			break;
		case "enumClassBody":
			visitEnumClassBody(n);
			break;
		case "enumEntries":
			visitEnumEntries(n);
			break;
		case "enumEntry":
			visitEnumEntry(n);
			break;
		case "type":
			visitType(n);
			break;
		case "typeReference":
			visitTypeReference(n);
			break;
		case "nullableType":
			visitNullableType(n);
			break;
		case "quest":
			visitQuest(n);
			break;
		case "userType":
			visitUserType(n);
			break;
		case "simpleUserType":
			visitSimpleUserType(n);
			break;
		case "typeProjection":
			visitTypeProjection(n);
			break;
		case "typeProjectionModifiers":
			visitTypeProjectionModifiers(n);
			break;
		case "typeProjectionModifier":
			visitTypeProjectionModifier(n);
			break;
		case "functionType":
			visitFunctionType(n);
			break;
		case "functionTypeParameters":
			visitFunctionTypeParameters(n);
			break;
		case "parenthesizedType":
			visitParenthesizedType(n);
			break;
		case "receiverType":
			visitReceiverType(n);
			break;
		case "parenthesizedUserType":
			visitParenthesizedUserType(n);
			break;
		case "statements":
			visitStatements(n);
			break;
		case "statement":
			visitStatement(n);
			break;
		case "label":
			visitLabel(n);
			break;
		case "controlStructureBody":
			visitControlStructureBody(n);
			break;
		case "block":
			visitBlock(n);
			break;
		case "loopStatement":
			visitLoopStatement(n);
			break;
		case "forStatement":
			visitForStatement(n);
			break;
		case "whileStatement":
			visitWhileStatement(n);
			break;
		case "doWhileStatement":
			visitDoWhileStatement(n);
			break;
		case "assignment":
			visitAssignment(n);
			break;
		case "semi":
			visitSemi(n);
			break;
		case "semis":
			visitSemis(n);
			break;
		case "expression":
			visitExpression(n);
			break;
		case "disjunction":
			visitDisjunction(n);
			break;
		case "conjunction":
			visitConjunction(n);
			break;
		case "equality":
			visitEquality(n);
			break;
		case "comparison":
			visitComparison(n);
			break;
		case "genericCallLikeComparison":
			visitGenericCallLikeComparison(n);
			break;
		case "infixOperation":
			visitInfixOperation(n);
			break;
		case "elvisExpression":
			visitElvisExpression(n);
			break;
		case "elvis":
			visitElvis(n);
			break;
		case "infixFunctionCall":
			visitInfixFunctionCall(n);
			break;
		case "rangeExpression":
			visitRangeExpression(n);
			break;
		case "additiveExpression":
			visitAdditiveExpression(n);
			break;
		case "multiplicativeExpression":
			visitMultiplicativeExpression(n);
			break;
		case "asExpression":
			visitAsExpression(n);
			break;
		case "prefixUnaryExpression":
			visitPrefixUnaryExpression(n);
			break;
		case "unaryPrefix":
			visitUnaryPrefix(n);
			break;
		case "postfixUnaryExpression":
			visitPostfixUnaryExpression(n);
			break;
		case "postfixUnarySuffix":
			visitPostfixUnarySuffix(n);
			break;
		case "directlyAssignableExpression":
			visitDirectlyAssignableExpression(n);
			break;
		case "parenthesizedDirectlyAssignableExpression":
			visitParenthesizedDirectlyAssignableExpression(n);
			break;
		case "assignableExpression":
			visitAssignableExpression(n);
			break;
		case "parenthesizedAssignableExpression":
			visitParenthesizedAssignableExpression(n);
			break;
		case "assignableSuffix":
			visitAssignableSuffix(n);
			break;
		case "indexingSuffix":
			visitIndexingSuffix(n);
			break;
		case "navigationSuffix":
			visitNavigationSuffix(n);
			break;
		case "callSuffix":
			visitCallSuffix(n);
			break;
		case "annotatedLambda":
			visitAnnotatedLambda(n);
			break;
		case "typeArguments":
			visitTypeArguments(n);
			break;
		case "valueArguments":
			visitValueArguments(n);
			break;
		case "valueArgument":
			visitValueArgument(n);
			break;
		case "primaryExpression":
			visitPrimaryExpression(n);
			break;
		case "parenthesizedExpression":
			visitParenthesizedExpression(n);
			break;
		case "collectionLiteral":
			visitCollectionLiteral(n);
			break;
		case "literalConstant":
			visitLiteralConstant(n);
			break;
		case "stringLiteral":
			visitStringLiteral(n);
			break;
		case "lineStringLiteral":
			visitLineStringLiteral(n);
			break;
		case "multiLineStringLiteral":
			visitMultiLineStringLiteral(n);
			break;
		case "lineStringContent":
			visitLineStringContent(n);
			break;
		case "lineStringExpression":
			visitLineStringExpression(n);
			break;
		case "multiLineStringContent":
			visitMultiLineStringContent(n);
			break;
		case "multiLineStringExpression":
			visitMultiLineStringExpression(n);
			break;
		case "lambdaLiteral":
			visitLambdaLiteral(n);
			break;
		case "lambdaParameters":
			visitLambdaParameters(n);
			break;
		case "lambdaParameter":
			visitLambdaParameter(n);
			break;
		case "anonymousFunction":
			visitAnonymousFunction(n);
			break;
		case "functionLiteral":
			visitFunctionLiteral(n);
			break;
		case "objectLiteral":
			visitObjectLiteral(n);
			break;
		case "thisExpression":
			visitThisExpression(n);
			break;
		case "superExpression":
			visitSuperExpression(n);
			break;
		case "ifExpression":
			visitIfExpression(n);
			break;
		case "whenSubject":
			visitWhenSubject(n);
			break;
		case "whenExpression":
			visitWhenExpression(n);
			break;
		case "whenEntry":
			visitWhenEntry(n);
			break;
		case "whenCondition":
			visitWhenCondition(n);
			break;
		case "rangeTest":
			visitRangeTest(n);
			break;
		case "typeTest":
			visitTypeTest(n);
			break;
		case "tryExpression":
			visitTryExpression(n);
			break;
		case "catchBlock":
			visitCatchBlock(n);
			break;
		case "finallyBlock":
			visitFinallyBlock(n);
			break;
		case "jumpExpression":
			visitJumpExpression(n);
			break;
		case "callableReference":
			visitCallableReference(n);
			break;
		case "assignmentAndOperator":
			visitAssignmentAndOperator(n);
			break;
		case "equalityOperator":
			visitEqualityOperator(n);
			break;
		case "comparisonOperator":
			visitComparisonOperator(n);
			break;
		case "inOperator":
			visitInOperator(n);
			break;
		case "isOperator":
			visitIsOperator(n);
			break;
		case "additiveOperator":
			visitAdditiveOperator(n);
			break;
		case "multiplicativeOperator":
			visitMultiplicativeOperator(n);
			break;
		case "asOperator":
			visitAsOperator(n);
			break;
		case "prefixUnaryOperator":
			visitPrefixUnaryOperator(n);
			break;
		case "postfixUnaryOperator":
			visitPostfixUnaryOperator(n);
			break;
		case "excl":
			visitExcl(n);
			break;
		case "memberAccessOperator":
			visitMemberAccessOperator(n);
			break;
		case "safeNav":
			visitSafeNav(n);
			break;
		case "modifiers":
			visitModifiers(n);
			break;
		case "parameterModifiers":
			visitParameterModifiers(n);
			break;
		case "modifier":
			visitModifier(n);
			break;
		case "typeModifiers":
			visitTypeModifiers(n);
			break;
		case "typeModifier":
			visitTypeModifier(n);
			break;
		case "classModifier":
			visitClassModifier(n);
			break;
		case "memberModifier":
			visitMemberModifier(n);
			break;
		case "visibilityModifier":
			visitVisibilityModifier(n);
			break;
		case "varianceModifier":
			visitVarianceModifier(n);
			break;
		case "typeParameterModifiers":
			visitTypeParameterModifiers(n);
			break;
		case "typeParameterModifier":
			visitTypeParameterModifier(n);
			break;
		case "functionModifier":
			visitFunctionModifier(n);
			break;
		case "propertyModifier":
			visitPropertyModifier(n);
			break;
		case "inheritanceModifier":
			visitInheritanceModifier(n);
			break;
		case "parameterModifier":
			visitParameterModifier(n);
			break;
		case "reificationModifier":
			visitReificationModifier(n);
			break;
		case "platformModifier":
			visitPlatformModifier(n);
			break;
		case "annotation":
			visitAnnotation(n);
			break;
		case "singleAnnotation":
			visitSingleAnnotation(n);
			break;
		case "multiAnnotation":
			visitMultiAnnotation(n);
			break;
		case "annotationUseSiteTarget":
			visitAnnotationUseSiteTarget(n);
			break;
		case "unescapedAnnotation":
			visitUnescapedAnnotation(n);
			break;
		case "simpleIdentifier":
			visitSimpleIdentifier(n);
			break;
		case "identifier":
			visitIdentifier(n);
			break;
		case "importList":
			startvisit(n.getChildren());
			break;
		case "classBody":
			startvisit(n.getChildren());
			break;
		default:
			System.out.println("unknown kotlin DefaultAstNode type: " + n.getDescription());
			startvisit(n.getChildren());
			break;
		}
	}

	protected void visitTypeAlias(final DefaultAstNode n) {

	}

	protected void visitDeclaration(final DefaultAstNode n) {

	}

	protected void visitPrimaryConstructor(final DefaultAstNode n) {

	}

	protected void visitDelegationSpecifiers(final DefaultAstNode n) {

	}

	protected void visitDelegationSpecifier(final DefaultAstNode n) {

	}

	protected void visitConstructorInvocation(final DefaultAstNode n) {

	}

	protected void visitAnnotatedDelegationSpecifier(final DefaultAstNode n) {

	}

	protected void visitExplicitDelegation(final DefaultAstNode n) {

	}

	protected void visitTypeParameters(final DefaultAstNode n) {

	}

	protected void visitTypeParameter(final DefaultAstNode n) {

	}

	protected void visitTypeConstraints(final DefaultAstNode n) {

	}

	protected void visitTypeConstraint(final DefaultAstNode n) {

	}

	protected void visitClassMemberDeclarations(final DefaultAstNode n) {

	}

	protected void visitClassMemberDeclaration(final DefaultAstNode n) {

	}

	protected void visitAnonymousInitializer(final DefaultAstNode n) {

	}

	protected void visitCompanionObject(final DefaultAstNode n) {

	}

	protected void visitFunctionValueParameters(final DefaultAstNode n) {

	}

	protected void visitFunctionValueParameter(final DefaultAstNode n) {

	}

	protected void visitFunctionDeclaration(final DefaultAstNode n) {

	}

	protected void visitFunctionBody(final DefaultAstNode n) {

	}

	protected void visitVariableDeclaration(final DefaultAstNode n) {

	}

	protected void visitMultiVariableDeclaration(final DefaultAstNode n) {

	}

	protected void visitPropertyDeclaration(final DefaultAstNode n) {

	}

	protected void visitPropertyDelegate(final DefaultAstNode n) {

	}

	protected void visitGetter(final DefaultAstNode n) {

	}

	protected void visitSetter(final DefaultAstNode n) {

	}

	protected void visitParametersWithOptionalType(final DefaultAstNode n) {

	}

	protected void visitParameterWithOptionalType(final DefaultAstNode n) {

	}

	protected void visitParameter(final DefaultAstNode n) {

	}

	protected void visitObjectDeclaration(final DefaultAstNode n) {

	}

	protected void visitSecondaryConstructor(final DefaultAstNode n) {

	}

	protected void visitConstructorDelegationCall(final DefaultAstNode n) {

	}

	protected void visitEnumClassBody(final DefaultAstNode n) {

	}

	protected void visitEnumEntries(final DefaultAstNode n) {

	}

	protected void visitEnumEntry(final DefaultAstNode n) {

	}

	protected void visitType(final DefaultAstNode n) {

	}

	protected void visitTypeReference(final DefaultAstNode n) {

	}

	protected void visitNullableType(final DefaultAstNode n) {

	}

	protected void visitQuest(final DefaultAstNode n) {

	}

	protected void visitUserType(final DefaultAstNode n) {

	}

	protected void visitSimpleUserType(final DefaultAstNode n) {

	}

	protected void visitTypeProjection(final DefaultAstNode n) {

	}

	protected void visitTypeProjectionModifiers(final DefaultAstNode n) {

	}

	protected void visitTypeProjectionModifier(final DefaultAstNode n) {

	}

	protected void visitFunctionType(final DefaultAstNode n) {

	}

	protected void visitFunctionTypeParameters(final DefaultAstNode n) {

	}

	protected void visitParenthesizedType(final DefaultAstNode n) {

	}

	protected void visitReceiverType(final DefaultAstNode n) {

	}

	protected void visitParenthesizedUserType(final DefaultAstNode n) {

	}

	protected void visitStatements(final DefaultAstNode n) {

	}

	protected void visitStatement(final DefaultAstNode n) {

	}

	protected void visitLabel(final DefaultAstNode n) {

	}

	protected void visitControlStructureBody(final DefaultAstNode n) {

	}

	protected void visitBlock(final DefaultAstNode n) {

	}

	protected void visitLoopStatement(final DefaultAstNode n) {

	}

	protected void visitForStatement(final DefaultAstNode n) {

	}

	protected void visitWhileStatement(final DefaultAstNode n) {

	}

	protected void visitDoWhileStatement(final DefaultAstNode n) {

	}

	protected void visitAssignment(final DefaultAstNode n) {

	}

	protected void visitSemi(final DefaultAstNode n) {

	}

	protected void visitSemis(final DefaultAstNode n) {

	}

	protected void visitExpression(final DefaultAstNode n) {

	}

	protected void visitDisjunction(final DefaultAstNode n) {

	}

	protected void visitConjunction(final DefaultAstNode n) {

	}

	protected void visitEquality(final DefaultAstNode n) {

	}

	protected void visitComparison(final DefaultAstNode n) {

	}

	protected void visitGenericCallLikeComparison(final DefaultAstNode n) {

	}

	protected void visitInfixOperation(final DefaultAstNode n) {

	}

	protected void visitElvisExpression(final DefaultAstNode n) {

	}

	protected void visitElvis(final DefaultAstNode n) {

	}

	protected void visitInfixFunctionCall(final DefaultAstNode n) {

	}

	protected void visitRangeExpression(final DefaultAstNode n) {

	}

	protected void visitAdditiveExpression(final DefaultAstNode n) {

	}

	protected void visitMultiplicativeExpression(final DefaultAstNode n) {
		final Expression.Builder eb = Expression.newBuilder();

		eb.setKind(Expression.ExpressionKind.OP_MULT);

		expressions.push(new ArrayList<Expression>());
		startvisit(n.getChildren());
		final List<Expression> children = expressions.pop();
		if (children.size() == 1)
			expressions.peek().add(children.get(0));
		eb.addAllExpressions(children);

		expressions.peek().add(eb.build());
	}

	protected void visitAsExpression(final DefaultAstNode n) {

	}

	protected void visitPrefixUnaryExpression(final DefaultAstNode n) {

	}

	protected void visitUnaryPrefix(final DefaultAstNode n) {

	}

	protected void visitPostfixUnaryExpression(final DefaultAstNode n) {

	}

	protected void visitPostfixUnarySuffix(final DefaultAstNode n) {

	}

	protected void visitDirectlyAssignableExpression(final DefaultAstNode n) {

	}

	protected void visitParenthesizedDirectlyAssignableExpression(final DefaultAstNode n) {

	}

	protected void visitAssignableExpression(final DefaultAstNode n) {

	}

	protected void visitParenthesizedAssignableExpression(final DefaultAstNode n) {

	}

	protected void visitAssignableSuffix(final DefaultAstNode n) {

	}

	protected void visitIndexingSuffix(final DefaultAstNode n) {

	}

	protected void visitNavigationSuffix(final DefaultAstNode n) {

	}

	protected void visitCallSuffix(final DefaultAstNode n) {

	}

	protected void visitAnnotatedLambda(final DefaultAstNode n) {

	}

	protected void visitTypeArguments(final DefaultAstNode n) {

	}

	protected void visitValueArguments(final DefaultAstNode n) {

	}

	protected void visitValueArgument(final DefaultAstNode n) {

	}

	protected void visitPrimaryExpression(final DefaultAstNode n) {

	}

	protected void visitParenthesizedExpression(final DefaultAstNode n) {
		final Expression.Builder eb = Expression.newBuilder();

		eb.setKind(Expression.ExpressionKind.PAREN);

		expressions.push(new ArrayList<Expression>());
		startvisit(n.getChildren());
		eb.addAllExpressions(expressions.pop());

		expressions.peek().add(eb.build());
	}

	protected void visitCollectionLiteral(final DefaultAstNode n) {

	}

	protected void visitLiteralConstant(final DefaultAstNode n) {
		final Expression.Builder eb = Expression.newBuilder();

		eb.setKind(Expression.ExpressionKind.LITERAL);
		// Grab the first (and presumably *only*) subexpression, cast as a terminal, and use its text
		eb.setLiteral(((DefaultAstTerminal)n.getChildren().get(0)).getText());

		expressions.peek().add(eb.build());
	}

	protected void visitStringLiteral(final DefaultAstNode n) {

	}

	protected void visitLineStringLiteral(final DefaultAstNode n) {

	}

	protected void visitMultiLineStringLiteral(final DefaultAstNode n) {

	}

	protected void visitLineStringContent(final DefaultAstNode n) {

	}

	protected void visitLineStringExpression(final DefaultAstNode n) {

	}

	protected void visitMultiLineStringContent(final DefaultAstNode n) {

	}

	protected void visitMultiLineStringExpression(final DefaultAstNode n) {

	}

	protected void visitLambdaLiteral(final DefaultAstNode n) {

	}

	protected void visitLambdaParameters(final DefaultAstNode n) {

	}

	protected void visitLambdaParameter(final DefaultAstNode n) {

	}

	protected void visitAnonymousFunction(final DefaultAstNode n) {

	}

	protected void visitFunctionLiteral(final DefaultAstNode n) {

	}

	protected void visitObjectLiteral(final DefaultAstNode n) {

	}

	protected void visitThisExpression(final DefaultAstNode n) {

	}

	protected void visitSuperExpression(final DefaultAstNode n) {

	}

	protected void visitIfExpression(final DefaultAstNode n) {

	}

	protected void visitWhenSubject(final DefaultAstNode n) {

	}

	protected void visitWhenExpression(final DefaultAstNode n) {

	}

	protected void visitWhenEntry(final DefaultAstNode n) {

	}

	protected void visitWhenCondition(final DefaultAstNode n) {

	}

	protected void visitRangeTest(final DefaultAstNode n) {

	}

	protected void visitTypeTest(final DefaultAstNode n) {

	}

	protected void visitTryExpression(final DefaultAstNode n) {

	}

	protected void visitCatchBlock(final DefaultAstNode n) {

	}

	protected void visitFinallyBlock(final DefaultAstNode n) {

	}

	protected void visitJumpExpression(final DefaultAstNode n) {

	}

	protected void visitCallableReference(final DefaultAstNode n) {

	}

	protected void visitAssignmentAndOperator(final DefaultAstNode n) {

	}

	protected void visitEqualityOperator(final DefaultAstNode n) {

	}

	protected void visitComparisonOperator(final DefaultAstNode n) {

	}

	protected void visitInOperator(final DefaultAstNode n) {

	}

	protected void visitIsOperator(final DefaultAstNode n) {

	}

	protected void visitAdditiveOperator(final DefaultAstNode n) {

	}

	protected void visitMultiplicativeOperator(final DefaultAstNode n) {

	}

	protected void visitAsOperator(final DefaultAstNode n) {

	}

	protected void visitPrefixUnaryOperator(final DefaultAstNode n) {

	}

	protected void visitPostfixUnaryOperator(final DefaultAstNode n) {

	}

	protected void visitExcl(final DefaultAstNode n) {

	}

	protected void visitMemberAccessOperator(final DefaultAstNode n) {

	}

	protected void visitSafeNav(final DefaultAstNode n) {

	}

	protected void visitModifiers(final DefaultAstNode n) {

	}

	protected void visitParameterModifiers(final DefaultAstNode n) {

	}

	protected void visitModifier(final DefaultAstNode n) {

	}

	protected void visitTypeModifiers(final DefaultAstNode n) {

	}

	protected void visitTypeModifier(final DefaultAstNode n) {

	}

	protected void visitClassModifier(final DefaultAstNode n) {

	}

	protected void visitMemberModifier(final DefaultAstNode n) {

	}

	protected void visitVisibilityModifier(final DefaultAstNode n) {

	}

	protected void visitVarianceModifier(final DefaultAstNode n) {

	}

	protected void visitTypeParameterModifiers(final DefaultAstNode n) {

	}

	protected void visitTypeParameterModifier(final DefaultAstNode n) {

	}

	protected void visitFunctionModifier(final DefaultAstNode n) {

	}

	protected void visitPropertyModifier(final DefaultAstNode n) {

	}

	protected void visitInheritanceModifier(final DefaultAstNode n) {

	}

	protected void visitParameterModifier(final DefaultAstNode n) {

	}

	protected void visitReificationModifier(final DefaultAstNode n) {

	}

	protected void visitPlatformModifier(final DefaultAstNode n) {

	}

	protected void visitAnnotation(final DefaultAstNode n) {

	}

	protected void visitSingleAnnotation(final DefaultAstNode n) {

	}

	protected void visitMultiAnnotation(final DefaultAstNode n) {

	}

	protected void visitAnnotationUseSiteTarget(final DefaultAstNode n) {

	}

	protected void visitUnescapedAnnotation(final DefaultAstNode n) {

	}

	protected void visitSimpleIdentifier(final DefaultAstNode n) {

	}

	protected void visitIdentifier(final DefaultAstNode n) {

	}

	protected void visitFileAnnot(final DefaultAstNode n) {
		Modifier.Builder mb = Modifier.newBuilder();

		mb.setKind(Modifier.ModifierKind.ANNOTATION);
		DefaultAstNode ast = (DefaultAstNode)n.getChildren().get(3);
		ast = (DefaultAstNode)ast.getChildren().get(0);
		ast = (DefaultAstNode)ast.getChildren().get(0);
		mb.setAnnotationName(typeName(ast));
		// FIXME doesnt store the values
		//mb.addAnnotationMembers();
		//mb.addAnnotationValues();

		modifiers.peek().add(mb.build());
	}

	protected void visit(final DefaultAstTerminal n) {
		if (n.getChannel().getName() == "HIDDEN") return;
		System.out.format("%s >>>%s<<< (%s)\n", n.getDescription(), n.getText(), n.getChannel().getName());
	}

	protected String getIdentifier(final List<KlassIdentifier> id) {
		boolean first = true;
		String s = "";
		for (final KlassIdentifier k : id) {
			if (!first)
				s += ".";
			else
				first = false;
			s += k.getIdentifier();
		}
		return s;
	}

	protected String getIdentifier(final KlassIdentifier id) {
		if (id == null) return "";
		return id.getIdentifier();
	}

	protected String getIdentifier(final DefaultAstTerminal n) {
		return n.getText();
	}

	protected String typeName(final DefaultAstNode n) {
		final List<String> parts = new ArrayList<String>();

		for (final Ast child : n.getChildren())
			if (child instanceof DefaultAstNode)
				parts.add(typeName((DefaultAstNode)child));
			else if (child instanceof DefaultAstTerminal)
				parts.add(typeName((DefaultAstTerminal)child));

		return String.join("", parts);
	}

	protected String typeName(final DefaultAstTerminal n) {
		return n.getText();
	}

	protected String fullyQualified(final String name) {
		if (b.getName().isEmpty())
			return name;
		return b.getName() + "." + name;
	}

	protected Type buildType(final List<KlassIdentifier> t) {
		return Type.newBuilder()
				   .setName(getIdentifier(t))
				   .setKind(TypeKind.OTHER)
				   .build();
	}
}
