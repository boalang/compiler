package boa.compiler.ast.statements;

import boa.compiler.ast.Node;
import boa.compiler.ast.Identifier;
import boa.compiler.ast.types.AbstractType;
import boa.compiler.visitors.AbstractVisitor;
import boa.compiler.visitors.AbstractVisitorNoArg;

/**
 * 
 * @author rdyer
 */
public class VarDeclStatement extends Statement {
	protected boolean bStatic;
	protected Identifier identifier;
	protected AbstractType t;
	protected Node initializer;

	public boolean isStatic() {
		return bStatic;
	}

	public Identifier getId() {
		return identifier;
	}

	public boolean hasType() {
		return t != null;
	}

	public AbstractType getType() {
		return t;
	}

	public boolean hasInitializer() {
		return initializer != null;
	}

	public Node getInitializer() {
		return initializer;
	}

	public VarDeclStatement (final Identifier identifier) {
		this(false, identifier, null, null);
	}

	public VarDeclStatement (final Identifier identifier, final AbstractType t) {
		this(false, identifier, t, null);
	}

	public VarDeclStatement (final Identifier identifier, final Node initializer) {
		this(false, identifier, null, initializer);
	}

	public VarDeclStatement (final Identifier identifier, final AbstractType t, final Node initializer) {
		this(false, identifier, t, initializer);
	}

	public VarDeclStatement (final boolean bStatic, final Identifier identifier) {
		this(bStatic, identifier, null, null);
	}

	public VarDeclStatement (final boolean bStatic, final Identifier identifier, final AbstractType t) {
		this(bStatic, identifier, t, null);
	}

	public VarDeclStatement (final boolean bStatic, final Identifier identifier, final Node initializer) {
		this(bStatic, identifier, null, initializer);
	}

	public VarDeclStatement (final boolean bStatic, final Identifier identifier, final AbstractType t, final Node initializer) {
		identifier.setParent(this);
		if (t != null)
			t.setParent(this);
		if (initializer != null)
			initializer.setParent(this);
		this.bStatic = bStatic;
		this.identifier = identifier;
		this.t = t;
		this.initializer = initializer;
	}

	/** {@inheritDoc} */
	@Override
	public <A> void accept(AbstractVisitor<A> v, A arg) {
		v.visit(this, arg);
	}

	/** {@inheritDoc} */
	@Override
	public void accept(AbstractVisitorNoArg v) {
		v.visit(this);
	}
}
