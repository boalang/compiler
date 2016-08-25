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
import java.util.List;
import java.util.Map;

import boa.compiler.SymbolTable;
import boa.compiler.ast.Call;
import boa.compiler.ast.Factor;
import boa.compiler.ast.Identifier;
import boa.compiler.ast.Program;
import boa.compiler.ast.expressions.FunctionExpression;
import boa.compiler.ast.expressions.VisitorExpression;
import boa.compiler.ast.statements.Block;
import boa.compiler.ast.statements.Statement;
import boa.compiler.ast.statements.VisitStatement;
import boa.compiler.visitors.AbstractVisitorNoArg;
import boa.compiler.visitors.TypeCheckingVisitor;
import boa.compiler.visitors.VisitClassifier;
import boa.types.BoaProtoTuple;
import boa.types.BoaVisitor;
import boa.types.proto.ProjectProtoTuple;

/**
 * Takes a list of {@link Program}s and merges them together into a single
 * program, attempting to merge visitors where possible.
 * 
 * @author rdyer
 */
public class VisitorMergingTransformer {
	public List<Program> mergePrograms(final List<Program> programs, final int maxMerged) {
		final List<Program> merged = new ArrayList<Program>();

		int pos = 0;
		int stop = maxMerged;

		while (pos < programs.size()) {
			final Program current = programs.get(pos++);
			preProcessProgram(current);

			for (; pos < stop && pos < programs.size(); pos++) {
				final Program p = programs.get(pos);
				preProcessProgram(p);
				mergePrograms(current, p);
//				merged.add(p);
			}

			stop += maxMerged;
			merged.add(current);
		}

		return merged;
	}

	/**
	 * Determines if a tree has a top-level visitor declaration.
	 * 
	 * @author rdyer
	 */
	protected class VisitorFindingVisitor extends AbstractVisitorNoArg {
		protected VisitorExpression visitor;

		public boolean hasVisitor() {
			return visitor != null;
		}

		public VisitorExpression getVisitor() {
			return visitor;
		}

		/** {@inheritDoc} */
		@Override
		protected void initialize() {
			visitor = null;
		}

		/** {@inheritDoc} */
		@Override
		public void visit(FunctionExpression n) {
			// dont look inside functions
		}

		/** {@inheritDoc} */
		@Override
		public void visit(final VisitorExpression n) {
			visitor = n;
		}
	}

	/**
	 * Determines if a tree has a visit() call.
	 * 
	 * @author rdyer
	 */
	protected class VisitCallFindingVisitor extends AbstractVisitorNoArg {
		protected boolean hasVisitCall;

		public boolean hasVisitCall() {
			return hasVisitCall;
		}

		/** {@inheritDoc} */
		@Override
		protected void initialize() {
			hasVisitCall = false;
		}

		/** {@inheritDoc} */
		@Override
		public void visit(final Factor n) {
			if (n.getOperand() instanceof Identifier) {
				final Identifier id = (Identifier)n.getOperand();

				if (id.getToken().equals("visit") && n.getOpsSize() == 1 && n.getOp(0) instanceof Call) {
					final Call c = (Call) n.getOp(0);

					if (c.getArgsSize() == 2 && c.getArg(0).type instanceof ProjectProtoTuple && c.getArg(1).type instanceof BoaVisitor)
						hasVisitCall = true;
				}
			}
		}
	}

	protected final VisitorFindingVisitor visitorFinder = new VisitorFindingVisitor();
	protected final VisitCallFindingVisitor visitCallFinder = new VisitCallFindingVisitor();
	protected final TypeCheckingVisitor typeChecker = new TypeCheckingVisitor();

	protected final StopStatementTransformer stopTransform = new StopStatementTransformer();
	protected final VariableRenameTransformer varRename = new VariableRenameTransformer();

	protected void preProcessProgram(final Program p) {
		stopTransform.start(p);
		// FIXME rdyer need to figure out how to avoid this, but anything
		// new in the transformations needs added to the environment
		typeChecker.start(p, new SymbolTable());
		varRename.start(p, p.jobName);
	}

