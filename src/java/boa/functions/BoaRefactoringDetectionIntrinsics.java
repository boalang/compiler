package boa.functions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import boa.types.Ast.Expression;
import boa.types.Ast.Expression.ExpressionKind;
import boa.runtime.BoaAbstractVisitor;

import static boa.functions.BoaAstIntrinsics.*;

public class BoaRefactoringDetectionIntrinsics {

	@FunctionSpec(name = "editdistance", returnType = "int", formalParameters = { "string", "string" })
	public static int editDistance(String x, String y) {
		int[][] dp = new int[x.length() + 1][y.length() + 1];

		for (int i = 0; i <= x.length(); i++) {
			for (int j = 0; j <= y.length(); j++) {
				if (i == 0) {
					dp[i][j] = j;
				} else if (j == 0) {
					dp[i][j] = i;
				} else {
					dp[i][j] = min(dp[i - 1][j - 1] + (x.charAt(i - 1) == y.charAt(j - 1) ? 0 : 1), dp[i - 1][j] + 1,
							dp[i][j - 1] + 1);
				}
			}
		}

		return dp[x.length()][y.length()];
	}

	public static int min(int... numbers) {
		return Arrays.stream(numbers).min().orElse(Integer.MAX_VALUE);
	}

	@FunctionSpec(name = "matchargswithreplacement", returnType = "bool", formalParameters = { "Expression",
			"Expression", "map[string] of Expression" })
	public static boolean matchArgumentsWithReplacement(Expression e1, Expression e2, HashMap<String, Expression> map)
			throws Exception {
		for (int i = 0; i < e1.getMethodArgsList().size() - 1; i++) {
			// check same pretty print
			if (prettyprint(e1.getMethodArgs(i)).equals(prettyprint(e2.getMethodArgs(i)))) {
				continue;
				// if map is not null check both replacements(ParaToArg and AST)
			} else if (map != null && map.size() != 0
					&& (replacementMatchWithAugment(e1.getMethodArgs(i), e2.getMethodArgs(i), map)
							|| replacementMatchWithAugment(e2.getMethodArgs(i), e1.getMethodArgs(i), map))) {
				continue;
				// if map is null
			} else if (map == null && replacementMatchWithAugment(e1.getMethodArgs(i), e2.getMethodArgs(i), null)) {
				continue;
			} else {
				return false;
			}
		}
		return false;
	}

	@FunctionSpec(name = "replacementmatchwithaugment", returnType = "bool", formalParameters = { "Expression",
			"Expression", "map[string] of Expression" })
	public static boolean replacementMatchWithAugment(Expression e1, Expression e2, HashMap<String, Expression> map)
			throws Exception {
		System.out.println("before parameter to argument replacement");
		System.out.println("e1: " + prettyprint(e1));
		System.out.println("e2: " + prettyprint(e2));
		Expression exp2 = e2;
		if (needParameterToArgumentReplacement(e2, map)) {
			exp2 = updateWithParameterToArgumentReplacement(e2, map);
			System.out.println("Need Parameter To Argument Replacement");
		}
		return replacementMatchWithAugmentUntil(e1, exp2, map);
	}

	private static boolean replacementMatchWithAugmentUntil(Expression e1, Expression e2,
			HashMap<String, Expression> map) throws Exception {
		// variables
		HashSet<String> variables1 = new HashSet<String>();
		HashSet<String> variables2 = new HashSet<String>();
		// method invocations
		HashSet<Expression> methodInvocations1 = new HashSet<Expression>();
		HashSet<Expression> methodInvocations2 = new HashSet<Expression>();
		// creations
		HashSet<Expression> creations1 = new HashSet<Expression>();
		HashSet<Expression> creations2 = new HashSet<Expression>();
		// types
		HashSet<String> types1 = new HashSet<String>();
		HashSet<String> types2 = new HashSet<String>();
		// literals: [{string}, {number}, {boolean}]
		ArrayList<HashSet<String>> literals1 = getNewLiterals();
		ArrayList<HashSet<String>> literals2 = getNewLiterals();
		// operators
		HashSet<Expression> operators1 = new HashSet<Expression>();
		HashSet<Expression> operators2 = new HashSet<Expression>();

		collectNodes(e1, variables1, methodInvocations1, creations1, types1, literals1, operators1);
		collectNodes(e2, variables2, methodInvocations2, creations2, types2, literals2, operators2);

		// remove all commons
		removeCommons(variables1, variables2);
		removeCommonExpressions(methodInvocations1, methodInvocations2);
		removeCommons(types1, types2);
		for (int i = 0; i < 3; i++)
			removeCommons(literals1.get(i), literals2.get(i));

		print("e1", e1, variables1, methodInvocations1, creations1, types1, literals1, operators1);
		print("e2", e2, variables2, methodInvocations2, creations2, types2, literals2, operators2);

		return false;
	}

