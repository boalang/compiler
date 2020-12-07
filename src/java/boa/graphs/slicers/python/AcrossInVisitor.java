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
import boa.types.Ast.Modifier;
import boa.types.Ast.Namespace;
import boa.types.Ast.Statement;
import boa.types.Ast.Expression.ExpressionKind;
import boa.types.Ast.Modifier.ModifierKind;
import boa.types.Ast.Statement.StatementKind;

public class AcrossInVisitor extends BoaAbstractVisitor {
	HashMap<String, Boolean> visitedScope = new HashMap<String, Boolean>();

	@Override
	protected boolean preVisit(final Method node) throws Exception {
		Status.namespaceScopeStack.push("method");

		return defaultPreVisit();
	}

	@Override
	protected boolean preVisit(final Statement node) throws Exception {

		if (node.getKind() == StatementKind.ASSERT)
			return false;

		if (node.getKind() == StatementKind.RETURN && node.getExpressionsCount() > 0) {
			if (SliceCriteriaAnalysis.isExpressionModified(node.getExpressions(0))
					|| SliceCriteriaAnalysis.isExpressionImpacted(node.getExpressions(0))) {
				Status.returnImpacted.put(Status.getCurrentScope(), true);
			}
		}

		ForwardSlicer.handleStatementForSymbolTable(node);

		final List<Statement> statementsList = node.getStatementsList();
		final int statementsSize = statementsList.size();
		for (int i = 0; i < statementsSize; i++)
			visit(statementsList.get(i));

		final List<Expression> initsList = node.getInitializationsList();
		final int initsSize = initsList.size();
		for (int i = 0; i < initsSize; i++)
			visit(initsList.get(i));

		final List<Expression> conditionsList = node.getConditionsList();
		final int conditionsSize = conditionsList.size();
		for (int i = 0; i < conditionsSize; i++)
			visit(conditionsList.get(i));

		if (node.hasVariableDeclaration())
			visit(node.getVariableDeclaration());

		if (node.getExpressionsCount() > 0)
			visit(node.getExpressions(0));

		return false;
	}

	@Override
	protected boolean preVisit(final Modifier node) throws Exception {
		if (node.getKind() == ModifierKind.ANNOTATION)
			return false;
		return defaultPreVisit();
	}

	@Override
	protected boolean preVisit(final Expression node) throws Exception {

		if (node.getKind() == ExpressionKind.YIELD && node.getExpressionsCount() > 0) {
			if (SliceCriteriaAnalysis.isExpressionModified(node.getExpressions(0))
					|| SliceCriteriaAnalysis.isExpressionImpacted(node.getExpressions(0))) {
				Status.returnImpacted.put(Status.getCurrentScope(), true);
			}
		}

		if (ForwardSlicerUtil.isProperAssignKind(node) && !Status.isMethodCallScope()) {
			ForwardSlicer.handleExpressionForSymbolTable(node);

			makeJump(node,false);
		}

		if (ForwardSlicerUtil.isMethodCallKind(node)) {
			if (SliceCriteriaAnalysis.addSliceToResult(node) == SliceStatus.NOT_CANDIDATE) {
				makeJump(node,false);
			}
		}

		if (ForwardSlicerUtil.isMethodCallKind(node)) {
			Status.statementScopeStack.push("call");
		}

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
		String scope = Status.getCurrentScope();
		SymbolTable.removeCriteriaMap(scope);
		SymbolTable.removeAliasMap(scope);
		Status.symbolTable.remove(scope);
		defaultPostVisit();
	}

