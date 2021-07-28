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
package boa.functions.langmode;

import java.util.List;
import java.util.ArrayList;

import com.intellij.core.CoreFileTypeRegistry;
import com.intellij.lang.LanguageParserDefinitions;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.fileTypes.FileTypeRegistry;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiManager;
import com.intellij.testFramework.LightVirtualFile;
import org.jetbrains.kotlin.idea.KotlinLanguage;
import org.jetbrains.kotlin.idea.KotlinFileType;
import org.jetbrains.kotlin.parsing.KotlinParserDefinition;
import org.jetbrains.kotlin.psi.KtFile;
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreProjectEnvironment;
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreApplicationEnvironment;

import boa.datagen.util.KotlinErrorCheckVisitor;
import boa.datagen.util.KotlinVisitor;
import boa.types.Ast.ASTRoot;
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
 * Boa functions for working with Kotlin ASTs.
 *
 * @author rdyer
 */
public class KotlinLangMode implements LangMode {
	public String type_name(final String s) {
		// FIXME convert to Kotlin
		// first, normalize the string
		final String t = s.replaceAll("<\\s+", "<")
			.replaceAll(",\\s+", ", ")
			.replaceAll("\\s*>\\s*", ">")
			.replaceAll("\\s*&\\s*", " & ")
			.replaceAll("\\s*\\|\\s*", " | ");

		if (!t.contains("."))
			return t;

		/*
		 * Remove qualifiers from anywhere in the string...
		 *
		 * SomeType                               =>  SomeType
		 * foo.SomeType                           =>  SomeType
		 * foo.bar.SomeType                       =>  SomeType
		 * SomeType<T>                            =>  SomeType<T>
		 * SomeType<T, S>                         =>  SomeType<T, S>
		 * SomeType<foo.bar.T, S>                 =>  SomeType<T, S>
		 * SomeType<T, foo.bar.S>                 =>  SomeType<T, S>
		 * foo.bar.SomeType<T, foo.bar.S<bar.Q>>  =>  SomeType<T, S<Q>>
		 * SomeType|foo.Bar                       =>  SomeType|Bar
		 * SomeType&foo.Bar                       =>  SomeType&Bar
		 * foo<T>.bar<T>                          =>  foo<T>.bar<T>
		 */
		return t.replaceAll("[^\\s,<>|&]+\\.([^\\s\\[.,><|&]+)", "$1");
	}

	///////////////////////////////
	// Literal testing functions */
	///////////////////////////////

	/**
	 * Returns <code>true</code> if the expression <code>e</code> is of kind
	 * <code>LITERAL</code> and is an integer literal.
	 *
	 * The test is a simplified grammar, based on the one from:
	 * https://github.com/Kotlin/kotlin-spec/blob/648afef3b9a7fccec7fdaa4aabde6d114bcf9d69/grammar/src/main/antlr/KotlinLexer.g4
	 *
	 * IntegerLiteral
	 *  ([1..9] [0..9_]*)? [0..9] [uU]? [lL]?
	 *
	 * HexLiteral
	 *  '0' [xX] [0-9a-fA-F] ([0-9a-fA-F_]* [0-9a-fA-F])? [uU]? [lL]?
	 *
	 * BinLiteral
	 *  '0' [bB] [01] ([01_]* [01])? [uU]? [lL]?
	 *
	 * If any of these match, it returns <code>true</code>.  Otherwise it
	 * returns <code>false</code>.
	 *
	 * @param e the expression to test
	 * @return true if the expression is an integer literal, otherwise false
	 */
	public boolean isIntLit(final Expression e) throws Exception {
		if (e.getKind() != Expression.ExpressionKind.LITERAL) return false;
		if (!e.hasLiteral()) return false;
		final String lit = e.getLiteral();

		if (lit.matches("^([1..9][0..9_]*)?[0..9][uU]?[lL]?$")) return true;
		if (lit.matches("^0[xX][0-9a-fA-F]([0-9a-fA-F_]*[0-9a-fA-F])?[uU]?[lL]?$")) return true;
		return lit.matches("^0[bB][01]([01_]*[01])?[uU]?[lL]?$");
	}

