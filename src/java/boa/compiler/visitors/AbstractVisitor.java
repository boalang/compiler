/*
 * Copyright 2014, Hridesh Rajan, Robert Dyer, Che Shian Hung,
 *                 Iowa State University of Science and Technology,
 *				   and Bowling Green State University
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
 * A visitor for the Boa abstract syntax tree.
 * 
 * @author rdyer
 * @author hridesh
 * @author ankuraga
 * @author rramu
 * @author hungc
 *
 * @param <RetType> the return type to pass up the tree while visiting
 * @param <ArgTypeT> the type of the argument to pass down the tree while visiting
 */
public abstract class AbstractVisitor<ReturnTypeT, ArgTypeT> {
	protected void initialize(final ArgTypeT arg) { }

	public ReturnTypeT start(final Node n, final ArgTypeT arg) {
		initialize(arg);
		return n.accept(this, arg);
	}

	public ReturnTypeT visit(final Start n, final ArgTypeT arg) {
		return n.getProgram().accept(this, arg);
	}

	public ReturnTypeT visit(final Program n, final ArgTypeT arg) {
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
		return null;
	}

	public ReturnTypeT visit(final Call n, final ArgTypeT arg) {
		for (final Expression e : n.getArgs())
			e.accept(this, arg);
		return null;
	}

	public ReturnTypeT visit(final Comparison n, final ArgTypeT arg) {
		n.getLhs().accept(this, arg);
		if (n.hasRhs())
			n.getRhs().accept(this, arg);
		return null;
	}

	public ReturnTypeT visit(final Component n, final ArgTypeT arg) {
		if (n.hasIdentifier())
			n.getIdentifier().accept(this, arg);
		n.getType().accept(this, arg);
		return null;
	}

	public ReturnTypeT visit(final EnumBodyDeclaration n, final ArgTypeT arg) {
		n.getIdentifier().accept(this, arg);
		n.getExp().accept(this, arg);
		return null;
	}

	public ReturnTypeT visit(final Composite n, final ArgTypeT arg) {
		for (final Pair p : n.getPairs())
			p.accept(this, arg);
		for (final Expression e : n.getExprs())
			e.accept(this, arg);
		return null;
	}

	public ReturnTypeT visit(final Conjunction n, final ArgTypeT arg) {
		n.getLhs().accept(this, arg);
		for (final Comparison c : n.getRhs())
			c.accept(this, arg);
		return null;
	}

	public ReturnTypeT visit(final Factor n, final ArgTypeT arg) {
		n.getOperand().accept(this, arg);
		for (final Node o : n.getOps())
			o.accept(this, arg);
		return null;
	}

	public ReturnTypeT visit(final Identifier n, final ArgTypeT arg) {
		return null;
	}

	public ReturnTypeT visit(final Index n, final ArgTypeT arg) {
		if (n.hasStart())
			n.getStart().accept(this, arg);
		if (n.hasEnd())
			n.getEnd().accept(this, arg);
		return null;
	}

	public ReturnTypeT visit(final Pair n, final ArgTypeT arg) {
		n.getExpr1().accept(this, arg);
		n.getExpr2().accept(this, arg);
		return null;
	}

	public ReturnTypeT visit(final Selector n, final ArgTypeT arg) {
		n.getId().accept(this, arg);
		return null;
	}

	public ReturnTypeT visit(final Term n, final ArgTypeT arg) {
		n.getLhs().accept(this, arg);
		for (final Factor f : n.getRhs())
			f.accept(this, arg);
		return null;
	}

	public ReturnTypeT visit(final UnaryFactor n, final ArgTypeT arg) {
		n.getFactor().accept(this, arg);
		return null;
	}

	//
	// statements
	//
	public ReturnTypeT visit(final AssignmentStatement n, final ArgTypeT arg) {
		n.getLhs().accept(this, arg);
		n.getRhs().accept(this, arg);
		return null;
	}

	public ReturnTypeT visit(final Block n, final ArgTypeT arg) {
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
		return null;
	}

	public ReturnTypeT visit(final BreakStatement n, final ArgTypeT arg) {
		return null;
	}

	public ReturnTypeT visit(final ContinueStatement n, final ArgTypeT arg) {
		return null;
	}

	public ReturnTypeT visit(final DoStatement n, final ArgTypeT arg) {
		n.getCondition().accept(this, arg);
		n.getBody().accept(this, arg);
		return null;
	}

	public ReturnTypeT visit(final EmitStatement n, final ArgTypeT arg) {
		n.getId().accept(this, arg);
		for (final Expression e : n.getIndices())
			e.accept(this, arg);
		n.getValue().accept(this, arg);
		if (n.hasWeight())
			n.getWeight().accept(this, arg);
		return null;
	}