	public JumpStatus initiateJump(Expression mainNode, boolean isCallback) throws Exception {
				
		if (Status.DEBUG)
			System.out.println(
					"Initiating across-in traversal for: " + ForwardSlicerUtil.convertExpressionToString(mainNode));

		String scope = Status.getCurrentScope();
		Status.acrossInStack.push(scope);

		Status.acrossInSessionActive = true;
		Status.currentCallDepth = 0;

		JumpStatus jumpStatus = makeJump(mainNode, isCallback);

		Status.acrossInSessionActive = false;
		Status.acrossInStack.clear();
		visitedScope.clear();

		if (Status.DEBUG)
			System.out.println("Exiting across-in traversal: " + jumpStatus.toString());
		
		return jumpStatus;
	}

	public JumpStatus makeJump(Expression mainNode, boolean isCallback) throws Exception {
		if (!Status.acrossInFlag || !mainNode.hasId() || Status.currentCallDepth >= Status.maximumCallDepth)
			return JumpStatus.JUMP_NOT_MADE;

		List<Expression> leftExps = new ArrayList<Expression>();
		List<Expression> rightExps = new ArrayList<Expression>();

		if (ForwardSlicerUtil.isProperAssignKind(mainNode)) {
			leftExps = ForwardSlicerUtil.expandOtherExpressions(mainNode.getExpressions(0));
			rightExps = ForwardSlicerUtil.expandOtherExpressions(mainNode.getExpressions(1));

			if (rightExps.size() != 1 && rightExps.size() != leftExps.size())
				return JumpStatus.JUMP_NOT_MADE;

			if (rightExps.size() == leftExps.size()) {
				JumpStatus jumpStatus = JumpStatus.JUMP_NOT_MADE;
				for (int i = 0; i < leftExps.size(); i++) {
					if (makeJump(leftExps.get(i), rightExps.get(i), isCallback) == JumpStatus.JUMP_MADE)
						jumpStatus = JumpStatus.JUMP_MADE;
				}
				return jumpStatus;
			} else
				return makeJump(mainNode.getExpressions(0), 
						mainNode.getExpressions(1), isCallback);
		} else
			return makeJump(null, mainNode, isCallback);
	}

	private JumpStatus makeJump(Expression left, Expression right, boolean isCallback) throws Exception {
		if (right == null)
			return JumpStatus.JUMP_NOT_MADE;
		if(isCallback==false && right.getKind() != ExpressionKind.METHODCALL)
			return JumpStatus.JUMP_NOT_MADE;
		if(isCallback && right.getKind() != ExpressionKind.VARACCESS)
			return JumpStatus.JUMP_NOT_MADE;
		
		Integer leftId = null;
		if (left != null)
			leftId = left.getId();
		String methodName = resolveMethodNameForJump(ForwardSlicerUtil.convertExpressionToString(right), leftId,
				right.getId());

		if (Status.DEBUG)
			System.out.println("Resolved method name for across-in jump: " + methodName);

		if (methodName == "" || visitedScope.containsKey(methodName))
			return JumpStatus.JUMP_NOT_MADE;

		visitedScope.put(methodName, true);
		Status.callPointMap.put(methodName, right.getId());

		String scope = Status.getCurrentScope();
		String nextScope = scope + Status.acrossInStackSeparator + methodName;

		if (isCallback || this.mapParameter(Status.astMethodMap.get(methodName), right, nextScope)) {
			Status.acrossInStack.push(methodName);

			Status.currentCallDepth = Status.currentCallDepth + 1;

			visit(Status.astMethodMap.get(methodName));

			if (left != null && Status.returnImpacted.containsKey(nextScope)) {
				for (Expression ex : ForwardSlicerUtil.expandOtherExpressions(left)) {
					SymbolTable.addToCriteria(ForwardSlicerUtil.convertExpressionToString(ex), ex.getId(), scope);
					if (Status.DEBUG) {
						System.out.println("Adding in slice criteria (return-mapping), Scope: " + scope + ", Variable:"
								+ ForwardSlicerUtil.convertExpressionToString(ex) + ",Location: " + ex.getId());
					}
				}
			}

			Status.acrossInStack.pop();
	
			Status.currentCallDepth = Status.currentCallDepth - 1;
		}
		visitedScope.remove(methodName);
		Status.callPointMap.remove(methodName);

		if (Status.DEBUG)
			System.out.println("Across-in jump made to " + methodName);

		if (Status.returnImpacted.containsKey(nextScope)) {
			Status.returnImpacted.remove(nextScope);
			return JumpStatus.RETURN_IMPACTED;
		}
		return JumpStatus.JUMP_MADE;
	}

