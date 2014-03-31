package boa.compiler.ast.statements;

import boa.compiler.ast.Identifier;
import boa.compiler.ast.types.AbstractType;
import boa.compiler.visitors.AbstractVisitor;
import boa.compiler.visitors.AbstractVisitorNoArg;

/**
 * 
 * @author rdyer
 */
public class TypeDecl extends Statement {
	protected Identifier identifier;
	protected AbstractType t;

	public Identifier getId() {
		return identifier;
	}

	public boolean hasType() {
		return t != null;
	}

	public AbstractType getType() {
		return t;
	}

	public TypeDecl(final Identifier identifier, final AbstractType t) {
		if (identifier != null)
			identifier.setParent(this);
		if (t != null)
			t.setParent(this);
		this.identifier = identifier;
		this.t = t;
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

	public TypeDecl clone() {
		final TypeDecl d = new TypeDecl(identifier.clone(), t.clone());
		copyFieldsTo(d);
		return d;
	}
}
