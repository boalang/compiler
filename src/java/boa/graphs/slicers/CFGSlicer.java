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

import boa.compiler.ast.statements.Statement;
import boa.graphs.cdg.CDG;
import boa.graphs.cdg.CDGNode;
import boa.graphs.cfg.CFG;
import boa.graphs.cfg.CFGNode;
import boa.runtime.BoaAbstractFixP;
import boa.runtime.BoaAbstractTraversal;
import boa.types.Ast;
import boa.types.Graph.Traversal.*;

import java.util.HashSet;
import java.util.Set;

/**
 * @author marafat
 */

public class CFGSlicer {

    public CFGSlicer() {}

//    public void getSlice(CFG cfg) throws Exception {
//        CDG cdg = new CDG(cfg);
//        BoaAbstractTraversal slice = new BoaAbstractTraversal<Set<Statement>>(true, true) {
//
//            protected Set<Statement> preTraverse(final CFGNode node) throws Exception {
//                String def = node.getDefVariables();
//                Set<String> use = node.getUseVariables();
//                Set<Integer> infl = new HashSet<Integer>();
//
//                if (node.getStmt() != null) {
//                    if (node.getStmt().getKind() == Ast.Statement.StatementKind.IF) {
//                        CDGNode cn = cdg.getNode(node.getId());
//                        for (CDGNode n: cn.getSuccessors())
//                            infl.add(n.getId());
//                    }
//                }
//
//            }
//
//            @Override
//            public void traverse(final CFGNode node, boolean flag) throws Exception {
//                if (flag) {
//                    currentResult = preTraverse(node);
//                    outputMapObj.put(node.getId(), currentResult);
//                } else
//                    outputMapObj.put(node.getId(), preTraverse(node));
//            }
//        };
//
//        BoaAbstractFixP fixp = new BoaAbstractFixP() {
//            boolean invoke1(final Set<Statement> current, final Set<Statement> previous) throws Exception {
//                Set<Statement> curr = new HashSet<Statement>(current);
//                curr.removeAll(previous);
//                return curr.size() == 0;
//            }
//
//            @Override
//            public boolean invoke(Object current, Object previous) throws Exception {
//                return invoke1((HashSet<Statement>) current, (HashSet<Statement>) previous);
//            }
//        };
//
//        slice.traverse(cfg, TraversalDirection.FORWARD, TraversalKind.REVERSEPOSTORDER, fixp);
//    }
}
