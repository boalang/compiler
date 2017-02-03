/*
 * Copyright 2014, Hridesh Rajan, Robert Dyer, 
 *                 and Iowa State University of Science and Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package boa.compiler.ast.statements;

import boa.compiler.ast.Identifier;
import boa.compiler.ast.expressions.Expression;
import boa.compiler.ast.types.AbstractType;
import boa.compiler.visitors.AbstractVisitor;
import boa.compiler.visitors.AbstractVisitorNoArg;
import boa.compiler.visitors.AbstractVisitorNoReturn;

/**
 * 
 * @author rdyer
 * @author hridesh
 */
public class VarDeclStatement extends Statement {
	protected boolean isStatic;
	protected Identifier identifier;
	protected AbstractType typeNode;
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
		return typeNode != null;
	}

	public AbstractType getType() {
		return typeNode;
	}

	public void setType(final AbstractType typeNode) {
		typeNode.setParent(this);
		this.typeNode = typeNode;
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

	public VarDeclStatement(final Identifier identifier) {
		this(false, identifier, null, null);
	}

	public VarDeclStatement(final Identifier identifier, final AbstractType typeNode) {
		this(false, identifier, typeNode, null);
	}

	public VarDeclStatement(final Identifier identifier, final Expression initializer) {
		this(false, identifier, null, initializer);
	}

	public VarDeclStatement(final Identifier identifier, final AbstractType typeNode, final Expression initializer) {
		this(false, identifier, typeNode, initializer);
	}

	public VarDeclStatement(final boolean isStatic, final Identifier identifier) {
		this(isStatic, identifier, null, null);
	}

	public VarDeclStatement(final boolean isStatic, final Identifier identifier, final AbstractType typeNode) {
		this(isStatic, identifier, typeNode, null);
	}

	public VarDeclStatement(final boolean isStatic, final Identifier identifier, final Expression initializer) {
		this(isStatic, identifier, null, initializer);
	}

	public VarDeclStatement(final boolean isStatic, final Identifier identifier, final AbstractType typeNode, final Expression initializer) {
		if (identifier != null)
			identifier.setParent(this);
		if (typeNode != null)
			typeNode.setParent(this);
		if (initializer != null)
			initializer.setParent(this);
		this.isStatic = isStatic;
		this.identifier = identifier;
		this.typeNode = typeNode;
		this.initializer = initializer;
	}

	/** {@inheritDoc} */
	@Override
	public <T,A> T accept(final AbstractVisitor<T,A> v, A arg) {
		return v.visit(this, arg);
	}

	/** {@inheritDoc} */
	@Override
	public <A> void accept(final AbstractVisitorNoReturn<A> v, A arg) {
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
			v.typeNode = typeNode.clone();
		if (hasInitializer())
			v.initializer = initializer.clone();
		copyFieldsTo(v);
		return v;
	}
}
