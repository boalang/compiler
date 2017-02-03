package boa.compiler.visitors.analysis;

import java.util.*;

import boa.compiler.ast.Call;
import boa.compiler.ast.Comparison;
import boa.compiler.ast.Component;
import boa.compiler.ast.Composite;
import boa.compiler.ast.Conjunction;
import boa.compiler.ast.Factor;
import boa.compiler.ast.Identifier;
import boa.compiler.ast.Index;
import boa.compiler.ast.Node;
import boa.compiler.TypeCheckException;
import boa.compiler.ast.Operand;
import boa.compiler.ast.Pair;
import boa.compiler.ast.Selector;
import boa.compiler.ast.Term;
import boa.compiler.ast.UnaryFactor;
import boa.compiler.ast.expressions.Expression;
import boa.types.*;
import boa.compiler.visitors.*;

public class InformationAnalysis extends AbstractVisitorNoArg {
	public CFGBuildingVisitor cfgBuilder;
	public boolean unionFound = false;
	public boolean intersectionFound = false;
	public boolean addFound = false;
	public boolean removeFound = false;
	public HashSet<Identifier> getValueNodesAlias = new HashSet<Identifier>();
	public HashSet<Identifier> totalGetValueNodes = new HashSet<Identifier>();
	protected final IdentifierFindingVisitor idFinder = new IdentifierFindingVisitor();
	protected final CallFindingVisitor callFinder = new CallFindingVisitor();
	protected boolean abortGeneration = false;
	public boolean argFlag = false;
	public boolean genFlag = false;
	public boolean killFlag = false;
	HashSet<String> mergeOperation = new HashSet<String>(); 
	int satisfiedNodes = 0;

	protected class CallFindingVisitor extends AbstractVisitorNoArg {
		protected boolean isCall;

		public boolean isCall() {
			return isCall;
		}

		/** {@inheritDoc} */
		@Override
		public void initialize() {
			super.initialize();
			isCall = false;
		}

		/** {@inheritDoc} */
		@Override
		public void visit(final Factor n) {
			for (final Node node : n.getOps()) {
				isCall = false;
				node.accept(this);
			}
		}

		/** {@inheritDoc} */
		@Override
		public void visit(final Call n) {
			isCall = true;
		}
	}

	protected class IdentifierFindingVisitor extends AbstractVisitorNoArg {
		protected final Set<String> names = new HashSet<String>();

		public Set<String> getNames() {
			return names;
		}

		/** {@inheritDoc} */
		@Override
		protected void initialize() {
			names.clear();
		}

		/** {@inheritDoc} */
		@Override
		public void visit(final Identifier n) {
			names.add(n.getToken());
		}
	}

	public final void dfs(final Node node, java.util.HashMap<Integer,String> nodeVisitStatus) {
		nodeVisitStatus.put(node.nodeId,"visited");
		node.accept(this);
		
		for (Node succ : node.successors) {
		    if (nodeVisitStatus.get(succ.nodeId).equals("unvisited")) {
			dfs(succ, nodeVisitStatus);
		    }
		}
	}

	public void start(CFGBuildingVisitor cfgBuilder, HashSet<Identifier> getValueNodesAlias, HashSet<Identifier> totalGetValueNodes) {
		this.getValueNodesAlias = getValueNodesAlias;
		this.totalGetValueNodes = totalGetValueNodes;
		java.util.HashMap<Integer,String> nodeVisitStatus = new java.util.HashMap<Integer,String>();
		for(Node subnode : cfgBuilder.order) {
			nodeVisitStatus.put(subnode.nodeId, "unvisited");
		}
		nodeVisitStatus.put(cfgBuilder.currentStartNodes.get(0).nodeId, "visited");
		dfs(cfgBuilder.currentStartNodes.get(0), nodeVisitStatus);
	}

