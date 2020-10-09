package boa.graphs.slicers.python;

import java.util.ArrayList;

import boa.types.Ast.Expression;
import boa.types.Ast.Expression.ExpressionKind;
import boa.types.Shared.ChangeKind;

public class SliceCriteriaAnalysis {

	public static boolean isImpacted(String usedIdentifierName, Integer useAstLocation) {
		return isImpacted(usedIdentifierName, useAstLocation, Status.getCurrentScope());
	}

	public static boolean isImpacted(String usedIdentifierName, Integer useAstLocation, String scope) {
		String targetScope = scope;
		while (!scope.equals("")) {
			if (isImpacted(usedIdentifierName, useAstLocation, scope, targetScope))
				return true;
			scope = Status.getParentScope(scope);
		}
		return false;
	}

	private static boolean isImpacted(String usedIdentifierName, Integer useAstLocation, String sourceScope,
			String targetScope) {
		ArrayList<Integer> criterias = SymbolTable.getCriteriaLocations(sourceScope, usedIdentifierName);
		if (criterias == null || criterias.size() == 0)
			return false; // not defined in this scope

		for (Integer loc : criterias) {
			if (CfgUtil.isAstNodesReachable(loc, useAstLocation, usedIdentifierName, sourceScope, targetScope)) {
				return true;
			}
		}
		return false;
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
		if (node != ChangeKind.UNCHANGED && node != ChangeKind.UNKNOWN && node != ChangeKind.UNMAPPED)
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
	
	public static SliceStatus addSliceToResult(Expression node)
	{
		if(node.hasId())
		{
			String identifierName = ForwardSlicerUtil.convertExpressionToString(node);

			String mt2 = NameResolver.resolveImport(identifierName, node.getId());
			
			if(!mt2.equals(""))
			{
				if(isExpressionImpacted(node) || isExpressionImpacted(node))
				{
					if (Status.DEBUG)
						System.out.println("Sliced line# "+mt2);
					
					return SliceStatus.SLICE_DONE;
				}
				
				return SliceStatus.CANDIDATE_NOT_SLICED;
			}
		}
		return SliceStatus.NOT_CANDIDATE;
	}
}
