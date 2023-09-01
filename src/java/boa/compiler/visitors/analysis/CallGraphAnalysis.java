/*
 * Copyright 2021, Robert Dyer
 *                 and University of Nebraska Board of Regents
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
 * See the License for the specif ic language governing permissions and
 * limitations under the License.
 */
package boa.compiler.visitors.analysis;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import boa.compiler.ast.Call;
import boa.compiler.ast.expressions.FunctionExpression;
import boa.compiler.ast.Factor;
import boa.compiler.ast.Identifier;
import boa.compiler.ast.Node;
import boa.compiler.visitors.AbstractVisitorNoArgNoRet;

/**
 * Computes a call graph for all user-defined functions.
 *
 * @author rdyer
 */
public class CallGraphAnalysis extends AbstractVisitorNoArgNoRet {
	private Map<String, Set<String>> calls = new HashMap<String, Set<String>>();
	private UDFCallFinder finder = new UDFCallFinder();

	/** {@inheritDoc} */
	@Override
	protected void initialize() {
		calls.clear();
	}

	/** {@inheritDoc} */
	@Override
	public void start(final Node n) {
		super.start(n);
		fixedpoint();
	}

	protected void fixedpoint() {
		boolean changed = true;

		while (changed) {
			changed = false;

			for (final String f : calls.keySet()) {
				final int len = calls.get(f).size();
				final Set<String> newc = new LinkedHashSet<String>();

				for (final String s : calls.get(f))
					if (calls.containsKey(s))
						newc.addAll(calls.get(s));
				calls.get(f).addAll(newc);

				if (calls.get(f).size() != len)
					changed = true;
			}
		}
	}

	public Set<String> getCalls(final String s) {
		return calls.get(s);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final FunctionExpression n) {
		super.visit(n);

		finder.start(n.getBody());
		final String name = getFunctionName(n);

		if (name != null)
			calls.put(name, new LinkedHashSet<String>(finder.getCalls()));
	}

	protected class UDFCallFinder extends AbstractVisitorNoArgNoRet {
		private Set<String> calls = new LinkedHashSet<String>();

		/** {@inheritDoc} */
		@Override
		protected void initialize() {
			calls.clear();
		}

		public Set<String> getCalls() {
			return calls;
		}

		/** {@inheritDoc} */
		@Override
		public void visit(final Factor n) {
			super.visit(n);

			if (n.getOpsSize() == 1 && n.getOp(0) instanceof Call && n.getOperand() instanceof Identifier) {
				final String id = ((Identifier)n.getOperand()).getToken();

				if (n.env.hasLocalFunction(id)) {
					calls.add(id);
				}
			}
		}

		/** {@inheritDoc} */
		@Override
		public void visit(final FunctionExpression n) {
			// don't nest
		}
	}
}
