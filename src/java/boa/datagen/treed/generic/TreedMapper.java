package boa.datagen.treed.generic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import boa.types.Ast.Declaration;
import boa.types.Ast.Expression;
import boa.types.Ast.Method;
import boa.types.Ast.Namespace;
import boa.types.Ast.Statement;
import boa.types.Ast.Type;
import boa.types.Ast.Variable;
import boa.types.Shared.ChangeKind;

public class TreedMapper implements TreedConstants {
	private Object astM, astN;
	private HashMap<String, Integer> nodeTypes = new HashMap<>();
	private HashMap<String, Object> propertyStatus = new HashMap<>();
	private HashMap<String, Object> propertyMap = new HashMap<>();
	private int type = 1;

	private HashMap<Object, Object> treeParent=new HashMap<Object, Object>();
	private HashMap<String, ArrayList<Object>> tree = new HashMap<String, ArrayList<Object>>();
	private HashMap<String, Integer> treeHeight = new HashMap<String, Integer>(),
			treeDepth = new HashMap<String, Integer>();
	private HashMap<String, HashMap<String, Integer>> treeVector = new HashMap<String, HashMap<String, Integer>>();
	private HashMap<String, HashMap<Object, Double>> treeMap = new HashMap<String, HashMap<Object, Double>>();
	private HashSet<Object> pivotsM = new HashSet<Object>(), pivotsN = new HashSet<Object>();
	private int numOfChanges = 0, numOfUnmaps = 0, numOfNonNameUnMaps = 0;

	public TreedMapper(Object astM, Object astN) {
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
		
		TreeChangeSetter changeSetter=new TreeChangeSetter(this.propertyStatus);
		astM=changeSetter.visit((Namespace) astM);
		astN=changeSetter.visit((Namespace) astN);
	}
	
	public Namespace getCurrentChanges()
	{
		return (Namespace) astN;
	}
	public Namespace getPreviousChanges()
	{
		return (Namespace) astM;
	}

	private void markUnchanges(Object node) {
		ArrayList<Object> children = tree.get(Integer.toHexString(node.hashCode()));
		for (Object child : children)
			markUnchanges(child);
		ChangeKind status = (ChangeKind) propertyStatus.get(Integer.toHexString(node.hashCode()));
		if (status == ChangeKind.UNCHANGED) {
			boolean unchanged = isUnchanged(children);
			if (unchanged) {
				Object mappedNode = (Object) propertyMap.get(Integer.toHexString(node.hashCode()));
				unchanged = isUnchanged(tree.get(Integer.toHexString(node.hashCode())));
			}
			if (!unchanged) {
				Object mappedNode = (Object) propertyMap.get(Integer.toHexString(node.hashCode()));
				propertyStatus.put(Integer.toHexString(node.hashCode()),ChangeKind.MODIFIED);
				propertyStatus.put(Integer.toHexString(mappedNode.hashCode()),ChangeKind.MODIFIED);
			}
		}
	}

	private boolean isUnchanged(ArrayList<Object> children) {
		for (Object child : children)
			if ((ChangeKind) propertyStatus.get(Integer.toHexString(child.hashCode())) != ChangeKind.UNCHANGED)
				return false;
		return true;
	}

	private void markChanges() {
		markAstM(astM);
		markAstN(astN);
	}

	private void markAstN(Object node) {
		if (propertyStatus.get(Integer.toHexString(node.hashCode())) == null) {
			propertyStatus.put(Integer.toHexString(node.hashCode()),ChangeKind.ADDED);
			numOfChanges++;
			numOfUnmaps++;
			if (!(TreedUtils.isLiteral(node)))
				numOfNonNameUnMaps++;
		}
		ArrayList<Object> children = tree.get(Integer.toHexString(node.hashCode()));
		for (Object child : children)
			markAstN(child);
	}

