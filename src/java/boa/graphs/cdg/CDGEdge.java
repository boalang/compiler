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

import boa.types.Control;

/**
 * Control Dependence Graph edge
 *
 * @author marafat
 */

public class CDGEdge {

    private CDGNode src;
    private CDGNode dest;
    private String label = ".";

    // Constructors

    /**
     * Constructs a CDG edge
     *
     * @param src starting node of the edge
     * @param dest destination node of the edge
     * @param label label of the edge
     */
    public CDGEdge(final CDGNode src, final CDGNode dest, final String label) {
        this.src = src;
        this.dest = dest;
        this.label = label;
    }

    /**
     * Constructs a CDG edge. Uses default label "."
     *
     * @param src starting node of the edge
     * @param dest destination node of the edge
     */
    public CDGEdge(final CDGNode src, final CDGNode dest) {
        this.src = src;
        this.dest = dest;
    }

    // Setters
    public void setSrc(final CDGNode src) {
        this.src = src;
    }

    public void setDest(final CDGNode dest) {
        this.dest = dest;
    }

    public void setLabel(final String label) {
        this.label = label;
    }

    // Getters
    public CDGNode getSrc() {
        return src;
    }

    public CDGNode getDest() {
        return dest;
    }

    public String getLabel() {
        return label;
    }

    /**
     * CDG Edge builder
     *
     * @return a CDG edge builder
     */
    public boa.types.Control.CDGEdge.Builder newBuilder() {
        final boa.types.Control.CDGEdge.Builder eb = boa.types.Control.CDGEdge.newBuilder();
        eb.setLabel(CDGEdge.getLabel(this.label));
        return eb;
    }

    /**
     * Returns the label type
     *
     * @param label edge label
     * @return the label type
     */
    public static Control.CDGEdge.CDGEdgeLabel getLabel(final String label) {
        if (label.equals("T"))
            return Control.CDGEdge.CDGEdgeLabel.TRUE;
        else if (label.equals("F"))
            return Control.CDGEdge.CDGEdgeLabel.FALSE;
        else
            return Control.CDGEdge.CDGEdgeLabel.NIL;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof CDGEdge)) return false;

        CDGEdge cdgEdge = (CDGEdge) o;

        return src.equals(cdgEdge.src) && dest.equals(cdgEdge.dest) && label.equals(cdgEdge.label);
    }

    @Override
    public int hashCode() {
        int result = src.hashCode();
        result = 31 * result + dest.hashCode();
        result = 31 * result + label.hashCode();
        return result;
    }
}
