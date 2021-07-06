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
import boa.types.Ast.Method;
import boa.types.Ast.Modifier;
import boa.types.Ast.Namespace;
import boa.types.Ast.PositionInfo;
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
	protected Stack<List<boa.types.Ast.Declaration>> declarations = new Stack<List<boa.types.Ast.Declaration>>();
	protected Stack<boa.types.Ast.Modifier> modifiers = new Stack<boa.types.Ast.Modifier>();
	protected Stack<boa.types.Ast.Expression> expressions = new Stack<boa.types.Ast.Expression>();
	protected Stack<List<boa.types.Ast.Variable>> fields = new Stack<List<boa.types.Ast.Variable>>();
	protected Stack<List<boa.types.Ast.Method>> methods = new Stack<List<boa.types.Ast.Method>>();
	protected Stack<List<boa.types.Ast.Statement>> statements = new Stack<List<boa.types.Ast.Statement>>();

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
		declarations.push(new ArrayList<Declaration>());
		fields.push(new ArrayList<Variable>());

		startvisit(n);

		final List<Declaration> decls = declarations.pop();
		for (final Declaration d : decls)
			b.addDeclarations(d);

		final List<Variable> fs = fields.pop();
		for (final Variable v : fs)
			b.addVariables(v);

		return b.build();
	}

	public boa.types.Ast.Expression getExpression() {
		return expressions.pop();
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

	private int indent = 0;
	private void indent() {
		for (int i = 0; i < indent; i++)
			System.out.print(" ");
	}

	protected void visit(final PackageHeader n) {
		b.setName(getIdentifier(n.getIdentifier()));
	}

	protected void visit(final Import n) {
		b.addImports(getIdentifier(n.getIdentifier()));
	}

	protected void visit(final KlassIdentifier n) {
		if (n == null) return;
		indent();
		System.out.format("KlassIdentifier(%s)\n", n.getIdentifier());
		indent += 2;
		startvisit(n.getChildren());
		indent -= 2;
	}

	protected void visit(final KlassDeclaration n) {
		switch (n.getKeyword()) {
		case "var":
			visitDeclarationVar(n);
			break;
		case "val":
			visitDeclarationVal(n);
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
		indent += 2;
		startvisit(n.getChildren());
		indent -= 2;
	}

	protected void visitDeclarationVar(final KlassDeclaration n) {
		final boa.types.Ast.Variable.Builder vb = boa.types.Ast.Variable.newBuilder();

		vb.setName(getIdentifier(n.getIdentifier()));

		fields.peek().add(vb.build());
	}

	protected void visitDeclarationVal(final KlassDeclaration n) {
		final boa.types.Ast.Variable.Builder vb = boa.types.Ast.Variable.newBuilder();

		vb.setName(getIdentifier(n.getIdentifier()));

		fields.peek().add(vb.build());
	}

	protected void visitDeclarationClass(final KlassDeclaration n) {
		final boa.types.Ast.Declaration.Builder db = boa.types.Ast.Declaration.newBuilder();

		db.setKind(boa.types.Ast.TypeKind.CLASS);
		db.setName(getIdentifier(n.getIdentifier()));
		db.setFullyQualifiedName(fullyQualified(getIdentifier(n.getIdentifier())));

		for (final KlassModifier m : n.getModifiers())
			visit(m);
		while (!modifiers.isEmpty())
			db.addModifiers(modifiers.pop());

		declarations.push(new ArrayList<Declaration>());
		fields.push(new ArrayList<Variable>());

		startvisit(n.getExpressions());

		db.addAllFields(fields.pop());
		db.addAllNestedDeclarations(declarations.pop());

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
		indent();
		System.out.format("KlassModifier(%s, %s)\n", n.getModifier(), n.getGroup().getGroup());
	}

	protected void visit(final KlassString n) {
		indent();
		System.out.println("KlassString");
		indent += 2;
		startvisitsc(n.getChildren());
		indent -= 2;
	}

	protected void startvisitsc(final List<StringComponent> sc) {
		for (final StringComponent s: sc)
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
		indent();
		System.out.println(sc.getDescription());
	}

	protected void visitsc(final StringComponentAstNodeExpression sc) {
		final Ast expression = sc.getExpression();
		if (expression instanceof KlassIdentifier) {
			indent();
			System.out.println(expression.getDescription());
		} else {
			startvisit(expression);
		}
	}

	protected void visit(final KlassInheritance n) {
		indent();
		System.out.println("KlassInheritance");
		indent += 2;
		startvisit(n.getChildren());
		indent -= 2;
	}

	protected void visit(final KlassAnnotation n) {
		indent();
		System.out.println(n.getAttachments());
		System.out.print("KlassAnnotation(");
		System.out.print(getIdentifier(n.getIdentifier()));
		System.out.println(")");
		indent += 2;
		startvisit(n.getChildren());
		indent -= 2;
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

	protected void visitTypeAlias(DefaultAstNode n) {

	}

	protected void	visitDeclaration(DefaultAstNode n) {

	}

	protected void	visitPrimaryConstructor(DefaultAstNode n) {

	}

	protected void	visitDelegationSpecifiers(DefaultAstNode n) {

	}

	protected void	visitDelegationSpecifier(DefaultAstNode n) {

	}

	protected void	visitConstructorInvocation(DefaultAstNode n) {

	}

	protected void	visitAnnotatedDelegationSpecifier(DefaultAstNode n) {

	}

	protected void	visitExplicitDelegation(DefaultAstNode n) {

	}

	protected void	visitTypeParameters(DefaultAstNode n) {

	}

	protected void	visitTypeParameter(DefaultAstNode n) {

	}

	protected void	visitTypeConstraints(DefaultAstNode n) {

	}

	protected void	visitTypeConstraint(DefaultAstNode n) {

	}

	protected void	visitClassMemberDeclarations(DefaultAstNode n) {

	}

	protected void	visitClassMemberDeclaration(DefaultAstNode n) {

	}

	protected void	visitAnonymousInitializer(DefaultAstNode n) {

	}

	protected void	visitCompanionObject(DefaultAstNode n) {

	}

	protected void	visitFunctionValueParameters(DefaultAstNode n) {

	}

	protected void	visitFunctionValueParameter(DefaultAstNode n) {

	}

	protected void	visitFunctionDeclaration(DefaultAstNode n) {

	}

	protected void	visitFunctionBody(DefaultAstNode n) {

	}

	protected void	visitVariableDeclaration(DefaultAstNode n) {

	}

	protected void	visitMultiVariableDeclaration(DefaultAstNode n) {

	}

	protected void	visitPropertyDeclaration(DefaultAstNode n) {

	}

	protected void	visitPropertyDelegate(DefaultAstNode n) {

	}

	protected void	visitGetter(DefaultAstNode n) {

	}

	protected void	visitSetter(DefaultAstNode n) {

	}

	protected void	visitParametersWithOptionalType(DefaultAstNode n) {

	}

	protected void	visitParameterWithOptionalType(DefaultAstNode n) {

	}

	protected void	visitParameter(DefaultAstNode n) {

	}

	protected void	visitObjectDeclaration(DefaultAstNode n) {

	}

	protected void	visitSecondaryConstructor(DefaultAstNode n) {

	}

	protected void	visitConstructorDelegationCall(DefaultAstNode n) {

	}

	protected void	visitEnumClassBody(DefaultAstNode n) {

	}

	protected void visitEnumEntries(DefaultAstNode n) {

	}

	protected void visitEnumEntry(DefaultAstNode n) {

	}

	protected void	visitType(DefaultAstNode n) {

	}

	protected void	visitTypeReference(DefaultAstNode n) {

	}

	protected void	visitNullableType(DefaultAstNode n) {

	}

	protected void	visitQuest(DefaultAstNode n) {

	}

	protected void	visitUserType(DefaultAstNode n) {

	}

	protected void	visitSimpleUserType(DefaultAstNode n) {

	}

	protected void	visitTypeProjection(DefaultAstNode n) {

	}

	protected void	visitTypeProjectionModifiers(DefaultAstNode n) {

	}

	protected void	visitTypeProjectionModifier(DefaultAstNode n) {

	}

	protected void	visitFunctionType(DefaultAstNode n) {

	}

	protected void	visitFunctionTypeParameters(DefaultAstNode n) {

	}

	protected void	visitParenthesizedType(DefaultAstNode n) {

	}

	protected void	visitReceiverType(DefaultAstNode n) {

	}

	protected void	visitParenthesizedUserType(DefaultAstNode n) {

	}

	protected void	visitStatements(DefaultAstNode n) {

	}

	protected void	visitStatement(DefaultAstNode n) {

	}

	protected void visitLabel(DefaultAstNode n) {

	}

	protected void visitControlStructureBody(DefaultAstNode n) {

	}

	protected void visitBlock(DefaultAstNode n) {

	}

	protected void visitLoopStatement(DefaultAstNode n) {

	}

	protected void visitForStatement(DefaultAstNode n) {

	}

	protected void visitWhileStatement(DefaultAstNode n) {

	}

	protected void visitDoWhileStatement(DefaultAstNode n) {

	}

	protected void visitAssignment(DefaultAstNode n) {

	}

	protected void visitSemi(DefaultAstNode n) {

	}

	protected void visitSemis(DefaultAstNode n) {

	}

	protected void visitExpression(DefaultAstNode n) {

	}

	protected void visitDisjunction(DefaultAstNode n) {

	}

	protected void visitConjunction(DefaultAstNode n) {

	}

	protected void visitEquality(DefaultAstNode n) {

	}

	protected void visitComparison(DefaultAstNode n) {

	}

	protected void visitGenericCallLikeComparison(DefaultAstNode n) {

	}

	protected void visitInfixOperation(DefaultAstNode n) {

	}

	protected void visitElvisExpression(DefaultAstNode n) {

	}

	protected void visitElvis(DefaultAstNode n) {

	}

	protected void visitInfixFunctionCall(DefaultAstNode n) {

	}

	protected void visitRangeExpression(DefaultAstNode n) {

	}

	protected void visitAdditiveExpression(DefaultAstNode n) {

	}

	protected void visitMultiplicativeExpression(DefaultAstNode n) {

	}

	protected void visitAsExpression(DefaultAstNode n) {

	}

	protected void visitPrefixUnaryExpression(DefaultAstNode n) {

	}

	protected void visitUnaryPrefix(DefaultAstNode n) {

	}

	protected void visitPostfixUnaryExpression(DefaultAstNode n) {

	}

	protected void visitPostfixUnarySuffix(DefaultAstNode n) {

	}

	protected void visitDirectlyAssignableExpression(DefaultAstNode n) {

	}

	protected void visitParenthesizedDirectlyAssignableExpression(DefaultAstNode n) {

	}

	protected void visitAssignableExpression(DefaultAstNode n) {

	}

	protected void visitParenthesizedAssignableExpression(DefaultAstNode n) {

	}

	protected void visitAssignableSuffix(DefaultAstNode n) {

	}

	protected void visitIndexingSuffix(DefaultAstNode n) {

	}

	protected void visitNavigationSuffix(DefaultAstNode n) {

	}

	protected void visitCallSuffix(DefaultAstNode n) {

	}

	protected void visitAnnotatedLambda(DefaultAstNode n) {

	}

	protected void visitTypeArguments(DefaultAstNode n) {

	}

	protected void visitValueArguments(DefaultAstNode n) {

	}

	protected void visitValueArgument(DefaultAstNode n) {

	}

	protected void visitPrimaryExpression(DefaultAstNode n) {

	}

	protected void visitParenthesizedExpression(DefaultAstNode n) {

	}

	protected void visitCollectionLiteral(DefaultAstNode n) {

	}

	protected void visitLiteralConstant(DefaultAstNode n) {

	}

	protected void visitStringLiteral(DefaultAstNode n) {

	}

	protected void visitLineStringLiteral(DefaultAstNode n) {

	}

	protected void visitMultiLineStringLiteral(DefaultAstNode n) {

	}

	protected void visitLineStringContent(DefaultAstNode n) {

	}

	protected void visitLineStringExpression(DefaultAstNode n) {

	}

	protected void visitMultiLineStringContent(DefaultAstNode n) {

	}

	protected void visitMultiLineStringExpression(DefaultAstNode n) {

	}

	protected void visitLambdaLiteral(DefaultAstNode n) {

	}

	protected void visitLambdaParameters(DefaultAstNode n) {

	}

	protected void visitLambdaParameter(DefaultAstNode n) {

	}

	protected void visitAnonymousFunction(DefaultAstNode n) {

	}

	protected void visitFunctionLiteral(DefaultAstNode n) {

	}

	protected void visitObjectLiteral(DefaultAstNode n) {

	}

	protected void visitThisExpression(DefaultAstNode n) {

	}

	protected void visitSuperExpression(DefaultAstNode n) {

	}

	protected void visitIfExpression(DefaultAstNode n) {

	}

	protected void visitWhenSubject(DefaultAstNode n) {

	}

	protected void visitWhenExpression(DefaultAstNode n) {

	}

	protected void visitWhenEntry(DefaultAstNode n) {

	}

	protected void visitWhenCondition(DefaultAstNode n) {

	}

	protected void visitRangeTest(DefaultAstNode n) {

	}

	protected void visitTypeTest(DefaultAstNode n) {

	}

	protected void visitTryExpression(DefaultAstNode n) {

	}

	protected void visitCatchBlock(DefaultAstNode n) {

	}

	protected void visitFinallyBlock(DefaultAstNode n) {

	}

	protected void visitJumpExpression(DefaultAstNode n) {

	}

	protected void visitCallableReference(DefaultAstNode n) {

	}

	protected void visitAssignmentAndOperator(DefaultAstNode n) {

	}

	protected void visitEqualityOperator(DefaultAstNode n) {

	}

	protected void visitComparisonOperator(DefaultAstNode n) {

	}

	protected void visitInOperator(DefaultAstNode n) {

	}

	protected void visitIsOperator(DefaultAstNode n) {

	}

	protected void visitAdditiveOperator(DefaultAstNode n) {

	}

	protected void visitMultiplicativeOperator(DefaultAstNode n) {

	}

	protected void visitAsOperator(DefaultAstNode n) {

	}

	protected void visitPrefixUnaryOperator(DefaultAstNode n) {

	}

	protected void visitPostfixUnaryOperator(DefaultAstNode n) {

	}

	protected void visitExcl(DefaultAstNode n) {

	}

	protected void visitMemberAccessOperator(DefaultAstNode n) {

	}

	protected void visitSafeNav(DefaultAstNode n) {

	}

	protected void visitModifiers(DefaultAstNode n) {

	}

	protected void visitParameterModifiers(DefaultAstNode n) {

	}

	protected void visitModifier(DefaultAstNode n) {

	}

	protected void visitTypeModifiers(DefaultAstNode n) {

	}

	protected void visitTypeModifier(DefaultAstNode n) {

	}

	protected void visitClassModifier(DefaultAstNode n) {

	}

	protected void visitMemberModifier(DefaultAstNode n) {

	}

	protected void visitVisibilityModifier(DefaultAstNode n) {

	}

	protected void visitVarianceModifier(DefaultAstNode n) {

	}

	protected void visitTypeParameterModifiers(DefaultAstNode n) {

	}

	protected void visitTypeParameterModifier(DefaultAstNode n) {

	}

	protected void visitFunctionModifier(DefaultAstNode n) {

	}

	protected void visitPropertyModifier(DefaultAstNode n) {

	}

	protected void visitInheritanceModifier(DefaultAstNode n) {

	}

	protected void visitParameterModifier(DefaultAstNode n) {

	}

	protected void visitReificationModifier(DefaultAstNode n) {

	}

	protected void visitPlatformModifier(DefaultAstNode n) {

	}

	protected void visitAnnotation(DefaultAstNode n) {

	}

	protected void visitSingleAnnotation(DefaultAstNode n) {

	}

	protected void visitMultiAnnotation(DefaultAstNode n) {

	}

	protected void visitAnnotationUseSiteTarget(DefaultAstNode n) {

	}

	protected void visitUnescapedAnnotation(DefaultAstNode n) {

	}

	protected void visitSimpleIdentifier(DefaultAstNode n) {

	}

	protected void visitIdentifier(DefaultAstNode n) {

	}


	protected void visitFileAnnot(final DefaultAstNode n) {
		boa.types.Ast.Modifier.Builder mb = boa.types.Ast.Modifier.newBuilder();

		mb.setKind(boa.types.Ast.Modifier.ModifierKind.ANNOTATION);
		mb.setAnnotationName(typeName((DefaultAstNode)n.getChildren().get(3)));
		// FIXME doesnt store the values
		//mb.addAnnotationMembers();
		//mb.addAnnotationValues();

		b.addModifiers(mb.build());
	}

	protected void visit(final DefaultAstTerminal n) {
		if (n.getChannel().getName() == "HIDDEN") return;
		indent();
		System.out.format("%s >>>%s<<< (%s)\n", n.getDescription(), n.getText(), n.getChannel().getName());
	}

	protected String getIdentifier(final List<KlassIdentifier> id) {
		boolean first = true;
		String s = "";
		for (final KlassIdentifier k: id) {
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
}
