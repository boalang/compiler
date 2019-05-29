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
import java.util.Map;
import java.util.LinkedHashMap;

import boa.compiler.visitors.AbstractVisitorNoArgNoRet;

import boa.compiler.ast.Factor;
import boa.compiler.ast.Identifier;
import boa.compiler.ast.Program;
import boa.compiler.ast.Comparison;
import boa.compiler.ast.Conjunction;
import boa.compiler.ast.Term;
import boa.compiler.ast.Node;
import boa.compiler.ast.Index;
import boa.compiler.ast.Operand;
import boa.compiler.ast.Table;
import boa.compiler.ast.Selector;
import boa.compiler.ast.statements.VarDeclStatement;
import boa.compiler.ast.statements.SubView;
import boa.compiler.ast.statements.AssignmentStatement;
import boa.compiler.ast.expressions.Expression;
import boa.compiler.ast.expressions.SimpleExpr;
import boa.compiler.ast.literals.ILiteral;
import boa.compiler.ast.types.RowType;
import boa.types.BoaType;
import boa.types.BoaTable;
import boa.types.BoaArray;

/**
 * Create anonymous variables for tables when needed
 * 
 * @author rdyer
 * @author hungc
 */
public class ViewTransformer extends AbstractVisitorNoArgNoRet {
	private int index = -1;
	private Program currentProgram;
	private Map<String, String> newFilterIdMap = new LinkedHashMap<String, String>();
	protected final String varPrefix = "anon_table_";
	private int count = 0;

	/** {@inheritDoc} */
	@Override
	public void visit(final Program n) {
		currentProgram = n;
		int len = n.getStatementsSize();
		int insertCount = 0;
		for (index = 0; index < n.getStatementsSize(); index++) {
			n.getStatement(index).accept(this);
			// if a node was added, dont visit it and
			// dont re-visit the node we were just at
			if (len != n.getStatementsSize()) {
				index += (n.getStatementsSize() - len);
				len = n.getStatementsSize();
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Table n) {
		if (n.getParent() instanceof RowType) return;

		Factor f = (Factor)n.getParent();
		String p = n.getTablePath();

		// check index
		for (int i = 0; i < f.getOps().size(); i++) {
			Node op = f.getOps().get(i);
			ILiteral lit = (ILiteral)((Index)op).getStart().getLhs().getLhs().getLhs().getLhs().getLhs().getOperand();
			p += "/" + lit.getLiteral();
		}

		// if it's cached
		if (newFilterIdMap.containsKey(p)) {
			String id = newFilterIdMap.get(p);
			Operand o = new Identifier(id);
			o.type = n.type;
			o.env = n.env;
			f.getOps().clear();
			f.setOperand(o);
			return;
		}

		// create new decl for the table
		String id = varPrefix + (count++);
		Operand o = new Identifier(id);
		BoaType bt = f.type;
		o.env = n.env;
		o.type = n.type;
		n.env.set(id, bt);
		final VarDeclStatement vds = ASTFactory.createVarDecl(id, n, bt, n.env);
		Factor f2 = (Factor) vds.getInitializer().getLhs().getLhs().getLhs().getLhs().getLhs();
		f2.env = f.env;
		for (int i = 0; i < f.getOpsSize(); i++)
			f2.addOp(f.getOp(i));
		currentProgram.env.set(id, vds.type);
		currentProgram.getStatements().add(index, vds);
		newFilterIdMap.put(p, id);

		f.setOperand(o);
		f.getOps().clear();
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final VarDeclStatement n) {
		Operand op = n.hasInitializer() ? n.getInitializer().getLhs().getLhs().getLhs().getLhs().getLhs().getOperand() : null;

		if (n.hasInitializer())
			n.getInitializer().accept(this);
		if (!(n.type instanceof BoaArray) || !(op != null && op.type instanceof BoaTable))
			return;

		String id = varPrefix + (count++);
		Operand o = new Identifier(id);
		BoaType bt = op.type;
		o.env = n.env;
		o.type = op.type;
		n.env.set(id, bt);
		final VarDeclStatement vds = ASTFactory.createVarDecl(id, op, bt, n.env);

		currentProgram.env.set(id, vds.type);
		currentProgram.getStatements().add(index, vds);

		Factor f = n.getInitializer().getLhs().getLhs().getLhs().getLhs().getLhs();
		f.setOperand(o);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final AssignmentStatement n) {
		Operand op = n.getRhs().getLhs().getLhs().getLhs().getLhs().getLhs().getOperand();
		n.getRhs().accept(this);
		if (!(n.type instanceof BoaArray) || !(op.type instanceof BoaTable))
			return;

		String id = varPrefix + (count++);
		Operand o = new Identifier(id);
		BoaType bt = op.type;
		o.env = n.env;
		o.type = op.type;
		n.env.set(id, bt);
		final VarDeclStatement vds = ASTFactory.createVarDecl(id, op, bt, n.env);

		currentProgram.env.set(id, vds.type);
		currentProgram.getStatements().add(index, vds);

		Factor f = n.getRhs().getLhs().getLhs().getLhs().getLhs().getLhs();
		f.setOperand(o);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final SubView n) {
		return;
	}
}
