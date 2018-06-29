/*
 * Copyright 2017, Anthony Urso, Hridesh Rajan, Robert Dyer, 
 *                 Iowa State University of Science and Technology
 *                 and Bowling Green State University
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
package boa.compiler.visitors;

import boa.compiler.ast.Call;
import boa.compiler.ast.Factor;
import boa.compiler.ast.Node;

/**
 * Finds if the expression is a Call.
 * 
 * @author rdyer
 */
public class CallFindingVisitor extends AbstractVisitorNoArgNoRet {
	protected boolean isCall;

	public boolean isCall() {
		return isCall;
	}

	/** {@inheritDoc} */
	@Override
	public void initialize() {
		super.initialize();
		isCall = false;
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Factor n) {
		for (final Node node : n.getOps()) {
			isCall = false;
			node.accept(this);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Call n) {
		isCall = true;
	}
}
