/*
 * Copyright 2018, Robert Dyer, Che Shian Hung, and Bowling Green State University
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
import boa.compiler.ast.Component;
import boa.compiler.ast.Node;
import boa.compiler.ast.expressions.Expression;
import boa.compiler.visitors.AbstractVisitor;
import boa.compiler.visitors.AbstractVisitorNoArgNoRet;
import boa.compiler.visitors.AbstractVisitorNoReturn;

/**
 * 
 * @author rdyer
 * @author hungc
 */
public class ForeachViewStatement extends Statement {
	protected Identifier tid;
	protected Identifier rid;
	protected Block body;

	public Identifier getTableIdentifier() {
		return tid;
	}

	public Identifier getRowIdentifier() {
		return rid;
	}

	public Block getBody() {
		return body;
	}

	public ForeachViewStatement(final Identifier tid, final Identifier rid, final Statement s) {
		this(tid, rid, Node.ensureBlock(s));
	}

	public ForeachViewStatement(final Identifier tid, final Identifier rid, final Block body) {
		if (tid != null)
			tid.setParent(this);
		if (rid != null)
			rid.setParent(this);
		if (body != null)
			body.setParent(this);
		this.tid = tid;
		this.rid = rid;
		this.body = body;
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
	public void accept(final AbstractVisitorNoArgNoRet v) {
		v.visit(this);
	}

	public ForeachViewStatement clone() {
		final ForeachViewStatement s = new ForeachViewStatement(tid.clone(), rid.clone(), body.clone());
		copyFieldsTo(s);
		return s;
	}
}
