package boa.graphs.slicers.python;

import java.util.ArrayList;

import boa.types.Ast.Expression;
import boa.types.Ast.Expression.ExpressionKind;
import boa.types.Shared.ChangeKind;

public class SliceCriteriaAnalysis {

	static AcrossInVisitor acrossInVisitor=new AcrossInVisitor();
	
	public static boolean isImpacted(String usedIdentifierName, Integer useAstLocation) {
		return isImpacted(usedIdentifierName, useAstLocation, Status.getProperCurrentScope());
	}

	public static boolean isImpacted(String usedIdentifierName, Integer useAstLocation, String scope) {
		String targetScope = scope;
		while (!scope.equals("")) {
			CfgReachbilityStatus reachbilityStatus=isImpacted(usedIdentifierName, useAstLocation, 
					Status.getAcrossInScopeFromProper(scope),
					targetScope);
			if (reachbilityStatus==CfgReachbilityStatus.REACHABLE)
				return true;
			if (reachbilityStatus==CfgReachbilityStatus.UNREACHABLE)
				return false;
			scope = Status.getParentScope(scope);
		}
		return false;
	}

	private static CfgReachbilityStatus isImpacted(String usedIdentifierName, Integer useAstLocation, String sourceScope,
			String targetScope) {
		ArrayList<Integer> criterias = SymbolTable.getCriteriaLocations(sourceScope, usedIdentifierName);
		if (criterias == null || criterias.size() == 0)
			return CfgReachbilityStatus.NOT_DEFINED; // not defined in this scope

		for (Integer loc : criterias) {
			if (CfgUtil.isAstNodesReachable(loc, useAstLocation, usedIdentifierName, sourceScope, targetScope)) {
				return CfgReachbilityStatus.REACHABLE;
			}
		}
		return CfgReachbilityStatus.UNREACHABLE;
	}

	public static boolean isExpressionImpacted(Expression node) {
		if (ForwardSlicerUtil.isAssignKind(node)) {
			if (ForwardSlicerUtil.isProperAssignKind(node))
				return isExpressionImpacted(node.getExpressions(1));
			return false;
		}
		if (node.getKind() == ExpressionKind.LAMBDA) {
			return false;
		}
		if (node.getKind() == ExpressionKind.FOR_LIST) {
			if (node.getExpressionsCount() > 1) {
				if (isExpressionImpacted(node.getExpressions(1)))
					return true;
			}
			return false;
		}
		if (node.getKind() == ExpressionKind.ARRAY_COMPREHENSION) {
			for (int i = 1; i < node.getExpressionsCount(); i++) {
				if (isExpressionImpacted(node.getExpressions(i)))
					return true;
			}
			return false;
		}

		if (node.getKind() == ExpressionKind.VARACCESS) {
			if (node.hasId() && isImpacted(ForwardSlicerUtil.convertExpressionToString(node), node.getId()))
				return true;
		}

		if (node.getKind() == ExpressionKind.METHODCALL) {
			for (Expression ex : node.getMethodArgsList()) {
				if (isExpressionImpacted(ex))
					return true;
			}
		}

		for (Expression ex : node.getExpressionsList()) {
			if (isExpressionImpacted(ex))
				return true;
		}

		return false;
	}

	public static boolean hasChanged(ChangeKind node) {
		if (node != ChangeKind.MOVED && node != ChangeKind.UNCHANGED && node != ChangeKind.UNKNOWN && node != ChangeKind.UNMAPPED)
			return true;
		return false;
	}

	public static boolean isExpressionModified(Expression node) {
		if (node.hasChange()) {
			if (hasChanged(node.getChange()))
				return true;
		}

		for (Expression ex : node.getExpressionsList()) {
			if (isExpressionModified(ex))
				return true;
		}
		return false;
	}
	public static boolean isExpressionImpactedOrModified(Expression node, Integer until) {
		if(until==null) return isExpressionModified(node) | isExpressionImpacted(node);
		for(int i=0; i<=until && i<node.getExpressionsCount();i++)
		{
			if(isExpressionModified(node.getExpressions(i)) || 
					isExpressionImpacted(node.getExpressions(i))) return true;
		}
		return false;
	}
	
