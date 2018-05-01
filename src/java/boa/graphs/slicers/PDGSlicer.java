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
import boa.graphs.pdg.PDGNode;
import boa.types.Ast.*;

import java.util.*;

/**
 * A forward slicer based on PDG
 *
 * @author marafat
 */

public class PDGSlicer {

    public ArrayList<PDGNode> entrynodes = new ArrayList<PDGNode>();
    private HashSet<PDGNode> slice = new HashSet<PDGNode>();
    private boolean normalize = false;

    public PDGSlicer(Method method, PDGNode n) throws Exception {
        if (n != null) {
            entrynodes.add(n);
            getSlice(new PDG(method, true));
        }
    }

    public PDGSlicer(Method method, PDGNode[] n) throws Exception {
        entrynodes.addAll(Arrays.asList(n));
        getSlice(new PDG(method, true));
    }

    public PDGSlicer(Method method, int nid, boolean normalize) throws Exception {
        this.normalize = normalize;
        PDG pdg = new PDG(method, true);
        PDGNode node = pdg.getNode(nid);
        if (node != null) {
            entrynodes.add(node);
            getSlice(pdg);
        }
    }

    public PDGSlicer(Method method, int nid) throws Exception {
        this(method, nid, true);
    }

    public PDGSlicer(Method method, Integer[] nids, boolean normalize) throws Exception {
        this.normalize = normalize;
        PDG pdg = new PDG(method, true);
        for (Integer i: nids) {
            PDGNode node = pdg.getNode(i);
            if (node != null)
                entrynodes.add(node);
        }
        if (entrynodes.size() > 0)
            getSlice(pdg);
    }

    public PDGSlicer(Method method, Integer[] nids) throws Exception {
        this(method, nids, true);
    }

    // Getters
    public ArrayList<PDGNode> getEntrynodesList() {
        return entrynodes;
    }

    public HashSet<PDGNode> getSlice() {
        return slice;
    }

    public void sliceHash() {}

    /**
     * Traverse the pdg to collect the nodes
     *
     * @param pdg program dependence graph
     */
    private void getSlice(PDG pdg) {
        Stack<PDGNode> nodes = new Stack<PDGNode>();
        Map<String, String> normalizedVars = new HashMap<String, String>();
        nodes.addAll(entrynodes);
        int varCount = 1;
        while (nodes.size() != 0) {
            PDGNode node = nodes.pop();
            // store normalized name mappings of def and use variables at this node
            // replace use and def variables with their normalized names
            if (normalize) {
                // def variables
                if (node.getDefVariable() != null) {
                    if (!normalizedVars.containsKey(node.getDefVariable())) {
                        normalizedVars.put(node.getDefVariable(), "var$" + varCount);
                        varCount++;
                    }
                    node.setDefVariable(normalizedVars.get(node.getDefVariable())); //FIXME: create a clone of the node and then set
                }
                // use variables
                HashSet<String> useVars = new HashSet<String>();
                for (String dVar : node.getUseVariables()) {
                    if (dVar != null) {
                        if (!normalizedVars.containsKey(dVar)) {
                            normalizedVars.put(dVar, "var$" + varCount); //FIXME: use string builder
                            varCount++;
                        }
                        useVars.add(normalizedVars.get(dVar));
                    }
                }
                node.setUseVariables(useVars);
                Expression exp = normalizeExpression(node.getExpr(), normalizedVars);
                node.setExpr(exp); //FIXME: create a clone of the node and then set
            }

            slice.add(node);
            for (PDGNode succ: node.getSuccessors())
                if (!slice.contains(succ))
                    nodes.push(succ);
        }
    }

    /**
     * Normalize the name in the given expression
     *
     * @param exp expression to be normalized
     * @param normalizedVars mapping of original names with normalized names in the expression
     * @return
     */
    private Expression normalizeExpression(final Expression exp, final Map<String, String> normalizedVars) {
        final List<Expression> convertedExpression = new ArrayList<Expression>();
        for (final Expression sub : exp.getExpressionsList())
            convertedExpression.add(normalizeExpression(sub, normalizedVars));

        switch (exp.getKind()) {
            case VARACCESS:
                if (normalizedVars.containsKey(exp.getVariable()))
                    return createVariable(normalizedVars.get(exp.getVariable()));
                else
                    return exp;

            case METHODCALL:
                final Expression.Builder bm = Expression.newBuilder(exp);

                for(int i = 0; i < convertedExpression.size(); i++) {
                    bm.setExpressions(i, convertedExpression.get(i));
                }

                for(int i = 0; i < exp.getMethodArgsList().size(); i++) {
                    Expression mArgs = normalizeExpression(exp.getMethodArgs(i), normalizedVars);
                    bm.setMethodArgs(i, mArgs);
                }

                return bm.build();

            case EQ:
            case NEQ:
            case GT:
            case LT:
            case GTEQ:
            case LTEQ:
            case LOGICAL_AND:
            case LOGICAL_OR:
            case LOGICAL_NOT:
            case PAREN:
            case NEW:
            case ASSIGN:
            case OP_ADD:
            case OP_SUB:
            case OP_MULT:
            case OP_DIV:
            case OP_MOD:
            case OP_DEC:
            case OP_INC:
            case ARRAYINDEX:
                return createExpression(exp.getKind(), convertedExpression.toArray(new Expression[convertedExpression.size()]));

            //TODO: Handle if needed
            case NEWARRAY:
            case ARRAYINIT:
            case ASSIGN_ADD:
            case ASSIGN_BITAND:
            case ASSIGN_BITOR:
            case ASSIGN_BITXOR:
            case ASSIGN_DIV:
            case ASSIGN_LSHIFT:
            case ASSIGN_MOD:
            case ASSIGN_MULT:
            case ASSIGN_RSHIFT:
            case ASSIGN_SUB:
            case ASSIGN_UNSIGNEDRSHIFT:
            case BIT_AND:
            case BIT_LSHIFT:
            case BIT_NOT:
            case BIT_OR:
            case BIT_RSHIFT:
            case BIT_UNSIGNEDRSHIFT:
            case BIT_XOR:
            case CAST:
            case CONDITIONAL:
            case NULLCOALESCE:

            case LITERAL:
            default:
                return exp;
        }
    }

    // Copied from BoaNormalFormintrincics. put all such methods in BoaNormalFormintrinsics in a class as static factories
    /**
     * Creates a new prefix/postfix/infix expression.
     *
     * @param kind the kind of the expression
     * @param exps the operands
     * @return the new expression
     */
    private static Expression createExpression(final Expression.ExpressionKind kind, final Expression... exps) {
        final Expression.Builder b = Expression.newBuilder();

        b.setKind(kind);
        for (final Expression e : exps)
            b.addExpressions(Expression.newBuilder(e).build());

        return b.build();
    }

    /**
     * Creates a new variable access expression.
     *
     * @param var the variable name
     * @return a new variable access expression
     */
    private static Expression createVariable(final String var) {
        final Expression.Builder exp = Expression.newBuilder();

        exp.setKind(Expression.ExpressionKind.VARACCESS);
        exp.setVariable(var);

        return exp.build();
    }
}
