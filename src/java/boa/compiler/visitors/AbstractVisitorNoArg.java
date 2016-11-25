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

/**
 * An abstract visitor class that passes no arguments during the visit.
 * 
 * @author rdyer
 * @author ankuraga
 */
public abstract class AbstractVisitorNoArg {
	protected void initialize() { }

	public void start(final Node n) {
		initialize();
		n.accept(this);
	}

	public void visit(final Start n) {
		n.getProgram().accept(this);
	}

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
	}

	public void visit(final Call n) {
		for (final Expression e : n.getArgs())
			e.accept(this);
	}

	public void visit(final Comparison n) {
		n.getLhs().accept(this);
		if (n.hasRhs())
			n.getRhs().accept(this);
	}

	public void visit(final Component n) {
		if (n.hasIdentifier())
			n.getIdentifier().accept(this);
		n.getType().accept(this);
	}

	public void visit(final EnumBodyDeclaration n) {
		n.getIdentifier().accept(this);
		n.getExp().accept(this);
	}

	public void visit(final Composite n) {
		for (final Pair p : n.getPairs())
			p.accept(this);
		for (final Expression e : n.getExprs())
			e.accept(this);
	}

	public void visit(final Conjunction n) {
		n.getLhs().accept(this);
		for (final Comparison c : n.getRhs())
			c.accept(this);
	}

	public void visit(final Factor n) {
		n.getOperand().accept(this);
		for (final Node o : n.getOps())
			o.accept(this);
	}

	public void visit(final Identifier n) {
	}

	public void visit(final Index n) {
		n.getStart().accept(this);
		if (n.hasEnd())
			n.getEnd().accept(this);
	}

	public void visit(final Pair n) {
		n.getExpr1().accept(this);
		n.getExpr2().accept(this);
	}

	public void visit(final Selector n) {
		n.getId().accept(this);
	}

	public void visit(final Term n) {
		n.getLhs().accept(this);
		for (final Factor f : n.getRhs())
			f.accept(this);
	}

	public void visit(final UnaryFactor n) {
		n.getFactor().accept(this);
	}

	//
	// statements
	//
	public void visit(final AssignmentStatement n) {
		n.getLhs().accept(this);
		n.getRhs().accept(this);
	}

	public void visit(final Block n) {
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
	}

	public void visit(final BreakStatement n) {
	}

	public void visit(final ContinueStatement n) {
	}

	public void visit(final DoStatement n) {
		n.getCondition().accept(this);
		n.getBody().accept(this);
	}

	public void visit(final EmitStatement n) {
		n.getId().accept(this);
		for (final Expression e : n.getIndices())
			e.accept(this);
		n.getValue().accept(this);
		if (n.hasWeight())
			n.getWeight().accept(this);
	}

	public void visit(final ExistsStatement n) {
		n.getVar().accept(this);
		n.getCondition().accept(this);
		n.getBody().accept(this);
	}

	public void visit(final ExprStatement n) {
		n.getExpr().accept(this);
	}

	public void visit(final ForeachStatement n) {
		n.getVar().accept(this);
		n.getCondition().accept(this);
		n.getBody().accept(this);
	}

	public void visit(final ForStatement n) {
		if (n.hasInit())
			n.getInit().accept(this);
		if (n.hasCondition())
			n.getCondition().accept(this);
		if (n.hasUpdate())
			n.getUpdate().accept(this);
		n.getBody().accept(this);
	}

	public void visit(final IfAllStatement n) {
		n.getVar().accept(this);
		n.getCondition().accept(this);
		n.getBody().accept(this);
	}

	public void visit(final IfStatement n) {
		n.getCondition().accept(this);
		n.getBody().accept(this);
		if (n.hasElse())
			n.getElse().accept(this);
	}

	public void visit(final PostfixStatement n) {
		n.getExpr().accept(this);
	}

	public void visit(final ReturnStatement n) {
		if (n.hasExpr())
			n.getExpr().accept(this);
	}

	public void visit(final StopStatement n) {
	}

	public void visit(final SwitchCase n) {
		for (final Expression e : n.getCases())
			e.accept(this);
		n.getBody().accept(this);
	}

	public void visit(final SwitchStatement n) {
		n.getCondition().accept(this);
		for (final SwitchCase sc : n.getCases())
			sc.accept(this);
		n.getDefault().accept(this);
	}

	public void visit(final VarDeclStatement n) {
		n.getId().accept(this);
		if (n.hasType())
			n.getType().accept(this);
		if (n.hasInitializer())
			n.getInitializer().accept(this);
	}

	public void visit(final VisitStatement n) {
		if (n.hasComponent())
			n.getComponent().accept(this);
		for (final Identifier id : n.getIdList())
			id.accept(this);
		n.getBody().accept(this);
	}

	public void visit(final WhileStatement n) {
		n.getCondition().accept(this);
		n.getBody().accept(this);
	}

	//
	// expressions
	//
	public void visit(final Expression n) {
		n.getLhs().accept(this);
		for (final Conjunction c : n.getRhs())
			c.accept(this);
	}

	public void visit(final FunctionExpression n) {
		n.getType().accept(this);
		n.getBody().accept(this);
	}

	public void visit(final ParenExpression n) {
		n.getExpression().accept(this);
	}

	public void visit(final SimpleExpr n) {
		n.getLhs().accept(this);
		for (final Term t : n.getRhs())
			t.accept(this);
	}

	public void visit(final VisitorExpression n) {
		n.getType().accept(this);
		n.getBody().accept(this);
	}

	//
	// literals
	//
	public void visit(final CharLiteral n) {
	}

	public void visit(final FloatLiteral n) {
	}

	public void visit(final IntegerLiteral n) {
	}

	public void visit(final StringLiteral n) {
	}

	public void visit(final TimeLiteral n) {
	}

	//
	// types
	//
	public void visit(final TypeDecl n) {
		n.getId().accept(this);
		n.getType().accept(this);
	}

	public void visit(final ArrayType n) {
		n.getValue().accept(this);
	}

	public void visit(final FunctionType n) {
		for (final Component c : n.getArgs())
			c.accept(this);
		if (n.hasType())
			n.getType().accept(this);
	}

	public void visit(final MapType n) {
		n.getIndex().accept(this);
		n.getValue().accept(this);
	}

	public void visit(final OutputType n) {
		n.getId().accept(this);
		for (final Expression e : n.getArgs())
			e.accept(this);
		for (final Component c : n.getIndices())
			c.accept(this);
		n.getType().accept(this);
		if (n.hasWeight())
			n.getWeight().accept(this);
	}

	public void visit(final StackType n) {
		n.getValue().accept(this);
	}

	public void visit(final SetType n) {
		n.getValue().accept(this);
	}

	public void visit(final TupleType n) {
		for (final Component c : n.getMembers())
			c.accept(this);
	}

	public void visit(final EnumType n) {
		for (final EnumBodyDeclaration c : n.getMembers()){
			c.accept(this);
		}
	}

	public void visit(final VisitorType n) {
	}
}