	static void sliceMethodParam(Expression node) throws Exception
	{
		if(ForwardSlicerUtil.getNumMethodActualArg(node)>0)
		{
			for (Expression ex : node.getMethodArgs(0).getExpressionsList()) {
				if(ForwardSlicerUtil.isProperAssignKind(ex) && 
						(ex.getExpressions(1).getKind()==ExpressionKind.VARACCESS || ex.getExpressions(1).getKind()==ExpressionKind.METHODCALL))
				{
					if(isExpressionModified(ex.getExpressions(1)) 
							|| isExpressionImpacted(ex.getExpressions(1)))
					{
						Status.sliceSet.add(ex.getExpressions(1).getId());
					}
					else if(Status.acrossInSessionActive && acrossInVisitor.makeJump(ex.getExpressions(1), 
							ex.getExpressions(1).getKind()==ExpressionKind.VARACCESS)==JumpStatus.RETURN_IMPACTED)
						Status.sliceSet.add(ex.getExpressions(1).getId());
					else if(!Status.acrossInSessionActive && 
							acrossInVisitor.initiateJump(ex.getExpressions(1), 
									ex.getExpressions(1).getKind()==ExpressionKind.VARACCESS)==JumpStatus.RETURN_IMPACTED)
						Status.sliceSet.add(ex.getExpressions(1).getId());
				}
				else if(ex.getKind()==ExpressionKind.VARACCESS || ex.getKind()==ExpressionKind.METHODCALL)
				{
					if(isExpressionModified(ex) 
							|| isExpressionImpacted(ex))
					{
						Status.sliceSet.add(ex.getId());
					}
					else if(Status.acrossInSessionActive && acrossInVisitor.makeJump(ex, ex.getKind()==ExpressionKind.VARACCESS)==JumpStatus.RETURN_IMPACTED)
						Status.sliceSet.add(ex.getId());
					else if(!Status.acrossInSessionActive && acrossInVisitor.initiateJump(ex, ex.getKind()==ExpressionKind.VARACCESS)==JumpStatus.RETURN_IMPACTED)
						Status.sliceSet.add(ex.getId());
				}
				
			}
		}
	}
	public static SliceStatus addSliceToResult(Integer until, Expression node) throws Exception
	{
		if(node.hasId())
		{
			String identifierName="";
			if(until==null)
				identifierName = ForwardSlicerUtil.convertExpressionToString(node);
			else
				identifierName = ForwardSlicerUtil.convertExpressionToString(node, until);
			
			String mt2 = NameResolver.resolveImport(identifierName,null, node.getId());
			
//			if (Status.DEBUG&& ForwardSlicerUtil.isDebugBitSet(Status.DEBUG_SLICING_BIT))
//				System.out.println("Trying to slice: "+identifierName+", resolved to: "+mt2);
//			
			if(!mt2.equals(""))
			{
				if (Status.DEBUG&& ForwardSlicerUtil.isDebugBitSet(Status.DEBUG_SLICING_BIT))
					System.out.println("Trying to slice: "+identifierName+", resolved to: "+mt2);
				
				if(until==null)
				{
					int i=0;
					for(Expression ex: node.getExpressionsList())
					{
						if(ex.getKind()==ExpressionKind.METHODCALL)
						{
							addSliceToResult(i, node);
						}
						i++;
					}
				}
				sliceMethodParam(node);
				
				boolean doSlice=false;
				if(isExpressionImpactedOrModified(node, until))
				{
					doSlice=true;
				}
				
				int id=node.getId();
				if(until!=null) id=node.getExpressions(until).getId();
				
				Status.resolvedNameMap.put(id, mt2);
				
				if(doSlice)
				{
					if (Status.DEBUG && ForwardSlicerUtil.isDebugBitSet(Status.DEBUG_SLICING_BIT))
					{	
				        System.out.println(Status.ANSI_GREEN+"Sliced line# "+mt2+Status.ANSI_RESET);
					}
					Status.sliceSet.add(id);
					return SliceStatus.SLICE_DONE;
				}
				
				return SliceStatus.CANDIDATE_NOT_SLICED;
			}
		}
		return SliceStatus.NOT_CANDIDATE;
	}
}
