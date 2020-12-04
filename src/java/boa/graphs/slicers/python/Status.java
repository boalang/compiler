package boa.graphs.slicers.python;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import boa.graphs.cfg.CFG;
import boa.graphs.cfg.CFGNode;
import boa.types.Ast.Expression;
import boa.types.Ast.Expression.ExpressionKind;
import boa.types.Ast.Method;
import boa.types.Ast.Statement;
import boa.types.Ast.Variable;

public class Status {
	public static Stack<String> globalScopeNameStack;
	public static Stack<String> statementScopeStack;
	public static Stack<String> namespaceScopeStack;
	public static Stack<String> acrossInStack;

	public static HashMap<String, SymbolTable> symbolTable;
	public static HashMap<String, CFG> cfgMap;
	public static HashMap<String, Method> astMethodMap;
	public static HashMap<Integer, Integer> cfgToAstIdMap;
	public static HashMap<Integer, String> slicedMap;


	public static HashMap<String, Boolean> returnImpacted;

	public static List<String> libraryFilter;
	public static List<String> moduleFilter;
//	public static HashMap<Integer, String> aliasName;
	public static boolean isModuleFound = false;
	public static boolean hasBeenRedefinedAnywhere = false;
	public static String acrossInStackSeparator = "->";
	public static HashMap<String, Integer> callPointMap;
	public static HashMap<String, Integer> acrossInParameterMap;

	public static HashMap<String, String> importMap;
	public static HashMap<String, String> objectNameMap;
	public static Integer nameResolveDepth = 0;

	public static boolean changeImpactAnalysisFlag = false;
	public static boolean acrossInFlag = false;
	public static boolean acrossInSessionActive = false;

	public static boolean DEBUG = false;
	public static boolean isParameterMapping = false;

	public static Integer maximumCallDepth = 20;
	public static Integer currentCallDepth = 0;

	//Library: https://github.com/mihnita/ansi-econsole/blob/master/AnsiConTest/src/AnsiConTest.java
    public static final String ANSI_RESET = "\u001b[m";  // Text Reset
    public static final String ANSI_GREEN = "\u001b[92m";   // GREEN

	static {
		statementScopeStack = new Stack<String>();
		globalScopeNameStack = new Stack<String>();
		namespaceScopeStack = new Stack<String>();
		acrossInStack = new Stack<String>();
		symbolTable = new HashMap<String, SymbolTable>();
		cfgMap = new HashMap<String, CFG>();
		astMethodMap = new HashMap<String, Method>();
		cfgToAstIdMap = new HashMap<Integer, Integer>();
		callPointMap = new HashMap<String, Integer>();
		acrossInParameterMap = new HashMap<String, Integer>();
		libraryFilter = new ArrayList<String>();
		moduleFilter = new ArrayList<String>();
		importMap = new HashMap<String, String>();
		objectNameMap = new HashMap<String, String>();
		returnImpacted=new HashMap<String, Boolean>();
		slicedMap = new HashMap<Integer, String>();
	}

	public static String getCurrentScope() {
		if (acrossInSessionActive)
			return convertStackToString(acrossInStack, acrossInStackSeparator);

		return convertStackToString(globalScopeNameStack, ".");
	}

	public static String getProperCurrentScope() {
		String scope = getCurrentScope();
		if (acrossInSessionActive) {
			return acrossInStack.peek();
		}
		return getCurrentScope();
	}
	public static String getProperScope(String scope) {
		return scope.substring(scope.lastIndexOf(Status.acrossInStackSeparator)+1);
	}

	public static String getParentScope(String scope) {
		if (scope.lastIndexOf(".") == -1)
			return "";
		String parScope = scope.substring(0, scope.lastIndexOf("."));

		return parScope;
	}
	
	public static String getAcrossInScopeFromProper(String scope) {

		if (acrossInSessionActive && callPointMap.containsKey(scope)) {
			String str = "";
			for (String tmp : acrossInStack.toArray(new String[0])) {
				if (str != "")
					str = str + acrossInStackSeparator + tmp;
				else
					str = tmp;
				if (tmp.equals(scope)) {
					break;
				}
			}
			return str;
		}
		return scope;
	}

	public static Integer getNumScope(String scope) {
		return scope.split("\\.").length;
	}

