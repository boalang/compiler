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

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import boa.graphs.pdg.PDG;
import boa.graphs.pdg.PDGEdge;
import boa.graphs.pdg.PDGNode;
import boa.graphs.slicers.PDGSlicer;
import boa.types.Ast;
import boa.types.Ast.Expression.Builder;
import boa.types.Ast.Expression.ExpressionKind;
import boa.types.Ast.Statement.StatementKind;
import boa.types.Ast.Expression;
import boa.types.Ast.Statement;

import static boa.functions.BoaAstIntrinsics.prettyprint;

/**
 * Boa functions for converting Expressions into various normal forms.
 *
 * @author marafat
 * @author rdyer
 */
public class BoaNormalFormIntrinsics {
	/**
	 * Gives list of non-argument variables present in a predicate expression
	 *
	 * @param e the expression in symbolic form
	 * @return array of variables which are not arg0, arg1, arg2, ...
	 * @throws Exception
	 */
	@FunctionSpec(name = "getnoargsvariables", returnType = "array of Expression", formalParameters = { "Expression" })
	public static Expression[] getNoArgsVariables(final Expression e) throws Exception {
		final List<Expression> variableList = new ArrayList<Expression>();

		if (e.getKind() == ExpressionKind.VARACCESS) {
			final String var = e.getVariable();
			if (!var.matches("arg\\$[0-9]+") && !"rcv$".equals(var))
				variableList.add(e);
		} else {
			for (final Expression sub : e.getExpressionsList())
				variableList.addAll(Arrays.asList(getNoArgsVariables(sub)));
		}

		return variableList.toArray(new Expression[variableList.size()]);
	}

	/**
	 * Replaces api arguments in the given expression with symbolic names
	 *
	 * @param e predicate expression
	 * @param reciever api receiver
	 * @param arguments api arguments
	 * @return the expression in symbolic form
	 */
	@FunctionSpec(name = "converttosymbolicname", returnType = "Expression", formalParameters = { "Expression", "Expression", "array of Expression"})
	public static Expression convertToSymbolicName(final Expression e, final Expression reciever, final Expression[] arguments) throws Exception {
		final List<Expression> convertedExpression = new ArrayList<Expression>();
		for (final Expression sub : e.getExpressionsList())
			convertedExpression.add(convertToSymbolicName(sub, reciever, arguments));

		switch (e.getKind()) {
			// return the expression
			case EQ:
			case NEQ:
			case GT:
			case LT:
			case GTEQ:
			case LTEQ:
			case LOGICAL_AND:
			case LOGICAL_OR:
			case LOGICAL_NOT:
			case PAREN:
			case NEW:
				return createExpression(e.getKind(), convertedExpression.toArray(new Expression[convertedExpression.size()]));

			case ASSIGN:
				return createExpression(e.getKind(), convertedExpression.toArray(new Expression[convertedExpression.size()])).getExpressions(1);

			case OP_ADD:
			case OP_SUB:
			case OP_MULT:
			case OP_DIV:
			case OP_MOD:
				final Expression replacedExpr = createExpression(e.getKind(),
						convertedExpression.toArray(new Expression[convertedExpression.size()]));

				if (replacedExpr.equals(reciever))
					return createVariable("rcv$");

				for (int i = 0; i < arguments.length; i++) {
					if (replacedExpr.equals(arguments[i]))
						return createVariable("arg$" + Integer.toString(i));
				}

				return replacedExpr;

			case OP_DEC:
			case OP_INC:
			case ARRAYACCESS:
				final Expression.Builder b = Expression.newBuilder(e);

				for(int i = 0; i < convertedExpression.size(); i++) {
					b.setExpressions(i, convertedExpression.get(i));
				}
				final Expression replacedExpr1 = b.build();

				if (replacedExpr1.equals(reciever))
					return createVariable("rcv$");

				for (int i = 0; i < arguments.length; i++) {
					if (replacedExpr1.equals(arguments[i]))
						return createVariable("arg$" + Integer.toString(i));
				}

				return replacedExpr1;

			case METHODCALL:
				final Expression.Builder bm = Expression.newBuilder(e);

				for(int i = 0; i < convertedExpression.size(); i++) {
					bm.setExpressions(i, convertedExpression.get(i));
				}

				for(int i = 0; i < e.getMethodArgsList().size(); i++) {
					Expression mArgs = convertToSymbolicName(e.getMethodArgs(i), reciever, arguments);
					bm.setMethodArgs(i, mArgs);
				}

				return bm.build();

			case VARACCESS:
				// replace with symbolic names
				if (e.equals(reciever))
					return createVariable("rcv$");

				for (int i = 0; i < arguments.length; i++) {
					if (e.equals(arguments[i]))
						return createVariable("arg$" + Integer.toString(i));
				}
				/*
				for (int i = 0; i < arguments.length; i++) {
					final Map<Integer, List<Object[]>> componentMap = seperate(arguments[i], true, true);
					final List<Object[]> variableList = new ArrayList<Object[]>();

					if (componentMap.containsKey(-1))
						break;

					if (componentMap.containsKey(0))
						variableList.addAll(componentMap.get(0));
					if (componentMap.containsKey(1))
						variableList.addAll(componentMap.get(1));

					boolean exist = false;
					for (final Object[] o: variableList) {
						if (o[0].equals(e)) {
							o[0] = createVariable("arg$" + Integer.toString(i));
							exist = true;
						} else
							o[2] = !((Boolean)o[2]);
					}

					if (exist)
						return combineLeft(variableList);

				}
				*/
				return e;

			// TODO: Handle as per need
			case NEWARRAY:
			case ARRAYINIT:
			case ASSIGN_ADD:
			case ASSIGN_BITAND:
			case ASSIGN_BITOR:
			case ASSIGN_BITXOR:
			case ASSIGN_DIV:
			case ASSIGN_LSHIFT:
			case ASSIGN_MOD:
			case ASSIGN_MULT:
			case ASSIGN_RSHIFT:
			case ASSIGN_SUB:
			case ASSIGN_UNSIGNEDRSHIFT:
			case BIT_AND:
			case BIT_LSHIFT:
			case BIT_NOT:
			case BIT_OR:
			case BIT_RSHIFT:
			case BIT_UNSIGNEDRSHIFT:
			case BIT_XOR:
			case CAST:
			case CONDITIONAL:
			case NULLCOALESCE:

			case LITERAL:
			default:
				return e;
		}
	}

	/**
	 * Assigns literal values to non-argument variables in a precondition expression
	 *
	 * @param e the predicate expression with symbolic_name
	 * @param replace the array of literal values of non-argument variables
	 * @return the replaced expression
	 */
	@FunctionSpec(name = "assignlatestvalue", returnType = "Expression", formalParameters = { "Expression", "map[Expression] of Expression"})
	public static Expression assignLatestValue(final Expression e, final Map<Expression,Expression> replace) {
		final List<Expression> changedExpression = new ArrayList<Expression>();
		for (final Expression sub : e.getExpressionsList())
			changedExpression.add(assignLatestValue(sub, replace));

		switch (e.getKind()) {
			// return the expression
			case EQ:
			case NEQ:
			case GT:
			case LT:
			case GTEQ:
			case LTEQ:
			case OP_ADD:
			case OP_SUB:
			case OP_MULT:
			case OP_DIV:
			case OP_MOD:
			case PAREN:
			case LOGICAL_AND:
			case LOGICAL_OR:
			case LOGICAL_NOT:
			case ASSIGN:
			case NEW:
				return createExpression(e.getKind(), changedExpression.toArray(new Expression[changedExpression.size()]));

			case VARACCESS:
				// replace with latest value
				if (replace.containsKey(e))
					return replace.get(e);

				return e;

			// TODO: Handle them as per need
			case NEWARRAY:
			case ARRAYINIT:
			case ASSIGN_ADD:
			case ASSIGN_BITAND:
			case ASSIGN_BITOR:
			case ASSIGN_BITXOR:
			case ASSIGN_DIV:
			case ASSIGN_LSHIFT:
			case ASSIGN_MOD:
			case ASSIGN_MULT:
			case ASSIGN_RSHIFT:
			case ASSIGN_SUB:
			case ASSIGN_UNSIGNEDRSHIFT:
			case BIT_AND:
			case BIT_LSHIFT:
			case BIT_NOT:
			case BIT_OR:
			case BIT_RSHIFT:
			case BIT_UNSIGNEDRSHIFT:
			case BIT_XOR:
			case CAST:
			case CONDITIONAL:
			case NULLCOALESCE:
			case OP_DEC:
			case OP_INC:
			case ARRAYACCESS:

			case METHODCALL:
			case LITERAL:
			default:
				return e;
		}
	}

	 // Algorithm: Normalizing expression -> (arg0 + 3 + arg3 -1 <= arg1 + 1 + 5)
	 // Iteration 1
	 // Reduce step: arg0 + arg3 + 2 <= arg1 + 6
	 // Move step: arg0 + arg3 - arg1 <= 6 - 2
	 // Move Step == Reduce Step, False
	 // Iteration 2
	 // Reduce step: arg0 + arg3 - arg1 <= 4
	 // Move step: arg0 + arg3 - arg1 <= 4
	 // Move Step == Reduce Step, True, therefore break
	 // Sort Left side
	 // arg0 - arg1 + arg3 <= 4
	 // return

	/**
	 * Normalizes a given expression according to the above algorithm
	 *
	 * @param e the expression to be normalized
	 * @return the normalized expression
	 * @throws Exception
	 */
	@FunctionSpec(name = "normalize", returnType = "Expression", formalParameters = { "Expression" })
	public static Expression normalize(final Expression e) throws Exception {
		Expression expRed;
		Expression expMov = e;
		Expression previous = e;

		for (int i = 0; i < 5; i++) {	// maximum iteration allowed = 5. Ideally should not exceed 2
			expRed = reduce(expMov);	// reduce Expression. reduce is required before move
			expMov = move(expRed);		// move Variables to the left and literals to the right.
			if (expMov.equals(previous))
				break;
			previous = expMov;
		}

		return sort(expMov);			// sort the left side of the final expression
	}

