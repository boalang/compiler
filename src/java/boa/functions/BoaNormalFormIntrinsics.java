/*
 * Copyright 2017, Robert Dyer, Mohd Arafat
 *                 and Bowling Green State University
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
package boa.functions;

import java.util.*;
import java.lang.IllegalArgumentException;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.JavaCore;

import boa.types.Ast.Expression.ExpressionKind;
import boa.compiler.ast.*;
import boa.compiler.ast.expressions.*;
import boa.datagen.util.Java8Visitor;
import boa.types.Ast.Expression;
import boa.compiler.ast.types.*;
import boa.types.*;
import boa.types.Ast.*;

/**
 * Boa functions for converting Expressions into various normal forms.
 *
 * @author marafat
 * @author rdyer
 */
public class BoaNormalFormIntrinsics {
	/**
	 * Converts a string expression into an AST.
	 *
	 * @param s the string to parse/convert
	 * @return the AST representation of the string
	 */
	@FunctionSpec(name = "parseexpression", returnType = "Expression", formalParameters = { "string" })
	public static Expression parseexpression(final String s) {
		final ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setKind(ASTParser.K_EXPRESSION);
		parser.setSource(s.toCharArray());

		final Map options = JavaCore.getOptions();
		JavaCore.setComplianceOptions(JavaCore.VERSION_1_8, options);
		parser.setCompilerOptions(options);

		try {
			final org.eclipse.jdt.core.dom.Expression e = (org.eclipse.jdt.core.dom.Expression) parser.createAST(null);
			final Java8Visitor visitor = new Java8Visitor(s, null);
			e.accept(visitor);
			return visitor.getExpression();
		} catch (final Exception e) {
			return null;
		}
	}

	/**
	 * Attempts to reduce an expression, simplifying wherever possible.
	 *
	 * @param e the expression to reduce
	 * @return the reduced form of the expression
	 */
	@FunctionSpec(name = "reduce", returnType = "Expression", formalParameters = { "Expression" })
	public static Expression reduce(final Expression e) throws Exception {
		final Object o = reduce_internal(e);
		if (o instanceof Expression)
			return (Expression)o;
		return createLiteral(o.toString());
	}

