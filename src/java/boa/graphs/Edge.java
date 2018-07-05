/*
 * Copyright 2018, Robert Dyer,
 *                 Bowling Green State University
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
package boa.graphs;

import boa.types.Control.Edge.EdgeLabel;

/**
 * A graph edge
 *
 * @author rdyer
 */
public abstract class Edge<N extends Node, E extends Edge<N, E>> implements Comparable<E> {
	protected N src;
	protected N dest;
	protected String label = ".";

	@Override
	public int compareTo(final E edge) {
		return this.dest.getId() - edge.dest.getId();
	}

    public Edge() {
    }

	public Edge(final N src, final N dest) {
		this.src = src;
		this.dest = dest;

		this.src.addOutEdge(this);
		this.dest.addInEdge(this);
	}

	public Edge(final N src, final N dest, final String label) {
		this(src, dest);

		this.label = label;
	}

	public N getSrc() {
		return src;
	}

	public void setSrc(final N node) {
		if (this.dest.getPredecessors().contains(node)) {
			delete();
			this.dest.getInEdge(node).setLabel(".");
		} else {
			this.src = node;
			node.addOutEdge(this);
		}
	}

	public N getDest() {
		return this.dest;
	}

	public void setDest(final N node) {
		if (this.src.getSuccessors().contains(node)) {
			delete();
			this.src.getOutEdge(node).setLabel(".");
		} else {
			this.dest = node;
			node.addInEdge(this);
		}
	}

	public String getLabel() {
		return this.label;
	}

	public void setLabel(final String label) {
		this.label = label;
	}

	public void delete() {
        this.src.removeOutEdge(this);
        this.dest.removeInEdge(this);
        this.src = this.dest = null;
	}

	public boa.types.Control.Edge.Builder newBuilder() {
		final boa.types.Control.Edge.Builder eb = boa.types.Control.Edge.newBuilder();
		eb.setLabel(Edge.convertLabel(this.label));
		return eb;
	}

	public static EdgeLabel convertLabel(final String label) {
		if (label.equals(".")) {
			return EdgeLabel.DEFAULT;
		} else if (label.equals("T")) {
			return EdgeLabel.TRUE;
		} else if (label.equals("F")) {
			return EdgeLabel.FALSE;
		} else if (label.equals("B")) {
			return EdgeLabel.BACKEDGE;
		} else if (label.equals("E")) {
			return EdgeLabel.EXITEDGE;
		} else {
			return EdgeLabel.NIL;
		}
	}

	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(src.toString());
		sb.append(" --> ");
		sb.append(dest.toString());
		if (!".".equals(label)) {
			sb.append(" [");
			sb.append(label);
			sb.append("]");
		}
		return sb.toString();
	}

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof Edge)) return false;

        final Edge e = (Edge) o;

        if (this.label == null && e.label != null) return false;
        return src.equals(e.src) && dest.equals(e.dest) && label.equals(e.label);
    }

    private int hash = -1;

    @Override
    public int hashCode() {
        if (hash == -1) {
            hash = 1;
            if (label != null)
                hash = 31 * hash + label.hashCode();
            hash = 31 * hash + src.hashCode();
            hash = 31 * hash + dest.hashCode();
        }
        return hash;
    }
}
