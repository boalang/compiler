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

import boa.compiler.ast.statements.Block;
import boa.compiler.ast.Call;
import boa.compiler.ast.Comparison;
import boa.compiler.ast.Component;
import boa.compiler.ast.Conjunction;
import boa.compiler.ast.Factor;
import boa.compiler.ast.Identifier;
import boa.compiler.ast.Program;
import boa.compiler.ast.Term;
import boa.compiler.ast.expressions.Expression;
import boa.compiler.ast.expressions.SimpleExpr;
import boa.compiler.ast.expressions.VisitorExpression;
import boa.compiler.ast.literals.IntegerLiteral;
import boa.compiler.ast.statements.ExprStatement;
import boa.compiler.ast.statements.Statement;
import boa.compiler.ast.statements.StopStatement;
import boa.compiler.ast.statements.VarDeclStatement;
import boa.compiler.ast.statements.VisitStatement;
import boa.compiler.ast.types.StackType;
import boa.compiler.visitors.AbstractVisitorNoArg;
import boa.compiler.visitors.VisitClassifier;
import boa.types.BoaScalar;
import boa.types.BoaStack;

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
		protected final List<Factor> factorList = new ArrayList<Factor>();
		
		@Override
		protected void initialize() {
			listCurrent.clear();
			factorList.clear();
			super.initialize();			
		}
				
		public Set<BoaScalar> getCurrentTypes(){
			return listCurrent;
		}
		
		public List<Factor> getFactorList(){
			return factorList;
		}
		/** @{inheritDoc} */
		@Override
		public void visit(final VisitorExpression n){
			//don't nest
		}
		
		/** @{inheritDoc} */
		@Override
		public void visit(final Factor n){
			//if(n.getOpsSize()==1){
				if (n.getOperand() instanceof Identifier){
					final Identifier id = (Identifier)n.getOperand();
					//System.out.println(id.getToken());
					if (id.getToken().equals("current")){
						if(n.getOp(0) instanceof Call){
							final Call c = (Call)n.getOp(0);
							if (c.getArgsSize() == 1) {
								//final Identifier idType = (Identifier)c.getArg(0).getLhs().getLhs().getLhs().getLhs().getLhs().getOperand();
								System.out.println("Inside check current");
								listCurrent.add((BoaScalar)c.getArg(0).type);
								factorList.add(n);
								
							}
						}
					}
				}
			//}
			super.visit(n);
		}
	}
	
	
	private void replaceCurrentCall(Factor n, VarDeclStatement v){
		
			final Identifier id = (Identifier)n.getOperand();
			final Call c = (Call)n.getOp(0);
								
			final Identifier idType = (Identifier)c.getArg(0).getLhs().getLhs().getLhs().getLhs().getLhs().getOperand();
			String stackType = v.type.toString();
			System.out.println(stackType.substring(stackType.lastIndexOf(' ') + 1));
							
			id.setToken("peek");
			idType.setToken(v.getId().getToken());
			c.getArg(0).type = v.type;
			System.out.println(v.getId().getToken());
								
	}

	private VarDeclStatement generateStackNode(BoaScalar b){
		final String typeName = b.toJavaType();
		System.out.println(typeName);
		final VarDeclStatement var = new VarDeclStatement(
				new Identifier(stackPrefix + stackCounter),
				new StackType(
					new Component(
						new Identifier(typeName.substring(typeName.lastIndexOf('.') + 1))
					)
				)
			);
		stackCounter++;
		var.type = new BoaStack(b);
		return var;
	}
	
	//Generate an expression statement to push node to the stack
	private ExprStatement generatePushExpStatement(String stackName, String nodeName, VisitorExpression e){
		
		Expression e1 = new Expression(
				new Conjunction(
						new Comparison(
								new SimpleExpr(
										new Term(
												new Factor(
														new Identifier(stackName)
														)
												)
										)
								)
						)
				);

		Expression e2 = new Expression(
				new Conjunction(
						new Comparison(
								new SimpleExpr(
										new Term(
												new Factor(
														new Identifier(nodeName)
														)
												)
										)
								)
						)
				);
		
		Call c = new Call();
		c.addArg(e1);
		c.addArg(e2);
		
		e1.getLhs().getLhs().getLhs().getLhs().getLhs().env = c.env = e.env;
		//e1.getLhs().getLhs().getLhs().getLhs().getLhs().getOperand().type
		
		final ExprStatement exp = new ExprStatement(
				new Expression(
						new Conjunction(
								new Comparison(
										new SimpleExpr(
												new Term(
														new Factor(
																new Identifier("push")
																).addOp(c)
														)
													)
												)
											)
									)
							);
		
		//exp.type = new 
		return exp;
		
	}

	//Generate an expression statement to Pop node to the stack
		private ExprStatement generatePopExpStatement(String stackName, VisitorExpression e){
			
			Expression e1 = new Expression(
					new Conjunction(
							new Comparison(
									new SimpleExpr(
											new Term(
													new Factor(
															new Identifier(stackName)
															)
													)
											)
									)
							)
					);

			Call c = new Call();
			c.addArg(e1);
			e1.getLhs().getLhs().getLhs().getLhs().getLhs().env =c.env =e.env;
			
			final ExprStatement exp = new ExprStatement(
					new Expression(
							new Conjunction(
									new Comparison(
											new SimpleExpr(
													new Term(
															new Factor(
																	new Identifier("pop")
																	).addOp(c)
															)
														)
													)
												)
										)
								);
			
			//System.out.println(exp.type.toString());
			return exp;
			
		}

	@Override
	public void visit(final Program n) {
		System.out.println("I am here");
		FindVisitorExpressions visitorsList = new FindVisitorExpressions();
		visitorsList.start(n);
		System.out.println(visitorsList.getVisitors().size());
		
		for(VisitorExpression e: visitorsList.getVisitors()){
			
			FindCurrentForVisitors currentSet = new FindCurrentForVisitors();
			currentSet.start(e.getBody());
			System.out.println(currentSet.getCurrentTypes().size());
			
			//get set of all "types" on which current is called for this visitor expression
			for(BoaScalar b: currentSet.getCurrentTypes()){
				VarDeclStatement v = generateStackNode(b);
				v.env = v.getType().env = ((StackType)v.getType()).getValue().getType().env = n.env;
				v.env.set(v.getId().getToken(), v.type);
				n.getStatements().add(0, v);
				
				//replace the current call with peek(generatedStack) 
				for(Factor f: currentSet.getFactorList()){
					replaceCurrentCall(f, v);
				}
				//Create object of Visit Classifier to get Before and After Maps
				VisitClassifier getVS = new VisitClassifier();
				getVS.start(e.getBody());
				String typeToFind = b.toJavaType().substring(b.toJavaType().lastIndexOf('.') + 1);
				
				//Add/Update Before visit clause
				if(getVS.getBeforeMap().containsKey(typeToFind)){
					VisitStatement vs = getVS.getBeforeMap().get(typeToFind);
					Statement pushToStack = generatePushExpStatement(v.getId().getToken(), vs.getComponent().getIdentifier().getToken(), e); 
					vs.getBody().getStatements().add(0, pushToStack);
				}
				else if(getVS.getBeforeMap().containsKey("_")){
					
					Statement pushToStack = generatePushExpStatement(v.getId().getToken(), "node", e);
					
					Block blk = getVS.getBeforeMap().get("_").getBody().clone();
					blk.addStatement(pushToStack);
					
					VisitStatement vs = new VisitStatement(true, 
							new Component(new Identifier("node"), new Identifier(typeToFind)),blk);
					
					vs.getComponent().getType().type = b;
					
					e.getBody().getStatements().add(vs);
					((ExprStatement)pushToStack).getExpr().getLhs().getLhs().getLhs().getLhs().getLhs().env = vs.env = e.env;
				}
				else{
					
					Statement pushToStack = generatePushExpStatement(v.getId().getToken(), "node",e);
					
					VisitStatement vs = new VisitStatement(true, 
							new Component(new Identifier("node"), new Identifier(typeToFind)),
							new Block().addStatement(pushToStack)
							);
					
					vs.getComponent().getType().type = b;
					
					e.getBody().getStatements().add(vs);
					((ExprStatement)pushToStack).getExpr().getLhs().getLhs().getLhs().getLhs().getLhs().env = vs.env = e.env;
					
				}
				
				//Add/Update After visit clause
				if(getVS.getAfterMap().containsKey(typeToFind)){
					VisitStatement vs = getVS.getAfterMap().get(typeToFind);
					Statement popFromStack = generatePopExpStatement(v.getId().getToken(),e); 
					vs.getBody().getStatements().add(popFromStack);
					
				}
				else if(getVS.getAfterMap().containsKey("_")){
					
					Statement popFromStack = generatePopExpStatement(v.getId().getToken(),e);
					
					Block blk = getVS.getAfterMap().get("_").getBody().clone();
					blk.addStatement(popFromStack);
					
					VisitStatement vs = new VisitStatement(false, 
							new Component(new Identifier("node"), new Identifier(typeToFind)),blk);
					vs.getComponent().getType().type = b;
					
					e.getBody().getStatements().add(vs);
					((ExprStatement)popFromStack).getExpr().getLhs().getLhs().getLhs().getLhs().getLhs().env = vs.env = e.env;
				}
				else{
					
					Statement popFromStack = generatePopExpStatement(v.getId().getToken(),e);
					
					VisitStatement vs = new VisitStatement(false, 
							new Component(new Identifier("node"), new Identifier(typeToFind)),
							new Block().addStatement(popFromStack)
							);
					vs.getComponent().getType().type = b;
					
					e.getBody().getStatements().add(vs);
					((ExprStatement)popFromStack).getExpr().getLhs().getLhs().getLhs().getLhs().getLhs().env = vs.env = e.env;
				}
			}
			
		}
		
	}
}