	/**
	 * Attempts to reduce an expression, simplifying wherever possible.
	 *
	 * @param e the expression to reduce
	 * @return the reduced form of the expression, either as a Number or a complex Expression
	 */
	private static Object reduce_internal(final Expression e) throws Exception {
        final Expression.Builder b;
		final List<Object> results = new ArrayList<Object>();
		for (final Expression sub : e.getExpressionsList())
			results.add(reduce_internal(sub));

		final List<Object> results2 = new ArrayList<Object>();
		Double dval = 0.0;
		Long ival = 0L;
		boolean first = true;

		switch (e.getKind()) {
			// reduce both sides of the comparison
			case EQ:
			case NEQ:
			case GT:
			case LT:
			case GTEQ:
			case LTEQ:
				return createExpression(e.getKind(), results.toArray(new Expression[results.size()]));

			case OP_ADD:
				// handle cases like '+x' or '+3'
				if (results.size() == 1) {
					final Object o = results.get(0);
					if (o instanceof Expression)
						return reduce_internal(e.getExpressions(0));
					if (o instanceof Double)
						return ((Double)o).doubleValue();
					return ((Long)o).longValue();
				}

				// if multiple arguments, try to add them all together
				for (final Object o : results) {
					if (o instanceof Expression)
						results2.add(o);
					else if (o instanceof Double)
						dval += ((Double)o).doubleValue();
					else if (o instanceof Long)
						ival += ((Long)o).longValue();
				}

				// both float and integer results, so merge them into float
				if (dval != 0.0 && ival != 0L) {
					dval += ival;
					ival = 0L;
				}

				if (dval != 0.0)
				// after merging, add the one that remains to results
					results2.add(0, dval);
				else
					results2.add(0, ival);

				if (results2.size() > 1) {
					// check for identity
					if (results2.get(0) instanceof Number && ((Number)results2.get(0)).doubleValue() == 0.0)
						results2.remove(0);
				}

				// if it reduced to a single term, return just the term otherwise return the whole expression
				if (results2.size() == 1)
					return results2.get(0);
				return createExpression(e.getKind(), convertArray(results2));

			case OP_SUB:
				// handle cases like '-x' or '-3'
				if (results.size() == 1) {
					final Object o = results.get(0);
					if (o instanceof Expression)
						return e;
					if (o instanceof Double)
						return -((Double)o).doubleValue();
					return -((Long)o).longValue();
				}

				// if multiple arguments, try to subtract them all together
				for (final Object o : results) {
					if (o instanceof Expression) {
						results2.add(o);
					} else {
						if (first) {
							results2.add(o);
						} else {
							if (o instanceof Double)
								dval += ((Double)o).doubleValue();
							else
								ival += ((Long)o).longValue();
						}
					}
					first = false;
				}

				// both float and integer results, so merge them into float
				if (dval != 0.0 && ival != 0L) {
					dval += ival;
					ival = 0L;
				}

				// if the first term is a number, perform subtraction on it
				if (results2.get(0) instanceof Number) {
					if (results2.get(0) instanceof Double) {
						if (dval != 0.0)
							results2.set(0, (Double)results2.get(0) - dval);
						else
							results2.set(0, (Double)results2.get(0) - ival);
					} else {
						if (dval != 0.0)
							results2.set(0, (double)(Long)results2.get(0) - dval);
						else
							results2.set(0, (Long)results2.get(0) - ival);
					}
				} else {
					// after merging, add the one that remains to results
					if (dval != 0.0)
						results2.add(dval);
					else if (ival != 0L)
						results2.add(ival);
				}

				if (results2.size() > 1) {
					final Object lhs = results2.get(0);

					// check for elimination
					if (lhs instanceof Expression) {
						int idx = results2.lastIndexOf(lhs);
						if (idx > 0) {
							results2.remove(idx);
							results2.set(0, 0L);
						}
					}

					// check for identity
					if (lhs instanceof Number && ((Number)lhs).doubleValue() == 0.0) {
						results2.remove(0);
						results2.set(0, createExpression(ExpressionKind.OP_SUB, (Expression)results2.get(0)));
					}
				}

				// if it reduced to a single term, return just the term otherwise return the whole expression
				if (results2.size() == 1)
					return results2.get(0);
				return createExpression(e.getKind(), convertArray(results2));

			case OP_MULT:
				dval = 1.0;
				ival = 1L;

				// if multiple arguments, try to multiply them all together
				for (final Object o : results) {
					if (o instanceof Expression)
						results2.add(o);
					else if (o instanceof Double)
						dval *= ((Double)o).doubleValue();
					else if (o instanceof Long)
						ival *= ((Long)o).longValue();
				}

				// both float and integer results, so merge them into float
				if (dval != 1.0 && ival != 1L) {
					dval *= ival;
					ival = 1L;
				}

				// after merging, add the one that remains to results
				if (dval != 1.0)
					results2.add(0, dval);
				else
					results2.add(0, ival);

				if (results2.size() > 1) {
					// check for identity
					if (results2.get(0) instanceof Number && ((Number)results2.get(0)).doubleValue() == 1.0)
						results2.remove(0);

					// check for elimination
					if (results2.get(0) instanceof Double && (Double)results2.get(0) == 0.0)
						return 0.0;
					else if (results2.get(0) instanceof Long && (Long)results2.get(0) == 0L)
						return 0L;
				}

				// if it reduced to a single term, return just the term otherwise return the whole expression
				if (results2.size() == 1)
					return results2.get(0);
				return createExpression(e.getKind(), convertArray(results2));

			case OP_DIV:
				dval = 1.0;
				ival = 1L;

				// if multiple arguments, try to divide them all together
				// in thise case, all of the denominators get multiplied together
				for (final Object o : results) {
					if (o instanceof Expression) {
						results2.add(o);
					} else {
						if (first) {
							results2.add(o);
						} else {
							if (o instanceof Double)
								dval *= ((Double)o).doubleValue();
							else if (o instanceof Long)
								ival *= ((Long)o).longValue();
						}
					}
					first = false;
				}

				// both float and integer results, so merge them into float
				if (dval != 1.0 && ival != 1L) {
					dval *= ival;
					ival = 1L;
				}

				final Object numerator = results2.get(0);
				// if the numerator is a number, try to do the actual division
				if (numerator instanceof Number) {
					if (dval != 1.0) {
						if (numerator instanceof Double)
							results2.set(0, ((Double)numerator).doubleValue() / dval);
						else
							results2.set(0, (double)((Long)numerator).longValue() / dval);
					} else {
						if (numerator instanceof Double)
							results2.set(0, ((Double)numerator).doubleValue() / (double)ival);
						else
							results2.set(0, div((double)((Long)numerator).longValue(), (double)ival));
					}
				} else {
					// otherwise just add the new denominator
					if (dval != 1.0)
						results2.add(dval);
					else
						results2.add(ival);
				}

				if (results2.size() > 1) {
					// check for elimination
					if (results2.get(0) instanceof Expression) {
						int idx = results2.lastIndexOf(results2.get(0));
						if (idx > 0) {
							results2.remove(idx);
							results2.set(0, 1L);
						}
					}

					// check for identity
					for (int i = 1; i < results2.size(); i++)
						if (results2.get(i) instanceof Number && ((Number)results2.get(i)).doubleValue() == 1.0)
							results2.remove(i);
				}

				// if it reduced to a single term, return just the term otherwise return the whole expression
				if (results2.size() == 1)
					return results2.get(0);
				return createExpression(e.getKind(), convertArray(results2));

			// literals are converted to numbers, if possible
			case LITERAL:
				if (BoaAstIntrinsics.isIntLit(e))
					return Long.parseLong(e.getLiteral());
				if (BoaAstIntrinsics.isFloatLit(e))
					return Double.parseDouble(e.getLiteral());
				return e;

			// return method call, but with each argument reduced
			case METHODCALL:
				b = Expression.newBuilder(e);

				b.clearMethodArgs();
				for (final Expression sub : e.getMethodArgsList())
					b.addMethodArgs((Expression)reduce(sub));

				return b.build();

			// remove parens
			case PAREN:
				return results.get(0);

			// these have sub-expressions we must reduce
			case VARACCESS:
			case OP_INC:
			case OP_DEC:
			case LOGICAL_NOT:
			case LOGICAL_AND:
			case LOGICAL_OR:
			case OP_MOD:
			case BIT_LSHIFT:
			case BIT_RSHIFT:
			case BIT_UNSIGNEDRSHIFT:
			case BIT_AND:
			case BIT_OR:
			case BIT_NOT:
			case BIT_XOR:
			case CAST:
/*
VARDECL
ARRAYINDEX
ARRAYINIT
TYPECOMPARE
NEW
NEWARRAY
CONDITIONAL
NULLCOALESCE
ASSIGN
ASSIGN_ADD
ASSIGN_SUB
ASSIGN_MULT
ASSIGN_DIV
ASSIGN_MOD
ASSIGN_BITXOR
ASSIGN_BITAND
ASSIGN_BITOR
ASSIGN_LSHIFT
ASSIGN_RSHIFT
ASSIGN_UNSIGNEDRSHIFT
ANNOTATION
METHOD_REFERENCE
LAMBDA
*/
			default:
				if (results.size() == 0)
					return e;

				// want the same message, but with new sub-expressions
				b = Expression.newBuilder(e);

				b.clearExpressions();
				for (final Object o : results)
                    b.addExpressions((Expression)o);

				return b.build();

			// for these we do nothing
				//return e;
		}
	}