	private void markAstM(Object node) {
		HashMap<Object, Double> maps = treeMap.get(Integer.toHexString(node.hashCode()));
		if (maps.isEmpty()) {
			propertyStatus.put(Integer.toHexString(node.hashCode()),ChangeKind.DELETED);
			numOfChanges++;
			numOfUnmaps++;
			if (!(TreedUtils.isLiteral(node)) && !(TreedUtils.isReturnStatement(node)) && !(TreedUtils.isBreakStatement(node))
					&& !(TreedUtils.isContinueStatement(node)))
				numOfNonNameUnMaps++;
		} else {
			Object mappedNode = maps.keySet().iterator().next();
			this.propertyMap.put(Integer.toHexString(node.hashCode()),mappedNode);
			this.propertyMap.put(Integer.toHexString(mappedNode.hashCode()),node);
			if (node == astM) {
				this.propertyStatus.put(Integer.toHexString(astM.hashCode()),ChangeKind.UNCHANGED);
				this.propertyStatus.put(Integer.toHexString(astN.hashCode()),ChangeKind.UNCHANGED);
			} else {
				System.out.println(Integer.toHexString(node.hashCode()));
				System.out.println(Integer.toHexString(mappedNode.hashCode()));
				Object p = treeParent.get(node), mp = treeParent.get(mappedNode);
				if (!treeMap.get(Integer.toHexString(p.hashCode())).containsKey(mp)) {
					this.propertyStatus.put(Integer.toHexString(node.hashCode()),ChangeKind.MOVED);
					this.propertyStatus.put(Integer.toHexString(mappedNode.hashCode()),ChangeKind.MOVED);
					numOfChanges += 2;
				} else {
					if (propertyStatus.get(Integer.toHexString(node.hashCode())) == null) {
						this.propertyStatus.put(Integer.toHexString(node.hashCode()),ChangeKind.MOVED);
						this.propertyStatus.put(Integer.toHexString(mappedNode.hashCode()),ChangeKind.MOVED);
						numOfChanges += 2;
					}
				}
			}
			// mark moving for children
			ArrayList<Object> children = tree.get(Integer.toHexString(node.hashCode())), mappedChildren = tree.get(Integer.toHexString(mappedNode.hashCode()));
			if (!children.isEmpty() && !mappedChildren.isEmpty()) {
				markChanges(children, mappedChildren);
			}
		}
		ArrayList<Object> children = tree.get(Integer.toHexString(node.hashCode()));
		for (Object child : children)
			markAstM(child);
	}

