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

import boa.types.Ast.Expression.ExpressionKind;
import boa.types.Ast.Expression;

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
	 * Replaces api arguments in an expression with symbolic names
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
			case ASSIGN:
			case NEW:
				return createExpression(e.getKind(), convertedExpression.toArray(new Expression[convertedExpression.size()]));

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
			case ARRAYINDEX:
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
			case ARRAYINDEX:

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
					//if((Byte)o[1] == 0)
					if(o[1].equals('n'))
						num.add((Expression) o[0]);
					else
						den.add((Expression) o[0]);
				}

				Expression sortExpr;
				final Expression numerator = createExpression(ExpressionKind.OP_MULT, num.toArray(new Expression[num.size()]));

				if(den.size() != 0) {
					final Expression denominator = createExpression(ExpressionKind.OP_MULT, den.toArray(new Expression[den.size()]));
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
				if (BoaAstIntrinsics.isStringLit(e)) {     // if it is a string expression, we don't want to process it
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
				for(final Expression e: expr.getExpressionsList())
					result.addAll(seperateNumDenom(e, type));
				break;

			case OP_DIV:
				for(int i = 0; i < expr.getExpressionsCount(); i++) {
					if(i % 2 == 0)
						result.addAll(seperateNumDenom(expr.getExpressions(i), 'n'));
					else
						result.addAll(seperateNumDenom(expr.getExpressions(i), 'd'));
				}
				break;

			case OP_ADD:
			case OP_SUB:
			case PAREN:
				//These cases will not execute once the expression is fully siimplified
				final Object[] oo = {normalize(expr), type};
				result.add(oo);
				break;

			default:
				final Object[] o = {expr, type};
				result.add(o);
		}

		return  result;
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
		final Object o = internalReduce(e);
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
	private static Object internalReduce(final Expression e) throws Exception {
		final Expression.Builder b;
		final List<Object> results = new ArrayList<Object>();
		for (final Expression sub : e.getExpressionsList())
			results.add(internalReduce(sub));

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
				Expression[] results1 = new Expression[results.size()] ;
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
					if (o instanceof Double)
						return ((Double)o).doubleValue();
					return ((Long)o).longValue();
				}

				// bring children up if the child node is an add
				for (int i = 0; i < results.size(); i++)
					if (results.get(i) instanceof Expression && ((Expression)results.get(i)).getKind() == ExpressionKind.OP_ADD) {
						final Expression subExp = (Expression)results.get(i);
						results.remove(i);
						for (int j = 0; j < subExp.getExpressionsCount(); j++)
							results.add(i + j, internalReduce(subExp.getExpressions(j)));
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
					// group common terms
					for (int i = 0; i < results2.size(); i++) {
						int count = 1;

						for (int j = i + 1; j < results2.size(); j++)
							if (results2.get(i).equals(results2.get(j)))
								count++;

						if (count > 1) {
							final Expression commonTerm = (Expression)results2.get(i);
							while (results2.remove(commonTerm))
								;
							results2.add(i, createExpression(ExpressionKind.OP_MULT, createLiteral("" + count), commonTerm));
						}
					}

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

					if (o instanceof Expression)
						return negate(o);
					if (o instanceof Double)
						return -((Double)o).doubleValue();
					return -((Long)o).longValue();
				}

				// bring children up if the child node is a sub
				for (int i = 0; i < results.size(); i++)
					if (results.get(i) instanceof Expression && ((Expression)results.get(i)).getKind() == ExpressionKind.OP_SUB) {
						final Expression subExp = (Expression)results.get(i);
						if (subExp.getExpressionsCount() > 1) {
							results.remove(i);
							for (int j = 0; j < subExp.getExpressionsCount(); j++)
								if (i == 0 || j == 0)
									results.add(i + j, internalReduce(subExp.getExpressions(j)));
								else
									results.add(i + j, negate(internalReduce(subExp.getExpressions(j))));
						}
					}

				final List<Object> adds = new ArrayList<Object>();

				// if multiple arguments, try to subtract them all together
				for (final Object o : results) {
					if (!first && isNegative(o)) {
						adds.add(negate(o));
					} else {
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
					Object lhs = results2.get(0);

					// check for elimination
					if (lhs instanceof Expression) {
						int idx = results2.lastIndexOf(lhs);
						if (idx > 0) {
							results2.remove(idx);
							results2.set(0, 0L);
							lhs = results2.get(0);
						}
					}

					// group common terms
					for (int i = 0; i < results2.size(); i++) {
						int count = 1;

						for (int j = i + 1; j < results2.size(); j++)
							if (results2.get(i).equals(results2.get(j)))
								count++;

						if (count > 1) {
							lhs = results2.get(i);
							while (results2.remove(lhs))
								;
							results2.add(i, createExpression(ExpressionKind.OP_MULT, createLiteral("" + count), (Expression)lhs));
						}

						lhs = results2.get(0);
					}

					// check for identity
					if (lhs instanceof Number && ((Number)lhs).doubleValue() == 0.0 && results2.size() > 1) {
						results2.remove(0);
						results2.set(0, negate(results2.get(0)));
					}
				}

				// if it reduced to a single term, return just the term otherwise return the whole expression
				Object result;
				if (results2.size() == 1)
					result = results2.get(0);
				else
					result = createExpression(e.getKind(), convertArray(results2));

				if (adds.size() > 0) {
					adds.add(0, result);
					result = internalReduce(createExpression(ExpressionKind.OP_ADD, convertArray(adds)));
				}

				return result;

			case OP_MULT:
				dval = 1.0;
				ival = 1L;

				// if multiple arguments, try to multiply them all together
				for (Object o : results) {
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
					else if (results2.get(0) instanceof Number && ((Number)results2.get(0)).doubleValue() == -1.0) {
						results2.remove(0);
						results2.set(0, negate(results2.get(0)));
					}

					// only at most 1 term should remain negative
					int lastNeg = -1;
					for (int i = results2.size() - 1; i >= 0; i--)
						if (isNegative(results2.get(i))) {
							if (lastNeg != -1) {
								results2.set(lastNeg, negate(results2.get(lastNeg)));
								results2.set(i, negate(results2.get(i)));
								lastNeg = -1;
							} else {
								lastNeg = i;
							}
						}

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
			case ARRAYINDEX:
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
	 * Divides a number.
	 * This method is used in place of actual division, only when both parts are doubles.
	 * If the resulting division results in an integer value, it returns a long.
	 *
	 * @param num the numerator
	 * @param denom the denominator
	 * @return the result of dividing num by denom
	 */
	private static Object div(final double num, final double denom) {
		final double result = num / denom;
		if (result == (long)result)
			return (long)result;
		return result;
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

							final TreeSet<Expression> s1 = new TreeSet<Expression>(comparator);
							s1.addAll(exp.getExpressionsList());
							s1.removeAll(exp2.getExpressionsList());

							final TreeSet<Expression> s2 = new TreeSet<Expression>(comparator);
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
	public static Expression cnf(final Expression e) {
		// push the ORs down into ANDs
		// (B ⋀ C) ⋁ A -> (B ⋁ A) ⋀ (C ⋁ A)
		// A ⋁ (B ⋀ C) -> (A ⋁ B) ⋀ (A ⋁ C)
		return simplify(normalform(nnf(e), ExpressionKind.LOGICAL_OR, ExpressionKind.LOGICAL_AND));
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
		return simplify(normalform(nnf(e), ExpressionKind.LOGICAL_AND, ExpressionKind.LOGICAL_OR));
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

	/**
	 * Creates a new variable access expression.
	 *
	 * @param var the variable name
	 * @return a new variable access expression
	 */
	private static Expression createVariable(final String var) {
		final Expression.Builder exp = Expression.newBuilder();

		exp.setKind(ExpressionKind.VARACCESS);
		exp.setVariable(var);

		return exp.build();
	}
}