	/**
	 * Rearranges a given expression with variables first, followed by literals
	 *
	 * @param e the expression to be moved
	 * @return moved expression
	 * @throws Exception
	 */
	private static Expression move(final Expression e) throws Exception {
		ExpressionKind kind = e.getKind();

		switch (kind) {
			// comparison operators
			case EQ:
			case NEQ:
			case GT:
			case LT:
			case GTEQ:
			case LTEQ:
				// Side and Signs are represented as booleans for optimization
				// Left = true, Right = false, Positive = true, Negative = false
				final List<Object[]> variableList = new ArrayList<Object[]>();
				final List<Object[]> literalList = new ArrayList<Object[]>();

				// if both sides are literals return the expression as is. For ex: 2 == 2 OR "Yes" == "Yes"
				if (e.getExpressions(0).getKind() == ExpressionKind.LITERAL &&
						e.getExpressions(1).getKind() == ExpressionKind.LITERAL)
					return e;

				// if string literal is to the left in the comparison then flip the expression and return it
				if (BoaAstIntrinsics.isStringLit(e.getExpressions(0))) {
					if (kind != ExpressionKind.EQ && kind != ExpressionKind.NEQ)
						kind = flipKind(kind);

					return createExpression(kind, e.getExpressions(1), e.getExpressions(0));
				}

				final Map<Integer, List<Object[]>> componentMapLeft = seperate(e.getExpressions(0), true, true);  // Call seperate on the left expression
				if (componentMapLeft.containsKey(-1))
					return e;
				if (componentMapLeft.containsKey(0))
					variableList.addAll(componentMapLeft.get(0));
				if (componentMapLeft.containsKey(1))
					literalList.addAll(componentMapLeft.get(1));

				final Map<Integer, List<Object[]>> componentMapRight = seperate(e.getExpressions(1), false, true);  // Call seperate on the right expression
				if (componentMapRight.containsKey(-1))
					return e;
				if (componentMapRight.containsKey(0))
					variableList.addAll(componentMapRight.get(0));
				if (componentMapRight.containsKey(1))
					literalList.addAll(componentMapRight.get(1));

				if (variableList.isEmpty())
					return e;
				final Expression variables = combineLeft(variableList);

				final Expression literals;
				if (!literalList.isEmpty())
					literals = combineRight(literalList);
				else
					literals = createExpression(ExpressionKind.OP_ADD, createLiteral("0"));

				return createExpression(kind, variables, literals);
			default:
				// no comparison operator return the expression as is
				return e;
		}
	}

	/**
	 * Sorts a given expression in alphabetical order with variables first, followed by literals
	 *
	 * @param e the expression to be sorted
	 * @return sorted expression
	 * @throws Exception
	 */
	private static Expression sort(final Expression e) throws Exception {
		ExpressionKind kind = e.getKind();

		switch (kind) {
			// comparison operators
			case EQ:
			case NEQ:
			case GT:
			case LT:
			case GTEQ:
			case LTEQ:
				final List<Object[]> leftList = new ArrayList<Object[]>();
				final List<Object[]> leftListLit = new ArrayList<Object[]>();
				final List<Object[]> rightList = new ArrayList<Object[]>();
				final List<Object[]> rightListLit = new ArrayList<Object[]>();

				/*
				   Each side of an expression is divided and stored in
				   two lists: one for variables and other for literals
				   The variable and literal lists are sorted seperately
				   for each side and then combined to form the respective sides.
				   The two sides are then combined to form the final sorted expression.
				 */
				final Map<Integer, List<Object[]>> componentMapLeft = seperate(e.getExpressions(0), true, true);
				if (componentMapLeft.containsKey(-1))
					return e;
				if (componentMapLeft.containsKey(0))
					leftList.addAll(componentMapLeft.get(0));
				if (componentMapLeft.containsKey(1))
					leftListLit.addAll(componentMapLeft.get(1));

				final Map<Integer, List<Object[]>> componentMapRight = seperate(e.getExpressions(1), false, true);
				if (componentMapRight.containsKey(-1))
					return e;
				if (componentMapRight.containsKey(0))
					rightList.addAll(componentMapRight.get(0));
				if (componentMapRight.containsKey(1))
					rightListLit.addAll(componentMapRight.get(1));

				Collections.sort(leftList, new ExpressionArrayComparator());
				Collections.sort(leftListLit, new ExpressionArrayComparator());
				Collections.sort(rightList, new ExpressionArrayComparator());
				Collections.sort(rightListLit, new ExpressionArrayComparator());

				leftList.addAll(leftListLit);
				rightList.addAll(rightListLit);

				if (leftList.isEmpty() || rightList.isEmpty())
					return e;

				// we want first component of left side to be positive
				// if first component is negative, flip all the signs and ExpressonKind
				if (((leftList.get(0))[2]).equals(false)) {
					for (int i = 0; i < leftList.size(); i++) {
						if (((leftList.get(i))[2]).equals(false))
							(leftList.get(i))[2] = true;
						else
							(leftList.get(i))[2] = false;
					}

					for (int i = 0; i < rightList.size(); i++) {
						if (((rightList.get(i))[2]).equals(false))
							(rightList.get(i))[2] = true;
						else
							(rightList.get(i))[2] = false;
					}

					if (kind != ExpressionKind.EQ && kind != ExpressionKind.NEQ)
						kind = flipKind(kind);
				}

				return createExpression(kind, combineLeft(leftList), combineRight(rightList));

			default:
				// no comparison operator
				// here, first component could be negative
				if (isNegative(e) && e.getExpressions(0) instanceof Expression && e.getExpressions(0).getKind() != ExpressionKind.LITERAL)
					return (Expression)negate(sort((Expression)negate(e)));
				final List<Object[]> variableList = new ArrayList<Object[]>();
				final List<Object[]> literalList = new ArrayList<Object[]>();

				final Map<Integer, List<Object[]>> componentMap = seperate(e, true, true);

				if (componentMap.containsKey(-1))
					return e;

				if (componentMap.containsKey(0))
					variableList.addAll(componentMap.get(0));
				if (componentMap.containsKey(1))
					literalList.addAll(componentMap.get(1));

				if (variableList.isEmpty() && literalList.isEmpty())
					return e;

				Collections.sort(variableList, new ExpressionArrayComparator());
				Collections.sort(literalList, new ExpressionArrayComparator());

				variableList.addAll(literalList);

				return combineLeft(variableList);
		}
	}

	/**
	 * Coverts a given expression into lists of sub expressions(VARCCESS, LITERAL etc)
	 *
	 * @param e the expression to be seperated
	 * @param side the side of the expression(left or right)
	 * @param sign the sign of the expression(positive or negative)
	 * @return the map of sub expression(variable list and literal list)
	 *         [0: Variable List] where Variable List is an ArrayList containing all VARACCESS occurances or MULT/DIV expressions
	 *         [1: Literal List] where Literal List is an ArrayList containing all LITERAL occurances
	 *         [-1: {true}] where -1 indicates occurance of a string or illegal expression which can not be changed
	 * @throws Exception
	 */
	private static Map<Integer, List<Object[]>> seperate(final Expression e, final boolean side, final boolean sign) throws Exception {
		final Map<Integer, List<Object[]>> componentMap = new LinkedHashMap<Integer,List<Object[]>>(3, 1);

		switch (e.getKind()) {
			case EQ:
			case NEQ:
			case GT:
			case LT:
			case GTEQ:
			case LTEQ:
				// if more than two comparison operator in one expression, then return it as is
				final List<Object[]> dummyList = new ArrayList<Object[]>();
				dummyList.add(new Object[] {true});
				componentMap.put(-1, dummyList);
				break;

			case OP_ADD:
				// break expression into sub expressions
				for (int i = 0; i < e.getExpressionsCount(); i++) {
					final Map<Integer, List<Object[]>> cMap = seperate(e.getExpressions(i), side, true);

					if (cMap.containsKey(0)) {
						if (componentMap.containsKey(0))
							componentMap.get(0).addAll(cMap.get(0));
						else
							componentMap.put(0, cMap.get(0));
					}

					if (cMap.containsKey(1)) {
						if (componentMap.containsKey(1))
							componentMap.get(1).addAll(cMap.get(1));
						else
							componentMap.put(1, cMap.get(1));
					}
				}
				break;

			case OP_SUB:
				// break expression into sub expressions
				for (int i = 0; i < e.getExpressionsCount(); i++) {
					final Map<Integer, List<Object[]>> cMap;

					if (i == 0 && e.getExpressionsCount() > 1)
						cMap = seperate(e.getExpressions(i), side, true);
					else
						cMap = seperate(e.getExpressions(i), side, false);

					if (cMap.containsKey(0)) {
						if (componentMap.containsKey(0))
							componentMap.get(0).addAll(cMap.get(0));
						else
							componentMap.put(0, cMap.get(0));
					}

					if (cMap.containsKey(1)) {
						if (componentMap.containsKey(1))
							componentMap.get(1).addAll(cMap.get(1));
						else
							componentMap.put(1, cMap.get(1));
					}
				}
				break;

			case OP_MULT:
			case OP_DIV:
				final List<Object[]> l = seperateNumDenom(e, 'n');
				final List<Expression> num= new ArrayList<Expression>();
				final List<Expression> den = new ArrayList<Expression>();

				Collections.sort(l, new ExpressionArrayComparator());
				int signCount = 0;
				for(final Object[] o: l) {
					if(((Expression)o[0]).getKind() == ExpressionKind.OP_SUB) {
						if(((Expression)o[0]).getExpressionsCount() == 1 ) {
							signCount++;
							o[0] = ((Expression) o[0]).getExpressions(0);
						}

					}
					// if((Byte)o[1] == 0)
					if(o[1].equals('n'))
						num.add((Expression) o[0]);
					else
						den.add((Expression) o[0]);
				}

				Expression sortExpr;
				Expression numerator = null;
				if (num.size() == 1)
					numerator = num.get(0);
				else
					numerator = createExpression(ExpressionKind.OP_MULT, num.toArray(new Expression[num.size()]));

				if(den.size() != 0) {
					Expression denominator = null;
					if (den.size() == 1)
						denominator = den.get(0);
					else
						denominator = createExpression(ExpressionKind.OP_MULT, den.toArray(new Expression[den.size()]));
					sortExpr = createExpression(ExpressionKind.OP_DIV, numerator, denominator);
				}
				else
					sortExpr = numerator;

				if(signCount % 2 == 1)
					sortExpr = createExpression(ExpressionKind.OP_SUB, sortExpr);

				final List<Object[]> vList = new ArrayList<Object[]>();
				vList.add(new Object[] {sortExpr, side, sign});
				componentMap.put(0, vList);
				break;

			case PAREN:
				componentMap.putAll(seperate(e.getExpressions(0), side, sign));
				break;

			case LITERAL:
				final List<Object[]> literalList = new ArrayList<Object[]>();
				if (BoaAstIntrinsics.isStringLit(e)) { // if it is a string expression, we don't want to process it
					literalList.add(new Object[] {true});
					componentMap.put(-1, literalList);
				} else {
					literalList.add(new Object[] {e, side, sign});
					componentMap.put(1, literalList);
				}
				break;

			case METHODCALL:
			case VARACCESS:
			default:
				final List<Object[]> variableList = new ArrayList<Object[]>();
				variableList.add(new Object[] {e, side, sign});
				componentMap.put(0, variableList);
				break;
		}

		return componentMap;
	}

	/**
	 * Breaks a Div/Mult expression into numerator and denominator
	 *
	 * @param expr to be seperated into numerator and denominator
	 * @param type numerator: 0, denominator: 1
	 * @return Returns a list containing numerators and denominators
	 * @throws Exception
	 */
	private static List<Object[]> seperateNumDenom(Expression expr, char type) throws Exception {
		final List<Object[]> result = new ArrayList<Object[]>();

		switch (expr.getKind()) {
			case OP_MULT:
				for (final Expression e : expr.getExpressionsList())
					result.addAll(seperateNumDenom(e, type));
				break;

			case OP_DIV:
				result.addAll(seperateNumDenom(expr.getExpressions(0), 'n'));
				for (int i = 1; i < expr.getExpressionsCount(); i++)
					result.addAll(seperateNumDenom(expr.getExpressions(i), 'd'));
				break;

			case OP_ADD:
			case OP_SUB:
			case PAREN:
				// these cases will not execute once the expression is fully siimplified
				result.add(new Object[] {normalize(expr), type});
				break;

			default:
				result.add(new Object[] {expr, type});
				break;
		}

		return result;
	}

