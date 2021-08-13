/*
 * Copyright 2016-2021, Hridesh Rajan, Robert Dyer, Neha Bhide
 *                 Iowa State University of Science and Technology
 *                 Bowling Green State University
 *                 and University of Nebraska Board of Regents
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
import boa.compiler.ast.Component;
import boa.compiler.ast.expressions.Expression;
import boa.compiler.ast.expressions.VisitorExpression;
import boa.compiler.ast.Factor;
import boa.compiler.ast.Identifier;
import boa.compiler.ast.Program;
import boa.compiler.ast.statements.Block;
import boa.compiler.ast.statements.ExprStatement;
import boa.compiler.ast.statements.Statement;
import boa.compiler.ast.statements.VarDeclStatement;
import boa.compiler.ast.statements.VisitStatement;
import boa.compiler.ast.types.StackType;
import boa.compiler.SymbolTable;
import boa.compiler.visitors.AbstractVisitorNoArgNoRet;
import boa.compiler.visitors.TypeCheckingVisitor;
import boa.compiler.visitors.VisitClassifier;
import boa.types.BoaTuple;
import boa.types.BoaStack;

// FIXME rdyer - if a visitor calls visit() on a node, it uses the current tree's stacks
// probably we should check if that node is the current node (or one of its direct children)
// and if it isnt, generate/use different stacks?

/**
 * Converts use of current(T) inherited attributes in visitors into stack variables.
 *
 * <p>General algorithm:
 *
 * <p>1) Find each instance of VisitorExpression, then for each:
 *       a) Find all instances of "current(T)" in the visitor
 *       b) Collect set of all unique types T found in 1a
 *    2) For each type T in the set from 1b:
 *       a) Add a variable 's_T_#' of type 'stack of T' at the top-most scope of the AST
 *       b) Where-ever we encounter 'current(T)', replace with code for 's_T_#.peek()'
 *       c) For each instance of VisitorExpression:
 *          ii)  Add/Update the before clause for T in the visitor
 *               1) If the visitor has a 'before T' clause, add 's_t_#.push(node)' as the first statement
 *               2) Otherwise, add a 'before T' clause with a 's_t_#.push(node)'
 *          iii) Add/Update the after clause for T in the visitor
 *               1) If the visitor has a 'after T' clause, add 's_t_#.pop()' as the first statement
 *               2) Otherwise, add a 'after T' clause with a 's_t_#.pop()'
 *
 * @author rdyer
 * @author nbhide
 */
public class InheritedAttributeTransformer extends AbstractVisitorNoArgNoRet {
	private final static String stackPrefix = "_inhattr_";
	private int stackCounter = 0;

	/**
	 * Creates a list of all the {@link VisitorExpression}s in the Boa AST.
	 */
	private class FindVisitorExpressions extends AbstractVisitorNoArgNoRet {
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
	private class FindCurrentInVisitors extends AbstractVisitorNoArgNoRet{
		protected final Set<BoaTuple> currents = new HashSet<BoaTuple>();
		protected final Map<BoaTuple, List<Factor>> factorMap = new HashMap<BoaTuple, List<Factor>>();

		/** @{inheritDoc} */
		@Override
		protected void initialize() {
			currents.clear();
			factorMap.clear();
		}

		public Set<BoaTuple> getCurrentTypes() {
			return currents;
		}

		public Map<BoaTuple, List<Factor>> getFactorList() {
			return factorMap;
		}

