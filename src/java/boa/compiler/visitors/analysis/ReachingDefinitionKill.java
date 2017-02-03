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
import boa.compiler.ast.statements.VarDeclStatement;
import boa.compiler.visitors.*;

public class ReachingDefinitionKill extends AbstractVisitorNoArg {
	HashMap<Integer,HashSet<Integer>> kill = new HashMap<Integer,HashSet<Integer>>();
	HashMap<String,HashSet<Integer>> defs = new HashMap<String,HashSet<Integer>>();
	Node currentNode = null;
	boolean killFlag = false;

	public ReachingDefinitionKill(HashMap<String,HashSet<Integer>> defs) {
		super();
		this.defs = defs;	
	}
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
		lhs.accept(this);
		killFlag = false;
		// fill the start/end/exit nodes
	}

	public void visit(final Expression n) {
		n.getLhs().accept(this);
		for (final Conjunction c : n.getRhs())
			c.accept(this);
	}

	public void visit(final VarDeclStatement n) {
		killFlag = true;
		n.getId().accept(this);
		killFlag = false;
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
		if(killFlag) {
			if(defs.containsKey(n.getToken())) {
				HashSet<Integer> tmp = (HashSet<Integer>)defs.get(n.getToken()).clone();
				tmp.remove(currentNode.nodeId);
				kill.put(currentNode.nodeId, tmp);
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
