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

import boa.graphs.pdg.PDG;
import boa.graphs.pdg.PDGEdge;
import boa.graphs.pdg.PDGNode;
import boa.types.Ast.*;
import boa.types.Control;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import static boa.functions.BoaAstIntrinsics.prettyprint;
import static boa.functions.BoaNormalFormIntrinsics.normalizeExpression;

/**
 * A forward slicer based on PDG
 *
 * @author marafat
 */

public class PDGSlicer {

    private Method md;
    private ArrayList<PDGNode> entrynodes = new ArrayList<PDGNode>();
    private HashSet<PDGNode> slice = new HashSet<PDGNode>();
    private boolean normalize = false;
    private int hashcode = -1;

    // Constructors
    public PDGSlicer(Method method, PDGNode n, boolean normalize) throws Exception {
        this.md = method;
        this.normalize = normalize;
        if (n != null) {
            entrynodes.add(n);
            getSlice(new PDG(method, true));
        }
    }

    public PDGSlicer(Method method, PDGNode[] n, boolean normalize) throws Exception {
        this.md = method;
        this.normalize = normalize;
        entrynodes.addAll(Arrays.asList(n));
        getSlice(new PDG(method, true));
    }

    public PDGSlicer(Method method, int nid, boolean normalize) throws Exception {
        this.md = method;
        this.normalize = normalize;
        PDG pdg = new PDG(method, true);
        PDGNode node = pdg.getNode(nid);
        if (node != null) {
            entrynodes.add(node);
            getSlice(pdg);
        }
    }

    public PDGSlicer(Method method, Integer[] nids, boolean normalize) throws Exception {
        this.md = method;
        this.normalize = normalize;
        PDG pdg = new PDG(method, true);
        for (Integer i: nids) {
            PDGNode node = pdg.getNode(i);
            if (node != null)
                entrynodes.add(node);
        }
        if (entrynodes.size() > 0) {
            getSlice(pdg);
        }
    }

    public PDGSlicer(PDG pdg, int nid, boolean normalize) throws Exception {
        this.md = pdg.getMethod();
        this.normalize = normalize;
        PDGNode node = pdg.getNode(nid);
        if (node != null) {
            entrynodes.add(node);
            getSlice(pdg);
        }
    }

    public PDGSlicer(PDG pdg, Integer[] nids, boolean normalize) throws Exception {
        this.md = pdg.getMethod();
        this.normalize = normalize;
        for (Integer i: nids) {
            PDGNode node = pdg.getNode(i);
            if (node != null)
                entrynodes.add(node);
        }
        if (entrynodes.size() > 0) {
            getSlice(pdg);
        }
    }

    // Getters
    public ArrayList<PDGNode> getEntrynodesList() {
        return entrynodes;
    }

    public HashSet<PDGNode> getSlice() {
        return slice;
    }

    /**
     * Returns cryptographic hash of the slice
     *
     * @param algorithm name of the cryptographic hashing algorithm
     * @return cryptographic hash of the slice
     */
    public String getCryptHash(String algorithm) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        Collections.sort(entrynodes, new PDGNodeComparator());

        Stack<PDGNode> nodes = new Stack<PDGNode>();
        nodes.addAll(entrynodes);
        Set<PDGNode> visited = new LinkedHashSet<PDGNode>();
        StringBuilder sb = new StringBuilder();

        while (nodes.size() != 0) {
            PDGNode node = nodes.pop();
            if (node.getStmt() != null)
                sb.append(prettyprint(node.getStmt()));
            if (node.getExpr() != null)
                sb.append(prettyprint(node.getExpr()));
            visited.add(node);

            for (PDGNode succ : node.getSuccessors()) {
                List<PDGEdge> edges = node.getOutEdges(succ);
                for (PDGEdge e: edges)
                    sb.append(e.getKind()).append(e.getLabel());

                if (!visited.contains(succ))
                    nodes.push(succ);
            }

        }

        MessageDigest md = MessageDigest.getInstance(algorithm);
        byte[] hcBytes = md.digest(sb.toString().getBytes("UTF-8"));