	/**
	 * Returns <code>true</code> if the expression <code>e</code> is of kind
	 * <code>LITERAL</code> and is a float literal.
	 *
	 * The test is a simplified grammar, based on the one from:
	 * https://github.com/Kotlin/kotlin-spec/blob/648afef3b9a7fccec7fdaa4aabde6d114bcf9d69/grammar/src/main/antlr/KotlinLexer.g4
	 *
	 * FloatLiteral
	 *  ([0..9] ([0..9_]* [0..9])?)? '.' [0..9] ([0..9_]* [0..9])? ([eE] [+-]? [0..9] ([0..9_]* [0..9])?)? [fF]
	 *  [0..9] ([0..9_]* [0..9])? [eE] [+-]? [0..9] ([0..9_]* [0..9])? [fF]
	 *  [0..9] ([0..9_]* [0..9])? [fF]
	 *
	 * DoubleLiteral
	 *  ([0..9] ([0..9_]* [0..9])?)? '.' [0..9] ([0..9_]* [0..9])? ([eE] [+-]? [0..9] ([0..9_]* [0..9])?)?
	 *  [0..9] ([0..9_]* [0..9])? [eE] [+-]? [0..9] ([0..9_]* [0..9])?
	 *
	 * If any of these match, it returns <code>true</code>.  Otherwise it
	 * returns <code>false</code>.
	 *
	 * @param e the expression to test
	 * @return true if the expression is a char literal, otherwise false
	 */
	public boolean isFloatLit(final Expression e) throws Exception {
		if (e.getKind() != Expression.ExpressionKind.LITERAL) return false;
		if (!e.hasLiteral()) return false;
		final String lit = e.getLiteral();

		if (lit.matches("^([0..9]([0..9_]*[0..9])?)?\\.[0..9]([0..9_]*[0..9])?([eE][+-]?[0..9]([0..9_]*[0..9])?)?[fF]$")) return true;
		if (lit.matches("^[0..9]([0..9_]*[0..9])?[eE][+-]?[0..9]([0..9_]*[0..9])?[fF]$")) return true;
		if (lit.matches("^[0..9]([0..9_]*[0..9])?[fF]$")) return true;

		if (lit.matches("^([0..9]([0..9_]*[0..9])?)?\\.[0..9]([0..9_]*[0..9])?([eE][+-]?[0..9]([0..9_]*[0..9])?)?$")) return true;
		return lit.matches("^[0..9]([0..9_]*[0..9])?[eE][+-]?[0..9]([0..9_]*[0..9])?$");
	}

	public boolean isCharLit(final Expression e) throws Exception {
		if (e.getKind() != Expression.ExpressionKind.LITERAL) return false;
		if (!e.hasLiteral()) return false;
		return e.getLiteral().startsWith("'");
	}

	public boolean isStringLit(final Expression e) throws Exception {
		if (e.getKind() != Expression.ExpressionKind.LITERAL) return false;
		if (!e.hasLiteral()) return false;
		return e.getLiteral().startsWith("\"");
	}

	public boolean isTypeLit(final Expression e) throws Exception {
		return false;
	}

	public boolean isBoolLit(final Expression e) throws Exception {
		if (e.getKind() != Expression.ExpressionKind.LITERAL) return false;
		if (!e.hasLiteral()) return false;
		final String lit = e.getLiteral();

		return lit.equals("true") || lit.equals("false");
	}

	public boolean isNullLit(final Expression e) throws Exception {
		if (e.getKind() != Expression.ExpressionKind.LITERAL) return false;
		if (!e.hasLiteral()) return false;
		return e.getLiteral().equals("null");
	}

	public boolean isLiteral(final Expression e, final String lit) throws Exception {
		return e.getKind() == Expression.ExpressionKind.LITERAL && e.hasLiteral() && e.getLiteral().equals(lit);
	}


	int indent = 0;
	private String indent() {
		String s = "";
		for (int i = 0; i < indent; i++)
			s += "    ";
		return s;
	}

	public String prettyprint(final ASTRoot r) {
		if (r == null) return "";

		String s = "";

		for (final Namespace n : r.getNamespacesList())
			s += prettyprint(n);

		return s;
	}

