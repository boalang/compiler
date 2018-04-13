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

/**
 * @author marafat
 */

public class CDGEdge {

    private CDGNode src;
    private CDGNode dest;
    private String label;

    public CDGEdge(final CDGNode src, final CDGNode dest) {
        this.src = src;
        this.dest = dest;
    }

    public CDGEdge(final CDGNode src, final CDGNode dest, final String label) {
        this.src = src;
        this.dest = dest;
        this.label = label;
    }

    //Setters
    public void setSrc(final CDGNode src) {
        this.src = src;
    }

    public void setDest(final CDGNode dest) {
        this.dest = dest;
    }

    public void setLabel(final String label) {
        this.label = label;
    }

    //Getters
    public CDGNode getSrc() {
        return src;
    }

    public CDGNode getDest() {
        return dest;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CDGEdge cdgEdge = (CDGEdge) o;

        if (!src.equals(cdgEdge.src)) return false;
        if (!dest.equals(cdgEdge.dest)) return false;
        return label.equals(cdgEdge.label);
    }

    @Override
    public int hashCode() {
        int result = src.hashCode();
        result = 31 * result + dest.hashCode();
        result = 31 * result + label.hashCode();
        return result;
    }
}
