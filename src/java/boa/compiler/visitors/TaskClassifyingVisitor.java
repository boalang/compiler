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
package boa.compiler.visitors;

import java.util.HashSet;
import java.util.Set;

import boa.compiler.ast.Call;
import boa.compiler.ast.Factor;
import boa.compiler.ast.Identifier;
import boa.compiler.ast.expressions.VisitorExpression;
import boa.compiler.ast.statements.VisitStatement;

import boa.types.BoaType;
import boa.types.proto.ASTRootProtoTuple;

/**
 * Analyze the code to see if it is simple or complex.
 * 
 * @author rdyer
 */
public class TaskClassifyingVisitor extends AbstractVisitorNoArg {
	protected final static Set<Class<? extends BoaType>> astTypes = new HashSet<Class<? extends BoaType>>();

	static {
		astTypes.addAll(new ASTRootProtoTuple().reachableTypes());
	}

	protected final Set<Class<? extends BoaType>> types = new HashSet<Class<? extends BoaType>>();

	private boolean complex = false;

	public boolean isComplex() {
		return complex;
	}

	/** {@inheritDoc} */
	@Override
	protected void initialize() {
		complex = false;
		types.clear();
	}

	/** {@inheritDoc} */
	@Override
	public void visit(Factor n) {
		if (n.getOperand() instanceof Identifier) {
			final String id = ((Identifier)n.getOperand()).getToken();
			if ("getast".equals(id) || "getcomments".equals(id))
				if (n.getOpsSize() > 0 && n.getOp(0) instanceof Call) {
					complex = true;
					return;
				}
		}

		super.visit(n);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final VisitStatement n) {
		super.visit(n);

		if (n.hasWildcard())
			complex = true;
		else if (n.hasComponent())
			types.add(n.getComponent().getType().type.getClass());
		else
			for (final Identifier id : n.getIdList())
				types.add(id.type.getClass());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final VisitorExpression n) {
		super.visit(n);

		types.retainAll(astTypes);
		if (!types.isEmpty())
			complex = true;
	}
}
