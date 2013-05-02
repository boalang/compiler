package boa.compiler.visitors;

import java.util.HashSet;
import java.util.Set;

import boa.compiler.ast.Identifier;

/**
 * 
 * @author rdyer
 */
public class IdentifierFindingVisitor extends AbstractVisitorNoArg {
	private final Set<String> names = new HashSet<String>();

	public Set<String> getNames() {
		return names;
	}

	/** {@inheritDoc} */
	@Override
	protected void initialize() {
		names.clear();
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Identifier n) {
		names.add(n.getToken());
	}
}