	/**
	 * Combines an expression list based on the combine rules for the left side.
	 * The sign of the variable/literal is changed if they are from the right side
	 *
	 * @param expList the expression list of variable and literals
	 * @return the combined expression
	 */
	private static Expression combineLeft(final List<Object[]> expList) {
		Expression e = (Expression)(expList.get(0))[0];

		if(!(e.getKind() == ExpressionKind.LITERAL && e.getLiteral().equals("0"))) {
			if ((((expList.get(0))[1]).equals(true) && ((expList.get(0))[2]).equals(false)) ||
					(((expList.get(0))[1]).equals(false) && ((expList.get(0))[2]).equals(true)))
				e = createExpression(ExpressionKind.OP_SUB, e);
		}

		for (int i = 1; i < expList.size(); i++) {
			if ((((expList.get(i))[1]).equals(true) && ((expList.get(i))[2]).equals(true)) ||
					(((expList.get(i))[1]).equals(false) && ((expList.get(i))[2]).equals(false)) ||
					((Expression)(expList.get(i))[0]).getKind() == ExpressionKind.LITERAL && ((Expression)(expList.get(i))[0]).getLiteral().equals("0"))
				e = createExpression(ExpressionKind.OP_ADD, e, (Expression)(expList.get(i))[0]);
			else
				e = createExpression(ExpressionKind.OP_SUB, e, (Expression)(expList.get(i))[0]);
		}

		return e;
	}

	/**
	 * Combines an expression list based on the combine rules for the righ side.
	 * The sign of the variable/literal is changed if they are from the left side
	 *
	 * @param expList the expression list of variable and literals
	 * @return the combined expression
	 */
	private static Expression combineRight(final List<Object[]> expList) {
		Expression e = (Expression)(expList.get(0))[0];

		 if (!e.getLiteral().equals("0")) {
			 if ((((expList.get(0))[1]).equals(true) && ((expList.get(0))[2]).equals(true)) ||
					 (((expList.get(0))[1]).equals(false) && ((expList.get(0))[2]).equals(false)))
				 e = createExpression(ExpressionKind.OP_SUB, e);
		 }

		for (int i = 1; i < expList.size(); i++) {
			if ((((expList.get(i))[1]).equals(false) && ((expList.get(i))[2]).equals(true)) ||
					(((expList.get(i))[1]).equals(true) && ((expList.get(i))[2]).equals(false)) ||
						 ((Expression)(expList.get(i))[0]).getLiteral().equals("0"))
				e = createExpression(ExpressionKind.OP_ADD, e, (Expression)(expList.get(i))[0]);
			else
				e = createExpression(ExpressionKind.OP_SUB, e, (Expression)(expList.get(i))[0]);
		}

		return e;
	}

	/**
	 * Flips the ExpressionKind(operator)
	 *
	 * @param kind the ExpressionKind
	 * @return flipped kind
	 */
	private static ExpressionKind flipKind(final ExpressionKind kind) {

		switch (kind) {
			case GT:   return ExpressionKind.LT;
			case LT: return ExpressionKind.GT;

			case LTEQ:   return ExpressionKind.GTEQ;
			case GTEQ: return ExpressionKind.LTEQ;

			case EQ:   return ExpressionKind.EQ;
			case NEQ:  return ExpressionKind.NEQ;

			default: throw new RuntimeException("invalid ExpressionKind: " + kind);
		}
	}

	// Comparator for sorting array list
	private static class ExpressionArrayComparator implements Comparator<Object[]> {
		public int compare(final Object[] e1, final Object[] e2) {
			return prettyprint((Expression)e1[0]).compareTo(prettyprint((Expression)e2[0]));
		}
	}

	/**
	 * Temporary method to convert map to array
	 * @param m map of Expression
	 * @return array of Expression
	 */
	@FunctionSpec(name = "converttoarray", returnType = "array of Expression", formalParameters = { "map[int] of Expression" })
	public static Expression[] convertToArray(Map<Long, Expression> m) {
		final Expression[] a = new Expression[m.size()];
		for(int i = 0; i < m.size(); i++){
			a[i] = m.get((long)i);
		}
		return a;
	}


	/**
	 * Attempts to reduce an expression, simplifying wherever possible.
	 *
	 * @param e the expression to reduce
	 * @return the reduced form of the expression
	 */
	@FunctionSpec(name = "reduce", returnType = "Expression", formalParameters = { "Expression" })
	public static Expression reduce(final Expression e) throws Exception {
		Object o = internalReduce(e);
		if (o instanceof Expression) {
			Object o2 = factorAll((Expression)o);
			if (o != o2)
				o = internalReduce((Expression)o2);
			if (o instanceof Expression) {
				o2 = distributeAll((Expression)o);
				if (o != o2)
					o = internalReduce((Expression)o2);
			}
			return finalReduce((Expression)o);
		}
		return createLiteral(o.toString());
	}

