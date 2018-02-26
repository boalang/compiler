/*
 * Copyright 2017, Hridesh Rajan, Robert Dyer
 *                 Iowa State University of Science and Technology
 *                 and Bowling Green State University
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
package boa.compiler.visitors;

import java.io.PrintStream;

import boa.compiler.ast.*;
import boa.compiler.ast.expressions.*;
import boa.compiler.ast.literals.*;
import boa.compiler.ast.statements.*;
import boa.compiler.ast.types.*;

/*
 * A debugging visitor class that pretty prints the Boa AST.
 *
 * @author rdyer
 */
public class PrettyPrintVisitor extends AbstractVisitorNoArgNoRet {
	private int indent = 0;
	final private PrintStream strm;

	public PrettyPrintVisitor() {
		this(System.out);
	}

	public PrettyPrintVisitor(final PrintStream strm) {
		this.strm = strm;
	}

	private void indent() {
		for (int i = 0; i < indent; i++)
			strm.print("    ");
	}

	// dont actually indent blocks
	// but many places a block can appear, statements could also appear
	// and we want to indent statements
	public void indentBlock(final Node n) {
		if (!(n instanceof Block))
			indent++;
	}

	public void outdentBlock(final Node n) {
		if (!(n instanceof Block))
			indent--;
	}