	protected void mergePrograms(final Program left, final Program right) {
		int currentLeft = 0;
		int currentRight = 0;

		// find first visitor decl
		for (; currentLeft < left.getStatementsSize(); currentLeft++) {
			visitorFinder.start(left.getStatement(currentLeft));
			if (visitorFinder.hasVisitor())
				break;
		}

		// find right's first visitor, and insert everything up until then into left
		for (; currentRight < right.getStatementsSize(); currentRight++) {
			final Statement rightStmt = right.getStatement(currentRight);

			visitorFinder.start(rightStmt);
			if (visitorFinder.hasVisitor())
				break;

			left.getStatement(currentLeft++).insertStatementBefore(rightStmt);
		}

		visitorFinder.start(left.getStatement(currentLeft++));
		final VisitorExpression leftVisitor = visitorFinder.getVisitor();
		visitorFinder.start(right.getStatement(currentRight++));
		mergeVisitors(leftVisitor, visitorFinder.getVisitor());

		// find left's visit call
		for (; currentLeft < left.getStatementsSize(); currentLeft++) {
			visitCallFinder.start(left.getStatement(currentLeft));
			if (visitCallFinder.hasVisitCall())
				break;
		}

		// find right's visit call, and insert everything up until then into left
		for (; currentRight < right.getStatementsSize(); currentRight++) {
			final Statement rightStmt = right.getStatement(currentRight);

			visitCallFinder.start(rightStmt);
			if (visitCallFinder.hasVisitCall())
				break;

			left.getStatement(currentLeft++).insertStatementBefore(rightStmt);
		}

		currentRight++;

		for (; currentRight < right.getStatementsSize(); currentRight++)
			left.addStatement(right.getStatement(currentRight));
	}

	protected final VisitClassifier leftClassifier = new VisitClassifier();
	protected final VisitClassifier rightClassifier = new VisitClassifier();

	protected void mergeVisitors(final VisitorExpression left, final VisitorExpression right) {
		leftClassifier.start(left.getBody());
		rightClassifier.start(right.getBody());

		if (rightClassifier.hasDefaultBefore()) {
			if (leftClassifier.hasDefaultBefore()) {
				final Block body = leftClassifier.getDefaultBefore().getBody();
				for (final Statement s : rightClassifier.getDefaultBefore().getBody().getStatements())
					body.addStatement(s);
			} else {
				left.getBody().addStatement(rightClassifier.getDefaultBefore());
			}
		}

		if (rightClassifier.hasDefaultAfter()) {
			if (leftClassifier.hasDefaultAfter()) {
				final Block body = leftClassifier.getDefaultAfter().getBody();
				for (final Statement s : rightClassifier.getDefaultAfter().getBody().getStatements())
					body.addStatement(s);
			} else {
				left.getBody().addStatement(rightClassifier.getDefaultAfter());
			}
		}

		mergeNonDefaultVisits(left, leftClassifier.getBefores(), rightClassifier.getBefores());
		mergeNonDefaultVisits(left, leftClassifier.getAfters(), rightClassifier.getAfters());
	}

	@SuppressWarnings("unchecked")
	private void mergeNonDefaultVisits(final VisitorExpression left, final List<VisitStatement> leftVisits, final List<VisitStatement> rightVisits) {
		final Map<Class<? extends BoaProtoTuple>, Block> bodies = new HashMap<Class<? extends BoaProtoTuple>, Block>();

		// make a map from types to their visits in the left visitor
		for (final VisitStatement v : leftVisits) {
			if (v.hasWildcard())
				continue;
			if (v.hasComponent())
				bodies.put((Class<? extends BoaProtoTuple>) v.getComponent().getType().type.getClass(), v.getBody());
			else
				for (final Identifier id : v.getIdList())
					bodies.put((Class<? extends BoaProtoTuple>) id.type.getClass(), v.getBody());
		}

		for (final VisitStatement v : rightVisits) {
			if (v.hasWildcard())
				continue;

			if (v.hasComponent()) {
				final Block body = bodies.get((Class<? extends BoaProtoTuple>) v.getComponent().getType().type.getClass());
				if (body != null)
					for (final Statement s : v.getBody().getStatements())
						body.addStatement(s);
				else
					left.getBody().addStatement(v);
				continue;
			}

			for (final Identifier id : v.getIdList()) {
				final Block body = bodies.get((Class<? extends BoaProtoTuple>) id.type.getClass());
				if (body != null)
					for (final Statement s : v.getBody().getStatements())
						body.addStatement(s);
				else {
					final VisitStatement vs = v.clone();
					vs.getIdList().clear();
					vs.addId(id.clone());
					left.getBody().addStatement(vs);
				}
			}
		}
	}
}