	private static Expression[] convertArray(final List<Object> arr) {
		for (int i = 0; i < arr.size(); i++)
			if (arr.get(i) instanceof Number)
				arr.set(i, createLiteral(arr.get(i).toString()));
		return arr.toArray(new Expression[arr.size()]);
	}

	private static Object div(final double num, final double denom) {
		final double result = num / denom;
		if (result == (long)result)
			return (long)result;
		return result;
	}

	/**
	 * A comparator for Expression types.
	 * Uses pretty printing and string comparison.
	 *
	 * @author rdyer
	 */
	public static class ExpressionComparator implements Comparator {
		public int compare(final Object o1, final Object o2) {
			final Expression e1 = (Expression)o1;
			final Expression e2 = (Expression)o2;
			return BoaAstIntrinsics.prettyprint(e1).compareTo(BoaAstIntrinsics.prettyprint(e2));
		}
	}

	private static ExpressionComparator comparator = new ExpressionComparator();

	/**
	 * Computes the negated normal form of an expression and then simplifies the result.
	 *
	 * @param e the expression to compute NNF on
	 * @return the negated normal form of e, simplified
	 */
	@FunctionSpec(name = "nnf", returnType = "Expression", formalParameters = { "Expression" })
	public static Expression NNF(final Expression e) {
		return simplify(internalNNF(e));
	}

