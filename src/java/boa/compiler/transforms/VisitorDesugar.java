/*
 * Copyright 2016, Hridesh Rajan, Robert Dyer, 
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
package boa.compiler.transforms;

import java.util.ArrayList;
import java.util.List;

import boa.compiler.ast.Component;
import boa.compiler.ast.Identifier;
import boa.compiler.ast.expressions.VisitorExpression;
import boa.compiler.ast.statements.Block;
import boa.compiler.ast.statements.Statement;
import boa.compiler.ast.statements.VisitStatement;
import boa.compiler.visitors.AbstractVisitorNoArg;
import boa.types.BoaType;
import boa.types.proto.*;

/**
 * De-sugars visitors, by taking VisitStatements with lists and turning them
 * into multiple VisitStatements with Components and identical bodies.
 * 
 * @author rdyer
 */
public class VisitorDesugar extends AbstractVisitorNoArg {
	/** {@inheritDoc} */
	@Override
	public void visit(final VisitorExpression n) {
		final List<VisitStatement> todo = new ArrayList<VisitStatement>();
		final Block b = n.getBody();

		for (final Statement s : b.getStatements()) {
			final VisitStatement vs = (VisitStatement)s;
			if (vs.getIdListSize() > 0)
				todo.add(vs);
		}

		for (final VisitStatement vs : todo) {
			final List<Identifier> ids = vs.getIdList();

			while (!ids.isEmpty()) {
				final VisitStatement newVs = createVisit(vs, ids.remove(0));

				if (ids.isEmpty())
					b.replaceStatement(vs, newVs);
				else
					vs.insertStatementAfter(newVs);
			}
		}
	}

	private VisitStatement createVisit(final VisitStatement old, final Identifier id) {
		final VisitStatement v = new VisitStatement(old.isBefore(), new Component(new Identifier("_UNUSED"), id.clone()), old.getBody().clone());

		v.type = old.type;
		v.env = old.env;

		v.beginLine = old.beginLine;
		v.beginColumn = old.beginColumn;
		v.endLine = old.endLine;
		v.endColumn = old.endColumn;

		return v;
	}
}
