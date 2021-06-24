/*
 * Copyright 2017-2021, Hridesh Rajan, Robert Dyer,
 *                 Iowa State University of Science and Technology
 *                 Bowling Green State University
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

import org.eclipse.dltk.python.internal.core.parser.PythonSourceParser;
import org.eclipse.dltk.python.parser.ast.PythonModuleDeclaration;
import org.eclipse.dltk.compiler.env.IModuleSource;
import org.eclipse.dltk.compiler.env.ModuleSource;
import boa.datagen.util.PythonVisitor;

import boa.datagen.util.JavaErrorCheckVisitor;
import boa.datagen.util.JavaVisitor;
import boa.types.Ast.ASTRoot;
import boa.types.Ast.Declaration;
import boa.types.Ast.Expression;
import boa.types.Ast.Method;
import boa.types.Ast.Modifier;
import boa.types.Ast.Namespace;
import boa.types.Ast.Statement;
import boa.types.Ast.Statement.StatementKind;
import boa.types.Ast.Type;
import boa.types.Ast.TypeKind;
import boa.types.Ast.Variable;

/**
 * Boa functions for working with Python ASTs.
 *
 * @author rdyer
 */
public class PythonLangMode implements LangMode {
	public String type_name(final String s) {
		if (!s.contains("."))
			return s;

		/*
		 * Remove qualifiers from anywhere in the string...
		 *
		 * SomeType                               =>  SomeType
		 * foo.SomeType                           =>  SomeType
		 * foo.bar.SomeType                       =>  SomeType
		 */
		return s.replaceAll("[^\\s]+\\.([^\\s\\[.]+)", "$1");
	}

	///////////////////////////////
	// Literal testing functions */
	///////////////////////////////

	// based off: https://docs.python.org/3/reference/lexical_analysis.html

	/**
		integer      ::=  decinteger | bininteger | octinteger | hexinteger
		decinteger   ::=  nonzerodigit (["_"] digit)* | "0"+ (["_"] "0")*
		bininteger   ::=  "0" ("b" | "B") (["_"] bindigit)+
		octinteger   ::=  "0" ("o" | "O") (["_"] octdigit)+
		hexinteger   ::=  "0" ("x" | "X") (["_"] hexdigit)+
		nonzerodigit ::=  "1"..."9"
		digit        ::=  "0"..."9"
		bindigit     ::=  "0" | "1"
		octdigit     ::=  "0"..."7"
		hexdigit     ::=  digit | "a"..."f" | "A"..."F"
	 */
	public boolean isIntLit(final Expression e) throws Exception {
		if (e.getKind() != Expression.ExpressionKind.LITERAL) return false;
		if (!e.hasLiteral()) return false;
		final String lit = e.getLiteral();

		if (lit.matches("^[1..9](_?[0..9])*$")) return true;
		if (lit.matches("^0+(_?0)*$")) return true;

		if (lit.matches("^0[bB](_?[01])+$")) return true;
		if (lit.matches("^0[oO](_?[0..7])+$")) return true;
		return lit.matches("^0[xX](_?[0..9a..fA..F])+$");
	}

	/**
		floatnumber   ::=  pointfloat | exponentfloat
		pointfloat    ::=  [digitpart] fraction | digitpart "."
		exponentfloat ::=  (digitpart | pointfloat) exponent
		digitpart     ::=  digit (["_"] digit)*
		fraction      ::=  "." digitpart
		exponent      ::=  ("e" | "E") ["+" | "-"] digitpart
	 */
	public boolean isFloatLit(final Expression e) throws Exception {
		if (e.getKind() != Expression.ExpressionKind.LITERAL) return false;
		if (!e.hasLiteral()) return false;
		final String lit = e.getLiteral();

		if (lit.matches("^([0..9](_?[0..9])*)?\\.[0..9](_?[0..9])*|[0..9](_?[0..9])*\\.$")) return true;
		if (lit.matches("^[0..9](_?[0..9])*[eE][+-]?\\.[0..9](_?[0..9])*$")) return true;
		return lit.matches("^(([0..9](_?[0..9])*)?\\.[0..9](_?[0..9])*|[0..9](_?[0..9])*\\.)[eE][+-]?\\.[0..9](_?[0..9])*$");
	}

	public boolean isCharLit(final Expression e) throws Exception {
		// Python does not have character literals, just strings
		return false;
	}

