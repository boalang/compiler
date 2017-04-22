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
import boa.compiler.ast.Operand;
import boa.compiler.ast.Pair;
import boa.compiler.ast.Selector;
import boa.compiler.ast.Term;
import boa.compiler.ast.UnaryFactor;
import boa.compiler.ast.expressions.Expression;
import boa.compiler.ast.expressions.SimpleExpr;
import boa.compiler.ast.statements.AssignmentStatement;
import boa.compiler.ast.statements.ReturnStatement;
import boa.compiler.ast.statements.VarDeclStatement;
import boa.compiler.visitors.*;

public class ReachingDefinitionGen extends AbstractVisitorNoArg {
	ArrayList<String> variablesMonitored = new ArrayList<String>();
	boolean inReturnStatement = false;
	HashMap<Node,HashSet<String>> killVariable = new HashMap<Node,HashSet<String>>();
	HashMap<Node,HashSet<String>> genVariable = new HashMap<Node,HashSet<String>>();

	HashMap<Integer,Integer> gen = new HashMap<Integer,Integer>();
	HashMap<String,HashSet<Integer>> defs = new HashMap<String,HashSet<Integer>>();

	Node currentNode = null;
	boolean killFlag = false, genFlag = false;

	public final void dfs(final Node node, java.util.HashMap<Integer,String> nodeVisitStatus) {
		currentNode = node;
		node.accept(this);
		nodeVisitStatus.put(node.nodeId,"visited");
		for (Node succ : node.successors) {
		    if (nodeVisitStatus.get(succ.nodeId).equals("unvisited")) {
			dfs(succ, nodeVisitStatus);
		    }
		}
	}

	public void start(CFGBuildingVisitor cfgBuilder) {
		java.util.HashMap<Integer,String> nodeVisitStatus = new java.util.HashMap<Integer,String>();
		for(Node subnode : cfgBuilder.order) {
			nodeVisitStatus.put(subnode.nodeId, "unvisited");
		}
		nodeVisitStatus.put(cfgBuilder.currentStartNodes.get(0).nodeId, "visited");
		dfs(cfgBuilder.currentStartNodes.get(0), nodeVisitStatus);
	}

	public void visit(final AssignmentStatement n) {
		Factor lhs = n.getLhs();
		killFlag = true;
		gen.put(currentNode.nodeId, currentNode.nodeId);
		lhs.accept(this);
		killFlag = false;
		genFlag = true;
		Expression rhs = n.getRhs();
		rhs.accept(this);
		genFlag = false;
	}

	public void visit(final Expression n) {
		n.getLhs().accept(this);
		for (final Conjunction c : n.getRhs())
			c.accept(this);
	}

	public void visit(final ReturnStatement n) {
		inReturnStatement = true;
		n.getExpr().getLhs().accept(this);
		inReturnStatement = false;
	}

	public void visit(final VarDeclStatement n) {
		killFlag = true;
		gen.put(currentNode.nodeId, currentNode.nodeId);
		n.getId().accept(this);
		killFlag = false;
		genFlag = true;
		if(n.hasInitializer()) {
			n.getInitializer().accept(this);
		}
		genFlag = false;
	}

	public void visit(final Comparison n) {
		n.getLhs().accept(this);
		if (n.hasRhs())
			n.getRhs().accept(this);
	}

	public void visit(final Component n) {
		if (n.hasIdentifier())
			n.getIdentifier().accept(this);
	}

	public void visit(final Composite n) {
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
		if(inReturnStatement)
			variablesMonitored.add(n.getToken());
		if(killFlag) {
			if(defs.containsKey(n.getToken())) {
				HashSet<Integer> tmp = defs.get(n.getToken());
				tmp.add(currentNode.nodeId);
				defs.put(n.getToken(), tmp);
			}
			else {
				HashSet<Integer> tmp = new HashSet<Integer>();
				tmp.add(currentNode.nodeId);
				defs.put(n.getToken(), tmp);
			}			
			if(killVariable.containsKey(currentNode)) {
				HashSet<String> tmp = killVariable.get(currentNode);
				tmp.add(n.getToken());
				killVariable.put(currentNode, tmp);
			}
			else {
				HashSet<String> tmp = new HashSet<String>();
				tmp.add(n.getToken());
				killVariable.put(currentNode, tmp);
			}
		}
		if(genFlag) {
			if(genVariable.containsKey(currentNode)) {
				HashSet<String> tmp = genVariable.get(currentNode);
				tmp.add(n.getToken());
				genVariable.put(currentNode, tmp);
			}
			else {
				HashSet<String> tmp = new HashSet<String>();
				tmp.add(n.getToken());
				genVariable.put(currentNode, tmp);
			}
		}
	}

	public void visit(final Term n) {
		n.getLhs().accept(this);
		for (final Factor f : n.getRhs())
			f.accept(this);
	}

	public void visit(final SimpleExpr n) {
		n.getLhs().accept(this);
		for (final Term t : n.getRhs())
			t.accept(this);
	}
}