	public String prettyprint(final Namespace n) {
		if (n == null) return "";

		String s = "";

		for (final Modifier m : n.getModifiersList())
			s += prettyprint(m) + "\n";

		if (n.getName().length() > 0)
			s += indent() + "package " + n.getName() + "\n\n";

		for (final String i : n.getImportsList())
			s += indent() + "import " + i + "\n";
		if (n.getImportsList().size() > 0)
			s += "\n";

		for (final Variable v : n.getVariablesList())
			s += prettyprint(v) + "\n";

		for (final Declaration d : n.getDeclarationsList())
			s += prettyprint(d);

		for (final Method m : n.getMethodsList())
			s += prettyprint(m);

		for (final Statement st : n.getStatementsList())
			s += prettyprint(st);

		for (final Expression e : n.getExpressionsList())
			s += prettyprint(e);

		return s;
	}

	public String prettyprint(final Declaration d) {
		// FIXME convert to Kotlin
		if (d == null) return "";

		String s = indent() + prettyprint(d.getModifiersList());

		switch (d.getKind()) {
			case INTERFACE:
				s += "interface " + d.getName();
				s += prettyprintClass(d);
				break;
			case ANONYMOUS:
				s += prettyprintDeclarationBody(d);
				break;
			default:
			case CLASS:
				if (d.getKind() == TypeKind.IMMUTABLE)
					s += "data ";
				if (d.getKind() == TypeKind.ENUM)
					s += "enum ";
				s += "class " + d.getName();
				s += prettyprintClass(d);
				break;
		}

		return s;
	}

	public String prettyprintClass(final Declaration klass) {
		String s = "";

		Method primaryMethod = null;

		final List<Method> knownMethods = new ArrayList<Method>();
		final List<Variable> knownFields = new ArrayList<Variable>();

		for (int i = 0 ; i < klass.getMethodsCount() ; i++) {
			final Method m = klass.getMethods(i);
			boolean isPrimary = false;
			for (int j = 0 ; j < m.getModifiersCount() ; i++) {
				final Modifier mod = m.getModifiers(j);
				if ((mod.getKind() == Modifier.ModifierKind.OTHER) && mod.getOther().equals("primary")) {
					isPrimary = true;
					break;
				}
			}
			if (isPrimary) primaryMethod = m;
			else knownMethods.add(m);
		}

		if(primaryMethod != null) {
			for (int i = 0 ; i < klass.getFieldsCount() ; i++) {
				final Variable v = klass.getFields(i);
				boolean isInPrimary = false;
				for (int j = 0 ; j < primaryMethod.getArgumentsCount() ; j ++) {
					final Variable vv = primaryMethod.getArguments(j);
					if (v.getName().equals(vv.getName())) {
						isInPrimary = true;
						break;
					}
				}
				if (!isInPrimary) knownFields.add(v);
			}
		} else {
			knownFields.addAll(klass.getFieldsList());
		}

		if (primaryMethod != null) {
			s += "(";
			boolean first = true;
			for (int i = 0 ; i < primaryMethod.getArgumentsCount(); i++) {
				if (i > 0) s += ", ";
				s += prettyprint(primaryMethod.getArguments(i));
			}
			s += ")";
		}

		if (klass.getGenericParametersCount() > 0) {
			s += "<";
			for (int i = 0; i < klass.getGenericParametersCount(); i++) {
				if (i != 0) s += ", ";
				s += prettyprint(klass.getGenericParameters(i));
			}
			s += ">";
		}
		if (klass.getParentsCount() > 0) {
			s += " : ";
			List<Statement> statementsToSearch = null;
			if (primaryMethod != null) {
				if (primaryMethod.getStatementsCount() > 0) {
					if (primaryMethod.getStatementsList().get(0).getKind() == Statement.StatementKind.BLOCK)
						statementsToSearch = primaryMethod.getStatementsList().get(0).getStatementsList();
					else
						statementsToSearch = primaryMethod.getStatementsList();
				}
			}
			for (int i = 0; i < klass.getParentsCount(); i++) {
				if (i > 0) s += ", ";
				s += prettyprint(klass.getParents(i));
				if (statementsToSearch != null) {
					for (final Statement st : statementsToSearch) {
						if ((st.getKind() == Statement.StatementKind.EXPRESSION)
								&& (st.getExpressionsList().get(0).getKind() == Expression.ExpressionKind.METHODCALL)) {
							final Expression expr = st.getExpressionsList().get(0);
							if (expr.getMethod().equals(klass.getParents(i).getName())) {
								s += "(";
								for (int j = 0 ; j < expr.getMethodArgsCount() ; j++) {
									if (j > 0) s += ", ";
									s += prettyprint(expr.getMethodArgs(j));
								}
								s += ")";
								break;
							}
						}
					}
				}
			}
		}
		if ((klass.getFieldsCount() > 0) || (klass.getMethodsList().size() > 0) || (klass.getNestedDeclarationsList().size() > 0)) {
			s += " {\n";
			indent++;

			for (final Variable field : knownFields) {
				s += indent() + prettyprint(field) + "\n";
			}

			for (final Method method : knownMethods) {
				s += prettyprint(method) + "\n";
			}

			for (final Declaration decl : klass.getNestedDeclarationsList()) {
				s += indent() + prettyprint(decl) + "\n";
			}

			s += "}\n";
			indent--;
		}

		return s;
	}

