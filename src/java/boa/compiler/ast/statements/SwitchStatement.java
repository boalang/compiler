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

import java.util.ArrayList;
import java.util.List;

import boa.compiler.ast.expressions.Expression;
import boa.compiler.visitors.AbstractVisitor;
import boa.compiler.visitors.AbstractVisitorNoArg;
import boa.compiler.visitors.AbstractVisitorNoReturn;

/**
 * 
 * @author rdyer
 * @author hridesh
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

	public void setDefault(final SwitchCase dfault) {
		dfault.setParent(this);
		this.dfault = dfault;
	}

	public SwitchStatement(final Expression condition) {
		this(condition, null);
	}

	public SwitchStatement(final Expression condition, final SwitchCase dfault) {
		if (condition != null)
			condition.setParent(this);
		if (dfault != null)
			dfault.setParent(this);
		this.condition = condition;
		this.dfault = dfault;
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

	public SwitchStatement clone() {
		final SwitchStatement sw = new SwitchStatement(condition.clone(), dfault.clone());
		for (final SwitchCase c : cases)
			sw.addCase(c.clone());
		copyFieldsTo(sw);
		return sw;
	}
}
