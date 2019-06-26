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

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
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
public class PrettyPrintVisitor extends AbstractVisitorNoArg {
	private int indent = 0;
	private PrintStream stream = System.out;

	private void indent() {
		for (int i = 0; i < indent; i++)
			stream.print("    ");
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

	public String startAndReturn(final Node n) {
		final ByteArrayOutputStream b = new ByteArrayOutputStream();
		this.stream = new PrintStream(b);
		super.start(n);
		this.stream.flush();
		return b.toString();
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Call n) {
		stream.print("(");
		boolean seen = false;
		for (final Expression e : n.getArgs()) {
			if (seen) stream.print(", ");
			else seen = true;
			e.accept(this);
		}
		stream.print(")");
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Comparison n) {
		n.getLhs().accept(this);
		if (n.hasOp()) {
			stream.print(" " + n.getOp() + " ");
			n.getRhs().accept(this);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Component n) {
		if (n.hasIdentifier()) {
			n.getIdentifier().accept(this);
			stream.print(": ");
		}
		n.getType().accept(this);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Composite n) {
		stream.print("{ ");
		if (n.isEmpty())
			stream.print(": ");
		boolean seen = false;
		for (final Pair p : n.getPairs()) {
			if (seen) stream.print(", ");
			else seen = true;
			p.accept(this);
		}
		seen = false;
		for (final Expression e : n.getExprs()) {
			if (seen) stream.print(", ");
			else seen = true;
			e.accept(this);
		}
		stream.print("}");
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Conjunction n) {
		n.getLhs().accept(this);
		for (int i = 0; i < n.getOpsSize(); i++) {
			stream.print(" " + n.getOp(i) + " ");
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
		stream.print(n.getToken());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Index n) {
		stream.print("[");
		n.getStart().accept(this);
		if (n.hasEnd()) {
			stream.print(" : ");
			n.getEnd().accept(this);
		}
		stream.print("]");
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Pair n) {
		n.getExpr1().accept(this);
		stream.print(" : ");
		n.getExpr2().accept(this);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Selector n) {
		stream.print(".");
		n.getId().accept(this);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Term n) {
		n.getLhs().accept(this);
		for (int i = 0; i < n.getOpsSize(); i++) {
			stream.print(" " + n.getOp(i) + " ");
			n.getRhs(i).accept(this);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final UnaryFactor n) {
		stream.print(n.getOp());
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
		stream.print(" = ");
		n.getRhs().accept(this);
		stream.println(";");
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Block n) {
		stream.println("{");
		indent++;
		super.visit(n);
		indent--;
		indent();
		stream.println("}");
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final BreakStatement n) {
		indent();
		stream.println("break;");
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final ContinueStatement n) {
		indent();
		stream.println("continue;");
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final DoStatement n) {
		indent();
		stream.println("do");
		n.getBody().accept(this);
		indent();
		stream.print("while (");
		n.getCondition().accept(this);
		stream.println(");");
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final EmitStatement n) {
		indent();
		n.getId().accept(this);
		if (n.getIndicesSize() > 0)
			for (final Expression e : n.getIndices()) {
				stream.print("[");
				e.accept(this);
				stream.print("]");
			}
		stream.print(" << ");
		n.getValue().accept(this);
		if (n.hasWeight()) {
			stream.print(" weight ");
			n.getWeight().accept(this);
		}
		stream.println(";");
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final ExistsStatement n) {
		indent();
		stream.print("exists (");
		n.getVar().accept(this);
		stream.print("; ");
		n.getCondition().accept(this);
		stream.print(") ");
		indentBlock(n.getBody());
		n.getBody().accept(this);
		outdentBlock(n.getBody());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final ExprStatement n) {
		indent();
		n.getExpr().accept(this);
		stream.println(";");
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final ForeachStatement n) {
		indent();
		stream.print("foreach (");
		n.getVar().accept(this);
		stream.print("; ");
		n.getCondition().accept(this);
		stream.print(") ");
		indentBlock(n.getBody());
		n.getBody().accept(this);
		outdentBlock(n.getBody());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final ForStatement n) {
		indent();
		stream.print("for (");
		if (n.hasInit()) n.getInit().accept(this);
		else stream.print(";");
		stream.print(" ");
		if (n.hasCondition()) n.getCondition().accept(this);
		else stream.print(";");
		stream.print(" ");
		if (n.hasUpdate()) n.getUpdate().accept(this);
		stream.print(") ");
		indentBlock(n.getBody());
		n.getBody().accept(this);
		outdentBlock(n.getBody());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final IfAllStatement n) {
		indent();
		stream.print("ifall (");
		n.getVar().accept(this);
		stream.print("; ");
		n.getCondition().accept(this);
		stream.print(") ");
		indentBlock(n.getBody());
		n.getBody().accept(this);
		outdentBlock(n.getBody());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final IfStatement n) {
		indent();
		stream.print("if (");
		n.getCondition().accept(this);
		stream.print(") ");
		indentBlock(n.getBody());
		n.getBody().accept(this);
		if (n.hasElse()) {
			outdentBlock(n.getBody());
			indent();
			stream.println("else ");
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
		stream.print(n.getOp());
		stream.println(";");
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final ReturnStatement n) {
		indent();
		stream.print("return");
		if (n.hasExpr()) {
			stream.print(" ");
			n.getExpr().accept(this);
		}
		stream.println(";");
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final StopStatement n) {
		indent();
		stream.println("stop;");
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final SwitchCase n) {
		indent();
		if (n.isDefault())
			stream.print("default: ");
		else {
			boolean seen = false;
			stream.print("case ");
			for (final Expression e : n.getCases()) {
				if (seen) stream.print(", ");
				else seen = true;
				e.accept(this);
			}
			stream.print(": ");
		}
		indentBlock(n.getBody());
		n.getBody().accept(this);
		outdentBlock(n.getBody());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final SwitchStatement n) {
		indent();
		stream.print("switch (");
		n.getCondition().accept(this);
		stream.println(") {");
		indent++;
		for (final SwitchCase sc : n.getCases())
			sc.accept(this);
		n.getDefault().accept(this);
		indent--;
		indent();
		stream.println("}");
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final TypeDecl n) {
		indent();
		stream.print("type ");
		n.getId().accept(this);
		stream.print(" =");
		n.getType().accept(this);
		stream.println(";");
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final VarDeclStatement n) {
		indent();
		if (n.isStatic()) stream.print("static ");
		n.getId().accept(this);
		if (n.hasType()) {
			stream.print(": ");
			n.getType().accept(this);
			if (n.hasInitializer())
				stream.print(" = ");
		} else {
			stream.print(" := ");
		}
		if (n.hasInitializer())
			n.getInitializer().accept(this);
		stream.println(";");
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final VisitStatement n) {
		indent();
		if (n.isBefore()) stream.print("before ");
		else stream.print("after ");
		if (n.hasWildcard()) stream.print("_");
		else if (n.hasComponent()) n.getComponent().accept(this);
		else {
			boolean seen = false;
			for (final Identifier id : n.getIdList()) {
				if (seen) stream.print(", ");
				else seen = true;
				id.accept(this);
			}
		}
		stream.print(" -> ");
		n.getBody().accept(this);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final WhileStatement n) {
		indent();
		stream.print("while (");
		n.getCondition().accept(this);
		stream.println(")");
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
			stream.print(" || ");
			n.getRhs(i).accept(this);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final ParenExpression n) {
		stream.print("(");
		n.getExpression().accept(this);
		stream.print(")");
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final SimpleExpr n) {
		n.getLhs().accept(this);
		for (int i = 0; i < n.getOpsSize(); i++) {
			stream.print(" " + n.getOp(i) + " ");
			n.getRhs(i).accept(this);
		}
	}

	//
	// types
	//
	/** {@inheritDoc} */
	@Override
	public void visit(final ArrayType n) {
		stream.print("array of ");
		n.getValue().accept(this);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final FunctionType n) {
		stream.print("function (");
		boolean seen = false;
		for (final Component c : n.getArgs()) {
			if (seen) stream.print(", ");
			else seen = true;
			c.accept(this);
		}
		stream.print(")");
		if (n.hasType()) {
			stream.print(" : ");
			n.getType().accept(this);
		}
		stream.println();
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final MapType n) {
		stream.print("map[");
		n.getIndex().accept(this);
		stream.print("] of ");
		n.getValue().accept(this);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final OutputType n) {
		stream.print("output ");
		n.getId().accept(this);
		if (n.getArgsSize() > 0) {
			stream.print("(");
			boolean seen = false;
			for (final Expression e : n.getArgs()) {
				if (seen) stream.print(", ");
				else seen = true;
				e.accept(this);
			}
			stream.print(")");
		}
		if (n.getIndicesSize() > 0) {
			for (final Component c : n.getIndices()) {
				stream.print("[");
				c.accept(this);
				stream.print("]");
			}
		}
		stream.print(" of ");
		n.getType().accept(this);
		if (n.hasWeight()) {
			stream.print(" weight ");
			n.getWeight().accept(this);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final StackType n) {
		stream.print("stack of ");
		n.getValue().accept(this);
	}
	
	/** {@inheritDoc} */
	@Override
	public void visit(final SetType n) {
		stream.print("set of ");
		n.getValue().accept(this);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final TupleType n) {
		stream.print("{ ");
		boolean seen = false;
		for (final Component c : n.getMembers()) {
			if (seen) stream.print(", ");
			else seen = true;
			c.accept(this);
		}
		stream.print(" }");
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final VisitorType n) {
		stream.print("visitor ");
	}

	//
	// literals
	//
	/** {@inheritDoc} */
	@Override
	public void visit(final CharLiteral n) {
		stream.print(n.getLiteral());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final FloatLiteral n) {
		stream.print(n.getLiteral());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final IntegerLiteral n) {
		stream.print(n.getLiteral());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final StringLiteral n) {
		stream.print(n.getLiteral());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final TimeLiteral n) {
		stream.print(n.getLiteral());
	}
}
