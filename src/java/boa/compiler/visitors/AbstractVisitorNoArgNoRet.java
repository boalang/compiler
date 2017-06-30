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
 * An abstract visitor class that passes no arguments during the visit and returns nothing.
 * 
 * @author rdyer
 * @author ankuraga
 * @author rramu
 */
public abstract class AbstractVisitorNoArgNoRet {
	protected void initialize() { }

    protected <T extends Node> void visitList(final List<T> l) {
		int len = l.size();
		for (int i = 0; i < l.size(); i++) {
			l.get(i).accept(this);
			// if nodes were added/removed before the current node, dont visit them
			// and be sure to start after the last visited node's index
            // which may be before or after i
			if (len != l.size()) {
				i += (l.size() - len);
				len = l.size();
			}
		}
    }

	public void start(final Node n) {
		initialize();
		n.accept(this);
	}

	public void visit(final Start n) {
		n.getProgram().accept(this);
	}

	public void visit(final Program n) {
        visitList(n.getStatements());
	}

	public void visit(final Call n) {
        visitList(n.getArgs());
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
        visitList(n.getPairs());
        visitList(n.getExprs());
	}

	public void visit(final Conjunction n) {
		n.getLhs().accept(this);
        visitList(n.getRhs());
	}

	public void visit(final Factor n) {
		n.getOperand().accept(this);
        visitList(n.getOps());
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
        visitList(n.getRhs());
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
        visitList(n.getStatements());
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
        visitList(n.getIndices());
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
        visitList(n.getCases());
		n.getBody().accept(this);
	}

	public void visit(final SwitchStatement n) {
		n.getCondition().accept(this);
        visitList(n.getCases());
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
        visitList(n.getIdList());
		n.getBody().accept(this);
	}

	public void visit(final TraverseStatement n) {
		if (n.hasComponent())
			n.getComponent().accept(this);
        visitList(n.getIdList());
		if (n.hasCondition())
			n.getCondition().accept(this);
		if(n.getReturnType()!=null) {
			n.getReturnType().accept(this);
		}
        visitList(n.getIfStatements());
		if(n.hasBody())
		n.getBody().accept(this);
	}

	public void visit(final FixPStatement n) {
		n.getParam1().accept(this);
		n.getParam2().accept(this);
        visitList(n.getIdList());
		if (n.hasCondition())
			n.getCondition().accept(this);
		if(n.getReturnType()!=null) {
			n.getReturnType().accept(this);
		}
		if(n.hasBody())
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
        visitList(n.getRhs());
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
        visitList(n.getRhs());
	}

	public void visit(final VisitorExpression n) {
		n.getType().accept(this);
		n.getBody().accept(this);
	}

	public void visit(final TraversalExpression n) {
		n.getType().accept(this);
		n.getBody().accept(this);
	}

	public void visit(final FixPExpression n) {
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
        visitList(n.getArgs());
		if (n.hasType())
			n.getType().accept(this);
	}

	public void visit(final MapType n) {
		n.getIndex().accept(this);
		n.getValue().accept(this);
	}

	public void visit(final OutputType n) {
		n.getId().accept(this);
        visitList(n.getArgs());
        visitList(n.getIndices());
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
        visitList(n.getMembers());
	}

	public void visit(final EnumType n) {
        visitList(n.getMembers());
	}

	public void visit(final VisitorType n) {
	}

	public void visit(final TraversalType n) {
		if (n.getIndex() != null)
			n.getIndex().accept(this);
	}

	public void visit(final FixPType n) {
	}
}
