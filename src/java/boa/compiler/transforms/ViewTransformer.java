/*
 * Copyright 2019, Robert Dyer, Che Shian Hung,
 *                 Bowling Green State University
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
import boa.compiler.ast.Factor;
import boa.compiler.ast.Identifier;
import boa.compiler.ast.Program;
import boa.compiler.ast.expressions.ParenExpression;
import boa.compiler.ast.literals.IntegerLiteral;
import boa.compiler.ast.statements.Block;
import boa.compiler.ast.statements.EmitStatement;
import boa.compiler.ast.statements.IfStatement;
import boa.compiler.ast.statements.VarDeclStatement;
import boa.compiler.ast.statements.SubView;
import boa.compiler.ast.types.OutputType;
import boa.compiler.visitors.AbstractVisitorNoArgNoRet;
import boa.types.BoaInt;

import boa.compiler.ast.statements.AssignmentStatement;
import boa.compiler.ast.expressions.Expression;
import boa.compiler.ast.expressions.SimpleExpr;
import boa.compiler.ast.Comparison;
import boa.compiler.ast.Conjunction;
import boa.compiler.ast.Term;
import boa.compiler.ast.Node;
import boa.compiler.ast.Index;
import boa.compiler.ast.Operand;
import boa.compiler.ast.Table;
import boa.types.BoaTable;

/**
 * Create anonymous variables for tables when needed
 * 
 * @author rdyer
 * @author hungc
 */
public class ViewTransformer extends AbstractVisitorNoArgNoRet {
	private int statementCounter = -1;
	protected final String varPrefix = "_table_";

	/** {@inheritDoc} */
	@Override
	public void visit(final Program n) {
		int len = n.getStatementsSize();
		for (statementCounter = 0; statementCounter < n.getStatementsSize(); statementCounter++) {
			n.getStatement(statementCounter).accept(this);
			// if a node was added, dont visit it and
			// dont re-visit the node we were just at
			if (len != n.getStatementsSize()) {
				statementCounter += (n.getStatementsSize() - len);
				len = n.getStatementsSize();
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final VarDeclStatement n) {
		if (n.type instanceof BoaTable && n.hasInitializer()) {
			Factor f = n.getInitializer().getLhs().getLhs().getLhs().getLhs().getLhs();
			// v := @hungc/fileCount[3][5];
			if (f.getOperand() instanceof Table && f.getOpsSize() > 0 && f.getOp(0) instanceof Index) {
				Program p = (Program)n.getParent();
				Operand o = new Identifier(varPrefix + n.getId().getToken());
				o.type = f.getOperand().type;
				o.env = f.getOperand().env;
				n.env.set(varPrefix + n.getId().getToken(), f.getOperand().type);
				n.env.set(n.getId().getToken(), n.type);
				
				final VarDeclStatement vds = ASTFactory.createVarDecl(varPrefix + n.getId().getToken(), f.getOperand(), f.getOperand().type, n.env);
				final VarDeclStatement vds2 = ASTFactory.createVarDecl(n.getId().getToken(), o, n.type, n.env);
				vds2.getInitializer().getLhs().getLhs().getLhs().getLhs().getLhs().env = n.env;
				for (Node op : f.getOps()) {
					vds2.getInitializer().getLhs().getLhs().getLhs().getLhs().getLhs().addOp(op);
				}

				p.getStatements().set(statementCounter, vds2);
				p.getStatements().add(statementCounter, vds);
				p.env.set(varPrefix + n.getId().getToken(), f.getOperand().type);
				p.env.set(n.getId().getToken(), n.type);
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final AssignmentStatement n) {
		if (n.type instanceof BoaTable) {
			Factor rhf = n.getRhs().getLhs().getLhs().getLhs().getLhs().getLhs();
			// v = @hungc/fileCount[3][5];
			if (rhf.getOperand() instanceof Table && rhf.getOpsSize() > 0 && rhf.getOp(0) instanceof Index) {
				Program p = (Program)n.getParent();
				Identifier lhs = (Identifier) n.getLhs().getOperand();
				Operand o = new Identifier(varPrefix + lhs.getToken());
				o.type = rhf.getOperand().type;
				o.env = rhf.getOperand().env;
				n.env.set(varPrefix + lhs.getToken(), rhf.getOperand().type);
				n.env.set(lhs.getToken(), n.type);

				final VarDeclStatement vds = ASTFactory.createVarDecl(varPrefix + lhs.getToken(), rhf.getOperand(), rhf.getOperand().type, n.env);
				final AssignmentStatement as = new AssignmentStatement(n.getLhs(), new Expression(new Conjunction(new Comparison(new SimpleExpr(new Term(new Factor(o)))))));
				as.type = as.getRhs().type = n.type;
				as.env = n.env;
				as.getRhs().getLhs().getLhs().getLhs().getLhs().getLhs().env = n.env;
				for (Node op : rhf.getOps()) {
					as.getRhs().getLhs().getLhs().getLhs().getLhs().getLhs().addOp(op);
				}

				p.getStatements().set(statementCounter, as);
				p.getStatements().add(statementCounter, vds);
				p.env.set(varPrefix + lhs.getToken(), rhf.getOperand().type);
				p.env.set(lhs.getToken(), n.type);
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final SubView n) {
		return;
	}
}
