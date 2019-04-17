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
	private Map<String, Table> tableMap = new LinkedHashMap<String, Table>();
	private Map<String, String> tableIdMap = new LinkedHashMap<String, String>();
	private Map<String, String> newFilterIdMap = new LinkedHashMap<String, String>();
	private List<VarDeclStatement> newFilterStatements = new ArrayList<VarDeclStatement>();
	protected final String varPrefix = "_table_";
	private int count = 0;

	/** {@inheritDoc} */
	@Override
	public void visit(final Program n) {
		int len = n.getStatementsSize();
		int insertCount = 0;
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
			final VarDeclStatement vds = ASTFactory.createVarDecl(entry.getValue(), o, t.type, n.env);
			n.env.set(entry.getValue(), t.type);
			n.getStatements().add(insertCount, vds);
			insertCount++;
		}

		for (VarDeclStatement vds : newFilterStatements) {
			n.env.set(vds.getId().getToken(), vds.type);
			n.getStatements().add(insertCount, vds);
			insertCount++;
		}
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Table n) {
		Node parent = n.getParent().getParent().getParent().getParent().getParent().getParent().getParent();
		Factor f = (Factor)n.getParent();
		String p = n.getTablePath();
		// if in vardecl or assignment statements or having no indices
		if (parent instanceof VarDeclStatement || parent instanceof AssignmentStatement || f.getOpsSize() == 0) {
			Operand o;
			String id;
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
		else {
			// caching, if @hungc/fileCount[3][5] repeats
			boolean allLiterals = true;
			for (Node op : f.getOps()) {
				if (!(((Index)op).getStart().getLhs().getLhs().getLhs().getLhs().getLhs().getOperand() instanceof ILiteral)) {
					allLiterals = false;
					break;
				}
				ILiteral lit = (ILiteral)((Index)op).getStart().getLhs().getLhs().getLhs().getLhs().getLhs().getOperand();
				p += "/" + lit.getLiteral();
			}
			if (allLiterals && newFilterIdMap.containsKey(p)) {
				String id = newFilterIdMap.get(p);
				Operand o = new Identifier(id);
				o.type = n.type;
				o.env = n.env;
				f.getOps().clear();
				f.setOperand(o);
				return;
			}

			// caching, if @hungc/fileCount repeats
			String id = varPrefix + count;
			if (tableIdMap.containsKey(n.getTablePath()))
				id = tableIdMap.get(n.getTablePath());
			else {
				n.env.set(varPrefix + count, n.type);
				final VarDeclStatement vds = ASTFactory.createVarDecl(varPrefix + count, n, n.type, n.env);
				newFilterStatements.add(vds);
				count++;
			}

			Operand o = new Identifier(id);
			Operand o2 = new Identifier(varPrefix + count);
			o.type = n.type;
			o.env = n.env;
			o2.type = f.type;
			o2.env = n.env;

			n.env.set(varPrefix + count, f.type);
			final VarDeclStatement vds2 = ASTFactory.createVarDecl(varPrefix + count, o, f.type, n.env);
			vds2.getInitializer().getLhs().getLhs().getLhs().getLhs().getLhs().env = n.env;
			for (Node op : f.getOps())
				vds2.getInitializer().getLhs().getLhs().getLhs().getLhs().getLhs().addOp(op);
			newFilterStatements.add(vds2);

			if (allLiterals)
				newFilterIdMap.put(p, varPrefix + count);
			count++;

			f.getOps().clear();
			f.setOperand(o2);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final SubView n) {
		return;
	}
}