	public static boolean isDirectClassScope() {
		if (namespaceScopeStack.isEmpty())
			return false;
		return namespaceScopeStack.peek().equals("class");
	}

	public static boolean hasClassScope() {
		if (namespaceScopeStack.isEmpty())
			return false;
		for (Object s : namespaceScopeStack.toArray()) {
			if (s.equals("class"))
				return true;
		}
		return false;
	}

	public static boolean isDirectMethodScope() {
		if (namespaceScopeStack.isEmpty())
			return false;
		return namespaceScopeStack.peek().equals("method");
	}

	public static boolean isMethodCallScope() {
		if (statementScopeStack.isEmpty())
			return false;
		return statementScopeStack.peek().equals("call");
	}

	public static String convertStackToString(Stack<String> st, String joinStr) {
		String str = "";
		Stack<String> tmp = (Stack<String>) st.clone();
		while (!tmp.isEmpty()) {
			if (str != "")
				str = tmp.pop() + joinStr + str;
			else
				str = tmp.pop();
		}
		return str;
	}

	public static void cfgToAstIdMapper() {
		String scope = getCurrentScope();
		if (!CfgUtil.isCfgDefined(scope))
			return;

		cfgToAstIdMap.put(0, 0);

		for (CFGNode cfgNode : Status.cfgMap.get(scope).getNodes()) {
			if (cfgNode.hasExpr()) {
				cfgToAstIdExpressionMapper(cfgNode.getExpr(), (int) cfgNode.getId());
			}
			if (cfgNode.hasStmt()) {
				cfgToAstIdStatementMapper(cfgNode.getStmt(), (int) cfgNode.getId());
			}
		}
	}

	static void cfgToAstIdExpressionMapper(Expression node, Integer id) {
		if (node.hasId()) {
			cfgToAstIdMap.put(node.getId(), id);
		}
		for (Expression e : node.getExpressionsList()) {
			cfgToAstIdExpressionMapper(e, id);
		}
		if (node.getKind() == ExpressionKind.METHODCALL) {
			for (Expression e : node.getMethodArgsList()) {
				cfgToAstIdExpressionMapper(e, id);
			}
		}
	}

	static void cfgToAstIdVariableMapper(Variable node, Integer id) {
		if (node.hasId()) {
			cfgToAstIdMap.put(node.getId(), id);
		}
		if (node.hasComputedName()) {
			cfgToAstIdExpressionMapper(node.getComputedName(), id);
		}
		if (node.hasInitializer()) {
			cfgToAstIdExpressionMapper(node.getInitializer(), id);
		}
	}

	static void cfgToAstIdStatementMapper(Statement node, Integer id) {
		if (node.hasId()) {
			for (Variable v : node.getVariableDeclarationsList()) {
				cfgToAstIdVariableMapper(v, id);
			}

			cfgToAstIdMap.put(node.getId(), id);

			for (Expression e : node.getExpressionsList()) {
				cfgToAstIdExpressionMapper(e, id);
			}

			for (Expression e : node.getConditionsList()) {
				cfgToAstIdExpressionMapper(e, id);
			}
		}
	}

	public static void clear() {
		statementScopeStack.clear();
		globalScopeNameStack.clear();
		namespaceScopeStack.clear();
		acrossInStack.clear();
		for (Map.Entry<String, SymbolTable> entry : symbolTable.entrySet()) {
			entry.getValue().clear();
		}
		symbolTable.clear();
		cfgMap.clear();
		astMethodMap.clear();
//		libraryFilter.clear();
//		moduleFilter.clear();
		importMap.clear();
		callPointMap.clear();
		objectNameMap.clear();
		acrossInParameterMap.clear();
		returnImpacted.clear();
		slicedMap.clear();
	}

	public static void setLibraryFilter(String[] b) {
		libraryFilter = Arrays.asList(b);
	}

	public static void setModuleFilter(String[] b) {
		moduleFilter = Arrays.asList(b);
	}

	public static void printMap(HashMap<String, String> mp) {
		for (Map.Entry<String, String> entry : mp.entrySet()) {
			System.out.println(entry.getKey() + " ==> " + entry.getValue());
		}
	}

	public static void printIntegerMap(HashMap<Integer, String> mp) {
		for (Map.Entry<Integer, String> entry : mp.entrySet()) {
			System.out.println(entry.getKey() + " ==> " + entry.getValue());
		}
	}
}
