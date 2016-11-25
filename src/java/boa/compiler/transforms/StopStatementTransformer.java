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
import boa.compiler.ast.Term;
import boa.compiler.ast.expressions.Expression;
import boa.compiler.ast.expressions.FunctionExpression;
import boa.compiler.ast.expressions.SimpleExpr;
import boa.compiler.ast.expressions.VisitorExpression;
import boa.compiler.ast.literals.IntegerLiteral;
import boa.compiler.ast.statements.Block;
import boa.compiler.ast.statements.ExprStatement;
import boa.compiler.ast.statements.IfStatement;
import boa.compiler.ast.statements.PostfixStatement;
import boa.compiler.ast.statements.ReturnStatement;
import boa.compiler.ast.statements.StopStatement;
import boa.compiler.ast.statements.VarDeclStatement;
import boa.compiler.ast.statements.VisitStatement;
import boa.compiler.ast.types.FunctionType;
import boa.compiler.visitors.AbstractVisitorNoArg;
import boa.compiler.visitors.VisitClassifier;
import boa.types.BoaProtoTuple;

/**
 * Finds all visitors and converts the stop statements into counter functions.
 * This makes the visitor's side effects the same while changing the semantics
 * so it doesn't actually stop the visit (and thus will visit all nodes).
 * 
 * General algorithm:
 * 
 * 1) convert all self-visit() calls to pass in copy of the original visitor
 * 2) add a long var declaration to the enclosing scope
 * 3) find all visit statements containing stop statements, for each one:
 *    a) lift the body into a new function in the surrounding scope
 *       i) replace all stop statements with incrementing long var and a return statement
 *    b) replace the original body with a call to the function
 *    c) add code "if (stop > 0) stop++;" to the START of the new body
 * 4) find all after visits matching the visited node types in step 1, for each one:
 *    a) wrap body in an "if (stop == 0)" guard
 *    b) add code "if (stop > 0) stop--;", after ALL if() blocks
 * 5) for any node type matching the visited node types in step 1 not found in step 2:
 *    a) add an after visit for that type
 *    b) add code "if (stop > 0) stop--;"
 * 6) wrap default before visit body in an "if (stop == 0)" guard
 * 7) find all before/after visits with a node type below a node type found in step 1, for each one:
 *    a) add code "if (stop > 0) stop--;", after ALL if() blocks
 * 
 * @author rdyer
 */
public class StopStatementTransformer extends AbstractVisitorNoArg {
	protected static final String funcId = "_hasStop";
	public static final String funcWithVisitorId = funcId + "WithVisitor";
	public static final String funcCurVisitorId = "_curVisitor_";

	protected static final String varCounterName = "_stopCounter";

	/**
	 * Finds all {@link StopStatement}s and records the
	 * {@link VisitStatement}s containing them.
	 * 
	 * @author rdyer
	 */
	private class StopFindingVisitor extends AbstractVisitorNoArg {
		protected VisitStatement lastVisit;
		protected final Set<VisitStatement> stops = new HashSet<VisitStatement>();

		/**
		 * Returns all found {@link VisitStatement}s that contain at least
		 * one {@link StopStatement}.
		 * 
		 * @return the {@link Set} of found {@link VisitStatement}s
		 */
		public Set<VisitStatement> getVisits() {
			return stops;
		}

		/** @{inheritDoc} */
		@Override
		protected void initialize() {
			lastVisit = null;
			stops.clear();
		}

		/** @{inheritDoc} */
		@Override
		public void visit(final VisitorExpression n) {
			// dont nest
		}

		/** @{inheritDoc} */
		@Override
		public void visit(final VisitStatement n) {
			lastVisit = n;
			super.visit(n);
		}

		/** @{inheritDoc} */
		@Override
		public void visit(final StopStatement n) {
			stops.add(lastVisit);
			super.visit(n);
		}
	}

	/**
	 * Finds all {@link StopStatement}s and transforms them into
	 * incrementing the long variable and then a {@link ReturnStatement}.
	 * 
	 * @author rdyer
	 */
	private class StopTransformer extends AbstractVisitorNoArg {
		/** @{inheritDoc} */
		@Override
		public void visit(final VisitorExpression n) {
			// dont nest
		}

