package boa.analysis.ds;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import boa.types.Ast.Expression;
import boa.types.Ast.Expression.ExpressionKind;
import boa.types.Ast.Method;
import boa.types.Ast.Statement;
import boa.types.Ast.Statement.StatementKind;
import boa.types.Ast.Variable;
import boa.types.Control.Edge.EdgeLabel;

public class Graph {
	Method method;
	boa.types.Control.Graph graph;
	Map<Integer, Node> nodes = new HashMap<Integer, Node>();
	Map<Node, Integer> nodeIds = new HashMap<Node, Integer>();
	int size;
	EdgeLabel edges[][];

	public Graph(Method method, boa.types.Control.Graph graph) {
		this.method = method;
		this.graph = graph;
		process();
	}

	private final void process() {
		this.nodes = new StatementIndexer().processMethodStatements(method,
				graph);
		constructNodeIndexMap();
		this.size = nodes.size();
		this.edges = new EdgeLabel[size][size];
		if (this.nodes.size() != graph.getNodesCount()) {
			return;
		}

		try {
			for (int i = 0; i < size; i++) {
				for (int j = 0; j < size; j++) {
					int index = i * size + j;
					edges[i][j] = graph.getEdges(index).getLabel();
				}
			}
		} catch (Exception e) {
			System.out.println(graph.getEdgesCount());
			e.printStackTrace();

		}
	}

	private final void constructNodeIndexMap() {
		for (Entry<Integer, Node> nodeEntry : nodes.entrySet()) {
			nodeIds.put(nodeEntry.getValue(), nodeEntry.getKey());
		}
	}

	public final List<Node> getPredsOf(Node node) {
		List<Node> preds = new ArrayList<Node>();
		int index = node.node.getId();
		for (int i = 0; i < size; i++) {
			EdgeLabel edgeLabel = edges[i][index];
			if (edgeLabel != EdgeLabel.NIL) {
				Node pred = nodes.get(i);
				preds.add(pred);
			}
		}
		return preds;
	}

	public final List<Node> getSuccsOf(Node node) {
		List<Node> succs = new ArrayList<Node>();
		int index = node.node.getId();
		for (int i = 0; i < size; i++) {
			EdgeLabel edgeLabel = edges[index][i];
			if (edgeLabel != EdgeLabel.NIL) {
				Node succ = nodes.get(i);
				succs.add(succ);
			}
		}
		return succs;
	}

	public final int size() {
		return size;
	}

	public final Iterator<Node> iterator() {
		return this.nodes.values().iterator();
	}

	public final List<Variable> getLocals() {
		List<Variable> locals = new ArrayList<Variable>();
		for (Iterator<Node> i = nodes.values().iterator(); i.hasNext();) {
			Node o = i.next();
			if (o.expr != null) {
				if (o.expr.getKind() == ExpressionKind.VARDECL) {
					for (Iterator<Variable> j = o.expr.getVariableDeclsList()
							.iterator(); j.hasNext();) {
						locals.add(j.next());
					}
				}
			}
		}
		return locals;
	}

	public final List<String> getUses() {
		List<String> uses = new ArrayList();
		for (Iterator<Node> i = nodes.values().iterator(); i.hasNext();) {
			Node o = i.next();
			if (o.expr != null) {
				if (o.expr.getKind() == ExpressionKind.VARACCESS) {
					uses.add(o.expr.getVariable());
				}
			}
		}
		return uses;
	}

	public final List<String> getUses(Node o) {
		List<String> uses = new ArrayList();
		if (o.expr != null) {
			if (o.expr.getKind() == ExpressionKind.VARACCESS) {
				uses.add(o.expr.getVariable());
			}
		}
		return uses;
	}

	public final List<String> getUses(Expression o) {
		List<String> uses = new ArrayList();
		if (o != null) {

			if (o.getKind() == ExpressionKind.VARACCESS) {
				uses.add(o.getVariable());
			}

			if (o.getExpressionsCount() > 0) {
				Iterator<Expression> subExpsIt = o.getExpressionsList()
						.iterator();

				while (subExpsIt.hasNext()) {
					Expression subExp = subExpsIt.next();

					if (subExp.getKind() == ExpressionKind.VARACCESS) {
						uses.add(subExp.getVariable());
					}
				}
			}
		}
		return uses;
	}

	public final List<String> getDefs(Statement s) {
		List<String> vars = new ArrayList();
		if (s.getKind() == StatementKind.EXPRESSION) {
			Expression expr = s.getExpression();
			if (expr.getKind() == ExpressionKind.ASSIGN) {
				Expression left = expr.getExpressions(0);
				vars.add(left.getVariable());
			}
		}
		return vars;
	}

	public final int getLocalCount() {
		return getLocals().size();
	}

	public final List<Node> getHeads() {
		List<Node> heads = new ArrayList<Node>();
		return heads;
	}

	public final List<Node> getTails() {
		List<Node> tails = new ArrayList<Node>();
		return tails;
	}

	public final String getPattern() {
		String pattern = "";
		for (int i = 0; i < size; i++)
			for (int j = 0; j < size; j++)
				pattern += getSymbol(edges[i][j]);
		return pattern;
	}

	private final String getSymbol(EdgeLabel l) {
		switch (l.getNumber()) {
		case EdgeLabel.NIL_VALUE:
			return "0";
		case EdgeLabel.DEFAULT_VALUE:
			return "1";
		case EdgeLabel.TRUE_VALUE:
			return "2";
		case EdgeLabel.FALSE_VALUE:
			return "3";
		case EdgeLabel.BACKEDGE_VALUE:
			return "4";
		case EdgeLabel.EXITEDGE_VALUE:
			return "5";
		case EdgeLabel.DATA_VALUE:
			return "6";
		case EdgeLabel.PDG_VALUE:
			return "7";
		}
		return "";
	}
}
