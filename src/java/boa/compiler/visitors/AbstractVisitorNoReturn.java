/*
 * Copyright 2017, Hridesh Rajan, Robert Dyer,
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

import java.util.List;

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
 *
 * @param <ArgTypeT> the type of the argument to pass down the tree while visiting
 */
public abstract class AbstractVisitorNoReturn<ArgTypeT> {
	protected void initialize(final ArgTypeT arg) { }

	protected <T extends Node> void visitList(final List<T> l, final ArgTypeT arg) {
		int len = l.size();
		for (int i = 0; i < l.size(); i++) {
			l.get(i).accept(this, arg);
			// if nodes were added/removed before the current node, dont visit them
			// and be sure to start after the last visited node's index
			// which may be before or after i
			if (len != l.size()) {
				i += (l.size() - len);
				len = l.size();
			}
		}
	}

	public void start(final Node n, final ArgTypeT arg) {
		initialize(arg);
		n.accept(this, arg);
	}

	public void visit(final Start n, final ArgTypeT arg) {
		n.getProgram().accept(this, arg);
	}

	public void visit(final Program n, final ArgTypeT arg) {
		visitList(n.getStatements(), arg);
	}

	public void visit(final Call n, final ArgTypeT arg) {
		visitList(n.getArgs(), arg);
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
		visitList(n.getExprs(), arg);
	}

	public void visit(final Conjunction n, final ArgTypeT arg) {
		n.getLhs().accept(this, arg);
		for (final Comparison c : n.getRhs())
			c.accept(this, arg);
	}

	public void visit(final Factor n, final ArgTypeT arg) {
		n.getOperand().accept(this, arg);
		visitList(n.getOps(), arg);
	}

	public void visit(final Identifier n, final ArgTypeT arg) {
	}

	public void visit(final Index n, final ArgTypeT arg) {
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
		visitList(n.getStatements(), arg);
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
		visitList(n.getIndices(), arg);
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
		visitList(n.getCases(), arg);
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
		visitList(n.getIdList(), arg);
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
		visitList(n.getIdList(), arg);
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
		visitList(n.getRhs(), arg);
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
		visitList(n.getArgs(), arg);
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
		visitList(n.getMembers(), arg);
	}

	public void visit(final EnumType n, final ArgTypeT arg) {
		for (final EnumBodyDeclaration c : n.getMembers())
			c.accept(this, arg);
	}

	public void visit(final VisitorType n, final ArgTypeT arg) {
	}

	public void visit(final TraversalType n, final ArgTypeT arg) {
		if (n.getIndex() != null)
			n.getIndex().accept(this, arg);
	}

	public void visit(final FixPType n, final ArgTypeT arg) {
	}
}
