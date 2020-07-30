package boa.datagen.treed.python;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.dltk.ast.ASTNode;
import org.eclipse.dltk.ast.ASTVisitor;
import org.eclipse.dltk.ast.declarations.MethodDeclaration;
import org.eclipse.dltk.ast.declarations.ModuleDeclaration;
import org.eclipse.dltk.ast.declarations.TypeDeclaration;
import org.eclipse.dltk.ast.expressions.Expression;
import org.eclipse.dltk.ast.expressions.Literal;
import org.eclipse.dltk.ast.statements.Block;
import org.eclipse.dltk.ast.statements.Statement;
import org.eclipse.dltk.python.parser.ast.PythonArgument;
import org.eclipse.dltk.python.parser.ast.PythonAssertStatement;
import org.eclipse.dltk.python.parser.ast.PythonDelStatement;
import org.eclipse.dltk.python.parser.ast.PythonExceptStatement;
import org.eclipse.dltk.python.parser.ast.PythonForStatement;
import org.eclipse.dltk.python.parser.ast.PythonImportFromStatement;
import org.eclipse.dltk.python.parser.ast.PythonImportStatement;
import org.eclipse.dltk.python.parser.ast.PythonTryStatement;
import org.eclipse.dltk.python.parser.ast.PythonWhileStatement;
import org.eclipse.dltk.python.parser.ast.PythonWithStatement;
import org.eclipse.dltk.python.parser.ast.PythonYieldStatement;
import org.eclipse.dltk.python.parser.ast.expressions.Assignment;
import org.eclipse.dltk.python.parser.ast.expressions.BinaryExpression;
import org.eclipse.dltk.python.parser.ast.expressions.CallHolder;
import org.eclipse.dltk.python.parser.ast.expressions.IndexHolder;
import org.eclipse.dltk.python.parser.ast.expressions.PrintExpression;
import org.eclipse.dltk.python.parser.ast.expressions.PythonForListExpression;
import org.eclipse.dltk.python.parser.ast.expressions.PythonImportExpression;
import org.eclipse.dltk.python.parser.ast.expressions.PythonLambdaExpression;
import org.eclipse.dltk.python.parser.ast.expressions.PythonListForExpression;
import org.eclipse.dltk.python.parser.ast.expressions.PythonSubscriptExpression;
import org.eclipse.dltk.python.parser.ast.expressions.ShortHandIfExpression;
import org.eclipse.dltk.python.parser.ast.expressions.UnaryExpression;
import org.eclipse.dltk.python.parser.ast.statements.BreakStatement;
import org.eclipse.dltk.python.parser.ast.statements.ContinueStatement;
import org.eclipse.dltk.python.parser.ast.statements.ExecStatement;
import org.eclipse.dltk.python.parser.ast.statements.GlobalStatement;
import org.eclipse.dltk.python.parser.ast.statements.IfStatement;
import org.eclipse.dltk.python.parser.ast.statements.ReturnStatement;
import org.eclipse.dltk.python.parser.ast.statements.SimpleStatement;
import org.eclipse.dltk.python.parser.ast.statements.TryFinallyStatement;

import boa.types.Shared.ChangeKind;

public class TreedMapper implements TreedConstants {
	private ASTNode astM, astN;
	private HashMap<String, Integer> nodeTypes = new HashMap<>();
	private int type = 1;

	private HashMap<ASTNode, ArrayList<ASTNode>> tree = new HashMap<ASTNode, ArrayList<ASTNode>>();
	private HashMap<ASTNode, Integer> treeHeight = new HashMap<ASTNode, Integer>(),
			treeDepth = new HashMap<ASTNode, Integer>();
	private HashMap<ASTNode, HashMap<String, Integer>> treeVector = new HashMap<ASTNode, HashMap<String, Integer>>();
	private HashMap<ASTNode, HashMap<ASTNode, Double>> treeMap = new HashMap<ASTNode, HashMap<ASTNode, Double>>();
	private HashSet<ASTNode> pivotsM = new HashSet<ASTNode>(), pivotsN = new HashSet<ASTNode>();
	private int numOfChanges = 0, numOfUnmaps = 0, numOfNonNameUnMaps = 0;

	public TreedMapper(ASTNode astM, ASTNode astN) {
		this.astM = astM;
		this.astN = astN;
	}

	public int getNumOfChanges() {
		return numOfChanges;
	}

	public int getNumOfUnmaps() {
		return numOfUnmaps;
	}

	public boolean isChanged() {
		return this.numOfChanges > 0;
	}

	public boolean hasUnmap() {
		return this.numOfUnmaps > 0;
	}

	public boolean hasNonNameUnmap() {
		return this.numOfNonNameUnMaps > 0;
	}
	
	public void clear()
	{
		this.nodeTypes.clear();
		this.tree.clear();
		this.treeHeight.clear();
		this.treeDepth.clear();
		this.treeVector.clear();
		this.treeMap.clear();
		this.pivotsM.clear();
		this.pivotsN.clear();
	}
	public void map() throws Exception {
		buildTrees();
		mapPivots();
		mapBottomUp();
//		mapMoving();
		mapTopDown();
		markChanges();
		markUnchanges(astM);
	}

	private void markUnchanges(ASTNode node) {
		ArrayList<ASTNode> children = tree.get(node);
		for (ASTNode child : children)
			markUnchanges(child);
		ChangeKind status = (ChangeKind) node.getProperty(PROPERTY_STATUS);
		if (status == ChangeKind.UNCHANGED) {
			boolean unchanged = isUnchanged(children);
			if (unchanged) {
				ASTNode mappedNode = (ASTNode) node.getProperty(PROPERTY_MAP);
				unchanged = isUnchanged(tree.get(mappedNode));
			}
			if (!unchanged) {
				ASTNode mappedNode = (ASTNode) node.getProperty(PROPERTY_MAP);
				node.setProperty(PROPERTY_STATUS, ChangeKind.MODIFIED);
				mappedNode.setProperty(PROPERTY_STATUS, ChangeKind.MODIFIED);
			}
		}
	}

	private boolean isUnchanged(ArrayList<ASTNode> children) {
		for (ASTNode child : children)
			if ((ChangeKind) child.getProperty(PROPERTY_STATUS) != ChangeKind.UNCHANGED)
				return false;
		return true;
	}

