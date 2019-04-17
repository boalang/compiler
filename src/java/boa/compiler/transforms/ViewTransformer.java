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
import boa.compiler.ast.statements.VarDeclStatement;
import boa.compiler.ast.statements.SubView;
import boa.compiler.ast.statements.AssignmentStatement;
import boa.compiler.ast.expressions.Expression;
import boa.compiler.ast.expressions.SimpleExpr;
import boa.types.BoaType;
import boa.types.BoaTable;

/**
 * Create anonymous variables for tables when needed
 * 
 * @author rdyer
 * @author hungc
 */
public class ViewTransformer extends AbstractVisitorNoArgNoRet {
	private Map<String, Table> tableMap = new LinkedHashMap<String, Table>();
	private Map<String, String> tableIdMap = new LinkedHashMap<String, String>();
	protected final String varPrefix = "_table_";
	private int count = 0;

	/** {@inheritDoc} */
	@Override
	public void visit(final Program n) {
		int len = n.getStatementsSize();
		for (int i = 0; i < n.getStatementsSize(); i++) {
			n.getStatement(i).accept(this);
			// if a node was added, dont visit it and
			// dont re-visit the node we were just at
			if (len != n.getStatementsSize()) {
				i += (n.getStatementsSize() - len);
				len = n.getStatementsSize();
			}
		}
		for (Map.Entry<String,String> entry: tableIdMap.entrySet()) {
			Operand o = new Table(entry.getKey());
			Table t = tableMap.get(entry.getKey());
			o.type = t.type;
			o.env = t.env;
			VarDeclStatement vds = ASTFactory.createVarDecl(entry.getValue(), o, t.type, n.env);
			n.env.set(entry.getValue(), t.type);
			n.getStatements().add(0, vds);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Table n) {
		Operand o;
		String id;
		Factor f = (Factor)n.getParent();
		String p = n.getTablePath();
		if (tableIdMap.containsKey(p))
			id = tableIdMap.get(p);
		else {
			id = varPrefix + count;
			count++;
			tableIdMap.put(p, id);
			tableMap.put(p, n);
		}
		o = new Identifier(id);
		o.env = n.env;
		o.type = n.type;
		f.setOperand(o);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final SubView n) {
		return;
	}
}
