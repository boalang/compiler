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

	public ForwardSlicer(ASTRoot _root, String[] moduleFilter, String[] filterCriteria, boolean changeImpactFlag,
			boolean acrossInFlag) {
		this.root = _root;

		Status.setLibraryFilter(filterCriteria);
		Status.setModuleFilter(moduleFilter);
		Status.changeImpactAnalysisFlag = changeImpactFlag;
		Status.acrossInFlag = acrossInFlag;

		SymbolTableGenerator st = new SymbolTableGenerator();

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
		if (ForwardSlicerUtil.isMethodCallKind(node)) {
			Status.statementScopeStack.push("call");
		}
		if (ForwardSlicerUtil.isMethodCallKind(node)) {
			String identifierName = ForwardSlicerUtil.convertExpressionToString(node);

			String mt2 = NameResolver.resolveImport(identifierName, node.getId());
//			 if(!mt2.equals(""))
//				 System.out.println(mt2+" : "+node.getId());

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

		defaultPostVisit();
	}

	@Override
	protected void postVisit(final Method node) throws Exception {
		Status.globalScopeNameStack.pop();

		defaultPostVisit();
	}

	void addForCriteria(List<Expression> leftExps, List<Expression> rightExps) {
		String scope = Status.getCurrentScope();

		if (leftExps.size() == rightExps.size()) {
			for (int i = 0; i < rightExps.size(); i++) {

				String identiferName = ForwardSlicerUtil.convertExpressionToString(leftExps.get(i));

				if (!identiferName.equals("_") && !identiferName.equals(".") && !identiferName.equals("")) {

					String rightIdentifierName = ForwardSlicerUtil.convertExpressionToString(rightExps.get(i));

					if (ChangeImpactAnalysis.isExpressionModified(rightExps.get(i))
							|| ChangeImpactAnalysis.isExpressionImpacted(rightExps.get(i))) {

						if (rightExps.get(i).getKind() == ExpressionKind.METHODCALL) {
							String mt2 = NameResolver.resolveImport(rightIdentifierName, rightExps.get(i).getId());
							if (!mt2.equals("")) {
								SymbolTable.addToCriteria(scope, identiferName, leftExps.get(i).getId());
								System.out.println("Adding in slice criteria, Scope: " + scope + ", Variable:"
										+ identiferName + ",Location: " + leftExps.get(i).getId());
							}
						} else {
							SymbolTable.addToCriteria(scope, identiferName, leftExps.get(i).getId());
							System.out.println("Adding in slice criteria, Scope: " + scope + ", Variable:"
									+ identiferName + ",Location: " + leftExps.get(i).getId());

						}

					}

				}
			}
		} else if (rightExps.size() == 1 && rightExps.get(0).getKind() == ExpressionKind.METHODCALL) {
			String rightIdentifierName = ForwardSlicerUtil.convertExpressionToString(rightExps.get(0));

			if (ChangeImpactAnalysis.isExpressionModified(rightExps.get(0))
					|| ChangeImpactAnalysis.isExpressionImpacted(rightExps.get(0))) {

				String mt2 = NameResolver.resolveImport(rightIdentifierName, rightExps.get(0).getId());
				if (!mt2.equals("")) {
					for (Expression ex : leftExps) {
						SymbolTable.addToCriteria(scope, ForwardSlicerUtil.convertExpressionToString(ex), ex.getId());
						System.out.println("Adding in slice criteria, Scope: " + scope + ", Variable:"
								+ ForwardSlicerUtil.convertExpressionToString(ex) + ",Location: " + ex.getId());
					}
				}

			}
		}
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

					String mt2 = NameResolver.resolveImport(rightIdentifierName, rightExps.get(i).getId());
					if (!mt2.equals("")) {
						Status.aliasName.put(leftExps.get(i).getId(), mt2);
						System.out.println("Mapping for alias: " + identiferName + " ==> " + mt2);
					}
				}
			}
		}
	}
}