	public void visit(final Call n) {
		try {
			this.idFinder.start(n.env.getOperand());
			final String funcName = this.idFinder.getNames().toArray()[0].toString();

			if(funcName.equals("union")) {
				unionFound = true;
				visit(n.getArgs());
				if(satisfiedNodes == 3) {
					mergeOperation.add("union");
				}
				else if(satisfiedNodes > 1 && satisfiedNodes < 3)
					genFlag = true;

				unionFound = false;
			}
			if(funcName.equals("intersect")) {
				intersectionFound = true;
				visit(n.getArgs());
					if(satisfiedNodes == 3) {
						mergeOperation.add("intersection");
					}
					else if(satisfiedNodes > 1 && satisfiedNodes < 3) {
						killFlag = true;
					}

				intersectionFound = false;
			}
			satisfiedNodes = 0;
			if(funcName.equals("add") || funcName.equals("addall")) {
				addFound = true;
				visit(n.getArgs());
				addFound = false;
			}
			if(funcName.equals("remove") || funcName.equals("removeall")) {
				removeFound = true;
				visit(n.getArgs());
				removeFound = false;
			}
		}
		catch(Exception e) {
		}
	}

	public void visit(final Identifier n) {
		if(unionFound && argFlag) {	
			for(Identifier getValueNodeAlias : getValueNodesAlias) {
				if(getValueNodeAlias.getToken().equals(n.getToken())) {
					satisfiedNodes++;
					break;
				}		
			}
			for(Identifier getValueNodeAlias : totalGetValueNodes) {
				if(getValueNodeAlias.getToken().equals(n.getToken())) {
					satisfiedNodes++;
					break;
				}		
			}
			argFlag = false;
		}
		if(intersectionFound && argFlag) {
			for(Identifier getValueNodeAlias : getValueNodesAlias) {
				if(getValueNodeAlias.getToken().equals(n.getToken())) {
					satisfiedNodes++;
					break;
				}		
			}
			for(Identifier getValueNodeAlias : totalGetValueNodes) {
				if(getValueNodeAlias.getToken().equals(n.getToken())) {
					satisfiedNodes++;
					break;
				}		
			}
			argFlag = false;
		}

		if(addFound && argFlag) {
			for(Identifier getValueNodeAlias : getValueNodesAlias) {
				if(getValueNodeAlias.getToken().equals(n.getToken())) {
					genFlag = true;
					break;
				}		
			}
			argFlag = false;
		}
		if(removeFound && argFlag) {
			for(Identifier getValueNodeAlias : getValueNodesAlias) {
				if(getValueNodeAlias.getToken().equals(n.getToken())) {
					killFlag = true;
					break;
				}		
			}
			argFlag = false;
		}
	}

	protected List<BoaType> check(final Call c) {
		if (c.getArgsSize() > 0)
			return this.check(c.getArgs());

		return new ArrayList<BoaType>();
	}

	protected List<BoaType> check(final List<Expression> el) {
		final List<BoaType> types = new ArrayList<BoaType>();

		for (final Expression e : el) {
			// special case of a function call, use its return type instead of function type
			if (e.type instanceof BoaFunction) {
				callFinder.start(e);
				if (callFinder.isCall()) {
					types.add(((BoaFunction) e.type).getType());
					continue;
				}
			}

			types.add(e.type);
		}

		return types;
	}

	public void visit(final Factor n) {
		if (n.getOpsSize() > 0) {
			n.env.setOperand(n.getOperand());
			abortGeneration = false;

			if (!(n.getOp(0) instanceof Call)) {
				n.getOperand().accept(this);
				n.env.setOperandType(n.getOperand().type);
			}

			for (int i = 0; !abortGeneration && i < n.getOpsSize(); i++) {
				final Node o = n.getOp(i);
				o.accept(this);
			}
		} else {
			n.getOperand().accept(this);
		}
	}

	protected void visit(final List<? extends Node> nl) {
		for (final Node n : nl) {
			argFlag = true;
			n.accept(this);
		}
	}
}
