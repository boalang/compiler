package boa.graphs.slicers.python;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import boa.functions.BoaGraphIntrinsics;
import boa.runtime.BoaAbstractVisitor;
import boa.types.Ast.Declaration;
import boa.types.Ast.Expression;
import boa.types.Ast.Method;
import boa.types.Ast.Namespace;
import boa.types.Ast.Expression.ExpressionKind;
import boa.types.Ast.Statement.StatementKind;
import boa.types.Ast.Statement;
import boa.types.Ast.Type;

public class SymbolTableGenerator extends BoaAbstractVisitor {
	
	
	@Override
	protected boolean preVisit(final Namespace node) throws Exception {
		Status.scopeTracer.push(node.getName());
		Status.cfgMap.put(Status.getCurrentScope(), 
				BoaGraphIntrinsics.getcfg(node));
		Status.cfgToAstIdMapper();
		
		return defaultPreVisit();
	}
	
	@Override
	protected boolean preVisit(final Declaration node) throws Exception {
		
		Status.scopeTracer.push(node.getName());
		Status.cfgMap.put(Status.getCurrentScope(), 
				BoaGraphIntrinsics.getcfg(node));
		Status.cfgToAstIdMapper();
		return defaultPreVisit();
	}
	
	
	@Override
	protected boolean preVisit(final Method node) throws Exception {
		Status.scopeTracer.push(node.getName());
		
		Status.cfgMap.put(Status.getCurrentScope(), 
				BoaGraphIntrinsics.getcfg(node));
		Status.cfgToAstIdMapper();
		
		Status.astMethodMap.put(Status.getCurrentScope(), node);
		
		return defaultPreVisit();
	}
	
	@Override
	protected boolean preVisit(final Statement node) throws Exception {
		
		if(node.getKind()==StatementKind.FOREACH || node.getKind()==StatementKind.WITH)
		{
			this.addToDefintions(ForwardSlicerUtil.
					getIdentiferNames(node));
		}
		
		return defaultPreVisit();
	}
	
	@Override
	protected boolean preVisit(final Expression node) throws Exception {
		if(ForwardSlicerUtil.isMethodCallKind(node))
		{
			Status.statementScope.push("call");
		}
		
		if(Status.isMethodCallScope())
			return defaultPreVisit();
		
		if(ForwardSlicerUtil.isProperAssignKind(node))
		{
			this.addToDefintions(ForwardSlicerUtil.
					getIdentiferNames(node.getExpressions(0)));
		}
		
		return defaultPreVisit();
	}
	private void addToDefintions(HashMap<String, Integer> mp)
	{
		String scope=Status.getCurrentScope();
		
		for (Map.Entry<String, Integer> entry : mp.entrySet()) {
		    String identiferName = entry.getKey();
		    Integer location = entry.getValue();
		    if(!identiferName.equals("_") && !identiferName.equals("."))
		    	SymbolTable.addToDefintions(scope, identiferName, location);
		}
	}
	
	@Override
	protected void postVisit(final Expression node) throws Exception {
		if(ForwardSlicerUtil.isMethodCallKind(node))
		{
			Status.statementScope.pop();
		}
		defaultPostVisit();
	}
	
	@Override
	protected void postVisit(final Namespace node) throws Exception {
		Status.scopeTracer.pop();
		
		defaultPostVisit();
	}
	
	@Override
	protected void postVisit(final Declaration node) throws Exception {
		Status.scopeTracer.pop();
		
		defaultPostVisit();
	}

	@Override
	protected void postVisit(final Method node) throws Exception {
		Status.scopeTracer.pop();
		
		defaultPostVisit();
	}
	

}
