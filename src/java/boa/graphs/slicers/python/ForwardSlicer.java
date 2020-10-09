package boa.graphs.slicers.python;

import java.util.ArrayList;
import java.util.List;

import boa.runtime.BoaAbstractVisitor;
import boa.types.Ast.ASTRoot;
import boa.types.Ast.Declaration;
import boa.types.Ast.Expression;
import boa.types.Ast.Method;
import boa.types.Ast.Namespace;
import boa.types.Ast.Statement;
import boa.types.Ast.Expression.ExpressionKind;
import boa.types.Ast.Statement.StatementKind;

public class ForwardSlicer extends BoaAbstractVisitor {

	ASTRoot root;

	public ForwardSlicer(ASTRoot _root, String[] moduleFilter, String[] filterCriteria, boolean changeImpactFlag,
			boolean acrossInFlag) {
		this.root = _root;

		Status.DEBUG = true;
		Status.acrossInFlag=true;

		Status.setLibraryFilter(filterCriteria);
		Status.setModuleFilter(moduleFilter);
		Status.changeImpactAnalysisFlag = changeImpactFlag;
		Status.acrossInFlag = acrossInFlag;

		SymbolTableGenerator st = new SymbolTableGenerator();

		try {
			st.visit(this.root);

			if (Status.DEBUG) {
				SymbolTable.printSymbolTable();
				Status.printMap(Status.importMap);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected boolean preVisit(final Namespace node) throws Exception {
		Status.globalScopeNameStack.push(node.getName().replace(".", "_"));

		return defaultPreVisit();
	}

	@Override
	protected boolean preVisit(final Declaration node) throws Exception {

		Status.globalScopeNameStack.push(node.getName().replace(".", "_"));
		Status.namespaceScopeStack.push("class");
		return defaultPreVisit();
	}

	@Override
	protected boolean preVisit(final Method node) throws Exception {
		Status.globalScopeNameStack.push(node.getName().replace(".", "_"));
		Status.namespaceScopeStack.push("method");
		
		return defaultPreVisit();
	}

	@Override
	protected boolean preVisit(final Statement node) throws Exception {

		if (node.getKind() == StatementKind.FOREACH || node.getKind() == StatementKind.WITH) {
			if (node.getExpressionsCount() > 0 && node.hasId()) {
				List<Expression> rightExps = ForwardSlicerUtil.expandOtherExpressions(node.getExpressions(0));
				List<String> leftIdentifiers = ForwardSlicerUtil.getIdentiferNamesAsList(node);
				if (leftIdentifiers != null) {
					this.addForNameResolution(leftIdentifiers.toArray(new String[0]), node.getId(), rightExps);
					this.addForCriteria(leftIdentifiers.toArray(new String[0]), node.getId(), rightExps);
				}
			}
		}

		return defaultPreVisit();
	}

	@Override
	protected boolean preVisit(final Expression node) throws Exception {
		if (ForwardSlicerUtil.isMethodCallKind(node)) {
			Status.statementScopeStack.push("call");
		}

		if (ForwardSlicerUtil.isMethodCallKind(node)) {
			if(SliceCriteriaAnalysis.addSliceToResult(node)==SliceStatus.NOT_CANDIDATE)
			{
				
			}
		}

		if (ForwardSlicerUtil.isProperAssignKind(node)) {
			if (Status.isMethodCallScope())
				return defaultPreVisit();

			List<Expression> leftExps = ForwardSlicerUtil.expandOtherExpressions(node.getExpressions(0));
			List<Expression> rightExps = ForwardSlicerUtil.expandOtherExpressions(node.getExpressions(1));

			this.addForNameResolution(leftExps, rightExps);
			this.addForCriteria(leftExps, rightExps);
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
	protected void postVisit(final Namespace node) throws Exception {
		Status.globalScopeNameStack.pop();

		defaultPostVisit();
	}

	@Override
	protected void postVisit(final Declaration node) throws Exception {
		Status.globalScopeNameStack.pop();
		Status.namespaceScopeStack.pop();
		defaultPostVisit();
	}

	@Override
	protected void postVisit(final Method node) throws Exception {
		Status.globalScopeNameStack.pop();
		Status.namespaceScopeStack.pop();
		defaultPostVisit();
	}

	void addForCriteria(String[] leftIdentifiers, Integer id, List<Expression> rightExps) {
		List<Expression> left = new ArrayList<Expression>();
		for (int i = 0; i < leftIdentifiers.length; i++) {
			Expression.Builder ex = Expression.newBuilder();
			ex.setVariable(leftIdentifiers[i]);
			ex.setId(id);
			ex.setKind(ExpressionKind.VARACCESS);
			left.add(ex.build());
		}
		addForCriteria(left, rightExps);
	}

	void addForCriteria(List<Expression> leftExps, List<Expression> rightExps) {
		String scope = Status.getCurrentScope();

		if (leftExps.size() == rightExps.size()) {
			for (int i = 0; i < rightExps.size(); i++) {

				String identiferName = ForwardSlicerUtil.convertExpressionToString(leftExps.get(i));

				if (!identiferName.equals("_") && !identiferName.equals(".") && !identiferName.equals("")) {

					String rightIdentifierName = ForwardSlicerUtil.convertExpressionToString(rightExps.get(i));

					if (SliceCriteriaAnalysis.isExpressionModified(rightExps.get(i))
							|| SliceCriteriaAnalysis.isExpressionImpacted(rightExps.get(i))) {

						if (rightExps.get(i).getKind() == ExpressionKind.METHODCALL) {
							String mt2 = NameResolver.resolveImport(rightIdentifierName, rightExps.get(i).getId());
							if (!mt2.equals("")) {
								SymbolTable.addToCriteria(identiferName, leftExps.get(i).getId());
								
								if (Status.DEBUG)
									System.out.println("Adding in slice criteria, Scope: " + scope + ", Variable:"
										+ identiferName + ",Location: " + leftExps.get(i).getId());
							}
						} else {
							SymbolTable.addToCriteria(identiferName, leftExps.get(i).getId());
							
							if (Status.DEBUG)
								System.out.println("Adding in slice criteria, Scope: " + scope + ", Variable:"
									+ identiferName + ",Location: " + leftExps.get(i).getId());

						}

					}

				}
			}
		} else if (rightExps.size() == 1 && rightExps.get(0).getKind() == ExpressionKind.METHODCALL) {
			String rightIdentifierName = ForwardSlicerUtil.convertExpressionToString(rightExps.get(0));

			if (SliceCriteriaAnalysis.isExpressionModified(rightExps.get(0))
					|| SliceCriteriaAnalysis.isExpressionImpacted(rightExps.get(0))) {

				String mt2 = NameResolver.resolveImport(rightIdentifierName, rightExps.get(0).getId());
				if (!mt2.equals("")) {
					for (Expression ex : leftExps) {
						String identiferName = ForwardSlicerUtil.convertExpressionToString(ex);

						if (!identiferName.equals("_") && !identiferName.equals(".") && !identiferName.equals("")) {

							SymbolTable.addToCriteria(identiferName, ex.getId());
							
							if (Status.DEBUG)
								System.out.println("Adding in slice criteria, Scope: " + scope + ", Variable:"
									+ identiferName + ",Location: " + ex.getId());
						}
					}
				}

			}
		}
	}

	void addForNameResolution(String[] leftIdentifiers, Integer id, List<Expression> rightExps) {
		List<Expression> left = new ArrayList<Expression>();
		for (int i = 0; i < leftIdentifiers.length; i++) {
			Expression.Builder ex = Expression.newBuilder();
			ex.setVariable(leftIdentifiers[i]);
			ex.setId(id);
			ex.setKind(ExpressionKind.VARACCESS);
			left.add(ex.build());
		}
		addForNameResolution(left, rightExps);

	}

	void addForNameResolution(List<Expression> leftExps, List<Expression> rightExps) {
		if (leftExps.size() == rightExps.size()) {
			for (int i = 0; i < rightExps.size(); i++) {
				if (rightExps.get(i).getKind() != ExpressionKind.METHODCALL
						&& rightExps.get(i).getKind() != ExpressionKind.VARACCESS
						&& rightExps.get(i).getKind() != ExpressionKind.ARRAYACCESS)
					continue;

				String identiferName = ForwardSlicerUtil.convertExpressionToString(leftExps.get(i));
				if (!identiferName.equals("_") && !identiferName.equals(".") && !identiferName.equals("")) {
					String rightIdentifierName = ForwardSlicerUtil.convertExpressionToString(rightExps.get(i));

					String mt2 = NameResolver.resolveName(rightIdentifierName, rightExps.get(i).getId());
					if (!mt2.equals("")) {
						Status.aliasName.put(leftExps.get(i).getId(), mt2);
						
						if (Status.DEBUG)
							System.out.println("Mapping for alias: " + identiferName + " ==> " + mt2);
					}
				}
			}
		}
	}
}
