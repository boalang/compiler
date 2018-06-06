/*
 * Copyright 2018, Robert Dyer, Mohd Arafat
 *                 and Bowling Green State University
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
package boa.graphs.pdg;

import boa.functions.BoaAstIntrinsics;
import boa.graphs.cdg.CDG;
import boa.graphs.cdg.CDGEdge;
import boa.graphs.cdg.CDGNode;
import boa.graphs.cfg.CFG;
import boa.graphs.ddg.DDG;
import boa.graphs.ddg.DDGNode;
import boa.types.Ast.*;
import boa.types.Control;

import java.util.*;

import static boa.functions.BoaAstIntrinsics.prettyprint;
import static boa.functions.BoaNormalFormIntrinsics.normalizeExpression;
import static boa.functions.BoaNormalFormIntrinsics.normalizeStatement;

/**
 * Program Dependence Graph builder
 *
 * @author marafat
 * @author rdyer
 */
public class PDG {
    private Method md;
    private PDGNode entryNode;
    private final HashSet<PDGNode> nodes = new HashSet<PDGNode>();
    private boolean normalize = false;
    private int hashcode;

    /**
     * Constructs a program dependence graph
     *
     * @param cdg control dependence graph
     * @param ddg data dependence graph
     */
    public PDG(final CDG cdg, final DDG ddg) {
        if (cdg.getNodes().size() > 0) {
            this.md = cdg.getMethod();
            addCDG(cdg);
            addDDGEdges(ddg);
            entryNode = getNode(0);
        }
    }

    /**
     * Constructs a program dependence graph.
     *
     * @param cfg control flow graph
     * @throws Exception if PDG construction fails
     */
    public PDG(final CFG cfg) throws Exception {
        this(new CDG(cfg), new DDG(cfg));
    }

    /**
     * Constructs a program dependence graph
     *
     * @param md method whose PDG is to be built
     * @param paramAsStatement if true, inserts parameters as assign statements at the
     *                         begining of control flow graph. Default is set to false
     * @throws Exception if PDG construction fails
     */
    public PDG(final Method md, final boolean paramAsStatement) throws Exception {
        this(new CFG(md, paramAsStatement).get());
    }

    /**
     * Constructs a program dependence graph
     *
     * @param md method whose PDG is to be built
     * @throws Exception if PDG construction fails
     */
    public PDG(final Method md) throws Exception {
        this(new CFG(md, false).get());
    }

    // Getters

    /**
     * Returns the method whose PDG is built
     *
     * @return the method whose PDG is built
     */
    public Method getMethod() {
        return md;
    }

    /**
     * Returns the entry node to the graph
     *
     * @return the entry node to the graph
     */
    public PDGNode getEntryNode() {
        return entryNode;
    }

    /**
     * Returns the set of all the nodes in the graph
     *
     * @return the set of all the nodes in the graph
     */
    public HashSet<PDGNode> getNodes() {
        return nodes;
    }