	public String prettyprintDeclarationBody(final Declaration d) {
		String s = " {\n";
		indent++;
		for (int i = 0; i < d.getFieldsCount(); i++) {
			s += indent() + prettyprint(d.getFieldsList().get(i));
			s += (!d.getFieldsList().get(i).hasVariableType()
					&& i < d.getFieldsCount() - 1
					&& !d.getFieldsList().get(i + 1).hasVariableType()) ? ",\n" : "\n";
		}
		for (final Method m : d.getMethodsList())
			s += m.getName().equals("<init>") ? prettyprint(m).replace(" <init>", d.getName()) : prettyprint(m);
		for (final Declaration d2 : d.getNestedDeclarationsList())
			s += prettyprint(d2);

		indent--;

		s += indent() + "}\n";

		return s;
	}

	public String prettyprint(final Type t) {
		// FIXME convert to Kotlin
		if (t == null) return "";

		String s = t.getName();

		if (t.getKind() == TypeKind.DELEGATED) {
			s += " by " + prettyprint(t.getDelegate());
		}
		return s;
	}

	public String prettyprint(final Method m) {
		// FIXME convert to Kotlin
		if (m == null) return "";
		String s = indent() + prettyprint(m.getModifiersList());

		if (m.getName().equals("<init>"))
			s += "constructor";
		else if (m.getName().equals("<clinit>"))
			s += "init ";
		else
			s += "fun ";

		if (m.getGenericParametersCount() > 0) {
			s += "<";
			for (int i = 0; i < m.getGenericParametersCount(); i++) {
				if (i > 0)
					s += ", ";
				s += prettyprint(m.getGenericParameters(i));
			}
			s += "> ";
		}

		if (!m.getName().equals("<init>") && !m.getName().equals("<clinit>"))
			s += m.getName();

		if (!m.getName().equals("<clinit>")) {
			s += "(";
			for (int i = 0; i < m.getArgumentsCount(); i++) {
				if (i > 0)
					s += ", ";
				s += prettyprint(m.getArguments(i));
			}
			s += ")";
		}

		if (!m.getReturnType().getName().equals(""))
			s += " : " + prettyprint(m.getReturnType());

		if (m.getExceptionTypesCount() > 0) {
			s += " throws";
			for (int i = 0; i < m.getExceptionTypesCount(); i++)
				s += " " + prettyprint(m.getExceptionTypes(i));
		}

		s += "\n";
		for (int i = 0; i < m.getStatementsCount(); i++)
			s += indent() + prettyprint(m.getStatements(i)) + "\n";

		return s;
	}

	public String prettyprint(final Variable v) {
		// FIXME convert to Kotlin
		if (v == null) return "";
		boolean hasVal = false;
		boolean isImplicit = false;

		final List<Modifier> mods = v.getModifiersList();
		final List<Modifier> newMods = new ArrayList<Modifier>();

		for (Modifier mod : mods) {
			if (mod.getKind() == Modifier.ModifierKind.FINAL)
				hasVal = true;
			else if (mod.getKind() == Modifier.ModifierKind.IMPLICIT)
				isImplicit = true;
			else
				newMods.add(mod);
		}

		String s = "";
		if (newMods.size() > 0)
			s += prettyprint(newMods);

		if (!isImplicit)
			s += hasVal ? "val " : "var ";

		s += v.getName();

		if (v.hasVariableType())
			s += ": " + prettyprint(v.getVariableType());

		if (v.getExpressionsCount() != 0)
			s += "("+ prettyprint(v.getExpressions(0)) +")";

		if (v.hasInitializer())
			s += " = " + prettyprint(v.getInitializer());

		return s;
	}

