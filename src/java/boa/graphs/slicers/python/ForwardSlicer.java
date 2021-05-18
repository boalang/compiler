package boa.graphs.slicers.python;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import boa.runtime.BoaAbstractVisitor;
import boa.types.Ast.ASTRoot;
import boa.types.Ast.Declaration;
import boa.types.Ast.Expression;
import boa.types.Ast.Method;
import boa.types.Ast.Namespace;
import boa.types.Ast.Statement;
import boa.types.Ast.Expression.ExpressionKind;
import boa.types.Ast.Statement.StatementKind;
import boa.types.Ast.Variable;

public class ForwardSlicer extends BoaAbstractVisitor {

	ASTRoot root;
	AcrossInVisitor acrossInVisitor = new AcrossInVisitor();
	boolean firstTurn = true;

	public ForwardSlicer(ASTRoot _root, String[] moduleFilter, String[] filterCriteria) {
		
//		Status.CRIERIA_FLAG=false; //remove
		
		this.root = _root;

		Status.setLibraryFilter(filterCriteria);
		Status.setModuleFilter(moduleFilter);

		SymbolTableGenerator st = new SymbolTableGenerator();

		try {
			st.visit(this.root);

			if (Status.DEBUG) {
				if(ForwardSlicerUtil.isDebugBitSet())
				{
					SymbolTable.printSymbolTable();
					Status.printMap(Status.importMap);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public ASTRoot initiateVisit(boolean acrossInFlag) {
		
		if (!Status.isModuleFound) {
			return root;
		}
		
		Status.acrossInFlag = false;
		try {
			this.visit(root);
			Status.acrossInFlag = acrossInFlag;
			firstTurn = false;
			this.visit(root);

			ASTRoot.Builder retAst = ASTRoot.newBuilder();
			if(Status.BACKWARD)
				retAst.addNamespaces(new TreeChangeSetter().visit(root.getNamespaces(1)));
			else
				retAst.addNamespaces(new TreeChangeSetter().visit(root.getNamespaces(0)));
			
			Status.clear();
			
			return retAst.build();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return root;
	}

	@Override
	protected boolean preVisit(final ASTRoot node) throws Exception {
		final List<Namespace> namespacesList = node.getNamespacesList();		
		if(Status.BACKWARD)
			visit(namespacesList.get(1));
		else
			visit(namespacesList.get(0));
		
		return false;
	}
	
	protected boolean preVisit(final Namespace node) throws Exception {
		Status.globalScopeNameStack.push(node.getName().replace(".", "_"));

		return defaultPreVisit();
	}

	@Override
	protected boolean preVisit(final Declaration node) throws Exception {

		Status.globalScopeNameStack.push(node.getName().replace(".", "_"));
		Status.namespaceScopeStack.push("class");

		if (Status.DEBUG)
		{
			if(ForwardSlicerUtil.isDebugBitSet(Status.DEBUG_STACK_TRACE_BIT))
				System.out.println("In class: " + node.getName());
		}

		return defaultPreVisit();
	}

	@Override
	protected boolean preVisit(final Method node) throws Exception {
		Status.globalScopeNameStack.push(node.getName().replace(".", "_"));
		Status.namespaceScopeStack.push("method");

		if (Status.DEBUG)
		{
			if(ForwardSlicerUtil.isDebugBitSet(Status.DEBUG_STACK_TRACE_BIT))
				System.out.println("In method: " + node.getName());
		}

		return defaultPreVisit();
	}

	@Override
	protected boolean preVisit(final Statement node) throws Exception {
		if (node.getKind() == StatementKind.ASSERT)
			return false;

		if (firstTurn)
			handleStatementForSymbolTable(node);
		return defaultPreVisit();
	}

	@Override
	protected boolean preVisit(final Expression node) throws Exception {

		if (ForwardSlicerUtil.isMethodCallKind(node) && !firstTurn) {
			if (SliceCriteriaAnalysis.addSliceToResult(null, node) == SliceStatus.NOT_CANDIDATE) {
				acrossInVisitor.initiateJump(node, false);
			}
		}

		if (ForwardSlicerUtil.isProperAssignKind(node) && !Status.isMethodCallScope()) {
			if (firstTurn)
				handleExpressionForSymbolTable(node);
			else
				acrossInVisitor.initiateJump(node, false);
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

	public static void handleStatementForSymbolTable(Statement node) {
		if (node.getKind() == StatementKind.FOREACH || node.getKind() == StatementKind.WITH) {

			List<Expression> rightExps = new ArrayList<>();
			HashMap<String, Integer> leftIdentifiers = new HashMap<String, Integer>();

			if (node.getKind() == StatementKind.WITH) {
				if (node.hasId()) {

					for (Variable b : node.getVariableDeclarationsList()) {
						if (b.hasInitializer()) {
							rightExps.add(b.getInitializer());
							leftIdentifiers.put(ForwardSlicerUtil.getVariableName(b), b.getId());
						}

					}

				}
			}
			if (node.getKind() == StatementKind.FOREACH) {
				if (node.hasId()) {

					for (Expression b : node.getConditionsList()) {
						rightExps.add(b);
					}

				}
				leftIdentifiers=ForwardSlicerUtil.getIdentiferNames(node);
			}
			if (leftIdentifiers != null && leftIdentifiers.size()>0) {
				addForNameResolution(leftIdentifiers, rightExps);
				addForCriteria(leftIdentifiers, rightExps);
			}
		}
	}

	public static void handleExpressionForSymbolTable(Expression node) {
		if (ForwardSlicerUtil.isProperAssignKind(node)) {

			List<Expression> leftExps = ForwardSlicerUtil.expandOtherExpressions(node.getExpressions(0));
			List<Expression> rightExps = ForwardSlicerUtil.expandOtherExpressions(node.getExpressions(1));
			
			if(leftExps!=null && leftExps.size()>0)
			{
				addForNameResolution(leftExps, rightExps);
				addForCriteria(leftExps, rightExps);
			}
		}
	}

	public static void addForCriteria(HashMap<String, Integer> leftIdentifiers, List<Expression> rightExps) {
		if(Status.CRIERIA_FLAG==false) 
			return;
		
		List<Expression> left = new ArrayList<Expression>();
		for (Map.Entry<String, Integer> entry : leftIdentifiers.entrySet()) {
			String identiferName = entry.getKey();
			Integer location = entry.getValue();
			Expression.Builder ex = Expression.newBuilder();
			ex.setVariable(identiferName);
			ex.setId(location);
			ex.setKind(ExpressionKind.VARACCESS);
			left.add(ex.build());
		}
		addForCriteria(left, rightExps);
	}

	public static void addForCriteria(List<Expression> leftExps, List<Expression> rightExps) {
		if(Status.CRIERIA_FLAG==false) 
			return;
		
		String scope = Status.getCurrentScope();

		if (leftExps.size() == rightExps.size()) {
			for (int i = 0; i < rightExps.size(); i++) {

				String identiferName = ForwardSlicerUtil.convertExpressionToString(leftExps.get(i));

				if (!identiferName.equals("_") && !identiferName.equals(".") && !identiferName.equals("")) {

					String rightIdentifierName = ForwardSlicerUtil.convertExpressionToString(rightExps.get(i));
					
					if(matchInitialStatementCriteria(leftExps.get(i), rightExps.get(i)))
						continue;
				
					if (SliceCriteriaAnalysis.isExpressionModified(rightExps.get(i))
							
							|| SliceCriteriaAnalysis.isExpressionImpacted(rightExps.get(i))) {

						if (rightExps.get(i).getKind() == ExpressionKind.METHODCALL) {
							String mt2 = NameResolver.resolveImport(rightIdentifierName, leftExps.get(i).getId(),
									rightExps.get(i).getId());
							if (!mt2.equals("")) {
								SymbolTable.addToCriteria(identiferName, leftExps.get(i).getId());

								if (Status.DEBUG && ForwardSlicerUtil.isDebugBitSet(Status.DEBUG_CRITERIA_BIT))
									System.out.println("Adding in slice criteria, Scope: " + scope + ", Variable:"
											+ identiferName + ",Location: " + leftExps.get(i).getId());
							}
						} else {
							SymbolTable.addToCriteria(identiferName, leftExps.get(i).getId());

							if (Status.DEBUG && ForwardSlicerUtil.isDebugBitSet(Status.DEBUG_CRITERIA_BIT))
								System.out.println("Adding in slice criteria, Scope: " + scope + ", Variable:"
										+ identiferName + ",Location: " + leftExps.get(i).getId());

						}

					}
					

				}
			}
		} else if (rightExps.size() == 1 && rightExps.get(0).getKind() == ExpressionKind.METHODCALL) {
			
//			if(matchInitialStatementCriteria(leftExps.get(i), rightExps.get(0)))
//				continue;
			
			String rightIdentifierName = ForwardSlicerUtil.convertExpressionToString(rightExps.get(0));

			if ( SliceCriteriaAnalysis.isExpressionModified(rightExps.get(0))
					|| SliceCriteriaAnalysis.isExpressionImpacted(rightExps.get(0))) {

				String mt2 = NameResolver.resolveImport(rightIdentifierName, leftExps.get(0).getId(),
						rightExps.get(0).getId());
				if (!mt2.equals("")) {
					for (Expression ex : leftExps) {
						String identiferName = ForwardSlicerUtil.convertExpressionToString(ex);

						if (!identiferName.equals("_") && !identiferName.equals(".") && !identiferName.equals("")) {

							SymbolTable.addToCriteria(identiferName, ex.getId());

							if (Status.DEBUG && ForwardSlicerUtil.isDebugBitSet(Status.DEBUG_CRITERIA_BIT))
								System.out.println("Adding in slice criteria, Scope: " + scope + ", Variable:"
										+ identiferName + ",Location: " + ex.getId());
						}
					}
				}

			}
		}
	}

	public static void addForNameResolution(HashMap<String, Integer> leftIdentifiers, List<Expression> rightExps) {
		List<Expression> left = new ArrayList<Expression>();

		for (Map.Entry<String, Integer> entry : leftIdentifiers.entrySet()) {
			String identiferName = entry.getKey();
			Integer location = entry.getValue();
			Expression.Builder ex = Expression.newBuilder();
			ex.setVariable(identiferName);
			ex.setId(location);
			ex.setKind(ExpressionKind.VARACCESS);
			left.add(ex.build());
		}
		addForNameResolution(left, rightExps);

	}

	public static void addForNameResolution(List<Expression> leftExps, List<Expression> rightExps) {
		if (leftExps.size() == rightExps.size()) {
			for (int i = 0; i < rightExps.size(); i++) {
				if (rightExps.get(i).getKind() != ExpressionKind.METHODCALL
						&& rightExps.get(i).getKind() != ExpressionKind.VARACCESS
						&& rightExps.get(i).getKind() != ExpressionKind.ARRAYACCESS)
					continue;

				String identiferName = ForwardSlicerUtil.convertExpressionToString(leftExps.get(i));
				if (!identiferName.equals("_") && !identiferName.equals(".") && !identiferName.equals("")) {
					String rightIdentifierName = ForwardSlicerUtil.convertExpressionToString(rightExps.get(i));

					String mt2 = NameResolver.resolveImport(rightIdentifierName, leftExps.get(i).getId(),
							rightExps.get(i).getId());
					if (mt2.equals("")) {
						mt2 = AcrossInVisitor.resolveMethodNameForJump(rightIdentifierName, leftExps.get(i).getId(),
								rightExps.get(i).getId());
					}
					if (!mt2.equals("")) {
						SymbolTable.addToAliasSet(leftExps.get(i).getId(), mt2);
						if (Status.DEBUG && ForwardSlicerUtil.isDebugBitSet(Status.DEBUG_ALIAS_BIT))
							System.out.println("Mapping for alias: " + identiferName + " ==> " + mt2);
					}
				}
			}
		}
	}
	
	public static boolean matchInitialStatementCriteria(Expression left, Expression right)
	{
		if(Status.CRITERIA_MODE!=InitialSliceCriteriaMode.STATEMENT)
		{
			return false;
		}
		
		String rightIdentifierName = ForwardSlicerUtil.convertExpressionToString(right);
		String identiferName = ForwardSlicerUtil.convertExpressionToString(left);
		String scope = Status.getCurrentScope();
		
		for(int i=0;i<Status.statementInitialCriteriaPattern.length;i++)
		{
			boolean doAdd=false;
			if(Status.statementInitialCriteriaPattern[i][0].equals("_") || 
					identiferName.matches(Status.statementInitialCriteriaPattern[i][0]))
			{
				if(Status.statementInitialCriteriaPattern[i][1].equals("_"))
					doAdd=true;
				if(right.getKind()==ExpressionKind.METHODCALL)
				{
					String mt2 = NameResolver.resolveImport(rightIdentifierName, left.getId(),
							right.getId());
					if (!mt2.equals("") && mt2.matches(Status.statementInitialCriteriaPattern[i][1]))
					{
						doAdd=true;
					}
				}
				else if(rightIdentifierName.matches(Status.statementInitialCriteriaPattern[i][1]))
					doAdd=true;
				
				if (doAdd)
				{
					SymbolTable.addToCriteria(identiferName, left.getId());
					
					if (Status.DEBUG && ForwardSlicerUtil.isDebugBitSet(Status.DEBUG_CRITERIA_BIT))
						System.out.println("Adding in slice criteria, Scope: " + scope + ", Variable:"
								+ identiferName + ",Location: " + left.getId());
					return true;
				}
			}
		}
		
		return false;
	}
}
