/*
 * Copyright 2015, Hridesh Rajan, Robert Dyer, Che Shian Hung,
 *                 Iowa State University of Science and Technology,
 *				   , and Bowling Green State University
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
 * A specialization of the Visitor that doesn't pass up a return value.
 * 
 * @author hridesh
 * @author rdyer
 * @author ankuraga
 * @author rramu
 * @author hungc
 *
 * @param <ArgTypeT> the type of the argument to pass down the tree while visiting
 */
public abstract class AbstractVisitorNoReturn<ArgTypeT> {
	protected void initialize(final ArgTypeT arg) { }

	public void start(final Node n, final ArgTypeT arg) {
		initialize(arg);
		n.accept(this, arg);
	}

	public void visit(final Start n, final ArgTypeT arg) {
		n.getProgram().accept(this, arg);
	}

	public void visit(final Program n, final ArgTypeT arg) {
		int len = n.getStatementsSize();
		for (int i = 0; i < n.getStatementsSize(); i++) {
			n.getStatement(i).accept(this, arg);
			// if a node was added, dont visit it and
			// dont re-visit the node we were just at
			if (len != n.getStatementsSize()) {
				i += (n.getStatementsSize() - len);
				len = n.getStatementsSize();
			}
		}
	}

	public void visit(final Call n, final ArgTypeT arg) {
		for (final Expression e : n.getArgs())
			e.accept(this, arg);
	}

	public void visit(final Comparison n, final ArgTypeT arg) {
		n.getLhs().accept(this, arg);
		if (n.hasRhs())
			n.getRhs().accept(this, arg);
	}

	public void visit(final Component n, final ArgTypeT arg) {
		if (n.hasIdentifier())
			n.getIdentifier().accept(this, arg);
		n.getType().accept(this, arg);
	}

	public void visit(final EnumBodyDeclaration n, final ArgTypeT arg) {
		n.getIdentifier().accept(this, arg);
		n.getExp().accept(this, arg);
	}

	public void visit(final Composite n, final ArgTypeT arg) {
		for (final Pair p : n.getPairs())
			p.accept(this, arg);
		for (final Expression e : n.getExprs())
			e.accept(this, arg);
	}

	public void visit(final Conjunction n, final ArgTypeT arg) {
		n.getLhs().accept(this, arg);
		for (final Comparison c : n.getRhs())
			c.accept(this, arg);
	}

	public void visit(final Factor n, final ArgTypeT arg) {
		n.getOperand().accept(this, arg);
		for (final Node o : n.getOps())
			o.accept(this, arg);
	}

	public void visit(final Identifier n, final ArgTypeT arg) {
	}

	public void visit(final Index n, final ArgTypeT arg) {
		if (n.hasStart())
			n.getStart().accept(this, arg);
		if (n.hasEnd())
			n.getEnd().accept(this, arg);
	}

	public void visit(final Pair n, final ArgTypeT arg) {
		n.getExpr1().accept(this, arg);
		n.getExpr2().accept(this, arg);
	}

	public void visit(final Selector n, final ArgTypeT arg) {
		n.getId().accept(this, arg);
	}

	public void visit(final Term n, final ArgTypeT arg) {
		n.getLhs().accept(this, arg);
		for (final Factor f : n.getRhs())
			f.accept(this, arg);
	}

	public void visit(final UnaryFactor n, final ArgTypeT arg) {
		n.getFactor().accept(this, arg);
	}

	//
	// statements
	//
	public void visit(final AssignmentStatement n, final ArgTypeT arg) {
		n.getLhs().accept(this, arg);
		n.getRhs().accept(this, arg);
	}

	public void visit(final Block n, final ArgTypeT arg) {
		int len = n.getStatementsSize();
		for (int i = 0; i < n.getStatementsSize(); i++) {
			n.getStatement(i).accept(this, arg);
			// if a node was added, dont visit it and
			// dont re-visit the node we were just at
			if (len != n.getStatementsSize()) {
				i += (n.getStatementsSize() - len);
				len = n.getStatementsSize();
			}
		}
	}

	public void visit(final BreakStatement n, final ArgTypeT arg) {
	}

	public void visit(final ContinueStatement n, final ArgTypeT arg) {
	}