	public void printChanges() throws Exception {
		printChanges(astM);
		printChanges(astN);
	}

	private void printChanges(ASTNode node) throws Exception {
		ASTVisitor visitor = (new ASTVisitor() {
			private int indent = 0;

			private void printIndent() {
				for (int i = 0; i < indent; i++)
					System.out.print("\t");
			}

			@Override
			public boolean visitGeneral(ASTNode node) {
				printIndent();
				ChangeKind status = (ChangeKind) node.getProperty(PROPERTY_STATUS);
				System.out.print(TreedUtils.buildASTLabel(node) + ": " + status);
				ASTNode mn = (ASTNode) node.getProperty(PROPERTY_MAP);
				if (status != ChangeKind.UNCHANGED && mn != null)
					System.out.print(" " + TreedUtils.buildASTLabel(mn));
				System.out.println();
				indent++;
				return true;
			}

			@Override
			public void endvisitGeneral(ASTNode node) {
				indent--;
			}
		});
		node.traverse(visitor);
	}

	private void markChanges() {
		markAstM(astM);
		markAstN(astN);
	}

	private void markAstN(ASTNode node) {
		if (node.getProperty(PROPERTY_STATUS) == null) {
			node.setProperty(PROPERTY_STATUS, ChangeKind.ADDED);
			numOfChanges++;
			numOfUnmaps++;
			if (!(node instanceof Literal))
				numOfNonNameUnMaps++;
		}
		ArrayList<ASTNode> children = tree.get(node);
		for (ASTNode child : children)
			markAstN(child);
	}

	private void markAstM(ASTNode node) {
		HashMap<ASTNode, Double> maps = treeMap.get(node);
		if (maps.isEmpty()) {
			node.setProperty(PROPERTY_STATUS, ChangeKind.DELETED);
			numOfChanges++;
			numOfUnmaps++;
			if (!(node instanceof Literal) && !(node instanceof ReturnStatement) && !(node instanceof BreakStatement)
					&& !(node instanceof ContinueStatement))
				numOfNonNameUnMaps++;
		} else {
			ASTNode mappedNode = maps.keySet().iterator().next();
			node.setProperty(PROPERTY_MAP, mappedNode);
			mappedNode.setProperty(PROPERTY_MAP, node);
			if (node == astM) {
				astM.setProperty(PROPERTY_STATUS, ChangeKind.UNCHANGED);
				astN.setProperty(PROPERTY_STATUS, ChangeKind.UNCHANGED);
			} else {
				ASTNode p = node.getParent(), mp = mappedNode.getParent();
				if (!treeMap.get(p).containsKey(mp)) {
					node.setProperty(PROPERTY_STATUS, ChangeKind.MOVED);
					mappedNode.setProperty(PROPERTY_STATUS, ChangeKind.MOVED);
					numOfChanges += 2;
				} else {
					if (node.getProperty(PROPERTY_STATUS) == null) {
						node.setProperty(PROPERTY_STATUS, ChangeKind.MOVED);
						mappedNode.setProperty(PROPERTY_STATUS, ChangeKind.MOVED);
						numOfChanges += 2;
					}
				}
			}
			// mark moving for children
			ArrayList<ASTNode> children = tree.get(node), mappedChildren = tree.get(mappedNode);
			if (!children.isEmpty() && !mappedChildren.isEmpty()) {
				markChanges(children, mappedChildren);
			}
		}
		ArrayList<ASTNode> children = tree.get(node);
		for (ASTNode child : children)
			markAstM(child);
	}

	private void markChanges(ArrayList<ASTNode> nodes, ArrayList<ASTNode> mappedNodes) {
		int len = nodes.size(), lenN = mappedNodes.size();
		int[][] d = new int[2][lenN + 1];
		char[][] p = new char[len + 1][lenN + 1];
		d[1][0] = 0;
		for (int j = 1; j <= lenN; j++)
			d[1][j] = 0;
		for (int i = 1; i <= len; i++) {
			ASTNode node = nodes.get(i - 1);
			HashMap<ASTNode, Double> maps = treeMap.get(node);
			for (int j = 0; j <= lenN; j++)
				d[0][j] = d[1][j];
			for (int j = 1; j <= lenN; j++) {
				ASTNode nodeN = mappedNodes.get(j - 1);
				if (maps.containsKey(nodeN)) {
					d[1][j] = d[0][j - 1] + 1;
					p[i][j] = 'D';
				} else if (d[0][j] >= d[1][j - 1]) {
					d[1][j] = d[0][j];
					p[i][j] = 'U';
				} else {
					d[1][j] = d[1][j - 1];
					p[i][j] = 'L';
				}
			}
		}
		int i = len, j = lenN;
		while (i > 0 && j > 0) {
			if (p[i][j] == 'D') {
				ASTNode node = nodes.get(i - 1), node2 = mappedNodes.get(j - 1);
				if (TreedUtils.buildLabelForVector(node) == TreedUtils.buildLabelForVector(node2)) {
					node.setProperty(PROPERTY_STATUS, ChangeKind.UNCHANGED);
					node2.setProperty(PROPERTY_STATUS, ChangeKind.UNCHANGED);
				} else {
					node.setProperty(PROPERTY_STATUS, ChangeKind.RENAMED);
					node2.setProperty(PROPERTY_STATUS, ChangeKind.RENAMED);
					numOfChanges += 2;
				}
				i--;
				j--;
			} else if (p[i][j] == 'U') {
				i--;
			} else {
				j--;
			}
		}
	}

	private void mapPivots() {
		for (ASTNode node : tree.keySet())
			treeMap.put(node, new HashMap<ASTNode, Double>());
		setMap(astM, astN, 1.0);
		ArrayList<ASTNode> lM = getChildrenContainers(astM), lN = getChildrenContainers(astN);
		ArrayList<ASTNode> heightsM = new ArrayList<ASTNode>(lM), heightsN = new ArrayList<ASTNode>(lN);
		Collections.sort(heightsM, new Comparator<ASTNode>() {
			@Override
			public int compare(ASTNode node1, ASTNode node2) {
				return treeHeight.get(node2) - treeHeight.get(node1);
			}
		});
		Collections.sort(heightsN, new Comparator<ASTNode>() {
			@Override
			public int compare(ASTNode node1, ASTNode node2) {
				return treeHeight.get(node2) - treeHeight.get(node1);
			}
		});
		mapPivots(lM, lN, heightsM, heightsN);
	}

