/*
 * Copyright 2021, Robert Dyer
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
import java.util.List;
import java.util.Set;

import boa.compiler.ast.Call;
import boa.compiler.ast.Comparison;
import boa.compiler.ast.Component;
import boa.compiler.ast.Conjunction;
import boa.compiler.ast.expressions.Expression;
import boa.compiler.ast.expressions.FunctionExpression;
import boa.compiler.ast.expressions.SimpleExpr;
import boa.compiler.ast.Factor;
import boa.compiler.ast.Identifier;
import boa.compiler.ast.Program;
import boa.compiler.ast.statements.Block;
import boa.compiler.ast.statements.VarDeclStatement;
import boa.compiler.ast.Term;
import boa.compiler.ast.types.StackType;
import boa.compiler.visitors.AbstractVisitorNoArgNoRet;
import boa.compiler.visitors.analysis.CallGraphAnalysis;
import boa.types.BoaAny;
import boa.types.BoaFunction;
import boa.types.BoaStack;
import boa.types.BoaTable;
import boa.types.BoaType;

/**
 * Finds all recursive user functions and automatically turns their locals
 * into stacks that push/pop on (potentially) recursive calls.
 *
 * <p>FIXME saves every local, even if not used after a call
 *       this is safe - but over-approximate and slow
 *
 * @author rdyer
 */
public class RecursiveFunctionTransformer extends AbstractVisitorNoArgNoRet {
	final private String varPrefix = "_rec_stack_";

	protected class VarDeclFinder extends AbstractVisitorNoArgNoRet {
		private List<VarDeclStatement> decls = new ArrayList<VarDeclStatement>();

		/** {@inheritDoc} */
		@Override
		protected void initialize() {
			decls.clear();
		}

		public List<VarDeclStatement> getDecls() { return decls; }

		/** {@inheritDoc} */
		@Override
		public void visit(final FunctionExpression n) {
			// don't nest
		}

		/** {@inheritDoc} */
		@Override
		public void visit(final VarDeclStatement n) {
			if (n.type instanceof BoaTable || n.type instanceof BoaFunction)
				return;

			decls.add(n);
		}
	}

	private CallGraphAnalysis calls = new CallGraphAnalysis();

	/** {@inheritDoc} */
	@Override
	public void visit(final Program n) {
		calls.start(n);
		super.visit(n);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final FunctionExpression n) {
		final String name = getFunctionName(n);

		// if this function is (mutually) recursive, transform it
		if (name != null && calls.getCalls(name).contains(name)) {
			// find the locals needing saved
			final VarDeclFinder finder = new VarDeclFinder();
			finder.start(n.getBody());
			final List<VarDeclStatement> decls = finder.getDecls();

			// generate stacks for each local
			if (decls.size() > 0) {
				final Block b = n.getBody();

				for (final VarDeclStatement v : decls) {
					final StackType st = new StackType(new Component(v.type.toAST(b.env)));
					st.env = b.env;
					final VarDeclStatement var = ASTFactory.createVarDecl(name + varPrefix + v.getId().getToken(), st, new BoaStack(v.type), b.env);

					b.env.set(var.getId().getToken(), var.type);
					n.getParent().insertStatementBefore(var);
				}

				// generate push/pop around each recursive call out
				new CallWrapper(decls, name).start(n.getBody());
			}
		}

		super.visit(n);
	}

	protected class CallWrapper extends AbstractVisitorNoArgNoRet {
		private List<VarDeclStatement> decls;
		private String name;
		private long counter;

		public CallWrapper(final List<VarDeclStatement> decls, final String name) {
			this.decls = decls;
			this.name = name;
		}

		/** {@inheritDoc} */
		@Override
		protected void initialize() {
			this.counter = 0;
		}

		/** {@inheritDoc} */
		@Override
		public void visit(final Factor n) {
			super.visit(n);

			if (n.getOpsSize() == 1 && n.getOp(0) instanceof Call && n.getOperand() instanceof Identifier) {
				final String id = ((Identifier)n.getOperand()).getToken();
				final Set<String> targetCalls = calls.getCalls(id);

				if (targetCalls != null && targetCalls.contains(name) && n.env.hasLocalFunction(id)) {
					final BoaType retType = n.getOp(0).type;

					// generate stack pushs
					for (final VarDeclStatement v : decls)
						n.insertStatementBefore(ASTFactory.createCall("push",
							n.env,
							new BoaAny(),
							ASTFactory.createIdentifierExpr(this.name + varPrefix + v.getId().getToken(), n.env, new BoaStack(v.type)),
							ASTFactory.createIdentifierExpr(v.getId().getToken(), n.env, v.type)));

					// lift call into tmp
					final VarDeclStatement tmpVar = new VarDeclStatement(
							new Identifier("_rec_tmp_" + counter),
							new Expression(
								new Conjunction(
									new Comparison(
										new SimpleExpr(
											new Term(n.clone())
										)
									)
								)
							)
						);
					tmpVar.type = tmpVar.getInitializer().type = retType;
					tmpVar.env = n.env;

					n.env.set(tmpVar.getId().getToken(), tmpVar.type);
					n.insertStatementBefore(tmpVar);

					// generate stack pops
					for (final VarDeclStatement v : decls)
						n.insertStatementBefore(ASTFactory.createAssignment(v.getId().getToken(),
							ASTFactory.createCall("pop", n.env,
								v.type,
								ASTFactory.createIdentifierExpr(this.name + varPrefix + v.getId().getToken(), n.env, new BoaStack(v.type))).getExpr(),
							retType,
							n.env));

					// replace call with tmp
					((Term)n.getParent()).replaceLhs(new Factor(ASTFactory.createIdentifier("_rec_tmp_" + counter++, n.env)));
				}
			}
		}

		/** {@inheritDoc} */
		@Override
		public void visit(final FunctionExpression n) {
			// don't nest
		}
	}
}