	private String prettyprint(final List<Modifier> mods) {
		String s = "";

		for (final Modifier m : mods)
			s += prettyprint(m) + " ";

		return s;
	}

	public String prettyprint(final Statement stmt) {
		// FIXME convert to Kotlin
		if (stmt == null) return "";

		String s = "";

		switch (stmt.getKind()) {
			case EMPTY:
				return ";";

			case BLOCK:
				s += "{\n";
				indent++;
				for (int i = 0; i < stmt.getStatementsCount(); i++)
					s += indent() + prettyprint(stmt.getStatements(i)) + "\n";
				indent--;
				s += indent() + "}";
				return s;

			case RETURN:
				s += "return";
				if (stmt.getExpressionsCount() > 1)
					s += "@" + prettyprint(stmt.getExpressions(0)) + " " + prettyprint(stmt.getExpressions(1));
				else if (stmt.getExpressionsCount() > 0)
					s += " " + prettyprint(stmt.getExpressions(0));
				return s;

			case BREAK:
				s += "break";
				if (stmt.getExpressionsCount() > 0)
					s += "@" + prettyprint(stmt.getExpressions(0));
				return s;

			case CONTINUE:
				s += "continue";
				if (stmt.getExpressionsCount() > 0)
					s += "@" + prettyprint(stmt.getExpressions(0));
				return s;

			case LABEL:
				return prettyprint(stmt.getExpressions(0)) + "@ " + prettyprint(stmt.getStatements(0));

			case CASE:
				return prettyprint(stmt.getExpressions(0)) + " -> ";

			case DEFAULT:
				return "else -> ";

			case EXPRESSION:
				return prettyprint(stmt.getExpressions(0)) + "\n";

			case TYPEDECL:
				return prettyprint(stmt.getTypeDeclaration());

			case CATCH:
				s += indent() + "catch (";
				s += prettyprint(stmt.getVariableDeclaration());
				s += ") {\n";
				indent++;
				for (int i = 0; i < stmt.getStatementsCount(); i++)
					s += indent() + prettyprint(stmt.getStatements(i)) + "\n";
				indent--;
				s += indent() + "}";
				return s;

			case FINALLY:
				s += indent() + "finally {\n";
				indent++;
				for (int i = 0; i < stmt.getStatementsCount(); i++)
					s += indent() + prettyprint(stmt.getStatements(i)) + "\n";
				indent--;
				s += indent() + "}";
				return s;

			case TRY:
				s += "try {\n";
				indent++;
				for (int i = 0; i < stmt.getStatementsCount(); i++)
					s += prettyprint(stmt.getStatements(i)) + "\n";
				indent--;
				s += indent() + "}";
				return s;

			case FOREACH:
				s += "for (" + prettyprint(stmt.getVariableDeclarations(0)) + " in " + prettyprint(stmt.getInitializations(0)) + ")\n";
				s += indent() + prettyprint(stmt.getStatements(0));
				return s;

			case DO:
				s += "do ";
				for (int i = 0; i < stmt.getStatementsCount(); i++)
					s += prettyprint(stmt.getStatements(i));
				s += " while (" + prettyprint(stmt.getConditions(0)) + ")";
				return s;

			case WHILE:
				s += "while (" + prettyprint(stmt.getConditions(0)) + ")";
				if (stmt.getExpressionsCount() == 0) {
					s += " ";
					for (int i = 0; i < stmt.getStatementsCount(); i++) {
						if (stmt.getStatements(i).getKind() != Statement.StatementKind.BLOCK) s += indent();
						s += prettyprint(stmt.getStatements(i));
					}
				} else {
					s += "\n";
					indent++;
					for (int i = 0; i < stmt.getExpressionsCount(); i++)
						s += indent() + prettyprint(stmt.getExpressions(i)) + "\n";
					indent--;
				}
				return s;

			case IF:
				s += "if (" + prettyprint(stmt.getConditions(0)) + ")\n";
				indent++;
				s += indent() + prettyprint(stmt.getStatements(0)) + "\n";
				indent--;
				if (stmt.getStatementsCount() > 1) {
					s += indent() + "else\n";
					indent++;
					s += indent() + prettyprint(stmt.getStatements(1)) + "\n";
					indent--;
				}
				return s;

			case SWITCH:
				s += "when (" + prettyprint(stmt.getConditions(0)) + ") {\n";
				indent++;
				for (int i = 0; i < stmt.getStatementsCount(); i++)
					s += indent() + prettyprint(stmt.getStatements(i)) + "\n";
				indent--;
				s += indent() + "}";
				return s;

			case THROW:
				return "throw " + prettyprint(stmt.getExpressions(0));

			default: return s;
		}
	}