	/**
	 * Attempts to reduce an expression, simplifying wherever possible.
	 *
	 * @param e the expression to reduce
	 * @return the reduced form of the expression, either as a Number or a complex Expression
	 */
	private static Object internalReduce(final Expression e) throws Exception {
		final Expression.Builder b;
		final List<Object> results = new ArrayList<Object>();
		for (final Expression sub : e.getExpressionsList())
			results.add(internalReduce(sub));

		final List<Object> results2 = new ArrayList<Object>();
		Double dval = 0.0;
		Long ival = 0L;
		boolean first = true;
		boolean isDouble = false;

		switch (e.getKind()) {
			// reduce both sides of the comparison
			case EQ:
			case NEQ:
			case GT:
			case LT:
			case GTEQ:
			case LTEQ:
				final Expression[] results1 = new Expression[results.size()] ;
				for(int i = 0; i < results.size(); i++) {
					if (results.get(i) instanceof Long || results.get(i) instanceof Double)
						results1[i] = createLiteral(results.get(i).toString());
					else
						results1[i] = (Expression)results.get(i);
				}
				return createExpression(e.getKind(), results1);

			case OP_ADD:
				// handle cases like '+x' or '+3'
				if (results.size() == 1) {
					final Object o = results.get(0);
					if (o instanceof Expression)
						return internalReduce(e.getExpressions(0));
					return o;
				}

				// bring children up if the child node is an ADD or SUB
				for (int i = 0; i < results.size(); i++) {
					if (results.get(i) instanceof Expression) {
						final Expression subExp = (Expression)results.get(i);
						switch (((Expression)results.get(i)).getKind()) {
							case OP_ADD:
								results.remove(i);
								for (int j = 0; j < subExp.getExpressionsCount(); j++)
									results.add(i + j, internalReduce(subExp.getExpressions(j)));
								break;
							case OP_SUB:
								if (subExp.getExpressionsCount() > 1) {
									results.set(i, internalReduce(subExp.getExpressions(0)));
									for (int j = 1; j < subExp.getExpressionsCount(); j++)
										results.add(i + j, internalReduce(negateExpression(subExp.getExpressions(j))));
								}
								break;
							default:
								break;
						}
					}
				}

				// if multiple arguments, try to add them all together
				for (final Object o : results) {
					if (o instanceof Expression) {
						results2.add(o);
					} else if (o instanceof Double) {
						dval += ((Double)o).doubleValue();
						isDouble = true;
					} else if (o instanceof Long) {
						ival += ((Long)o).longValue();
					}
				}

				// both float and integer results, so merge them into float
				if (isDouble) {
					dval += ival;
					ival = 0L;
				}

				// after merging, add the one that remains to results
				if (isDouble)
					results2.add(0, dval);
				else
					results2.add(0, ival);

				if (results2.size() > 1) {
					// group common terms
					final Map<Expression, ArrayList<Integer>> commonMap = new LinkedHashMap<Expression, ArrayList<Integer>>();
					final Map<Expression, Double> doubleCountMap = new LinkedHashMap<Expression, Double>();

					// skip the first term since the first term is always literal
					for (int i = 1; i < results2.size(); i++) {
						boolean noLiteral = false;

						// if Mult, like 2 * x
						if ((results2.get(i) instanceof Expression && ((Expression)results2.get(i)).getKind() == ExpressionKind.OP_MULT)) {
							final Expression subExp = (Expression)results2.get(i);
							int literalCount = 0;
							int literalIndex = -1;
							boolean negLiteral = false;
							boolean negExp = false;
							Expression literal = null;
							Expression exp = null;
							for (int j = 0; j < subExp.getExpressionsCount(); j++){
								if (subExp.getExpressions(j).getKind() == ExpressionKind.LITERAL) {
									literal = subExp.getExpressions(j);
									literalCount++;
									literalIndex = j;
								} else if (subExp.getExpressions(j).getKind() == ExpressionKind.OP_SUB && ((Expression)negate(subExp.getExpressions(j))).getKind() == ExpressionKind.LITERAL) {
									literal = (Expression)negate(subExp.getExpressions(j));
									negLiteral = true;
									literalCount++;
									literalIndex = j;
								} else if (subExp.getExpressions(j).getKind() == ExpressionKind.OP_SUB && ((Expression)subExp.getExpressions(j)).getExpressionsCount() == 1) {
									negExp = !negExp;
									exp = (Expression)negate(subExp.getExpressions(j));
								} else {
									exp = (Expression)subExp.getExpressions(j);
								}
							}
							if (literalCount == 1) {
								// for cases like 3 * x * y
								if (subExp.getExpressionsCount() != 2) {
									final List<Object> tempExpList = new ArrayList<Object>();
									for (int j = 0; j < subExp.getExpressionsCount(); j++) {
										if (j != literalIndex)
											tempExpList.add(subExp.getExpressions(j));
									}
									exp = (Expression)internalReduce(createExpression(subExp.getKind(), convertArray(tempExpList)));
								}
								if (commonMap.containsKey(exp)) {
									final ArrayList<Integer> ary = commonMap.get(exp);
									ary.add(i);
									commonMap.put(exp, ary);
									if ((negLiteral || negExp) && !(negLiteral && negExp))
										doubleCountMap.put(exp, doubleCountMap.get(exp) - Double.parseDouble(literal.getLiteral()));
									else
										doubleCountMap.put(exp, doubleCountMap.get(exp) + Double.parseDouble(literal.getLiteral()));
								} else {
									final ArrayList<Integer> ary = new ArrayList<Integer>();
									ary.add(i);
									commonMap.put(exp, ary);
									if ((negLiteral || negExp) && !(negLiteral && negExp))
										doubleCountMap.put(exp, Double.parseDouble(literal.getLiteral()) * -1);
									else
										doubleCountMap.put(exp, Double.parseDouble(literal.getLiteral()));
								}
							}
							// for cases like x * y or x * y * z
							else {
								noLiteral = true;
							}
						}

						// regular common terms
						if (noLiteral || (results2.get(i) instanceof Expression && ((Expression)results2.get(i)).getKind() != ExpressionKind.OP_MULT)) {
							boolean negExp = false;
							Expression exp = (Expression)internalReduce((Expression)results2.get(i));

							if (isNegative(exp)) {
								negExp = true;
								exp = (Expression)negate(exp);
							}

							if (commonMap.containsKey(exp)) {
								final ArrayList<Integer> ary = commonMap.get(exp);
								ary.add(i);
								commonMap.put(exp, ary);
								if (negExp)
									doubleCountMap.put(exp, doubleCountMap.get(exp) - 1.0);
								else
									doubleCountMap.put(exp, doubleCountMap.get(exp) + 1.0);
							}
							else {
								final ArrayList<Integer> ary = new ArrayList<Integer>();
								ary.add(i);
								commonMap.put(exp, ary);
								if (negExp)
									doubleCountMap.put(exp, -1.0);
								else
									doubleCountMap.put(exp, 1.0);
							}
						}
					}

					// record the repeated index and add common terms at the end
					boolean[] repeat = new boolean[results2.size()];

					for (final Map.Entry<Expression, ArrayList<Integer>> entry : commonMap.entrySet()) {
						final ArrayList<Integer> ary = entry.getValue();

						if (ary.size() > 1) {
							final Expression o = entry.getKey();

							// record index
							for (final Integer i : ary)
								repeat[i] = true;

							if (doubleCountMap.get(o) != 0.0) {
								if (doubleCountMap.get(o) == 1.0)
									results2.add(o);
								else if (doubleCountMap.get(o) == -1.0)
									results2.add(negate(o));
								else if (doubleCountMap.get(o) % 1 == 0)
									results2.add(internalReduce(createExpression(ExpressionKind.OP_MULT, createLiteral("" + Integer.toString(doubleCountMap.get(o).intValue())), o)));
								else
									results2.add(internalReduce(createExpression(ExpressionKind.OP_MULT, createLiteral("" + Double.toString(doubleCountMap.get(o))), o)));
							}
						}
					}

					// remove common terms
					for (int i = repeat.length - 1; i >= 0; i--)
						if (repeat[i])
							results2.remove(i);

					// check for identity
					if (results2.get(0) instanceof Number && ((Number)results2.get(0)).doubleValue() == 0.0 && results2.size() > 1)
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

					// double negatives
					if (isNegative(o)) {
						final Object neg = negate(o);
						if (neg instanceof Expression)
							return internalReduce((Expression)neg);
						return neg;
					}

					return negate(o);
				}

				// for 2+ term cases, we convert to an OP_ADD with each RHS term negated
				// e.g., x - 2 - y becomes x + -2 + -y
				for (int i = 1; i < results.size(); i++)
					results.set(i, negate(results.get(i)));

				// then we reduce the new OP_ADD
				return internalReduce(createExpression(ExpressionKind.OP_ADD, convertArray(results)));

			case OP_MULT:
				dval = 1.0;
				ival = 1L;

				// bring children up if the child node is an mult
				for (int i = 0; i < results.size(); i++)
					if (results.get(i) instanceof Expression && ((Expression)results.get(i)).getKind() == ExpressionKind.OP_MULT) {
						final Expression subExp = (Expression)results.get(i);
						results.remove(i);
						for (int j = 0; j < subExp.getExpressionsCount(); j++)
							results.add(i + j, internalReduce(subExp.getExpressions(j)));
					}

				// if multiple arguments, try to multiply them all together
				for (final Object o : results) {
					if (o instanceof Expression) {
						results2.add(o);
					} else if (o instanceof Double) {
						dval *= ((Double)o).doubleValue();
						isDouble = true;
					} else if (o instanceof Long) {
						ival *= ((Long)o).longValue();
					}
				}

				// both float and integer results, so merge them into float
				if (isDouble) {
					dval *= ival;
					ival = 1L;
				}

				// after merging, add the one that remains to results
				if (isDouble)
					results2.add(0, dval);
				else
					results2.add(0, ival);

				if (results2.size() > 1) {
					// check for elimination
					if (results2.get(0) instanceof Double && (Double)results2.get(0) == 0.0)
						return 0.0;
					if (results2.get(0) instanceof Long && (Long)results2.get(0) == 0L)
						return 0L;

					// check for identity
					boolean hasNumLiteral = true;
					if (results2.get(0) instanceof Number && ((Number)results2.get(0)).doubleValue() == 1.0) {
						results2.remove(0);
						hasNumLiteral = false;
					} else if (results2.get(0) instanceof Number && ((Number)results2.get(0)).doubleValue() == -1.0) {
						results2.remove(0);
						results2.set(0, negate(results2.get(0)));
						hasNumLiteral = false;
					}

					// only the first term can remain negative
					boolean positive = true;
					for (int i = results2.size() - 1; i >= 0; i--)
						if (isNegative(results2.get(i))) {
							positive = !positive;
							results2.set(i, negate(results2.get(i)));
						}

					// simplify terms and literal
					dval = 1.0;
					ival = 1L;

					boolean[] cancelTerms = new boolean[results2.size()];

					final Map<Expression, List<Integer>> numMap = new LinkedHashMap<Expression, List<Integer>>();
					final Map<Expression, List<Integer>> denMap = new LinkedHashMap<Expression, List<Integer>>();

					for (int i = hasNumLiteral ? 1 : 0; i < results2.size(); i++) {
						final Expression subExp = (Expression)results2.get(i);

						// if the term is like 1 / x or 1 / 2
						if (subExp.getKind() == ExpressionKind.OP_DIV && subExp.getExpressions(0).getKind() == ExpressionKind.LITERAL) {
							// if denominator is a number (1 / 2)
							if (subExp.getExpressions(1).getKind() == ExpressionKind.LITERAL) {
								final Object literal = internalReduce(subExp.getExpressions(1));
								if (literal instanceof Double) {
									dval *= ((Double)literal).doubleValue();
									isDouble = true;
								} else {
									ival *= ((Long)literal).longValue();
								}
								cancelTerms[i] = true;
							// if regular denominator term
							} else {
								// if able to cancel
								final Expression subExp2 = subExp.getExpressions(1);
								if (numMap.containsKey(subExp2)) {
									final List<Integer> aryList = numMap.get(subExp2);
									cancelTerms[aryList.get(0)] = true;
									cancelTerms[i] = true;
									aryList.remove(0);
									if (aryList.size() == 0)
										numMap.remove(subExp2);
								} else {
									if (!denMap.containsKey(subExp2))
										denMap.put(subExp2, new ArrayList<Integer>());
									denMap.get(subExp2).add(i);
								}
							}
						// if regular term like x or y
						} else {
							// if able to cancel
							if (results2.get(i) instanceof Expression) {
								final Expression subExp2 = (Expression) results2.get(i);
								if (denMap.containsKey(subExp2)) {
									final List<Integer> aryList = denMap.get(subExp2);
									cancelTerms[aryList.get(0)] = true;
									cancelTerms[i] = true;
									aryList.remove(0);
									if (aryList.size() == 0)
										denMap.remove(subExp2);
								} else {
									if (!numMap.containsKey(subExp2))
										numMap.put(subExp2, new ArrayList<Integer>());
									numMap.get(subExp2).add(i);
								}
							}
						}
					}

					for (int i = results2.size() - 1; i >= 0; i--)
						if (cancelTerms[i])
							results2.remove(i);

					if (isDouble) {
						dval *= ival;
						ival = 1L;
					}

					if (hasNumLiteral && (dval != 1.0 || ival != 1L)) {
						final Object numLiteral = results2.get(0);
						Number num = null;
						if (dval != 1.0)
							num = ((Number)numLiteral).doubleValue() / dval;
						else if (numLiteral instanceof Double)
							num = ((Number)numLiteral).doubleValue() / ival.doubleValue();
						else
							num = ((Number)numLiteral).longValue() / ival;
						if (num.doubleValue() == 1.0) {
							results2.remove(0);
							if (isDouble)
								for (int i = 0; i < results2.size(); i++)
									results2.set(i, toDouble(results2.get(i)));
						} else
							results2.set(0, num);
					} else {
						if (dval != 1.0)
							results2.add(createExpression(ExpressionKind.OP_DIV, createLiteral("1"), createLiteral("" + dval)));
						else if (ival != 1L)
							results2.add(createExpression(ExpressionKind.OP_DIV, createLiteral("1"), createLiteral("" + ival)));
					}

					if (results2.size() == 0) {
						if (isDouble)
							return positive ? 1.0 : -1.0;
						return positive ? 1L : -1L;
					}

					if (results2.size() > 0 && !positive) {
						if (results2.get(0) instanceof Expression && ((Expression)results2.get(0)).getKind() == ExpressionKind.OP_DIV) {
							b = Expression.newBuilder((Expression)results2.get(0));
							b.setExpressions(0, (Expression)negate(b.getExpressions(0)));
							results2.set(0, b.build());
						} else {
							results2.set(0, negate(results2.get(0)));
						}
					}
				}

				// if it reduced to a single term, return just the term otherwise return the whole expression
				if (results2.size() == 1)
					return results2.get(0);
				return createExpression(e.getKind(), convertArray(results2));

			case OP_DIV:
				if (results.size() == 2 && !(results.get(0) instanceof Expression))
					if (((Number)results.get(0)).doubleValue() == 1.0 || ((Number)results.get(0)).doubleValue() == -1.0)
						if (!(results.get(1) instanceof Expression) || ((Expression)results.get(1)).getKind() != ExpressionKind.OP_MULT)
							return e;

				int count = 0;
				for (int i = 0; i < results.size(); i++) {
					if (i != 0 && isNegative(results.get(i))) {
						results.set(i, negate(results.get(i)));
						count++;
					}
					if (i != 0)
						results.set(i, inverse(results.get(i)));
				}

				if (count % 2 == 1)
					results.set(0, negate(results.get(0)));

				return internalReduce(createExpression(ExpressionKind.OP_MULT, convertArray(results)));

			// literals are converted to numbers, if possible
			case LITERAL:
				if (BoaAstIntrinsics.isIntLit(e))
					return Long.decode(e.getLiteral());
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
			case ASSIGN:
			case ASSIGN_ADD:
			case ASSIGN_BITAND:
			case ASSIGN_BITOR:
			case ASSIGN_BITXOR:
			case ASSIGN_DIV:
			case ASSIGN_LSHIFT:
			case ASSIGN_MOD:
			case ASSIGN_MULT:
			case ASSIGN_RSHIFT:
			case ASSIGN_SUB:
			case ASSIGN_UNSIGNEDRSHIFT:
			case ARRAYACCESS:
			case ARRAYINIT:
			case BIT_AND:
			case BIT_LSHIFT:
			case BIT_NOT:
			case BIT_OR:
			case BIT_RSHIFT:
			case BIT_UNSIGNEDRSHIFT:
			case BIT_XOR:
			case CAST:
			case CONDITIONAL:
			case LOGICAL_AND:
			case LOGICAL_NOT:
			case LOGICAL_OR:
			case NULLCOALESCE:
			case OP_DEC:
			case OP_INC:
			case OP_MOD:
			case VARACCESS:
/* TODO handle these expression kinds
			case ANNOTATION:
			case LAMBDA:
			case METHOD_REFERENCE:
			case NEW:
			case NEWARRAY:
			case TYPECOMPARE:
			case VARDECL:
*/
			default:
				if (results.size() == 0)
					return e;

				// want the same message, but with new sub-expressions
				b = Expression.newBuilder(e);

				b.clearExpressions();
				for (final Object o : results) {
					if (o instanceof Long || o instanceof Double)
						b.addExpressions(createLiteral(o.toString()));
					else
						b.addExpressions((Expression) o);
				}
				return b.build();
		}
	}

