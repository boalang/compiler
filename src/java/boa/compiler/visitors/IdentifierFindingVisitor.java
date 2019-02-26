/*
 * Copyright 2017, Anthony Urso, Hridesh Rajan, Robert Dyer, Ramanathan Ramu,
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

import java.util.HashSet;
import java.util.Set;

import boa.compiler.ast.Identifier;

/**
 *
 * @author rdyer
 */
public class IdentifierFindingVisitor extends AbstractVisitorNoArgNoRet {
	protected final Set<String> names = new HashSet<String>();

	public Set<String> getNames() {
		return names;
	}

	/** {@inheritDoc} */
	@Override
	protected void initialize() {
		names.clear();
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Identifier n) {
		names.add(n.getToken());
	}
}
