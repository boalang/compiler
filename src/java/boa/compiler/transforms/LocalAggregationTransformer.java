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
import java.util.List;

import boa.aggregators.Aggregator;
import boa.compiler.ast.Comparison;
import boa.compiler.ast.Conjunction;
import boa.compiler.ast.Factor;
import boa.compiler.ast.Identifier;
import boa.compiler.ast.Program;
import boa.compiler.ast.Term;
import boa.compiler.ast.expressions.Expression;
import boa.compiler.ast.expressions.ParenExpression;
import boa.compiler.ast.expressions.SimpleExpr;
import boa.compiler.ast.literals.IntegerLiteral;
import boa.compiler.ast.statements.AssignmentStatement;
import boa.compiler.ast.statements.Block;
import boa.compiler.ast.statements.EmitStatement;
import boa.compiler.ast.statements.IfStatement;
import boa.compiler.ast.statements.VarDeclStatement;
import boa.compiler.ast.types.OutputType;
import boa.compiler.visitors.AbstractVisitorNoArg;
import boa.types.BoaInt;

/**
 * Performs aggregations locally (in Boa) as much as possible, before
 * sending output to the output variable.  This is like a combiner step,
 * but performed via rewrites to the Boa program (and thus happens
 * before the combiner).
 * 
 * @author rdyer
 */
public class LocalAggregationTransformer extends AbstractVisitorNoArg {
	/**
	 * Finds all output variables using a 'sum' {@link Aggregator}.
	 * 
	 * @author rdyer
	 */
	protected class SumAggregatorFindingVisitor extends AbstractVisitorNoArg {
		private final List<String> vars = new ArrayList<String>();

		public List<String> getVars() {
			return vars;
		}

		/** {@inheritDoc} */
		@Override
		protected void initialize() {
			vars.clear();
		}

		/** {@inheritDoc} */
		@Override
		public void visit(final VarDeclStatement n) {
			if (n.hasType() && n.getType() instanceof OutputType) {
				final OutputType t = (OutputType) n.getType();
				if ("sum".equals(t.getId().getToken()) && t.getIndicesSize() == 0 && t.getType().getType() instanceof Identifier && ((Identifier)t.getType().getType()).getToken().equals("int"))
					vars.add(n.getId().getToken());
			}
		}
	}

	protected final SumAggregatorFindingVisitor sumAggregatorFinder = new SumAggregatorFindingVisitor();

	protected final String varPrefix = "_local_aggregator_";

	/** {@inheritDoc} */
	@Override
	public void visit(final Program n) {
		sumAggregatorFinder.start(n);

		for (final String s : sumAggregatorFinder.getVars())
			generateCacheVariable(n, s);

		super.visit(n);

		for (final String s : sumAggregatorFinder.getVars())
			generateCacheOutput(n, s);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final EmitStatement n) {
		if (!sumAggregatorFinder.getVars().contains(n.getId().getToken()))
			return;

		generateStoreValue(n);
	}

	protected void generateCacheVariable(final Program n, final String s) {
		final VarDeclStatement var = ASTFactory.createVarDecl(varPrefix + s, new IntegerLiteral("0"), new BoaInt(), n.env);
		n.env.set(varPrefix + s, new BoaInt());
		n.getStatements().add(0, var);
	}

	protected void generateCacheOutput(final Program n, String s) {
		final Identifier id = ASTFactory.createIdentifier(varPrefix + s, n.env);
		n.getStatements().add(
			new IfStatement(
				ASTFactory.createComparison(id, "!=", new IntegerLiteral("0")),
				new Block().addStatement(
					new EmitStatement(
						ASTFactory.createIdentifier(s, n.env),
						ASTFactory.createFactorExpr(id.clone())
					)
				)
			)
		);
	}

	protected void generateStoreValue(final EmitStatement n) {
		final Identifier id = ASTFactory.createIdentifier(varPrefix + n.getId().getToken(), n.env);

		n.replaceStatement(n,
			new AssignmentStatement(
				new Factor(id.clone()),
				ASTFactory.createComparison(id, "+", new ParenExpression(n.getValue().clone()))
			)
		);
	}
}
