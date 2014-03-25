package boa.compiler.ast.statements;

import boa.compiler.ast.Identifier;
import boa.compiler.ast.Node;
import boa.compiler.ast.expressions.Expression;
import boa.compiler.ast.types.AbstractType;
import boa.compiler.visitors.AbstractVisitor;
import boa.compiler.visitors.AbstractVisitorNoArg;
import boa.parser.Token;

/**
 * 
 * @author rdyer
 */
public class VarDeclStatement extends Statement {
	protected boolean isStatic;
	protected Identifier identifier;
	protected AbstractType t;
	protected Expression initializer;

	public boolean isStatic() {
		return isStatic;
	}

	public void setStatic(final boolean isStatic) {
		this.isStatic = isStatic;
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

	public void setType(final AbstractType t) {
		t.setParent(this);
		this.t = t;
	}

	public boolean hasInitializer() {
		return initializer != null;
	}

	public Expression getInitializer() {
		return initializer;
	}

	public void setInitializer(final Expression initializer) {
		initializer.setParent(this);
		this.initializer = initializer;
	}

	public VarDeclStatement (final Identifier identifier) {
		this(false, identifier, null, null);
	}

	public VarDeclStatement (final Identifier identifier, final AbstractType t) {
		this(false, identifier, t, null);
	}

	public VarDeclStatement (final Identifier identifier, final Expression initializer) {
		this(false, identifier, null, initializer);
	}

	public VarDeclStatement (final Identifier identifier, final AbstractType t, final Expression initializer) {
		this(false, identifier, t, initializer);
	}

	public VarDeclStatement (final boolean isStatic, final Identifier identifier) {
		this(isStatic, identifier, null, null);
	}

	public VarDeclStatement (final boolean isStatic, final Identifier identifier, final AbstractType t) {
		this(isStatic, identifier, t, null);
	}

	public VarDeclStatement (final boolean isStatic, final Identifier identifier, final Expression initializer) {
		this(isStatic, identifier, null, initializer);
	}

	public VarDeclStatement (final boolean isStatic, final Identifier identifier, final AbstractType t, final Expression initializer) {
		if (identifier != null)
			identifier.setParent(this);
		if (t != null)
			t.setParent(this);
		if (initializer != null)
			initializer.setParent(this);
		this.isStatic = isStatic;
		this.identifier = identifier;
		this.t = t;
		this.initializer = initializer;
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

	public VarDeclStatement clone() {
		final VarDeclStatement v = new VarDeclStatement(isStatic, identifier.clone());
		if (hasType())
			v.t = t.clone();
		if (hasInitializer())
			v.initializer = initializer.clone();
		copyFieldsTo(v);
		return v;
	}

	public VarDeclStatement setStart(final Token first) {
		return (VarDeclStatement)setPositions(first.beginLine, first.beginColumn, endLine, endColumn);
	}

	public VarDeclStatement setEnd(final Token last) {
		return (VarDeclStatement)setPositions(beginLine, beginColumn, last.endLine, last.endColumn);
	}

	public VarDeclStatement setPositions(final Node first, final Node middle, final Node last) {
		if (last != null)
			return (VarDeclStatement)setPositions(first.beginLine, first.beginColumn, last.endLine, last.endColumn);
		if (middle != null)
			return (VarDeclStatement)setPositions(first.beginLine, first.beginColumn, middle.endLine, middle.endColumn);
		return (VarDeclStatement)setPositions(first.beginLine, first.beginColumn, first.endLine, first.endColumn);
	}
}