	public void visit(final DoStatement n, final ArgTypeT arg) {
		n.getCondition().accept(this, arg);
		n.getBody().accept(this, arg);
	}

	public void visit(final EmitStatement n, final ArgTypeT arg) {
		n.getId().accept(this, arg);
		for (final Expression e : n.getIndices())
			e.accept(this, arg);
		n.getValue().accept(this, arg);
		if (n.hasWeight())
			n.getWeight().accept(this, arg);
	}

	public void visit(final ExistsStatement n, final ArgTypeT arg) {
		n.getVar().accept(this, arg);
		n.getCondition().accept(this, arg);
		n.getBody().accept(this, arg);
	}

	public void visit(final ExprStatement n, final ArgTypeT arg) {
		n.getExpr().accept(this, arg);
	}

	public void visit(final ForeachStatement n, final ArgTypeT arg) {
		n.getVar().accept(this, arg);
		n.getCondition().accept(this, arg);
		n.getBody().accept(this, arg);
	}

	public void visit(final ForStatement n, final ArgTypeT arg) {
		if (n.hasInit())
			n.getInit().accept(this, arg);
		if (n.hasCondition())
			n.getCondition().accept(this, arg);
		if (n.hasUpdate())
			n.getUpdate().accept(this, arg);
		n.getBody().accept(this, arg);
	}

	public void visit(final IfAllStatement n, final ArgTypeT arg) {
		n.getVar().accept(this, arg);
		n.getCondition().accept(this, arg);
		n.getBody().accept(this, arg);
	}

	public void visit(final IfStatement n, final ArgTypeT arg) {
		n.getCondition().accept(this, arg);
		n.getBody().accept(this, arg);
		if (n.hasElse())
			n.getElse().accept(this, arg);
	}

	public void visit(final PostfixStatement n, final ArgTypeT arg) {
		n.getExpr().accept(this, arg);
	}

	public void visit(final ReturnStatement n, final ArgTypeT arg) {
		if (n.hasExpr())
			n.getExpr().accept(this, arg);
	}

	public void visit(final StopStatement n, final ArgTypeT arg) {
	}

	public void visit(final SwitchCase n, final ArgTypeT arg) {
		for (final Expression e : n.getCases())
			e.accept(this, arg);
		n.getBody().accept(this, arg);
	}

	public void visit(final SwitchStatement n, final ArgTypeT arg) {
		n.getCondition().accept(this, arg);
		for (final SwitchCase sc : n.getCases())
			sc.accept(this, arg);
		n.getDefault().accept(this, arg);
	}

	public void visit(final VarDeclStatement n, final ArgTypeT arg) {
		n.getId().accept(this, arg);
		if (n.hasType())
			n.getType().accept(this, arg);
		if (n.hasInitializer())
			n.getInitializer().accept(this, arg);
	}

	public void visit(final VisitStatement n, final ArgTypeT arg) {
		if (n.hasComponent())
			n.getComponent().accept(this, arg);
		for (final Identifier id : n.getIdList())
			id.accept(this, arg);
		n.getBody().accept(this, arg);
	}

	public void visit(final TraverseStatement n, final ArgTypeT arg) {
		if (n.hasComponent())
			n.getComponent().accept(this, arg);
		for (final Identifier id : n.getIdList())
			id.accept(this, arg);
		if (n.hasCondition())
			n.getCondition().accept(this,arg);
		if(n.getReturnType()!=null) {
			n.getReturnType().accept(this, arg);
		}
		for (final IfStatement ifStatement : n.getIfStatements())
			ifStatement.accept(this, arg);
		if(n.hasBody())
		n.getBody().accept(this, arg);
	}

	public void visit(final FixPStatement n, final ArgTypeT arg) {
		n.getParam1().accept(this, arg);
		n.getParam2().accept(this, arg);
		for (final Identifier id : n.getIdList())
			id.accept(this, arg);
		if (n.hasCondition())
			n.getCondition().accept(this,arg);
		if(n.getReturnType()!=null) {
			n.getReturnType().accept(this, arg);
		}
		if(n.hasBody())
		n.getBody().accept(this, arg);
	}

