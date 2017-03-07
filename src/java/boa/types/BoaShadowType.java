/*
 * Copyright 2017, Hridesh Rajan, Robert Dyer, Kaushik Nimmala
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
package boa.types;

import boa.compiler.ast.Node;
import boa.compiler.SymbolTable;

/**
 * A shadow type.
 * 
 * @author rdyer
 * @author kaushin
 */
public abstract class BoaShadowType extends BoaTuple {
	public String getDeclarationIdentifierEraser;
	public BoaTuple getDeclarationSymbolTableEraser;

	/**
	 * Construct a {@link BoaShadowType}.
	 */
	public BoaShadowType(final String id, final BoaTuple t) {
		this.getDeclarationIdentifierEraser = id;
		this.getDeclarationSymbolTableEraser = t;
	}

	protected void addShadow(final String name, final BoaType t) {
		names.put(name, members.size());
		members.add(t);
	}

	public abstract Node lookupCodegen(final String name, final String nodeId, final SymbolTable env);
}