	public String prettyprint(final Expression e) {
		// FIXME convert to Kotlin
		if (e == null) return "";

		String s = "";

		if (e.getModifiersCount() > 0) {
			for(int i = 0 ; i < e.getModifiersCount() ; i++) {
				s += prettyprint(e.getModifiers(i)) + " ";
			}
		}

		switch (e.getKind()) {
			case OP_ADD:
				if (e.getExpressionsCount() == 1)
					return ppPrefix("+", e);
				return ppInfix("+", e.getExpressionsList());
			case OP_SUB:
				if (e.getExpressionsCount() == 1)
					return ppPrefix("-", e);
				return ppInfix("-", e.getExpressionsList());

			case LOGICAL_AND:           return "(" + ppInfix("&&", e.getExpressionsList()) + ")";
			case LOGICAL_OR:            return "(" + ppInfix("||", e.getExpressionsList()) + ")";

			case EQ:                    return ppInfix("==",   e.getExpressionsList());
			case NEQ:                   return ppInfix("!=",   e.getExpressionsList());
			case LT:                    return ppInfix("<",    e.getExpressionsList());
			case GT:                    return ppInfix(">",    e.getExpressionsList());
			case LTEQ:                  return ppInfix("<=",   e.getExpressionsList());
			case GTEQ:                  return ppInfix(">=",   e.getExpressionsList());
			case OP_DIV:                return ppInfix("/",    e.getExpressionsList());
			case OP_MULT:               return ppInfix("*",    e.getExpressionsList());
			case OP_MOD:                return ppInfix("%",    e.getExpressionsList());
			case BIT_AND:               return ppInfix("&",    e.getExpressionsList());
			case BIT_OR:                return ppInfix("|",    e.getExpressionsList());
			case BIT_XOR:               return ppInfix("^",    e.getExpressionsList());
			case BIT_LSHIFT:            return ppInfix("<<",   e.getExpressionsList());
			case BIT_RSHIFT:            return ppInfix(">>",   e.getExpressionsList());
			case BIT_UNSIGNEDRSHIFT:    return ppInfix(">>>",  e.getExpressionsList());
			case ASSIGN:                return ppInfix("=",    e.getExpressionsList());
			case ASSIGN_ADD:            return ppInfix("+=",   e.getExpressionsList());
			case ASSIGN_SUB:            return ppInfix("-=",   e.getExpressionsList());
			case ASSIGN_MULT:           return ppInfix("*=",   e.getExpressionsList());
			case ASSIGN_DIV:            return ppInfix("/=",   e.getExpressionsList());
			case ASSIGN_MOD:            return ppInfix("%=",   e.getExpressionsList());
			case ASSIGN_BITXOR:         return ppInfix("^=",   e.getExpressionsList());
			case ASSIGN_BITAND:         return ppInfix("&=",   e.getExpressionsList());
			case ASSIGN_BITOR:          return ppInfix("|=",   e.getExpressionsList());
			case ASSIGN_LSHIFT:         return ppInfix("<<=",  e.getExpressionsList());
			case ASSIGN_RSHIFT:         return ppInfix(">>=",  e.getExpressionsList());
			case ASSIGN_UNSIGNEDRSHIFT: return ppInfix(">>>=", e.getExpressionsList());
			case ARRAY_COMPREHENSION:   return ppInfix("..", e.getExpressionsList());

			case LOGICAL_NOT: return ppPrefix("!", e);
			case BIT_NOT:     return ppPrefix("~", e);

			case OP_DEC:
				if (e.getIsPostfix())
					return ppPostfix("--", e);
				return ppPrefix("--", e);
			case OP_INC:
				if (e.getIsPostfix())
					return ppPostfix("++", e);
				return ppPrefix("++", e);

			case PAREN: return "(" + prettyprint(e.getExpressions(0)) + ")";
			case LABEL:
			case LITERAL: return e.getLiteral();
			case VARACCESS:
				for (int i = 0; i < e.getExpressionsCount(); i++)
					s += prettyprint(e.getExpressions(i)) + ".";
				s += e.getVariable();
				return s;
			case CAST: return "(" + e.getNewType().getName() + ")" + prettyprint(e.getExpressions(0));
			case CONDITIONAL: return prettyprint(e.getExpressions(0)) + " ? " + prettyprint(e.getExpressions(1)) + " : " + prettyprint(e.getExpressions(2));
			case NULLCOALESCE: return prettyprint(e.getExpressions(0)) + " ?? " + prettyprint(e.getExpressions(1));

			case METHODCALL:
				for (int i = 0; i < e.getExpressionsCount(); i++)
					s += prettyprint(e.getExpressions(i)) + ".";
				if (e.getGenericParametersCount() > 0) {
					s += "<";
					for (int i = 0; i < e.getGenericParametersCount(); i++) {
						if (i > 0)
							s += ", ";
						s += prettyprint(e.getGenericParameters(i));
					}
					s += ">";
				}
				s += e.getMethod() + "(";
				for (int i = 0; i < e.getMethodArgsCount(); i++) {
					if (i > 0)
						s += ", ";
					s += prettyprint(e.getMethodArgs(i));
				}
				s += ")";
				return s;

			case TYPECOMPARE:
				if (e.getExpressionsCount() == 1)
					s += prettyprint(e.getExpressions(0)) + " ";
				s += "is " + prettyprint(e.getNewType());
				return s;

			case NEWARRAY:
				s += "new ";
				final String arrtype = prettyprint(e.getNewType());
				s += arrtype.substring(0, arrtype.length() - 1);
				for (int i = 0; i < e.getExpressionsCount(); i++)
					s += prettyprint(e.getExpressions(i));
				s += "]";
				return s;

			case NEW:
				s += "new ";
				s += prettyprint(e.getNewType());
				if (e.getGenericParametersCount() > 0) {
					s += "<";
					for (int i = 0; i < e.getGenericParametersCount(); i++) {
						if (i > 0)
							s += ", ";
						s += prettyprint(e.getGenericParameters(i));
					}
					s += ">";
				}
				s += "(";
				for (int i = 0; i < e.getMethodArgsCount(); i++) {
					if (i > 0)
						s += ", ";
					s += prettyprint(e.getMethodArgs(i));
				}
				s += ")";
				if (e.hasAnonDeclaration())
					s += prettyprint(e.getAnonDeclaration());
				return s;

			case ARRAYACCESS:
				return prettyprint(e.getExpressions(0)) + "[" + prettyprint(e.getExpressions(1)) + "]";

			case ARRAYINIT:
				s += "{";
				for (int i = 0; i < e.getExpressionsCount(); i++) {
					if (i > 0)
						s += ", ";
					s += prettyprint(e.getExpressions(i));
				}
				s += "}";
				return s;

			case ANNOTATION:
				return prettyprint(e.getModifiersList());

			case VARDECL:
				for (int i = 0; i < e.getVariableDeclsCount(); i++) {
					s += prettyprint(e.getVariableDecls(i));
				}

				return s;

			case IN:
				if (e.getExpressionsCount() > 1) {
					s += prettyprint(e.getExpressions(0)) + " in " + prettyprint(e.getExpressions(1));
				} else if (e.getExpressionsCount() > 0) {
					s += "in " + prettyprint(e.getExpressions(0));
				}
				return s;

			case LAMBDA:
				s += "{ ";
				for (int i = 0; i < e.getVariableDeclsCount(); i++) {
					if (i > 0)
						s += ", ";
					s+= prettyprint(e.getVariableDecls(i));
				}
				if (e.getVariableDeclsCount() > 0)
					s += " -> ";
				if (e.getStatementsCount() != 0)
					s += prettyprint(e.getStatements(0));
				if (e.getExpressionsCount() != 0)
					s += prettyprint(e.getExpressions(0));
				s += "}";
				return s;

			case SWITCH:
				s += "when (" + prettyprint(e.getExpressions(0)) + ") {\n";
				indent++;
				for (int i = 0 ; i < e.getStatementsCount() ; i ++)
					s += indent() + prettyprint(e.getStatements(i)) + "\n";
				indent--;
				s += indent() + "}";

			case TEMPLATE:
				s += e.getLiteral();
				return s;

			default: return s;
		}
	}

