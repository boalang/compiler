/*
 * Copyright 2016, Hridesh Rajan, Robert Dyer,
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import boa.compiler.ast.Call;
import boa.compiler.ast.Comparison;
import boa.compiler.ast.Component;
import boa.compiler.ast.Composite;
import boa.compiler.ast.Conjunction;
import boa.compiler.ast.Factor;
import boa.compiler.ast.Identifier;
import boa.compiler.ast.Index;
import boa.compiler.ast.Node;
import boa.compiler.ast.Operand;
import boa.compiler.ast.Pair;
import boa.compiler.ast.Selector;
import boa.compiler.ast.Term;
import boa.compiler.ast.UnaryFactor;
import boa.compiler.ast.expressions.Expression;
import boa.compiler.ast.expressions.FixPExpression;
import boa.compiler.ast.expressions.FunctionExpression;
import boa.compiler.ast.expressions.ParenExpression;
import boa.compiler.ast.expressions.SimpleExpr;
import boa.compiler.ast.expressions.TraversalExpression;
import boa.compiler.ast.expressions.VisitorExpression;
import boa.compiler.ast.literals.CharLiteral;
import boa.compiler.ast.literals.FloatLiteral;
import boa.compiler.ast.literals.IntegerLiteral;
import boa.compiler.ast.literals.StringLiteral;
import boa.compiler.ast.literals.TimeLiteral;
import boa.compiler.ast.statements.AssignmentStatement;
import boa.compiler.ast.statements.Block;
import boa.compiler.ast.statements.BreakStatement;
import boa.compiler.ast.statements.ContinueStatement;
import boa.compiler.ast.statements.DoStatement;
import boa.compiler.ast.statements.EmitStatement;
import boa.compiler.ast.statements.ExistsStatement;
import boa.compiler.ast.statements.ExprStatement;
import boa.compiler.ast.statements.FixPStatement;
import boa.compiler.ast.statements.ForStatement;
import boa.compiler.ast.statements.ForeachStatement;
import boa.compiler.ast.statements.IfAllStatement;
import boa.compiler.ast.statements.IfStatement;
import boa.compiler.ast.statements.PostfixStatement;
import boa.compiler.ast.statements.ReturnStatement;
import boa.compiler.ast.statements.Statement;
import boa.compiler.ast.statements.StopStatement;
import boa.compiler.ast.statements.SwitchCase;
import boa.compiler.ast.statements.SwitchStatement;
import boa.compiler.ast.statements.TraverseStatement;
import boa.compiler.ast.statements.TypeDecl;
import boa.compiler.ast.statements.VarDeclStatement;
import boa.compiler.ast.statements.VisitStatement;
import boa.compiler.ast.statements.WhileStatement;
import boa.compiler.ast.types.ArrayType;
import boa.compiler.ast.types.FixPType;
import boa.compiler.ast.types.FunctionType;
import boa.compiler.ast.types.MapType;
import boa.compiler.ast.types.OutputType;
import boa.compiler.ast.types.QueueType;
import boa.compiler.ast.types.SetType;
import boa.compiler.ast.types.StackType;
import boa.compiler.ast.types.TupleType;

/**
 * @author ganeshau
 */
public class CFGBuildingVisitor extends AbstractVisitorNoArgNoRet {
	private int id = 0;
	public List<Node> currentStartNodes;
	public List<Node> currentEndNodes;
	public List<Node> currentExitNodes;
	private static List<Node> emptyList = new ArrayList<Node>(0);

	public List<Node> order;