	/**
	 * Computes negated normal form of an expression, without simplifying it.
	 *
	 * @param e the expression to compute NNF on
	 * @return the negated normal form of e
	 */
	private static Expression internalNNF(final Expression e) {
		switch (e.getKind()) {
			case LOGICAL_NOT:
				// push negations in
				return pushNegIn(e.getExpressions(0));

			case PAREN:
				// remove parens
				return internalNNF(e.getExpressions(0));

			case LOGICAL_AND:
			case LOGICAL_OR:
				// recurse into operands
				final Expression[] exps = new Expression[e.getExpressionsCount()];
				for (int i = 0; i < exps.length; i++)
					exps[i] = internalNNF(e.getExpressions(i));
				return createExpression(e.getKind(), exps);

			default:
				// anything else is unchanged
				return e;
		}
	}

	/**
	 * Pushes a negation further into an expression.
	 *
	 * @param e the expression to push a negation into
	 * @return an expression with negations pushed in
	 */
	private static Expression pushNegIn(final Expression e) {
		switch (e.getKind()) {
			case PAREN:
				// remove parens
				return pushNegIn(e.getExpressions(0));

			case LOGICAL_NOT:
				// double-negation elimination
				// !(!a) = a
				return internalNNF(e.getExpressions(0));

			case LOGICAL_AND:
			case LOGICAL_OR:
				// De Morgan's law
				// !(a & b) -> !a | !b
				// !(a | b) -> !a & !b
				final Expression[] exps = new Expression[e.getExpressionsCount()];
				for (int i = 0; i < exps.length; i++)
					exps[i] = internalNNF(createExpression(ExpressionKind.LOGICAL_NOT, e.getExpressions(i)));
				return createExpression(negateKind(e.getKind()), exps);

			case GT:
			case LT:
			case EQ:
			case GTEQ:
			case LTEQ:
			case NEQ:
				// binary comparison operators get flipped
				final Expression[] exps2 = new Expression[e.getExpressionsCount()];
				for (int i = 0; i < exps2.length; i++)
					exps2[i] = internalNNF(e.getExpressions(i));
				return createExpression(negateKind(e.getKind()), exps2);

			default:
				// atoms maintain the negation
				return createExpression(ExpressionKind.LOGICAL_NOT, e);
		}
	}

	/**
	 * Gives the negated expression kind.
	 *
	 * @param kind the ExpressionKind to negate
	 * @return the negated kind
	 */
	private static ExpressionKind negateKind(final ExpressionKind kind) {
		switch (kind) {
			case GT:   return ExpressionKind.LTEQ;
			case LTEQ: return ExpressionKind.GT;

			case LT:   return ExpressionKind.GTEQ;
			case GTEQ: return ExpressionKind.LT;

			case EQ:   return ExpressionKind.NEQ;
			case NEQ:  return ExpressionKind.EQ;

			case LOGICAL_AND: return ExpressionKind.LOGICAL_OR;
			case LOGICAL_OR:  return ExpressionKind.LOGICAL_AND;

			default: throw new RuntimeException("invalid ExpressionKind: " + kind);
		}
	}

	// convenience literals for comparisons - must clone if you want to use in output
	private static Expression trueLit = createLiteral("true");
	private static Expression falseLit = createLiteral("false");

