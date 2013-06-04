package boa.compiler.transforms;

import java.util.ArrayList;
import java.util.List;

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
 * but performed via rewrites to the Boa program.
 * 
 * @author rdyer
 */
public class LocalAggregationTransformer extends AbstractVisitorNoArg {
	protected class OutputVarFindingVisitor extends AbstractVisitorNoArg {
		private final List<String> vars = new ArrayList<String>();
		private String lastId;

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
			lastId = n.getId().getToken();
			super.visit(n);
		}

		/** {@inheritDoc} */
		@Override
		public void visit(final OutputType n) {
			if (n.getId().getToken().equals("sum") && n.getIndicesSize() == 0)
				vars.add(lastId);
		}
	}

	protected final OutputVarFindingVisitor outputVarFinder = new OutputVarFindingVisitor();

	protected final String varPrefix = "_local_aggregator_";

	/** {@inheritDoc} */
	@Override
	public void visit(final Program n) {
		outputVarFinder.start(n);
		for (final String s : outputVarFinder.getVars()) {
			final VarDeclStatement var = new VarDeclStatement(
					new Identifier(varPrefix + s),
					new Expression(
						new Conjunction(
							new Comparison(
								new SimpleExpr(
									new Term(
										new Factor(
											new IntegerLiteral("0")
										)
									)
								)
							)
						)
					)
				);
			n.getStatements().add(0, var);

			n.env.set(varPrefix + s, new BoaInt());
			var.type = var.getInitializer().type = new BoaInt();
			var.env = n.env;
		}

		super.visit(n);

		for (final String s : outputVarFinder.getVars()) {
			final Identifier id = new Identifier(varPrefix + s);
			id.env = n.env;
			n.getStatements().add(
				new IfStatement(
					new Expression(
						new Conjunction(
							new Comparison(
								new SimpleExpr(
									new Term(
										new Factor(id)
									)
								),
								"!=",
								new SimpleExpr(
									new Term(
										new Factor(new IntegerLiteral("0"))
									)
								)
							)
						)
					),
					new Block().addStatement(
						new EmitStatement(
							new Identifier(s),
							new Expression(
								new Conjunction(
									new Comparison(
										new SimpleExpr(
											new Term(
												new Factor(id.clone())
											)
										)
									)
								)
							)
						)
					)
				)
			);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final EmitStatement n) {
		if (!outputVarFinder.getVars().contains(n.getId().getToken()))
			return;

		final Identifier id = new Identifier(varPrefix + n.getId().getToken());
		id.env = n.env;

		final SimpleExpr e = new SimpleExpr(
			new Term(
				new Factor(
					id
				)
			)
		);
		e.addOp("+");
		e.addRhs(new Term(
			new Factor(
				new ParenExpression(n.getValue().clone())
			)
		));

		n.replaceStatement(n, new AssignmentStatement(
				new Factor(
					id.clone()
				),
				new Expression(
					new Conjunction(
						new Comparison(e)
					)
				)));
	}
}
