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
public class PrettyPrintVisitor extends AbstractVisitorNoArg {
	private int indent = 0;

	private void indent() {
		for (int i = 0; i < indent; i++)
			System.out.print("    ");
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
		System.out.print("(");
		boolean seen = false;
		for (final Expression e : n.getArgs()) {
			if (seen) System.out.print(", ");
			else seen = true;
			e.accept(this);
		}
		System.out.print(")");
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Comparison n) {
		n.getLhs().accept(this);
		if (n.hasOp()) {
			System.out.print(" " + n.getOp() + " ");
			n.getRhs().accept(this);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Component n) {
		if (n.hasIdentifier()) {
			n.getIdentifier().accept(this);
			System.out.print(": ");
		}
		n.getType().accept(this);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Composite n) {
		System.out.print("{ ");
		if (n.isEmpty())
			System.out.print(": ");
		boolean seen = false;
		for (final Pair p : n.getPairs()) {
			if (seen) System.out.print(", ");
			else seen = true;
			p.accept(this);
		}
		seen = false;
		for (final Expression e : n.getExprs()) {
			if (seen) System.out.print(", ");
			else seen = true;
			e.accept(this);
		}
		System.out.print("}");
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Conjunction n) {
		n.getLhs().accept(this);
		for (int i = 0; i < n.getOpsSize(); i++) {
			System.out.print(" " + n.getOp(i) + " ");
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
		System.out.print(n.getToken());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Index n) {
		System.out.print("[");
		n.getStart().accept(this);
		if (n.hasEnd()) {
			System.out.print(" : ");
			n.getEnd().accept(this);
		}
		System.out.print("]");
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Pair n) {
		n.getExpr1().accept(this);
		System.out.print(" : ");
		n.getExpr2().accept(this);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Selector n) {
		System.out.print(".");
		n.getId().accept(this);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Term n) {
		n.getLhs().accept(this);
		for (int i = 0; i < n.getOpsSize(); i++) {
			System.out.print(" " + n.getOp(i) + " ");
			n.getRhs(i).accept(this);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final UnaryFactor n) {
		System.out.print(n.getOp());
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
		System.out.print(" = ");
		n.getRhs().accept(this);
		System.out.println(";");
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Block n) {
		System.out.println("{");
		indent++;
		super.visit(n);
		indent--;
		indent();
		System.out.println("}");
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final BreakStatement n) {
		indent();
		System.out.println("break;");
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final ContinueStatement n) {
		indent();
		System.out.println("continue;");
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final DoStatement n) {
		indent();
		System.out.println("do");
		n.getBody().accept(this);
		indent();
		System.out.print("while (");
		n.getCondition().accept(this);
		System.out.println(");");
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final EmitStatement n) {
		indent();
		n.getId().accept(this);
		if (n.getIndicesSize() > 0)
			for (final Expression e : n.getIndices()) {
				System.out.print("[");
				e.accept(this);
				System.out.print("]");
			}
		System.out.print(" << ");
		n.getValue().accept(this);
		if (n.hasWeight()) {
			System.out.print(" weight ");
			n.getWeight().accept(this);
		}
		System.out.println(";");
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final ExistsStatement n) {
		indent();
		System.out.print("exists (");
		n.getVar().accept(this);
		System.out.print("; ");
		n.getCondition().accept(this);
		System.out.print(") ");
		indentBlock(n.getBody());
		n.getBody().accept(this);
		outdentBlock(n.getBody());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final ExprStatement n) {
		indent();
		n.getExpr().accept(this);
		System.out.println(";");
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final ForeachStatement n) {
		indent();
		System.out.print("foreach (");
		n.getVar().accept(this);
		System.out.print("; ");
		n.getCondition().accept(this);
		System.out.print(") ");
		indentBlock(n.getBody());
		n.getBody().accept(this);
		outdentBlock(n.getBody());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final ForStatement n) {
		indent();
		System.out.print("for (");
		if (n.hasInit()) n.getInit().accept(this);
		else System.out.print(";");
		System.out.print(" ");
		if (n.hasCondition()) n.getCondition().accept(this);
		else System.out.print(";");
		System.out.print(" ");
		if (n.hasUpdate()) n.getUpdate().accept(this);
		System.out.print(") ");
		indentBlock(n.getBody());
		n.getBody().accept(this);
		outdentBlock(n.getBody());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final IfAllStatement n) {
		indent();
		System.out.print("ifall (");
		n.getVar().accept(this);
		System.out.print("; ");
		n.getCondition().accept(this);
		System.out.print(") ");
		indentBlock(n.getBody());
		n.getBody().accept(this);
		outdentBlock(n.getBody());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final IfStatement n) {
		indent();
		System.out.print("if (");
		n.getCondition().accept(this);
		System.out.print(") ");
		indentBlock(n.getBody());
		n.getBody().accept(this);
		if (n.hasElse()) {
			outdentBlock(n.getBody());
			indent();
			System.out.println("else ");
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
		System.out.print(n.getOp());
		System.out.println(";");
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final ReturnStatement n) {
		indent();
		System.out.print("return");
		if (n.hasExpr()) {
			System.out.print(" ");
			n.getExpr().accept(this);
		}
		System.out.println(";");
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final StopStatement n) {
		indent();
		System.out.println("stop;");
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final SwitchCase n) {
		indent();
		if (n.isDefault())
			System.out.print("default: ");
		else {
			boolean seen = false;
			for (final Expression e : n.getCases()) {
				if (seen) System.out.print(", ");
				else seen = true;
				e.accept(this);
			}
			System.out.print(": ");
		}
		indentBlock(n.getBody());
		n.getBody().accept(this);
		outdentBlock(n.getBody());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final SwitchStatement n) {
		indent();
		System.out.print("switch (");
		n.getCondition().accept(this);
		System.out.println(") {");
		indent++;
		for (final SwitchCase sc : n.getCases())
			sc.accept(this);
		n.getDefault().accept(this);
		indent--;
		indent();
		System.out.println("}");
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final TypeDecl n) {
		indent();
		System.out.print("type ");
		n.getId().accept(this);
		System.out.print(" =");
		n.getType().accept(this);
		System.out.println(";");
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final VarDeclStatement n) {
		indent();
		if (n.isStatic()) System.out.print("static ");
		n.getId().accept(this);
		if (n.hasType()) {
			System.out.print(": ");
			n.getType().accept(this);
			if (n.hasInitializer())
				System.out.print(" = ");
		} else {
			System.out.print(" := ");
		}
		if (n.hasInitializer())
			n.getInitializer().accept(this);
		System.out.println(";");
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final VisitStatement n) {
		indent();
		if (n.isBefore()) System.out.print("before ");
		else System.out.print("after ");
		if (n.hasWildcard()) System.out.print("_");
		else if (n.hasComponent()) n.getComponent().accept(this);
		else {
			boolean seen = false;
			for (final Identifier id : n.getIdList()) {
				if (seen) System.out.print(", ");
				else seen = true;
				id.accept(this);
			}
		}
		System.out.print(" -> ");
		n.getBody().accept(this);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final WhileStatement n) {
		indent();
		System.out.print("while (");
		n.getCondition().accept(this);
		System.out.println(")");
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
			System.out.print(" || ");
			n.getRhs(i).accept(this);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final ParenExpression n) {
		System.out.print("(");
		n.getExpression().accept(this);
		System.out.print(")");
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final SimpleExpr n) {
		n.getLhs().accept(this);
		for (int i = 0; i < n.getOpsSize(); i++) {
			System.out.print(" " + n.getOp(i) + " ");
			n.getRhs(i).accept(this);
		}
	}

	//
	// types
	//
	/** {@inheritDoc} */
	@Override
	public void visit(final ArrayType n) {
		System.out.print("array of ");
		n.getValue().accept(this);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final FunctionType n) {
		System.out.print("function (");
		boolean seen = false;
		for (final Component c : n.getArgs()) {
			if (seen) System.out.print(", ");
			else seen = true;
			c.accept(this);
		}
		System.out.print(")");
		if (n.hasType()) {
			System.out.print(" : ");
			n.getType().accept(this);
		}
		System.out.println();
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final MapType n) {
		System.out.print("map[");
		n.getIndex().accept(this);
		System.out.print("] of ");
		n.getValue().accept(this);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final OutputType n) {
		System.out.print("output ");
		n.getId().accept(this);
		if (n.getArgsSize() > 0) {
			System.out.print("(");
			boolean seen = false;
			for (final Expression e : n.getArgs()) {
				if (seen) System.out.print(", ");
				else seen = true;
				e.accept(this);
			}
			System.out.print(")");
		}
		if (n.getIndicesSize() > 0) {
			for (final Component c : n.getIndices()) {
				System.out.print("[");
				c.accept(this);
				System.out.print("]");
			}
		}
		System.out.print(" of ");
		n.getType().accept(this);
		if (n.hasWeight()) {
			System.out.print("weight ");
			n.getWeight().accept(this);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final StackType n) {
		System.out.print("stack of ");
		n.getValue().accept(this);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final SetType n) {
		System.out.print("set of ");
		n.getValue().accept(this);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final TupleType n) {
		System.out.print("{ ");
		boolean seen = false;
		for (final Component c : n.getMembers()) {
			if (seen) System.out.print(", ");
			else seen = true;
			c.accept(this);
		}
		System.out.print(" }");
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final VisitorType n) {
		System.out.print("visitor ");
	}

	//
	// literals
	//
	/** {@inheritDoc} */
	@Override
	public void visit(final CharLiteral n) {
		System.out.print(n.getLiteral());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final FloatLiteral n) {
		System.out.print(n.getLiteral());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final IntegerLiteral n) {
		System.out.print(n.getLiteral());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final StringLiteral n) {
		System.out.print(n.getLiteral());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final TimeLiteral n) {
		System.out.print(n.getLiteral());
	}
}
