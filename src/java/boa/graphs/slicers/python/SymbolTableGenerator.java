package boa.graphs.slicers.python;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import boa.runtime.BoaAbstractVisitor;
import boa.types.Ast.Declaration;
import boa.types.Ast.Expression;
import boa.types.Ast.Method;
import boa.types.Ast.Namespace;
import boa.types.Ast.Expression.ExpressionKind;
import boa.types.Ast.Statement;
import boa.types.Ast.Type;

public class SymbolTableGenerator extends BoaAbstractVisitor {
	
	
	@Override
	protected boolean preVisit(final Namespace node) throws Exception {
		Status.scopeTracer.push(node.getName());
		
		return defaultPreVisit();
	}
	
	@Override
	protected boolean preVisit(final Declaration node) throws Exception {
		
		Status.scopeTracer.push(node.getName());
		
		return defaultPreVisit();
	}
	
	
	@Override
	protected boolean preVisit(final Method node) throws Exception {
		Status.scopeTracer.push(node.getName());
		
		return defaultPreVisit();
	}
	
	@Override
	protected boolean preVisit(final Statement node) throws Exception {
		
		return defaultPreVisit();
	}
	
	@Override
	protected boolean preVisit(final Expression node) throws Exception {
		
		if(ForwardSlicerUtil.isProperAssignKind(node))
		{
			String scope=Status.getCurrentScope();
			
			for (Map.Entry<String, Integer> entry : ForwardSlicerUtil.getIdentiferNames(node.getExpressions(0)).entrySet()) {
			    String identiferName = entry.getKey();
			    Integer location = entry.getValue();
			    SymbolTable.addToDefintions(scope, identiferName, location);
			}
		}
		
		return defaultPreVisit();
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
