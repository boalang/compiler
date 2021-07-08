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
import java.util.Map;

import kotlin.Unit;
// import kotlinx.ast.common.ast.Ast;
// import kotlinx.ast.common.AstResult;
// import kotlinx.ast.common.AstSource;
// import kotlinx.ast.grammar.kotlin.common.SummaryKt;
// import kotlinx.ast.grammar.kotlin.target.antlr.java.KotlinGrammarAntlrJavaParser;

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
			s += "\t";
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

		s += prettyprint(n.getModifiersList());

		if (n.getName().length() > 0)
			s += indent() + "package " + n.getName() + ";\n";

		for (final String i : n.getImportsList())
			s += indent() + "import " + i + "\n";

		for (final Variable v : n.getVariablesList())
			s += prettyprint(v);

		for (final Method m : n.getMethodsList())
			s += prettyprint(m);

		for (final Statement st : n.getStatementsList())
			s += prettyprint(st);

		for (final Expression e : n.getExpressionsList())
			s += prettyprint(e);

		for (final Declaration d : n.getDeclarationsList())
			s += prettyprint(d);

		return s;
	}

	public String prettyprint(final Declaration d) {
		// FIXME convert to Kotlin
		if (d == null) return "";

		String s = indent() + prettyprint(d.getModifiersList());

		switch (d.getKind()) {
			case INTERFACE:
				s += "interface " + d.getName();
				if (d.getGenericParametersCount() > 0) {
					s += "<";
					for (int i = 0; i < d.getGenericParametersCount(); i++) {
						if (i != 0) s += ", ";
						s += prettyprint(d.getGenericParameters(i));
					}
					s += ">";
				}
				if (d.getParentsCount() > 0) {
					s += " extends ";
					for (int i = 0; i < d.getParentsCount(); i++) {
						if (i != 0) s += ", ";
						s += prettyprint(d.getParents(i));
					}
				}
				s += " {\n";
				break;
			case ANONYMOUS:
				break;
			case ENUM:
				s += "enum " + d.getName();
				break;
			case ANNOTATION:
				s += "@interface class " + d.getName();
				if (d.getGenericParametersCount() > 0) {
					s += "<";
					for (int i = 0; i < d.getGenericParametersCount(); i++) {
						if (i != 0) s += ", ";
						s += prettyprint(d.getGenericParameters(i));
					}
					s += ">";
				}
				if (d.getParentsCount() > 0) {
					int i = 0;
					if (d.getParents(i).getKind() == TypeKind.CLASS)
						s += " extends " + prettyprint(d.getParents(i++));
					if (i < d.getParentsCount()) {
						s += " implements ";
						for (int j = i; i < d.getParentsCount(); i++) {
							if (i != j) s += ", ";
							s += prettyprint(d.getParents(i));
						}
					}
				}
				break;
			default:
			case CLASS:
				s += "class " + d.getName();
				if (d.getGenericParametersCount() > 0) {
					s += "<";
					for (int i = 0; i < d.getGenericParametersCount(); i++) {
						if (i != 0) s += ", ";
						s += prettyprint(d.getGenericParameters(i));
					}
					s += ">";
				}
				if (d.getParentsCount() > 0) {
					int i = 0;
					if (d.getParents(i).getKind() == TypeKind.CLASS)
						s += " extends " + prettyprint(d.getParents(i++));
					if (i < d.getParentsCount()) {
						s += " implements ";
						for (int j = i; i < d.getParentsCount(); i++) {
							if (i != j) s += ", ";
							s += prettyprint(d.getParents(i));
						}
					}
				}
				break;
		}

		s += " {\n";

		indent++;
		for (int i = 0; i < d.getFieldsCount(); i++) {
			s += indent() + prettyprint(d.getFieldsList().get(i));
			s += (!d.getFieldsList().get(i).hasVariableType()
					&& i < d.getFieldsCount() - 1
					&& !d.getFieldsList().get(i + 1).hasVariableType()) ? ",\n" : ";\n";
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

		return t.getName();
	}

	public String prettyprint(final Method m) {
		// FIXME convert to Kotlin
		if (m == null) return "";
		String s = indent() + prettyprint(m.getModifiersList());

		if (m.getGenericParametersCount() > 0) {
			s += "<";
			for (int i = 0; i < m.getGenericParametersCount(); i++) {
				if (i > 0)
					s += ", ";
				s += prettyprint(m.getGenericParameters(i));
			}
			s += "> ";
		}

		s += prettyprint(m.getReturnType()) + " " + m.getName() + "(";
		for (int i = 0; i < m.getArgumentsCount(); i++) {
			if (i > 0)
				s += ", ";
			s += prettyprint(m.getArguments(i));
		}
		s += ")";

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

		String s = "";
		if (v.getModifiersCount() > 0)
			s += prettyprint(v.getModifiersList());

		if (v.hasVariableType())
			s += prettyprint(v.getVariableType()) + " ";

		s += v.getName();

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
				if (stmt.getExpressionsCount() > 0)
					s += " " + prettyprint(stmt.getExpressions(0));
				s += ";";
				return s;
			case BREAK:
				s += "break";
				if (stmt.getExpressionsCount() > 0)
					s += " " + prettyprint(stmt.getExpressions(0));
				s += ";";
				return s;
			case CONTINUE:
				s += "continue";
				if (stmt.getExpressionsCount() > 0)
					s += " " + prettyprint(stmt.getExpressions(0));
				s += ";";
				return s;

			case ASSERT:
				s += "assert ";
				s += prettyprint(stmt.getConditions(0));
				if (stmt.getExpressionsCount() > 0)
					s += " " + prettyprint(stmt.getExpressions(0));
				s += ";";
				return s;

			case LABEL:
				return prettyprint(stmt.getExpressions(0)) + ": " + prettyprint(stmt.getStatements(0));

			case CASE:
				return "case " + prettyprint(stmt.getExpressions(0)) + ":";

			case DEFAULT:
				return "default:";

			case EXPRESSION:
				return prettyprint(stmt.getExpressions(0)) + ";";

			case TYPEDECL:
				return prettyprint(stmt.getTypeDeclaration());

			case SYNCHRONIZED:
				s += "synchronized () {\n";
				indent++;
				for (int i = 0; i < stmt.getStatementsCount(); i++)
					s += indent() + prettyprint(stmt.getStatements(i)) + "\n";
				indent--;
				s += "}";
				return s;

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
				s += "try";
				if (stmt.getInitializationsCount() > 0) {
					s += "(";
					for (int i = 0; i < stmt.getInitializationsCount(); i++) {
						if (i > 0)
							s += ", ";
						s += prettyprint(stmt.getInitializations(i));
					}
					s += ")";
				}
				s += " ";
				for (int i = 0; i < stmt.getStatementsCount(); i++) {
					s += prettyprint(stmt.getStatements(i)) + "\n";
				}
				return s;

			case FOR:
				s += "for (";
				if (stmt.hasVariableDeclaration()) {
					s += prettyprint(stmt.getVariableDeclaration()) + " : " + prettyprint(stmt.getConditions(0));
				} else {
					for (int i = 0; i < stmt.getInitializationsCount(); i++) {
						if (i > 0)
							s += ", ";
						s += prettyprint(stmt.getInitializations(i));
					}
					s += "; " + prettyprint(stmt.getConditions(0)) + "; ";
					for (int i = 0; i < stmt.getUpdatesCount(); i++) {
						if (i > 0)
							s += ", ";
						s += prettyprint(stmt.getUpdates(i));
					}
				}
				s += ")\n";
				indent++;
				s += indent() + prettyprint(stmt.getStatements(0)) + "\n";
				indent--;
				return s;

			case FOREACH:
				s += "for (" + prettyprint(stmt.getVariableDeclaration()) + " : " + prettyprint(stmt.getExpressions(0)) + ")\n";
				s += indent() + prettyprint(stmt.getStatements(0));
				return s;

			case DO:
				s += "do\n";
				indent++;
				for (int i = 0; i < stmt.getStatementsCount(); i++)
					s += indent() + prettyprint(stmt.getStatements(i)) + "\n";
				indent--;
				s += indent() + "while (" + prettyprint(stmt.getConditions(0)) + ");";
				return s;

			case WHILE:
				s += "while (" + prettyprint(stmt.getConditions(0)) + ") {\n";
				indent++;
				for (int i = 0; i < stmt.getStatementsCount(); i++)
					s += indent() + prettyprint(stmt.getStatements(i)) + "\n";
				indent--;
				s += indent() + "}";
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
				s += "switch (" + prettyprint(stmt.getExpressions(0)) + ") {\n";
				indent++;
				for (int i = 0; i < stmt.getStatementsCount(); i++)
					s += indent() + prettyprint(stmt.getStatements(i)) + "\n";
				indent--;
				s += indent() + "}";
				return s;

			case THROW:
				return "throw " + prettyprint(stmt.getExpressions(0)) + ";";

			default: return s;
		}
	}

	public String prettyprint(final Expression e) {
		// FIXME convert to Kotlin
		if (e == null) return "";

		String s = "";

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
				return prettyprint(e.getExpressions(0)) + " instanceof " + prettyprint(e.getNewType());

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
				return prettyprint(e.getAnnotation());

			case VARDECL:
				s += prettyprint(e.getVariableDecls(0).getModifiersList());
				s += prettyprint(e.getVariableDecls(0).getVariableType()) + " ";
				for (int i = 0; i < e.getVariableDeclsCount(); i++) {
					if (i > 0)
						s += ", ";
					s += e.getVariableDecls(i).getName();
					if (e.getVariableDecls(i).hasInitializer())
						s += " = " + prettyprint(e.getVariableDecls(i).getInitializer());
				}
				return s;

			case LAMBDA:
				s += "(";
				for (int i = 0; i < e.getVariableDeclsCount(); i++) {
					if (i > 0)
						s += ", ";
					String type = prettyprint(e.getVariableDecls(i).getVariableType());
					if (!type.equals(""))
						s += type + " ";
					s += e.getVariableDecls(i).getName();
				}
				s += ") -> ";
				if (e.getStatementsCount() != 0)
					s += prettyprint(e.getStatements(0));
				if (e.getExpressionsCount() != 0)
					s += prettyprint(e.getExpressions(0));
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

		s.append(prettyprint(exps.get(0)));
		for (int i = 1; i < exps.size(); i++) {
			s.append(" ");
			s.append(op);
			s.append(" ");
			s.append(prettyprint(exps.get(i)));
		}

		return s.toString();
	}

	public String prettyprint(final Modifier m) {
		if (m == null) return "";

		String s = "";

		switch (m.getKind()) {
			case OTHER: return m.getOther();

			case VISIBILITY:
				switch (m.getVisibility()) {
					case PUBLIC:    return "public";
					case PRIVATE:   return "private";
					case PROTECTED: return "protected";
					case INTERNAL:  return "internal";
					default: return s;
				}

			case ANNOTATION:
				// FIXME convert to Kotlin - handle @file:[foo, bar] syntax
				s = "@" + m.getAnnotationName();
				if (m.getAnnotationMembersCount() > 0) s += "(";
				for (int i = 0; i < m.getAnnotationMembersCount(); i++) {
					if (i > 0) s += ", ";
					s += m.getAnnotationMembers(i) + " = " + prettyprint(m.getAnnotationValues(i));
				}
				if (m.getAnnotationMembersCount() > 0) s += ")";
				return s;

			case FINAL:        return "final";
			case ABSTRACT:     return "abstract";

			default: return s;
		}
	}

	public Expression parseexpression(final String s) {
		final ASTRoot root = parse(s);
		if (root.getNamespacesCount() == 0) return null;
		final Namespace n = root.getNamespaces(0);
		if (n.getExpressionsCount() == 0) return null;
		return n.getExpressions(0);
	}

	public ASTRoot parse(final String s) {
		final ASTRoot.Builder ast = ASTRoot.newBuilder();

		// FIXME: Handle parsing of Kotlin code
		// try {
		// 	final AstSource source = new AstSource.String("", s);
		// 	final AstResult<Unit, List<Ast>> astList = SummaryKt.summary(KotlinGrammarAntlrJavaParser.INSTANCE.parseKotlinFile(source), true);
		// 	ast.addNamespaces(new KotlinVisitor().getNamespace(astList.get()));
		// } catch (final Throwable e) {
		// 	// do nothing
		// }

		return ast.build();
	}
}
