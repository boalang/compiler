package boa.graphs.slicers.python;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import boa.functions.BoaStringIntrinsics;
import boa.runtime.BoaAbstractVisitor;
import boa.types.Ast.ASTRoot;
import boa.types.Ast.Declaration;
import boa.types.Ast.Expression;
import boa.types.Ast.Method;
import boa.types.Ast.Namespace;
import boa.types.Ast.Statement;
import boa.types.Ast.Expression.ExpressionKind;
import boa.types.Ast.Statement.StatementKind;

public class AcrossInVisitor extends BoaAbstractVisitor {
	HashMap<String, Boolean> visitedScope=new HashMap<String, Boolean>();
	

	@Override
	protected boolean preVisit(final Method node) throws Exception {
		Status.namespaceScopeStack.push("method");

		return defaultPreVisit();
	}

	@Override
	protected boolean preVisit(final Statement node) throws Exception {

		
		final List<Statement> statementsList = node.getStatementsList();
		final int statementsSize = statementsList.size();
		for (int i = 0; i < statementsSize; i++)
			visit(statementsList.get(i));
		
		return false;
	}

	@Override
	protected boolean preVisit(final Expression node) throws Exception {
	
		return defaultPreVisit();
	}

	@Override
	protected void postVisit(final Expression node) throws Exception {
		if (ForwardSlicerUtil.isMethodCallKind(node)) {
			Status.statementScopeStack.pop();
		}
		defaultPostVisit();
	}

	@Override
	protected void postVisit(final Method node) throws Exception {
		Status.namespaceScopeStack.pop();
		defaultPostVisit();
	}

	public JumpStatus initiateJump(Expression mainNode) throws Exception {
		if (!Status.acrossInFlag || !mainNode.hasId() ||
				Status.currentCallDepth>=Status.maximumCallDepth)
			return JumpStatus.JUMP_NOT_MADE;

		Expression node = mainNode;
		if (ForwardSlicerUtil.isProperAssignKind(mainNode))
			node = mainNode.getExpressions(1);

		String methodName = resolveMethodNameForJump(ForwardSlicerUtil.convertExpressionToString(node), node.getId());

		if (methodName != "")
			return JumpStatus.JUMP_NOT_MADE;
		
		visitedScope.clear();
		
		visitedScope.put(methodName, true);
		
		String scope=Status.getCurrentScope();
		Status.acrossInStack.push(scope);
		Status.acrossInStack.push(scope+Status.acrossInStackSeparator+methodName);
		
		Status.acrossInSessionActive=true;
		Status.currentCallDepth=0;
		
		visit(Status.astMethodMap.get(methodName));
		
		Status.acrossInSessionActive=false;
		Status.acrossInStack.clear();
		
		return JumpStatus.JUMP_MADE;
	}

	public static String resolveMethodNameForJump(String methodName, Integer id) {

		String tmp = NameResolver.resolveObjectName(methodName, id);

		if (tmp == "")
			tmp = methodName;
		tmp = resolveMethodNameForJump(tmp);

		return tmp;
	}

	public static String resolveMethodNameForJump(String methodName) {

		String scope = Status.getCurrentScope();
		String[] tarr = BoaStringIntrinsics.splitall(methodName, "\\.");
		if (tarr.length == 0)
			return "";
		for (int i = 0; i <= tarr.length; i++) {
			String tmp = scope;
			if (scope == "")
				tmp = methodName;
			else
				tmp = scope + "." + methodName;

			if (Status.astMethodMap.containsKey(tmp)) {
				return tmp;
			}
			scope = Status.getParentScope(scope);
		}
		return "";
	}
}
