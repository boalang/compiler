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
package boa.graphs.cdg;

import boa.graphs.Edge;

/**
 * Control Dependence Graph edge
 *
 * @author marafat
 * @author rdyer
 */
public class CDGEdge extends Edge<CDGNode, CDGEdge> {
    /**
     * Constructs a CDG edge
     *
     * @param src starting node of the edge
     * @param dest destination node of the edge
     * @param label label of the edge
     */
    public CDGEdge(final CDGNode src, final CDGNode dest, final String label) {
        super(src, dest, label);
    }

    /**
     * Returns the label type
     *
     * @param label edge label
     * @return the label type
     */
    public static boa.types.Control.Edge.EdgeLabel convertLabel(final String label) {
        if (label.equals("T"))
            return boa.types.Control.Edge.EdgeLabel.TRUE;
        if (label.equals("F"))
            return boa.types.Control.Edge.EdgeLabel.FALSE;
        return boa.types.Control.Edge.EdgeLabel.NIL;
    }
}