	/** {@inheritDoc} */
	@Override
	public void initialize() {
		indent = 0;
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Call n) {
		strm.print("(");
		boolean seen = false;
		for (final Expression e : n.getArgs()) {
			if (seen) strm.print(", ");
			else seen = true;
			e.accept(this);
		}
		strm.print(")");
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Comparison n) {
		n.getLhs().accept(this);
		if (n.hasOp()) {
			strm.print(" " + n.getOp() + " ");
			n.getRhs().accept(this);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Component n) {
		if (n.hasIdentifier()) {
			n.getIdentifier().accept(this);
			strm.print(": ");
		}
		n.getType().accept(this);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Composite n) {
		strm.print("{ ");
		if (n.isEmpty())
			strm.print(": ");
		boolean seen = false;
		for (final Pair p : n.getPairs()) {
			if (seen) strm.print(", ");
			else seen = true;
			p.accept(this);
		}
		seen = false;
		for (final Expression e : n.getExprs()) {
			if (seen) strm.print(", ");
			else seen = true;
			e.accept(this);
		}
		strm.print("}");
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Conjunction n) {
		n.getLhs().accept(this);
		for (int i = 0; i < n.getOpsSize(); i++) {
			strm.print(" " + n.getOp(i) + " ");
			n.getRhs(i).accept(this);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Factor n) {
		n.getOperand().accept(this);
		for (final Node op : n.getOps())
			op.accept(this);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Identifier n) {
		strm.print(n.getToken());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Index n) {
		strm.print("[");
		n.getStart().accept(this);
		if (n.hasEnd()) {
			strm.print(" : ");
			n.getEnd().accept(this);
		}
		strm.print("]");
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Pair n) {
		n.getExpr1().accept(this);
		strm.print(" : ");
		n.getExpr2().accept(this);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Selector n) {
		strm.print(".");
		n.getId().accept(this);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Term n) {
		n.getLhs().accept(this);
		for (int i = 0; i < n.getOpsSize(); i++) {
			strm.print(" " + n.getOp(i) + " ");
			n.getRhs(i).accept(this);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final UnaryFactor n) {
		strm.print(n.getOp());
		n.getFactor().accept(this);
	}

	//
	// statements
	//
	/** {@inheritDoc} */
	@Override
	public void visit(final AssignmentStatement n) {
		indent();
		n.getLhs().accept(this);
		strm.print(" = ");
		n.getRhs().accept(this);
		strm.println(";");
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Block n) {
		strm.println("{");
		indent++;
		super.visit(n);
		indent--;
		indent();
		strm.println("}");
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final BreakStatement n) {
		indent();
		strm.println("break;");
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final ContinueStatement n) {
		indent();
		strm.println("continue;");
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final DoStatement n) {
		indent();
		strm.println("do");
		n.getBody().accept(this);
		indent();
		strm.print("while (");
		n.getCondition().accept(this);
		strm.println(");");
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final EmitStatement n) {
		indent();
		n.getId().accept(this);
		if (n.getIndicesSize() > 0)
			for (final Expression e : n.getIndices()) {
				strm.print("[");
				e.accept(this);
				strm.print("]");
			}
		strm.print(" << ");
		n.getValue().accept(this);
		if (n.hasWeight()) {
			strm.print(" weight ");
			n.getWeight().accept(this);
		}
		strm.println(";");
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final ExistsStatement n) {
		indent();
		strm.print("exists (");
		n.getVar().accept(this);
		strm.print("; ");
		n.getCondition().accept(this);
		strm.print(") ");
		indentBlock(n.getBody());
		n.getBody().accept(this);
		outdentBlock(n.getBody());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final ExprStatement n) {
		indent();
		n.getExpr().accept(this);
		strm.println(";");
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final ForeachStatement n) {
		indent();
		strm.print("foreach (");
		n.getVar().accept(this);
		strm.print("; ");
		n.getCondition().accept(this);
		strm.print(") ");
		indentBlock(n.getBody());
		n.getBody().accept(this);
		outdentBlock(n.getBody());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final ForStatement n) {
		indent();
		strm.print("for (");
		if (n.hasInit()) n.getInit().accept(this);
		else strm.print(";");
		strm.print(" ");
		if (n.hasCondition()) n.getCondition().accept(this);
		else strm.print(";");
		strm.print(" ");
		if (n.hasUpdate()) n.getUpdate().accept(this);
		strm.print(") ");
		indentBlock(n.getBody());
		n.getBody().accept(this);
		outdentBlock(n.getBody());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final IfAllStatement n) {
		indent();
		strm.print("ifall (");
		n.getVar().accept(this);
		strm.print("; ");
		n.getCondition().accept(this);
		strm.print(") ");
		indentBlock(n.getBody());
		n.getBody().accept(this);
		outdentBlock(n.getBody());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final IfStatement n) {
		indent();
		strm.print("if (");
		n.getCondition().accept(this);
		strm.print(") ");
		indentBlock(n.getBody());
		n.getBody().accept(this);
		if (n.hasElse()) {
			outdentBlock(n.getBody());
			indent();
			strm.print("else ");
			indentBlock(n.getElse());
			n.getElse().accept(this);
			outdentBlock(n.getElse());
		} else {
			outdentBlock(n.getBody());
		}
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final PostfixStatement n) {
		indent();
		n.getExpr().accept(this);
		strm.print(n.getOp());
		strm.println(";");
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final ReturnStatement n) {
		indent();
		strm.print("return");
		if (n.hasExpr()) {
			strm.print(" ");
			n.getExpr().accept(this);
		}
		strm.println(";");
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final StopStatement n) {
		indent();
		strm.println("stop;");
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final SwitchCase n) {
		indent();
		if (n.isDefault())
			strm.print("default: ");
		else {
			boolean seen = false;
			strm.print("case ");
			for (final Expression e : n.getCases()) {
				if (seen) strm.print(", ");
				else seen = true;
				e.accept(this);
			}
			strm.print(": ");
		}
		indentBlock(n.getBody());
		n.getBody().accept(this);
		outdentBlock(n.getBody());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final SwitchStatement n) {
		indent();
		strm.print("switch (");
		n.getCondition().accept(this);
		strm.println(") {");
		indent++;
		for (final SwitchCase sc : n.getCases())
			sc.accept(this);
		n.getDefault().accept(this);
		indent--;
		indent();
		strm.println("}");
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final TypeDecl n) {
		indent();
		strm.print("type ");
		n.getId().accept(this);
		strm.print(" =");
		n.getType().accept(this);
		strm.println(";");
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final VarDeclStatement n) {
		indent();
		if (n.isStatic()) strm.print("static ");
		n.getId().accept(this);
		if (n.hasType()) {
			strm.print(": ");
			n.getType().accept(this);
			if (n.hasInitializer())
				strm.print(" = ");
		} else {
			strm.print(" := ");
		}
		if (n.hasInitializer())
			n.getInitializer().accept(this);
		strm.println(";");
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final VisitStatement n) {
		indent();
		if (n.isBefore()) strm.print("before ");
		else strm.print("after ");
		if (n.hasWildcard()) strm.print("_");
		else if (n.hasComponent()) n.getComponent().accept(this);
		else {
			boolean seen = false;
			for (final Identifier id : n.getIdList()) {
				if (seen) strm.print(", ");
				else seen = true;
				id.accept(this);
			}
		}
		strm.print(" -> ");
		n.getBody().accept(this);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final WhileStatement n) {
		indent();
		strm.print("while (");
		n.getCondition().accept(this);
		strm.println(")");
		n.getBody().accept(this);
	}

	//
	// expressions
	//
	/** {@inheritDoc} */
	@Override
	public void visit(final Expression n) {
		n.getLhs().accept(this);
		for (int i = 0; i < n.getRhsSize(); i++) {
			strm.print(" || ");
			n.getRhs(i).accept(this);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final FunctionExpression n) {
		n.getType().accept(this);
		indentBlock(n.getBody());
		n.getBody().accept(this);
		outdentBlock(n.getBody());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final ParenExpression n) {
		strm.print("(");
		n.getExpression().accept(this);
		strm.print(")");
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final SimpleExpr n) {
		n.getLhs().accept(this);
		for (int i = 0; i < n.getOpsSize(); i++) {
			strm.print(" " + n.getOp(i) + " ");
			n.getRhs(i).accept(this);
		}
	}

	//
	// types
	//
	/** {@inheritDoc} */
	@Override
	public void visit(final ArrayType n) {
		strm.print("array of ");
		n.getValue().accept(this);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final FunctionType n) {
		strm.print("function (");
		boolean seen = false;
		for (final Component c : n.getArgs()) {
			if (seen) strm.print(", ");
			else seen = true;
			c.accept(this);
		}
		strm.print(")");
		if (n.hasType()) {
			strm.print(" : ");
			n.getType().accept(this);
		}
		strm.println();
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final MapType n) {
		strm.print("map[");
		n.getIndex().accept(this);
		strm.print("] of ");
		n.getValue().accept(this);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final OutputType n) {
		strm.print("output ");
		n.getId().accept(this);
		if (n.getArgsSize() > 0) {
			strm.print("(");
			boolean seen = false;
			for (final Expression e : n.getArgs()) {
				if (seen) strm.print(", ");
				else seen = true;
				e.accept(this);
			}
			strm.print(")");
		}
		if (n.getIndicesSize() > 0) {
			for (final Component c : n.getIndices()) {
				strm.print("[");
				c.accept(this);
				strm.print("]");
			}
		}
		strm.print(" of ");
		n.getType().accept(this);
		if (n.hasWeight()) {
			strm.print(" weight ");
			n.getWeight().accept(this);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final StackType n) {
		strm.print("stack of ");
		n.getValue().accept(this);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final SetType n) {
		strm.print("set of ");
		n.getValue().accept(this);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final TupleType n) {
		strm.print("{ ");
		boolean seen = false;
		for (final Component c : n.getMembers()) {
			if (seen) strm.print(", ");
			else seen = true;
			c.accept(this);
		}
		strm.print(" }");
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final VisitorType n) {
		strm.print("visitor ");
	}

	//
	// literals
	//
	/** {@inheritDoc} */
	@Override
	public void visit(final CharLiteral n) {
		strm.print(n.getLiteral());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final FloatLiteral n) {
		strm.print(n.getLiteral());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final IntegerLiteral n) {
		strm.print(n.getLiteral());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final StringLiteral n) {
		strm.print(n.getLiteral());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final TimeLiteral n) {
		strm.print(n.getLiteral());
	}
}
