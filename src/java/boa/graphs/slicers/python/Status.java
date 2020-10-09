package boa.graphs.slicers.python;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
	
	public static List<String> libraryFilter;
	public static List<String> moduleFilter;
	public static HashMap<Integer, String> aliasName;
	public static boolean isModuleFound=false;
	public static boolean hasBeenRedefinedAnywhere=false;
	public static String acrossInStackSeparator="-";
	
	public static HashMap<String, String> importMap;
	public static HashMap<String, String> objectNameMap;
	public static Integer nameResolveDepth=0;
	
	public static boolean changeImpactAnalysisFlag=false;
	public static boolean acrossInFlag=false;
	public static boolean acrossInSessionActive=false;

	public static boolean DEBUG=false;
	
	public static Integer maximumCallDepth=20;
	public static Integer currentCallDepth=0;

	static
	{
		statementScopeStack=new Stack<String>();
		globalScopeNameStack=new Stack<String>();
		namespaceScopeStack=new Stack<String>();
		acrossInStack=new Stack<String>();
		symbolTable=new HashMap<String, SymbolTable>();
		cfgMap=new HashMap<String, CFG>();
		astMethodMap=new HashMap<String, Method>();
		cfgToAstIdMap=new HashMap<Integer, Integer>();
		libraryFilter=new ArrayList<String>();
		moduleFilter=new ArrayList<String>();
		aliasName=new HashMap<Integer, String>();
		importMap=new HashMap<String, String>();
		objectNameMap=new HashMap<String, String>();
	}
	
	public static String getCurrentScope()
	{
		if(acrossInSessionActive)
			return convertStackToString(acrossInStack, acrossInStackSeparator);
		
		return convertStackToString(globalScopeNameStack, ".");
	}
	public static String getParentScope(String scope)
	{
		if(scope.lastIndexOf(".")==-1) return "";
		return scope.substring(0, scope.lastIndexOf("."));
	}
	public static Integer getNumScope(String scope)
	{
		return scope.split("\\.").length;
	}
	public static boolean isDirectClassScope()
	{
		if(namespaceScopeStack.isEmpty()) return false;
		return namespaceScopeStack.peek().equals("class");
	}
	public static boolean hasClassScope()
	{
		if(namespaceScopeStack.isEmpty()) return false;
		for(Object s: namespaceScopeStack.toArray())
		{
			if(s.equals("class")) return true;
		}
		return false;
	}
	public static boolean isDirectMethodScope()
	{
		if(namespaceScopeStack.isEmpty()) return false;
		return namespaceScopeStack.peek().equals("method");
	}
	public static boolean isMethodCallScope()
	{
		if(statementScopeStack.isEmpty()) return false;
		return statementScopeStack.peek().equals("call");
	}
	public static String convertStackToString(Stack<String> st, String joinStr) {
		String str = "";
		Stack<String> tmp=(Stack<String>) st.clone();
		while (!tmp.isEmpty()) {
			if (str != "")
				str = tmp.pop() + joinStr + str;
			else
				str = tmp.pop();
		}
		return str;
	}
	
	public static void cfgToAstIdMapper()
	{
		String scope=getCurrentScope();
		if(!CfgUtil.isCfgDefined(scope)) return;
		
		cfgToAstIdMap.put(0,0);	
		
		for(CFGNode cfgNode: Status.cfgMap.get(scope).getNodes())
		{
			if(cfgNode.hasExpr())
			{
				cfgToAstIdExpressionMapper(cfgNode.getExpr(), 
						(int) cfgNode.getId());
			}
			if(cfgNode.hasStmt())
			{
				cfgToAstIdStatementMapper(cfgNode.getStmt(), 
						(int) cfgNode.getId());
			}
		}
	}
	static void cfgToAstIdExpressionMapper(Expression node, Integer id)
	{
		if(node.hasId()){
			cfgToAstIdMap.put(node.getId(),id);	
		}
		for(Expression e : node.getExpressionsList())
		{
			cfgToAstIdExpressionMapper(e, id);
		}
		if(node.getKind()==ExpressionKind.METHODCALL)
		{
			for(Expression e : node.getMethodArgsList())
			{
				cfgToAstIdExpressionMapper(e, id);
			}
		}
	}
	static void cfgToAstIdVariableMapper(Variable node, Integer id)
	{
		if(node.hasId()){
			cfgToAstIdMap.put(node.getId(),id);	
		}
		if(node.hasComputedName()){
			cfgToAstIdExpressionMapper(node.getComputedName(), id);
		}
		if(node.hasInitializer()){
			cfgToAstIdExpressionMapper(node.getInitializer(), id);
		}
	}
	static void cfgToAstIdStatementMapper(Statement node, Integer id)
	{
		if(node.hasId())
		{
			for(Variable v : node.getVariableDeclarationsList())
			{
				cfgToAstIdVariableMapper(v, id);
			}
			
			cfgToAstIdMap.put(node.getId(),id);	
			
			for(Expression e : node.getExpressionsList())
			{
				cfgToAstIdExpressionMapper(e, id);
			}
			
			for(Expression e : node.getConditionsList())
			{
				cfgToAstIdExpressionMapper(e, id);
			}
		}
	}
	
	public static void clear()
	{
		statementScopeStack.clear();
		globalScopeNameStack.clear();
		namespaceScopeStack.clear();
		acrossInStack.clear();
		for(Map.Entry<String, SymbolTable> entry : symbolTable.entrySet())
		{
			entry.getValue().clear();
		}
		symbolTable.clear();
		cfgMap.clear();
		astMethodMap.clear();
		libraryFilter.clear();
		moduleFilter.clear();
		aliasName.clear();
		importMap.clear();
		objectNameMap.clear();
	}
	public static void setLibraryFilter(String[] b)
	{
		libraryFilter=Arrays.asList(b);
	}
	public static void setModuleFilter(String[] b)
	{
		moduleFilter=Arrays.asList(b);
	}
	
	public static void printMap(HashMap<String, String> mp)
	{
		for(Map.Entry<String, String> entry : mp.entrySet())
		{
			System.out.println(entry.getKey()+" ==> " + entry.getValue());
		}
	}
	public static void printIntegerMap(HashMap<Integer, String> mp)
	{
		for(Map.Entry<Integer, String> entry : mp.entrySet())
		{
			System.out.println(entry.getKey()+" ==> " + entry.getValue());
		}
	}
}

enum SliceStatus
{
	SLICE_DONE, //program point p is impacted and fits filter criteria
	NOT_CANDIDATE, //Doesn't fit filter criteria
	CANDIDATE_NOT_SLICED //fits but not sliced
}

enum JumpStatus
{
	JUMP_MADE,
	JUMP_NOT_MADE,
	RETURN_IMPACTED //jump made and return impacted
}