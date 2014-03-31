package boa.compiler.ast;

import boa.compiler.ast.statements.Block;
import boa.compiler.ast.statements.Statement;
import boa.compiler.visitors.AbstractVisitor;
import boa.compiler.visitors.AbstractVisitorNoArg;

/**
 * 
 * @author rdyer
 */
public class Program extends Block {
	public String jobName;

	/** {@inheritDoc} */
	@Override
	public <A> void accept(final AbstractVisitor<A> v, A arg) {
		v.visit(this, arg);
	}

	/** {@inheritDoc} */
	@Override
	public void accept(final AbstractVisitorNoArg v) {
		v.visit(this);
	}

	public Program clone() {
		final Program p = new Program();
		p.jobName = jobName;
		for (final Statement s : statements)
			p.addStatement(s.clone());
		copyFieldsTo(p);
		return p;
	}
}