	public Node getNodeById(int id) {
		for (Node node : order) {
			if (node.nodeId == id) {
				return node;
			}
		}
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Block n) {
		List<Statement> stats = n.getStatements();

		if (stats.size() == 0) {
			singleton(n);
		} else {
			visitStatements(stats);
			addNode(n);
			visitList(stats);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final VarDeclStatement n) {
		Expression init = null;
		if (n.hasInitializer()) {
			init = n.getInitializer();
			init.accept(this);
		} else {
			currentStartNodes = new ArrayList<Node>(1);
			currentStartNodes.add(n);
		}

		currentEndNodes = new ArrayList<Node>(1);
		currentEndNodes.add(n);
		currentExitNodes = emptyList;
		addNode(n);

		// connect the nodes
		if (init != null) {
			connectToEndNodesOf(init, n);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final DoStatement n) {
		Block body = n.getBody();
		Expression cond = n.getCondition();

		// fill the start/end/exit nodes
		List<Node> finalEndNodes = new ArrayList<Node>();
		List<Node> finalExcEndNodes = new ArrayList<Node>();
		body.accept(this);

		List<Node> bodyStartNodes = new ArrayList<Node>(currentStartNodes);
		List<Node> bodyEndNodes = new ArrayList<Node>(currentEndNodes);
		List<Node> bodyExcEndNodes = new ArrayList<Node>(currentExitNodes);

		// for breaks;
		List<Node> tempBreakNodes = new ArrayList<Node>();
		List<Node> tempContinueNodes = new ArrayList<Node>();
		bodyExcEndNodes = resolveBreaks(n, tempBreakNodes, bodyExcEndNodes);

		// for continues;
		bodyEndNodes.addAll(tempBreakNodes);
		bodyExcEndNodes = resolveContinues(n, tempContinueNodes, bodyExcEndNodes);

		cond.accept(this);
		finalEndNodes.addAll(tempBreakNodes);
		finalEndNodes.addAll(currentEndNodes);
		finalExcEndNodes.addAll(bodyExcEndNodes);
		finalExcEndNodes.addAll(currentExitNodes);

		currentStartNodes = bodyStartNodes;
		currentEndNodes = finalEndNodes;
		currentExitNodes = finalExcEndNodes;

		addNode(n);

		// connect the nodes
		connectStartNodesToEndNodesOf(cond, body);
		connectStartNodesToEndNodesOf(body, cond);
		for (Node jct1 : tempContinueNodes) {
			jct1.successors.addAll(currentStartNodes);
			for (Node jct2 : currentStartNodes) {
				jct2.predecessors.add(jct1);
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final WhileStatement n) {
		Expression cond = n.getCondition();
		Block body = n.getBody();

		// fill the start/end/exit nodes
		cond.accept(this);
		List<Node> condStartNodes = currentStartNodes;
		List<Node> condEndNodes = currentEndNodes;
		List<Node> condExcEndNodes = currentExitNodes;
		List<Node> finalEndNodes = new ArrayList<Node>();

		body.accept(this);
		List<Node> bodyExcEndNodes = new ArrayList<Node>(currentExitNodes);

		// for breaks;
		List<Node> tempBreakNodes = new ArrayList<Node>();
		List<Node> tempContinueNodes = new ArrayList<Node>();
		bodyExcEndNodes = resolveBreaks(n, tempBreakNodes, bodyExcEndNodes);

		// for continues;
		finalEndNodes.addAll(tempBreakNodes);
		bodyExcEndNodes = resolveContinues(n, tempContinueNodes, bodyExcEndNodes);

		List<Node> finalExitNodes = new ArrayList<Node>();
		finalEndNodes.addAll(condEndNodes);
		finalExitNodes.addAll(bodyExcEndNodes);
		finalExitNodes.addAll(condExcEndNodes);

		currentStartNodes = condStartNodes;
		currentEndNodes = finalEndNodes;
		currentExitNodes = finalExitNodes;

		addNode(n);

		// connect the nodes
		connectStartNodesToEndNodesOf(cond, body);
		connectStartNodesToEndNodesOf(body, cond);
		for (Node jct1 : tempContinueNodes) {
			jct1.successors.addAll(condStartNodes);
			for (Node jct2 : condStartNodes) {
				jct2.predecessors.add(jct1);
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final ForStatement n) {
		Statement initS = n.getInit();
		List<Statement> init = new ArrayList<Statement>();
		init.add(initS);
		Expression cond = n.getCondition();
		Statement stepS = n.getUpdate();
		List<Statement> step = new ArrayList<Statement>();
		step.add(stepS);
		Block body = n.getBody();

		List<Node> tempContinueNodes = new ArrayList<Node>();
		if (init.isEmpty()) {
			List<Node> finalEndNodes = new ArrayList<Node>();

			if (cond != null) {
				// fill the start/end/exit nodes
				cond.accept(this);
				finalEndNodes.addAll(currentEndNodes);

				List<Node> currentStartNodes = this.currentStartNodes;
				body.accept(this);

				List<Node> currentExcEndNodes = new ArrayList<Node>(this.currentExitNodes);

				// for breaks;
				List<Node> tempBreakNodes = new ArrayList<Node>();
				currentExcEndNodes = resolveBreaks(n, tempBreakNodes, currentExcEndNodes);

				// for continues;
				finalEndNodes.addAll(tempBreakNodes);
				currentExcEndNodes = resolveContinues(n, tempContinueNodes, currentExcEndNodes);

				if (!step.isEmpty()) {
					visitStatements(step);
				}

				this.currentStartNodes = currentStartNodes;
				this.currentEndNodes = finalEndNodes;
				this.currentExitNodes = currentExcEndNodes;

				addNode(n);
			} else { /* tree.cond == null, condition is empty. */
				body.accept(this);

				List<Node> currentStartNodes = this.currentStartNodes;
				List<Node> currentExcEndNodes = new ArrayList<Node>(this.currentExitNodes);

				// for breaks;
				List<Node> tempBreakNodes = new ArrayList<Node>();
				currentExcEndNodes = resolveBreaks(n, tempBreakNodes, currentExcEndNodes);

				// for continues;
				finalEndNodes.addAll(tempBreakNodes);
				currentExcEndNodes = resolveContinues(n, tempContinueNodes, currentExcEndNodes);

				if (!step.isEmpty()) {
					visitStatements(step);
				}

				this.currentStartNodes = currentStartNodes;
				this.currentEndNodes = finalEndNodes;
				this.currentExitNodes = currentExcEndNodes;

				addNode(n);
			}
		} else { /* !init.isEmpty() */
			List<Node> finalEndNodes = new ArrayList<Node>();

			visitStatements(init);
			List<Node> currentStartNodes = this.currentStartNodes;

			if (cond != null) {
				cond.accept(this);
			}

			List<Node> currentEndNodes = new ArrayList<Node>(this.currentEndNodes);

			body.accept(this);

			List<Node> currentExcEndNodes = new ArrayList<Node>(this.currentExitNodes);

			// for breaks;
			List<Node> tempBreakNodes = new ArrayList<Node>();
			currentExcEndNodes = resolveBreaks(n, tempBreakNodes, currentExcEndNodes);

			// for continues;
			finalEndNodes.addAll(tempBreakNodes);
			currentExcEndNodes = resolveContinues(n, tempContinueNodes, currentExcEndNodes);

			finalEndNodes.addAll(currentEndNodes);

			if (!step.isEmpty()) {
				visitStatements(step);
			}

			this.currentStartNodes = currentStartNodes;
			this.currentEndNodes = finalEndNodes;
			this.currentExitNodes = currentExcEndNodes;

			addNode(n);

			// connect the nodes
			Node lastStatement = visitList(init);
			if (cond != null) {
				connectStartNodesToEndNodesOf(cond, lastStatement);
			} else {
				connectStartNodesToEndNodesOf(body, lastStatement);
			}
		}

		// connect the nodes
		Node nextStartNodeTree = null;
		if (cond != null) {
			nextStartNodeTree = cond;

			connectStartNodesToEndNodesOf(body, cond);
		} else {
			nextStartNodeTree = body;
		}

		if (!step.isEmpty()) {
			Node lastStatement = visitList(step);
			if (cond != null) {
				connectStartNodesToEndNodesOf(cond, lastStatement);
			} else {
				connectStartNodesToEndNodesOf(body, lastStatement);
			}
			connectStartNodesToEndNodesOf(lastStatement, body);
		} else {
			connectStartNodesToEndNodesOf(nextStartNodeTree, body);
		}

		if (cond != null) {
			for (Node jct1 : tempContinueNodes) {
				jct1.successors.addAll(cond.startNodes);
				for (Node jct2 : cond.startNodes) {
					jct2.predecessors.add(jct1);
				}
			}
		} else {
			for (Node jct1 : tempContinueNodes) {
				jct1.successors.addAll(body.startNodes);
				for (Node jct2 : body.startNodes) {
					jct2.predecessors.add(jct1);
				}
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final ForeachStatement n) {
		Block body = n.getBody();
		Expression expr = n.getCondition();

		// fill the start/end/exit nodes
		init(n);
		order.add(n);

		expr.accept(this);
		body.accept(this);

		List<Node> bodyExcEndNodes = currentExitNodes;

		List<Node> temp_start = new ArrayList<Node>(1);
		temp_start.add(n);
		this.currentStartNodes = temp_start;

		// for breaks;
		List<Node> tempBreakNodes = new ArrayList<Node>();
		List<Node> tempContinueNodes = new ArrayList<Node>();
		bodyExcEndNodes = resolveBreaks(n, tempBreakNodes, bodyExcEndNodes);

		// for continues;
		bodyExcEndNodes = resolveContinues(n, tempContinueNodes, bodyExcEndNodes);

		currentExitNodes = bodyExcEndNodes;

		n.startNodes = currentStartNodes;
		n.endNodes = currentEndNodes;
		n.exitNodes = currentExitNodes;

		for (Node jct : tempBreakNodes) {
			jct.successors.add(n);
			n.predecessors.add(jct);
		}

		// connect the nodes
		connectToStartNodesOf(n, expr);

		connectStartNodesToEndNodesOf(body, expr);

		connectStartNodesToEndNodesOf(body, body);

		for (Node jct1 : tempContinueNodes) {
			jct1.successors.addAll(body.startNodes);
			for (Node jct2 : body.startNodes) {
				jct2.predecessors.add(jct1);
			}
		}
	}

	/* used by visitSwitch and visitCase only, which visit the single node then
	 * the subsequent list. */
	public void switchAndCase(Node single, List<? extends Node> list) {
		Node head = list.get(0);
		if (head != null) {
			connectStartNodesToEndNodesOf(head, single);
			Node prev = head;
			for (int i = 1; i < list.size(); i++) {
				Node node = list.get(i);
				connectStartNodesToEndNodesOf(node, prev);
				prev = node;
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final SwitchCase n) {
		List<Expression> cases = n.getCases();
		Block body = n.getBody();

		if (cases.size() > 0) {
			visitStatements(cases);
			Node pat = visitList(cases);
			List<Node> currentStartNodes = this.currentStartNodes;
			body.accept(this);
			this.currentStartNodes = currentStartNodes;
			addNode(n);
			// connect the nodes
			switchAndCase(pat, body.getStatements());
		} else {
			body.accept(this);
			addNode(n);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final SwitchStatement n) {
		Expression selector = n.getCondition();
		List<SwitchCase> cases = n.getCases();

		// fill the start/end/exit nodes
		selector.accept(this);

		if (n.getDefault() != null)
			cases.add(n.getDefault());

		if (cases.size() > 0) {
			List<Node> finalEndNodes = new ArrayList<Node>();
			List<Node> selectorEndNodes = currentEndNodes;
			List<Node> finalExcEndNodes = new ArrayList<Node>();

			List<Node> previousEndNodes = null;
			// for breaks;
			List<Node> tempBreakNodes = new ArrayList<Node>();

			Iterator<SwitchCase> scIter = cases.iterator();
			while (scIter.hasNext()) {
				SwitchCase c = scIter.next();

				c.accept(this);

				if (previousEndNodes != null) {
					for (Node pre : previousEndNodes) {
						connectStartNodesToEndNodesOf(c, pre);
					}
				}

				connectStartNodesToEndNodesOf(c, selector);

				List<Node> currentExcEndNodes = currentExitNodes;

				// for breaks;
				currentExcEndNodes = resolveBreaks(n, tempBreakNodes, currentExcEndNodes);

				previousEndNodes = currentEndNodes;
				finalExcEndNodes.addAll(currentExcEndNodes);
			}

			if (n.getDefault() == null) {
				finalEndNodes.addAll(selectorEndNodes);
			}

			finalEndNodes.addAll(tempBreakNodes);

			this.currentEndNodes = finalEndNodes;
			this.currentExitNodes = finalExcEndNodes;
		}

		currentStartNodes = new ArrayList<Node>(1);
		currentStartNodes.add(n);

		addNode(n);

		connectToStartNodesOf(n, selector);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final IfStatement n) {
		Expression cond = n.getCondition();
		Block thenpart = n.getBody();
		Block elsepart = n.getElse();

		// fill the start/end/exit nodes
		cond.accept(this);
		List<Node> currentStartNodes = this.currentStartNodes;
		List<Node> currentEndNodes = this.currentEndNodes;

		List<Node> finalEndNodes = new ArrayList<Node>();
		List<Node> finalExcEndNodes = new ArrayList<Node>();

		thenpart.accept(this);
		finalEndNodes.addAll(this.currentEndNodes);
		finalExcEndNodes.addAll(currentExitNodes);
		if (elsepart != null) {
			elsepart.accept(this);
			finalEndNodes.addAll(this.currentEndNodes);
			finalExcEndNodes.addAll(currentExitNodes);
		} else {
			finalEndNodes.addAll(currentEndNodes);
		}

		this.currentStartNodes = currentStartNodes;
		this.currentEndNodes = finalEndNodes;
		this.currentExitNodes = finalExcEndNodes;
		addNode(n);

		// connect the nodes
		connectStartNodesToEndNodesOf(thenpart, cond);

		if (elsepart != null) {
			connectStartNodesToEndNodesOf(elsepart, cond);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final BreakStatement n) {
		// fill the start/end/exit nodes
		currentEndNodes = emptyList;
		currentExitNodes = new ArrayList<Node>(1);
		currentExitNodes.add(n);

		currentStartNodes = new ArrayList<Node>(1);
		currentStartNodes.add(n);
		addNode(n);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final ContinueStatement n) {
		// fill the start/end/exit nodes
		currentEndNodes = emptyList;
		currentExitNodes = new ArrayList<Node>(1);
		currentExitNodes.add(n);

		currentStartNodes = new ArrayList<Node>(1);
		currentStartNodes.add(n);
		addNode(n);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final ReturnStatement n) {
		// fill the start/end/exit nodes
		Expression expr = n.getExpr();

		if (expr != null) {
			expr.accept(this);
		} else {
			currentStartNodes = new ArrayList<Node>(1);
			currentStartNodes.add(n);
		}

		currentEndNodes = emptyList;
		currentExitNodes = new ArrayList<Node>(1);
		currentExitNodes.add(n);

		addNode(n);

		// connect the nodes
		if (expr != null) {
			connectToEndNodesOf(expr, n);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Call n) {
		List<Expression> args = n.getArgs();

		if (!args.isEmpty()) {
			visitStatements(args);
		}

		currentEndNodes = new ArrayList<Node>(1);
		currentEndNodes.add(n);
		currentExitNodes = emptyList;
		addNode(n);

		// connect the nodes
		if (!args.isEmpty()) {
			Node lastArg = visitList(args);
			connectToEndNodesOf(lastArg, n);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final TypeDecl n) {
		singleton(n);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final ArrayType n) {
		singleton(n);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final FunctionType n) {
		singleton(n);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final FixPType n) {
		singleton(n);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final MapType n) {
		singleton(n);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final OutputType n) {
		singleton(n);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final StackType n) {
		singleton(n);
	}
	
	/** {@inheritDoc} */
	@Override
	public void visit(final QueueType n) {
		singleton(n);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final SetType n) {
		singleton(n);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final TupleType n) {
		singleton(n);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final CharLiteral n) {
		singleton(n);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final FloatLiteral n) {
		singleton(n);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final IntegerLiteral n) {
		singleton(n);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final StringLiteral n) {
		singleton(n);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final TimeLiteral n) {
		singleton(n);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final FixPExpression n) {
		Block body = n.getBody();

		body.accept(this);

		currentEndNodes = new ArrayList<Node>(1);
		currentEndNodes.add(n);

		addNode(n);

		// connect the nodes
		connectToEndNodesOf(body, n);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final TraversalExpression n) {
		Block body = n.getBody();

		body.accept(this);

		currentEndNodes = new ArrayList<Node>(1);
		currentEndNodes.add(n);

		addNode(n);

		// connect the nodes
		connectToEndNodesOf(body, n);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final VisitorExpression n) {
		Block body = n.getBody();

		body.accept(this);

		currentEndNodes = new ArrayList<Node>(1);
		currentEndNodes.add(n);

		addNode(n);

		// connect the nodes
		connectToEndNodesOf(body, n);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final SimpleExpr n) {
		Term lhs = n.getLhs();

		lhs.accept(this);

		if (n.getRhsSize() > 0) {
			Term rhs = n.getRhs(0);
			List<Node> currentStartNodes = this.currentStartNodes;
			rhs.accept(this);

			currentEndNodes = new ArrayList<Node>(1);
			currentEndNodes.add(n);

			this.currentStartNodes = currentStartNodes;
			addNode(n);

			// connect the nodes
			connectStartNodesToEndNodesOf(rhs, lhs);
			connectToEndNodesOf(rhs, n);
		} else {
			currentEndNodes = new ArrayList<Node>(1);
			currentEndNodes.add(n);

			addNode(n);

			// connect the nodes
			connectToEndNodesOf(lhs, n);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final ParenExpression n) {
		Expression body = n.getExpression();

		body.accept(this);

		currentEndNodes = new ArrayList<Node>(1);
		currentEndNodes.add(n);

		addNode(n);

		// connect the nodes
		connectToEndNodesOf(body, n);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final FunctionExpression n) {
		Block body = n.getBody();

		body.accept(this);

		currentEndNodes = new ArrayList<Node>(1);
		currentEndNodes.add(n);

		addNode(n);

		// connect the nodes
		connectToEndNodesOf(body, n);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Expression n) {
		Conjunction lhs = n.getLhs();

		// fill the start/end/exit nodes
		lhs.accept(this);

		if (n.getRhsSize() > 0) {
			Conjunction rhs = n.getRhs(0);
			List<Node> currentStartNodes = this.currentStartNodes;
			rhs.accept(this);

			currentEndNodes = new ArrayList<Node>(1);
			currentEndNodes.add(n);

			this.currentStartNodes = currentStartNodes;
			addNode(n);

			// connect the nodes
			connectStartNodesToEndNodesOf(rhs, lhs);
			connectToEndNodesOf(rhs, n);
		} else {
			currentEndNodes = new ArrayList<Node>(1);
			currentEndNodes.add(n);

			addNode(n);

			// connect the nodes
			connectToEndNodesOf(lhs, n);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final FixPStatement n) {
		this.order = new ArrayList<Node>();
		n.getBody().accept(this);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final TraverseStatement n) {
		this.order = new ArrayList<Node>();
		n.getBody().accept(this);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final VisitStatement n) {
		Component lhs = n.getComponent();

		// fill the start/end/exit nodes
		lhs.accept(this);

		Block rhs = n.getBody();
		List<Node> currentStartNodes = this.currentStartNodes;
		rhs.accept(this);

		currentEndNodes = new ArrayList<Node>(1);
		currentEndNodes.add(n);

		this.currentStartNodes = currentStartNodes;
		addNode(n);

		// connect the nodes
		connectStartNodesToEndNodesOf(rhs, lhs);
		connectToEndNodesOf(rhs, n);

	}

	/** {@inheritDoc} */
	@Override
	public void visit(final StopStatement n) {
		// fill the start/end/exit nodes
		singleton(n);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final PostfixStatement n) {
		Expression body = n.getExpr();

		body.accept(this);

		currentEndNodes = new ArrayList<Node>(1);
		currentEndNodes.add(n);

		addNode(n);

		// connect the nodes
		connectToEndNodesOf(body, n);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final IfAllStatement n) {
		Component initS = n.getVar();
		List<Component> init = new ArrayList<Component>(); init.add(initS);
		Expression cond = n.getCondition();
		Block thenpart = n.getBody();

		List<Node> finalEndNodes = new ArrayList<Node>();

		visitStatements(init);
		List<Node> currentStartNodes = this.currentStartNodes;

		// fill the start/end/exit nodes
		cond.accept(this);
		currentStartNodes = this.currentStartNodes;
		List<Node> currentEndNodes = this.currentEndNodes;

		List<Node> finalExcEndNodes = new ArrayList<Node>();

		thenpart.accept(this);
		finalEndNodes.addAll(this.currentEndNodes);
		finalExcEndNodes.addAll(currentExitNodes);
		finalEndNodes.addAll(currentEndNodes);

		this.currentStartNodes = currentStartNodes;
		this.currentEndNodes = finalEndNodes;
		this.currentExitNodes = finalExcEndNodes;
		addNode(n);

		// connect the nodes
		connectStartNodesToEndNodesOf(thenpart, cond);

		// connect the nodes
		Node lastStatement = visitList(init);
		if (cond != null) {
			connectStartNodesToEndNodesOf(cond, lastStatement);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final ExistsStatement n) {
		Component initS = n.getVar();
		List<Component> init = new ArrayList<Component>(); init.add(initS);
		Expression cond = n.getCondition();
		Block thenpart = n.getBody();

		List<Node> finalEndNodes = new ArrayList<Node>();

		visitStatements(init);
		List<Node> currentStartNodes = this.currentStartNodes;

		// fill the start/end/exit nodes
		cond.accept(this);
		currentStartNodes = this.currentStartNodes;
		List<Node> currentEndNodes = this.currentEndNodes;

		List<Node> finalExcEndNodes = new ArrayList<Node>();

		thenpart.accept(this);
		finalEndNodes.addAll(this.currentEndNodes);
		finalExcEndNodes.addAll(currentExitNodes);
		finalEndNodes.addAll(currentEndNodes);

		this.currentStartNodes = currentStartNodes;
		this.currentEndNodes = finalEndNodes;
		this.currentExitNodes = finalExcEndNodes;
		addNode(n);

		// connect the nodes
		connectStartNodesToEndNodesOf(thenpart, cond);

		// connect the nodes
		Node lastStatement = visitList(init);
		if (cond != null) {
			connectStartNodesToEndNodesOf(cond, lastStatement);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final ExprStatement n) {
		Expression body = n.getExpr();

		body.accept(this);

		currentEndNodes = new ArrayList<Node>(1);
		currentEndNodes.add(n);

		addNode(n);

		// connect the nodes
		connectToEndNodesOf(body, n);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final EmitStatement n) {
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final AssignmentStatement n) {
		Factor lhs = n.getLhs();
		Expression rhs = n.getRhs();
		// fill the start/end/exit nodes
		lhs.accept(this);

		List<Node> currentStartNodes = this.currentStartNodes;
		rhs.accept(this);

		currentEndNodes = new ArrayList<Node>(1);
		currentEndNodes.add(n);

		this.currentStartNodes = currentStartNodes;
		addNode(n);

		// connect the nodes
		connectStartNodesToEndNodesOf(rhs, lhs);
		connectToEndNodesOf(rhs, n);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Comparison n) {
		SimpleExpr lhs = n.getLhs();

		// fill the start/end/exit nodes
		lhs.accept(this);

		if (n.hasRhs()) {
			SimpleExpr rhs = n.getRhs();
			List<Node> currentStartNodes = this.currentStartNodes;
			rhs.accept(this);

			currentEndNodes = new ArrayList<Node>(1);
			currentEndNodes.add(n);

			this.currentStartNodes = currentStartNodes;
			addNode(n);

			// connect the nodes
			connectStartNodesToEndNodesOf(rhs, lhs);
			connectToEndNodesOf(rhs, n);
		} else {
			currentEndNodes = new ArrayList<Node>(1);
			currentEndNodes.add(n);

			addNode(n);

			// connect the nodes
			connectToEndNodesOf(lhs, n);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Component n) {
		Identifier i = n.getIdentifier();
		i.accept(this);

		currentEndNodes = new ArrayList<Node>(1);
		currentEndNodes.add(n);
		currentExitNodes = emptyList;

		addNode(n);

		// connect the nodes
		connectToEndNodesOf(i, n);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Composite n) {
		List<Expression> exprs = n.getExprs();
		if (exprs.size() > 0) {
			visitStatements(exprs);
			addNode(n);
			visitList(exprs);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Pair n) {
		Expression lhs = n.getExpr1();
		Expression rhs = n.getExpr2();
		// fill the start/end/exit nodes
		lhs.accept(this);

		List<Node> currentStartNodes = this.currentStartNodes;
		rhs.accept(this);

		currentEndNodes = new ArrayList<Node>(1);
		currentEndNodes.add(n);

		this.currentStartNodes = currentStartNodes;
		addNode(n);

		// connect the nodes
		connectStartNodesToEndNodesOf(rhs, lhs);
		connectToEndNodesOf(rhs, n);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Conjunction n) {
		Comparison lhs = n.getLhs();
		// fill the start/end/exit nodes
		lhs.accept(this);

		if (n.getRhsSize() > 0) {
			Comparison rhs = n.getRhs(0);
			List<Node> currentStartNodes = this.currentStartNodes;
			rhs.accept(this);

			currentEndNodes = new ArrayList<Node>(1);
			currentEndNodes.add(n);

			this.currentStartNodes = currentStartNodes;
			addNode(n);

			// connect the nodes
			connectStartNodesToEndNodesOf(rhs, lhs);
			connectToEndNodesOf(rhs, n);
		} else {
			currentEndNodes = new ArrayList<Node>(1);
			currentEndNodes.add(n);

			addNode(n);

			// connect the nodes
			connectToEndNodesOf(lhs, n);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Factor n) {
		Operand meth = n.getOperand();
		List<Node> args = n.getOps();
		// fill the start/end/exit nodes
		meth.accept(this);

		List<Node> startNodes = currentStartNodes;

		if (!args.isEmpty()) {
			visitStatements(args);
		}

		currentStartNodes = startNodes;

		currentEndNodes = new ArrayList<Node>(1);
		currentEndNodes.add(n);
		currentExitNodes = emptyList;
		addNode(n);

		// connect the nodes
		if (!args.isEmpty()) {
			Node lastArg = visitList(args);
			connectStartNodesToEndNodesOf(args.get(0), meth);
			connectToEndNodesOf(lastArg, n);
		} else {
			connectToEndNodesOf(meth, n);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final UnaryFactor n) {
		Factor f = n.getFactor();
		f.accept(this);

		currentEndNodes = new ArrayList<Node>(1);
		currentEndNodes.add(n);
		currentExitNodes = emptyList;

		addNode(n);

		// connect the nodes
		connectToEndNodesOf(f, n);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Identifier n) {
		singleton(n);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Index n) {
		Expression lhs = n.getStart();

		// fill the start/end/exit nodes
		lhs.accept(this);

		if (n.hasEnd()) {
			Expression rhs = n.getEnd();
			List<Node> currentStartNodes = this.currentStartNodes;
			rhs.accept(this);

			currentEndNodes = new ArrayList<Node>(1);
			currentEndNodes.add(n);

			this.currentStartNodes = currentStartNodes;
			addNode(n);

			// connect the nodes
			connectStartNodesToEndNodesOf(rhs, lhs);
			connectToEndNodesOf(rhs, n);
		} else {
			currentEndNodes = new ArrayList<Node>(1);
			currentEndNodes.add(n);

			addNode(n);

			// connect the nodes
			connectToEndNodesOf(lhs, n);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Selector n) {
		Identifier selected = n.getId();

		selected.accept(this);

		currentEndNodes = new ArrayList<Node>(1);
		currentEndNodes.add(n);
		currentExitNodes = emptyList;

		addNode(n);

		// connect the nodes
		connectToEndNodesOf(selected, n);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Term n) {
		Factor lhs = n.getLhs();
		// fill the start/end/exit nodes
		lhs.accept(this);

		if (n.getRhsSize() > 0) {
			Factor rhs = n.getRhs(0);
			List<Node> currentStartNodes = this.currentStartNodes;
			rhs.accept(this);

			currentEndNodes = new ArrayList<Node>(1);
			currentEndNodes.add(n);

			this.currentStartNodes = currentStartNodes;
			addNode(n);

			// connect the nodes
			connectStartNodesToEndNodesOf(rhs, lhs);
			connectToEndNodesOf(rhs, n);
		} else {
			currentEndNodes = new ArrayList<Node>(1);
			currentEndNodes.add(n);

			addNode(n);

			// connect the nodes
			connectToEndNodesOf(lhs, n);
		}
	}

	// ######################### Private methods ###########################

	private void singleton(Node node) {
		currentEndNodes = new ArrayList<Node>(1);
		currentEndNodes.add(node);
		currentExitNodes = emptyList;

		if (currentStartNodes == null) {
			currentStartNodes = new ArrayList<Node>(1);
			currentStartNodes.add(node);
		}

		addNode(node);

		currentStartNodes = new ArrayList<Node>(1);
		currentStartNodes.add(node);
	}

	private void addNode(Node node) {
		node.startNodes = currentStartNodes;
		node.endNodes = currentEndNodes;
		node.exitNodes = currentExitNodes;

		init(node);
		order.add(node);
	}

	private static void init(Node node) {
		if (node.predecessors == null) {
			node.predecessors = new ArrayList<Node>();
		}

		if (node.successors == null) {
			node.successors = new ArrayList<Node>();
		}
	}

	private Node visitList(List<? extends Node> nodes) {
		Node last = null;
		Iterator<? extends Node> nodesIter = (Iterator<? extends Node>) nodes.iterator();

		if (nodes.size() > 0) {
			last = nodesIter.next();

			while (nodesIter.hasNext()) {
				Node current = nodesIter.next();
				connectStartNodesToEndNodesOf(current, last);
				last = current;
			}
		}
		return last;
	}

	private static void connectToEndNodesOf(Node start, Node end) {
		for (Node endNode : start.endNodes) {
			if (endNode.successors != null) {
				endNode.successors.add(end);
			}
			if (end.predecessors != null) {
				end.predecessors.add(endNode);
			}
		}
	}

	private static void connectStartNodesToEndNodesOf(Node start, Node end) {
		for (Node endNode : end.endNodes) {
			for (Node startNode : start.startNodes) {
				endNode.successors.add(startNode);
				startNode.predecessors.add(endNode);
			}
		}
	}

	private static void connectToStartNodesOf(Node start, Node end) {
		for (Node startNode : end.startNodes) {
			startNode.predecessors.add(start);
			start.successors.add(startNode);
		}
	}

	private void visitStatements(List<? extends Node> statements) {
		ArrayList<Node> finalExcEndNodes = new ArrayList<Node>();
		Iterator<? extends Node> nodesIter = (Iterator<? extends Node>) statements.iterator();
		Node head = nodesIter.next();
		if (head != null) {
			head.accept(this);
			List<Node> currentStartNodes = this.currentStartNodes;
			List<Node> currentExcEndNodes = this.currentExitNodes;

			finalExcEndNodes.addAll(currentExcEndNodes);

			while (nodesIter.hasNext()) {
				head = nodesIter.next();
				head.accept(this);
				finalExcEndNodes.addAll(this.currentExitNodes);
			}
			this.currentStartNodes = currentStartNodes;
			this.currentExitNodes = finalExcEndNodes;
		}
	}

	private static List<Node> resolveBreaks(Node target, List<Node> endNodes, List<Node> nodes) {
		List<Node> remaining = new ArrayList<Node>();
		for (Node node : nodes) {
			if (node instanceof BreakStatement) {
				endNodes.add(node);
			}
		}
		return remaining;
	}

	private static List<Node> resolveContinues(Node target, List<Node> endNodes, List<Node> nodes) {
		List<Node> remaining = new ArrayList<Node>();
		for (Node node : nodes) {
			if (node instanceof ContinueStatement) {
				endNodes.add(node);
			}
		}
		return remaining;
	}
}
