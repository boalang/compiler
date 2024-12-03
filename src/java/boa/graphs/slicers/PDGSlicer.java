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
package boa.graphs.slicers;

import static boa.functions.BoaAstIntrinsics.prettyprint;
import static boa.functions.BoaNormalFormIntrinsics.normalizeExpression;
import static boa.functions.BoaNormalFormIntrinsics.normalizeStatement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import boa.graphs.pdg.PDG;
import boa.graphs.pdg.PDGEdge;
import boa.graphs.pdg.PDGNode;
import boa.types.Ast.Method;
import boa.types.Control;

/**
 * A forward slicer based on PDG
 *
 * @author marafat
 * @author rdyer
 */
public class PDGSlicer {
    private final Method md;
    private final ArrayList<PDGNode> entryNodes = new ArrayList<PDGNode>();
    private final Set<PDGNode> slice = new LinkedHashSet<PDGNode>();
    private boolean normalize = false;
    private int hashcode = 0;

    /**
     * Constructs a PDGSlicer instance for the given PDGNode
     *
     * @param md method
     * @param node PDGNode
     * @param normalize if true expression of each node is normalized
     */
    public PDGSlicer(final Method md, final PDGNode node, final boolean normalize) throws Exception {
        this.md = md;
        this.normalize = normalize;
        if (node != null) {
            entryNodes.add(node);
            traverse();
        }
    }

    /**
     * Constructs a PDGSlicer instance for the given array of PDGNodes
     *
     * @param md method
     * @param nodes array of PDGNodes
     * @param normalize if true expression of each node is normalized
     */
    public PDGSlicer(final Method md, final PDGNode[] nodes, final boolean normalize) throws Exception {
        this.md = md;
        this.normalize = normalize;
        entryNodes.addAll(Arrays.asList(nodes));
        traverse();
    }

    /**
     * Constructs a PDGSlicer instance for the given PDGNode id
     *
     * @param md method
     * @param nid node id of the PDGNode
     * @param normalize if true expression of each node is normalized
     */
    public PDGSlicer(final Method md, final int nid, final boolean normalize) throws Exception {
        this.md = md;
        this.normalize = normalize;
        final PDG pdg = new PDG(md, false);
        final PDGNode node = pdg.getNode(nid);
        if (node != null) {
            entryNodes.add(node);
            traverse();
        }
    }

    /**
     * Constructs a PDGSlicer instance for the given array of PDGNode ids
     *
     * @param md method
     * @param nids array of PDGNode ids
     * @param normalize if true expression of each node is normalized
     */
    public PDGSlicer(final Method md, final Integer[] nids, final boolean normalize) throws Exception {
        this.md = md;
        this.normalize = normalize;
        final PDG pdg = new PDG(md, false);
        for (final Integer i: nids) {
            final PDGNode node = pdg.getNode(i);
            if (node != null)
                entryNodes.add(node);
        }
        if (entryNodes.size() > 0) {
            traverse();
        }
    }

    /**
     * Constructs a PDGSlicer instance for the given PDG graph and PDGNode id
     *
     * @param pdg PDG graph
     * @param nid PDGNode id
     * @param normalize if true expression of each node is normalized
     */
    public PDGSlicer(final PDG pdg, final int nid, final boolean normalize) throws Exception {
        this.md = pdg.getMethod();
        this.normalize = normalize;
        final PDGNode node = pdg.getNode(nid);
        if (node != null) {
            entryNodes.add(node);
            traverse();
        }
    }

    /**
     * Constructs a PDGSlicer instance for the given PDG graph and array of PDGNode ids
     *
     * @param pdg PDG graph
     * @param nids array of PDGNode ids
     * @param normalize if true expression of each node is normalized
     */
    public PDGSlicer(final PDG pdg, final Integer[] nids, final boolean normalize) throws Exception {
        this.md = pdg.getMethod();
        this.normalize = normalize;
        for (final Integer i: nids) {
            final PDGNode node = pdg.getNode(i);
            if (node != null)
                entryNodes.add(node);
        }
        if (entryNodes.size() > 0) {
            traverse();
        }
    }

    /**
     * Returns the method whose PDG is built
     *
     * @return the method whose PDG is built
     */
    public Method getMethod() {
        return md;
    }

    /**
     * Returns the list of starting nodes of the slice
     *
     * @return the list of starting nodes of the slice
     */
    public ArrayList<PDGNode> getEntrynodesList() {
        return entryNodes;
    }

    /**
     * Returns all the nodes in the slice
     *
     * @return all the nodes in the slice
     */
    public Set<PDGNode> getSlice() {
        return slice;
    }

    /**
     * Returns the total nodes in the slice
     *
     * @return the total nodes in the slice
     */
    public int getTotalNodes() {
        return slice.size();
    }

