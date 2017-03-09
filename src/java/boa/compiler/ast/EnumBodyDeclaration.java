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
package boa.compiler.ast;

import boa.compiler.ast.expressions.Expression;
import boa.compiler.ast.types.AbstractType;
import boa.compiler.visitors.AbstractVisitor;
import boa.compiler.visitors.AbstractVisitorNoArg;
import boa.compiler.visitors.AbstractVisitorNoReturn;

/**
 *
 * @author ankuraga
 */
public class EnumBodyDeclaration extends AbstractType {
	protected Identifier id;
	protected Expression exp;

	public boolean hasIdentifier() {
		return id != null;
	}

	public Identifier getIdentifier() {
		return id;
	}
	
	public Expression getExp() {
		return exp;
	}
	
	public EnumBodyDeclaration(final Identifier id, final Expression exp) {
		if (id != null)
			id.setParent(this);
		if (exp != null)
			exp.setParent(this);
		this.id = id;
		this.exp = exp;
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

	@Override
	public void replaceExpression(final Expression oldExp, final Expression newExp) {
		if (oldExp == exp) {
			newExp.setParent(this);
			exp = newExp;
		}
	}

	public EnumBodyDeclaration clone() {
		final EnumBodyDeclaration c = new EnumBodyDeclaration(id.clone(), exp.clone());
		copyFieldsTo(c);
		return c;
	}
}
