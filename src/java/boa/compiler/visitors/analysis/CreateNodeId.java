package boa.compiler.visitors.analysis;

import java.util.*;

import boa.compiler.ast.Node;
import boa.compiler.visitors.*;

public class CreateNodeId extends AbstractVisitorNoArg {
	int id = 0;

	public final void createNodeIds(final Node node, java.util.HashMap<Node,String> nodeVisitStatus) {
		nodeVisitStatus.put(node,"visited");
		node.nodeId = ++id;
		for (Node succ : node.successors) {
		    if (nodeVisitStatus.get(succ).equals("unvisited")) {
			createNodeIds(succ, nodeVisitStatus);
		    }
		}
	}

	public void start(CFGBuildingVisitor cfgBuilder) {
		java.util.HashMap<Node,String> nodeVisitStatus1 = new java.util.HashMap<Node,String>();
		for(Node subnode : cfgBuilder.order) {
			nodeVisitStatus1.put(subnode, "unvisited");
		}
		nodeVisitStatus1.put(cfgBuilder.currentStartNodes.get(0), "visited");
		createNodeIds(cfgBuilder.currentStartNodes.get(0), nodeVisitStatus1);
	}	
}
