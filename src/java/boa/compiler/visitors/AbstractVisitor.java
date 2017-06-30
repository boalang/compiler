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

import java.util.List;

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
 *
 * @param <RetType> the return type to pass up the tree while visiting
 * @param <ArgTypeT> the type of the argument to pass down the tree while visiting
 */
public abstract class AbstractVisitor<ReturnTypeT, ArgTypeT> {
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

	public ReturnTypeT start(final Node n, final ArgTypeT arg) {
		initialize(arg);
		return n.accept(this, arg);
	}

	public ReturnTypeT visit(final Start n, final ArgTypeT arg) {
		return n.getProgram().accept(this, arg);
	}

	public ReturnTypeT visit(final Program n, final ArgTypeT arg) {
        visitList(n.getStatements(), arg);
		return null;
	}

	public ReturnTypeT visit(final Call n, final ArgTypeT arg) {
        visitList(n.getArgs(), arg);
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
        visitList(n.getPairs(), arg);
        visitList(n.getExprs(), arg);
		return null;
	}

	public ReturnTypeT visit(final Conjunction n, final ArgTypeT arg) {
		n.getLhs().accept(this, arg);
        visitList(n.getRhs(), arg);
		return null;
	}

	public ReturnTypeT visit(final Factor n, final ArgTypeT arg) {
		n.getOperand().accept(this, arg);
        visitList(n.getOps(), arg);
		return null;
	}

	public ReturnTypeT visit(final Identifier n, final ArgTypeT arg) {
		return null;
	}

	public ReturnTypeT visit(final Index n, final ArgTypeT arg) {
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
        visitList(n.getRhs(), arg);
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
        visitList(n.getStatements(), arg);
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
        visitList(n.getIndices(), arg);
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
        visitList(n.getCases(), arg);
		n.getBody().accept(this, arg);
		return null;
	}

	public ReturnTypeT visit(final SwitchStatement n, final ArgTypeT arg) {
		n.getCondition().accept(this, arg);
        visitList(n.getCases(), arg);
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
        visitList(n.getIdList(), arg);
		n.getBody().accept(this, arg);
		return null;
	}

	public ReturnTypeT visit(final TraverseStatement n, final ArgTypeT arg) {
		if (n.hasComponent())
			n.getComponent().accept(this, arg);
        visitList(n.getIdList(), arg);
		if (n.hasCondition())
			n.getCondition().accept(this, arg);
        visitList(n.getIfStatements(), arg);
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
        visitList(n.getIdList(), arg);
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
        visitList(n.getRhs(), arg);
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
        visitList(n.getRhs(), arg);
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

	public ReturnTypeT visit(final ArrayType n, final ArgTypeT arg) {
		n.getValue().accept(this, arg);
		return null;
	}

	public ReturnTypeT visit(final FunctionType n, final ArgTypeT arg) {
        visitList(n.getArgs(), arg);
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
        visitList(n.getArgs(), arg);
        visitList(n.getIndices(), arg);
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
        visitList(n.getMembers(), arg);
		return null;
	}

	public ReturnTypeT visit(final EnumType n, final ArgTypeT arg) {
        visitList(n.getMembers(), arg);
		return null;
	}

	public ReturnTypeT visit(final VisitorType n, final ArgTypeT arg) {
		return null;
	}

	public ReturnTypeT visit(final TraversalType n, final ArgTypeT arg) {
		if (n.getIndex() != null)
			n.getIndex().accept(this, arg);
		return null;
	}
	
	public ReturnTypeT visit(final FixPType n, final ArgTypeT arg) {
		return null;
	}
}