	/**
	 * Processes an expression (operand).  Handles several simplifications.
	 *
	 * @param e the expression to process
	 * @param exps the result list of operands
	 * @param seen what operands we have already seen
	 * @param kind the kind of operator we are processing operands for
	 * @return true if we should continue processing expressions, otherwise false (happens on annihilation/complementation)
	 */
	private static boolean processExpression(final Expression e, final List<Expression> exps, final Set<Expression> seen, final ExpressionKind kind) {
		// idempotence
		// a && a = a
		// a || a = a
		if (!seen.contains(e)) {
			exps.add(e);
			seen.add(e);
		}

		// given a, look for !a
		// given !a, look for a
		// stays null if can't be found
		Expression e2 = null;
		if (e.getKind() == ExpressionKind.LOGICAL_NOT) {
			if (exps.contains(e.getExpressions(0)))
				e2 = e.getExpressions(0);
		} else {
			if (exps.contains(createExpression(ExpressionKind.LOGICAL_NOT, e)))
				e2 = createExpression(ExpressionKind.LOGICAL_NOT, e);
		}

		// annihilation: a || true  = true
		// complementation: a || !a = true
		if (kind == ExpressionKind.LOGICAL_OR && (e2 != null || exps.contains(trueLit))) {
			exps.clear();
			exps.add(Expression.newBuilder(trueLit).build());
			return false;
		}

		// annihilation: a && false = false
		// complementation: a && !a = false
		if (kind == ExpressionKind.LOGICAL_AND && (e2 != null || exps.contains(falseLit))) {
			exps.clear();
			exps.add(Expression.newBuilder(falseLit).build());
			return false;
		}

		return true;
	}

