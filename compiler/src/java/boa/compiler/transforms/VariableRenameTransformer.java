package boa.compiler.transforms;

import boa.compiler.ast.Identifier;
import boa.compiler.ast.Node;
import boa.compiler.ast.Selector;
import boa.compiler.ast.statements.VarDeclStatement;
import boa.compiler.visitors.AbstractVisitorNoArg;
import boa.types.BoaTable;

public class VariableRenameTransformer extends AbstractVisitorNoArg {
	private String prefix = "";

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