	private String ppPrefix(final String op, final Expression e) {
		return op + prettyprint(e.getExpressions(0));
	}

	private String ppPostfix(final String op, final Expression e) {
		return prettyprint(e.getExpressions(0)) + op;
	}

	private String ppInfix(final String op, final List<Expression> exps) {
		StringBuilder s = new StringBuilder();

		if (exps.size() == 1) {
			s.append(op);
			s.append(prettyprint(exps.get(0)));
		} else {
			s.append(prettyprint(exps.get(0)));
			for (int i = 1; i < exps.size(); i++) {
				s.append(" ");
				s.append(op);
				s.append(" ");
				s.append(prettyprint(exps.get(i)));
			}
		}

		return s.toString();
	}

	public String prettyprint(final Modifier m) {
		if (m == null) return "";

		switch (m.getKind()) {
			case VISIBILITY:
				switch (m.getVisibility()) {
					case PUBLIC:    return "public";
					case PRIVATE:   return "private";
					case PROTECTED: return "protected";
					case INTERNAL:  return "internal";
					default: return "";
				}

			case ANNOTATION:
				String s = "@";
				if (m.hasOther())
					s += m.getOther() + ":";
				s += m.getAnnotationName();
				if (m.getAnnotationValuesCount() > 0) s += "(";
				for (int i = 0; i < m.getAnnotationValuesCount(); i++) {
					if (i > 0) s += ", ";
					s += prettyprint(m.getAnnotationValues(i));
				}
				if (m.getAnnotationValuesCount() > 0) s += ")";
				return s;

			case FINAL:    return "final";
			case ABSTRACT: return "abstract";

			default: return m.getOther();
		}
	}

