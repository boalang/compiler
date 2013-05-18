package boa.compiler.ast.statements;

import java.util.ArrayList;
import java.util.List;

import boa.compiler.ast.expressions.Expression;
import boa.compiler.visitors.AbstractVisitor;
import boa.compiler.visitors.AbstractVisitorNoArg;

/**
 * 
 * @author rdyer
 */
public class SwitchStatement extends Statement {
	protected Expression condition;
	protected final List<SwitchCase> cases = new ArrayList<SwitchCase>();
	protected SwitchCase dfault;

	public Expression getCondition() {
		return condition;
	}

	public List<SwitchCase> getCases() {
		return cases;
	}

	public int getCasesSize() {
		return cases.size();
	}

	public SwitchCase getCase(final int index) {
		return cases.get(index);
	}

	public void addCase(final SwitchCase c) {
		c.setParent(this);
		cases.add(c);
	}

	public SwitchCase getDefault() {
		return dfault;
	}

	public SwitchStatement(final Expression condition, final SwitchCase dfault) {
		condition.setParent(this);
		dfault.setParent(this);
		this.condition = condition;
		this.dfault = dfault;
	}

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

	public SwitchStatement clone() {
		final SwitchStatement sw = new SwitchStatement(condition.clone(), dfault.clone());
		for (final SwitchCase c : cases)
			sw.addCase(c.clone());
		copyFieldsTo(sw);
		return sw;
	}
}
