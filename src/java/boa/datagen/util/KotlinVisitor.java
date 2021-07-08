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

// import kotlinx.ast.common.ast.Ast;
// import kotlinx.ast.common.ast.AstWithRawAstKt;
// import kotlinx.ast.common.ast.DefaultAstNode;
// import kotlinx.ast.common.ast.DefaultAstTerminal;
// import kotlinx.ast.common.klass.KlassAnnotation;
// import kotlinx.ast.common.klass.KlassDeclaration;
// import kotlinx.ast.common.klass.KlassIdentifier;
// import kotlinx.ast.common.klass.KlassInheritance;
// import kotlinx.ast.common.klass.KlassModifier;
// import kotlinx.ast.common.klass.KlassString;
// import kotlinx.ast.common.klass.KlassTypeParameter;
// import kotlinx.ast.common.klass.StringComponent;
// import kotlinx.ast.common.klass.StringComponentRaw;
// import kotlinx.ast.common.klass.StringComponentAstNodeExpression;
// import kotlinx.ast.grammar.kotlin.common.summary.Import;
// import kotlinx.ast.grammar.kotlin.common.summary.PackageHeader;

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

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiRecursiveElementVisitor;
import com.intellij.psi.PsiWhiteSpace;

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

	public Namespace getNamespace(KtFile kt) {

		modifiers.push(new ArrayList<Modifier>());
		declarations.push(new ArrayList<Declaration>());
		fields.push(new ArrayList<Variable>());
		methods.push(new ArrayList<Method>());
		statements.push(new ArrayList<Statement>());
		expressions.push(new ArrayList<Expression>());

		b.setName("test");

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


	public Void visitKtElement(KtElement element, Void v) {
		visitElement(element);
		return null;
	}

	@Override
	public void visitElement(final PsiElement element) {
		indent();
		System.out.print(element);
		if (element instanceof org.jetbrains.kotlin.psi.KtPackageDirective)
			this.visitElement((KtPackageDirective) element);
		else if (element instanceof org.jetbrains.kotlin.psi.KtConstantExpression)
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

	public Void visitPackageDirective(final KtPackageDirective directive, Void v) {
		System.out.println("**** Package Directive ****");
		b.setName(directive.getQualifiedName());
		return null;
	}

	@Override
	public void visitWhiteSpace(final PsiWhiteSpace space) {
	}

}