	public Expression parseexpression(final String content) {
		final ASTRoot root = parse(content);
		if (root.getNamespacesCount() == 0) return null;
		final Namespace n = root.getNamespaces(0);
		if (n.getExpressionsCount() == 0) return null;
		return n.getExpressions(0);
	}

	private static PsiManager kProjectManager = null;

	public static KtFile tryparse(final String path, final String content, final boolean debug) {
		try {
			if (kProjectManager == null) {
				final Disposable disp = Disposer.newDisposable();
				final KotlinCoreApplicationEnvironment kae = KotlinCoreApplicationEnvironment.create(disp, false);
				final Project proj = new KotlinCoreProjectEnvironment(disp, kae).getProject();
				((CoreFileTypeRegistry)FileTypeRegistry.getInstance()).registerFileType(KotlinFileType.INSTANCE, "kt");
				LanguageParserDefinitions.INSTANCE.addExplicitExtension(KotlinLanguage.INSTANCE,
											new KotlinParserDefinition());
				kProjectManager = PsiManager.getInstance(proj);
			}

			final VirtualFile file = new LightVirtualFile(path, KotlinFileType.INSTANCE, content);
			final KtFile theKt = new KtFile(kProjectManager.findViewProvider(file), false);

			if (!debug && new KotlinErrorCheckVisitor().hasError(theKt))
				return null;

			return theKt;
		} catch (final Exception e) {
			if (debug) e.printStackTrace();
			return null;
		}
	}
	private static final KotlinVisitor visitor = new KotlinVisitor();

	public ASTRoot parse(final String content) {
		final ASTRoot.Builder ast = ASTRoot.newBuilder();

		final KtFile theKt = tryparse("boa.kt", content, false);
		if (theKt != null)
			ast.addNamespaces(visitor.getNamespace(theKt));

		return ast.build();
	}
}