		/** @{inheritDoc} */
		@Override
		public void visit(final StopStatement n) {
			final ReturnStatement ret = new ReturnStatement();
			n.getParent().replaceStatement(n, ret);

			ret.insertStatementBefore(
				new PostfixStatement(
					ASTFactory.createFactorExpr(
						ASTFactory.createIdentifier(varCounterName, n.env)
					),
					"++"
				)
			);
		}
	}

	/**
	 * Converts all self-visit() calls to use a copy of the visitor.
	 * 
	 * @author rdyer
	 */
	private class SelfVisitTransformer extends AbstractVisitorNoArg {
		/** @{inheritDoc} */
		@Override
		public void visit(final VisitorExpression n) {
			// dont nest
		}

		/** @{inheritDoc} */
		@Override
		public void visit(final Factor n) {
			if (n.getOperand() instanceof Identifier) {
				final Identifier id = (Identifier)n.getOperand();
				if (id.getToken().equals("visit")) {
					final Call c = (Call)n.getOp(0);
					if (c.getArgsSize() == 1) {
						c.addArg(ASTFactory.createFactorExpr(original.clone()));
						return;
					}
				}
			}

			super.visit(n);
		}
	}

	protected final StopFindingVisitor stopFinder = new StopFindingVisitor();
	protected final VisitClassifier visitClassifier = new VisitClassifier();
	protected final StopTransformer transformStops = new StopTransformer();
	protected final SelfVisitTransformer transformSelfVisits = new SelfVisitTransformer();

	protected int counter = 0;
	protected VisitorExpression original;

	/** @{inheritDoc} */
	@Override
	protected void initialize() {
		counter = 0;
		original = null;
	}

