/*
 * Copyright 2018, Hridesh Rajan, Ganesha Upadhyaya, Robert Dyer,
 *                 Bowling Green State University
 *                 and Iowa State University of Science and Technology
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
package boa.graphs.cfg;

import boa.types.Control.CFGEdge.CFGEdgeLabel;
import boa.types.Control.CFGNode.CFGNodeType;

/**
 * Control flow graph builder edge
 *
 * @author ganeshau
 * @author rdyer
 */
public class CFGEdge implements Comparable<CFGEdge> {
	public static long numOfEdges = 0;

	private long id;
	private CFGNode src;
	private CFGNode dest;
	private String label = ".";

	@Override
	public int compareTo(final CFGEdge edge) {
		return this.dest.getId() - edge.dest.getId();
	}

	public CFGEdge(final long id, final CFGNode src, final CFGNode dest) {
		this.id = id;
		this.src = src;
		this.dest = dest;

		this.src.addOutEdge(this);
		this.dest.addInEdge(this);

		if (this.src.getKind() == CFGNodeType.CONTROL) {
			if (this.src.hasFalseBranch()) {
				this.label = "T";
			} else {
				if (this.label == null || this.label.compareTo(".") != 0) {
					this.label = "F";
				}
			}
		}
	}

	public CFGEdge(final long id, final CFGNode src, final CFGNode dest, final String label) {
		this(id, src, dest);

		this.label = label;
	}

	public CFGEdge(final CFGNode src, final CFGNode dest) {
		this(++numOfEdges, src, dest);
	}

	public CFGEdge(final CFGNode src, final CFGNode dest, final String label) {
		this(++numOfEdges, src, dest, label);
	}

	public CFGNode getSrc() {
		return src;
	}

	public void setSrc(final CFGNode node) {
		if (this.dest.getPredecessorsList().contains(node)) {
			delete();
			final CFGEdge e = (CFGEdge)this.dest.getInEdge(node); //FIXME: redundant cast
			e.setLabel(".");
		} else {
			this.src = node;
			node.addOutEdge(this);
		}
	}

	public CFGNode getDest() {
		return this.dest;
	}

	public void setDest(final CFGNode node) {
		if (this.src.getSuccessorsList().contains(node)) {
			delete();
			final CFGEdge e = (CFGEdge)this.src.getOutEdge(node); //FIXME: redundant cast
			e.setLabel(".");
		} else {
			this.dest = node;
			node.addInEdge(this);
		}
	}

	public long getId() {
		return this.id;
	}

	public void setId(final long id) {
		this.id = id;
	}

	public String label() {
		return this.label;
	}

	public void setLabel(final String label) {
		this.label = label;
	}

	private void delete() {
		this.src.getOutEdges().remove(this);
		this.dest.getInEdges().remove(this);
	}

	public boa.types.Control.CFGEdge.Builder newBuilder() {
		final boa.types.Control.CFGEdge.Builder eb = boa.types.Control.CFGEdge.newBuilder();
		eb.setLabel(CFGEdge.getLabel(this.label));
		return eb;
	}

	public static CFGEdgeLabel getLabel(final String label) {
		if (label.equals(".")) {
			return CFGEdgeLabel.DEFAULT;
		} else if (label.equals("T")) {
			return CFGEdgeLabel.TRUE;
		} else if (label.equals("F")) {
			return CFGEdgeLabel.FALSE;
		} else if (label.equals("B")) {
			return CFGEdgeLabel.BACKEDGE;
		} else if (label.equals("E")) {
			return CFGEdgeLabel.EXITEDGE;
		} else {
			return CFGEdgeLabel.NIL;
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
}
