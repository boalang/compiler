/*
 * Copyright 2016, Hridesh Rajan, Robert Dyer, Neha Bhide
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

import java.util.HashSet;
import java.util.Set;
import java.util.*;

import boa.compiler.ast.Call;
import boa.compiler.ast.Factor;
import boa.compiler.ast.Identifier;
import boa.compiler.ast.Program;
import boa.compiler.ast.expressions.VisitorExpression;
import boa.compiler.ast.statements.StopStatement;
import boa.compiler.visitors.AbstractVisitorNoArg;
import boa.types.BoaScalar;

/**
 * Converts use of current(T) inherited attributes in visitors into stack variables.
 *
 * General algorithm:
 *
 * 1) Find each instance of VisitorExpression, then for each:
 *    a) Find all instances of "current(T)" in the visitor
 *    b) Collect set of all unique types T found in 1a
 *    c) For each type T in the set from 1b:
 *       i)   Add a variable 's_T_#' of type 'stack of T' at the top-most scope of the AST
 *       ii)  Where-ever we encounter 'current(T)', replace with code for 's_T_#.peek()'
 *       iii) Add/Update the before clause for T in the visitor
 *            a) If the visitor has a 'before T' clause, add 's_t_#.push(node)' as the first statement
 *            b) Otherwise, add a 'before T' clause with a 's_t_#.push(node)'
 *       iv)  Add/Update the after clause for T in the visitor
 *            a) If the visitor has a 'after T' clause, add 's_t_#.pop()' as the first statement
 *            b) Otherwise, add a 'after T' clause with a 's_t_#.pop()'
 *
 * @author rdyer
 * @author nbhide
 */
public class InheritedAttributeTransformer extends AbstractVisitorNoArg {
	/** {@inheritDoc} */
	private int stackCounter = 1;
	private final String stackPrefix = "_s_"; 
	private class FindVisitorExpressions extends AbstractVisitorNoArg {
		
		protected final List<VisitorExpression> visitorList = new ArrayList<VisitorExpression>();

		/**
		 * Creates a list of all the Visitors in the Boa AST 
		 *
		 */
		
		@Override
		protected void initialize() {
			visitorList.clear();
			super.initialize();
		}
		public List<VisitorExpression> getVisitors() {
			return visitorList;
		}

		/** @{inheritDoc} */
		@Override
		public void visit(final VisitorExpression n) {
			visitorList.add(n);
			super.visit(n);
		}
	}
		
	public class FindCurrentForVisitors extends AbstractVisitorNoArg{
		protected final Set<BoaScalar> listCurrent = new HashSet<BoaScalar>();
		protected VisitorExpression e = null;
		protected boolean flagMyVE = false;
		
		FindCurrentForVisitors(VisitorExpression current){
			e = current;
		}
		
		@Override
		protected void initialize() {
			listCurrent.clear();
			super.initialize();
		}
				
		public Set<BoaScalar> getCurrentTypes(){
			return listCurrent;
		}
		
		/** @{inheritDoc} */
		@Override
		public void visit(final VisitorExpression n){
			if(e.equals(n))
				flagMyVE = true;
		}
		
		
		/** @{inheritDoc} */
		@Override
		public void visit(final Factor n){
			if(flagMyVE){
				if(n.getOpsSize()==1){
					if (n.getOperand() instanceof Identifier) {
						final Identifier id = (Identifier)n.getOperand();
						if (id.getToken().equals("current")) {
							final Call c = (Call)n.getOp(0);
							if (c.getArgsSize() == 1) {
								final Identifier idType = (Identifier)c.getArg(0).getLhs().getLhs().getLhs().getLhs().getLhs().getOperand();
								// listCurrent.add(idType.getToken()); What do I add to set which is of type BoaScalar
							}
						}
					}
				}
			}
		}
	}
	
	public class transformASTAlgorithm extends AbstractVisitorNoArg{
		@Override
		public void visit(final Program n) {
			
		}
	} 
	@Override
	public void visit(final Program n) {
		
		FindVisitorExpressions visitorsList = new FindVisitorExpressions();
		visitorsList.start(n);
		
		for(VisitorExpression e: visitorsList.getVisitors()){
			
			FindCurrentForVisitors currentSet = new FindCurrentForVisitors(e);
			currentSet.start(n);
			for(BoaScalar b: currentSet.getCurrentTypes()){
				// String currentStack = stackPrefix + b.  ? What do I add here ?
			}
			
		}
		
	}
}
