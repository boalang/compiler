package boa.graphs.slicers.python;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import boa.functions.BoaGraphIntrinsics;
import boa.functions.BoaStringIntrinsics;
import boa.runtime.BoaAbstractVisitor;
import boa.types.Ast.ASTRoot;
import boa.types.Ast.Cell;
import boa.types.Ast.Declaration;
import boa.types.Ast.Expression;
import boa.types.Ast.Method;
import boa.types.Ast.Namespace;
import boa.types.Ast.Expression.ExpressionKind;
import boa.types.Ast.Statement.StatementKind;
import boa.types.Ast.Statement;
import boa.types.Ast.Type;
import boa.types.Ast.Variable;

public class SymbolTableGenerator extends BoaAbstractVisitor {

	
	@Override
	protected boolean preVisit(final ASTRoot node) throws Exception {
		final List<Namespace> namespacesList = node.getNamespacesList();		
		if(Status.BACKWARD)
			visit(namespacesList.get(1));
		else
			visit(namespacesList.get(0));
		
		return false;
	}
	
	@Override
	protected boolean preVisit(final Namespace node) throws Exception {
		Status.globalScopeNameStack.push(node.getName().replace(".", "_"));
		Status.cfgMap.put(Status.getCurrentScope(), BoaGraphIntrinsics.getcfg(node));
		Status.cfgToAstIdMapper();

		for (String imp : node.getImportsList()) {
			if (imp.matches("^\\..*") || imp.equals("")) // ignore relative imports
				continue;

			boolean flag = false;

			for (String lib : Status.moduleFilter) {
				if (imp.matches("^" + lib + ".*") || imp.matches("^from " + lib + ".*")) {
					Status.isModuleFound = true;
					flag = true;
					break;
				}
			}

			for (String lib : Status.libraryFilter) {
				if (imp.matches("^" + lib + ".*") || imp.matches("^from " + lib + ".*")) {
					flag = true;
					break;
				}
			}
			if (!flag)
				continue;

			if (imp.matches("^from .*")) {
				imp = imp.substring("from".length());
				imp = BoaStringIntrinsics.trim(imp);
				if (imp.matches("^\\..*"))
					continue;

				long v = BoaStringIntrinsics.indexOf(" as ", imp);
				if (v == -1) {
					String[] p2 = BoaStringIntrinsics.splitall(imp, " ");
					if(p2.length<2)
					{
						p2=new String[2];
						p2[0]=imp.substring(0, imp.lastIndexOf("."));
						p2[1]=imp.substring(imp.lastIndexOf(".")+1);
					}
					if (p2.length == 2) {
						Status.importMap.put(p2[1], p2[0] + "." + p2[1]);
					}
				} else {

					Status.importMap.put(BoaStringIntrinsics.substring(imp, v + 4), BoaStringIntrinsics
							.stringReplace(BoaStringIntrinsics.substring(imp, 0, v), " ", ".", true));
				}

			} else {
				long v = BoaStringIntrinsics.indexOf(" as ", imp);
				if (v != -1) {
					Status.importMap.put(BoaStringIntrinsics.substring(imp, v + 4),
							BoaStringIntrinsics.substring(imp, 0, v));
				} else {
					Status.importMap.put(imp, imp);

					v = BoaStringIntrinsics.indexOf(".", imp);
					if (v > 0) {
						Status.importMap.put(BoaStringIntrinsics.substring(imp, v),
								BoaStringIntrinsics.substring(imp, 0, v));
					}
				}
			}

		}

		if (!Status.isModuleFound) {
			postVisit(node);
			return false;
		}

//		for (String lib : Status.moduleFilter) {
//			Status.importMap.put(lib, lib);
//
//		}
//		for (String lib : Status.libraryFilter) {
//			Status.importMap.put(lib, lib);
//		}

		Status.objectNameMap.put(Status.getCurrentScope(), Status.getCurrentScope());
		return defaultPreVisit();
	}

	@Override
	protected boolean preVisit(final Declaration node) throws Exception {

		Status.globalScopeNameStack.push(node.getName().replace(".", "_"));
		Status.objectNameMap.put(Status.getCurrentScope(), Status.getCurrentScope());
		Status.cfgMap.put(Status.getCurrentScope(), BoaGraphIntrinsics.getcfg(node));
		Status.cfgToAstIdMapper();
		return defaultPreVisit();
	}

	@Override
	protected boolean preVisit(final Method node) throws Exception {
		Status.globalScopeNameStack.push(node.getName().replace(".", "_"));
		Status.objectNameMap.put(Status.getCurrentScope(), Status.getCurrentScope());

		Status.cfgMap.put(Status.getCurrentScope(), BoaGraphIntrinsics.getcfg(node));
		Status.cfgToAstIdMapper();
		for (Variable v : node.getArgumentsList()) {
			Status.cfgToAstIdVariableMapper(v, 0);
		}

		Status.astMethodMap.put(Status.getCurrentScope(), node);

		this.addToDefintions(ForwardSlicerUtil.getArgumentsMap(node));

		return defaultPreVisit();
	}

	@Override
	protected boolean preVisit(final Statement node) throws Exception {
		if (node.getKind() == StatementKind.ASSERT)
			return false;

		if (node.getKind() == StatementKind.FOREACH || node.getKind() == StatementKind.WITH) {
			this.addToDefintions(ForwardSlicerUtil.getIdentiferNames(node));
		}

		return defaultPreVisit();
	}

	@Override
	protected boolean preVisit(final Expression node) throws Exception {

		if (ForwardSlicerUtil.isProperAssignKind(node) && !Status.isMethodCallScope()) {
			HashMap<String, Integer> ids = ForwardSlicerUtil.getIdentiferNames(node.getExpressions(0));
			this.addToDefintions(ids);
		}

		if (ForwardSlicerUtil.isMethodCallKind(node)) {
			Status.statementScopeStack.push("call");
		}
		return defaultPreVisit();
	}

	private void addToDefintions(HashMap<String, Integer> mp) {
		for (Map.Entry<String, Integer> entry : mp.entrySet()) {
			String identiferName = entry.getKey();
			Integer location = entry.getValue();
			if (!identiferName.equals("_") && !identiferName.equals(".") && !identiferName.equals(""))
				SymbolTable.addToDefintions(identiferName, location);
		}
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

}