	/**
	 * Converts literals in an expression into doubles.
	 */
	private static Object toDouble(final Object o) {
		if (o instanceof Number)
			return ((Number)o).doubleValue();

		final Expression.Builder b = Expression.newBuilder((Expression)o);
		for (int i = 0; i < b.getExpressionsCount(); i++) {
			final Expression sub = b.getExpressions(i);
			if (sub.getKind() == ExpressionKind.LITERAL)
				b.setExpressions(i, createLiteral("" + Double.parseDouble(sub.getLiteral())));
			else
				b.setExpressions(i, (Expression)toDouble(sub));
		}

		return b.build();
	}

	/**
	 * takes an expression to distribute recursively
	 *
	 * @param e the expression to distribute
	 * @return the distribute form of the expression
	 */
	private static Expression distributeAll(final Expression e) throws Exception {
		if (e.getExpressionsCount() == 1 && e.getKind() != ExpressionKind.PAREN)
			return e;

		final List<Object> results = new ArrayList<Object>();
		for (final Expression sub : e.getExpressionsList())
			results.add(sub);

		switch (e.getKind()) {
			case OP_ADD:
				for (int i = 0; i < results.size(); i++) {
					if (results.get(i) instanceof Expression &&
						(((Expression)results.get(i)).getKind() == ExpressionKind.OP_MULT ||
						(((Expression)results.get(i)).getKind() == ExpressionKind.PAREN &&
						((Expression)((Expression)results.get(i)).getExpressions(0)).getKind() == ExpressionKind.OP_MULT)))
						results.set(i, distributeAll((Expression)results.get(i)));
				}
				return createExpression(e.getKind(), convertArray(results));

			case OP_MULT:
				boolean hasAdd = false;
				for (int i = 0; i < results.size(); i++) {
					if (results.get(i) instanceof Expression) {
						Expression subExp = (Expression)results.get(i);
						if (subExp.getKind() == ExpressionKind.PAREN)
							subExp = subExp.getExpressions(0);
						if (subExp.getKind() == ExpressionKind.OP_ADD)
							hasAdd = true;
						else if (subExp.getKind() == ExpressionKind.OP_DIV &&
								(subExp.getExpressions(1).getKind() == ExpressionKind.OP_ADD ||
								(subExp.getExpressions(1).getKind() == ExpressionKind.PAREN &&
								subExp.getExpressions(1).getExpressions(0).getKind() == ExpressionKind.OP_ADD))) {
							hasAdd = true;
						}
					}
				}
				if (hasAdd)
					return distribute(e);
			case PAREN:
				if(results.get(0) instanceof Expression)
					results.set(0, distributeAll((Expression)results.get(0)));
				return createExpression(e.getKind(), convertArray(results));
			default:
				break;
		}
		return e;
	}

	/**
	 * takes an OP_MULT expression to distribute the terms
	 *
	 * @param e the expression to distribute
	 * @return the distribute form of the expression
	 */
	private static Expression distribute(final Expression e) throws Exception {
		if (e.getKind() != ExpressionKind.OP_MULT)
			return e;

		final List<Object> results = new ArrayList<Object>();
		final List<Object> divResults = new ArrayList<Object>();
		final List<Object> addResults = new ArrayList<Object>();
		final List<List<Object>> addList = new ArrayList<List<Object>>();

		int addTermsNum = 1;
		boolean divHasAdd = false;
		for (Expression sub : e.getExpressionsList()) {
			if (sub.getKind() == ExpressionKind.PAREN)
				sub = sub.getExpressions(0);
			if (sub.getKind() == ExpressionKind.OP_ADD) {
				addResults.add(sub);
				addTermsNum *= sub.getExpressionsCount();
			}
			else if (sub.getKind() == ExpressionKind.OP_DIV) {
				if (sub.getExpressions(1).getKind() == ExpressionKind.OP_ADD ||
					(sub.getExpressions(1).getKind() == ExpressionKind.PAREN &&
					sub.getExpressions(1).getExpressions(0).getKind() == ExpressionKind.OP_ADD)) {
					divHasAdd = true;
				}
				divResults.add(sub.getExpressions(1));
			}
			else
				results.add(sub);
		}

		if (addResults.size() == 0 && !divHasAdd)
			return e;

		// combine div terms
		if (divHasAdd && divResults.size() > 1)
			results.add(createExpression(ExpressionKind.OP_DIV, createLiteral("1"), distribute(createExpression(ExpressionKind.OP_MULT, convertArray(divResults)))));
		else
			for (Object o: divResults)
				results.add(createExpression(ExpressionKind.OP_DIV, createLiteral("1"), (Expression) o));

		// initialize addList
		for (int i = 0; i < addTermsNum; i++) {
			List<Object> l = new ArrayList<Object>(results);
			addList.add(l);
		}

		// multiply the terms
		int continuousNum = addTermsNum;
		for (int i = 0; i < addResults.size(); i++) {
			Expression subExp = (Expression)addResults.get(i);
			int index = 0;
			continuousNum = continuousNum / subExp.getExpressionsCount();
			while (index < addTermsNum) {
				for (int j = 0; j < subExp.getExpressionsCount(); j++) {
					for (int k = 0; k < continuousNum; k++) {
						List<Object> l = addList.get(index);
						l.add(subExp.getExpressions(j));
						index++;
					}
				}
			}
		}

		// add mutiplied terms
		results.clear();
		for (int i = 0; i < addTermsNum; i++) {
			Object temp = internalReduce(createExpression(ExpressionKind.OP_MULT, convertArray(addList.get(i))));
			if (temp instanceof Expression)
				results.add(sort((Expression)temp));
			else
				results.add(temp);
		}

		return createExpression(ExpressionKind.OP_ADD, convertArray(results));
	}

	/**
	 * takes an expression to factor recursively
	 *
	 * @param e the expression to distribute
	 * @return the factor form of the expression
	 */
	private static Expression factorAll(final Expression e) throws Exception {
		if (e.getExpressionsCount() == 1 && e.getKind() != ExpressionKind.PAREN)
			return e;

		final List<Object> results = new ArrayList<Object>();
		for (final Expression sub : e.getExpressionsList())
			results.add(sub);

		switch (e.getKind()) {
			case OP_ADD:
				for (int i = 0; i < results.size(); i++) {
					if (results.get(i) instanceof Expression &&
						(((Expression)results.get(i)).getKind() == ExpressionKind.OP_MULT ||
						(((Expression)results.get(i)).getKind() != ExpressionKind.PAREN ||
						((Expression)((Expression)results.get(i)).getExpressions(0)).getKind() != ExpressionKind.OP_MULT)))
						results.set(i, factorAll((Expression)results.get(i)));
				}

			case OP_MULT:
				boolean hasDiv = false;
				boolean hasAdd = false;
				List<Integer> indexList = new ArrayList<Integer>();
				for (int i = 0; i < results.size(); i++) {
					if (results.get(i) instanceof Expression &&
						(((Expression)results.get(i)).getKind() == ExpressionKind.OP_DIV ||
						(((Expression)results.get(i)).getKind() == ExpressionKind.PAREN &&
						((Expression)((Expression)results.get(i)).getExpressions(0)).getKind() == ExpressionKind.OP_DIV))) {
						hasDiv = true;
						results.set(i, factorAll((Expression)results.get(i)));
					}
					if (results.get(i) instanceof Expression &&
						(((Expression)results.get(i)).getKind() == ExpressionKind.OP_ADD ||
						(((Expression)results.get(i)).getKind() == ExpressionKind.PAREN &&
						((Expression)((Expression)results.get(i)).getExpressions(0)).getKind() == ExpressionKind.OP_ADD))) {
						hasAdd = true;
						indexList.add(i);
					}
				}
				if (hasDiv && hasAdd) {
					for (Integer i : indexList) {
						if (((Expression)results.get(i)).getKind() == ExpressionKind.PAREN)
							results.set(i, createExpression(ExpressionKind.PAREN, factor(((Expression)results.get(i)).getExpressions(0))));
						else
							results.set(i, factor((Expression)results.get(i)));
					}
					return createExpression(e.getKind(), convertArray(results));
				}

			case OP_DIV:
				for (int i = 0; i < results.size(); i++) {
					if (results.get(i) instanceof Expression &&
						((Expression)results.get(i)).getKind() == ExpressionKind.OP_ADD)
						results.set(i, factor((Expression)results.get(i)));
					if (results.get(i) instanceof Expression &&
						((Expression)results.get(i)).getKind() == ExpressionKind.PAREN &&
						((Expression)((Expression)results.get(i)).getExpressions(0)).getKind() == ExpressionKind.OP_ADD)
						results.set(i, createExpression(ExpressionKind.PAREN, factor(((Expression)results.get(i)).getExpressions(0))));
				}
				return createExpression(e.getKind(), convertArray(results));

			case PAREN:
				if(results.get(0) instanceof Expression)
					results.set(0, factorAll((Expression)results.get(0)));
				return createExpression(e.getKind(), convertArray(results));

			default:
				break;
		}
		return e;
	}

