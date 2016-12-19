/*
 * Copyright 2016, Hridesh Rajan, Robert Dyer, Neha Bhide
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
 * A debugging visitor class that prints the Boa AST.
 *
 * @author nbhide
 * @author rdyer
 */
public class ASTPrintingVisitor extends AbstractVisitorNoArg {
	private int indent = 0;

	private void indent() {
		for (int i = 0; i < indent; i++)
			System.out.print("    ");
	}

	/** {@inheritDoc} */
	@Override
	public void initialize() {
		indent = 0;
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Start n) {
		indent();
		System.out.println("Start");
		indent++;
		super.visit(n);
		indent--;
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Program n) {
		indent();
		System.out.println("Program");
		indent++;
		super.visit(n);
		indent--;
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Call n) {
		indent();
		System.out.println("Call");
		indent++;
		super.visit(n);
		indent--;
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Comparison n) {
		indent();
		System.out.println("Comparison");
		indent++;
		super.visit(n);
		indent--;
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Component n) {
		indent();
		System.out.println("Component");
		indent++;
		super.visit(n);
		indent--;
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Composite n) {
		indent();
		System.out.println("Composite");
		indent++;
		super.visit(n);
		indent--;
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Conjunction n) {
		indent();
		System.out.println("Conjunction");
		indent++;
		super.visit(n);
		indent--;
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Factor n) {
		indent();
		System.out.println("Factor");
		indent++;
		super.visit(n);
		indent--;
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Identifier n) {
		indent();
		System.out.println("Identifier");
		indent++;
		super.visit(n);
		indent--;
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Index n) {
		indent();
		System.out.println("Index");
		indent++;
		super.visit(n);
		indent--;
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Pair n) {
		indent();
		System.out.println("Pair");
		indent++;
		super.visit(n);
		indent--;
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Selector n) {
		indent();
		System.out.println("Selector");
		indent++;
		super.visit(n);
		indent--;
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Term n) {
		indent();
		System.out.println("Term");
		indent++;
		super.visit(n);
		indent--;
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final UnaryFactor n) {
		indent();
		System.out.println("UnaryFactor");
		indent++;
		super.visit(n);
		indent--;
	}

	//
	// statements
	//
	/** {@inheritDoc} */
	@Override
	public void visit(final AssignmentStatement n) {
		indent();
		System.out.println("AssignmentStatement");
		indent++;
		super.visit(n);
		indent--;
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Block n) {
		indent();
		System.out.println("Block");
		indent++;
		super.visit(n);
		indent--;
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final BreakStatement n) {
		indent();
		System.out.println("BreakStatement");
		indent++;
		super.visit(n);
		indent--;
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final ContinueStatement n) {
		indent();
		System.out.println("ContinueStatement");
		indent++;
		super.visit(n);
		indent--;
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final DoStatement n) {
		indent();
		System.out.println("DoStatement");
		indent++;
		super.visit(n);
		indent--;
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final EmitStatement n) {
		indent();
		System.out.println("EmitStatement");
		indent++;
		super.visit(n);
		indent--;
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final ExistsStatement n) {
		indent();
		System.out.println("ExistsStatement");
		indent++;
		super.visit(n);
		indent--;
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final ExprStatement n) {
		indent();
		System.out.println("ExprStatement");
		indent++;
		super.visit(n);
		indent--;
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final ForeachStatement n) {
		indent();
		System.out.println("ForeachStatement");
		indent++;
		super.visit(n);
		indent--;
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final ForStatement n) {
		indent();
		System.out.println("ForStatement");
		indent++;
		super.visit(n);
		indent--;
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final IfAllStatement n) {
		indent();
		System.out.println("IfAllStatement");
		indent++;
		super.visit(n);
		indent--;
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final IfStatement n) {
		indent();
		System.out.println("IfStatement");
		indent++;
		super.visit(n);
		indent--;
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final PostfixStatement n) {
		indent();
		System.out.println("PostfixStatement");
		indent++;
		super.visit(n);
		indent--;
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final ReturnStatement n) {
		indent();
		System.out.println("ReturnStatement");
		indent++;
		super.visit(n);
		indent--;
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final StopStatement n) {
		indent();
		System.out.println("StopStatement");
		indent++;
		super.visit(n);
		indent--;
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final SwitchCase n) {
		indent();
		System.out.println("SwitchCase");
		indent++;
		super.visit(n);
		indent--;
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final SwitchStatement n) {
		indent();
		System.out.println("SwitchStatement");
		indent++;
		super.visit(n);
		indent--;
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final VarDeclStatement n) {
		indent();
		System.out.println("VarDeclStatement");
		indent++;
		super.visit(n);
		indent--;
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final VisitStatement n) {
		indent();
		System.out.println("VisitStatement");
		indent++;
		super.visit(n);
		indent--;
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final WhileStatement n) {
		indent();
		System.out.println("WhileStatement");
		indent++;
		super.visit(n);
		indent--;
	}