	public PDGNode[] sortNodes() {
		try {
			final PDGNode[] results = new PDGNode[nodes.size()];
			for (final PDGNode node : nodes) {
				results[node.getId()] = node;
			}
			return results;
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return null;
	}

    /**
     * Returns the PDG node for the given node id. If not found then returns null
     *
     * @param id node id
     * @return
     */
    public PDGNode getNode(final int id) {
        for (final PDGNode n : nodes)
            if (n.getId() == id)
                return n;

        return null;
    }

    /**
     * normalize the expression for each PDGNode
     */
    public void normalize() throws Exception {
        if (!normalize) {
            normalize = true;
            if (entryNode == null)
                return;
            final Stack<PDGNode> nodes = new Stack<PDGNode>();
            nodes.add(entryNode);
            final Set<PDGNode> visited = new HashSet<PDGNode>();
            final Map<String, String> normalizedVars = new HashMap<String, String>();
            int varCount = 1;
            // if normalization is enabled then normalize node expression
            // also update use and def variables for each node
            try {
                while (nodes.size() != 0) {
                    final PDGNode node = nodes.pop();
                    // store normalized name mappings of def and use variables at this node
                    // replace use and def variables in the node with their normalized names
                    // def variable
                    if (node.getDefVariable() != null && !node.getDefVariable().equals("")) {
                        if (!normalizedVars.containsKey(node.getDefVariable())) {
                            normalizedVars.put(node.getDefVariable(), "var$" + varCount);
                            varCount++;
                        }
                        node.setDefVariable(normalizedVars.get(node.getDefVariable()));
                    }
                    // use variables
                    final HashSet<String> useVars = new HashSet<String>();
                    for (final String dVar : node.getUseVariables()) {
                        if (dVar != null) {
                            if (!normalizedVars.containsKey(dVar)) {
                                normalizedVars.put(dVar, "var$" + varCount);
                                varCount++;
                            }
                            useVars.add(normalizedVars.get(dVar));
                        }
                    }
                    node.setUseVariables(useVars);
                    if (node.hasStmt())
                        node.setStmt(normalizeStatement(node.getStmt(), normalizedVars));
                    if (node.hasExpr())
                        node.setExpr(normalizeExpression(node.getExpr(), normalizedVars));

                    for (final PDGEdge e : node.getOutEdges()) {
                        final String label = normalizedVars.get(e.getLabel());
                        if (label != null)
                            e.setLabel(label);
                    }

                    visited.add(node);
                    // if successor has not been visited, add it
                    Collections.sort(node.getSuccessors());
                    for (final PDGNode succ : node.getSuccessors())
                        if (!visited.contains(succ) && !nodes.contains(succ))
                            nodes.push(succ);
                }
            } catch (Exception e) {
                System.out.println(prettyprint(md));
                throw e;
            }
        }
    }

    /**
     * Adds CDG nodes and edges to program dependence graph
     *
     * @param cdg control dependence graph
     */
    private void addCDG(final CDG cdg) {
        for (final CDGNode n : cdg.getNodes()) {
            nodes.add(new PDGNode(n));
        }

        for (final CDGNode n : cdg.getNodes()) {
            final PDGNode node = getNode(n.getId());
            for (final CDGEdge ie : n.getInEdges())
                node.addInEdge(new PDGEdge(getNode(ie.getSrc().getId()), getNode(ie.getDest().getId()), ie.getLabel(), Control.Edge.EdgeType.CONTROL));
            for (final CDGEdge oe : n.getOutEdges())
                node.addOutEdge(new PDGEdge(getNode(oe.getSrc().getId()), getNode(oe.getDest().getId()), oe.getLabel(), Control.Edge.EdgeType.CONTROL));
        }
    }

    /**
     * Adds DDG edges to the PDG graph
     *
     * @param ddg data dependency graph
     */
    private void addDDGEdges(final DDG ddg) {
        // all the nodes and control edges have already added. Only adds data edges
        try {
            for (final Map.Entry<DDGNode, Set<DDGNode>> entry : ddg.getDefUseChain().entrySet()) {
                final PDGNode src = getNode(entry.getKey().getId());
                for (final DDGNode d : entry.getValue()) {
                    final PDGNode dest = getNode(d.getId());
                    new PDGEdge(src, dest, entry.getKey().getDefVariable(), Control.Edge.EdgeType.DATA);
                }
            }
        } catch (final Exception e) {
            System.out.println(BoaAstIntrinsics.prettyprint(md));
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof PDG)) return false;
        final PDG pdgSlicer = (PDG) o;

        final Stack<PDGNode> nodes1 = new Stack<PDGNode>();
        final Stack<PDGNode> nodes2 = new Stack<PDGNode>();
        final Set<PDGNode> visited1 = new HashSet<PDGNode>();
        final Set<PDGNode> visited2 = new HashSet<PDGNode>();
        nodes1.add(entryNode);
        nodes2.add(pdgSlicer.getEntryNode());

        while (nodes1.size() != 0) {
            if (nodes1.size() != nodes2.size())
                return false;
            final PDGNode node1 = nodes1.pop();
            final PDGNode node2 = nodes2.pop();
            // compare statements
            if ((node1.getStmt() == null  && node2.getStmt() != null) ||
                    (node1.getStmt() != null  && node2.getStmt() == null))
                return false;
            if (node1.getStmt() != null && node2.getStmt() != null &&
                    !node1.getStmt().equals(node2.getStmt())) // use string comparisons?? prettyprint
                return false;

            // compare expressions
            if ((node1.getExpr() == null  && node2.getExpr() != null) ||
                    (node1.getExpr() != null  && node2.getExpr() == null))
                return false;
            if (node1.getExpr() != null && node2.getExpr() != null &&
                    !node1.getExpr().equals(node2.getExpr())) // use string comparisons?? prettyprint
                return false;

            // compare out edges
            if (node1.getOutEdges().size() != node2.getOutEdges().size())
                return false;

            visited1.add(node1);
            visited2.add(node2);

            for (int i = 0; i < node1.getSuccessors().size(); i++) {
                final List<PDGEdge> outEdges1 = node1.getOutEdges(node1.getSuccessors().get(i));
                final List<PDGEdge> outEdges2 = node2.getOutEdges(node2.getSuccessors().get(i));
                if (outEdges1.size() != outEdges2.size())
                    return false;
                for (int j = 0; j < outEdges1.size(); j++) {
                    if (outEdges1.get(j).getKind() != outEdges2.get(j).getKind() ||
                            !outEdges1.get(j).getLabel().equals(outEdges2.get(j).getLabel()))
                        return false;
                }

                if (!visited1.contains(node1.getSuccessors().get(i)))
                    nodes1.push(node1.getSuccessors().get(i));
                if (!visited2.contains(node2.getSuccessors().get(i)))
                    nodes2.push(node2.getSuccessors().get(i));
            }

        }

        return true;
    }

    @Override
    public int hashCode() {
        /*
         * Some strings may have zero hashcode due to integer overflow.
         * For those strings hashcode will be recalculated everytime hashcode is
         * requested but the general contract for the hashCode() method will still hold.
         */
        if (hashcode == 0) {
            final Stack<PDGNode> nodes = new Stack<PDGNode>();
            nodes.add(entryNode);
            final Set<PDGNode> visited = new HashSet<PDGNode>();
            final StringBuilder sb = new StringBuilder();
            while (nodes.size() != 0) {
                final PDGNode node = nodes.pop();
                visited.add(node);
                if (node.hasExpr())
                    sb.append(node.getExpr());
                if (node.hasStmt())
                    sb.append(node.getStmt());
                Collections.sort(node.getSuccessors());
                for (final PDGNode succ : node.getSuccessors())
                    if (!visited.contains(succ) && !nodes.contains(succ))
                        nodes.push(succ);
            }
            // compute and cache hash
            hashcode = sb.toString().hashCode();
        }
        return hashcode;
    }
}