	public void visit(final WhileStatement n, final ArgTypeT arg) {
		n.getCondition().accept(this, arg);
		n.getBody().accept(this, arg);
	}

	//
	// expressions
	//
	public void visit(final Expression n, final ArgTypeT arg) {
		n.getLhs().accept(this, arg);
		for (final Conjunction c : n.getRhs())
			c.accept(this, arg);
	}

	public void visit(final FunctionExpression n, final ArgTypeT arg) {
		n.getType().accept(this, arg);
		n.getBody().accept(this, arg);
	}

	public void visit(final ParenExpression n, final ArgTypeT arg) {
		n.getExpression().accept(this, arg);
	}

	public void visit(final SimpleExpr n, final ArgTypeT arg) {
		n.getLhs().accept(this, arg);
		for (final Term t : n.getRhs())
			t.accept(this, arg);
	}

	public void visit(final VisitorExpression n, final ArgTypeT arg) {
		n.getType().accept(this, arg);
		n.getBody().accept(this, arg);
	}

	public void visit(final TraversalExpression n, final ArgTypeT arg) {
		n.getType().accept(this, arg);
		n.getBody().accept(this, arg);
	}

	public void visit(final FixPExpression n, final ArgTypeT arg) {
		n.getType().accept(this, arg);
		n.getBody().accept(this, arg);
	}

	//
	// literals
	//
	public void visit(final CharLiteral n, final ArgTypeT arg) {
	}

	public void visit(final FloatLiteral n, final ArgTypeT arg) {
	}

	public void visit(final IntegerLiteral n, final ArgTypeT arg) {
	}

	public void visit(final StringLiteral n, final ArgTypeT arg) {
	}

	public void visit(final TimeLiteral n, final ArgTypeT arg) {
	}

	//
	// types
	//
	public void visit(final TypeDecl n, final ArgTypeT arg) {
		n.getId().accept(this, arg);
		n.getType().accept(this, arg);
	}

	public void visit(final RowType n, final ArgTypeT arg) {
		n.getId().accept(this, arg);
		for (final Index i : n.getIndices())
			i.accept(this, arg);
	}

	public void visit(final ArrayType n, final ArgTypeT arg) {
		n.getValue().accept(this, arg);
	}

	public void visit(final FunctionType n, final ArgTypeT arg) {
		for (final Component c : n.getArgs())
			c.accept(this, arg);
		if (n.hasType())
			n.getType().accept(this, arg);
	}

	public void visit(final MapType n, final ArgTypeT arg) {
		n.getIndex().accept(this, arg);
		n.getValue().accept(this, arg);
	}

	public void visit(final OutputType n, final ArgTypeT arg) {
		n.getId().accept(this, arg);
		for (final Expression e : n.getArgs())
			e.accept(this, arg);
		for (final Component c : n.getIndices())
			c.accept(this, arg);
		n.getType().accept(this, arg);
		if (n.hasWeight())
			n.getWeight().accept(this, arg);
	}

	public void visit(final StackType n, final ArgTypeT arg) {
		n.getValue().accept(this, arg);
	}

	public void visit(final SetType n, final ArgTypeT arg) {
		n.getValue().accept(this, arg);
	}

	public void visit(final TupleType n, final ArgTypeT arg) {
		for (final Component c : n.getMembers())
			c.accept(this, arg);
	}

	public void visit(final EnumType n, final ArgTypeT arg) {
		for (final EnumBodyDeclaration c : n.getMembers())
			c.accept(this, arg);
	}

	public void visit(final VisitorType n, final ArgTypeT arg) {
	}

	public void visit(final TraversalType n, final ArgTypeT arg) {
		if(n.getIndex()!=null)
			n.getIndex().accept(this, arg);
	}

	public void visit(final FixPType n, final ArgTypeT arg) {
	}

	public void visit(final TableType n, final ArgTypeT arg) {
		for (final Component c : n.getIndices())
			c.accept(this, arg);
		n.getType().accept(this, arg);
	}

	public void visit(final Table n, final ArgTypeT arg) {
	}

	public void visit(final SubView n, final ArgTypeT arg) {
		n.getId().accept(this, arg);
		n.getProgram().accept(this, arg);
	}
}