	private void mapPivots(ArrayList<ASTNode> lM, ArrayList<ASTNode> lN, ArrayList<ASTNode> heightsM,
			ArrayList<ASTNode> heightsN) {
		if (lM.size() * lN.size() > MAX_BIPARTITE_MATCH_SIZE) {
			lM.clear();
			lN.clear();
		}
		ArrayList<Integer> lcsM = new ArrayList<Integer>(), lcsN = new ArrayList<Integer>();
		lcs(lM, lN, lcsM, lcsN);
		for (int i = lcsM.size() - 1; i >= 0; i--) {
			int indexM = lcsM.get(i), indexN = lcsN.get(i);
			ASTNode nodeM = lM.get(indexM), nodeN = lN.get(indexN);
			setMap(nodeM, nodeN, 1.0);
			pivotsM.add(nodeM);
			pivotsN.add(nodeN);
			lM.remove(indexM);
			lN.remove(indexN);
			heightsM.remove(nodeM);
			heightsN.remove(nodeN);
		}
		while (!lM.isEmpty() && !lN.isEmpty()) {
			int hM = treeHeight.get(heightsM.get(0));
			int hN = treeHeight.get(heightsN.get(0));
			boolean expandedM = false, expandedN = false;
			if (hM >= hN)
				expandedM = expandForPivots(lM, heightsM, hM);
			if (hN >= hM)
				expandedN = expandForPivots(lN, heightsN, hN);
			if (expandedM || expandedN) {
				mapPivots(lM, lN, heightsM, heightsN);
				break;
			}
		}
	}

	private boolean expandForPivots(ArrayList<ASTNode> l, ArrayList<ASTNode> heights, int h) {
		HashSet<ASTNode> nodes = new HashSet<ASTNode>();
		for (ASTNode node : heights) {
			if (treeHeight.get(node) == h)
				nodes.add(node);
			else
				break;
		}
		boolean expanded = false;
		for (int i = l.size() - 1; i >= 0; i--) {
			ASTNode node = l.get(i);
			if (nodes.contains(node)) {
				l.remove(i);
				heights.remove(0);
				ArrayList<ASTNode> children = getChildrenContainers(node);
				if (!children.isEmpty() && children.size() <= MAX_EXPENSION_SIZE) {
					expanded = true;
					for (int j = 0; j < children.size(); j++) {
						ASTNode child = children.get(j);
						l.add(i + j, child);
						int index = Collections.binarySearch(heights, child, new Comparator<ASTNode>() {
							@Override
							public int compare(ASTNode node1, ASTNode node2) {
								return treeHeight.get(node2) - treeHeight.get(node1);
							}
						});
						if (index < 0)
							index = -(index + 1);
						heights.add(index, child);
					}
				}
			}
		}
		return expanded;
	}

	private boolean expandForMoving(ArrayList<ASTNode> l, ArrayList<ASTNode> heights, int h) {
		HashSet<ASTNode> nodes = new HashSet<ASTNode>();
		for (ASTNode node : heights) {
			if (treeHeight.get(node) == h)
				nodes.add(node);
			else
				break;
		}
		boolean expanded = false;
		for (int i = l.size() - 1; i >= 0; i--) {
			ASTNode node = l.get(i);
			if (nodes.contains(node)) {
				l.remove(i);
				heights.remove(0);
				ArrayList<ASTNode> children = getNotYetMappedDescendantContainers(node);
				if (!children.isEmpty()) {
					expanded = true;
					for (int j = 0; j < children.size(); j++) {
						ASTNode child = children.get(j);
						l.add(i + j, child);
						int index = Collections.binarySearch(heights, child, new Comparator<ASTNode>() {
							@Override
							public int compare(ASTNode node1, ASTNode node2) {
								return treeHeight.get(node2) - treeHeight.get(node1);
							}
						});
						if (index < 0)
							index = -(index + 1);
						heights.add(index, child);
					}
				}
			}
		}
		return expanded;
	}

	private void lcs(ArrayList<ASTNode> lM, ArrayList<ASTNode> lN, ArrayList<Integer> lcsM, ArrayList<Integer> lcsN) {
		int lenM = lM.size(), lenN = lN.size();
		int[][] d = new int[2][lenN + 1];
		char[][] p = new char[lenM + 1][lenN + 1];
		for (int j = 0; j <= lenN; j++)
			d[1][j] = 0;
		for (int i = lenM - 1; i >= 0; i--) {
			ASTNode nodeM = lM.get(i);
			int hM = treeHeight.get(nodeM);
			HashMap<String, Integer> vM = treeVector.get(nodeM);
			for (int j = 0; j <= lenN; j++)
				d[0][j] = d[1][j];
			for (int j = lenN - 1; j >= 0; j--) {
				ASTNode nodeN = lN.get(j);
				int hN = treeHeight.get(nodeN);
				HashMap<String, Integer> vN = treeVector.get(nodeN);
				if (hM == hN && nodeM.getNodeType() == nodeN.getNodeType() && vM.equals(vN)
						&& subtreeMatch(nodeM, nodeN)) {
					d[1][j] = d[0][j + 1] + 1;
					p[i][j] = 'D';
				} else if (d[0][j] >= d[1][j + 1]) {
					d[1][j] = d[0][j];
					p[i][j] = 'U';
				} else {
					d[1][j] = d[1][j + 1];
					p[i][j] = 'R';
				}
			}
		}
		int i = 0, j = 0;
		while (i < lenM && j < lenN) {
			if (p[i][j] == 'D') {
				lcsM.add(i);
				lcsN.add(j);
				i++;
				j++;
			} else if (p[i][j] == 'U')
				i++;
			else
				j++;
		}
	}

	private boolean subtreeMatch(ASTNode nodeM, ASTNode nodeN) {
		if (!labelMatch(nodeM, nodeN))
			return false;
		ArrayList<ASTNode> childrenM = tree.get(nodeM), childrenN = tree.get(nodeN);
		if (childrenM.size() != childrenN.size())
			return false;
		if (childrenM.size() == 0)
			return nodeM.toString().equals(nodeN.toString());
		for (int i = 0; i < childrenM.size(); i++) {
			if (!subtreeMatch(childrenM.get(i), childrenN.get(i)))
				return false;
		}
		return true;
	}

