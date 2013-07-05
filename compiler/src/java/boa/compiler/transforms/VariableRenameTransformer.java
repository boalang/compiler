package boa.compiler.transforms;

import boa.compiler.ast.Identifier;
import boa.compiler.ast.Node;
import boa.compiler.ast.Selector;
import boa.compiler.ast.statements.VarDeclStatement;
import boa.compiler.ast.statements.VisitStatement;
import boa.compiler.visitors.AbstractVisitorNoArg;

/**
 * Finds and renames all variables in the tree, including their declarations
 * and uses.  Renames are done by adding a specified prefix to the front of
 * the names.
 * 
 * @author rdyer
 */
public class VariableRenameTransformer extends AbstractVisitorNoArg {
	protected String prefix = "_";
	protected final String nodeName = "_n";

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

	protected String argName;

	/** {@inheritDoc} */
	@Override
	protected void initialize() {
		argName = null;
	}

	/** {@inheritDoc} */
	@Override
	public void visit(VisitStatement n) {
		// special case variable renaming for visit statements that name
		// their argument - this allows merging two visit statements from
		// different programs together, ensuring they use the same arg name
		if (n.hasComponent()) {
			argName = n.getComponent().getIdentifier().getToken();
			n.getComponent().accept(this);
			n.getBody().accept(this);
			argName = "";
			return;
		}
		super.visit(n);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(VarDeclStatement n) {
		final String newToken;
		if (!n.getId().getToken().equals(argName))
			newToken = prefix + n.getId().getToken();
		else
			newToken = nodeName;
		n.env.set(newToken, n.env.get(n.getId().getToken()));

		n.getId().accept(this);
		if (n.hasInitializer())
			n.getInitializer().accept(this);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(Selector n) {
		// do nothing, we dont want to rename the selector's identifier
	}

	/** {@inheritDoc} */
	@Override
	public void visit(Identifier n) {
		final String id = n.getToken();

		if (n.env.hasType(id) || n.env.hasGlobal(id) || n.env.hasGlobalFunction(id))
			return;

		if (!id.equals(argName))
			n.setToken(prefix + id);
		else
			n.setToken(nodeName);
	}
}
