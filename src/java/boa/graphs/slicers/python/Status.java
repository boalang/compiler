package boa.graphs.slicers.python;

import java.util.HashMap;
import java.util.Stack;

import boa.graphs.cfg.CFG;
import boa.graphs.cfg.CFGNode;
import boa.types.Ast.Expression;
import boa.types.Ast.Expression.ExpressionKind;
import boa.types.Ast.Method;
import boa.types.Ast.Statement;
import boa.types.Ast.Variable;

public class Status {
	public static Stack<String> scopeTracer;
	public static Stack<String> statementScope;
	
	public static HashMap<String, SymbolTable> symbolTable;
	public static HashMap<String, CFG> cfgMap;
	public static HashMap<String, Method> astMethodMap;
	public static HashMap<Integer, Integer> cfgToAstIdMap;
	
	static
	{
		statementScope=new Stack<String>();
		scopeTracer=new Stack<String>();
		symbolTable=new HashMap<String, SymbolTable>();
		cfgMap=new HashMap<String, CFG>();
		astMethodMap=new HashMap<String, Method>();
		cfgToAstIdMap=new HashMap<Integer, Integer>();
	}
	
	public static String getCurrentScope()
	{
		return convertStackToString(scopeTracer, ".");
	}
	
	public static boolean isMethodCallScope()
	{
		if(statementScope.isEmpty()) return false;
		return statementScope.peek().equals("call");
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
		if(!ForwardSlicerUtil.isCfgDefined(scope)) return;
		
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
}
