package boa.compiler.ast.statements;

import java.util.ArrayList;
import java.util.List;

import boa.compiler.ast.Node;
import boa.compiler.ast.expressions.Expression;
import boa.compiler.visitors.AbstractVisitor;
import boa.compiler.visitors.AbstractVisitorNoArg;

/**
 * 
 * @author rdyer
 */
public class SwitchCase extends Statement {
	protected boolean isDefault;
	protected final List<Expression> cases = new ArrayList<Expression>();
	protected final Block body;

	public boolean isDefault() {
		return isDefault;
	}

	public List<Expression> getCases() {
		return cases;
	}

	public int getCasesSize() {
		return cases.size();
	}

	public Expression getCase(final int index) {
		return cases.get(index);
	}

	public void addCase(final Expression e) {
		e.setParent(this);
		cases.add(e);
	}

	public Block getBody() {
		return body;
	}

	public SwitchCase(final boolean isDefault, final Block body) {
		this(isDefault, body, null);
	}

	public SwitchCase(final boolean isDefault, final Block body, final List<Expression> cases) {
		if (body != null)
			body.setParent(this);
		this.isDefault = isDefault;
		this.body = body;
		if (cases != null)
			for (final Expression e : cases) {
				e.setParent(this);
				this.cases.add(e);
			}
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

	public SwitchCase clone() {
		final SwitchCase sc = new SwitchCase(isDefault, body.clone());
		for (final Expression e : cases)
			sc.addCase(e.clone());
		copyFieldsTo(sc);
		return sc;
	}
}
