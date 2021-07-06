/*
 * Copyright 2021, Robert Dyer
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
		startvisit(n);
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
		b.setName("");
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
			System.err.println("unknown kotlin node: " + n.getClass());
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

	private int indent = 0;
	private void indent() {
		for (int i = 0; i < indent; i++)
			System.out.print(" ");
	}

	protected void visit(final PackageHeader n) {
		b.setName(getIdentifier(n.getIdentifier()));
		//b.addAllModifiers(visitAnnotationsList(pkg.annotations()));
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
		indent();
		System.out.print("KlassDeclaration(" + n.getKeyword() + " ");
		System.out.print(getIdentifier(n.getIdentifier()));
		System.out.println(")");
		indent += 2;
		startvisit(n.getChildren());
		indent -= 2;
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
		for (final Ast i : n.getChildren())
			startvisit(i);
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
		indent();
		System.out.println(n.getDescription());
		for (final Ast a : n.getChildren()) {
			indent += 2;
			startvisit(a);
			indent -= 2;
		}
	}

	protected void visit(final DefaultAstTerminal n) {
		if (n.getChannel().getName() == "HIDDEN") return;
		indent();
		System.out.format("%s >>>%s<<< (%s)\n", n.getDescription(), n.getText(), n.getChannel().getName());
	}
}
