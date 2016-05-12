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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import boa.compiler.ast.expressions.VisitorExpression;
import boa.compiler.ast.Identifier;
import boa.compiler.ast.statements.VisitStatement;

/**
 * Classifies each {@link VisitStatement} into groups.  Contains groups
 * for before/after visitors and default visitors (if any).
 * 
 * @author rdyer
 */
public class VisitClassifier extends AbstractVisitorNoArg {
	protected List<VisitStatement> befores = new ArrayList<VisitStatement>();
	protected List<VisitStatement> afters = new ArrayList<VisitStatement>();
	protected Map<String,VisitStatement> beforeMap = new HashMap<String,VisitStatement>();
	protected Map<String,VisitStatement> afterMap = new HashMap<String,VisitStatement>();

	public boolean hasDefaultBefore() {
		return beforeMap.containsKey("_");
	}

	public VisitStatement getDefaultBefore() {
		return beforeMap.get("_");
	}

	public boolean hasDefaultAfter() {
		return afterMap.containsKey("_");
	}

	public VisitStatement getDefaultAfter() {
		return afterMap.get("_");
	}

	public List<VisitStatement> getAfters() {
		return afters;
	}

	public List<VisitStatement> getBefores() {
		return befores;
	}

	public Map<String,VisitStatement> getAfterMap() {
		return afterMap;
	}

	public Map<String,VisitStatement> getBeforeMap() {
		return beforeMap;
	}

	/** @{inheritDoc} */
	@Override
	protected void initialize() {
		befores.clear();
		afters.clear();
		beforeMap.clear();
		afterMap.clear();
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
				beforeMap.put("_", n);
			else if (n.hasComponent())
				beforeMap.put(((Identifier)n.getComponent().getType()).getToken(), n);
			else
				for (final Identifier id : n.getIdList())
					beforeMap.put(id.getToken(), n);
			befores.add(n);
		} else {
			if (n.hasWildcard())
				afterMap.put("_", n);
			else if (n.hasComponent())
				afterMap.put(((Identifier)n.getComponent().getType()).getToken(), n);
			else
				for (final Identifier id : n.getIdList())
					afterMap.put(id.getToken(), n);
			afters.add(n);
		}
	}
}
