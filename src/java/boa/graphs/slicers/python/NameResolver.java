package boa.graphs.slicers.python;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import boa.functions.BoaGraphIntrinsics;
import boa.functions.BoaStringIntrinsics;
import boa.runtime.BoaAbstractVisitor;
import boa.types.Ast.Declaration;
import boa.types.Ast.Expression;
import boa.types.Ast.Method;
import boa.types.Ast.Namespace;
import boa.types.Ast.Statement;
import boa.types.Ast.Expression.ExpressionKind;
import boa.types.Ast.Statement.StatementKind;

public class NameResolver extends BoaAbstractVisitor {
	public static String getReachableAliasMappedName(String usedIdentifierName, 
			Integer useAstLocation)
	{
		return getReachableAliasMappedName(usedIdentifierName, useAstLocation, Status.getCurrentScope());
	}
	public static String getReachableAliasMappedName(String usedIdentifierName, 
			Integer useAstLocation, String scope)
	{
		ArrayList<Integer> defs=SymbolTable.getDefLocations(scope, usedIdentifierName);
		if(defs==null || defs.size()==0) return ""; //not defined in this scope
		
		for(Integer loc: defs)
		{
			if(Status.aliasName.containsKey(loc) && CfgUtil.isAstNodesReachable(loc, useAstLocation, usedIdentifierName, scope))
			{
				return Status.aliasName.get(loc);
			}
		}
		return "-"; //defined in this scope, but not reachable
	}
	
	public static String resolveImport(String usedIdentifierName, 
			Integer useAstLocation)
	{
		
		return resolveImport(usedIdentifierName,useAstLocation,Status.getCurrentScope());
	}
	public static String resolveImport(String usedIdentifierName, 
			Integer useAstLocation, String scope)
	{
		String [] tarr=BoaStringIntrinsics.splitall(usedIdentifierName, "\\.");
		if(tarr.length==0) return "";
		
		for(int i=tarr.length - 1;i>=0;i--)
		{
			String str="";
			for(int j=0;j<=i;j++)
				str=str+"."+tarr[j];
			str=BoaStringIntrinsics.substring(str, 1);
			
			String mt1=getReachableAliasMappedName(str, useAstLocation, scope);
			
			if(mt1.equals("-")) return "";
			
			if(!mt1.equals(""))
			{
				str=mt1;
				for(int j=i+1;j<tarr.length;j++)
				{
					str=str+"."+tarr[j];	
				}
				
				return constuctAliasNameFromBaseImport(str);
			}
		}
		return constuctAliasNameFromBaseImport(usedIdentifierName);

	}
	static String constuctAliasNameFromBaseImport(String str)
	{
		String []narr=BoaStringIntrinsics.splitall(str, "\\.");
		str="";
		String ret="";
		int pos=0;
		for(int j=0;j<narr.length;j++)
		{
			if(j==0)
				str=narr[j];
			else
				str=str+"."+narr[j];
			
			if(Status.importMap.containsKey(str))
			{
				ret=Status.importMap.get(str);
				pos=j;
			}
		}
		if(ret=="")
			return "";
		
		for(int j=pos+1;j<narr.length;j++)
			ret=ret+"."+narr[j];
		return ret;
	}
	
	protected boolean preVisit(final Namespace node) throws Exception {
		Status.globalScopeNameStack.push(node.getName());
	
		return defaultPreVisit();
	}
	
	@Override
	protected boolean preVisit(final Declaration node) throws Exception {
		
		Status.globalScopeNameStack.push(node.getName());

		return defaultPreVisit();
	}
	
	
	@Override
	protected boolean preVisit(final Method node) throws Exception {
		Status.globalScopeNameStack.push(node.getName());
		
		return defaultPreVisit();
	}
	
	@Override
	protected boolean preVisit(final Expression node) throws Exception {
		if(ForwardSlicerUtil.isMethodCallKind(node))
		{
			Status.statementScopeStack.push("call");
		}
		
		if(Status.isMethodCallScope())
			return defaultPreVisit();
		
		if(ForwardSlicerUtil.isProperAssignKind(node))
		{
			List<Expression> leftExps=ForwardSlicerUtil.expandOtherExpressions(node.getExpressions(0));
			List<Expression> rightExps=ForwardSlicerUtil.expandOtherExpressions(node.getExpressions(1));

			if(leftExps.size()==rightExps.size())
			{
				for(int i=0;i<rightExps.size();i++)
				{
					if(rightExps.get(i).getKind()!=ExpressionKind.METHODCALL &&
							rightExps.get(i).getKind()!=ExpressionKind.VARACCESS &&
							rightExps.get(i).getKind()!=ExpressionKind.ARRAYACCESS)
						continue;
					
					String identiferName=ForwardSlicerUtil.convertExpressionToString(leftExps.get(i));
					 if(!identiferName.equals("_") && !identiferName.equals(".") &&
					    		!identiferName.equals(""))
					 {
						 String rightIdentifierName=ForwardSlicerUtil.convertExpressionToString(rightExps.get(i));
						 
						 String mt2=NameResolver.resolveImport(rightIdentifierName, rightExps.get(i).getId());
						 if(!mt2.equals(""))
							 Status.aliasName.put(leftExps.get(i).getId(), mt2);
					 }
				}
			}
		}
		
		return defaultPreVisit();
	}
	
	@Override
	protected void postVisit(final Expression node) throws Exception {
		if(ForwardSlicerUtil.isMethodCallKind(node))
		{
			Status.statementScopeStack.pop();
		}
		defaultPostVisit();
	}
	
	@Override
	protected void postVisit(final Namespace node) throws Exception {
		Status.globalScopeNameStack.pop();
		
		defaultPostVisit();
	}
	
	@Override
	protected void postVisit(final Declaration node) throws Exception {
		Status.globalScopeNameStack.pop();
		
		defaultPostVisit();
	}

	@Override
	protected void postVisit(final Method node) throws Exception {
		Status.globalScopeNameStack.pop();
		
		defaultPostVisit();
	}
}