	/**
	 * takes an OP_ADD or OP_SUB expression to factor out the common terms
	 *
	 * @param e the expression to factor
	 * @return the factored form of the expression
	 */
	private static Expression factor(final Expression e) throws Exception {
		if (e.getKind() != ExpressionKind.OP_ADD && e.getKind() != ExpressionKind.OP_SUB)
			return e;

		final List<Object> results = new ArrayList<Object>();
		for (final Expression sub : e.getExpressionsList())
			results.add(sub);

		final Map<Expression, Integer> commonTermMap = new LinkedHashMap<Expression, Integer>();
		final List<Object> commonResults = new ArrayList<Object>();
		boolean allNegative = true;
		final boolean[] negativeList = new boolean[results.size()];

		// get first terms
		for (int i = 0; i < ((Expression)results.get(0)).getExpressionsCount(); i++) {
			Expression exp = ((Expression)results.get(0)).getExpressions(i);
			if (i == 0 && isNegative(exp)) {
				results.set(0, negate(results.get(0)));
				exp = (Expression)negate(exp);
				negativeList[0] = true;
			}

			if (commonTermMap.containsKey(exp))
				commonTermMap.put(exp, commonTermMap.get(exp) + 1);
			else
				commonTermMap.put(exp, 1);
		}

		// scan through results list and get the common terms
		for (int i = 1; i < results.size(); i++) {
			final Map<Expression, Integer> tempMap = new LinkedHashMap<Expression, Integer>();
			for (int j = 0; j < ((Expression)results.get(i)).getExpressionsCount(); j++) {
				Expression exp = ((Expression)results.get(i)).getExpressions(j);
				if (j == 0 && isNegative(exp)) {
					results.set(i, negate(results.get(i)));
					exp = (Expression)negate(exp);
					negativeList[i] = true;
				}
				if (commonTermMap.containsKey(exp)) {
					if (tempMap.containsKey(exp))
						tempMap.put(exp, tempMap.get(exp) + 1);
					else
						tempMap.put(exp, 1);

					if (commonTermMap.get(exp) == 1)
						commonTermMap.remove(exp);
					else
						commonTermMap.put(exp, commonTermMap.get(exp) - 1);
				}
			}

			if (tempMap.size() == 0)
				return e;

			commonTermMap.clear();
			commonTermMap.putAll(tempMap);
		}

		// see if all terms are negative
		for (int i = 0; i < results.size(); i++) {
			if (!negativeList[i]) {
				allNegative = false;
				break;
			}
		}

		// remove the common terms
		for (int i = 0; i < results.size(); i++) {
			final Map<Expression, Integer> tempMap = new LinkedHashMap<Expression, Integer>(commonTermMap);
			final Expression.Builder b = Expression.newBuilder((Expression)results.get(i));
			for (int j = ((Expression)results.get(i)).getExpressionsCount() - 1; j >= 0; j--) {
				final Expression subExp = ((Expression)results.get(i)).getExpressions(j);
				if (tempMap.containsKey(subExp)) {
					b.removeExpressions(j);
					if (tempMap.get(subExp) == 1)
						tempMap.remove(subExp);
					else
						tempMap.put(subExp, tempMap.get(subExp) - 1);
				}
			}
			if (b.getExpressionsCount() == 0)
				b.addExpressions(createLiteral("1"));
			results.set(i, b.build());
			if (!allNegative && negativeList[i] && b.getExpressionsCount() != 0)
				results.set(i, negate(results.get(i)));
		}

		// create common terms' expression
		for (final Map.Entry<Expression, Integer> entry : commonTermMap.entrySet()) {
			for (int i = 0; i < entry.getValue(); i++) {
				commonResults.add(entry.getKey());
			}
		}

		// negate the first term is all negative
		if (allNegative)
			commonResults.set(0, negate(commonResults.get(0)));

		Object e2 = internalReduce(createExpression(e.getKind(), convertArray(results)));
		if (e2 instanceof Expression)
			e2 = finalReduce((Expression)e2);

		// return final expression
		return createExpression(ExpressionKind.OP_MULT, createExpression(ExpressionKind.OP_MULT, convertArray(commonResults)), (Expression)e2);
	}

	/**
	 * Fixes the expression to remove cases like "x + -y", converting them to the proper "x - y" form.
	 *
	 * @param e the expression to reduce
	 * @return the reduced form of the expression
	 */
	private static Expression finalReduce(final Expression e) throws Exception {
		if (e.getExpressionsCount() == 1 && e.getKind() != ExpressionKind.PAREN)
			return e;

		final List<Object> results = new ArrayList<Object>();
		for (final Expression sub : e.getExpressionsList())
			results.add(sub);

		switch (e.getKind()) {
			case OP_ADD:
				// resolve all + - issues (3 + -x)
				final List<Object> negResult = new ArrayList<Object>();
				for (int i = results.size() - 1; i >= 0; i--) {
					if (i == 0 &&
						((Expression)results.get(i)).getKind() == ExpressionKind.OP_SUB &&
						((Expression)results.get(i)).getExpressions(0).getKind() == ExpressionKind.LITERAL)
						break;

					if (isNegative(results.get(i))) {
						negResult.add(0, negate(results.get(i)));
						results.remove(i);
					}
				}

				// append the neg terms to the end and return whole as OP_SUB
				if (negResult.size() > 0) {
					if (results.size() == 0)
						negResult.set(0, negate(negResult.get(0)));
					else if (results.size() == 1)
						negResult.add(0, results.get(0));
					else
						negResult.add(0, createExpression(e.getKind(), convertArray(results)));
					for (int i = 0; i < negResult.size(); i++)
						negResult.set(i, finalReduce((Expression)negResult.get(i)));
					return createExpression(ExpressionKind.OP_SUB, convertArray(negResult));
				}
				for (int i = 0; i < results.size(); i++) {
					results.set(i, finalReduce((Expression)results.get(i)));
				}
				return createExpression(e.getKind(), convertArray(results));

			case OP_MULT:
				// push all divs at the end
				final List<Object> numeratorList = new ArrayList<Object>();
				final List<Object> denominatorList = new ArrayList<Object>();
				for (int i = results.size() - 1; i >= 0; i--) {
					if (!(results.get(i) instanceof Expression))
						continue;
					results.set(i, finalReduce((Expression)results.get(i)));
					if (((Expression)results.get(i)).getKind() != ExpressionKind.OP_DIV &&
						(((Expression)results.get(i)).getKind() != ExpressionKind.PAREN ||
						((Expression)((Expression)results.get(i)).getExpressions(0)).getKind() != ExpressionKind.OP_DIV))
						continue;
					Expression subExp = ((Expression)results.get(i)).getExpressions(0);
					if (((Expression)results.get(i)).getKind() != ExpressionKind.PAREN)
						subExp = (Expression)results.get(i);
					results.remove(i);
					// if numerator is not 1
					if (subExp.getExpressions(0).getKind() != ExpressionKind.LITERAL ||
						Double.parseDouble(subExp.getExpressions(0).getLiteral()) != 1.0){
						numeratorList.add(0, subExp.getExpressions(0));
					}
					for (int j = subExp.getExpressionsCount() - 1; j > 0; j--) {
						denominatorList.add(0, subExp.getExpressions(j));
					}
				}
				// if there are new numerators
				if (numeratorList.size() > 0)
					for (Object o : numeratorList)
						results.add(o);

				// simplify denominatorList
				if (denominatorList.size() > 1) {
					final Object multDiv = internalReduce(createExpression(ExpressionKind.OP_MULT, convertArray(denominatorList)));
					denominatorList.clear();
					if (multDiv instanceof Expression)
						for (final Expression sub : ((Expression)multDiv).getExpressionsList())
							denominatorList.add(sub);
					else
						denominatorList.add(createLiteral("" + multDiv));
				}
				if (denominatorList.size() > 0) {
					// if one div
					if (denominatorList.size() == 1) {
						if (results.size() == 0)
							denominatorList.add(0, createLiteral("1"));
						else if (results.size() == 1)
							denominatorList.add(0, results.get(0));
						else
							denominatorList.add(0, createExpression(e.getKind(), convertArray(results)));
						return createExpression(ExpressionKind.OP_DIV, convertArray(denominatorList));
					}
					// mult the div
					else {
						if (results.size() == 0)
							results.add(createLiteral("1"));
						final Object multDiv = createExpression(ExpressionKind.OP_MULT, convertArray(denominatorList));
						if (results.size() == 1) {
							results.add(multDiv);
							return createExpression(ExpressionKind.OP_DIV, convertArray(results));
						} else {
							final Expression mult = createExpression(e.getKind(), convertArray(results));
							return createExpression(ExpressionKind.OP_DIV, mult, (Expression)multDiv);
						}

					}
				}
				break;

			case OP_DIV:
				int count = 0;
				for (int i = 1; i < results.size(); i++) {
					if (results.get(i) instanceof Expression)
						results.set(i, finalReduce((Expression)results.get(i)));
					if (isNegative(results.get(i))) {
						results.set(i, negate(results.get(i)));
						count++;
					}
				}
				if (count % 2 == 1)
					results.set(0, negate(results.get(0)));
				return createExpression(e.getKind(), convertArray(results));

			case PAREN:
				if(results.get(0) instanceof Expression)
					results.set(0, finalReduce((Expression)results.get(0)));
				return createExpression(e.getKind(), convertArray(results));

			default:
				break;
		}

		return e;
	}

	/**
	 * Converts a list of values into an Expression array.
	 * The values may contain Numbers, which are converted into Expression.
	 *
	 * @param arr the list of values to convert
	 * @return an array of Expression
	 */
	private static Expression[] convertArray(final List<Object> arr) {
		for (int i = 0; i < arr.size(); i++)
			if (arr.get(i) instanceof Number)
				arr.set(i, createLiteral(arr.get(i).toString()));
		return arr.toArray(new Expression[arr.size()]);
	}

	/**
	 * Determines if an object is already negative.
	 *
	 * @param o the object to test
	 * @return true if the object is a negative value
	 */
	private static boolean isNegative(final Object o) {
		if (o instanceof Double)
			return ((Double)o).doubleValue() < 0.0;
		if (o instanceof Long)
			return ((Long)o).longValue() < 0L;

		final Expression e = (Expression)o;
		switch (e.getKind()) {
			case OP_SUB:
				return e.getExpressionsCount() == 1;
			case OP_DIV:
			case OP_MULT:
				for (int i = 0; i < e.getExpressionsCount(); i++)
					if (isNegative(e.getExpressions(i)))
						return true;
				return false;
			case PAREN:
				return isNegative(e.getExpressions(0));
			default:
				return false;
		}
	}

	/**
	 * Inverse an Object.
	 *
	 * @param o an object to inverse (either an Expression or a Number)
	 * @return an Object representing the inverse form of o
	 */
	private static Object inverse(final Object o) {
		if (o instanceof Number) {
			// if o is 1 or -1
			if (((Number)o).doubleValue() == 1.0 || ((Number)o).doubleValue() == -1.0)
				return o;

			// create fraction
			return createExpression(ExpressionKind.OP_DIV, createLiteral("1"), createLiteral("" + o));
		}

		final Expression e = (Expression)o;
		final Expression.Builder b = Expression.newBuilder(e);
		switch (e.getKind()) {
			case OP_ADD:
			case OP_SUB:
				// if literal 1 or -1
				if (e.getExpressionsCount() == 1 && e.getExpressions(0).getKind() == ExpressionKind.LITERAL)
					if (Double.parseDouble(e.getExpressions(0).getLiteral()) == 1.0 || Double.parseDouble(e.getExpressions(0).getLiteral()) == -1.0)
						return o;
				break;

			case OP_MULT:
				// inverse each term
				for (int i = 0; i < e.getExpressionsCount(); i++)
					b.setExpressions(i, (Expression)inverse(e.getExpressions(i)));
				return b.build();

			case OP_DIV:
				// if first term is 1 or -1
				final boolean negative = isNegative(e.getExpressions(0));
				final Expression first = negative ? (Expression)negate(e.getExpressions(0)) : e.getExpressions(0);

				if (first.getKind() == ExpressionKind.LITERAL && (first.getLiteral() == "1" || first.getLiteral() == "1.0")) {
					// if only 1 denominator, return denominator
					if (e.getExpressionsCount() == 2)
						return negative ? negate(e.getExpressions(1)) : e.getExpressions(1);

					// if more than 1 denominator, return mult
					b.removeExpressions(0);
					b.setKind(ExpressionKind.OP_MULT);
					return negative ? negate(b.build()) : b.build();
				}

				// inverse the first term and mult the rest
				b.setExpressions(0, (Expression)inverse(b.getExpressions(0)));
				b.setKind(ExpressionKind.OP_MULT);
				return b.build();

			case LITERAL:
				if (Double.parseDouble(e.getLiteral()) == 1.0 || Double.parseDouble(e.getLiteral()) == -1.0)
					return e;
				break;

			case PAREN:
				return inverse(e.getExpressions(0));

			default:
				break;
		}

		return createExpression(ExpressionKind.OP_DIV, createLiteral("1"), (Expression)o);
	}