	private boolean labelMatch(ASTNode nodeM, ASTNode nodeN) {
		if (nodeM.getNodeType() != nodeN.getNodeType())
			return false;

		if (nodeM instanceof Expression)
			return labelMatch((Expression) nodeM, (Expression) nodeN);

		return true;
	}

	private boolean labelMatch(Expression nodeM, Expression nodeN) {
		if (!nodeM.getClass().getSimpleName().equals(nodeN.getClass().getSimpleName()))
			return false;
		return nodeM.getOperator().equals(nodeN.getOperator());
	}

	@SuppressWarnings("unused")
	private void lss(ArrayList<ASTNode> lM, ArrayList<ASTNode> lN, ArrayList<Integer> lcsM, ArrayList<Integer> lcsN,
			double threshold) {
		int lenM = lM.size(), lenN = lN.size();
		double[][] d = new double[2][lenN + 1];
		char[][] p = new char[lenM + 1][lenN + 1];
		for (int j = 0; j <= lenN; j++)
			d[1][j] = 0;
		for (int i = lenM - 1; i >= 0; i--) {
			ASTNode nodeM = lM.get(i);
			for (int j = 0; j <= lenN; j++)
				d[0][j] = d[1][j];
			for (int j = lenN - 1; j >= 0; j--) {
				ASTNode nodeN = lN.get(j);
				double sim = computeSimilarity(nodeM, nodeN, threshold);
				if (nodeM.getNodeType() == nodeN.getNodeType() && sim >= threshold) {
					d[1][j] = d[0][j + 1] + sim;
					p[i][j] = 'D';
				} else if (d[0][j] >= d[1][j + 1]) {
					d[1][j] = d[0][j];
					p[i][j] = 'U';
				} else {
					d[1][j] = d[1][j + 1];
					p[i][j] = 'R';
				}
			}
		}
		int i = 0, j = 0;
		while (i < lenM && j < lenN) {
			if (p[i][j] == 'D') {
				lcsM.add(i);
				lcsN.add(j);
				i++;
				j++;
			} else if (p[i][j] == 'U')
				i++;
			else
				j++;
		}
	}

	private ArrayList<ASTNode> getChildrenContainers(ASTNode node) {
		ArrayList<ASTNode> children = new ArrayList<ASTNode>();
		for (ASTNode child : tree.get(node)) {
			if (treeHeight.get(child) >= MIN_HEIGHT)
				children.add(child);
		}
		return children;
	}

	private void mapBottomUp() {
		ArrayList<ASTNode> heightsM = new ArrayList<ASTNode>(pivotsM);
		Collections.sort(heightsM, new Comparator<ASTNode>() {
			@Override
			public int compare(ASTNode node1, ASTNode node2) {
				int d = treeHeight.get(node2) - treeHeight.get(node1);
				if (d != 0)
					return d;
				d = treeDepth.get(node1) - treeDepth.get(node2);
				if (d != 0)
					return d;
				return node1.start() - node2.start();
			}
		});
		Set<ASTNode> visitedAncestorsM = new HashSet<ASTNode>(), visitedAncestorsN = new HashSet<ASTNode>();
		for (ASTNode nodeM : heightsM) {
			ASTNode nodeN = treeMap.get(nodeM).keySet().iterator().next();
			ArrayList<ASTNode> ancestorsM = new ArrayList<ASTNode>(), ancestorsN = new ArrayList<ASTNode>();
			ancestorsM.removeAll(visitedAncestorsM);
			ancestorsN.removeAll(visitedAncestorsN);
			getNotYetMappedAncestors(nodeM, ancestorsM);
			getNotYetMappedAncestors(nodeN, ancestorsN);
			map(ancestorsM, ancestorsN, MIN_SIM);
			visitedAncestorsM.addAll(ancestorsM);
			visitedAncestorsN.addAll(ancestorsN);
		}
	}

	private ArrayList<ASTNode> map(ArrayList<ASTNode> nodesM, ArrayList<ASTNode> nodesN, double threshold) {
		HashMap<ASTNode, HashSet<Pair>> pairsOfAncestor = new HashMap<ASTNode, HashSet<Pair>>();
		ArrayList<Pair> pairs = new ArrayList<Pair>();
		PairDescendingOrder comparator = new PairDescendingOrder();
		for (ASTNode nodeM : nodesM) {
			HashSet<Pair> pairs1 = new HashSet<Pair>();
			for (ASTNode nodeN : nodesN) {
				double sim = computeSimilarity(nodeM, nodeN, threshold);
				if (sim >= threshold) {
					Pair pair = new Pair(nodeM, nodeN, sim, -Math.abs(
							(nodeM.getParent().start() - nodeM.start()) - (nodeN.getParent().start() - nodeN.start())));
					pairs1.add(pair);
					HashSet<Pair> pairs2 = pairsOfAncestor.get(nodeN);
					if (pairs2 == null)
						pairs2 = new HashSet<Pair>();
					pairs2.add(pair);
					pairsOfAncestor.put(nodeN, pairs2);
					int index = Collections.binarySearch(pairs, pair, comparator);
					if (index < 0)
						pairs.add(-1 - index, pair);
					else
						pairs.add(index, pair);
				}
			}
			pairsOfAncestor.put(nodeM, pairs1);
		}
		ArrayList<ASTNode> nodes = new ArrayList<ASTNode>();
		Set<ASTNode> matches = new HashSet<ASTNode>();
		for (int i = 0; i < pairs.size(); i++) {
			Pair pair = pairs.get(i);
			ASTNode nodeM = (ASTNode) pair.getObj1(), nodeN = (ASTNode) pair.getObj2();
			if (matches.contains(nodeM) || matches.contains(nodeN))
				continue;
			setMap(nodeM, nodeN, pair.getWeight());
			nodes.add(nodeM);
			nodes.add(nodeN);
			matches.add(nodeM);
			matches.add(nodeN);
		}
		/*
		 * while (!pairs.isEmpty()) { Pair pair = pairs.get(0); ASTNode nodeM =
		 * (ASTNode) pair.getObj1(), nodeN = (ASTNode) pair.getObj2(); setMap(nodeM,
		 * nodeN, pair.getWeight()); nodes.add(nodeM); nodes.add(nodeN); for (Pair p :
		 * pairsOfAncestor.get(nodeM)) pairs.remove(p); for (Pair p :
		 * pairsOfAncestor.get(nodeN)) pairs.remove(p); }
		 */
		return nodes;
	}

