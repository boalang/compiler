package boa.compiler.visitors;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import boa.compiler.ast.Factor;
import boa.compiler.ast.Index;
import boa.compiler.ast.Node;

/**
 * 
 * @author rdyer
 */
public class IndexeeFindingVisitor extends AbstractVisitor<String> {
	private final IdentifierFindingVisitor idFinder;
	private Factor firstFactor;
	private Node lastFactor;

	private Map<Node, Node> lastFactors = new HashMap<Node, Node>();
	private final Set<Node> indexees = new HashSet<Node>();

	public IndexeeFindingVisitor(final IdentifierFindingVisitor namefinder) {
		this.idFinder = namefinder;
	}

	/** {@inheritDoc} */
	@Override
	protected void initialize(String arg) {
		lastFactors.clear();
		indexees.clear();
		firstFactor = null;
		lastFactor = null;
	}

	public Map<Node, Node> getFactors() {
		return lastFactors;
	}

	public Set<Node> getIndexees() {
		return indexees;
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Factor n, final String arg) {
		firstFactor = n;
		n.getOperand().accept(this, arg);
		lastFactor = n.getOperand();

		for (final Node f : n.getOps()) {
			f.accept(this, arg);
			lastFactor = f;
		}
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Index n, final String arg) {
		this.idFinder.start(n);

		if (this.idFinder.getNames().contains(arg)) {
			indexees.add(firstFactor);
			lastFactors.put(firstFactor, lastFactor);
		}
	}
}
