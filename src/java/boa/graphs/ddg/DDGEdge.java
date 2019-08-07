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
package boa.graphs.ddg;

import boa.graphs.Edge;
import boa.types.Control;

/**
 * Data Dependence Graph edge
 *
 * @author marafat
 * @author rdyer
 */
public class DDGEdge extends Edge<DDGNode, DDGEdge> {
    /**
     * Constructs a DDG edge
     *
     * @param src starting node of the edge
     * @param dest destination node of the edge
     * @param label label of the edge
     */
    public DDGEdge(final DDGNode src, final DDGNode dest, final String label) {
        super(src, dest, label);
    }

    /**
     * Constructs a DDG edge. Uses default label
     *
     * @param src starting node of the edge
     * @param dest destination node of the edge
     */
    public DDGEdge(final DDGNode src, final DDGNode dest) {
        super(src, dest);
    }

    /**
     * Returns the label type
     *
     * @param label edge label
     * @return label type
     */
    public static Control.Edge.EdgeLabel getLabel(final String label) {
        if (!label.equals("."))
            return Control.Edge.EdgeLabel.VARDEF;
        return Control.Edge.EdgeLabel.NIL;
    }
}