	private void setMap(ASTNode nodeM, ASTNode nodeN, double w) {
		// map node of tree 1 to corresponding node of tree 2
		treeMap.get(nodeM).put(nodeN, w);
		treeMap.get(nodeN).put(nodeM, w);
	}

	private double computeSimilarity(ASTNode nodeM, ASTNode nodeN, double threshold) {
		if (nodeM.getNodeType() != nodeN.getNodeType())
			return 0;
		ArrayList<ASTNode> childrenM = tree.get(nodeM), childrenN = tree.get(nodeN);
		if (childrenM.isEmpty() && childrenN.isEmpty()) {

			int type = nodeM.getNodeType();
			double sim = 0;
//			if (type == ASTNode.ARRAY_CREATION 
//					|| type == ASTNode.ARRAY_INITIALIZER 
//					|| type == ASTNode.BLOCK 
//					|| type == ASTNode.INFIX_EXPRESSION 
//					|| type == ASTNode.METHOD_INVOCATION
//					|| type == ASTNode.SWITCH_STATEMENT
//					)
//				sim = MIN_SIM_MOVE;
//			else 
			{
				String sM = nodeM.toString(), sN = nodeN.toString();
				int lM = sM.length(), lN = sN.length();
				if (lM > 1000 || lN > 1000) {
					if (lM == 0 && lN == 0)
						sim = 1;
					else if (lM == 0 || lN == 0)
						sim = 0;
					else
						sim = lM > lN ? lN * 1.0 / lM : lM * 1.0 / lN;
				} else
					sim = StringProcessor.computeCharLCS(StringProcessor.serializeToChars(sM),
							StringProcessor.serializeToChars(sN));
			}
			sim = threshold + sim * (1 - threshold);
			return sim;
		}
		if (!childrenM.isEmpty() && !childrenN.isEmpty()) {
			HashMap<String, Integer> vM = treeVector.get(nodeM), vN = treeVector.get(nodeN);
			double sim = computeSimilarity(vM, vN);
			/*
			 * double[] sims = computeSimilarity(childrenM, childrenN); for (double s :
			 * sims) sim += s; return sim / (sims.length + 1);
			 */
			return sim;
		}
		return 0;
	}

	@SuppressWarnings("unused")
	private double[] computeSimilarity(ArrayList<ASTNode> l1, ArrayList<ASTNode> l2) {
		double[] sims = new double[Math.max(l1.size(), l2.size())];
		Arrays.fill(sims, 0.0);
		HashMap<ASTNode, HashSet<Pair>> pairsOfNode = new HashMap<ASTNode, HashSet<Pair>>();
		ArrayList<Pair> pairs = new ArrayList<Pair>();
		PairDescendingOrder comparator = new PairDescendingOrder();
		for (ASTNode node1 : l1) {
			HashSet<Pair> pairs1 = new HashSet<Pair>();
			for (ASTNode node2 : l2) {
				double sim = computeSimilarity(treeVector.get(node1), treeVector.get(node2));
				if (sim > 0) {
					Pair pair = new Pair(node1, node2, sim);
					pairs1.add(pair);
					HashSet<Pair> pairs2 = pairsOfNode.get(node2);
					if (pairs2 == null)
						pairs2 = new HashSet<Pair>();
					pairs2.add(pair);
					pairsOfNode.put(node2, pairs2);
					int index = Collections.binarySearch(pairs, pair, comparator);
					if (index < 0)
						pairs.add(-1 - index, pair);
					else
						pairs.add(index, pair);
				}
			}
			pairsOfNode.put(node1, pairs1);
		}
		int i = 0;
		while (!pairs.isEmpty()) {
			Pair pair = pairs.get(0);
			sims[i++] = pair.getWeight();
			for (Pair p : pairsOfNode.get(pair.getObj1()))
				pairs.remove(p);
			for (Pair p : pairsOfNode.get(pair.getObj2()))
				pairs.remove(p);
		}

		return sims;
	}

	private double computeSimilarity(HashMap<String, Integer> vM, HashMap<String, Integer> vN) {
		double sim = 0.0;
		HashSet<String> keys = new HashSet<String>(vM.keySet());
		keys.retainAll(vN.keySet());
		for (String key : keys)
			sim += Math.min(vM.get(key), vN.get(key));
		sim = 2 * (sim + SIM_SMOOTH) / (length(vM) + length(vN) + 2 * SIM_SMOOTH);
		return sim;
	}

	private <E> int length(HashMap<E, Integer> vector) {
		int len = 0;
		for (int val : vector.values())
			len += val;
		return len;
	}

	private void getNotYetMappedAncestors(ASTNode node, ArrayList<ASTNode> ancestors) {
		ASTNode p = node.getParent();
		if (treeMap.get(p).isEmpty()) {
			ancestors.add(p);
			getNotYetMappedAncestors(p, ancestors);
		}
	}

	private void mapTopDown() {
		mapTopDown(astM);
	}

