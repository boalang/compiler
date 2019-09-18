/*
 * Copyright 2017, Hridesh Rajan, Robert Dyer, Ramanathan Ramu
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
package boa.compiler.visitors.analysis;

import boa.compiler.ast.Node;
import boa.compiler.visitors.*;

/**
 * @author rramu
 */
public class CreateNodeId extends AbstractVisitorNoArgNoRet {
	int id = 0;

	public final void createNodeIds(final Node node, final java.util.Set<Node> visitedNodes) {
		visitedNodes.add(node);
		node.nodeId = ++id;
		for (final Node succ : node.successors) {
			if (!visitedNodes.contains(succ)) {
				createNodeIds(succ, visitedNodes);
			}
		}
	}

	public void start(final CFGBuildingVisitor cfgBuilder) {
		final java.util.Set<Node> visitedNodes = new java.util.HashSet<Node>();
		visitedNodes.add(cfgBuilder.currentStartNodes.get(0));
		createNodeIds(cfgBuilder.currentStartNodes.get(0), visitedNodes);
	}
}