	private void markChanges(ArrayList<Object> nodes, ArrayList<Object> mappedNodes) {
		int len = nodes.size(), lenN = mappedNodes.size();
		int[][] d = new int[2][lenN + 1];
		char[][] p = new char[len + 1][lenN + 1];
		d[1][0] = 0;
		for (int j = 1; j <= lenN; j++)
			d[1][j] = 0;
		for (int i = 1; i <= len; i++) {
			Object node = nodes.get(i - 1);
			HashMap<Object, Double> maps = treeMap.get(Integer.toHexString(node.hashCode()));
			for (int j = 0; j <= lenN; j++)
				d[0][j] = d[1][j];
			for (int j = 1; j <= lenN; j++) {
				Object nodeN = mappedNodes.get(j - 1);
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
				Object node = nodes.get(i - 1), node2 = mappedNodes.get(j - 1);
				if (TreedUtils.buildLabelForVector(node, this.nodeTypes) == TreedUtils.buildLabelForVector(node2, this.nodeTypes)) {
					this.propertyStatus.put(Integer.toHexString(node.hashCode()), ChangeKind.UNCHANGED);
					this.propertyStatus.put(Integer.toHexString(node2.hashCode()), ChangeKind.UNCHANGED);
				} else {
					this.propertyStatus.put(Integer.toHexString(node.hashCode()), ChangeKind.RENAMED);
					this.propertyStatus.put(Integer.toHexString(node2.hashCode()), ChangeKind.RENAMED);
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
		for (String node : tree.keySet())
			treeMap.put(node, new HashMap<Object, Double>());
		setMap(astM, astN, 1.0);
		ArrayList<Object> lM = getChildrenContainers(astM), lN = getChildrenContainers(astN);
		ArrayList<Object> heightsM = new ArrayList<Object>(lM), heightsN = new ArrayList<Object>(lN);
		Collections.sort(heightsM, new Comparator<Object>() {
			@Override
			public int compare(Object node1, Object node2) {
				return treeHeight.get(Integer.toHexString(node2.hashCode())) - treeHeight.get(Integer.toHexString(node1.hashCode()));
			}
		});
		Collections.sort(heightsN, new Comparator<Object>() {
			@Override
			public int compare(Object node1, Object node2) {
				return treeHeight.get(Integer.toHexString(node2.hashCode())) - treeHeight.get(Integer.toHexString(node1.hashCode()));
			}
		});
		mapPivots(lM, lN, heightsM, heightsN);
	}

	private void mapPivots(ArrayList<Object> lM, ArrayList<Object> lN, ArrayList<Object> heightsM,
			ArrayList<Object> heightsN) {
		if (lM.size() * lN.size() > MAX_BIPARTITE_MATCH_SIZE) {
			lM.clear();
			lN.clear();
		}
		ArrayList<Integer> lcsM = new ArrayList<Integer>(), lcsN = new ArrayList<Integer>();
		lcs(lM, lN, lcsM, lcsN);
		for (int i = lcsM.size() - 1; i >= 0; i--) {
			int indexM = lcsM.get(i), indexN = lcsN.get(i);
			Object nodeM = lM.get(indexM), nodeN = lN.get(indexN);
			setMap(nodeM, nodeN, 1.0);
			pivotsM.add(nodeM);
			pivotsN.add(nodeN);
			lM.remove(indexM);
			lN.remove(indexN);
			heightsM.remove(nodeM);
			heightsN.remove(nodeN);
		}
		while (!lM.isEmpty() && !lN.isEmpty()) {
			int hM = treeHeight.get(Integer.toHexString(heightsM.get(0).hashCode()));
			int hN = treeHeight.get(Integer.toHexString(heightsN.get(0).hashCode()));
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

	private boolean expandForPivots(ArrayList<Object> l, ArrayList<Object> heights, int h) {
		HashSet<Object> nodes = new HashSet<Object>();
		for (Object node : heights) {
			if (treeHeight.get(Integer.toHexString(node.hashCode())) == h)
				nodes.add(node);
			else
				break;
		}
		boolean expanded = false;
		for (int i = l.size() - 1; i >= 0; i--) {
			Object node = l.get(i);
			if (nodes.contains(node)) {
				l.remove(i);
				heights.remove(0);
				ArrayList<Object> children = getChildrenContainers(node);
				if (!children.isEmpty() && children.size() <= MAX_EXPENSION_SIZE) {
					expanded = true;
					for (int j = 0; j < children.size(); j++) {
						Object child = children.get(j);
						l.add(i + j, child);
						int index = Collections.binarySearch(heights, child, new Comparator<Object>() {
							@Override
							public int compare(Object node1, Object node2) {
								return treeHeight.get(Integer.toHexString(node2.hashCode())) - treeHeight.get(Integer.toHexString(node1.hashCode()));
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

	private void lcs(ArrayList<Object> lM, ArrayList<Object> lN, ArrayList<Integer> lcsM, ArrayList<Integer> lcsN) {
		int lenM = lM.size(), lenN = lN.size();
		int[][] d = new int[2][lenN + 1];
		char[][] p = new char[lenM + 1][lenN + 1];
		for (int j = 0; j <= lenN; j++)
			d[1][j] = 0;
		for (int i = lenM - 1; i >= 0; i--) {
			Object nodeM = lM.get(i);
			int hM = treeHeight.get(Integer.toHexString(nodeM.hashCode()));
			HashMap<String, Integer> vM = treeVector.get(Integer.toHexString(nodeM.hashCode()));
			for (int j = 0; j <= lenN; j++)
				d[0][j] = d[1][j];
			for (int j = lenN - 1; j >= 0; j--) {
				Object nodeN = lN.get(j);
				int hN = treeHeight.get(Integer.toHexString(nodeN.hashCode()));
				HashMap<String, Integer> vN = treeVector.get(Integer.toHexString(nodeN.hashCode()));
				if (hM == hN && nodeTypes.get(TreedUtils.getNodeType(nodeM)) == nodeTypes.get(TreedUtils.getNodeType(nodeN)) && vM.equals(vN)
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

	private boolean subtreeMatch(Object nodeM, Object nodeN) {
		if (!labelMatch(nodeM, nodeN))
			return false;
		ArrayList<Object> childrenM = tree.get(Integer.toHexString(nodeM.hashCode())), childrenN = tree.get(Integer.toHexString(nodeN.hashCode()));
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

	private boolean labelMatch(Object nodeM, Object nodeN) {
		if (nodeTypes.get(TreedUtils.getNodeType(nodeM)) != nodeTypes.get(TreedUtils.getNodeType(nodeN)))
			return false;

		if (nodeM instanceof Expression)
			return labelMatch((Expression) nodeM, (Expression) nodeN);
		
		if (nodeM instanceof Statement)
			return TreedUtils.getNodeType(nodeM).equals(TreedUtils.getNodeType(nodeN));
		
		return true;
	}

	private boolean labelMatch(Expression nodeM, Expression nodeN) {
		if (!TreedUtils.getNodeType(nodeM).equals(TreedUtils.getNodeType(nodeN)))
			return false;

		return TreedUtils.getNodeName(nodeM).equals(TreedUtils.getNodeName(nodeN));
	}

	@SuppressWarnings("unused")
	private void lss(ArrayList<Object> lM, ArrayList<Object> lN, ArrayList<Integer> lcsM, ArrayList<Integer> lcsN,
			double threshold) {
		int lenM = lM.size(), lenN = lN.size();
		double[][] d = new double[2][lenN + 1];
		char[][] p = new char[lenM + 1][lenN + 1];
		for (int j = 0; j <= lenN; j++)
			d[1][j] = 0;
		for (int i = lenM - 1; i >= 0; i--) {
			Object nodeM = lM.get(i);
			for (int j = 0; j <= lenN; j++)
				d[0][j] = d[1][j];
			for (int j = lenN - 1; j >= 0; j--) {
				Object nodeN = lN.get(j);
				double sim = computeSimilarity(nodeM, nodeN, threshold);
				if (nodeTypes.get(TreedUtils.getNodeType(nodeM)) == nodeTypes.get(TreedUtils.getNodeType(nodeN)) && sim >= threshold) {
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

	private ArrayList<Object> getChildrenContainers(Object node) {
		ArrayList<Object> children = new ArrayList<Object>();
		for (Object child : tree.get(Integer.toHexString(node.hashCode()))) {
			if (treeHeight.get(Integer.toHexString(child.hashCode())) >= MIN_HEIGHT)
				children.add(child);
		}
		return children;
	}

	private void mapBottomUp() {
		ArrayList<Object> heightsM = new ArrayList<Object>(pivotsM);
		Collections.sort(heightsM, new Comparator<Object>() {
			@Override
			public int compare(Object node1, Object node2) {
				int d = treeHeight.get(Integer.toHexString(node2.hashCode())) - treeHeight.get(Integer.toHexString(node1.hashCode()));
				if (d != 0)
					return d;
				d = treeDepth.get(Integer.toHexString(node1.hashCode())) - treeDepth.get(Integer.toHexString(node2.hashCode()));
				if (d != 0)
					return d;
				return 0;
			}
		});
		Set<Object> visitedAncestorsM = new HashSet<Object>(), visitedAncestorsN = new HashSet<Object>();
		for (Object nodeM : heightsM) {
			Object nodeN = treeMap.get(Integer.toHexString(nodeM.hashCode())).keySet().iterator().next();
			ArrayList<Object> ancestorsM = new ArrayList<Object>(), ancestorsN = new ArrayList<Object>();
			ancestorsM.removeAll(visitedAncestorsM);
			ancestorsN.removeAll(visitedAncestorsN);
			getNotYetMappedAncestors(nodeM, ancestorsM);
			getNotYetMappedAncestors(nodeN, ancestorsN);
			map(ancestorsM, ancestorsN, MIN_SIM);
			visitedAncestorsM.addAll(ancestorsM);
			visitedAncestorsN.addAll(ancestorsN);
		}
	}

	private ArrayList<Object> map(ArrayList<Object> nodesM, ArrayList<Object> nodesN, double threshold) {
		HashMap<Object, HashSet<Pair>> pairsOfAncestor = new HashMap<Object, HashSet<Pair>>();
		ArrayList<Pair> pairs = new ArrayList<Pair>();
		PairDescendingOrder comparator = new PairDescendingOrder();
		for (Object nodeM : nodesM) {
			HashSet<Pair> pairs1 = new HashSet<Pair>();
			for (Object nodeN : nodesN) {
				double sim = computeSimilarity(nodeM, nodeN, threshold);
				if (sim >= threshold) {
					Pair pair = new Pair(nodeM, nodeN, sim, 0.0);
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
		ArrayList<Object> nodes = new ArrayList<Object>();
		Set<Object> matches = new HashSet<Object>();
		for (int i = 0; i < pairs.size(); i++) {
			Pair pair = pairs.get(i);
			Object nodeM = (Object) pair.getObj1(), nodeN = (Object) pair.getObj2();
			if (matches.contains(nodeM) || matches.contains(nodeN))
				continue;
			setMap(nodeM, nodeN, pair.getWeight());
			nodes.add(nodeM);
			nodes.add(nodeN);
			matches.add(nodeM);
			matches.add(nodeN);
		}
		/*
		 * while (!pairs.isEmpty()) { Pair pair = pairs.get(0); Object nodeM =
		 * (Object) pair.getObj1(), nodeN = (Object) pair.getObj2(); setMap(nodeM,
		 * nodeN, pair.getWeight()); nodes.add(nodeM); nodes.add(nodeN); for (Pair p :
		 * pairsOfAncestor.get(nodeM)) pairs.remove(p); for (Pair p :
		 * pairsOfAncestor.get(nodeN)) pairs.remove(p); }
		 */
		return nodes;
	}

	private void setMap(Object nodeM, Object nodeN, double w) {
		// map node of tree 1 to corresponding node of tree 2
		treeMap.get(Integer.toHexString(nodeM.hashCode())).put(nodeN, w);
		treeMap.get(Integer.toHexString(nodeN.hashCode())).put(nodeM, w);
	}

	private double computeSimilarity(Object nodeM, Object nodeN, double threshold) {
		if (nodeTypes.get(TreedUtils.getNodeType(nodeM)) != nodeTypes.get(TreedUtils.getNodeType(nodeN)))
			return 0;
		ArrayList<Object> childrenM = tree.get(Integer.toHexString(nodeM.hashCode())), childrenN = tree.get(Integer.toHexString(nodeN.hashCode()));
		if (childrenM.isEmpty() && childrenN.isEmpty()) {

			int type = nodeTypes.get(TreedUtils.getNodeType(nodeM));
			double sim = 0;
//			if (type == Object.ARRAY_CREATION 
//					|| type == Object.ARRAY_INITIALIZER 
//					|| type == Object.BLOCK 
//					|| type == Object.INFIX_EXPRESSION 
//					|| type == Object.METHOD_INVOCATION
//					|| type == Object.SWITCH_STATEMENT
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
			HashMap<String, Integer> vM = treeVector.get(Integer.toHexString(nodeM.hashCode())), vN = treeVector.get(Integer.toHexString(nodeN.hashCode()));
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
	private double[] computeSimilarity(ArrayList<Object> l1, ArrayList<Object> l2) {
		double[] sims = new double[Math.max(l1.size(), l2.size())];
		Arrays.fill(sims, 0.0);
		HashMap<Object, HashSet<Pair>> pairsOfNode = new HashMap<Object, HashSet<Pair>>();
		ArrayList<Pair> pairs = new ArrayList<Pair>();
		PairDescendingOrder comparator = new PairDescendingOrder();
		for (Object node1 : l1) {
			HashSet<Pair> pairs1 = new HashSet<Pair>();
			for (Object node2 : l2) {
				double sim = computeSimilarity(treeVector.get(Integer.toHexString(node1.hashCode())), treeVector.get(Integer.toHexString(node2.hashCode())));
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

	private void getNotYetMappedAncestors(Object node, ArrayList<Object> ancestors) {
		Object p = treeParent.get(node);
		if (treeMap.get(Integer.toHexString(p.hashCode())).isEmpty()) {
			ancestors.add(p);
			getNotYetMappedAncestors(p, ancestors);
		}
	}

	private void mapTopDown() {
		mapTopDown(astM);
	}

	private void mapTopDown(Object nodeM) {
		ArrayList<Object> childrenM = tree.get(Integer.toHexString(nodeM.hashCode()));
		HashMap<Object, Double> maps = treeMap.get(Integer.toHexString(nodeM.hashCode()));
		if (!maps.isEmpty()) {
			Object nodeN = maps.keySet().iterator().next();
			ArrayList<Object> childrenN = tree.get(Integer.toHexString(nodeN.hashCode()));
			if (pivotsM.contains(nodeM)) {
				mapUnchangedNodes(nodeM, nodeN);
				return;
			} else {
				ArrayList<Object> nodesM = getNotYetMatchedNodes(childrenM), nodesN = getNotYetMatchedNodes(childrenN);
				ArrayList<Object> mappedChildrenM = new ArrayList<Object>(),
						mappedChildrenN = new ArrayList<Object>();

				if ( nodeM instanceof Statement && !TreedUtils.isBlockStatement(nodeM)) {
					this.topDownHelper_map(this.topDownHelper_convertExpression(((Statement) nodeM).getExpressionsList()),
							this.topDownHelper_convertExpression(((Statement) nodeN).getExpressionsList()), 
							mappedChildrenM, mappedChildrenN);
					this.topDownHelper_map(this.topDownHelper_convertExpression(((Statement) nodeM).getConditionsList()),
							this.topDownHelper_convertExpression(((Statement) nodeN).getConditionsList()), 
							mappedChildrenM, mappedChildrenN);
					this.topDownHelper_map(this.topDownHelper_convertStatement(((Statement) nodeM).getStatementsList()),
							this.topDownHelper_convertStatement(((Statement) nodeN).getStatementsList()), 
							mappedChildrenM, mappedChildrenN);
					this.topDownHelper_map(this.topDownHelper_convertVariable(((Statement) nodeM).getVariableDeclarationsList()),
							this.topDownHelper_convertVariable(((Statement) nodeN).getVariableDeclarationsList()), 
							mappedChildrenM, mappedChildrenN);
					
				}  else if ( nodeM instanceof Expression) {
					this.topDownHelper_map(this.topDownHelper_convertExpression(((Expression) nodeM).getExpressionsList()),
							this.topDownHelper_convertExpression(((Expression) nodeN).getExpressionsList()), 
							mappedChildrenM, mappedChildrenN);
					this.topDownHelper_map(this.topDownHelper_convertExpression(((Expression) nodeM).getMethodArgsList()),
							this.topDownHelper_convertExpression(((Expression) nodeN).getMethodArgsList()), 
							mappedChildrenM, mappedChildrenN);
					this.topDownHelper_map(this.topDownHelper_convertStatement(((Expression) nodeM).getStatementsList()),
							this.topDownHelper_convertStatement(((Expression) nodeN).getStatementsList()), 
							mappedChildrenM, mappedChildrenN);
					this.topDownHelper_map(this.topDownHelper_convertVariable(((Expression) nodeM).getVariableDeclsList()),
							this.topDownHelper_convertVariable(((Expression) nodeN).getVariableDeclsList()), 
							mappedChildrenM, mappedChildrenN);
				}

				else if (nodeM instanceof Method) {
					this.topDownHelper_map(this.topDownHelper_convertStatement(((Method) nodeM).getStatementsList()),
							this.topDownHelper_convertStatement(((Method) nodeN).getStatementsList()), 
							mappedChildrenM, mappedChildrenN);
					this.topDownHelper_map(this.topDownHelper_convertVariable(((Method) nodeM).getArgumentsList()),
							this.topDownHelper_convertVariable(((Method) nodeN).getArgumentsList()), 
							mappedChildrenM, mappedChildrenN);
				} else if (nodeM instanceof Declaration) {
					this.topDownHelper_map(this.topDownHelper_convertStatement(((Declaration) nodeM).getStatementsList()),
							this.topDownHelper_convertStatement(((Declaration) nodeN).getStatementsList()), 
							mappedChildrenM, mappedChildrenN);
					this.topDownHelper_map(this.topDownHelper_convertTypes(((Declaration) nodeM).getParentsList()),
							this.topDownHelper_convertTypes(((Declaration) nodeN).getParentsList()), 
							mappedChildrenM, mappedChildrenN);
				} 
				else if (nodeM instanceof Variable) {
					mappedChildrenM.add(((Variable) nodeM).getComputedName());
					mappedChildrenN.add(((Variable) nodeN).getComputedName());
					mappedChildrenM.add(((Variable) nodeM).getInitializer());
					mappedChildrenN.add(((Variable) nodeN).getInitializer());
				}
				else if (nodeM instanceof Type) {
					mappedChildrenM.add(((Type) nodeM).getComputedName());
					mappedChildrenN.add(((Type) nodeN).getComputedName());
				}


				if (!mappedChildrenM.isEmpty() && !mappedChildrenN.isEmpty()) {
					for (int i = 0; i < mappedChildrenM.size(); i++) {
						Object childM = mappedChildrenM.get(i), childN = mappedChildrenN.get(i);
						if (childM != null && childN != null) {
							if (treeMap.get(Integer.toHexString(childM.hashCode())).isEmpty() && treeMap.get(Integer.toHexString(childN.hashCode())).isEmpty()) {
								double sim = 0;
								if (nodeTypes.get(TreedUtils.getNodeType(childM)) == nodeTypes.get(TreedUtils.getNodeType(childN))) {
									int type = nodeTypes.get(TreedUtils.getNodeType(childM));
									
									sim = computeSimilarity(childM, childN, MIN_SIM);
									
									if (sim >= MIN_SIM) {
										setMap(childM, childN, MIN_SIM);
										if (TreedUtils.buildASTLabel(childM).equals(TreedUtils.buildASTLabel(childN))) {
											this.propertyStatus.put(Integer.toHexString(childM.hashCode()),ChangeKind.UNCHANGED);
											this.propertyStatus.put(Integer.toHexString(childN.hashCode()),ChangeKind.UNCHANGED);
										} else {
											this.propertyStatus.put(Integer.toHexString(childM.hashCode()),ChangeKind.RENAMED);
											this.propertyStatus.put(Integer.toHexString(childN.hashCode()),ChangeKind.RENAMED);
										}
									}
								}
								if (sim < MIN_SIM) {
									ArrayList<Object> tempM = new ArrayList<Object>(),
											tempN = new ArrayList<Object>();
									tempM.add(childM);
									tempN.add(childN);
									int hM = treeHeight.get(Integer.toHexString(childM.hashCode())), hN = treeHeight.get(Integer.toHexString(childN.hashCode()));
									if (hM >= hN) {
										tempM.remove(childM);
										tempM.addAll(getNotYetMatchedNodes(tree.get(Integer.toHexString(childM.hashCode()))));
									}
									if (hN >= hM) {
										tempN.remove(childN);
										tempN.addAll(getNotYetMatchedNodes(tree.get(Integer.toHexString(childN.hashCode()))));
									}
									ArrayList<Object> mappedNodes = map(tempM, tempN, MIN_SIM_MOVE);
									for (int j = 0; j < mappedNodes.size(); j += 2) {
										Object mappedNodeM = mappedNodes.get(j), mappedNodeN = mappedNodes.get(j + 1);
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
					Object nM = nodesM.get(iM), nN = nodesN.get(iN);
					setMap(nM, nN, 1.0);
					nodesM.remove(iM);
					nodesN.remove(iN);
				}
				lcsM.clear();
				lcsN.clear();
				ArrayList<Object> mappedNodes = map(nodesM, nodesN, MIN_SIM);
				for (int i = 0; i < mappedNodes.size(); i += 2) {
					Object mappedNodeM = mappedNodes.get(i), mappedNodeN = mappedNodes.get(i + 1);
					nodesM.remove(mappedNodeM);
					nodesN.remove(mappedNodeN);
				}
				/*
				 * ArrayList<Object> maxsM = new ArrayList<Object>(), maxsN = new
				 * ArrayList<Object>(); int maxhM = maxHeight(nodesM, maxsM), maxhN =
				 * maxHeight(nodesN, maxsN); if (maxhM >= maxhN) { for (Object node : maxsM) {
				 * nodesM.remove(node); nodesM.addAll(getNotYetMatchedNodes(tree.get(node))); }
				 * } if (maxhN >= maxhM) { for (Object node : maxsN) { nodesN.remove(node);
				 * nodesN.addAll(getNotYetMatchedNodes(tree.get(node))); } } mappedNodes =
				 * map(nodesM, nodesN, MIN_SIM_MOVE); for (int i = 0; i < mappedNodes.size(); i
				 * += 2) { Object mappedNodeM = mappedNodes.get(i), mappedNodeN =
				 * mappedNodes.get(i+1); nodesM.remove(mappedNodeM); nodesN.remove(mappedNodeN);
				 * }
				 */
			}
		}
		for (Object child : childrenM)
			mapTopDown(child);
	}

	@SuppressWarnings("unused")
	private int maxHeight(ArrayList<Object> nodes, ArrayList<Object> maxs) {
		int max = 0;
		for (Object node : nodes) {
			int h = treeHeight.get(Integer.toHexString(node.hashCode()));
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

	private void mapUnchangedNodes(Object nodeM, Object nodeN) {
		setMap(nodeM, nodeN, 1.0);
		ArrayList<Object> childrenM = tree.get(Integer.toHexString(nodeM.hashCode())), childrenN = tree.get(Integer.toHexString(nodeN.hashCode()));
		for (int i = 0; i < childrenM.size(); i++)
			mapUnchangedNodes(childrenM.get(i), childrenN.get(i));
	}

	private ArrayList<Object> getNotYetMatchedNodes(ArrayList<Object> l) {
		ArrayList<Object> nodes = new ArrayList<Object>();
		for (Object node : l)
			if (treeMap.get(Integer.toHexString(node.hashCode())).isEmpty())
				nodes.add(node);
		return nodes;
	}
	
	List<Object> topDownHelper_convertTypes(List<Type> lst)
	{
		List<Object> ret=new ArrayList<Object>();
		for(Type e: lst)
			ret.add(e);
		return ret;
	}
	List<Object> topDownHelper_convertExpression(List<Expression> lst)
	{
		List<Object> ret=new ArrayList<Object>();
		for(Expression e: lst)
			ret.add(e);
		return ret;
	}
	List<Object> topDownHelper_convertStatement(List<Statement> lst)
	{
		List<Object> ret=new ArrayList<Object>();
		for(Statement e: lst)
			ret.add(e);
		return ret;
	}
	List<Object> topDownHelper_convertVariable(List<Variable> lst)
	{
		List<Object> ret=new ArrayList<Object>();
		for(Variable e: lst)
			ret.add(e);
		return ret;
	}
	void topDownHelper_map(List<Object> left,List<Object> right, 
			ArrayList<Object> mappedChildrenM, ArrayList<Object> mappedChildrenN) {
		int ls=left.size(), rs=right.size();
		int ms=Math.max(ls, rs);
		for(int i=0;i<ms;i++)
		{
			if(i<ls)
				mappedChildrenM.add(left.get(i));
			else
				mappedChildrenM.add(null);
			
			if(i<rs)
				mappedChildrenN.add(right.get(i));
			else
				mappedChildrenN.add(null);
		}
	}

	private void buildTrees() throws Exception {
		buildTree(astM);
		buildTree(astN);
	}

	private void buildTree(final Object root) throws Exception {
		TreedBuilder visitor = new TreedBuilder(root, type, nodeTypes);
		visitor.visit((Namespace) root, null);
		this.type = visitor.type;
		this.nodeTypes = visitor.nodeTypes;
		tree.putAll(visitor.tree);
		treeHeight.putAll(visitor.treeHeight);
		treeDepth.putAll(visitor.treeDepth);
		treeVector.putAll(visitor.treeVector);
		treeParent.putAll(visitor.treeParent);
	}

}