	@SuppressWarnings("deprecation")
	private void mapTopDown(ASTNode nodeM) {
		ArrayList<ASTNode> childrenM = tree.get(nodeM);
		HashMap<ASTNode, Double> maps = treeMap.get(nodeM);
		if (!maps.isEmpty()) {
			ASTNode nodeN = maps.keySet().iterator().next();
			ArrayList<ASTNode> childrenN = tree.get(nodeN);
			if (pivotsM.contains(nodeM)) {
				mapUnchangedNodes(nodeM, nodeN);
				return;
			} else {
				ArrayList<ASTNode> nodesM = getNotYetMatchedNodes(childrenM), nodesN = getNotYetMatchedNodes(childrenN);
				ArrayList<ASTNode> mappedChildrenM = new ArrayList<ASTNode>(),
						mappedChildrenN = new ArrayList<ASTNode>();

				if (nodeM instanceof PythonWhileStatement) {
					mappedChildrenM.add(((PythonWhileStatement) nodeM).getCondition());
					mappedChildrenN.add(((PythonWhileStatement) nodeN).getCondition());
					mappedChildrenM.add(((PythonWhileStatement) nodeM).getAction());
					mappedChildrenN.add(((PythonWhileStatement) nodeN).getAction());
					mappedChildrenM.add(((PythonWhileStatement) nodeM).getElseStatement());
					mappedChildrenN.add(((PythonWhileStatement) nodeN).getElseStatement());
				} else if (nodeM instanceof PythonForStatement) {
					mappedChildrenM.add(((PythonForStatement) nodeM).getfMainArguments());
					mappedChildrenN.add(((PythonForStatement) nodeN).getfMainArguments());
					mappedChildrenM.add(((PythonForStatement) nodeM).getCondition());
					mappedChildrenN.add(((PythonForStatement) nodeN).getCondition());
					mappedChildrenM.add(((PythonForStatement) nodeM).getAction());
					mappedChildrenN.add(((PythonForStatement) nodeN).getAction());
					mappedChildrenM.add(((PythonForStatement) nodeM).getfElseStatement());
					mappedChildrenN.add(((PythonForStatement) nodeN).getfElseStatement());
				} else if (nodeM instanceof PythonWithStatement) {
					mappedChildrenM.add(((PythonWithStatement) nodeM).getWhat());
					mappedChildrenN.add(((PythonWithStatement) nodeN).getWhat());
					mappedChildrenM.add(((PythonWithStatement) nodeM).getAs());
					mappedChildrenN.add(((PythonWithStatement) nodeN).getAs());
					mappedChildrenM.add(((PythonWithStatement) nodeM).getBlock());
					mappedChildrenN.add(((PythonWithStatement) nodeN).getBlock());
				} else if (nodeM instanceof PythonYieldStatement) {
					mappedChildrenM.add(((PythonYieldStatement) nodeM).getExpression());
					mappedChildrenN.add(((PythonYieldStatement) nodeN).getExpression());
				} else if (nodeM instanceof PythonDelStatement) {
					mappedChildrenM.add(((PythonDelStatement) nodeM).getExpression());
					mappedChildrenN.add(((PythonDelStatement) nodeN).getExpression());
				} else if (nodeM instanceof ContinueStatement) {
					mappedChildrenM.add(((ContinueStatement) nodeM).getExpression());
					mappedChildrenN.add(((ContinueStatement) nodeN).getExpression());
				} else if (nodeM instanceof BreakStatement) {
					mappedChildrenM.add(((BreakStatement) nodeM).getExpression());
					mappedChildrenN.add(((BreakStatement) nodeN).getExpression());
				} else if (nodeM instanceof ExecStatement) {
					mappedChildrenM.add(((ExecStatement) nodeM).getExpression());
					mappedChildrenN.add(((ExecStatement) nodeN).getExpression());
				} else if (nodeM instanceof GlobalStatement) {
					mappedChildrenM.add(((GlobalStatement) nodeM).getExpression());
					mappedChildrenN.add(((GlobalStatement) nodeN).getExpression());
				} else if (nodeM instanceof IfStatement) {
					mappedChildrenM.add(((IfStatement) nodeM).getCondition());
					mappedChildrenN.add(((IfStatement) nodeN).getCondition());
					mappedChildrenM.add(((IfStatement) nodeM).getThen());
					mappedChildrenN.add(((IfStatement) nodeN).getThen());
					mappedChildrenM.add(((IfStatement) nodeM).getElse());
					mappedChildrenN.add(((IfStatement) nodeN).getElse());
				} else if (nodeM instanceof PythonExceptStatement) {
					mappedChildrenM.add(((PythonExceptStatement) nodeM).getMessage());
					mappedChildrenN.add(((PythonExceptStatement) nodeN).getMessage());
					mappedChildrenM.add(((PythonExceptStatement) nodeM).getBody());
					mappedChildrenN.add(((PythonExceptStatement) nodeN).getBody());
				} else if (nodeM instanceof TryFinallyStatement) {
					mappedChildrenM.add(((TryFinallyStatement) nodeM).getfBody());
					mappedChildrenN.add(((TryFinallyStatement) nodeN).getfBody());
				} else if (nodeM instanceof PythonTryStatement) {
					mappedChildrenM.add(((PythonTryStatement) nodeM).getBody());
					mappedChildrenN.add(((PythonTryStatement) nodeN).getBody());
					mappedChildrenM.add(((PythonTryStatement) nodeM).getfElseStatement());
					mappedChildrenN.add(((PythonTryStatement) nodeN).getfElseStatement());
				} else if (nodeM instanceof ReturnStatement) {
					mappedChildrenM.add(((ReturnStatement) nodeM).getExpression());
					mappedChildrenN.add(((ReturnStatement) nodeN).getExpression());
				} else if (nodeM instanceof PythonImportStatement) {
					mappedChildrenM.add(((PythonImportStatement) nodeM).getExpression());
					mappedChildrenN.add(((PythonImportStatement) nodeN).getExpression());
				} else if (nodeM instanceof PythonImportFromStatement) {
					mappedChildrenM.add(((PythonImportFromStatement) nodeM).getfImportExpressions());
					mappedChildrenN.add(((PythonImportFromStatement) nodeN).getfImportExpressions());
					mappedChildrenM.add(((PythonImportFromStatement) nodeM).getfModuleExpression());
					mappedChildrenN.add(((PythonImportFromStatement) nodeN).getfModuleExpression());
				}

				else if (nodeM instanceof MethodDeclaration) {
					mappedChildrenM.add(((MethodDeclaration) nodeM).getBody());
					mappedChildrenN.add(((MethodDeclaration) nodeN).getBody());
				} else if (nodeM instanceof TypeDeclaration) {
					mappedChildrenM.add(((TypeDeclaration) nodeM).getBody());
					mappedChildrenN.add(((TypeDeclaration) nodeN).getBody());
				}

				else if (nodeM instanceof BinaryExpression) {
					mappedChildrenM.add(((BinaryExpression) nodeM).getLeft());
					mappedChildrenN.add(((BinaryExpression) nodeN).getRight());
				} else if (nodeM instanceof UnaryExpression) {
					mappedChildrenM.add(((UnaryExpression) nodeM).getExpression());
					mappedChildrenN.add(((UnaryExpression) nodeN).getExpression());
				} else if (nodeM instanceof PythonLambdaExpression) {
					mappedChildrenM.add(((PythonLambdaExpression) nodeM).getBodyExpression());
					mappedChildrenN.add(((PythonLambdaExpression) nodeN).getBodyExpression());
				} else if (nodeM instanceof CallHolder) {
					mappedChildrenM.add(((CallHolder) nodeM).getArguments());
					mappedChildrenN.add(((CallHolder) nodeN).getArguments());
				} else if (nodeM instanceof IndexHolder) {
					mappedChildrenM.add(((IndexHolder) nodeM).getIndex());
					mappedChildrenN.add(((IndexHolder) nodeN).getIndex());
				} else if (nodeM instanceof PythonForListExpression) {
					mappedChildrenM.add(((PythonForListExpression) nodeM).getFrom());
					mappedChildrenN.add(((PythonForListExpression) nodeN).getFrom());
					mappedChildrenM.add(((PythonForListExpression) nodeM).getIfList());
					mappedChildrenN.add(((PythonForListExpression) nodeN).getIfList());
					mappedChildrenM.add(((PythonForListExpression) nodeM).getVars());
					mappedChildrenN.add(((PythonForListExpression) nodeN).getVars());
				} else if (nodeM instanceof PythonListForExpression) {
					mappedChildrenM.add(((PythonListForExpression) nodeM).getMaker());
					mappedChildrenN.add(((PythonListForExpression) nodeN).getMaker());
				} else if (nodeM instanceof PythonSubscriptExpression) {
					mappedChildrenM.add(((PythonSubscriptExpression) nodeM).getCondition());
					mappedChildrenN.add(((PythonSubscriptExpression) nodeN).getCondition());
					mappedChildrenM.add(((PythonSubscriptExpression) nodeM).getSlice());
					mappedChildrenN.add(((PythonSubscriptExpression) nodeN).getSlice());
					mappedChildrenM.add(((PythonSubscriptExpression) nodeM).getTest());
					mappedChildrenN.add(((PythonSubscriptExpression) nodeN).getTest());
				} else if (nodeM instanceof ShortHandIfExpression) {
					mappedChildrenM.add(((ShortHandIfExpression) nodeM).getCondition());
					mappedChildrenN.add(((ShortHandIfExpression) nodeN).getCondition());
					mappedChildrenM.add(((ShortHandIfExpression) nodeM).getThen());
					mappedChildrenN.add(((ShortHandIfExpression) nodeN).getThen());
					mappedChildrenM.add(((ShortHandIfExpression) nodeM).getElse());
					mappedChildrenN.add(((ShortHandIfExpression) nodeN).getElse());
				} else if (nodeM instanceof PrintExpression) {
					mappedChildrenM.add(((PrintExpression) nodeM).getExpression());
					mappedChildrenN.add(((PrintExpression) nodeN).getExpression());
				} else if (nodeM instanceof PythonArgument) {
					mappedChildrenM.add(((PythonArgument) nodeM).getInitialization());
					mappedChildrenN.add(((PythonArgument) nodeN).getInitialization());
				}
				else if (nodeM instanceof SimpleStatement) {
					mappedChildrenM.add(((SimpleStatement) nodeM).getExpression());
					mappedChildrenN.add(((SimpleStatement) nodeN).getExpression());
				}

				if (!mappedChildrenM.isEmpty() && !mappedChildrenN.isEmpty()) {
					for (int i = 0; i < mappedChildrenM.size(); i++) {
						ASTNode childM = mappedChildrenM.get(i), childN = mappedChildrenN.get(i);
						if (childM != null && childN != null) {
							if (treeMap.get(childM).isEmpty() && treeMap.get(childN).isEmpty()) {
								double sim = 0;
								if (childM.getNodeType() == childN.getNodeType()) {
									int type = childM.getNodeType();
									if (childM instanceof Block
											|| (treeMap.get(childM).isEmpty() && treeMap.get(childN).isEmpty()))
										sim = 1.0;
									else
										sim = computeSimilarity(childM, childN, MIN_SIM);
									if (sim >= MIN_SIM) {
										setMap(childM, childN, MIN_SIM);
										if (TreedUtils.buildASTLabel(childM).equals(TreedUtils.buildASTLabel(childN))) {
											childM.setProperty(PROPERTY_MAP, ChangeKind.UNCHANGED);
											childN.setProperty(PROPERTY_MAP, ChangeKind.UNCHANGED);
										} else {
											childM.setProperty(PROPERTY_MAP, ChangeKind.RENAMED);
											childN.setProperty(PROPERTY_MAP, ChangeKind.RENAMED);
										}
									}
								}
								if (sim < MIN_SIM) {
									ArrayList<ASTNode> tempM = new ArrayList<ASTNode>(),
											tempN = new ArrayList<ASTNode>();
									tempM.add(childM);
									tempN.add(childN);
									int hM = treeHeight.get(childM), hN = treeHeight.get(childN);
									if (hM >= hN) {
										tempM.remove(childM);
										tempM.addAll(getNotYetMatchedNodes(tree.get(childM)));
									}
									if (hN >= hM) {
										tempN.remove(childN);
										tempN.addAll(getNotYetMatchedNodes(tree.get(childN)));
									}
									ArrayList<ASTNode> mappedNodes = map(tempM, tempN, MIN_SIM_MOVE);
									for (int j = 0; j < mappedNodes.size(); j += 2) {
										ASTNode mappedNodeM = mappedNodes.get(j), mappedNodeN = mappedNodes.get(j + 1);
										tempM.remove(mappedNodeM);
										tempN.remove(mappedNodeN);
									}
								}
							}
						}
						nodesM.remove(childM);
						nodesN.remove(childN);
					}
				}
				ArrayList<Integer> lcsM = new ArrayList<Integer>(), lcsN = new ArrayList<Integer>();
				lcs(nodesM, nodesN, lcsM, lcsN);
				for (int i = lcsM.size() - 1; i >= 0; i--) {
					int iM = lcsM.get(i), iN = lcsN.get(i);
					ASTNode nM = nodesM.get(iM), nN = nodesN.get(iN);
					setMap(nM, nN, 1.0);
					nodesM.remove(iM);
					nodesN.remove(iN);
				}
				lcsM.clear();
				lcsN.clear();
				ArrayList<ASTNode> mappedNodes = map(nodesM, nodesN, MIN_SIM);
				for (int i = 0; i < mappedNodes.size(); i += 2) {
					ASTNode mappedNodeM = mappedNodes.get(i), mappedNodeN = mappedNodes.get(i + 1);
					nodesM.remove(mappedNodeM);
					nodesN.remove(mappedNodeN);
				}
				/*
				 * ArrayList<ASTNode> maxsM = new ArrayList<ASTNode>(), maxsN = new
				 * ArrayList<ASTNode>(); int maxhM = maxHeight(nodesM, maxsM), maxhN =
				 * maxHeight(nodesN, maxsN); if (maxhM >= maxhN) { for (ASTNode node : maxsM) {
				 * nodesM.remove(node); nodesM.addAll(getNotYetMatchedNodes(tree.get(node))); }
				 * } if (maxhN >= maxhM) { for (ASTNode node : maxsN) { nodesN.remove(node);
				 * nodesN.addAll(getNotYetMatchedNodes(tree.get(node))); } } mappedNodes =
				 * map(nodesM, nodesN, MIN_SIM_MOVE); for (int i = 0; i < mappedNodes.size(); i
				 * += 2) { ASTNode mappedNodeM = mappedNodes.get(i), mappedNodeN =
				 * mappedNodes.get(i+1); nodesM.remove(mappedNodeM); nodesN.remove(mappedNodeN);
				 * }
				 */
			}
		}
		for (ASTNode child : childrenM)
			mapTopDown(child);
	}

