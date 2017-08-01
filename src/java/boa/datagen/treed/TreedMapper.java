package boa.datagen.treed;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;
import boa.datagen.util.JavaASTUtil;
import boa.types.Shared.ChangeKind;

public class TreedMapper implements TreedConstants {
	private ASTNode astM, astN;
	private HashMap<ASTNode, ArrayList<ASTNode>> tree = new HashMap<ASTNode, ArrayList<ASTNode>>();
	private HashMap<ASTNode, Integer> treeHeight = new HashMap<ASTNode, Integer>(), treeDepth = new HashMap<ASTNode, Integer>();
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
	
	public void map() {
		buildTrees();
		mapPivots();
		mapBottomUp();
		mapMoving();
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

	public void printChanges() {
		printChanges(astM);
		printChanges(astN);
	}

	private void printChanges(ASTNode node) {
		node.accept(new ASTVisitor() {
			private int indent = 0;
			
			private void printIndent() {
				for (int i = 0; i < indent; i++)
					System.out.print("\t");
			}
			
			@Override
			public void preVisit(ASTNode node) {
				printIndent();
				ChangeKind status = (ChangeKind) node.getProperty(PROPERTY_STATUS);
				System.out.print(TreedUtils.buildASTLabel(node) + ": " + status);
				ASTNode mn = (ASTNode) node.getProperty(PROPERTY_MAP);
				if (status != ChangeKind.UNCHANGED && mn != null)
					System.out.print(" " + TreedUtils.buildASTLabel(mn));
				System.out.println();
				indent++;
			}
			
			@Override
			public void postVisit(ASTNode node) {
				indent--;
			}
		});
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
			if (!(node instanceof SimpleName))
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
			if (!(node instanceof SimpleName) && !(node instanceof ReturnStatement) && !(node instanceof BreakStatement) && !(node instanceof ContinueStatement))
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
				ASTNode node = nodes.get(i-1), node2 = mappedNodes.get(j-1);
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
			}
			else {
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

	private void mapPivots(ArrayList<ASTNode> lM, ArrayList<ASTNode> lN, ArrayList<ASTNode> heightsM, ArrayList<ASTNode> heightsN) {
		ArrayList<Integer> lcsM = new ArrayList<Integer>(), lcsN = new ArrayList<Integer>();
		lcs(lM, lN, lcsM, lcsN);
		for (int i = lcsM.size()-1; i >= 0; i--) {
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
		for (int i = l.size()-1; i >= 0; i--) {
			ASTNode node = l.get(i);
			if (nodes.contains(node)) {
				l.remove(i);
				heights.remove(0);
				ArrayList<ASTNode> children = getChildrenContainers(node);
				if (!children.isEmpty()) {
					expanded = true;
					for (int j = 0; j < children.size(); j++) {
						ASTNode child = children.get(j);
						l.add(i+j, child);
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
		for (int i = l.size()-1; i >= 0; i--) {
			ASTNode node = l.get(i);
			if (nodes.contains(node)) {
				l.remove(i);
				heights.remove(0);
				ArrayList<ASTNode> children = getNotYetMappedDescendantContainers(node);
				if (!children.isEmpty()) {
					expanded = true;
					for (int j = 0; j < children.size(); j++) {
						ASTNode child = children.get(j);
						l.add(i+j, child);
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
		for (int i = lenM-1; i >= 0; i--) {
			ASTNode nodeM = lM.get(i);
			int hM = treeHeight.get(nodeM);
			HashMap<String, Integer> vM = treeVector.get(nodeM);
			for (int j = 0; j <= lenN; j++)
				d[0][j] = d[1][j];
			for (int j = lenN-1; j >= 0; j--) {
				ASTNode nodeN = lN.get(j);
				int hN = treeHeight.get(nodeN);
				HashMap<String, Integer> vN = treeVector.get(nodeN);
				if (hM == hN && nodeM.getNodeType() == nodeN.getNodeType() && vM.equals(vN) && subtreeMatch(nodeM, nodeN)) {
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
		if (nodeM instanceof Assignment)
			return labelMatch((Assignment) nodeM, (Assignment) nodeN);
		if (nodeM instanceof InfixExpression)
			return labelMatch((InfixExpression) nodeM, (InfixExpression) nodeN);
		if (nodeM instanceof PostfixExpression)
			return labelMatch((PostfixExpression) nodeM, (PostfixExpression) nodeN);
		if (nodeM instanceof PrefixExpression)
			return labelMatch((PrefixExpression) nodeM, (PrefixExpression) nodeN);
		return true;
	}

	private boolean labelMatch(Assignment nodeM, Assignment nodeN) {
		return nodeM.getOperator().equals(nodeN.getOperator());
	}

	private boolean labelMatch(InfixExpression nodeM, InfixExpression nodeN) {
		return nodeM.getOperator().equals(nodeN.getOperator());
	}

	private boolean labelMatch(PostfixExpression nodeM, PostfixExpression nodeN) {
		return nodeM.getOperator().equals(nodeN.getOperator());
	}

	private boolean labelMatch(PrefixExpression nodeM, PrefixExpression nodeN) {
		return nodeM.getOperator().equals(nodeN.getOperator());
	}
	
	@SuppressWarnings("unused")
	private void lss(ArrayList<ASTNode> lM, ArrayList<ASTNode> lN, ArrayList<Integer> lcsM, ArrayList<Integer> lcsN, double threshold) {
		int lenM = lM.size(), lenN = lN.size();
		double[][] d = new double[2][lenN + 1];
		char[][] p = new char[lenM + 1][lenN + 1];
		for (int j = 0; j <= lenN; j++)
			d[1][j] = 0;
		for (int i = lenM-1; i >= 0; i--) {
			ASTNode nodeM = lM.get(i);
			for (int j = 0; j <= lenN; j++)
				d[0][j] = d[1][j];
			for (int j = lenN-1; j >= 0; j--) {
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
				return node1.getStartPosition() - node2.getStartPosition();
			}
		});
		for (ASTNode nodeM : heightsM) {
			ASTNode nodeN = treeMap.get(nodeM).keySet().iterator().next();
			ArrayList<ASTNode> ancestorsM = new ArrayList<ASTNode>(), ancestorsN = new ArrayList<ASTNode>();
			getNotYetMappedAncestors(nodeM, ancestorsM);
			getNotYetMappedAncestors(nodeN, ancestorsN);
			map(ancestorsM, ancestorsN, MIN_SIM);
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
					Pair pair = new Pair(nodeM, nodeN, sim, 
							-Math.abs((nodeM.getParent().getStartPosition() - nodeM.getStartPosition()) - (nodeN.getParent().getStartPosition() - nodeN.getStartPosition())));
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
		while (!pairs.isEmpty()) {
			Pair pair = pairs.get(0);
			ASTNode nodeM = (ASTNode) pair.getObj1(), nodeN = (ASTNode) pair.getObj2();
			setMap(nodeM, nodeN, pair.getWeight());
			nodes.add(nodeM);
			nodes.add(nodeN);
			for (Pair p : pairsOfAncestor.get(nodeM))
				pairs.remove(p);
			for (Pair p : pairsOfAncestor.get(nodeN))
				pairs.remove(p);
		}
		return nodes;
	}

	private void setMap(ASTNode nodeM, ASTNode nodeN, double w) {
		treeMap.get(nodeM).put(nodeN, w);
		treeMap.get(nodeN).put(nodeM, w);
	}

	private double computeSimilarity(ASTNode nodeM, ASTNode nodeN, double threshold) {
		if (nodeM.getNodeType() != nodeN.getNodeType())
			return 0;
		ArrayList<ASTNode> childrenM = tree.get(nodeM), childrenN = tree.get(nodeN);
		if (childrenM.isEmpty() && childrenN.isEmpty()) {
			if (nodeM instanceof Modifier) {
				Modifier mnM = (Modifier) nodeM, mnN = (Modifier) nodeN;
				if (JavaASTUtil.getType(mnM) != JavaASTUtil.getType(mnN))
					return 0;
			}
			int type = nodeM.getNodeType();
			double sim = 0;
			if (type == ASTNode.ARRAY_CREATION 
					|| type == ASTNode.ARRAY_INITIALIZER 
					|| type == ASTNode.BLOCK 
					|| type == ASTNode.INFIX_EXPRESSION 
					|| type == ASTNode.METHOD_INVOCATION
					|| type == ASTNode.SWITCH_STATEMENT
					)
				sim = MIN_SIM_MOVE;
			else {
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
					sim = StringProcessor.computeCharLCS(StringProcessor.serializeToChars(sM), StringProcessor.serializeToChars(sN));
			}
			sim = threshold + sim * (1 - threshold);
			return sim;
		}
		if (!childrenM.isEmpty() && !childrenN.isEmpty()) {
			HashMap<String, Integer> vM = treeVector.get(nodeM), vN = treeVector.get(nodeN);
			double sim = computeSimilarity(vM, vN);
			double[] sims = computeVectorSimilarity(childrenM, childrenN);
			for (double s : sims)
				sim += s;
			return sim / (sims.length + 1);
		}
		return 0;
	}

	private double[] computeVectorSimilarity(ArrayList<ASTNode> l1, ArrayList<ASTNode> l2) {
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
					int index = Collections.binarySearch(pairs, pair,
							comparator);
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
				ArrayList<ASTNode> mappedChildrenM = new ArrayList<ASTNode>(), mappedChildrenN = new ArrayList<ASTNode>();
				if (nodeM instanceof Statement) {
					if (nodeM instanceof DoStatement) {
						mappedChildrenM.add(((DoStatement) nodeM).getBody());
						mappedChildrenN.add(((DoStatement) nodeN).getBody());
					} else if (nodeM instanceof EnhancedForStatement) {
						mappedChildrenM.add(((EnhancedForStatement) nodeM).getBody());
						mappedChildrenN.add(((EnhancedForStatement) nodeN).getBody());
					} else if (nodeM instanceof ForStatement) {
						mappedChildrenM.add(((ForStatement) nodeM).getBody());
						mappedChildrenN.add(((ForStatement) nodeN).getBody());
					} else if (nodeM instanceof SynchronizedStatement) {
						mappedChildrenM.add(((SynchronizedStatement) nodeM).getBody());
						mappedChildrenN.add(((SynchronizedStatement) nodeN).getBody());
					} else if (nodeM instanceof ThrowStatement) {
						mappedChildrenM.add(((ThrowStatement) nodeM).getExpression());
						mappedChildrenN.add(((ThrowStatement) nodeN).getExpression());
					} else if (nodeM instanceof TryStatement) {
						mappedChildrenM.add(((TryStatement) nodeM).getBody());
						mappedChildrenN.add(((TryStatement) nodeN).getBody());
					} else if (nodeM instanceof TypeDeclarationStatement) {
						mappedChildrenM.add(((TypeDeclarationStatement) nodeM).getDeclaration());
						mappedChildrenN.add(((TypeDeclarationStatement) nodeN).getDeclaration());
					} else if (nodeM instanceof WhileStatement) {
						mappedChildrenM.add(((WhileStatement) nodeM).getBody());
						mappedChildrenN.add(((WhileStatement) nodeN).getBody());
					}
				} else if (nodeM instanceof MethodDeclaration) {
					mappedChildrenM.add(((MethodDeclaration) nodeM).getBody());
					mappedChildrenN.add(((MethodDeclaration) nodeN).getBody());
				} else if (nodeM instanceof CatchClause) {
					mappedChildrenM.add(((CatchClause) nodeM).getBody());
					mappedChildrenN.add(((CatchClause) nodeN).getBody());
				} else if (nodeM instanceof Expression) {
					if (nodeM instanceof ClassInstanceCreation) {
						ClassInstanceCreation cicM = (ClassInstanceCreation) nodeM, cicN = (ClassInstanceCreation) nodeN;
						mappedChildrenM.add(cicM.getExpression());
						mappedChildrenN.add(cicN.getExpression());
						if (cicM.getAST().apiLevel() >= AST.JLS3)
							mappedChildrenM.add(cicM.getType());
						else
							mappedChildrenM.add(cicM.getName());
						if (cicN.getAST().apiLevel() >= AST.JLS3)
							mappedChildrenN.add(cicN.getType());
						else
							mappedChildrenN.add(cicN.getName());
					} else if (nodeM instanceof MethodInvocation) {
						MethodInvocation miM = (MethodInvocation) nodeM, miN = (MethodInvocation) nodeN;
						mappedChildrenM.add(miM.getExpression());
						mappedChildrenN.add(miN.getExpression());
						mappedChildrenM.add(miM.getName());
						mappedChildrenN.add(miN.getName());
					} else if (nodeM instanceof SuperMethodInvocation) {
						SuperMethodInvocation miM = (SuperMethodInvocation) nodeM, miN = (SuperMethodInvocation) nodeN;
						mappedChildrenM.add(miM.getQualifier());
						mappedChildrenN.add(miN.getQualifier());
						mappedChildrenM.add(miM.getName());
						mappedChildrenN.add(miN.getName());
					}
				}
				if (!mappedChildrenM.isEmpty() && !mappedChildrenN.isEmpty()) {
					for (int i = 0; i < mappedChildrenM.size(); i++) {
						ASTNode childM = mappedChildrenM.get(i), childN = mappedChildrenN.get(i);
						if (childM != null && childN != null) {
							if (treeMap.get(childM).isEmpty() && treeMap.get(childN).isEmpty()) {
								double sim = 0;
								if (childM.getNodeType() == childN.getNodeType()) {
									int type = childM.getNodeType();
									if (type == ASTNode.BLOCK || (treeMap.get(childM).isEmpty() && treeMap.get(childN).isEmpty()))
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
									ArrayList<ASTNode> tempM = new ArrayList<ASTNode>(), tempN = new ArrayList<ASTNode>();
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
										ASTNode mappedNodeM = mappedNodes.get(j), mappedNodeN = mappedNodes.get(j+1);
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
				for (int i = lcsM.size()-1; i >= 0; i--) {
					int iM = lcsM.get(i), iN = lcsN.get(i);
					ASTNode nM = nodesM.get(iM), nN = nodesN.get(iN);
					setMap(nM, nN, 1.0);
					nodesM.remove(iM);
					nodesN.remove(iN);
				}
				/*lcsM.clear(); lcsN.clear();
				lss(nodesM, nodesN, lcsM, lcsN, MIN_SIM);
				for (int i = lcsM.size()-1; i >= 0; i--) {
					int iM = lcsM.get(i), iN = lcsN.get(i);
					ASTNode nM = nodesM.get(iM), nN = nodesN.get(iN);
					setMap(nM, nN, 0.99050); // TODO
					nodesM.remove(iM);
					nodesN.remove(iN);
				}*/
				lcsM.clear(); lcsN.clear();
				ArrayList<ASTNode> mappedNodes = map(nodesM, nodesN, MIN_SIM);
				for (int i = 0; i < mappedNodes.size(); i += 2) {
					ASTNode mappedNodeM = mappedNodes.get(i), mappedNodeN = mappedNodes.get(i+1);
					nodesM.remove(mappedNodeM);
					nodesN.remove(mappedNodeN);
				}
				ArrayList<ASTNode> maxsM = new ArrayList<ASTNode>(), maxsN = new ArrayList<ASTNode>();
				int maxhM = maxHeight(nodesM, maxsM), maxhN = maxHeight(nodesN, maxsN);
				if (maxhM >= maxhN) {
					for (ASTNode node : maxsM) {
						nodesM.remove(node);
						nodesM.addAll(getNotYetMatchedNodes(tree.get(node)));
					}
				}
				if (maxhN >= maxhM) {
					for (ASTNode node : maxsN) {
						nodesN.remove(node);
						nodesN.addAll(getNotYetMatchedNodes(tree.get(node)));
					}
				}
				mappedNodes = map(nodesM, nodesN, MIN_SIM_MOVE);
				for (int i = 0; i < mappedNodes.size(); i += 2) {
					ASTNode mappedNodeM = mappedNodes.get(i), mappedNodeN = mappedNodes.get(i+1);
					nodesM.remove(mappedNodeM);
					nodesN.remove(mappedNodeN);
				}
			}
		}
		for (ASTNode child : childrenM)
			mapTopDown(child);
	}

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

	private void mapMoving() {
		ArrayList<ASTNode> lM = getNotYetMappedDescendantContainers(astM), lN = getNotYetMappedDescendantContainers(astN);
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

	private void mapMoving(ArrayList<ASTNode> lM, ArrayList<ASTNode> lN, ArrayList<ASTNode> heightsM, ArrayList<ASTNode> heightsN) {
		ArrayList<ASTNode> mappedNodes = map(lM, lN, MIN_SIM_MOVE);
		for (int i = 0; i < mappedNodes.size(); i += 2) {
			ASTNode nodeM = mappedNodes.get(i), nodeN = mappedNodes.get(i+1);
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

	private void buildTrees() {
		buildTree(astM);
		buildTree(astN);
	}

	private void buildTree(final ASTNode root) {
		final TreedBuilder visitor = new TreedBuilder(root);
		root.accept(visitor);
		tree.putAll(visitor.tree);
		treeHeight.putAll(visitor.treeHeight);
		treeDepth.putAll(visitor.treeDepth);
		treeVector.putAll(visitor.treeVector);
	}
	
}