    /**
     * Returns the total control nodes in the slice
     *
     * @return the total control nodes in the slice
     */
    public int getTotalControlNodes() {
        int totalControlNodes = 0;
        for (final PDGNode node: slice)
            if (node.getKind() == Control.Node.NodeType.CONTROL)
                totalControlNodes = totalControlNodes + 1;
        return totalControlNodes;
    }

    /**
     * Returns the total edges in the slice
     *
     * @return the total edges in the slice
     */
    public int getTotalEdges() {
        int totalEdges = 0;
        for (final PDGNode node: slice)
            totalEdges = totalEdges + node.getOutEdges().size();
        return totalEdges;
    }

    /**
     * Returns the set of slice nodes sorted by node ids
     *
     * @return the set of slice nodes sorted by node ids
     */
    public List<PDGNode> getSortedSlice() {
        final List<PDGNode> sorted = new ArrayList<PDGNode>(slice);
        Collections.sort(sorted);
        return sorted;
    }

    /**
     * Traverse the slice to collect sliced nodes. Normalizes expression of each node if normalize is
     * true. Computes hash of the slice and caches it
     */
    private void traverse() throws Exception {
        final Stack<PDGNode> nodes = new Stack<PDGNode>();
        nodes.addAll(entryNodes);
        final Map<String, String> normalizedVars = new HashMap<String, String>();
        final StringBuilder sb = new StringBuilder(); // for hashcode caching
        int varCount = 1;
        // traverse and collect sliced nodes
        // if normalization is enabled then normalize node expression
        // also update use and def variables for each node
        try {
            while (nodes.size() != 0) {
                final PDGNode node = nodes.pop();
                // store normalized name mappings of def and use variables at this node
                // replace use and def variables in the node with their normalized names
                if (!slice.contains(node)) {
                    if (normalize) {
                        // def variable
                        if (node.getDefVariable() != null && !node.getDefVariable().equals("")) {
                            if (!normalizedVars.containsKey(node.getDefVariable())) {
                                normalizedVars.put(node.getDefVariable(), "var$" + varCount);
                                varCount++;
                            }
                            node.setDefVariable(normalizedVars.get(node.getDefVariable()));
                        }
                        // use variables
                        final Set<String> useVars = new LinkedHashSet<String>();
                        for (final String dVar : node.getUseVariables()) {
                            if (dVar != null) {
                                if (!normalizedVars.containsKey(dVar)) {
                                    normalizedVars.put(dVar, "var$" + varCount); // FIXME: use string builder
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
                    }

                    slice.add(node);
                    // for hashcode caching
                    if (node.hasExpr())
                        sb.append(node.getExpr());
                    if (node.hasStmt())
                        sb.append(node.getStmt());
                    // if successor has not been visited, add it
                    Collections.sort(node.getSuccessors());
                    for (final PDGNode succ : node.getSuccessors())
                            nodes.push(succ);
                }
            }
        } catch (final Exception e) {
            System.out.println(prettyprint(md));
            throw e;
        }

        // compute and cache hash
        hashcode = sb.toString().hashCode();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof PDGSlicer)) return false;
        final PDGSlicer pdgSlicer = (PDGSlicer) o;

        final Stack<PDGNode> nodes1 = new Stack<PDGNode>();
        final Stack<PDGNode> nodes2 = new Stack<PDGNode>();
        final Set<PDGNode> visited1 = new LinkedHashSet<PDGNode>();
        final Set<PDGNode> visited2 = new LinkedHashSet<PDGNode>();
        nodes1.addAll(entryNodes);
        nodes2.addAll(pdgSlicer.getEntrynodesList());

        while (nodes1.size() != 0) {
            if (nodes1.size() != nodes2.size())
                return false;
            final PDGNode node1 = nodes1.pop();
            final PDGNode node2 = nodes2.pop();
            // compare statements
            if ((!node1.hasStmt() && node2.hasStmt())
                    || (node1.hasStmt() && !node2.hasStmt()))
                return false;
            if (node1.hasStmt() && node2.hasStmt()
                    && !node1.getStmt().equals(node2.getStmt()))
                return false;

            // compare expressions
            if ((!node1.hasExpr() && node2.hasStmt())
                    || (node1.hasExpr() && !node2.hasExpr()))
                return false;
            if (node1.hasExpr() && node2.hasExpr()
                    && !node1.getExpr().equals(node2.getExpr()))
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
                    if (outEdges1.get(j).getKind() != outEdges2.get(j).getKind()
                            || !outEdges1.get(j).getLabel().equals(outEdges2.get(j).getLabel()))
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
        return hashcode;
    }
}