	@SuppressWarnings("unused")
	private int maxHeight(ArrayList<ASTNode> nodes, ArrayList<ASTNode> maxs) {
		int max = 0;
		for (ASTNode node : nodes) {
			int h = treeHeight.get(node);
			if (h >= max) {
				if (h > max) {
					max = h;
					maxs.clear();
				}
				maxs.add(node);
			}
		}
		return max;
	}

	private void mapUnchangedNodes(ASTNode nodeM, ASTNode nodeN) {
		setMap(nodeM, nodeN, 1.0);
		ArrayList<ASTNode> childrenM = tree.get(nodeM), childrenN = tree.get(nodeN);
		for (int i = 0; i < childrenM.size(); i++)
			mapUnchangedNodes(childrenM.get(i), childrenN.get(i));
	}

	private ArrayList<ASTNode> getNotYetMatchedNodes(ArrayList<ASTNode> l) {
		ArrayList<ASTNode> nodes = new ArrayList<ASTNode>();
		for (ASTNode node : l)
			if (treeMap.get(node).isEmpty())
				nodes.add(node);
		return nodes;
	}

	private void mapMoving() throws Exception {
		astM.traverse(new ASTVisitor() {

			@Override
			public boolean visitGeneral(ASTNode node) {
				HashMap<ASTNode, Double> maps = treeMap.get(node);
				if (!maps.isEmpty()) {
					ASTNode mapped = maps.keySet().iterator().next();
					mapMoving(node, mapped);
					return false;
				}
				return true;
			}

			@Override
			public boolean visit(MethodDeclaration node) {
				HashMap<ASTNode, Double> maps = treeMap.get(node);
				if (!maps.isEmpty()) {
					ASTNode mapped = maps.keySet().iterator().next();
					mapMoving(node, mapped);
					return false;
				}
				return true;
			}
		});
//		mapMoving(astM, astN);
	}