	/**
	 * Similar to negate(), but ensures the returned value is always an Expression.
	 *
	 * @param o an object to negate (either an Expression or a Number)
	 * @return an Expression representing the negated form of o
	 */
	private static Expression negateExpression(final Object o) {
		final Object neg = negate(o);
		if (neg instanceof Expression)
			return (Expression)neg;
		return createLiteral("" + neg);
	}

	/**
	 * Negates an Expression/Number.
	 *
	 * @param o an object to negate (either an Expression or a Number)
	 * @return an Expression or a Number representing the negated form of o
	 */
	private static Object negate(final Object o) {
		if (o instanceof Double)
			return - ((Double)o).doubleValue();
		if (o instanceof Long)
			return - ((Long)o).longValue();

		final Expression e = (Expression)o;
		final Expression.Builder b = Expression.newBuilder(e);

		switch (e.getKind()) {
			case OP_SUB:
				if (e.getExpressionsCount() == 1)
					return e.getExpressions(0);

				b.setKind(ExpressionKind.OP_ADD);
				b.setExpressions(0, negateExpression(e.getExpressions(0)));
				return b.build();

			case OP_ADD:
				if (e.getExpressionsCount() == 1)
					return createExpression(ExpressionKind.OP_SUB, e.getExpressions(0));

				b.setKind(ExpressionKind.OP_SUB);
				b.setExpressions(0, negateExpression(e.getExpressions(0)));
				return b.build();

			case OP_DIV:
			case OP_MULT:
				// find first negative term - if none, use the first term
				int i = 0;
				for (; i < e.getExpressionsCount() && !isNegative(e.getExpressions(i)); i++)
					;
				if (i == e.getExpressionsCount())
					i = 0;

				// negate the term
				b.setExpressions(i, negateExpression(e.getExpressions(i)));
				return b.build();

			default:
				break;
		}

		return createExpression(ExpressionKind.OP_SUB, e);
	}

