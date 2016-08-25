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

import java.util.ArrayList;
import java.util.List;

import boa.compiler.ast.expressions.VisitorExpression;
import boa.compiler.ast.statements.VisitStatement;

/**
 * Classifies each {@link VisitStatement} into groups.  Contains groups
 * for before/after visitors and default visitors (if any).
 * 
 * @author rdyer
 */
public class VisitClassifier extends AbstractVisitorNoArg {
	protected VisitStatement defaultBeforeVisit;
	protected VisitStatement defaultAfterVisit;
	protected List<VisitStatement> befores = new ArrayList<VisitStatement>();
	protected List<VisitStatement> afters = new ArrayList<VisitStatement>();

	public boolean hasDefaultBefore() {
		return defaultBeforeVisit != null;
	}

	public VisitStatement getDefaultBefore() {
		return defaultBeforeVisit;
	}

	public boolean hasDefaultAfter() {
		return defaultAfterVisit != null;
	}

	public VisitStatement getDefaultAfter() {
		return defaultAfterVisit;
	}

	public List<VisitStatement> getAfters() {
		return afters;
	}

	public List<VisitStatement> getBefores() {
		return befores;
	}

	/** @{inheritDoc} */
	@Override
	protected void initialize() {
		defaultBeforeVisit = null;
		defaultAfterVisit = null;
		befores.clear();
		afters.clear();
	}

	/** @{inheritDoc} */
	@Override
	public void visit(final VisitorExpression n) {
		// dont nest
	}

	/** @{inheritDoc} */
	@Override
	public void visit(VisitStatement n) {
		if (n.isBefore()) {
			if (n.hasWildcard())
				defaultBeforeVisit = n;
			befores.add(n);
		} else {
			if (n.hasWildcard())
				defaultAfterVisit = n;
				afters.add(n);
		}
	}
}