	/**
	 * Simplifies an expression.
	 *
	 * @param e the expression to simplify
	 * @return a simplified version of the expression
	 */
	@FunctionSpec(name = "simplify", returnType = "Expression", formalParameters = { "Expression" })
	public static Expression simplify(final Expression e) {
		switch (e.getKind()) {
			case LOGICAL_AND:
			case LOGICAL_OR:
				final List<Expression> exps = new ArrayList<Expression>();
				final Set<Expression> seen = new HashSet<Expression>();

				// recurse in and simplify inner expressions
				OUTER:
				for (int i = 0; i < e.getExpressionsCount(); i++) {
					final Expression e2 = simplify(e.getExpressions(i));

					if (e2.getKind() == e.getKind()) {
						for (int j = 0; j < e2.getExpressionsCount(); j++) {
							final Expression e3 = simplify(e2.getExpressions(j));

							if (!processExpression(e3, exps, seen, e.getKind()))
								break OUTER;
						}
					} else {
						if (!processExpression(e2, exps, seen, e.getKind()))
							break OUTER;
					}
				}

				// identity
				// a || false = a
				if      (e.getKind() == ExpressionKind.LOGICAL_OR)  while (exps.remove(falseLit)) ;
				// a && true  = a
				else if (e.getKind() == ExpressionKind.LOGICAL_AND) while (exps.remove(trueLit)) ;

				// elimination
				// (a && b) || (a && !b) = a
				// (a || b) && (a || !b) = a
				for (int i = 0; i < exps.size(); i++) {
					final Expression exp = exps.get(i);

					if (exp.getKind() == ExpressionKind.LOGICAL_AND || exp.getKind() == ExpressionKind.LOGICAL_OR)
						for (int j = 0; j < exps.size(); j++) {
							final Expression exp2 = exps.get(j);

							if (i == j || exp2.getKind() != exp.getKind())
								continue;

							final TreeSet<Expression> s1 = new TreeSet(comparator);
							s1.addAll(exp.getExpressionsList());
							s1.removeAll(exp2.getExpressionsList());

							final TreeSet<Expression> s2 = new TreeSet(comparator);
							s2.addAll(exp2.getExpressionsList());
							s2.removeAll(exp.getExpressionsList());

							if (s1.size() != 1 || s2.size() != 1)
								continue;

							final Expression e1 = s1.first();
							final Expression e2 = s2.first();

							if (e1.getKind() == ExpressionKind.LOGICAL_NOT && e2.getKind() != ExpressionKind.LOGICAL_NOT) {
								if (e1.getExpressions(0).equals(e2)) {
									final Expression.Builder b1 = Expression.newBuilder(exp);
									b1.getExpressionsList().remove(e1);
									exps.set(i, b1.build());

									final Expression.Builder b2 = Expression.newBuilder(exp2);
									b2.getExpressionsList().remove(e2);
									exps.set(j, b2.build());
								}
							}
							else if (e1.getKind() != ExpressionKind.LOGICAL_NOT && e2.getKind() == ExpressionKind.LOGICAL_NOT) {
								if (e2.getExpressions(0).equals(e1)) {
									final Expression.Builder b1 = Expression.newBuilder(exp);
									b1.getExpressionsList().remove(e1);
									exps.set(i, b1.build());

									final Expression.Builder b2 = Expression.newBuilder(exp2);
									b2.getExpressionsList().remove(e2);
									exps.set(j, b2.build());
								}
							}
						}
				}

				for (int i = 0; i < exps.size(); i++) {
					final Expression exp = exps.get(i);

					if (exp.getKind() != ExpressionKind.LOGICAL_AND && exp.getKind() != ExpressionKind.LOGICAL_OR)
						for (int j = 0; j < exps.size(); j++) {
							final Expression exp2 = exps.get(j);

							if (i == j || exp.getKind() == exp2.getKind())
								continue;
							if (exp2.getKind() != ExpressionKind.LOGICAL_AND && exp2.getKind() != ExpressionKind.LOGICAL_OR)
								continue;

							// absorption
							// a && (a || b) = a
							// a || (a && b) = a
							if (exp2.getExpressionsList().contains(exp)) {
								exps.remove(j);
								j--;
								continue;
							}

							final Expression negExp;
							if (exp.getKind() == ExpressionKind.LOGICAL_NOT)
								negExp = exp.getExpressions(0);
							else
								negExp = createExpression(ExpressionKind.LOGICAL_NOT, exp);

							// negative absorption
							// a && (!a || b || c) = a && (b || c)
							// a || (!a && b && c) = a || (b && c)
							if (exp2.getExpressionsList().contains(negExp)) {
								final Expression.Builder b = Expression.newBuilder(exp2);
								b.removeExpressions(b.getExpressionsList().indexOf(negExp));
								if (b.getExpressionsList().size() == 1)
									exps.set(j, b.getExpressionsList().get(0));
								else
									exps.set(j, b.build());
							}
						}
				}


				// commutativity (sort expressions)
				// b && a = a && b
				// b || a = a || b
				Collections.sort(exps, comparator);

				if (exps.size() == 1) return exps.get(0);

				if (exps.size() == 0) {
					if (e.getKind() == ExpressionKind.LOGICAL_OR)  return Expression.newBuilder(falseLit).build();
					if (e.getKind() == ExpressionKind.LOGICAL_AND) return Expression.newBuilder(trueLit).build();
				}

				return createExpression(e.getKind(), exps.toArray(new Expression[exps.size()]));

			default:
				return e;
		}
	}

	/**
	 * Computes the conjunctive normal form of an expression and then simplifies the result.
	 *
	 * @param e the expression to compute CNF on
	 * @return the conjunctive normal form of e, simplified
	 */
	@FunctionSpec(name = "cnf", returnType = "Expression", formalParameters = { "Expression" })
	public static Expression CNF(final Expression e) {
		// push the ORs down into ANDs
		// (B ⋀ C) ⋁ A -> (B ⋁ A) ⋀ (C ⋁ A)
		// A ⋁ (B ⋀ C) -> (A ⋁ B) ⋀ (A ⋁ C)
		return simplify(normalform(NNF(e), ExpressionKind.LOGICAL_OR, ExpressionKind.LOGICAL_AND));
	}

