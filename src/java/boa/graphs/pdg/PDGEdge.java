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

/**
 * @author marafat
 */

public class PDGEdge {

    private PDGNode src;
    private PDGNode dest;
    private String label; //name of the variable for data edge, T or F for Control Edge
    private PDGEdgeType kind;

    public PDGEdge(PDGNode src, PDGNode dest, String label, PDGEdgeType kind) {
        this.src = src;
        this.dest = dest;
        this.label = label;
        this.kind = kind;
    }

    //Setters
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

    //Getters
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

    enum PDGEdgeType {
        ControlEdge,
        DataEdge;
    }

}