	/**
	 * A comparator for Expression types.
	 * Uses pretty printing and string comparison.
	 *
	 * @author rdyer
	 */
	public static class ExpressionComparator implements Comparator<Expression> {
		public int compare(final Expression e1, final Expression e2) {
			return prettyprint(e1).compareTo(prettyprint(e2));
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
	public static Expression nnf(final Expression e) {
		return simplify(internalNNF(e), null, 0);
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
		if (e.getKind() == ExpressionKind.PAREN)
			return simplify(e.getExpressions(0));
		return simplify(e, null, 0);
	}

	public static Expression simplify(final Expression e, final ExpressionKind parentKind, final int pos) {
		switch (e.getKind()) {
			case PAREN:
				Expression sub = simplify(e.getExpressions(0), parentKind, pos);
				if (checkPriority(parentKind, sub, pos) > 0)
					return createExpression(e.getKind(), sub);
				return sub;

			case LOGICAL_NOT:
				final Expression inner = simplify(e.getExpressions(0), e.getKind(), 0);
				if (inner.equals(trueLit)) return Expression.newBuilder(falseLit).build();
				if (inner.equals(falseLit)) return Expression.newBuilder(trueLit).build();
				// remove double negation
				if (inner.getKind() == ExpressionKind.LOGICAL_NOT)
					return inner.getExpressions(0);
				return createExpression(e.getKind(), inner);

			case LOGICAL_AND:
			case LOGICAL_OR:
				final List<Expression> exps = new ArrayList<Expression>();
				final Set<Expression> seen = new HashSet<Expression>();

				// recurse in and simplify inner expressions
				OUTER:
				for (int i = 0; i < e.getExpressionsCount(); i++) {
					final Expression e2 = simplify(e.getExpressions(i), e.getKind(), i);

					if (e2.getKind() == e.getKind()) {
						for (int j = 0; j < e2.getExpressionsCount(); j++) {
							final Expression e3 = simplify(e2.getExpressions(j), e.getKind(), j);

							if (!processExpression(e3, exps, seen, e.getKind()))
								break OUTER;
						}
					} else if (e2.getKind() == ExpressionKind.PAREN && e2.getExpressions(0).getKind() == e.getKind()) {
						for (int j = 0; j < e2.getExpressions(0).getExpressionsCount(); j++) {
							final Expression e3 = simplify(e2.getExpressions(0).getExpressions(j), e.getKind(), j);

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

					if (getKind(exp) == ExpressionKind.LOGICAL_AND || getKind(exp) == ExpressionKind.LOGICAL_OR)
						for (int j = 0; j < exps.size(); j++) {
							final Expression exp2 = exps.get(j);

							if (i == j || getKind(exp2) != getKind(exp))
								continue;

							final TreeSet<Expression> s1 = new TreeSet<Expression>(comparator);
							s1.addAll(getExpressionsList(exp));
							s1.removeAll(getExpressionsList(exp2));

							final TreeSet<Expression> s2 = new TreeSet<Expression>(comparator);
							s2.addAll(getExpressionsList(exp2));
							s2.removeAll(getExpressionsList(exp));

							if (s1.size() != 1 || s2.size() != 1)
								continue;

							final Expression e1 = s1.first();
							final Expression e2 = s2.first();

							if (getKind(e1) == ExpressionKind.LOGICAL_NOT && getKind(e2) != ExpressionKind.LOGICAL_NOT) {
								if (e1.getExpressions(0).equals(e2)) {
									final Expression.Builder b1 = Expression.newBuilder(exp);
									getExpressionsList(b1).remove(e1);
									exps.set(i, b1.build());

									final Expression.Builder b2 = Expression.newBuilder(exp2);
									getExpressionsList(b2).remove(e2);
									exps.set(j, b2.build());
								}
							}
							else if (getKind(e1) != ExpressionKind.LOGICAL_NOT && getKind(e2) == ExpressionKind.LOGICAL_NOT) {
								if (e2.getExpressions(0).equals(e1)) {
									final Expression.Builder b1 = Expression.newBuilder(exp);
									getExpressionsList(b1).remove(e1);
									exps.set(i, b1.build());

									final Expression.Builder b2 = Expression.newBuilder(exp2);
									getExpressionsList(b2).remove(e2);
									exps.set(j, b2.build());
								}
							}
						}
				}

				for (int i = 0; i < exps.size(); i++) {
					final Expression exp = exps.get(i);

					if (getKind(exp) != ExpressionKind.LOGICAL_AND && getKind(exp) != ExpressionKind.LOGICAL_OR)
						for (int j = 0; j < exps.size(); j++) {
							final Expression exp2 = exps.get(j);

							if (i == j || getKind(exp) == getKind(exp2))
								continue;
							if (getKind(exp2) != ExpressionKind.LOGICAL_AND && getKind(exp2) != ExpressionKind.LOGICAL_OR)
								continue;

							// absorption
							// a && (a || b) = a
							// a || (a && b) = a
							if (getExpressionsList(exp2).contains(exp)) {
								exps.remove(j);
								j--;
								continue;
							}

							final Expression negExp;
							if (getKind(exp) == ExpressionKind.LOGICAL_NOT)
								negExp = exp.getExpressions(0);
							else
								negExp = createExpression(ExpressionKind.LOGICAL_NOT, exp);

							// negative absorption
							// a && (!a || b || c) = a && (b || c)
							// a || (!a && b && c) = a || (b && c)
							if (getExpressionsList(exp2).contains(negExp)) {
								final Expression.Builder b = Expression.newBuilder(exp2);
								removeExpression(b, getExpressionsList(b).indexOf(negExp));
								if (getExpressionsList(b).size() == 1)
									exps.set(j, getExpressionsList(b).get(0));
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

	private static int checkPriority(ExpressionKind parentKind, Expression sub, int pos) {
		if (parentKind == null)
			return -1;
		if (parentKind == ExpressionKind.PAREN)
			return 0;
		ExpressionKind subKind = sub.getKind();
		if (parentKind == subKind)
			return 0;
		int priority2 = getPriority(subKind);
//		String name = subKind.toString();
//		if (name.startsWith("OP_") && sub.getExpressionsCount() == 1)
		if (sub.getExpressionsCount() == 1
				&& (subKind == ExpressionKind.OP_ADD || subKind == ExpressionKind.OP_SUB))
			priority2 = 0;
		if (priority2 == 0)
			return -1;
		int priority1 = getPriority(parentKind);
		if (priority1 >= 0 && priority2 >= 0) {
			if (priority1 != priority2)
				return priority2 - priority1;
			if (pos == 0 || priority1 == 0)
				return 0;
			if (parentKind == ExpressionKind.OP_ADD && subKind == ExpressionKind.OP_SUB)
				return 0;
//			if (parentKind == ExpressionKind.OP_MULT && subKind == ExpressionKind.OP_DIV)
//				return 0;
//			String name = parentKind.toString();
//			if (name.startsWith("BIT_") || name.startsWith("OP_"))
//				return 1;
		}
		return 1;
	}

	private static int getPriority(ExpressionKind kind) {
		switch (kind) {
		case ARRAY_COMPREHENSION:
		case ARRAYACCESS:
		case ARRAYELEMENT:
		case ARRAYINIT:
		case LOGICAL_NOT:
		case LITERAL:
		case METHODCALL:
		case VARACCESS:
		case PAREN:
		case BIT_NOT:
		case CONDITIONAL:
			return 0;
		case OP_DEC:
		case OP_INC:
			return 0;
		case OP_ADD: return 12;
		case OP_CONCAT: return 12;
		case OP_DIV: return 11;
		case OP_MOD: return 11;
		case OP_MULT: return 11;
		case OP_POW: return 11;
		case OP_SUB:  return 12;
		case OP_THREE_WAY_COMPARE:
		case OP_UNPACK:
			return 10;
		case BIT_AND:
		case BIT_LSHIFT:
		case BIT_OR:
		case BIT_RSHIFT:
		case BIT_UNSIGNEDRSHIFT:
		case BIT_XOR:
			return 20;
		case EQ:
		case GT:
		case GTEQ:
		case LT:
		case LTEQ:
		case NEQ:
			return 30;
		case LOGICAL_AND:
			return 40;
		case LOGICAL_OR:
			return 41;
		default: return -1;
		}
	}

	private static void removeExpression(Builder b, int index) {
		if (b.getKind() == ExpressionKind.PAREN)
			removeExpression(b.getExpressionsBuilder(0), index);
		else
			b.removeExpressions(index);
	}

	private static List<Expression> getExpressionsList(Builder exp) {
		if (exp.getKind() == ExpressionKind.PAREN)
			return getExpressionsList(exp.getExpressionsBuilder(0));
		return exp.getExpressionsList();
	}

	private static List<Expression> getExpressionsList(Expression exp) {
		if (exp.getKind() == ExpressionKind.PAREN)
			return getExpressionsList(exp.getExpressions(0));
		return exp.getExpressionsList();
	}

	private static ExpressionKind getKind(Expression exp) {
		if (exp.getKind() == ExpressionKind.PAREN)
			return getKind(exp.getExpressions(0));
		return exp.getKind();
	}

	/**
	 * Computes the conjunctive normal form of an expression and then simplifies the result.
	 *
	 * @param e the expression to compute CNF on
	 * @return the conjunctive normal form of e, simplified
	 */
	@FunctionSpec(name = "cnf", returnType = "Expression", formalParameters = { "Expression" })
	public static Expression cnf(final Expression e) {
		// push the ORs down into ANDs
		// (B ^ C) v A -> (B v A) ^ (C v A)
		// A v (B ^ C) -> (A v B) ^ (A v C)
		return simplify(normalform(nnf(e), ExpressionKind.LOGICAL_OR, ExpressionKind.LOGICAL_AND), ExpressionKind.LOGICAL_AND, 0);
	}

	/**
	 * Computes the disjunctive normal form of an expression and then simplifies the result.
	 *
	 * @param e the expression to compute DNF on
	 * @return the disjunctive normal form of e, simplified
	 */
	@FunctionSpec(name = "dnf", returnType = "Expression", formalParameters = { "Expression" })
	public static Expression dnf(final Expression e) {
		// push the ANDs down into ORs
		// (B v C) ^ A -> (B ^ A) v (C ^ A)
		// A ^ (B v C) -> (A ^ B) v (A ^ C)
		return simplify(normalform(nnf(e), ExpressionKind.LOGICAL_AND, ExpressionKind.LOGICAL_OR), ExpressionKind.LOGICAL_OR, 0);
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
		if (e.getKind() == ExpressionKind.PAREN)
			return normalform(e.getExpressions(0), distributedOp, innerOp);
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
	public static Expression createExpression(final ExpressionKind kind, final Expression... exps) {
		final Expression.Builder b = Expression.newBuilder();

		b.setKind(kind);
		for (int i = 0; i < exps.length; i++) {
			final Expression e = exps[i];
			if (checkPriority(kind, e, i) > 0)
				b.addExpressions(Expression.newBuilder(createExpression(ExpressionKind.PAREN, e)).build());
			else
				b.addExpressions(Expression.newBuilder(e).build());
		}
		return b.build();
	}

	/**
	 * Creates a new statement.
	 *
	 * @param kind the kind of the statement
	 * @param stmts the operands
	 * @return the new statement
	 */
	public static Statement createStatement(final StatementKind kind, final Statement... stmts) {
		final Statement.Builder b = Statement.newBuilder();

		b.setKind(kind);
		for (final Statement e : stmts)
			b.addStatements(Statement.newBuilder(e).build());

		return b.build();
	}

	/**
	 * Creates a new literal expression.
	 *
	 * @param lit the literal value
	 * @return a new literal expression
	 */
	public static Expression createLiteral(final String lit) {
		// handle negative number literals properly
		if (lit.startsWith("-"))
			return createExpression(ExpressionKind.OP_SUB, createLiteral(lit.substring(1)));

		final Expression.Builder b = Expression.newBuilder();

		b.setKind(ExpressionKind.LITERAL);
		b.setLiteral(lit);

		return b.build();
	}

	/**
	 * Creates a new variable access expression.
	 *
	 * @param var the variable name
	 * @return a new variable access expression
	 */
	public static Expression createVariable(final String var) {
		final Expression.Builder exp = Expression.newBuilder();

		exp.setKind(ExpressionKind.VARACCESS);
		exp.setVariable(var);

		return exp.build();
	}

	/**
	 * Returns cryptographic hash of the pdg using the given algorithm
	 *
	 * @param pdg PDG graph
	 * @param algorithm name of the cryptographic hashing algorithm
	 * @return cryptographic hash of the pdg using the given algorithm
	 * @throws NoSuchAlgorithmException if the given cryptographic algorithm is not supported by JVM
	 * @throws UnsupportedEncodingException if the character encoding used is not supported by JVM
	 */
	@FunctionSpec(name = "getcrypthash", returnType = "string", formalParameters = { "PDG", "string" })
	public static String getCryptHash(final PDG pdg, final String algorithm) throws UnsupportedEncodingException, NoSuchAlgorithmException {
		final Stack<PDGNode> nodes = new Stack<PDGNode>();
		if (pdg.getEntryNode() != null)
			nodes.add(pdg.getEntryNode());
		return getCryptHash(nodes, algorithm);
	}

	/**
	 * Returns cryptographic hash of the slice using the given algorithm
	 *
	 * @param pdgslice slice of the PDG graph
	 * @param algorithm name of the cryptographic hashing algorithm
	 * @return cryptographic hash of the slice using the given algorithm
	 * @throws UnsupportedEncodingException
	 * @throws NoSuchAlgorithmException
	 */
	@FunctionSpec(name = "getcrypthash", returnType = "string", formalParameters = { "PDGSlicer", "string" })
	public static String getCryptHash(final PDGSlicer pdgslice, final String algorithm) throws UnsupportedEncodingException, NoSuchAlgorithmException {
		final Stack<PDGNode> nodes = new Stack<PDGNode>();
		nodes.addAll(pdgslice.getEntrynodesList());
		return getCryptHash(nodes, algorithm);
	}

	/**
	 * Returns cryptographic hash for the given pdg nodes
	 *
	 * @param nodes stack of pdg nodes
	 * @param algorithm name of the cryptographic hashing algorithm
	 * @return cryptographic hash for the given stack of pdg nodes
	 * @throws NoSuchAlgorithmException if the given cryptographic algorithm is not supported by JVM
	 * @throws UnsupportedEncodingException if the character encoding used is not supported by JVM
	 */
	private static String getCryptHash(final Stack<PDGNode> nodes, final String algorithm) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		final Set<PDGNode> visited = new HashSet<PDGNode>();
		final StringBuilder sb = new StringBuilder();

		while (nodes.size() != 0) {
			final PDGNode node = nodes.pop();
			if (node.getStmt() != null)
				sb.append(prettyprint(node.getStmt()));
			if (node.getExpr() != null)
				sb.append(prettyprint(node.getExpr()));
			visited.add(node);

			for (final PDGNode succ : node.getSuccessors()) {
				final List<PDGEdge> edges = node.getOutEdges(succ);
				for (PDGEdge e: edges)
					sb.append(e.getKind()).append(e.getLabel());

				if (!visited.contains(succ))
					nodes.push(succ);
			}
		}

		final MessageDigest md = MessageDigest.getInstance(algorithm);
		final byte[] hcBytes = md.digest(sb.toString().getBytes("UTF-8"));

		final StringBuilder sBDigest = new StringBuilder();
		for (final byte b: hcBytes)
			sBDigest.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));

		return sBDigest.toString();
	}

	/**
	 * Returns the normalized statement given the normalized variable map
	 *
	 * @param stmt statement to be normalized
	 * @param normalizedVars mapping of original names with normalized names in the expression
	 * @return the normalized statement
	 */
	public static Statement normalizeStatement(final Statement stmt, final Map<String, String> normalizedVars) {
		final List<Statement> convertedStatement = new ArrayList<Statement>();
		for (final Statement sub : stmt.getStatementsList())
			convertedStatement.add(normalizeStatement(sub, normalizedVars));

		switch (stmt.getKind()) {
			case RETURN:
				final Expression exp = stmt.getExpressions(0);
				if (!exp.isInitialized())
					return stmt;
				final Statement.Builder sb = Statement.newBuilder(stmt);
				sb.setKind(stmt.getKind());
				sb.addExpressions(normalizeExpression(stmt.getExpressions(0), normalizedVars));
				return sb.build();

			case OTHER:
			case BLOCK:
			case TYPEDECL:
			case SYNCHRONIZED:
			case EXPRESSION:
			case FOR:
			case DO:
			case WHILE:
			case IF:
			case ASSERT:
			case BREAK:
			case CONTINUE:
			case LABEL:
			case SWITCH:
			case CASE:
			case DEFAULT:
			case TRY:
			case THROW:
			case CATCH:
			case FINALLY:
			case EMPTY:
			default:
				return stmt;
				//return createStatement(stmt.getKind(), convertedStatement.toArray(new Statement[convertedStatement.size()]));
		}
	}

	/**
	 * Returns the normalized expression given the normalized variable map
	 *
	 * @param exp expression to be normalized
	 * @param normalizedVars mapping of original names with normalized names in the expression
	 * @return the normalized expression
	 */
	public static Expression normalizeExpression(final Expression exp, final Map<String, String> normalizedVars) {
		final List<Expression> convertedExpression = new ArrayList<Expression>();
		for (final Expression sub : exp.getExpressionsList())
			convertedExpression.add(normalizeExpression(sub, normalizedVars));

		switch (exp.getKind()) {
			case VARDECL:
				final Expression.Builder eb = Expression.newBuilder(exp);
				final Ast.Variable v = exp.getVariableDecls(0);
				final Ast.Variable.Builder vb = Ast.Variable.newBuilder(v);
				if (normalizedVars.containsKey(v.getName()))
					vb.setName(normalizedVars.get(v.getName()));
				if (v.getInitializer().getExpressionsList().size() != 0)
					vb.setInitializer(normalizeExpression(v.getInitializer(), normalizedVars));
				eb.setVariableDecls(0, vb.build());
				return eb.build();

			case VARACCESS:
				if (normalizedVars.containsKey(exp.getVariable()))
					return createVariable(normalizedVars.get(exp.getVariable()));
				else
					return exp;

			case METHODCALL:
				final Expression.Builder bm = Expression.newBuilder(exp);

				for (int i = 0; i < convertedExpression.size(); i++) {
					bm.setExpressions(i, convertedExpression.get(i));
				}

				for (int i = 0; i < exp.getMethodArgsList().size(); i++) {
					Expression mArgs = normalizeExpression(exp.getMethodArgs(i), normalizedVars);
					bm.setMethodArgs(i, mArgs);
				}

				return bm.build();

			case NEW:
			case NEWARRAY:
			case ARRAYINIT:
			case ARRAYACCESS:
				final Expression.Builder bn = Expression.newBuilder(exp);

				for (int i = 0; i < convertedExpression.size(); i++) {
					bn.setExpressions(i, convertedExpression.get(i));
				}

				return bn.build();

			case EQ:
			case NEQ:
			case GT:
			case LT:
			case GTEQ:
			case LTEQ:
			case LOGICAL_AND:
			case LOGICAL_OR:
			case LOGICAL_NOT:
			case ASSIGN:
			case ASSIGN_ADD:
			case ASSIGN_SUB:
			case ASSIGN_DIV:
			case ASSIGN_MULT:
			case ASSIGN_MOD:
			case ASSIGN_BITAND:
			case ASSIGN_BITOR:
			case ASSIGN_BITXOR:
			case ASSIGN_LSHIFT:
			case ASSIGN_RSHIFT:
			case ASSIGN_UNSIGNEDRSHIFT:
			case BIT_AND:
			case BIT_LSHIFT:
			case BIT_NOT:
			case BIT_OR:
			case BIT_RSHIFT:
			case BIT_UNSIGNEDRSHIFT:
			case BIT_XOR:
			case PAREN:
			case OP_ADD:
			case OP_SUB:
			case OP_MULT:
			case OP_DIV:
			case OP_MOD:
			case OP_DEC:
			case OP_INC:
			case CAST:
			case CONDITIONAL:
			case NULLCOALESCE:
				return createExpression(exp.getKind(), convertedExpression.toArray(new Expression[convertedExpression.size()]));

			case LITERAL:
			default:
				return exp;
		}
	}

}