	/**
	 * Computes the disjunctive normal form of an expression and then simplifies the result.
	 *
	 * @param e the expression to compute DNF on
	 * @return the disjunctive normal form of e, simplified
	 */
	@FunctionSpec(name = "dnf", returnType = "Expression", formalParameters = { "Expression" })
	public static Expression DNF(final Expression e) {
		// push the ANDs down into ORs
		// (B v C) ^ A -> (B ^ A) v (C ^ A)
		// A ^ (B v C) -> (A ^ B) v (A ^ C)
		return simplify(normalform(NNF(e), ExpressionKind.LOGICAL_AND, ExpressionKind.LOGICAL_OR));
	}

	/**
	 * Helper to get the count of operands.
	 *
	 * @param e the expression to count
	 * @return the number of operands in expression
	 */
	private static int operandCount(final Expression e) {
		// if not an AND or OR, it is an atom
		if (e.getKind() != ExpressionKind.LOGICAL_AND && e.getKind() != ExpressionKind.LOGICAL_OR)
			return 1;

		return e.getExpressionsCount();
	}

	/**
	 * Computes a normal form of an expression, simplifying as it goes.
	 *
	 * @param e the expression to compute a normal form on
	 * @param distributedOp this is the operator we want to distribute
	 * @param innerOp this is the inner operator
	 * @return the normalized expression
	 */
	private static Expression normalform(final Expression e, final ExpressionKind distributedOp, final ExpressionKind innerOp) {
		// just an atom, return it
		if (e.getKind() != innerOp && e.getKind() != distributedOp)
			return e;

		// AND/OR complex ops
		final Expression[] exps = new Expression[e.getExpressionsCount()];

		for (int i = 0; i < exps.length; i++)
			exps[i] = normalform(e.getExpressions(i), distributedOp, innerOp);

		// complex op, top-level operator
		if (e.getKind() == innerOp)
			return createExpression(innerOp, exps);

		// complex op, inner operator
		// here we distribute
		int[] sizes = new int[exps.length];
		sizes[exps.length - 1] = 1;
		for (int i = exps.length - 2; i >= 0; i--)
			sizes[i] = sizes[i + 1] * operandCount(exps[i + 1]);

		final int totalSize = operandCount(exps[0]) * sizes[0];

		final Expression[][] exps2 = new Expression[totalSize][exps.length];

		for (int pos = 0; pos < exps.length; pos++) {
			final Expression curExp = exps[pos];

			int curIndex = 0;
			int stride = 0;

			for (int i = 0; i < totalSize; i++) {
				if (exps2[i] == null)
					exps2[i] = new Expression[exps.length];

				if (curExp.getKind() != ExpressionKind.LOGICAL_AND && curExp.getKind() != ExpressionKind.LOGICAL_OR)
					exps2[i][pos] = Expression.newBuilder(curExp).build();
				else
					exps2[i][pos] = Expression.newBuilder(curExp.getExpressions(curIndex)).build();

				stride++;
				if (stride >= sizes[pos]) {
					stride = 0;
					curIndex++;
					if (curIndex >= operandCount(curExp))
						curIndex = 0;
				}
			}
		}

		// create the parts
		final Expression[] exps3 = new Expression[totalSize];
		for (int i = 0; i < exps3.length; i++)
			exps3[i] = createExpression(distributedOp, exps2[i]);

		// put them together
		if (exps3.length == 1) return exps3[0];
		return createExpression(innerOp, exps3);
	}

	/**
	 * Creates a new prefix/postfix/infix expression.
	 *
	 * @param kind the kind of the expression
	 * @param exps the operands
	 * @return the new expression
	 */
	private static Expression createExpression(final ExpressionKind kind, final Expression... exps) {
		final Expression.Builder b = Expression.newBuilder();

		b.setKind(kind);
		for (final Expression e : exps)
			b.addExpressions(Expression.newBuilder(e).build());

		return b.build();
	}

	/**
	 * Creates a new literal expression.
	 *
	 * @param lit the literal value
	 * @return a new literal expression
	 */
	private static Expression createLiteral(final String lit) {
		// handle negative number literals properly
		if (lit.startsWith("-"))
			return createExpression(ExpressionKind.OP_SUB, createLiteral(lit.substring(1)));

		final Expression.Builder b = Expression.newBuilder();

		b.setKind(ExpressionKind.LITERAL);
		b.setLiteral(lit);

		return b.build();
	}
}
