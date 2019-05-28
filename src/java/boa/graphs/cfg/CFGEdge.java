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

import boa.graphs.Edge;
import boa.types.Control.Node.NodeType;

/**
 * Control flow graph builder edge
 *
 * @author ganeshau
 * @author rdyer
 */
public class CFGEdge extends Edge<CFGNode, CFGEdge> {
	public CFGEdge(final CFGNode src, final CFGNode dest) {
        super(src, dest);

		if (this.src.getKind() == NodeType.CONTROL) {
			if (this.src.hasFalseBranch()) {
				this.label = "T";
			} else {
				if (this.label == null || this.label.compareTo(".") != 0) {
					this.label = "F";
				}
			}
		}
	}

	public CFGEdge(final CFGNode src, final CFGNode dest, final String label) {
		this(src, dest);

		this.label = label;
	}
}
