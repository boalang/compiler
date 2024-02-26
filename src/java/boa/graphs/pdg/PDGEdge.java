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

import boa.graphs.Edge;
import boa.types.Control.Edge.EdgeLabel;
import boa.types.Control.Edge.EdgeType;

/**
 * Program Dependence Graph edge
 *
 * @author marafat
 * @author rdyer
 */
public class PDGEdge extends Edge<PDGNode, PDGEdge> {
    private EdgeType kind;

    /**
     * Constructs a PDG edge
     *
     * @param src starting node of the edge
     * @param dest destination node of the edge
     * @param label label of the edge
     * @param kind kind of the edge i.e Control or Data
     */
    public PDGEdge(final PDGNode src, final PDGNode dest, final String label, final EdgeType kind) {
		this.src = src;
		this.dest = dest;
		this.label = label;
        this.kind = kind;

		this.src.addOutEdge(this);
		this.dest.addInEdge(this);
    }

    public void setKind(final EdgeType kind) {
        this.kind = kind;
    }

    public EdgeType getKind() {
        return kind;
    }

    /**
     * PDG Edge builder
     *
     * @return a DDG edge builder
     */
    public boa.types.Control.Edge.Builder newBuilder() {
        final boa.types.Control.Edge.Builder eb = super.newBuilder();
        eb.setLabel(PDGEdge.getLabel(this.kind, this.label));
        return eb;
    }

    /**
     * Returns the label type
     *
     * @param label edge label
     * @return label type
     */
    public static EdgeLabel getLabel(final EdgeType type, final String label) {
        if (type == EdgeType.CONTROL) {
            if (label.equals("T"))
                return EdgeLabel.TRUE;
            if (label.equals("F"))
                return EdgeLabel.FALSE;
        } else if (type == EdgeType.DATA) {
            if (label != null)
                return EdgeLabel.VARDEF;
        }
        return EdgeLabel.NIL;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!super.equals(o)) return false;
        if (!(o instanceof PDGEdge)) return false;

        final PDGEdge pdgEdge = (PDGEdge) o;

        return kind == pdgEdge.kind;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + kind.hashCode();
        return result;
    }
}