	/**
		stringliteral   ::=  [stringprefix](shortstring | longstring)
		stringprefix    ::=  "r" | "u" | "R" | "U" | "f" | "F"
		                     | "fr" | "Fr" | "fR" | "FR" | "rf" | "rF" | "Rf" | "RF"
		shortstring     ::=  "'" shortstringitem* "'" | '"' shortstringitem* '"'
		longstring      ::=  "'''" longstringitem* "'''" | '"""' longstringitem* '"""'
		<p>
		bytesliteral   ::=  bytesprefix(shortbytes | longbytes)
		bytesprefix    ::=  "b" | "B" | "br" | "Br" | "bR" | "BR" | "rb" | "rB" | "Rb" | "RB"
	 */
	public boolean isStringLit(final Expression e) throws Exception {
		if (e.getKind() != Expression.ExpressionKind.LITERAL) return false;
		if (!e.hasLiteral()) return false;
		final String lit = e.getLiteral();

		if (lit.matches("^[rRuUfF]?['\"]")) return true;
		if (lit.matches("^[fF][rR]?['\"]")) return true;
		if (lit.matches("^[rR][fF]['\"]")) return true;

		if (lit.startsWith("b")) return true;
		if (lit.startsWith("B")) return true;
		return lit.matches("^[rR][bB]");
	}

	public boolean isTypeLit(final Expression e) throws Exception {
		return false;
	}

	public boolean isBoolLit(final Expression e) throws Exception {
		if (e.getKind() != Expression.ExpressionKind.LITERAL) return false;
		if (!e.hasLiteral()) return false;
		final String lit = e.getLiteral();
		return lit.equals("True") || lit.equals("False");
	}

	public boolean isNullLit(final Expression e) throws Exception {
		if (e.getKind() != Expression.ExpressionKind.LITERAL) return false;
		if (!e.hasLiteral()) return false;
		return e.getLiteral().equals("None");
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

		for (final String i : n.getImportsList())
			if (i.startsWith("from "))
				s += indent() + i + "\n";
			else
				s += indent() + "import " + i + "\n";

		for (final Variable v : n.getVariablesList())
			s += prettyprint(v);

		for (final Declaration d : n.getDeclarationsList())
			s += prettyprint(d);

		for (final Method m : n.getMethodsList())
			s += prettyprint(m);

		for (final Statement st : n.getStatementsList())
			s += prettyprint(st);

		return s;
	}

	public String prettyprint(final Declaration d) {
		if (d == null) return "";

		String s = prettyprint(d.getModifiersList());

		s += indent() + "class " + d.getName();
		if (d.getParentsCount() > 0) {
			s += "(";
			for (int i = 0; i < d.getParentsCount(); i++) {
				if (i > 0) s += ", ";
				s += prettyprint(d.getParents(i));
			}
			s += ")";
		}

		s += ":\n";

		indent++;

		for (final Declaration d2 : d.getNestedDeclarationsList())
			s += prettyprint(d2);
		for (final Statement st : d.getStatementsList())
			s += prettyprint(st);
		if (d.getStatementsCount() > 0)
			s += "\n";
		for (final Method m : d.getMethodsList())
			s += prettyprint(m);

		indent--;

		s += "\n";

		return s;
	}

	public String prettyprint(final Type t) {
		if (t == null) return "";

		return prettyprint(t.getComputedName());
	}

	public String prettyprint(final Method m) {
		if (m == null) return "";

		String s = prettyprint(m.getModifiersList());

		s += indent() + "def " + m.getName() + "(";
		for (int i = 0; i < m.getArgumentsCount(); i++) {
			if (i > 0)
				s += ", ";
			s += prettyprint(m.getArguments(i));
		}
		s += "):\n";

		for (int i = 0; i < m.getStatementsCount(); i++)
			s += prettyprint(m.getStatements(i));

		s += "\n";
		return s;
	}

	public String prettyprint(final Variable v) {
		if (v == null) return "";

		String s = "";

		if (v.hasComputedName())
			s += v.getComputedName();
		else
			s += v.getName();

		if (v.hasVariableType())
			s += " : " + prettyprint(v.getVariableType());

		if (v.getExpressionsCount() != 0)
			s += "(" + prettyprint(v.getExpressions(0)) + ")";

		if (v.hasInitializer())
			s += " = " + prettyprint(v.getInitializer());

		return s;
	}

	private String prettyprint(final List<Modifier> mods) {
		String s = "";

		for (final Modifier m : mods)
			s += indent() + prettyprint(m) + "\n";

		return s;
	}