	private void mapMoving(ASTNode astM, ASTNode astN) {
		ArrayList<ASTNode> lM = getNotYetMappedDescendantContainers(astM),
				lN = getNotYetMappedDescendantContainers(astN);
		ArrayList<ASTNode> heightsM = new ArrayList<ASTNode>(lM), heightsN = new ArrayList<ASTNode>(lN);
		Collections.sort(heightsM, new Comparator<ASTNode>() {
			@Override
			public int compare(ASTNode node1, ASTNode node2) {
				return treeHeight.get(node2) - treeHeight.get(node1);
			}
		});
		Collections.sort(heightsN, new Comparator<ASTNode>() {
			@Override
			public int compare(ASTNode node1, ASTNode node2) {
				return treeHeight.get(node2) - treeHeight.get(node1);
			}
		});
		mapMoving(lM, lN, heightsM, heightsN);
	}

	private void mapMoving(ArrayList<ASTNode> lM, ArrayList<ASTNode> lN, ArrayList<ASTNode> heightsM,
			ArrayList<ASTNode> heightsN) {
		ArrayList<ASTNode> mappedNodes = map(lM, lN, MIN_SIM_MOVE);
		for (int i = 0; i < mappedNodes.size(); i += 2) {
			ASTNode nodeM = mappedNodes.get(i), nodeN = mappedNodes.get(i + 1);
			lM.remove(nodeM);
			lN.remove(nodeN);
			heightsM.remove(nodeM);
			heightsN.remove(nodeN);
		}
		while (!lM.isEmpty() && !lN.isEmpty()) {
			int hM = treeHeight.get(heightsM.get(0));
			int hN = treeHeight.get(heightsN.get(0));
			boolean expandedM = false, expandedN = false;
			if (hM >= hN)
				expandedM = expandForMoving(lM, heightsM, hM);
			if (hN >= hM)
				expandedN = expandForMoving(lN, heightsN, hN);
			if (expandedM || expandedN) {
				mapMoving(lM, lN, heightsM, heightsN);
				break;
			}
		}
	}

	private ArrayList<ASTNode> getNotYetMappedDescendantContainers(ASTNode node) {
		ArrayList<ASTNode> children = new ArrayList<ASTNode>();
		for (ASTNode child : tree.get(node)) {
			if (!pivotsM.contains(child) && !pivotsN.contains(child) && treeHeight.get(child) >= MIN_HEIGHT) {
				if (treeMap.get(child).isEmpty())
					children.add(child);
				else
					children.addAll(getNotYetMappedDescendantContainers(child));
			}
		}
		return children;
	}

	private void buildTrees() throws Exception {
		buildTree(astM);
		buildTree(astN);
	}

	private void buildTree(final ASTNode root) throws Exception {
		TreedBuilder visitor = new TreedBuilder(root, type, nodeTypes);
		root.traverse(visitor);
		this.type = visitor.type;
		this.nodeTypes = visitor.nodeTypes;
		tree.putAll(visitor.tree);
		treeHeight.putAll(visitor.treeHeight);
		treeDepth.putAll(visitor.treeDepth);
		treeVector.putAll(visitor.treeVector);
		
		visitor.clear();
		visitor=null;
	}

}
