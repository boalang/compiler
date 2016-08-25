/*
 * Copyright 2014, Hridesh Rajan, Robert Dyer, 
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

/**
 * Control flow graph builder edge
 * @author ganeshau
 *
 */
public class CFGEdge {
	public static long numOfEdges = 0;

	private long id;
	private CFGNode src;
	private CFGNode dest;
	private String label = ".";

	public CFGEdge(CFGNode src, CFGNode dest) {
		this.id = ++numOfEdges;
		this.src = src;
		this.dest = dest;
		src.addOutEdge(this);
		dest.addInEdge(this);

		if (src.getType() == CFGNode.TYPE_CONTROL) {
			if (src.hasFalseBranch()) {
				this.label = "T";
			} else {
				if (label == null || label.compareTo(".") != 0) {
					this.label = "F";
				}
			}
		}
	}

	public CFGEdge(CFGNode src, CFGNode dest, String label) {
		this(src, dest);
		this.label = label;
	}

	public CFGNode getSrc() {
		return src;
	}

	public void setSrc(CFGNode node) {
		if (dest.getInNodes().contains(node)) {
			delete();
			CFGEdge e = (CFGEdge) dest.getInEdge(node);
			e.setLabel(".");
		} else {
			this.src = node;
			node.addOutEdge(this);
		}
	}

	public CFGNode getDest() {
		return dest;
	}

	public void setDest(CFGNode node) {
		if (src.getOutNodes().contains(node)) {
			delete();
			CFGEdge e = (CFGEdge) src.getOutEdge(node);
			e.setLabel(".");
		} else {
			this.dest = node;
			node.addInEdge(this);
		}
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public void delete() {
		this.src.getOutEdges().remove(this);
		this.dest.getInEdges().remove(this);
	}

	public CFGEdge(long id, CFGNode src, CFGNode dest) {
		this.id = id;
		this.src = src;
		this.dest = dest;
		src.addOutEdge(this);
		dest.addInEdge(this);

		if (src.getType() == CFGNode.TYPE_CONTROL) {
			if (src.hasFalseBranch()) {
				this.label = "T";
			} else {
				if (label == null || label.compareTo(".") != 0) {
					this.label = "F";
				}
			}
		}
	}

	public CFGEdge(long id, CFGNode src, CFGNode dest, String label) {
		this(id, src, dest);
		this.label = label;
	}
}