	/** @{inheritDoc} */
	@SuppressWarnings("unchecked")
	@Override
	public void visit(VisitorExpression n) {
		int wildcardStop = 0;
		final Map<BoaProtoTuple, Integer> beforeStopTypes = new HashMap<BoaProtoTuple, Integer>();
		final Map<VisitStatement, Integer> bodyMap = new HashMap<VisitStatement, Integer>();

		original = n.clone();

		//
		// 1) convert all self-visit() calls to pass in copy of the original visitor
		//
		transformSelfVisits.start(n.getBody());

		//
		// 2) add a long var declaration to the enclosing scope
		//
		n.insertStatementBefore(
			new VarDeclStatement(
				new Identifier(varCounterName),
				new Identifier("int"),
				ASTFactory.createFactorExpr(new IntegerLiteral("0"))
			)
		);

		//
		// 3) find all visit statements containing stop statements, for each one:
		//
		stopFinder.start(n.getBody());
		for (final VisitStatement v : stopFinder.getVisits()) {
			//
			//    a) lift the body into a new function in the surrounding scope
			//
			final FunctionType funcType = new FunctionType();
			final String funcName;
			if (v.hasComponent()) {
				funcType.addArg(v.getComponent().clone());
				funcName = funcWithVisitorId + ++counter;
			} else {
				funcName = funcId + ++counter;
			}
			final Block funcBody = v.getBody().clone();
			final VarDeclStatement var = new VarDeclStatement(
					new Identifier(funcName),
					ASTFactory.createFactorExpr(new FunctionExpression(funcType, funcBody))
				);
			n.insertStatementBefore(var);

			//
			//       i) replace all stop statements with incrementing long var and a return statement
			//
			transformStops.start(funcBody);


			//
			//    b) replace the body with a call to the function
			//
			final Call c2 = new Call();
			if (funcType.getArgsSize() > 0)
				c2.addArg(ASTFactory.createFactorExpr(funcType.getArg(0).getIdentifier().clone()));
			final Factor f2 = new Factor(new Identifier(funcName));
			f2.addOp(c2);
			v.setBody(new Block().addStatement(new ExprStatement(
							new Expression(
								new Conjunction(
									new Comparison(
										new SimpleExpr(
											new Term(f2)
										)
									)
								)
							)
						)));

			bodyMap.put(v, counter);

			if (v.hasWildcard())
				wildcardStop = counter;
			else if (v.hasComponent())
				beforeStopTypes.put((BoaProtoTuple)v.getComponent().type, counter);
			else
				for (final Identifier id : v.getIdList())
					beforeStopTypes.put((BoaProtoTuple)id.type, counter);
		}

		final Set<BoaProtoTuple> keys = new HashSet<BoaProtoTuple>(beforeStopTypes.keySet());
		final Set<BoaProtoTuple> remainingAfters = new HashSet<BoaProtoTuple>(keys);

		//
		// 4) find all after visits matching the visited node types in step 1, for each one:
		//
		visitClassifier.start(n.getBody());
		for (final VisitStatement v : visitClassifier.getAfters()) {
			final Set<BoaProtoTuple> types = new HashSet<BoaProtoTuple>();
			if (v.hasComponent())
				types.add((BoaProtoTuple)v.getComponent().getType().type);
			else
				for (final Identifier id : v.getIdList())
					types.add((BoaProtoTuple)id.type);

			final Set<Integer> intersection = new HashSet<Integer>();
			for (final BoaProtoTuple t : keys)
				if (types.contains(t))
					intersection.add(beforeStopTypes.get(t));
			if (wildcardStop > 0 && v.hasWildcard())
				intersection.add(wildcardStop);

			if (intersection.isEmpty())
				continue;

			remainingAfters.removeAll(types);

			//
			//    a) wrap body in an "if (stop == 0)" guard
			//
			addGuard(v);

			//
			//    b) add code "if (stop > 0) stop--;", after ALL if() blocks
			//
			createReset(v.getBody());
		}

		//
		// 5) for any node type matching the visited node types in step 1 not found in step 2:
		//
		for (final BoaProtoTuple t : remainingAfters) {
			//
			//    a) add an after visit for that type
			//
			final Block body = new Block();
			n.getBody().addStatement(new VisitStatement(false, new Component(new Identifier("n"), new Identifier(t.toString())), body));

			//
			//    b) add code "if (stop > 0) stop--;"
			//
			createReset(body);
		}

		if (wildcardStop > 0 && !visitClassifier.hasDefaultAfter()) {
			final Block body = new Block();
			n.getBody().addStatement(new VisitStatement(false, true, body));
			createReset(body);
		}

		//
		// 6) wrap default before visit body in an "if (stop == 0)" guard
		//
		if (visitClassifier.hasDefaultBefore())
			addGuard(visitClassifier.getDefaultBefore());

		//
		// 7) find all before/after visits with a node type below a node type found in step 1, for each one:
		//
		for (final BoaProtoTuple t : beforeStopTypes.keySet()) {
			final List<VisitStatement> statements = new ArrayList<VisitStatement>(visitClassifier.getBefores());
			statements.addAll(visitClassifier.getAfters());

			for (final VisitStatement v : statements) {
				final Set<Class<? extends BoaProtoTuple>> types = new HashSet<Class<? extends BoaProtoTuple>>();
				if (v.hasComponent())
					types.add((Class<? extends BoaProtoTuple>) v.getComponent().getType().type.getClass());
				else
					for (final Identifier id : v.getIdList())
						types.add((Class<? extends BoaProtoTuple>) id.type.getClass());
				
				types.retainAll(t.reachableTypes());
				if (types.isEmpty())
					continue;

				//
				//    a) add code "if (stop > 0) stop--;", after ALL if() blocks
				//
				// FIXME the nesting makes for more than 1 guard
				addGuard(v);
			}
		}

		//    c) add code "if (stop > 0) stop++;" to the START of the new body
		for (final VisitStatement v : bodyMap.keySet())
			v.getBody().getStatements().add(0,
				new IfStatement(
					ASTFactory.createComparison(
						new Identifier(varCounterName),
						">",
						new IntegerLiteral("0")
					),
					new Block().addStatement(
						new PostfixStatement(
							ASTFactory.createFactorExpr(new Identifier(varCounterName)),
							"++"
						)
					)
				)
			);
	}

	protected void addGuard(final VisitStatement v) {
		final Block body = new Block();
		body.addStatement(
			new IfStatement(
				ASTFactory.createComparison(
					new Identifier(varCounterName),
					"==",
					new IntegerLiteral("0")
				),
				v.getBody().clone()
			)
		);
		v.setBody(body);
	}

	protected void createReset(final Block body) {
		body.addStatement(
			new IfStatement(
				ASTFactory.createComparison(
					new Identifier(varCounterName),
					">",
					new IntegerLiteral("0")
				),
				new Block().addStatement(
					new PostfixStatement(
						ASTFactory.createFactorExpr(new Identifier(varCounterName)),
						"--"
					)
				)
			)
		);
	}
}