        StringBuilder sBDigest = new StringBuilder();
        for (byte b: hcBytes)
            sBDigest.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));

        return sBDigest.toString();
    }

    /**
     * Traverse the pdg to collect sliced nodes.
     *
     * @param pdg program dependence graph
     */
    private void getSlice(PDG pdg) throws Exception {
        Stack<PDGNode> nodes = new Stack<PDGNode>();
        nodes.addAll(entrynodes);
        Map<String, String> normalizedVars = new HashMap<String, String>();
        int varCount = 1;
        // traverse and collect sliced nodes
        // if normalization is enabled then normalize node expressioin
        // also update use and def variables for each node
        try {
            while (nodes.size() != 0) {
                PDGNode node = nodes.pop();
                // store normalized name mappings of def and use variables at this node
                // replace use and def variables in the node with their normalized names
                if (normalize) {
                    // def variable
                    if (!node.getDefVariable().equals("")) {
                        if (!normalizedVars.containsKey(node.getDefVariable())) {
                            normalizedVars.put(node.getDefVariable(), "var$" + varCount);
                            varCount++;
                        }
                        node.setDefVariable(normalizedVars.get(node.getDefVariable())); // FIXME: create a clone of the node and then set
                    }
                    // use variables
                    HashSet<String> useVars = new HashSet<String>();
                    for (String dVar : node.getUseVariables()) {
                        if (dVar != null) {
                            if (!normalizedVars.containsKey(dVar)) {
                                normalizedVars.put(dVar, "var$" + varCount); // FIXME: use string builder
                                varCount++;
                            }
                            useVars.add(normalizedVars.get(dVar));
                        }
                    }
                    node.setUseVariables(useVars);
                    if (node.hasExpr())
                        node.setExpr(normalizeExpression(node.getExpr(), normalizedVars)); // FIXME: create a clone of the node and then set

                    for (PDGEdge e: node.getOutEdges()) {
                        String label = normalizedVars.get(e.getLabel());
                        if (label != null)
                            e.setLabel(label);
                    }
                }

                slice.add(node);
                // if successor has not been visited, add it
                for (PDGNode succ : node.getSuccessors())
                    if (!slice.contains(succ))
                        nodes.push(succ);
            }
        } catch (Exception e) {
            System.out.println(prettyprint(md));
            throw e;
        }

        // compute and cache hash
        calculateHash();

    }

    /**
     * Computes hash code of the slice for caching purpose
     */
    private void calculateHash() {
        Collections.sort(entrynodes, new PDGNodeComparator());

        Stack<PDGNode> nodes = new Stack<PDGNode>();
        nodes.addAll(entrynodes);
        Set<PDGNode> visited = new HashSet<PDGNode>();
        StringBuilder sb = new StringBuilder();

        while (nodes.size() != 0) {
            PDGNode node = nodes.pop();
            sb.append(prettyprint(node.getExpr()));

            visited.add(node);
            Collections.sort(node.getSuccessors(), new PDGNodeComparator());

            for (PDGNode succ : node.getSuccessors())
                if (!visited.contains(succ))
                    nodes.push(succ);

        }

        hashcode = sb.toString().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PDGSlicer)) return false;
        PDGSlicer pdgSlicer = (PDGSlicer) o;

        Stack<PDGNode> nodes1 = new Stack<PDGNode>();
        Stack<PDGNode> nodes2 = new Stack<PDGNode>();
        Set<PDGNode> visited1 = new HashSet<PDGNode>();
        Set<PDGNode> visited2 = new HashSet<PDGNode>();
        nodes1.addAll(entrynodes);
        nodes2.addAll(pdgSlicer.getEntrynodesList());

        while (nodes1.size() != 0) {
            if (nodes1.size() != nodes2.size())
                return false;
            PDGNode node1 = nodes1.pop();
            PDGNode node2 = nodes2.pop();
            if (!node1.getExpr().equals(node2.getExpr())) // use string comparisons??
                return false;
            if (node1.getOutEdges().size() != node2.getOutEdges().size())
                return false;

            visited1.add(node1);
            visited2.add(node2);

            for (int i = 0; i < node1.getSuccessors().size(); i++) {
                List<PDGEdge> outEdges1 = node1.getOutEdges(node1.getSuccessors().get(i));
                List<PDGEdge> outEdges2 = node2.getOutEdges(node2.getSuccessors().get(i));
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
        return hashcode;
    }

    /**
     * Comparator for node expressions
     */
    public static class PDGNodeComparator implements Comparator<PDGNode> {
        public int compare(final PDGNode n1, final PDGNode n2) {
            if (n1.hasExpr() && n2.hasExpr())
                return prettyprint(n1.getExpr()).compareTo(prettyprint(n2.getExpr()));
            if (n1.hasStmt() && n2.hasStmt())
                return prettyprint(n1.getStmt()).compareTo(prettyprint(n2.getStmt()));
            if (n1.hasExpr() || n2.hasExpr()) {
                if (n1.hasExpr())
                    return 0;
                if (n2.hasExpr())
                    return -1;
            }
            if (n1.hasStmt() || n2.hasStmt()) {
                if (n1.hasStmt())
                    return 0;
                if (n2.hasStmt())
                    return -1;
            }

            return 1;
        }
    }
}