	public String prettyprint(final Statement stmt) {
		if (stmt == null) return "";

		String s = "";

		switch (stmt.getKind()) {
			case EMPTY:
				return indent() + "pass\n";

			case BLOCK:
				indent++;

				for (final Variable v : stmt.getVariableDeclarationsList())
					s += prettyprint(v) + "\n";

				for (final Declaration d : stmt.getTypeDeclarationsList())
					s += prettyprint(d);

				for (final Method m : stmt.getMethodsList())
					s += prettyprint(m);

				for (final Statement st : stmt.getStatementsList())
					s += prettyprint(st);

				indent--;
				return s;

			case RETURN:
				s += indent() + "return";
				if (stmt.getExpressionsCount() > 0)
					s += " " + prettyprint(stmt.getExpressions(0));
				s += "\n";
				return s;

			case BREAK:
				s += indent() + "break";
				if (stmt.getExpressionsCount() > 0)
					s += " " + prettyprint(stmt.getExpressions(0));
				s += "\n";
				return s;

			case CONTINUE:
				s += indent() + "continue";
				if (stmt.getExpressionsCount() > 0)
					s += " " + prettyprint(stmt.getExpressions(0));
				s += "\n";
				return s;

			case ASSERT:
				s += indent() + "assert ";
				s += prettyprint(stmt.getConditions(0));
				if (stmt.getExpressionsCount() > 0)
					s += ", " + prettyprint(stmt.getExpressions(0));
				s += "\n";
				return s;

			case EXPRESSION:
				return indent() + prettyprint(stmt.getExpressions(0)) + "\n";

			case DEL:
				return indent() + "del" + " " + prettyprint(stmt.getExpressions(0)) + "\n";

			case CATCH:
				s += indent() + "except";
				if (stmt.hasVariableDeclaration()) {
					s += " " + prettyprint(stmt.getVariableDeclaration().getVariableType());
					if (stmt.getExpressionsCount() > 0)
						s += " as " + prettyprint(stmt.getExpressions(0));
				}
				s += ":\n";
				for (int i = 0; i < stmt.getStatementsCount(); i++)
					s += prettyprint(stmt.getStatements(i));
				return s;

			case FINALLY:
				s += indent() + "finally:\n";
				for (int i = 0; i < stmt.getStatementsCount(); i++)
					s += prettyprint(stmt.getStatements(i));
				return s;

			case TRY:
				s += indent() + "try:\n";
				for (int i = 0; i < stmt.getStatementsCount(); i++) {
					// FIXME the else: and finally: are swapped, if both appear
					if (i == stmt.getStatementsCount() - 1)
						if (stmt.getStatements(i).getKind() != StatementKind.CATCH)
							if (stmt.getStatements(i).getKind() != StatementKind.FINALLY)
								s += indent() + "else:\n";
					s += prettyprint(stmt.getStatements(i));
				}
				return s;

			case RAISE:
				s += indent() + "raise";
				if (stmt.getExpressionsCount() > 0)
					s += " " + prettyprint(stmt.getExpressions(0));
				// FIXME handle 'from x' after parser handles it
				s += ":\n";
				return s;

			case FOREACH:
				s += indent() + "for ";
				for (int i = 0; i < stmt.getVariableDeclarationsCount(); i++) {
					if (i > 0)
						s += ", ";
					s += prettyprint(stmt.getVariableDeclarations(i));
				}
				s += " in" + prettyprint(stmt.getConditions(0)) + ":\n";
				indent++;
				s += prettyprint(stmt.getStatements(0));
				indent--;
				if (stmt.getStatementsCount() > 1) {
					s += indent() + "else:\n";
					indent++;
					s += prettyprint(stmt.getStatements(1));
					indent--;
				}
				return s;

			case WHILE:
				s += indent() + "while " + prettyprint(stmt.getConditions(0)) + ":\n";
				indent++;
				s += prettyprint(stmt.getStatements(0));
				indent--;
				if (stmt.getStatementsCount() > 1) {
					s += indent() + "else:\n";
					indent++;
					s += prettyprint(stmt.getStatements(1));
					indent--;
				}
				return s;

			case IF:
				s += indent() + "if " + prettyprint(stmt.getConditions(0)) + ":\n";
				indent++;
				s += prettyprint(stmt.getStatements(0));
				indent--;
				if (stmt.getStatementsCount() > 1) {
					s += indent() + "else:\n";
					indent++;
					s += prettyprint(stmt.getStatements(1));
					indent--;
				}
				return s;

			case WITH:
				s += indent() + "with ";
				for (int i = 0; i < stmt.getExpressionsCount(); i++) {
					if (i > 0)
						s += ", ";
					s += prettyprint(stmt.getExpressions(i));
				}
				// FIXME handle the 'as' cases: x as y, w as z
				s += ":\n";
				for (int i = 0; i < stmt.getStatementsCount(); i++)
					s += prettyprint(stmt.getStatements(i));
				return s;

			case GLOBAL:
				s += indent() + "global ";
				for (int i = 0; i < stmt.getExpressionsCount(); i++) {
					if (i > 0)
						s += ", ";
					s += prettyprint(stmt.getExpressions(i));
				}
				s += "\n";
				return s;

			default: return s;
		}
	}

