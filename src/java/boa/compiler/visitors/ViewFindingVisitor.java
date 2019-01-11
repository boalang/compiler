/*
 * Copyright 2017, Robert Dyer, Che Shian Hung,
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

import boa.compiler.ast.Table;

import java.util.List;
import java.util.ArrayList;

/**
 * Finds if the expression is a Call.
 * 
 * @author rdyer
 * @author hungc
 */
public class ViewFindingVisitor extends AbstractVisitorNoArgNoRet {

	/** {@inheritDoc} */
	@Override
	public void initialize() {
		super.initialize();
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Table n) {
		if (n.getJobNum() != null)
			System.out.println(n.getJobNum());
		else if (n.getUserName() != null)
			System.out.println(n.getUserName() + "/" + n.getViewName());
	}
}
