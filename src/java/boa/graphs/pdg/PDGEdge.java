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

import boa.types.Control;
import boa.types.Control.PDGEdge.*;

/**
 * Program Dependence Graph builder edge
 *
 * @author marafat
 */

public class PDGEdge {

    private PDGNode src;
    private PDGNode dest;
    private String label; // name of the variable for data edge, T or F for Control Edge
    private PDGEdgeType kind;

    public PDGEdge(PDGNode src, PDGNode dest, String label, PDGEdgeType kind) {
        this.src = src;
        this.dest = dest;
        this.label = label;
        this.kind = kind;
    }

    // Setters
    public void setSrc(final PDGNode src) {
        this.src = src;
    }

    public void setDest(final PDGNode dest) {
        this.dest = dest;
    }

    public void setLabel(final String label) {
        this.label = label;
    }

    public void setKind(final PDGEdgeType kind) {
        this.kind = kind;
    }

    // Getters
    public PDGNode getSrc() {
        return src;
    }

    public PDGNode getDest() {
        return dest;
    }

    public String getLabel() {
        return label;
    }

    public PDGEdgeType getKind() {
        return kind;
    }

    /**
     * PDG Edge builder
     *
     * @return
     */
    public boa.types.Control.PDGEdge.Builder newBuilder() {
        final boa.types.Control.PDGEdge.Builder eb = boa.types.Control.PDGEdge.newBuilder();
        eb.setLabel(PDGEdge.getLabel(this.kind, this.label));
        return eb;
    }

    /**
     * Gives back label type
     *
     * @param label edge label
     * @return label type
     */
    public static Control.PDGEdge.PDGEdgeLabel getLabel(final PDGEdgeType type, final String label) {
        if (type == PDGEdgeType.CONTROL) {
            if (label.equals("T"))
                return Control.PDGEdge.PDGEdgeLabel.TRUE;
            else if (label.equals("F"))
                return Control.PDGEdge.PDGEdgeLabel.FALSE;
            else
                return Control.PDGEdge.PDGEdgeLabel.NIL;
        } else if (type == PDGEdgeType.DATA) {
            if (label != null)
                return PDGEdgeLabel.VARDEF;
            else
                return PDGEdgeLabel.NIL;
        } else
            return PDGEdgeLabel.NIL;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PDGEdge pdgEdge = (PDGEdge) o;

        if (!src.equals(pdgEdge.src)) return false;
        if (!dest.equals(pdgEdge.dest)) return false;
        if (!label.equals(pdgEdge.label)) return false;
        return kind == pdgEdge.kind;
    }

    @Override
    public int hashCode() {
        int result = src.hashCode();
        result = 31 * result + dest.hashCode();
        result = 31 * result + label.hashCode();
        result = 31 * result + kind.hashCode();
        return result;
    }
}
