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
import boa.types.BoaType;
import boa.types.BoaTable;

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
		Node parent = n.getParent().getParent().getParent().getParent().getParent().getParent().getParent();
		Factor f = (Factor)n.getParent();
		String p = n.getTablePath();

		// if have indices
		int splitCount = -1;
		for (int i = 0; i < f.getOps().size(); i++) {
			Node op = f.getOps().get(i);
			if (op instanceof Index && !(((Index)op).getStart().getLhs().getLhs().getLhs().getLhs().getLhs().getOperand() instanceof ILiteral)) {
				splitCount = i;
				break;
			}
			else if (op instanceof Selector) {
				p += "/" + ((Selector)op).getId().getToken();
				continue;
			}

			ILiteral lit = (ILiteral)((Index)op).getStart().getLhs().getLhs().getLhs().getLhs().getLhs().getOperand();
			p += "/" + lit.getLiteral();
		}

		// if it's cached
		if (newFilterIdMap.containsKey(p)) {
			String id = newFilterIdMap.get(p);
			Operand o = new Identifier(id);
			o.type = n.type;
			o.env = n.env;
			if (splitCount == -1)
				f.getOps().clear();
			else
				for (int i = 0; i < splitCount; i++)
					f.getOps().remove(0);
			f.setOperand(o);
			return;
		}

		// create new decl for the table
		String id = varPrefix + (count++);
		newFilterIdMap.put(p, id);
		Operand o = new Identifier(id);
		BoaType bt = splitCount == -1  ? f.type : (splitCount == 0 ? n.type : f.getOp(splitCount - 1).type);
		o.env = n.env;
		o.type = n.type;
		n.env.set(id, bt);
		final VarDeclStatement vds = ASTFactory.createVarDecl(id, n, bt, n.env);
		Factor f2 = (Factor) vds.getInitializer().getLhs().getLhs().getLhs().getLhs().getLhs();
		f2.env = f.env;
		if (f.getOpsSize() != 0 && splitCount != 0) {
			int limit = splitCount == -1 ? f.getOpsSize() : splitCount;
			for (int i = 0; i < limit; i++)
				f2.addOp(f.getOp(i));
		}
		currentProgram.env.set(id, vds.type);
		currentProgram.getStatements().add(index, vds);

		// if second stmt needed
		if (f.getOpsSize() > 0 && splitCount != -1) {
			String id2 = varPrefix + (count++);
			Operand o2 = new Identifier(id2);
			o2.env = n.env;
			o2.type = n.type;
			n.env.set(id2, f.type);
			final VarDeclStatement vds2 = ASTFactory.createVarDecl(id2, o, f.type, n.env);
			Factor f3 = (Factor) vds2.getInitializer().getLhs().getLhs().getLhs().getLhs().getLhs();
			f3.env = f.env;
			for (int i = splitCount; i < f.getOpsSize(); i++)
				f3.addOp(f.getOp(i));

			for (int i = 0; i < splitCount; i++)
				f.getOps().remove(0);

			currentProgram.env.set(id2, vds2.type);
			currentProgram.getStatements().add(index + 1, vds2);
			f.setOperand(o2);
			f.getOps().clear();
			return;
		}

		f.setOperand(o);
		f.getOps().clear();
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final SubView n) {
		return;
	}
}
