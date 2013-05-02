package boa.compiler.transforms;

import boa.compiler.ast.Identifier;
import boa.compiler.ast.Node;
import boa.compiler.ast.Selector;
import boa.compiler.ast.statements.VarDeclStatement;
import boa.compiler.visitors.AbstractVisitorNoArg;
import boa.types.BoaTable;

/**
 * Finds and renames all variables in the tree, including their declarations
 * and uses.  Renames are done by adding a specified prefix to the front of
 * the names.
 * 
 * @author rdyer
 */
public class VariableRenameTransformer extends AbstractVisitorNoArg {
	private String prefix = "";

	/**
	 * Starts a variable renaming transformation with a given prefix.
	 * 
	 * @param n the node to start transform at
	 * @param prefix the prefix to add to the start of names
	 */
	public void start(Node n, String prefix) {
		this.prefix = prefix + "_";
		start(n);
	}

	@Override
	public void visit(VarDeclStatement n) {
		if (n.type instanceof BoaTable)
			return;

		final String newToken = prefix + n.getId().getToken();
		n.env.set(newToken, n.env.get(n.getId().getToken()));

		super.visit(n);
	}

	@Override
	public void visit(Selector n) {
		// do nothing, we dont want to rename the selector's identifier
	}

	/** {@inheritDoc} */
	@Override
	public void visit(Identifier n) {
		final String id = n.getToken();

		if (n.env.hasType(id) || n.env.hasGlobal(id) || n.env.hasFunction(id) || n.type instanceof BoaTable)
			return;

		n.setToken(prefix + id);
	}
}