	public ReturnTypeT visit(final ExistsStatement n, final ArgTypeT arg) {
		n.getVar().accept(this, arg);
		n.getCondition().accept(this, arg);
		n.getBody().accept(this, arg);
		return null;
	}

	public ReturnTypeT visit(final ExprStatement n, final ArgTypeT arg) {
		n.getExpr().accept(this, arg);
		return null;
	}

	public ReturnTypeT visit(final ForeachStatement n, final ArgTypeT arg) {
		n.getVar().accept(this, arg);
		n.getCondition().accept(this, arg);
		n.getBody().accept(this, arg);
		return null;
	}

	public ReturnTypeT visit(final ForStatement n, final ArgTypeT arg) {
		if (n.hasInit())
			n.getInit().accept(this, arg);
		if (n.hasCondition())
			n.getCondition().accept(this, arg);
		if (n.hasUpdate())
			n.getUpdate().accept(this, arg);
		n.getBody().accept(this, arg);
		return null;
	}

	public ReturnTypeT visit(final IfAllStatement n, final ArgTypeT arg) {
		n.getVar().accept(this, arg);
		n.getCondition().accept(this, arg);
		n.getBody().accept(this, arg);
		return null;
	}

	public ReturnTypeT visit(final IfStatement n, final ArgTypeT arg) {
		n.getCondition().accept(this, arg);
		n.getBody().accept(this, arg);
		if (n.hasElse())
			n.getElse().accept(this, arg);
		return null;
	}

	public ReturnTypeT visit(final PostfixStatement n, final ArgTypeT arg) {
		n.getExpr().accept(this, arg);
		return null;
	}

	public ReturnTypeT visit(final ReturnStatement n, final ArgTypeT arg) {
		if (n.hasExpr())
			n.getExpr().accept(this, arg);
		return null;
	}

	public ReturnTypeT visit(final StopStatement n, final ArgTypeT arg) {
		return null;
	}

	public ReturnTypeT visit(final SwitchCase n, final ArgTypeT arg) {
		for (final Expression e : n.getCases())
			e.accept(this, arg);
		n.getBody().accept(this, arg);
		return null;
	}

	public ReturnTypeT visit(final SwitchStatement n, final ArgTypeT arg) {
		n.getCondition().accept(this, arg);
		for (final SwitchCase sc : n.getCases())
			sc.accept(this, arg);
		n.getDefault().accept(this, arg);
		return null;
	}

	public ReturnTypeT visit(final VarDeclStatement n, final ArgTypeT arg) {
		n.getId().accept(this, arg);
		if (n.hasType())
			n.getType().accept(this, arg);
		if (n.hasInitializer())
			n.getInitializer().accept(this, arg);
		return null;
	}

	public ReturnTypeT visit(final VisitStatement n, final ArgTypeT arg) {
		if (n.hasComponent())
			n.getComponent().accept(this, arg);
		for (final Identifier id : n.getIdList())
			id.accept(this, arg);
		n.getBody().accept(this, arg);
		return null;
	}

	public ReturnTypeT visit(final TraverseStatement n, final ArgTypeT arg) {
		if (n.hasComponent())
			n.getComponent().accept(this, arg);
		for (final Identifier id : n.getIdList())
			id.accept(this, arg);
		if (n.hasCondition())
			n.getCondition().accept(this, arg);
		for (final IfStatement ifStatement : n.getIfStatements())
			ifStatement.accept(this, arg);
		if(n.getReturnType()!=null) {
			n.getReturnType().accept(this, arg);
		}
		if(n.hasBody())
		n.getBody().accept(this, arg);
		return null;
	}

	public ReturnTypeT visit(final FixPStatement n, final ArgTypeT arg) {
		n.getParam1().accept(this, arg);
		n.getParam2().accept(this, arg);
		for (final Identifier id : n.getIdList())
			id.accept(this, arg);
		if (n.hasCondition())
			n.getCondition().accept(this, arg);
		if(n.getReturnType()!=null) {
			n.getReturnType().accept(this, arg);
		}
		if(n.hasBody())
		n.getBody().accept(this, arg);
		return null;
	}

	public ReturnTypeT visit(final WhileStatement n, final ArgTypeT arg) {
		n.getCondition().accept(this, arg);
		n.getBody().accept(this, arg);
		return null;
	}

	//
	// expressions
	//
	public ReturnTypeT visit(final Expression n, final ArgTypeT arg) {
		n.getLhs().accept(this, arg);
		for (final Conjunction c : n.getRhs())
			c.accept(this, arg);
		return null;
	}

