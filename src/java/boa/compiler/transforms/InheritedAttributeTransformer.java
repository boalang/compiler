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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import boa.compiler.ast.statements.Block;
import boa.compiler.ast.statements.ExprStatement;
import boa.compiler.ast.statements.Statement;
import boa.compiler.ast.statements.VarDeclStatement;
import boa.compiler.ast.statements.VisitStatement;
import boa.compiler.ast.types.StackType;
import boa.compiler.SymbolTable;
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
	private final static String stackPrefix = "_inhattr_";
	private static int stackCounter = 0;

	private SymbolTable env;

	/**
	 * Creates a list of all the {@link VisitorExpression}s in the Boa AST.
	 */
	private class FindVisitorExpressions extends AbstractVisitorNoArg {
		protected final List<VisitorExpression> visitors = new ArrayList<VisitorExpression>();

		/** @{inheritDoc} */
		@Override
		protected void initialize() {
			visitors.clear();
		}

		public List<VisitorExpression> getVisitors() {
			return visitors;
		}

		/** @{inheritDoc} */
		@Override
		public void visit(final VisitorExpression n) {
			visitors.add(n);
			super.visit(n);
		}
	}

	/**
	 * Generates a set of all distinct types T in calls current(T) and also a
	 * mapping from each type T found to a list of all uses in current(T).
	 */
	private class FindCurrentForVisitors extends AbstractVisitorNoArg{
		protected final Set<BoaScalar> currents = new HashSet<BoaScalar>();
		protected final Map<BoaScalar,List<Factor>> factorMap = new HashMap<BoaScalar,List<Factor>>();

		/** @{inheritDoc} */
		@Override
		protected void initialize() {
			currents.clear();
			factorMap.clear();
			super.initialize();
		}

		public Set<BoaScalar> getCurrentTypes() {
			return currents;
		}

		public Map<BoaScalar,List<Factor>> getFactorList() {
			return factorMap;
		}

		/** @{inheritDoc} */
		@Override
		public void visit(final VisitorExpression n) {
			//don't nest
		}

		/** @{inheritDoc} */
		@Override
		public void visit(final Factor n) {
			if (n.getOperand() instanceof Identifier) {
				final Identifier id = (Identifier)n.getOperand();

				if (id.getToken().equals("current") && n.getOp(0) instanceof Call) {
					final Call c = (Call)n.getOp(0);

					if (c.getArgsSize() == 1) {
						final BoaScalar t = (BoaScalar)c.getArg(0).type;
						currents.add(t);

						if (!factorMap.containsKey(t))
							factorMap.put(t, new ArrayList<Factor>());
						factorMap.get(t).add(n);
					}
				}
			}
			super.visit(n);
		}
	}

	// replaces a call to current(T) to peek(s_T_#)
	private void replaceCurrentCall(final Factor n, final VarDeclStatement v) {
		final Identifier id = (Identifier)n.getOperand();
		final Call c = (Call)n.getOp(0);
		final Identifier idType = (Identifier)c.getArg(0).getLhs().getLhs().getLhs().getLhs().getLhs().getOperand();

		id.setToken("peek");
		idType.setToken(v.getId().getToken());
		c.getArg(0).type = v.type;
	}

	// creates a stack variable
	private VarDeclStatement generateStackNode(final BoaScalar b) {
		final String typeName = b.toJavaType();
		final VarDeclStatement var = new VarDeclStatement(
				createIdentifier(stackPrefix + stackCounter),
				new StackType(
					new Component(
						createIdentifier(typeName.substring(typeName.lastIndexOf('.') + 1))
					)
				)
			);
		stackCounter++;
		var.type = new BoaStack(b);
		return var;
	}

	// generate the push call
	private ExprStatement generatePushExpStatement(final BoaScalar b, final String stackName, final String nodeName, final VisitorExpression e) {
		final Expression e1 = createIdentifierExpr(stackName, e.env);
		e1.type = new BoaStack(b);

		final Expression e2 = createIdentifierExpr(nodeName, e.env);
		e2.type = b;

		final Call c = new Call();
		c.addArg(e1);
		c.addArg(e2);
		c.env = e.env;

		return createIdentifierExprStatement("push", e.env, c);
	}

	// generate the pop call
	private ExprStatement generatePopExpStatement(final BoaScalar b, final String stackName, final VisitorExpression e) {
		final Expression e1 = createIdentifierExpr(stackName, e.env);
		e1.type = new BoaStack(b);

		final Call c = new Call();
		c.addArg(e1);
		c.env = e.env;

		return createIdentifierExprStatement("pop", e.env, c);
	}

	private ExprStatement createIdentifierExprStatement(final String name, final SymbolTable env, final Call c) {
		final Expression exp = createIdentifierExpr(name, env);

		exp.getLhs().getLhs().getLhs().getLhs().getLhs().addOp(c);

		return new ExprStatement(exp);
	}

	private Expression createIdentifierExpr(final String name, final SymbolTable env) {
		final Expression exp = new Expression(
			new Conjunction(
				new Comparison(
					new SimpleExpr(
						new Term(
							new Factor(createIdentifier(name))
						)
					)
				)
			)
		);
		exp.getLhs().getLhs().getLhs().getLhs().getLhs().env = env;
		return exp;
	}

	private Identifier createIdentifier(final String name) {
		final Identifier id = new Identifier(name);
		id.env = env;
		return id;
	}

	/** @{inheritDoc} */
	@Override
	public void visit(final Program n) {
		env = n.env;

		// 1) Find each instance of VisitorExpression, then for each:
		final FindVisitorExpressions visitorsList = new FindVisitorExpressions();
		visitorsList.start(n);

		for (final VisitorExpression e: visitorsList.getVisitors()) {
		//    a) Find all instances of "current(T)" in the visitor
		//    b) Collect set of all unique types T found in 1a
			final FindCurrentForVisitors currentSet = new FindCurrentForVisitors();
			currentSet.start(e.getBody());

		//    c) For each type T in the set from 1b:
			for (final BoaScalar b: currentSet.getCurrentTypes()) {
				env = e.env;

		//       i)   Add a variable 's_T_#' of type 'stack of T' at the top-most scope of the AST
				final VarDeclStatement v = generateStackNode(b);
				v.env = v.getType().env = ((StackType)v.getType()).getValue().getType().env = n.env;
				v.env.set(v.getId().getToken(), v.type);
				n.getStatements().add(0, v);

		//       ii)  Where-ever we encounter 'current(T)', replace with code for 's_T_#.peek()'
				for (final Factor f: currentSet.getFactorList().get(b))
					replaceCurrentCall(f, v);

				final VisitClassifier getVS = new VisitClassifier();
				getVS.start(e.getBody());
				final String typeToFind = b.toJavaType().substring(b.toJavaType().lastIndexOf('.') + 1);

		//       iii) Add/Update the before clause for T in the visitor
		//            a) If the visitor has a 'before T' clause, add 's_t_#.push(node)' as the first statement
				if (getVS.getBeforeMap().containsKey(typeToFind)) {
					final VisitStatement vs = getVS.getBeforeMap().get(typeToFind);
					final Statement pushToStack = generatePushExpStatement(b, v.getId().getToken(), vs.getComponent().getIdentifier().getToken(), e);
					vs.getBody().getStatements().add(0, pushToStack);
				} else {
		//            b) Otherwise, add a 'before T' clause with a 's_t_#.push(node)'
					final Block blk;
					final Statement pushToStack = generatePushExpStatement(b, v.getId().getToken(), "node", e);

					if (getVS.getBeforeMap().containsKey("_")) {
						blk = getVS.getBeforeMap().get("_").getBody().clone();
						blk.getStatements().add(0, pushToStack);
					} else {
						blk = new Block().addStatement(pushToStack);
					}

					final VisitStatement vs = new VisitStatement(true, new Component(createIdentifier("node"), createIdentifier(typeToFind)), blk);
					vs.getComponent().getType().type = b;
					vs.env = e.env;

					e.getBody().getStatements().add(vs);
				}

		//       iv)  Add/Update the after clause for T in the visitor
		//            a) If the visitor has a 'after T' clause, add 's_t_#.pop()' as the first statement
				if (getVS.getAfterMap().containsKey(typeToFind)) {
					final VisitStatement vs = getVS.getAfterMap().get(typeToFind);
					final Statement popFromStack = generatePopExpStatement(b, v.getId().getToken(),e);
					vs.getBody().getStatements().add(popFromStack);
				} else {
		//            b) Otherwise, add a 'after T' clause with a 's_t_#.pop()'
					final Block blk;
					final Statement popFromStack = generatePopExpStatement(b, v.getId().getToken(),e);

					if (getVS.getAfterMap().containsKey("_")) {
						blk = getVS.getAfterMap().get("_").getBody().clone();
						blk.getStatements().add(popFromStack);
					} else {
						blk = new Block().addStatement(popFromStack);
					}

					final VisitStatement vs = new VisitStatement(false, new Component(createIdentifier("node"), createIdentifier(typeToFind)), blk);
					vs.getComponent().getType().type = b;
					vs.env = e.env;

					e.getBody().getStatements().add(vs);
				}
			}
		}
	}
}