	//
	// expressions
	//
	/** {@inheritDoc} */
	@Override
	public void visit(final Expression n) {
		indent();
		System.out.println("Expression");
		indent++;
		super.visit(n);
		indent--;
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final FunctionExpression n) {
		indent();
		System.out.println("FunctionExpression");
		indent++;
		super.visit(n);
		indent--;
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final ParenExpression n) {
		indent();
		System.out.println("ParenExpression");
		indent++;
		super.visit(n);
		indent--;
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final SimpleExpr n) {
		indent();
		System.out.println("SimpleExpr");
		indent++;
		super.visit(n);
		indent--;
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final VisitorExpression n) {
		indent();
		System.out.println("VisitorExpression");
		indent++;
		super.visit(n);
		indent--;
	}

	//
	// literals
	//
	/** {@inheritDoc} */
	@Override
	public void visit(final CharLiteral n) {
		indent();
		System.out.println("CharLiteral");
		indent++;
		super.visit(n);
		indent--;
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final FloatLiteral n) {
		indent();
		System.out.println("FloatLiteral");
		indent++;
		super.visit(n);
		indent--;
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final IntegerLiteral n) {
		indent();
		System.out.println("IntegerLiteral");
		indent++;
		super.visit(n);
		indent--;
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final StringLiteral n) {
		indent();
		System.out.println("StringLiteral");
		indent++;
		super.visit(n);
		indent--;
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final TimeLiteral n) {
		indent();
		System.out.println("TimeLiteral");
		indent++;
		super.visit(n);
		indent--;
	}

	//
	// types
	//
	/** {@inheritDoc} */
	@Override
	public void visit(final TypeDecl n) {
		indent();
		System.out.println("TypeDecl");
		indent++;
		super.visit(n);
		indent--;		}

	/** {@inheritDoc} */
	@Override
	public void visit(final ArrayType n) {
		indent();
		System.out.println("ArrayType");
		indent++;
		super.visit(n);
		indent--;
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final FunctionType n) {
		indent();
		System.out.println("FunctionType");
		indent++;
		super.visit(n);
		indent--;
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final MapType n) {
		indent();
		System.out.println("MapType");
		indent++;
		super.visit(n);
		indent--;
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final OutputType n) {
		indent();
		System.out.println("OutputType");
		indent++;
		super.visit(n);
		indent--;
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final StackType n) {
		indent();
		System.out.println("StackType");
		indent++;
		super.visit(n);
		indent--;
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final SetType n) {
		indent();
		System.out.println("SetType");
		indent++;
		super.visit(n);
		indent--;
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final TupleType n) {
		indent();
		System.out.println("TupleType");
		indent++;
		super.visit(n);
		indent--;
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final VisitorType n) {
		indent();
		System.out.println("VisitorType");
		indent++;
		super.visit(n);
		indent--;
	}
}