		/** @{inheritDoc} */
		@Override
		public void visit(final Factor n) {
			if (n.getOperand() instanceof Identifier) {
				final Identifier id = (Identifier)n.getOperand();

				if (id.getToken().equals("current") && n.getOp(0) instanceof Call) {
					final Call c = (Call)n.getOp(0);

					if (c.getArgsSize() == 1) {
						final BoaTuple t = (BoaTuple)c.getArg(0).type;
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

	// generate the push call
	private ExprStatement generatePushExpStatement(final BoaTuple b, final String stackName, final String nodeName, final VisitorExpression e) {
		final Expression e1 = ASTFactory.createIdentifierExpr(stackName, e.env, new BoaStack(b));
		final Expression e2 = ASTFactory.createIdentifierExpr(nodeName, e.env, b);

		return ASTFactory.createCall("push", e.env, null, e1, e2);
	}

	// generate the pop call
	private ExprStatement generatePopExpStatement(final BoaTuple b, final String stackName, final VisitorExpression e) {
		final Expression e1 = ASTFactory.createIdentifierExpr(stackName, e.env, new BoaStack(b));

		return ASTFactory.createCall("pop", e.env, b, e1);
	}

	/** @{inheritDoc} */
	@Override
	public void visit(final Program n) {
		// 1) Find each instance of VisitorExpression, then for each:
		final FindVisitorExpressions visitorsList = new FindVisitorExpressions();
		visitorsList.start(n);
		final List<VisitorExpression> visitors = visitorsList.getVisitors();

		final FindCurrentInVisitors currentSet = new FindCurrentInVisitors();
		//        a) Find all instances of "current(T)" in the visitor
		//        b) Collect set of all unique types T found in 1a
		currentSet.start(n);

		// 2) For each type T in the set from 1b:
		for (final BoaTuple b: currentSet.getCurrentTypes()) {
			//    a) Add a variable 's_T_#' of type 'stack of T' at the top-most scope of the AST
			final String typeToFind = b.toJavaType().substring(b.toJavaType().lastIndexOf('.') + 1); // FIXME this is a bit of a hack

			final StackType st = new StackType(new Component(ASTFactory.createIdentifier(typeToFind, n.env)));
			st.type = new BoaStack(b);

			final VarDeclStatement v = ASTFactory.createVarDecl(stackPrefix + stackCounter++, st, st.type, n.env);
			TypeCheckingVisitor.instance.start(v, n.env);
			n.getStatements().add(0, v);

			//    b) Where-ever we encounter 'current(T)', replace with code for 's_T_#.peek()'
			for (final Factor f: currentSet.getFactorList().get(b))
				replaceCurrentCall(f, v);

			//    c) For each instance of VisitorExpression:
			for (final VisitorExpression e: visitors) {
				final VisitClassifier getVS = new VisitClassifier();
				getVS.start(e.getBody());

				final String token = v.getId().getToken();

				e.env.set(token, st.type);

				//   ii)  Add/Update the before clause for T in the visitor
				updateVisitClause(true, n.env, getVS.getBeforeMap(), typeToFind, b, e, token, st);

				//   iii) Add/Update the after clause for T in the visitor
				updateVisitClause(false, n.env, getVS.getAfterMap(), typeToFind, b, e, token, st);
			}
		}
	}

	//   ii)  Add/Update the before clause for T in the visitor
	//   iii) Add/Update the after clause for T in the visitor
	private void updateVisitClause(final boolean isBefore, final SymbolTable env, final Map<String, VisitStatement> visitMap, final String typeToFind, final BoaTuple b, final VisitorExpression e, final String token, final StackType st) {
		final VisitStatement vs;

		//        1) If the visitor has a 'before T' clause, add 's_t_#.push(node)' as the first statement
		if (visitMap.containsKey(typeToFind)) {
			vs = visitMap.get(typeToFind);

			vs.getBody().getStatements().add(0, generatePushExpStatement(b, token, vs.getComponent().getIdentifier().getToken(), e));
		} else {
			//    2) Otherwise, add a 'before T' clause with a 's_t_#.push(node)'
			final Block blk;
			if (visitMap.containsKey("_"))
				blk = visitMap.get("_").getBody().clone();
			else
				blk = new Block();

			blk.getStatements().add(0, generatePushExpStatement(b, token, "_n", e));

			vs = new VisitStatement(isBefore, new Component(ASTFactory.createIdentifier("_n", env), ASTFactory.createIdentifier(typeToFind, env)), blk);
			TypeCheckingVisitor.instance.start(vs, e.env);

			e.getBody().getStatements().add(vs);
			new VariableDeclRenameTransformer().start(vs);
		}

		vs.getBody().env.set(token, st.type);
	}
}