	private static void removeCommonExpressions(HashSet<Expression> exps1, HashSet<Expression> exps2) throws Exception {
		for (Iterator<Expression> itr1 = exps1.iterator(); itr1.hasNext();) {
			Expression e1 = itr1.next();
			for (Iterator<Expression> itr2 = exps2.iterator(); itr2.hasNext();) {
				Expression e2 = itr2.next();
				boolean matched = false;
				if (prettyprint(e1).equals(prettyprint(e2))) {
					matched = true;
				} else if (e1.getKind().equals(ExpressionKind.METHODCALL)) {
					// check method name and args
					if (e1.getMethod().equals(e2.getMethod()) && replacementMatchWithAugment(e1, e2, null))
						matched = true;
				} else if (isCreation(e1)) {
					// check new type and args
					// TODO
				} else if (isOperator(e1)) {
					// check operator kind
					if (isArithmeticOperator(e1) && isArithmeticOperator(e2)
							|| isBitwiseOperator(e1) && isBitwiseOperator(e2)
							|| isLogicalOperator(e1) && isLogicalOperator(e2)
							|| isRelationalOperator(e1) && isRelationalOperator(e2))
						matched = true;
				}
				if (matched) {
					itr1.remove();
					itr2.remove();
				}
			}
		}
	}

	private static void removeCommons(HashSet<String> s1, HashSet<String> s2) {
		HashSet<String> intersection = new HashSet<String>(s1);
		intersection.retainAll(s2);
		s1.removeAll(intersection);
		s2.removeAll(intersection);
	}

	private static void print(String str, Expression e1, HashSet<String> variables1,
			HashSet<Expression> methodInvocations1, HashSet<Expression> creations1, HashSet<String> types1,
			ArrayList<HashSet<String>> literals1, HashSet<Expression> operators1) {
		System.out.println("\n" + str + ": " + prettyprint(e1));
		System.out.println("variables1: " + variables1);
		System.out.print("methodInvocations1: ");
		for (Expression e : methodInvocations1)
			System.out.print(prettyprint(e) + " | ");
		System.out.print("\ncreations1: ");
		for (Expression e : creations1)
			System.out.print(prettyprint(e) + " | ");
		System.out.print("\ntypes1: ");
		for (Expression e : creations1)
			System.out.print(prettyprint(e) + " | ");
		System.out.print("\nliterals1: ");
		for (HashSet<String> s : literals1)
			System.out.print(s + " | ");
		System.out.print("\noperators1: ");
		for (Expression e : operators1)
			System.out.print(e + " | ");
		System.out.println("\n");
	}

	@FunctionSpec(name = "updatewithparametertoargumentreplacement", returnType = "Expression", formalParameters = {
			"Expression", "map[string] of Expression" })
	public static Expression updateWithParameterToArgumentReplacement(Expression e, HashMap<String, Expression> map) {
		if (e.getExpressionsCount() == 0 && e.getMethodArgsCount() == 0 && e.getKind().equals(ExpressionKind.VARACCESS)
				&& map.containsKey(e.getVariable()))
			return map.get(e.getVariable());
		Expression.Builder eb = Expression.newBuilder(e);
		for (int i = 0; i < eb.getExpressionsCount() - 1; i++)
			eb.setExpressions(i, updateWithParameterToArgumentReplacement(eb.getExpressions(i), map));
		for (int i = 0; i < eb.getMethodArgsCount() - 1; i++)
			eb.setMethodArgs(i, updateWithParameterToArgumentReplacement(eb.getMethodArgs(i), map));
		return eb.build();
	}

	private static boolean needParameterToArgumentReplacement(Expression e, HashMap<String, Expression> map) {
		if (map == null || map.size() == 0)
			return false;
		if (e.getExpressionsCount() == 0 && e.getMethodArgsCount() == 0 && e.getKind().equals(ExpressionKind.VARACCESS)
				&& map.containsKey(e.getVariable()))
			return true;
		for (Expression exp1 : e.getExpressionsList())
			if (needParameterToArgumentReplacement(exp1, map))
				return true;
		for (Expression exp2 : e.getMethodArgsList())
			if (needParameterToArgumentReplacement(exp2, map))
				return true;
		return false;
	}

	private static void collectNodes(Expression e, HashSet<String> variables, HashSet<Expression> methodInvocations,
			HashSet<Expression> creations, HashSet<String> types, ArrayList<HashSet<String>> literals,
			HashSet<Expression> operators1) throws Exception {
		new boa.runtime.BoaAbstractVisitor() {
			@Override
			protected boolean preVisit(final boa.types.Ast.Expression exp) throws Exception {
				// collect local variables w/o "this."
				if (exp.getKind().equals(ExpressionKind.VARACCESS) && exp.getExpressionsCount() == 0)
					variables.add(exp.getVariable());
				// collect method invocations
				if (exp.getKind().equals(ExpressionKind.METHODCALL))
					methodInvocations.add(exp);
				// collect class instantiations
				if (isCreation(exp))
					creations.add(exp);
				// collect literals: [{string}, {number}, {boolean}]
				if (isStringLit(exp))
					literals.get(0).add(exp.getLiteral());
				if (isIntLit(exp))
					literals.get(1).add(exp.getLiteral());
				if (isBoolLit(exp))
					literals.get(2).add(exp.getLiteral());
				// collect operators
				if (isOperator(exp))
					operators1.add(exp);
				return true;
			}

			@Override
			protected boolean preVisit(final boa.types.Ast.Type ty) throws Exception {
				// collect types
				types.add(ty.getName());
				return true;
			}
		}.visit(e);
	}

	private static ArrayList<HashSet<String>> getNewLiterals() {
		ArrayList<HashSet<String>> res = new ArrayList<HashSet<String>>();
		for (int i = 0; i < 3; i++)
			res.add(new HashSet<String>());
		return res;
	}

}
