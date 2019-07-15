package boa.functions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import boa.types.Ast.Expression;
import boa.types.Ast.Expression.ExpressionKind;
import boa.types.Ast.Statement;

import org.apache.commons.text.similarity.LevenshteinDistance;

import static boa.functions.BoaAstIntrinsics.*;

public class BoaRefactoringDetectionIntrinsics {

	@FunctionSpec(name = "editdistance", returnType = "int", formalParameters = { "string", "string" })
	public static int editDistance(String x, String y) {
		return new LevenshteinDistance().apply(x, y);
	}
	@FunctionSpec(name = "matchargswithreplacement", returnType = "bool", formalParameters = { "Expression",
			"Expression" })
	public static boolean matchArgumentsWithReplacement(Expression e1, Expression e2) throws Exception {
		for (int i = 0; i < e1.getMethodArgsList().size() - 1; i++) {
			if (prettyprint(e1.getMethodArgs(i)).equals(prettyprint(e2.getMethodArgs(i)))
					|| replacementMatch(e1.getMethodArgs(i), e2.getMethodArgs(i))) {
				continue;
			} else {
				return false;
			}
		}
		return false;
	}

	@FunctionSpec(name = "updatewithparametertoargumentreplacement", returnType = "Statement", formalParameters = {
			"Statement", "map[string] of Expression" })
	public static Statement updateWithParameterToArgumentReplacement(Statement s, HashMap<String, Expression> map) {
		Statement.Builder sb = Statement.newBuilder(s);
		for (int i = 0; i < sb.getInitializationsCount() - 1; i++)
			sb.setInitializations(i, updateWithParameterToArgumentReplacementUntil(sb.getInitializations(i), map));
		for (int i = 0; i < sb.getConditionsCount() - 1; i++)
			sb.setConditions(i, updateWithParameterToArgumentReplacementUntil(sb.getConditions(i), map));
		for (int i = 0; i < sb.getUpdatesCount() - 1; i++)
			sb.setUpdates(i, updateWithParameterToArgumentReplacementUntil(sb.getUpdates(i), map));
		for (int i = 0; i < sb.getExpressionsCount() - 1; i++)
			sb.setExpressions(i, updateWithParameterToArgumentReplacementUntil(sb.getExpressions(i), map));
		return sb.build();
	}

	private static Expression updateWithParameterToArgumentReplacementUntil(Expression e,
			HashMap<String, Expression> map) {
		if (e.getExpressionsCount() == 0 && e.getMethodArgsCount() == 0 && e.getKind().equals(ExpressionKind.VARACCESS)
				&& map.containsKey(e.getVariable()))
			return map.get(e.getVariable());
		Expression.Builder eb = Expression.newBuilder(e);
		for (int i = 0; i < eb.getExpressionsCount() - 1; i++)
			eb.setExpressions(i, updateWithParameterToArgumentReplacementUntil(eb.getExpressions(i), map));
		for (int i = 0; i < eb.getMethodArgsCount() - 1; i++)
			eb.setMethodArgs(i, updateWithParameterToArgumentReplacementUntil(eb.getMethodArgs(i), map));
		return eb.build();
	}

	@FunctionSpec(name = "needparametertoargumentreplacement", returnType = "bool", formalParameters = { "Statement",
			"map[string] of Expression" })
	public static boolean needParameterToArgumentReplacement(Statement s, HashMap<String, Expression> map) {
		for (Expression e : s.getInitializationsList())
			if (needParameterToArgumentReplacement(e, map))
				return true;
		for (Expression e : s.getConditionsList())
			if (needParameterToArgumentReplacement(e, map))
				return true;
		for (Expression e : s.getUpdatesList())
			if (needParameterToArgumentReplacement(e, map))
				return true;
		for (Expression e : s.getExpressionsList())
			if (needParameterToArgumentReplacement(e, map))
				return true;
		return false;
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

	@FunctionSpec(name = "replacementmatch", returnType = "bool", formalParameters = { "Expression",
			"Expression" })
	public static boolean replacementMatch(Expression e1, Expression e2) throws Exception {
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
		removeCommonExpressions(creations1, creations2);
		removeCommons(types1, types2);
		for (int i = 0; i < 3; i++)
			removeCommons(literals1.get(i), literals2.get(i));
		removeCommonExpressions(operators1, operators2);

		// remove compatible code elements for replacements
		// check replacements for compatible variables
		removeCompatibleCodeElements(variables1, variables2, e1, e2);
		// check replacements for compatible method invocations
		removeCompatibleExpressions(methodInvocations1, methodInvocations2);
		// check replacements for compatible creations
		removeCompatibleExpressions(creations1, creations2);
		// check replacements for compatible types
		
		// check replacements for compatible literals
		
		// check replacements for compatible operators
		removeCompatibleExpressions(operators1, operators2);
		
		print("e1", e1, variables1, methodInvocations1, creations1, types1, literals1, operators1);
		print("e2", e2, variables2, methodInvocations2, creations2, types2, literals2, operators2);

		return false;
	}

	private static void removeCompatibleCodeElements(HashSet<String> set1, HashSet<String> set2, Expression e1, Expression e2) {
		for (Iterator<String> itr1 = set1.iterator(); itr1.hasNext();) {
			String s1 = itr1.next();
			for (Iterator<String> itr2 = set2.iterator(); itr2.hasNext();) {
				String s2 = itr2.next();
				
			}
		}
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
					if (e1.getMethod().equals(e2.getMethod()) && matchArgumentsWithReplacement(e1, e2))
						matched = true;
				} else if (isCreation(e1)) {
					// check new type and args
					if (e1.getNewType().getName().equals(e2.getNewType().getName()) && matchArgumentsWithReplacement(e1, e2))
						matched = true;
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
	
	private static void removeCompatibleExpressions(HashSet<Expression> exps1, HashSet<Expression> exps2) throws Exception {
		for (Iterator<Expression> itr1 = exps1.iterator(); itr1.hasNext();) {
			Expression e1 = itr1.next();
			for (Iterator<Expression> itr2 = exps2.iterator(); itr2.hasNext();) {
				Expression e2 = itr2.next();
				boolean matched = false;
				if (prettyprint(e1).equals(prettyprint(e2))) {
					matched = true;
				} else if (e1.getKind().equals(ExpressionKind.METHODCALL)) {
					// check method name and args
					if (matchArgumentsWithReplacement(e1, e2))
						matched = true;
				} else if (isCreation(e1)) {
					// check new type and args
					if (matchArgumentsWithReplacement(e1, e2))
						matched = true;
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
