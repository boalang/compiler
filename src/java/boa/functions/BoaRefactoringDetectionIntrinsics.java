package boa.functions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
		if (e1.getMethodArgsCount() != e2.getMethodArgsCount())
			return false;
//		System.out.println("333 1 " + prettyprint(e1) + " " + prettyprint(e2));
		for (int i = 0; i < e1.getMethodArgsList().size(); i++) {
//			String s1 = prettyprint(e1.getMethodArgs(i));
//			String s2 = prettyprint(e2.getMethodArgs(i));
//			System.out.println("333 2 " + s1 + " " + s2 + " " + s1.equals(s2));
			if (prettyprint(e1.getMethodArgs(i)).equals(prettyprint(e2.getMethodArgs(i)))
					|| replacementMatch(e1.getMethodArgs(i), e2.getMethodArgs(i))) {
				continue;
			} else {
				return false;
			}
		}
		return true;
	}

	@FunctionSpec(name = "updatewithparametertoargumentreplacement", returnType = "Statement", formalParameters = {
			"Statement", "map[string] of Expression" })
	public static Statement updateWithParameterToArgumentReplacement(Statement s, HashMap<String, Expression> map) {
		Statement.Builder sb = Statement.newBuilder(s);
		for (int i = 0; i < sb.getInitializationsCount(); i++)
			sb.setInitializations(i, updateWithParameterToArgumentReplacementUntil(sb.getInitializations(i), map));
		for (int i = 0; i < sb.getConditionsCount(); i++)
			sb.setConditions(i, updateWithParameterToArgumentReplacementUntil(sb.getConditions(i), map));
		for (int i = 0; i < sb.getUpdatesCount(); i++)
			sb.setUpdates(i, updateWithParameterToArgumentReplacementUntil(sb.getUpdates(i), map));
		for (int i = 0; i < sb.getExpressionsCount(); i++)
			sb.setExpressions(i, updateWithParameterToArgumentReplacementUntil(sb.getExpressions(i), map));
		return sb.build();
	}

	private static Expression updateWithParameterToArgumentReplacementUntil(Expression e,
			HashMap<String, Expression> map) {
		if (e.getExpressionsCount() == 0 && e.getMethodArgsCount() == 0 && e.getKind().equals(ExpressionKind.VARACCESS)
				&& map.containsKey(e.getVariable()))
			return map.get(e.getVariable());
		Expression.Builder eb = Expression.newBuilder(e);
		for (int i = 0; i < eb.getExpressionsCount(); i++)
			eb.setExpressions(i, updateWithParameterToArgumentReplacementUntil(eb.getExpressions(i), map));
		for (int i = 0; i < eb.getMethodArgsCount(); i++)
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

	@FunctionSpec(name = "replacementmatch", returnType = "bool", formalParameters = { "Expression", "Expression" })
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

		ReplacementInfo info = new ReplacementInfo(prettyprint(e1), prettyprint(e2));

		// remove compatible code elements for replacements
		removeCompatibleCodeElements(variables1, variables2, info);
		removeCompatibleExpressions(methodInvocations1, methodInvocations2, info);
		removeCompatibleExpressions(creations1, creations2, info);
		removeCompatibleCodeElements(types1, types2, info);
		for (int i = 0; i < 3; i++)
			removeCompatibleCodeElements(literals1.get(i), literals2.get(i), info);
		removeCompatibleExpressions(operators1, operators2, info);

		// CHECKME add code element replacement types from here
		
		// remove compatible method invocations and creations
		removeCompatibleMetodInvocationsAndCreations(methodInvocations1, creations2, info.prettyPrint1,
				info.prettyPrint2, info.distance);
		removeCompatibleMetodInvocationsAndCreations(methodInvocations2, creations1, info.prettyPrint2,
				info.prettyPrint1, info.distance);
		
		// remove compatible variables and method invocations
		
		
		// remove compatible variables and method invocations
		
		
		// remove compatible variables and literals
		
		
		System.out.println("----------------------------------------------------------------------");
		print("e1", e1, variables1, methodInvocations1, creations1, types1, literals1, operators1);
		print("e2", e2, variables2, methodInvocations2, creations2, types2, literals2, operators2);
		System.out.println("----------------------------------------------------------------------");

		return !hasRemainings(variables1, methodInvocations1, creations1, types1, literals1, operators1)
				&& !hasRemainings(variables2, methodInvocations2, creations2, types2, literals2, operators2);
	}

	private static void removeCompatibleMetodInvocationsAndCreations(HashSet<Expression> methodInvocations,
			HashSet<Expression> creations, String prettyPrint1, String prettyPrint2, int distance) throws Exception {
		if (methodInvocations.size() <= creations.size()) {
			removeCompatibleMetodInvocationsAndCreations1(methodInvocations, creations, prettyPrint1, prettyPrint2,
					distance);
		} else {
			removeCompatibleMetodInvocationsAndCreations1(creations, methodInvocations, prettyPrint2, prettyPrint1,
					distance);
		}
	}

	private static void removeCompatibleMetodInvocationsAndCreations1(HashSet<Expression> set1,
			HashSet<Expression> set2, String prettyPrint1, String prettyPrint2, int distance) throws Exception {
		for (Iterator<Expression> itr1 = set1.iterator(); itr1.hasNext();) {
			Expression e1 = itr1.next();
//			System.out.println("111 " + prettyprint(e1));
			String s1 = e1.getKind().equals(ExpressionKind.METHODCALL) ? e1.getMethod() : ("new " + e1.getNewType().getName());
			TreeMap<Double, ArrayList<Expression>> map = new TreeMap<Double, ArrayList<Expression>>();
			for (Iterator<Expression> itr2 = set2.iterator(); itr2.hasNext();) {
				Expression e2 = itr2.next();
//				System.out.println("222 " + prettyprint(e2) + " " + matchArgumentsWithReplacement(e1, e2));
				String s2 = e2.getKind().equals(ExpressionKind.METHODCALL) ? e2.getMethod() : ("new " + e2.getNewType().getName());
				if (matchArgumentsWithReplacement(e1, e2)) {
					String afterReplacement1 = prettyPrint1.replaceAll(Pattern.quote(s1), Matcher.quoteReplacement(s2));
					int tempDistance = editDistance(afterReplacement1, prettyPrint2);
					if (tempDistance >= 0 && tempDistance < distance
					// && syntaxAwareReplacement(s1, s2, prettyprint1, prettyprint2)
					) {
						if (tempDistance == 0)
							break;
						double normalized = (double) tempDistance
								/ (double) Math.max(afterReplacement1.length(), prettyPrint2.length());
						ArrayList<Expression> pairs = new ArrayList<Expression>(2);
						pairs.add(e1);
						pairs.add(e2);
						map.put(normalized, pairs);
					}
				}
			}
			if (!map.isEmpty()) {
				ArrayList<Expression> bestPairs = map.firstEntry().getValue();
				set1.remove(bestPairs.get(0));
				set2.remove(bestPairs.get(1));
			}
		}
	}

	private static class ReplacementInfo {
		String prettyPrint1;
		String prettyPrint2;
		int distance;

		ReplacementInfo(String prettyPrint1, String prettyPrint2) {
			this.prettyPrint1 = prettyPrint1;
			this.prettyPrint2 = prettyPrint2;
			this.distance = editDistance(prettyPrint1, prettyPrint2);
		}
	}

	private static boolean hasRemainings(HashSet<String> variables1, HashSet<Expression> methodInvocations1,
			HashSet<Expression> creations1, HashSet<String> types1, ArrayList<HashSet<String>> literals1,
			HashSet<Expression> operators1) {
		return max(variables1.size(), methodInvocations1.size(), creations1.size(), types1.size(), literals1.size(),
				operators1.size()) != 0;
	}

	private static int max(int... numbers) {
		return Arrays.stream(numbers).max().orElse(Integer.MIN_VALUE);
	}

	private static void removeCompatibleCodeElements(HashSet<String> set1, HashSet<String> set2, ReplacementInfo info) {
		if (set1.size() <= set2.size()) {
			removeCompatibleCodeElements(set1, set2, info.prettyPrint1, info.prettyPrint2, info.distance);
		} else {
			removeCompatibleCodeElements(set2, set1, info.prettyPrint2, info.prettyPrint1, info.distance);
		}
	}

	private static void removeCompatibleCodeElements(HashSet<String> set1, HashSet<String> set2, String prettyPrint1,
			String prettyPrint2, int distance) {
		for (Iterator<String> itr1 = set1.iterator(); itr1.hasNext();) {
			String s1 = itr1.next();
			TreeMap<Double, ArrayList<String>> map = new TreeMap<Double, ArrayList<String>>();
			for (Iterator<String> itr2 = set2.iterator(); itr2.hasNext();) {
				String s2 = itr2.next();
				String afterReplacement1 = prettyPrint1.replaceAll(Pattern.quote(s1), Matcher.quoteReplacement(s2));
				int tempDistance = editDistance(afterReplacement1, prettyPrint2);
				if (tempDistance >= 0 && tempDistance < distance
//						&& syntaxAwareReplacement(s1, s2, prettyprint1, prettyprint2)
				) {
					if (tempDistance == 0)
						break;
					double normalized = (double) tempDistance
							/ (double) Math.max(afterReplacement1.length(), prettyPrint2.length());
					ArrayList<String> pairs = new ArrayList<String>(2);
					pairs.add(s1);
					pairs.add(s2);
					map.put(normalized, pairs);
				}
			}
			if (!map.isEmpty()) {
				ArrayList<String> bestPairs = map.firstEntry().getValue();
				set1.remove(bestPairs.get(0));
				set2.remove(bestPairs.get(1));
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
					if (e1.getNewType().getName().equals(e2.getNewType().getName())
							&& matchArgumentsWithReplacement(e1, e2))
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

	private static void removeCompatibleExpressions(HashSet<Expression> exps1, HashSet<Expression> exps2,
			ReplacementInfo info) throws Exception {
		for (Iterator<Expression> itr1 = exps1.iterator(); itr1.hasNext();) {
			Expression e1 = itr1.next();
			for (Iterator<Expression> itr2 = exps2.iterator(); itr2.hasNext();) {
				Expression e2 = itr2.next();
				boolean matched = false;
				if (prettyprint(e1).equals(prettyprint(e2))) {
					matched = true;
				} else if (e1.getKind().equals(ExpressionKind.METHODCALL)) {
					// check method name and args
					if (matchArgumentsWithReplacement(e1, e2)
							&& compatiableForReplacement(e1.getMethod(), e2.getMethod(), info))
						matched = true;
				} else if (isCreation(e1)) {
					// check new type and args
					if (matchArgumentsWithReplacement(e1, e2)
							&& compatiableForReplacement(e1.getNewType().getName(), e2.getNewType().getName(), info))
						matched = true;
				} else if (isOperator(e1)) {
					// check operator kind
					if (exps1.size() == exps2.size())
						matched = true;
				}
				if (matched) {
					itr1.remove();
					itr2.remove();
				}
			}
		}
	}

	private static boolean compatiableForReplacement(String s1, String s2, ReplacementInfo info) {
		String afterReplacement1 = info.prettyPrint1.replaceAll(Pattern.quote(s1), Matcher.quoteReplacement(s2));
		int tempDistance = editDistance(afterReplacement1, info.prettyPrint2);
		// && syntaxAwareReplacement(s1, s2, prettyprint1, prettyprint2)
		return tempDistance >= 0 && tempDistance < info.distance;
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
		System.out.println(str + ": " + prettyprint(e1));
		System.out.println("variables: " + variables1);
		System.out.print("methodInvocations: ");
		for (Expression e : methodInvocations1)
			System.out.print(prettyprint(e) + " | ");
		System.out.print("\ncreations: ");
		for (Expression e : creations1)
			System.out.print(prettyprint(e) + " | ");
		System.out.print("\ntypes: ");
		for (Expression e : creations1)
			System.out.print(prettyprint(e) + " | ");
		System.out.print("\nliterals: ");
		for (HashSet<String> s : literals1)
			System.out.print(s + " | ");
		System.out.print("\noperators: ");
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

	////////////////////////////////////////////////////////////
	// syntaxAwareReplacement copyed from Reafctoring Miner //
	////////////////////////////////////////////////////////////
	private static final String[] SPECIAL_CHARACTERS = { ";", ",", ")", "=", "+", "-", ">", "<", ".", "]", " " };

	public static int countInstances(String completeString, String subString) {
		for (String character : SPECIAL_CHARACTERS) {
			int index = completeString.indexOf(subString + character);
			if (index != -1) {
				return (completeString.length() - completeString.replace(subString + character, "").length())
						/ (subString.length() + 1);
			}
		}
		return 0;
	}

	public static boolean contains(String completeString, String subString) {
		for (String character : SPECIAL_CHARACTERS) {
			if (completeString.contains(subString + character)) {
				return true;
			}
		}
		return false;
	}

	public static String performReplacement(String completeString, String subString, String replacement) {
		String temp = new String(completeString);
		if (completeString.equals(subString)) {
			temp = temp.replace(subString, replacement);
			return temp;
		}
		for (String character : SPECIAL_CHARACTERS) {
			if (completeString.contains(subString + character)) {
				temp = temp.replace(subString + character, replacement + character);
			}
		}
		return temp;
	}

	public static String performReplacement(String completeString1, String completeString2, String subString1,
			String subString2, Set<String> variables1, Set<String> variables2) {
		String temp = new String(completeString1);
		boolean replacementOccurred = false;
		for (String character : SPECIAL_CHARACTERS) {
			if (variables1.contains(subString1) && variables2.contains(subString2)
					&& completeString1.contains(subString1 + character)
					&& completeString2.contains(subString2 + character)) {
				temp = temp.replace(subString1 + character, subString2 + character);
				replacementOccurred = true;
			}
		}
		if (!replacementOccurred) {
			for (String character : SPECIAL_CHARACTERS) {
				if (variables1.contains(subString1) && variables2.contains(subString2)
						&& completeString1.contains(subString1 + character)
						&& syntaxAwareReplacement(subString1, subString2, completeString1, completeString2)) {
					temp = temp.replace(subString1 + character, subString2 + character);
					replacementOccurred = true;
				}
			}
		}
		if (!replacementOccurred && completeString1.contains(subString1) && completeString2.contains(subString2)) {
			try {
				char nextCharacter1 = completeString1.charAt(completeString1.indexOf(subString1) + subString1.length());
				char nextCharacter2 = completeString2.charAt(completeString2.indexOf(subString2) + subString2.length());
				if (nextCharacter1 == nextCharacter2) {
					temp = completeString1.replaceAll(Pattern.quote(subString1), Matcher.quoteReplacement(subString2));
				}
			} catch (IndexOutOfBoundsException e) {
				return temp;
			}
		}
		return temp;
	}

	public static int indexOf(String completeString, String subString) {
		for (String character : SPECIAL_CHARACTERS) {
			int index = completeString.indexOf(subString + character);
			if (index != -1) {
				return index;
			}
		}
		return -1;
	}

	public static int lastIndexOf(String completeString, String subString) {
		for (String character : SPECIAL_CHARACTERS) {
			int index = completeString.lastIndexOf(subString + character);
			if (index != -1) {
				return index;
			}
		}
		return -1;
	}

	public static boolean syntaxAwareReplacement(String s1, String s2, String argumentizedString1,
			String argumentizedString2) {
		int smallStringLength = 4;
		int firstIndex1 = s1.length() < smallStringLength ? indexOf(argumentizedString1, s1)
				: argumentizedString1.indexOf(s1);
		int lastIndex1 = s1.length() < smallStringLength ? lastIndexOf(argumentizedString1, s1)
				: argumentizedString1.lastIndexOf(s1);
		int length1 = argumentizedString1.length();
		String firstCharacterBefore1 = null;
		String firstCharacterAfter1 = null;
		String lastCharacterBefore1 = null;
		String lastCharacterAfter1 = null;
		if (firstIndex1 != -1) {
			firstCharacterBefore1 = firstIndex1 == 0 ? ""
					: Character.toString(argumentizedString1.charAt(firstIndex1 - 1));
			firstCharacterAfter1 = firstIndex1 + s1.length() == length1 ? ""
					: Character.toString(argumentizedString1.charAt(firstIndex1 + s1.length()));
			if (lastIndex1 != firstIndex1) {
				lastCharacterBefore1 = lastIndex1 == 0 ? ""
						: Character.toString(argumentizedString1.charAt(lastIndex1 - 1));
				lastCharacterAfter1 = lastIndex1 + s1.length() == length1 ? ""
						: Character.toString(argumentizedString1.charAt(lastIndex1 + s1.length()));
			}
		}

		int firstIndex2 = s2.length() < smallStringLength ? indexOf(argumentizedString2, s2)
				: argumentizedString2.indexOf(s2);
		int lastIndex2 = s2.length() < smallStringLength ? lastIndexOf(argumentizedString2, s2)
				: argumentizedString2.lastIndexOf(s2);
		int length2 = argumentizedString2.length();
		String firstCharacterBefore2 = null;
		String firstCharacterAfter2 = null;
		String lastCharacterBefore2 = null;
		String lastCharacterAfter2 = null;
		if (firstIndex2 != -1) {
			firstCharacterBefore2 = firstIndex2 == 0 ? ""
					: Character.toString(argumentizedString2.charAt(firstIndex2 - 1));
			firstCharacterAfter2 = firstIndex2 + s2.length() == length2 ? ""
					: Character.toString(argumentizedString2.charAt(firstIndex2 + s2.length()));
			if (lastIndex2 != firstIndex2) {
				lastCharacterBefore2 = lastIndex2 == 0 ? ""
						: Character.toString(argumentizedString2.charAt(lastIndex2 - 1));
				lastCharacterAfter2 = lastIndex2 + s2.length() == length2 ? ""
						: Character.toString(argumentizedString2.charAt(lastIndex2 + s2.length()));
			}
		}
		return (compatibleCharacterBeforeMatch(firstCharacterBefore1, firstCharacterBefore2)
				&& compatibleCharacterAfterMatch(firstCharacterAfter1, firstCharacterAfter2))
				|| (compatibleCharacterBeforeMatch(firstCharacterBefore1, lastCharacterBefore2)
						&& compatibleCharacterAfterMatch(firstCharacterAfter1, lastCharacterAfter2))
				|| (compatibleCharacterBeforeMatch(lastCharacterBefore1, firstCharacterBefore2)
						&& compatibleCharacterAfterMatch(lastCharacterAfter1, firstCharacterAfter2))
				|| (compatibleCharacterBeforeMatch(lastCharacterBefore1, lastCharacterBefore2)
						&& compatibleCharacterAfterMatch(lastCharacterAfter1, lastCharacterAfter2));
	}

	private static boolean compatibleCharacterBeforeMatch(String characterBefore1, String characterBefore2) {
		if (characterBefore1 != null && characterBefore2 != null) {
			if (characterBefore1.equals(characterBefore2))
				return true;
			if (characterBefore1.equals(",") && characterBefore2.equals("("))
				return true;
			if (characterBefore1.equals("(") && characterBefore2.equals(","))
				return true;
			if (characterBefore1.equals(" ") && characterBefore2.equals(""))
				return true;
			if (characterBefore1.equals("") && characterBefore2.equals(" "))
				return true;
		}
		return false;
	}

	private static boolean compatibleCharacterAfterMatch(String characterAfter1, String characterAfter2) {
		if (characterAfter1 != null && characterAfter2 != null) {
			if (characterAfter1.equals(characterAfter2))
				return true;
			if (characterAfter1.equals(",") && characterAfter2.equals(")"))
				return true;
			if (characterAfter1.equals(")") && characterAfter2.equals(","))
				return true;
		}
		return false;
	}

}