	public String prettyprint(final Expression e) {
		if (e == null) return "";

		String s = "";

		switch (e.getKind()) {
			case LOGICAL_AND:           return "(" + ppInfix("and", e.getExpressionsList()) + ")";
			case LOGICAL_OR:            return "(" + ppInfix("or", e.getExpressionsList()) + ")";

			case IN:                    return ppInfix("in",     e.getExpressionsList());
			case NOT_IN:                return ppInfix("not in", e.getExpressionsList());
			case IS:                    return ppInfix("is",     e.getExpressionsList());
			case IS_NOT:                return ppInfix("is not", e.getExpressionsList());

			case EQ:                    return ppInfix("==",   e.getExpressionsList());
			case NEQ:                   return ppInfix("!=",   e.getExpressionsList());
			case LT:                    return ppInfix("<",    e.getExpressionsList());
			case GT:                    return ppInfix(">",    e.getExpressionsList());
			case LTEQ:                  return ppInfix("<=",   e.getExpressionsList());
			case GTEQ:                  return ppInfix(">=",   e.getExpressionsList());
			case OP_ADD:                return ppInfix("+",    e.getExpressionsList());
			case OP_SUB:                return ppInfix("-",    e.getExpressionsList());
			case OP_DIV:                return ppInfix("/",    e.getExpressionsList());
			case OP_MULT:               return ppInfix("*",    e.getExpressionsList());
			case OP_MOD:                return ppInfix("%",    e.getExpressionsList());
			case OP_POW:                return ppInfix("**",   e.getExpressionsList());
			case OP_INT_DIV:            return ppInfix("//",   e.getExpressionsList());
			case BIT_AND:               return ppInfix("&",    e.getExpressionsList());
			case BIT_OR:                return ppInfix("|",    e.getExpressionsList());
			case BIT_XOR:               return ppInfix("^",    e.getExpressionsList());
			case BIT_LSHIFT:            return ppInfix("<<",   e.getExpressionsList());
			case BIT_RSHIFT:            return ppInfix(">>",   e.getExpressionsList());
			case ASSIGN:                return ppInfix("=",    e.getExpressionsList());
			case ASSIGN_ADD:            return ppInfix("+=",   e.getExpressionsList());
			case ASSIGN_SUB:            return ppInfix("-=",   e.getExpressionsList());
			case ASSIGN_MULT:           return ppInfix("*=",   e.getExpressionsList());
			case ASSIGN_DIV:            return ppInfix("/=",   e.getExpressionsList());
			case ASSIGN_MOD:            return ppInfix("%=",   e.getExpressionsList());
			case ASSIGN_POW:            return ppInfix("**=",  e.getExpressionsList());
			case ASSIGN_INT_DIV:        return ppInfix("//=",  e.getExpressionsList());
			case ASSIGN_BITXOR:         return ppInfix("^=",   e.getExpressionsList());
			case ASSIGN_BITAND:         return ppInfix("&=",   e.getExpressionsList());
			case ASSIGN_BITOR:          return ppInfix("|=",   e.getExpressionsList());
			case ASSIGN_LSHIFT:         return ppInfix("<<=",  e.getExpressionsList());
			case ASSIGN_RSHIFT:         return ppInfix(">>=",  e.getExpressionsList());

			case LOGICAL_NOT: return ppPrefix("not ", e);
			case BIT_NOT:     return ppPrefix("~", e);

			case UNARY: return prettyprint(e.getExpressions(0)) + " " + prettyprint(e.getExpressions(1));
			case PAREN: return "(" + prettyprint(e.getExpressions(0)) + ")";

			case LITERAL: return e.getLiteral();
			case VARACCESS:
				for (int i = 0; i < e.getExpressionsCount(); i++)
					s += prettyprint(e.getExpressions(i)) + ".";
				s += e.getVariable();
				return s;
			case CONDITIONAL:
				s += prettyprint(e.getExpressions(0)) + " if " + prettyprint(e.getExpressions(1));
				if (e.getExpressionsCount() > 2)
					s += " else " + prettyprint(e.getExpressions(2));
				return s;

			case YIELD:
				return indent() + "yield " + prettyprint(e.getExpressions(0)) + "\n";

			case METHODCALL:
				for (int i = 0; i < e.getExpressionsCount(); i++)
					s += prettyprint(e.getExpressions(i)) + ".";
				s += e.getMethod();
				for (int i = 0; i < e.getMethodArgsCount(); i++)
					s += prettyprint(e.getMethodArgs(i));
				return s;

			case CALLHOLDER:
				s += "(";
				for (int i = 0; i < e.getExpressionsCount(); i++) {
					if (i > 0)
						s += ", ";
					s += prettyprint(e.getExpressions(i));
				}
				s += ")";
				return s;

			case LAMBDA:
				s += "lambda";
				for (int i = 0; i < e.getVariableDeclsCount(); i++) {
					if (i > 0)
						s += ",";
					s += " " + e.getVariableDecls(i).getName();
				}
				s += ": " + prettyprint(e.getExpressions(0));
				return s;

			case TUPLE:
				s += e.getMethod() + "(";
				for (int i = 0; i < e.getExpressionsCount(); i++) {
					if (i > 0)
						s += ", ";
					s += prettyprint(e.getExpressions(i));
				}
				s += ")";
				return s;

			case DICT:
				s += "{ ";
				for (int i = 0; i < e.getExpressionsCount(); i++) {
					if (i > 0)
						s += ", ";
					final Expression node = e.getExpressions(i);
					s += prettyprint(node.getExpressions(0)) + " : " + prettyprint(node.getExpressions(1));
				}
				s += " }";
				return s;

			case SET:
				s += "{ ";
				for (int i = 0; i < e.getExpressionsCount(); i++) {
					if (i > 0)
						s += ", ";
					s += prettyprint(e.getExpressions(i));
				}
				s += " }";
				return s;

			case ARRAYINDEX:
				s += "[";
				if (e.getExpressionsCount() == 3) {
					s += prettyprint(e.getExpressions(0));
					s += ":";
					s += prettyprint(e.getExpressions(1));
					s += ":";
					s += prettyprint(e.getExpressions(2));
				} else {
					// FIXME current AST ambiguous for less than 3 exps
					s += "...";
				}
				s += "]";
				return s;

			case FOR_LIST:
				s += "for " + prettyprint(e.getExpressions(0));
				s += " in " + prettyprint(e.getExpressions(1));
				if (e.getExpressionsCount() > 2)
					s +=  "if " + prettyprint(e.getExpressions(2));
				return s;

			case ARRAY_COMPREHENSION:
				return "[" + prettyprint(e.getExpressions(0)) + "]";

			case NEWARRAY:
				s += e.getMethod() + "[";
				for (int i = 0; i < e.getExpressionsCount(); i++) {
					if (i > 0)
						s += ", ";
					s += prettyprint(e.getExpressions(i));
				}
				s += "]";
				return s;

			case ARRAYACCESS:
				for (int i = 0; i < e.getExpressionsCount() - 1; i++) {
					if (i > 0)
						s += ".";
					s += prettyprint(e.getExpressions(i));
				}
				s += prettyprint(e.getExpressions(e.getExpressionsCount() - 1));
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

		String s = indent() + "@" + m.getAnnotationName();

		if (m.getAnnotationValuesCount() > 0) {
			s += "(";
			for (int i = 0; i < m.getAnnotationValuesCount(); i++) {
				if (i > 0) s += ", ";
				s += m.getAnnotationValues(i) + " = " + prettyprint(m.getAnnotationValues(i));
			}
			s += ")";
		}

		s += "\n";
		return s;
	}

	public Expression parseexpression(final String s) {
		final ASTRoot ast = parse(s);

		if (ast.getNamespacesCount() == 1) {
			final Namespace ns = ast.getNamespaces(0);
			if (ns.getStatementsCount() == 1) {
				final Statement st = ns.getStatements(0);
				if (st.getExpressionsCount() == 1) {
					return st.getExpressions(0);
				}
			}
		}

		final Expression.Builder e = Expression.newBuilder();
		e.setKind(Expression.ExpressionKind.OTHER);
		return e.build();
	}

	public ASTRoot parse(final String s) {
		final StringBuilder sb = new StringBuilder();

		PythonSourceParser parser = new PythonSourceParser();
		IModuleSource input = new ModuleSource(s);

		final ASTRoot.Builder ast = ASTRoot.newBuilder();
		PythonVisitor visitor = new PythonVisitor();
		visitor.enableDiff = false;

		try {
			ast.addNamespaces(visitor.getNamespace((PythonModuleDeclaration) parser.parse(input, null), "<Boa dynamic parse()>"));
		} catch (final Throwable e) {
			// do nothing
		}

		return ast.build();
	}
}
