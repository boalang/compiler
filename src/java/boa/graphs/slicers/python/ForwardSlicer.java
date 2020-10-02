package boa.graphs.slicers.python;

import java.util.List;

import boa.runtime.BoaAbstractVisitor;
import boa.types.Ast.ASTRoot;
import boa.types.Ast.Declaration;
import boa.types.Ast.Expression;
import boa.types.Ast.Method;
import boa.types.Ast.Namespace;
import boa.types.Ast.Expression.ExpressionKind;

public class ForwardSlicer extends BoaAbstractVisitor {
	
	ASTRoot root;
	
	public ForwardSlicer(ASTRoot _root,String[] moduleFilter, String[] filterCriteria)
	{
		this.root=_root;
		
		Status.setLibraryFilter(filterCriteria);
		Status.setModuleFilter(moduleFilter);
		
		SymbolTableGenerator st=new SymbolTableGenerator();
		
		try {
			st.visit(this.root);
			
			SymbolTable.printSymbolTable();
			Status.printMap(Status.importMap);
						
		} catch (Exception e) {
			e.printStackTrace();
		}
//		System.out.println("Slices: ");
	}
	
	protected boolean preVisit(final Namespace node) throws Exception {
		Status.globalScopeNameStack.push(node.getName().replace(".", "_"));
	
		return defaultPreVisit();
	}
	
	@Override
	protected boolean preVisit(final Declaration node) throws Exception {
		
		Status.globalScopeNameStack.push(node.getName().replace(".", "_"));

		return defaultPreVisit();
	}
	
	
	@Override
	protected boolean preVisit(final Method node) throws Exception {
		Status.globalScopeNameStack.push(node.getName().replace(".", "_"));
		
		return defaultPreVisit();
	}
	
	@Override
	protected boolean preVisit(final Expression node) throws Exception {
		if(ForwardSlicerUtil.isMethodCallKind(node))
		{
			Status.statementScopeStack.push("call");
		}
		if(ForwardSlicerUtil.isMethodCallKind(node))
		{
			String identifierName=ForwardSlicerUtil.convertExpressionToString(node);
			 
			 String mt2=NameResolver.resolveImport(identifierName, node.getId());
//			 if(!mt2.equals(""))
//				 System.out.println(mt2+" : "+node.getId());
			 
		}
		
		if(ForwardSlicerUtil.isProperAssignKind(node))
		{
			if(Status.isMethodCallScope())
				return defaultPreVisit();
			
			List<Expression> leftExps=ForwardSlicerUtil.expandOtherExpressions(node.getExpressions(0));
			List<Expression> rightExps=ForwardSlicerUtil.expandOtherExpressions(node.getExpressions(1));

			this.addForNameResolution(leftExps, rightExps);
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
	
	void addForNameResolution(List<Expression> leftExps, List<Expression> rightExps)
	{
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
					 if(!mt2.equals("")) {
						 Status.aliasName.put(leftExps.get(i).getId(),
								 mt2);
						 System.out.println("Mapping for alias: "+identiferName+" ==> "+mt2);
					 }
				 }
			}
		}
	}
}
