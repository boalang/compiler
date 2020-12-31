package boa.graphs.slicers.python;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;

import boa.graphs.cfg.CFG;
import boa.graphs.cfg.CFGNode;
import boa.types.Ast.Statement.StatementKind;

public class CfgUtil {
	static boolean[] visitedCfgNode;

	public static boolean isAstNodesReachable(Integer sourceId, Integer targetId, String identifierName) {
		return isAstNodesReachable(sourceId, targetId, identifierName, Status.getCurrentScope(),
				Status.getCurrentScope());
	}

	public static boolean isAstNodesReachable(Integer sourceId, Integer targetId, String identifierName,
			String sourceScope, String targetScope) {
		if (!Status.cfgToAstIdMap.containsKey(sourceId))
			return false;
		if (!Status.cfgToAstIdMap.containsKey(targetId))
			return false;

		sourceId = Status.cfgToAstIdMap.get(sourceId);
		targetId = Status.cfgToAstIdMap.get(targetId);
		
		if (!sourceScope.equals(Status.getAcrossInScopeFromProper(targetScope))) {
			Integer intermediateCfgTargetId = -1;

			if (Status.acrossInSessionActive) {
				if (Status.callPointMap.containsKey(sourceScope)) {
					Integer callpointId = Status.callPointMap.get(sourceScope);
					if (!Status.cfgToAstIdMap.containsKey(callpointId))
						return false;
					intermediateCfgTargetId = Status.cfgToAstIdMap.get(callpointId);
				}
			}
			else
			{
				if(!isCfgDefined(Status.getProperScope(sourceScope))) return false;
				intermediateCfgTargetId=(int) Status.cfgMap.
						get(Status.getProperScope(sourceScope)).getExitNode().getId();
			
			}
			
			if(intermediateCfgTargetId!=-1 && !isCfgNodesReachable(sourceId, intermediateCfgTargetId, identifierName, sourceScope))
				return false;
			
			sourceId = 0;
		}

		return isCfgNodesReachable(sourceId, targetId, identifierName, targetScope);
	}

	public static boolean isCfgNodesReachable(Integer sourceId, Integer targetId, String identifierName, String scope) {
		scope=Status.getProperScope(scope);
		
		if (!isCfgDefined(scope))
			return false;

		Stack<CFGNode> st = new Stack<CFGNode>();
		CFG cfg = Status.cfgMap.get(scope);
		for (CFGNode cn : cfg.getNodes()) {
			if (cn.getId() == sourceId) {
				st.push(cn);
				break;
			}
		}

//		Status.hasBeenRedefinedAnywhere = false;

		visitedCfgNode = new boolean[cfg.getNodes().size()];

		boolean pathFound = false;

		while (!st.isEmpty()) {
			CFGNode t = st.pop();
			visitedCfgNode[(int) t.getId()] = true;

			if (t.getId() != sourceId) {
				HashMap<String, Integer> ids = null;

				if (t.hasExpr() && ForwardSlicerUtil.isProperAssignKind(t.getExpr()))
					ids = ForwardSlicerUtil.getIdentiferNames(t.getExpr().getExpressions(0));
				if (t.hasStmt()) {
					if (t.getStmt().getKind() == StatementKind.FOREACH || t.getStmt().getKind() == StatementKind.WITH) {
						ids = ForwardSlicerUtil.getIdentiferNames(t.getStmt());
					}
				}
				if (ids != null && ids.containsKey(identifierName)) {
//					Status.hasBeenRedefinedAnywhere = true;
					continue;
				}
			}
			if (t.getId() == targetId) {
				pathFound = true;
				st.clear();
				return true;
			}

			for (CFGNode v : t.getSuccessors())
				if (!visitedCfgNode[(int) v.getId()])
					st.push(v);
		}

		return pathFound;

	}

	public static boolean isCfgDefined(String scope) {
		if (!Status.cfgMap.containsKey(scope))
			return false;
		if (Status.cfgMap.get(scope) == null)
			return false;
		if (Status.cfgMap.get(scope).getNodes() == null || Status.cfgMap.get(scope).getNodes().size() < 1)
			return false;
		return true;
	}

}
