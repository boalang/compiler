/*
	 * Copyright 2014, Hridesh Rajan, Robert Dyer, 
	 *                 and Iowa State University of Science and Technology
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

public class PrintBoaTree extends AbstractVisitorNoArg{
	
	/**
	 * A visitor class that prints the Boa type-checked tree.
	 * 
	 * @author nbhide
	 */
		private int indent = 0;

		private void indent() {
			for (int i = 0; i < indent; i++)
			System.out.print("    ");
		}
		
		public void start(final Node n) {
			indent();
			System.out.println("Node");
			indent++;
			super.start(n);
			indent--;
		}

		public void visit(final Start n) {
			indent();
			System.out.println("Start");
			indent++;
			super.visit(n);
			indent--;
		}

		public void visit(final Program n) {
			indent();
			System.out.println("Program");
			indent++;
			super.visit(n);
			indent--;
		}

		public void visit(final Call n) {
			indent();
			System.out.println("Call");
			indent++;
			super.visit(n);
			indent--;
		}

		public void visit(final Comparison n) {
			indent();
			System.out.println("Comparison");
			indent++;
			super.visit(n);
			indent--;
		}

		public void visit(final Component n) {
			indent();
			System.out.println("Component");
			indent++;
			super.visit(n);
			indent--;
		}

		public void visit(final Composite n) {
			indent();
			System.out.println("Composite");
			indent++;
			super.visit(n);
			indent--;
		}

		public void visit(final Conjunction n) {
			indent();
			System.out.println("Conjunction");
			indent++;
			super.visit(n);
			indent--;
		}

		public void visit(final Factor n) {
			indent();
			System.out.println("Factor");
			indent++;
			super.visit(n);
			indent--;
		}

		public void visit(final Identifier n) {
			indent();
			System.out.println("Identifier");
			indent++;
			super.visit(n);
			indent--;
		}

		public void visit(final Index n) {
			indent();
			System.out.println("Index");
			indent++;
			super.visit(n);
			indent--;
		}

		public void visit(final Pair n) {
			indent();
			System.out.println("Pair");
			indent++;
			super.visit(n);
			indent--;
		}

		public void visit(final Selector n) {
			indent();
			System.out.println("Selector");
			indent++;
			super.visit(n);
			indent--;
		}

		public void visit(final Term n) {
			indent();
			System.out.println("Term");
			indent++;
			super.visit(n);
			indent--;
		}

		public void visit(final UnaryFactor n) {
			indent();
			System.out.println("Unary Factor");
			indent++;
			super.visit(n);
			indent--;
		}

		//
		// statements
		//
		public void visit(final AssignmentStatement n) {
			indent();
			System.out.println("Assignment Statement");
			indent++;
			super.visit(n);
			indent--;
		}

		public void visit(final Block n) {
			indent();
			System.out.println("Block");
			indent++;
			super.visit(n);
			indent--;
		}

		public void visit(final BreakStatement n) {
			indent();
			System.out.println("Break Statement");
			indent++;
			super.visit(n);
			indent--;
		}

		public void visit(final ContinueStatement n) {
			indent();
			System.out.println("Continue Statement");
			indent++;
			super.visit(n);
			indent--;
		}

		public void visit(final DoStatement n) {
			indent();
			System.out.println("Do Statement");
			indent++;
			super.visit(n);
			indent--;
		}

		public void visit(final EmitStatement n) {
			indent();
			System.out.println("Emit Statement");
			indent++;
			super.visit(n);
			indent--;
		}

		public void visit(final ExistsStatement n) {
			indent();
			System.out.println("Exists Statement");
			indent++;
			super.visit(n);
			indent--;
		}

		public void visit(final ExprStatement n) {
			indent();
			System.out.println("Expression Statement");
			indent++;
			super.visit(n);
			indent--;
		}

		public void visit(final ForeachStatement n) {
			indent();
			System.out.println("Foreach Statement");
			indent++;
			super.visit(n);
			indent--;
		}

		public void visit(final ForStatement n) {
			indent();
			System.out.println("For Statement");
			indent++;
			super.visit(n);
			indent--;
		}

		public void visit(final IfAllStatement n) {
			indent();
			System.out.println("If All Statement");
			indent++;
			super.visit(n);
			indent--;
		}

		public void visit(final IfStatement n) {
			indent();
			System.out.println("If Statement");
			indent++;
			super.visit(n);
			indent--;
		}

		public void visit(final PostfixStatement n) {
			indent();
			System.out.println("PostFix Statement");
			indent++;
			super.visit(n);
			indent--;
		}

		public void visit(final ReturnStatement n) {
			indent();
			System.out.println("Return Statement");
			indent++;
			super.visit(n);
			indent--;
		}

		public void visit(final StopStatement n) {
			indent();
			System.out.println("Stop Statement");
			indent++;
			super.visit(n);
			indent--;
		}
		

		public void visit(final SwitchCase n) {
			indent();
			System.out.println("Switchcase");
			indent++;
			super.visit(n);
			indent--;
		}

		public void visit(final SwitchStatement n) {
			indent();
			System.out.println("Switch Statement");
			indent++;
			super.visit(n);
			indent--;
		}

		public void visit(final VarDeclStatement n) {
			indent();
			System.out.println("Var Declaration Statement");
			indent++;
			super.visit(n);
			indent--;
		}

		public void visit(final VisitStatement n) {
			indent();
			System.out.println("Visits Statement");
			indent++;
			super.visit(n);
			indent--;
		}

		public void visit(final WhileStatement n) {
			indent();
			System.out.println("While Statement");
			indent++;
			super.visit(n);
			indent--;
		}

		//
		// expressions
		//
		public void visit(final Expression n) {
			indent();
			System.out.println("Expressions");
			indent++;
			super.visit(n);
			indent--;
		}

		public void visit(final FunctionExpression n) {
			indent();
			System.out.println("Func Expressions");
			indent++;
			super.visit(n);
			indent--;
		}

		public void visit(final ParenExpression n) {
			indent();
			System.out.println("Paren Expressions");
			indent++;
			super.visit(n);
			indent--;
		}

		public void visit(final SimpleExpr n) {
			indent();
			System.out.println("Simple Expressions");
			indent++;
			super.visit(n);
			indent--;
		}

		public void visit(final VisitorExpression n) {
			indent();
			System.out.println("Visitor Expressions");
			indent++;
			super.visit(n);
			indent--;
		}

		//
		// literals
		//
		public void visit(final CharLiteral n) {
			indent();
			System.out.println("Char Literal");
			indent++;
			super.visit(n);
			indent--;
		}

		public void visit(final FloatLiteral n) {
			indent();
			System.out.println("Float Literal");
			indent++;
			super.visit(n);
			indent--;
		}

		public void visit(final IntegerLiteral n) {
			indent();
			System.out.println("Integer Literal");
			indent++;
			super.visit(n);
			indent--;
		}

		public void visit(final StringLiteral n) {
			indent();
			System.out.println("String Literal");
			indent++;
			super.visit(n);
			indent--;
		}

		public void visit(final TimeLiteral n) {
			indent();
			System.out.println("Time Literal");
			indent++;
			super.visit(n);
			indent--;
		}

		//
		// types
		//
		public void visit(final TypeDecl n) {
			indent();
			System.out.println("Type Declaration");
			indent++;
			super.visit(n);
			indent--;		}

		public void visit(final ArrayType n) {
			indent();
			System.out.println("Array Type");
			indent++;
			super.visit(n);
			indent--;
		}

		public void visit(final FunctionType n) {
			indent();
			System.out.println("Function Type");
			indent++;
			super.visit(n);
			indent--;
		}

		public void visit(final MapType n) {
			indent();
			System.out.println("Map Type");
			indent++;
			super.visit(n);
			indent--;
		}

		public void visit(final OutputType n) {
			indent();
			System.out.println("Output Type");
			indent++;
			super.visit(n);
			indent--;
		}

		public void visit(final StackType n) {
			indent();
			System.out.println("Stack Type");
			indent++;
			super.visit(n);
			indent--;
		}

		public void visit(final SetType n) {
			indent();
			System.out.println("Set Type");
			indent++;
			super.visit(n);
			indent--;
		}

		public void visit(final TupleType n) {
			indent();
			System.out.println("Tuple Type");
			indent++;
			super.visit(n);
			indent--;
		}

		public void visit(final VisitorType n) {
			indent();
			System.out.println("Visitor Type");
			indent++;
			super.visit(n);
			indent--;
		}	
}