	public static boolean mapParameter(Method targetMethod, Expression callSite, String nextScope) {
		if (ForwardSlicerUtil.getNumMethodFormalArg(targetMethod) != ForwardSlicerUtil.getNumMethodActualArg(callSite))
			return false;
		Status.isParameterMapping=true;
		int j = 0;
		if (ForwardSlicerUtil.hasSelfArg(targetMethod))
			j = 1;
		for (Expression ex : callSite.getMethodArgs(0).getExpressionsList()) {
			String leftIdentiferName = ForwardSlicerUtil.getIdentiferName(targetMethod.getArguments(j));
			Integer leftId = targetMethod.getArguments(j).getId();

			if (ForwardSlicerUtil.isProperAssignKind(ex)) {
				String rightIdentifierName = ForwardSlicerUtil.convertExpressionToString(ex.getExpressions(0));

				if (SliceCriteriaAnalysis.isExpressionModified(ex.getExpressions(1))
						|| SliceCriteriaAnalysis.isExpressionImpacted(ex.getExpressions(1))) {
					SymbolTable.addToCriteria(rightIdentifierName, leftId, nextScope);
					if (Status.DEBUG) {
						System.out.println("Adding in slice criteria (parameter-mapping), Scope: " + nextScope
								+ ", Variable:" + rightIdentifierName + ",Location: " + leftId);
					}
				}
			    rightIdentifierName = ForwardSlicerUtil.convertExpressionToString(ex.getExpressions(1));

				String mt2 = NameResolver.resolveName(rightIdentifierName, ex.getExpressions(0).getId(),
						ex.getExpressions(1).getId());
				if (!mt2.equals("")) {
					SymbolTable.addToAliasSet(leftId, mt2, nextScope);
					if (Status.DEBUG)
						System.out
								.println("Mapping for alias(parameter-mapping): " + leftIdentiferName + " ==> " + mt2);
				}
			} else if (SliceCriteriaAnalysis.isExpressionModified(ex)
					|| SliceCriteriaAnalysis.isExpressionImpacted(ex)) {
				
				SymbolTable.addToCriteria(leftIdentiferName, leftId, nextScope);
				if (Status.DEBUG) {
					System.out.println("Adding in slice criteria (parameter-mapping), Scope: " + nextScope
							+ ", Variable:" + leftIdentiferName + ",Location: " + leftId);
				}
				String rightIdentifierName = ForwardSlicerUtil.convertExpressionToString(ex);

				String mt2 = NameResolver.resolveName(rightIdentifierName, null, ex.getId());
				if (!mt2.equals("")) {
					SymbolTable.addToAliasSet(leftId, mt2, nextScope);
					if (Status.DEBUG)
						System.out
								.println("Mapping for alias(parameter-mapping): " + leftIdentiferName + " ==> " + mt2);
				}

			}
			j++;
		}
		Status.isParameterMapping=false;
		return true;
	}

	public static String resolveMethodNameForJump(String methodName, Integer defAstLocation, Integer id) {

		String tmp = NameResolver.resolveObjectName(methodName, defAstLocation, id);

		if (tmp == "")
			tmp = methodName;
		methodName=tmp;
		tmp = resolveMethodNameForJump(tmp);
		if (tmp == "" && methodName.startsWith("self."))
			tmp = methodName.substring(5);

		return resolveMethodNameForJump(tmp);
	}

	private static String resolveMethodNameForJump(String methodName) {

		String scope = Status.getProperCurrentScope();
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