	public ReturnTypeT visit(final FunctionExpression n, final ArgTypeT arg) {
		n.getType().accept(this, arg);
		n.getBody().accept(this, arg);
		return null;
	}

	public ReturnTypeT visit(final ParenExpression n, final ArgTypeT arg) {
		n.getExpression().accept(this, arg);
		return null;
	}

	public ReturnTypeT visit(final SimpleExpr n, final ArgTypeT arg) {
		n.getLhs().accept(this, arg);
		for (final Term t : n.getRhs())
			t.accept(this, arg);
		return null;
	}

	public ReturnTypeT visit(final VisitorExpression n, final ArgTypeT arg) {
		n.getType().accept(this, arg);
		n.getBody().accept(this, arg);
		return null;
	}

	public ReturnTypeT visit(final TraversalExpression n, final ArgTypeT arg) {
		n.getType().accept(this, arg);
		n.getBody().accept(this, arg);
		return null;
	}

	public ReturnTypeT visit(final FixPExpression n, final ArgTypeT arg) {
		n.getType().accept(this, arg);
		n.getBody().accept(this, arg);
		return null;
	}

	//
	// literals
	//
	public ReturnTypeT visit(final CharLiteral n, final ArgTypeT arg) {
		return null;
	}

	public ReturnTypeT visit(final FloatLiteral n, final ArgTypeT arg) {
		return null;
	}

	public ReturnTypeT visit(final IntegerLiteral n, final ArgTypeT arg) {
		return null;
	}

	public ReturnTypeT visit(final StringLiteral n, final ArgTypeT arg) {
		return null;
	}

	public ReturnTypeT visit(final TimeLiteral n, final ArgTypeT arg) {
		return null;
	}

	//
	// types
	//
	public ReturnTypeT visit(final TypeDecl n, final ArgTypeT arg) {
		n.getId().accept(this, arg);
		n.getType().accept(this, arg);
		return null;
	}

	public ReturnTypeT visit(final RowType n, final ArgTypeT arg) {
		n.getId().accept(this, arg);
		for (final Index i : n.getIndices())
			i.accept(this, arg);
		return null;
	}

	public ReturnTypeT visit(final ArrayType n, final ArgTypeT arg) {
		n.getValue().accept(this, arg);
		return null;
	}

	public ReturnTypeT visit(final FunctionType n, final ArgTypeT arg) {
		for (final Component c : n.getArgs())
			c.accept(this, arg);
		if (n.hasType())
			n.getType().accept(this, arg);
		return null;
	}

	public ReturnTypeT visit(final MapType n, final ArgTypeT arg) {
		n.getIndex().accept(this, arg);
		n.getValue().accept(this, arg);
		return null;
	}

	public ReturnTypeT visit(final OutputType n, final ArgTypeT arg) {
		n.getId().accept(this, arg);
		for (final Expression e : n.getArgs())
			e.accept(this, arg);
		for (final Component c : n.getIndices())
			c.accept(this, arg);
		n.getType().accept(this, arg);
		if (n.hasWeight())
			n.getWeight().accept(this, arg);
		return null;
	}

	public ReturnTypeT visit(final StackType n, final ArgTypeT arg) {
		n.getValue().accept(this, arg);
		return null;
	}

	public ReturnTypeT visit(final SetType n, final ArgTypeT arg) {
		n.getValue().accept(this, arg);
		return null;
	}

	public ReturnTypeT visit(final TupleType n, final ArgTypeT arg) {
		for (final Component c : n.getMembers())
			c.accept(this, arg);
		return null;
	}

	public ReturnTypeT visit(final EnumType n, final ArgTypeT arg) {
		for (final EnumBodyDeclaration c : n.getMembers())
			c.accept(this, arg);
		return null;
	}

	public ReturnTypeT visit(final VisitorType n, final ArgTypeT arg) {
		return null;
	}

	public ReturnTypeT visit(final TraversalType n, final ArgTypeT arg) {
		if(n.getIndex()!=null)
			n.getIndex().accept(this, arg);
		return null;
	}
	
	public ReturnTypeT visit(final FixPType n, final ArgTypeT arg) {
		return null;
	}

	public ReturnTypeT visit(final TableType n, final ArgTypeT arg) {
		for (final Component c : n.getIndices())
			c.accept(this, arg);
		n.getType().accept(this, arg);
		return null;
	}

	public ReturnTypeT visit(final Table n, final ArgTypeT arg) {
		return null;
	}

	public ReturnTypeT visit(final SubView n, final ArgTypeT arg) {
		n.getId().accept(this, arg);
		n.getProgram().accept(this, arg);
		return null;
	}
}
