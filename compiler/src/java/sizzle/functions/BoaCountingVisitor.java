package sizzle.functions;

import sizzle.runtime.BoaAbstractVisitor;

/**
 * Boa AST visitor that aggregates using a counter.
 * 
 * @author rdyer
 */
public class BoaCountingVisitor extends BoaAbstractVisitor {
	public long count;

	@Override
	public BoaAbstractVisitor initialize() {
		count = 0;
		return super.initialize();
	}
}
