/*
 * Copyright 2016, Hridesh Rajan, Robert Dyer, Ramanathan Ramu
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
package boa.runtime;

import java.util.Queue;
import java.util.LinkedList;
import java.util.Stack;

import boa.types.Graph.*;
import boa.graphs.cfg.*;

/**
 * Boa abstract graph traversal.
 *
 * @author rramu
 */
public abstract class BoaAbstractTraversal<T1> {
	public java.util.HashMap<Integer, T1> outputMapObj;
	public java.util.HashMap<Integer, T1> prevOutputMapObj;
	public T1 currentResult;

	boolean isLoopSensitive = false;
	boolean isFlowSensitive = false;

	public BoaAbstractTraversal(boolean isFlowSensitive, boolean isLoopSensitive) {
		this.isFlowSensitive = isFlowSensitive;
		this.isLoopSensitive = isLoopSensitive;
	}

	public T1 getValue(final CFGNode node) throws Exception {
		return (T1)outputMapObj.get(node.getId());
	}

	public void clear() {
		if (outputMapObj != null)
			outputMapObj.clear();
		if (prevOutputMapObj != null)
			prevOutputMapObj.clear();
	}

	public BoaAbstractTraversal<T1> initialize() {
		return this;
	}

	protected boolean defaultPreTraverse() throws Exception {
		return true;
	}

	protected boolean preTraverse(final CFG cfg) throws Exception {
		return defaultPreTraverse();
	}

	protected void defaultPostTraverse() throws Exception { }

	protected void postTraverse(final CFG cfg) throws Exception {
		defaultPostTraverse();
	}

	protected void postTraverse(final CFGNode node) throws Exception {
		defaultPostTraverse();
	}

	public final void traverse(final CFG cfg, final Traversal.TraversalDirection direction, final Traversal.TraversalKind kind, final String str) throws Exception {
		traverse(cfg, direction, kind);
	}

	public final void dfsForward(final CFGNode node, java.util.HashMap<Integer, String> nodeVisitStatus) throws Exception {
		try {
			traverse(node, false);
			nodeVisitStatus.put(node.getId(), "visited");
			for (final CFGNode succ : node.getSuccessorsList()) {
				if (nodeVisitStatus.get(succ.getId()).equals("unvisited")) {
					dfsForward(succ, nodeVisitStatus);
				}
			}
		} catch (final java.lang.StackOverflowError e) {
			return;
		}
	}

	public final void dfsBackward(final CFGNode node, java.util.HashMap<Integer, String> nodeVisitStatus) throws Exception {
		traverse(node, false);
		nodeVisitStatus.put(node.getId(), "visited");
		for (final CFGNode pred : node.getPredecessorsList()) {
			if (nodeVisitStatus.get(pred.getId()).equals("unvisited")) {
				dfsBackward(pred, nodeVisitStatus);
			}
		}
	}

	public final void postorderBackward(final CFGNode node, java.util.HashMap<Integer, String> nodeVisitStatus) throws Exception {
		nodeVisitStatus.put(node.getId(), "visited");
		for (final CFGNode succ : node.getSuccessorsList()) {
			if (nodeVisitStatus.get(succ.getId()).equals("unvisited")) {
				postorderBackward(succ, nodeVisitStatus);
			}
		}
		traverse(node, false);
	}

	public final void postorderForward(final CFGNode node, java.util.HashMap<Integer, String> nodeVisitStatus) throws Exception {
		nodeVisitStatus.put(node.getId(), "visited");
		for (final CFGNode pred : node.getPredecessorsList()) {
			if (nodeVisitStatus.get(pred.getId()).equals("unvisited")) {
				postorderForward(pred, nodeVisitStatus);
			}
		}
		traverse(node, false);
	}

	public final void populateWithPostorder(final CFGNode node, java.util.HashMap<Integer, String> nodeVisitStatus, Queue<CFGNode> queue) throws Exception {
		nodeVisitStatus.put(node.getId(), "visited");
		for (final CFGNode succ : node.getSuccessorsList()) {
			if (nodeVisitStatus.get(succ.getId()).equals("unvisited")) {
				populateWithPostorder(succ, nodeVisitStatus, queue);
			}
		}
		queue.offer(node);
	}

	public final void populateWithReversePostorder(final CFGNode node, java.util.HashMap<Integer, String> nodeVisitStatus, Stack<CFGNode> stack) throws Exception {
		nodeVisitStatus.put(node.getId(), "visited");
		for (final CFGNode succ : node.getSuccessorsList()) {
			if (nodeVisitStatus.get(succ.getId()).equals("unvisited")) {
				populateWithReversePostorder(succ, nodeVisitStatus, stack);
			}
		}
		stack.push(node);
	}

	public final void worklistReversePostorderForward(final Stack<CFGNode> stack, final BoaAbstractFixP fixp, final Traversal.TraversalKind kind) throws Exception {
		int nodeCount = 0;
		while (!stack.isEmpty()) {
			final CFGNode node = stack.pop();
			traverse(node, true);
			nodeCount++;
			if (nodeCount > 3500) {
				return;
			}
			final boolean curFlag = outputMapObj.containsKey(node.getId());
			boolean fixpFlag = false;
			if (curFlag) {
				final boolean prevFlag = prevOutputMapObj.containsKey(node.getId());
				if (curFlag && prevFlag) {
					fixpFlag = fixp.invoke((T1)outputMapObj.get(node.getId()), (T1)prevOutputMapObj.get(node.getId()));
				}
			}
			if (!fixpFlag) {
				for (final CFGNode succ : node.getSuccessorsList()) {
					if (!stack.contains(succ))
						stack.push(succ);
				}
			}
			prevOutputMapObj.put(node.getId(), currentResult);
		}
	}

	public final void worklistReversePostorderBackward(final Stack<CFGNode> stack, final BoaAbstractFixP fixp, final Traversal.TraversalKind kind) throws Exception {
		while (!stack.isEmpty()) {
			final CFGNode node = stack.pop();
			traverse(node, true);
			final boolean curFlag = outputMapObj.containsKey(node.getId());
			boolean fixpFlag = false;
			if (curFlag) {
				final boolean prevFlag = prevOutputMapObj.containsKey(node.getId());
				if (curFlag && prevFlag) {
					fixpFlag = fixp.invoke((T1)outputMapObj.get(node.getId()), (T1)prevOutputMapObj.get(node.getId()));
				}
			}
			if (!fixpFlag) {
				for (final CFGNode succ : node.getPredecessorsList()) {
					if (!stack.contains(succ))
						stack.push(succ);
				}
			}
			prevOutputMapObj.put(node.getId(), currentResult);
		}
	}

	public final void worklistReversePostorderWithoutFixp(final Stack<CFGNode> stack, final Traversal.TraversalKind kind) throws Exception {
		while (!stack.isEmpty()) {
			final CFGNode node = stack.pop();
			traverse(node, true);
			final boolean fixpFlag = true;
			if (!fixpFlag) {
				for (final CFGNode succ : node.getSuccessorsList()) {
					if (!stack.contains(succ))
						stack.push(succ);
				}
			}
		}
	}

	public final void worklistPostorderWithoutFixp(final Queue<CFGNode> queue, final Traversal.TraversalKind kind) throws Exception {
		while (!queue.isEmpty()) {
			final CFGNode node = queue.remove();
			traverse(node, true);
			final boolean fixpFlag = true;
			if (!fixpFlag) {
				for (final CFGNode pred : node.getPredecessorsList()) {
					if (!queue.contains(pred))
						queue.add(pred);
				}
			}
		}
	}

	public final void worklistPostorderBackward(final Queue<CFGNode> queue, final BoaAbstractFixP fixp, final Traversal.TraversalKind kind) throws Exception {
		while (!queue.isEmpty()) {
			final CFGNode node = queue.remove();
			traverse(node, true);
			final boolean curFlag = outputMapObj.containsKey(node.getId());
			boolean fixpFlag = false;
			if (curFlag) {
				final boolean prevFlag = prevOutputMapObj.containsKey(node.getId());
				if (curFlag && prevFlag) {
					fixpFlag = fixp.invoke((T1)outputMapObj.get(node.getId()), (T1)prevOutputMapObj.get(node.getId()));
				}
			}
			if (!fixpFlag) {
				for (final CFGNode pred : node.getPredecessorsList()) {
					if (!queue.contains(pred))
						queue.add(pred);
				}
			}
			prevOutputMapObj.put(node.getId(), currentResult);
		}
	}

	public final void worklistPostorderForward(final Queue<CFGNode> queue, final BoaAbstractFixP fixp, final Traversal.TraversalKind kind) throws Exception {
		while (!queue.isEmpty()) {
			final CFGNode node = queue.remove();
			traverse(node, true);
			final boolean curFlag = outputMapObj.containsKey(node.getId());
			boolean fixpFlag = false;
			if (curFlag) {
				final boolean prevFlag = prevOutputMapObj.containsKey(node.getId());
				if (curFlag && prevFlag) {
					fixpFlag = fixp.invoke((T1)outputMapObj.get(node.getId()), (T1)prevOutputMapObj.get(node.getId()));
				}
			}
			if (!fixpFlag) {
				for (final CFGNode pred : node.getSuccessorsList()) {
					if (!queue.contains(pred))
						queue.add(pred);
				}
			}
			prevOutputMapObj.put(node.getId(), currentResult);
		}
	}

	public final void traverse(final boa.graphs.cfg.CFG cfg, final Traversal.TraversalDirection direction, final Traversal.TraversalKind kind, final BoaAbstractFixP fixp) throws Exception {
		try {
			if (outputMapObj == null) {
				outputMapObj = new java.util.HashMap<Integer, T1>();
			}
			switch (kind) {
				case DFS:
				case POSTORDER:
				case REVERSEPOSTORDER:
				case ITERATIVE:
				case RANDOM:
					boolean fixpFlag;
					do {
						prevOutputMapObj = new java.util.HashMap<Integer, T1>(outputMapObj);
						traverse(cfg, direction, kind);
						fixpFlag = true;
						final java.util.HashSet<CFGNode> nl = cfg.getNodes();
						for (final CFGNode node : nl) {
							boolean curFlag = outputMapObj.containsKey(node.getId());
							boolean prevFlag = prevOutputMapObj.containsKey(node.getId());
							if (curFlag) {
								if (outputMapObj.containsKey(node.getId()) && prevOutputMapObj.containsKey(node.getId())) {
									fixpFlag = fixpFlag && fixp.invoke((T1)outputMapObj.get(node.getId()), (T1)prevOutputMapObj.get(node.getId()));
								} else {
									fixpFlag = false;
									break;
								}
							}
						}
					} while (!fixpFlag);
					break;
				case WORKLIST_POSTORDER:
				case WORKLIST_REVERSEPOSTORDER:
					prevOutputMapObj = new java.util.HashMap<Integer, T1>();
					traverseWithFixp(cfg, direction, kind, fixp);
					break;
				case HYBRID:
					prevOutputMapObj = new java.util.HashMap<Integer, T1>();
					final java.util.HashMap<Integer, String> nodeVisitStatus = new java.util.HashMap<Integer, String>();
					final CFGNode[] nl = cfg.sortNodes();
					for (int i = 0; i < nl.length; i++) {
						nodeVisitStatus.put(nl[i].getId(), "unvisited");
					}
					if (nl.length != 0) {
						if (this.isFlowSensitive) {
							if (this.isLoopSensitive) {
								switch (direction) {
									case BACKWARD:
										if (cfg.getIsLoopPresent()) {
											final Queue<CFGNode> queue = new LinkedList<CFGNode>();
											populateWithPostorder(cfg.getEntryNode(), nodeVisitStatus, queue);
											worklistPostorderBackward(queue, fixp, kind);
										} else if (cfg.getIsBranchPresent()) {
											postorderBackward(cfg.getEntryNode(), nodeVisitStatus);
										} else {
											for (int i = nl.length - 1; i >= 0; i--) {
												traverse(nl[i], false);
											}
										}
										break;
									case FORWARD:
										if (cfg.getIsLoopPresent()) {
											final Stack<CFGNode> stack = new Stack<CFGNode>();
											populateWithReversePostorder(cfg.getEntryNode(), nodeVisitStatus, stack);
											worklistReversePostorderForward(stack, fixp, kind);
										} else if (cfg.getIsBranchPresent()) {
											postorderForward(cfg.getExitNode(), nodeVisitStatus);
										} else {
											for (int i = 0; i < nl.length; i++) {
												traverse(nl[i], false);
											}
										}
										break;
									default:
										break;
								}
							} else {
								switch (direction) {
									case BACKWARD:
										if (cfg.getIsLoopPresent()) {
											final Queue<CFGNode> queue = new LinkedList<CFGNode>();
											populateWithPostorder(cfg.getEntryNode(), nodeVisitStatus, queue);
											while (!queue.isEmpty()) {
												traverse(queue.remove(), false);
											}
										} else if (cfg.getIsBranchPresent()) {
											postorderBackward(cfg.getEntryNode(), nodeVisitStatus);
										} else {
											for (int i = nl.length - 1; i >= 0; i--) {
												traverse(nl[i], false);
											}
										}
										break;
									case FORWARD:
										if (cfg.getIsLoopPresent()) {
											final Stack<CFGNode> stack = new Stack<CFGNode>();
											populateWithReversePostorder(cfg.getEntryNode(), nodeVisitStatus, stack);
											while (!stack.isEmpty()) {
												traverse(stack.pop(), false);
											}
										} else if (cfg.getIsBranchPresent()) {
											postorderForward(cfg.getExitNode(), nodeVisitStatus);
										} else {
											for (int i = 0; i < nl.length; i++) {
												traverse(nl[i], false);
											}
										}
										break;
									default:
										break;
								}
							}
						}
					}
					break;
				default:
					break;
			}
		} catch (final java.lang.StackOverflowError e) {
			return;
		}
	}

	public final void traverseWithFixp(final CFG cfg, final Traversal.TraversalDirection direction, final Traversal.TraversalKind kind, final BoaAbstractFixP fixp) throws Exception {
		if (preTraverse(cfg)) {
			if (outputMapObj==null) {
				outputMapObj = new java.util.HashMap<Integer, T1>();
			}
			if (cfg.getNodes().size() != 0) {
				java.util.HashMap<Integer, String> nodeVisitStatus = new java.util.HashMap<Integer, String>();
				for (CFGNode nl : cfg.getNodes()) {
					nodeVisitStatus.put(nl.getId(), "unvisited");
				}
				switch (kind) {
					case WORKLIST_POSTORDER:
						Queue<CFGNode> queue = new LinkedList<CFGNode>();
						populateWithPostorder(cfg.getEntryNode(), nodeVisitStatus, queue);
						switch (direction) {
							case BACKWARD:
								worklistPostorderBackward(queue, fixp, kind);
								break;

							case FORWARD:
								worklistPostorderForward(queue, fixp, kind);
								break;
							default : break;
						}
						break;
					case WORKLIST_REVERSEPOSTORDER:
						Stack<CFGNode> stack = new Stack<CFGNode>();
						populateWithReversePostorder(cfg.getEntryNode(), nodeVisitStatus, stack);
						switch (direction) {
							case BACKWARD:
								worklistReversePostorderBackward(stack, fixp, kind);
								break;

							case FORWARD:
								worklistReversePostorderForward(stack, fixp, kind);
								break;
							default : break;
						}
						break;
					default : break;
				}
			}
		}
	}

	public final void traverse(final boa.graphs.cfg.CFG cfg, final Traversal.TraversalDirection direction, final Traversal.TraversalKind kind) throws Exception {
		try {
			if (preTraverse(cfg)) {
				if (outputMapObj==null) {
					outputMapObj = new java.util.HashMap<Integer, T1>();
				}
				if (cfg.getNodes().size()!=0) {
					java.util.HashMap<Integer, String> nodeVisitStatus = new java.util.HashMap<Integer, String>();
					CFGNode[] nl = cfg.sortNodes();
					for (int i = 0;i<nl.length;i++) {
						nodeVisitStatus.put(nl[i].getId(), "unvisited");
					}
					switch (kind) {
						case DFS:
							switch (direction) {
								case FORWARD:
									dfsForward(cfg.getEntryNode(), nodeVisitStatus);
									break;
								default:
								case BACKWARD:
									dfsBackward(cfg.getExitNode(), nodeVisitStatus);
									break;
							}
							break;
						case POSTORDER:
							postorderBackward(cfg.getEntryNode(), nodeVisitStatus);
							break;
						case REVERSEPOSTORDER:
							postorderForward(cfg.getExitNode(), nodeVisitStatus);
							break;
						case WORKLIST_POSTORDER:
							switch (direction) {
								case FORWARD:
									Stack<CFGNode> stack = new Stack<CFGNode>();
									populateWithReversePostorder(cfg.getEntryNode(), nodeVisitStatus, stack);
									worklistReversePostorderWithoutFixp(stack, kind);
									break;
								default:
								case BACKWARD:
									Queue<CFGNode> queue = new LinkedList<CFGNode>();
									populateWithPostorder(cfg.getEntryNode(), nodeVisitStatus, queue);
									worklistPostorderWithoutFixp(queue, kind);
									break;
							}
							break;
						case WORKLIST_REVERSEPOSTORDER:
							switch (direction) {
								case FORWARD:
									final Queue<CFGNode> queue = new LinkedList<CFGNode>();
									populateWithPostorder(cfg.getEntryNode(), nodeVisitStatus, queue);
									worklistPostorderWithoutFixp(queue, kind);
									break;
								default:
								case BACKWARD:
									final Stack<CFGNode> stack = new Stack<CFGNode>();
									populateWithReversePostorder(cfg.getEntryNode(), nodeVisitStatus, stack);
									worklistReversePostorderWithoutFixp(stack, kind);
									break;
							}
							break;
						case ITERATIVE:
							switch (direction) {
								case FORWARD:
									for (int i = 0; i < nl.length; i++) {
										traverse(nl[i], false);
									}
									break;
								default:
								case BACKWARD:
									for (int i = nl.length - 1; i >= 0; i--) {
										traverse(nl[i], false);
									}
									break;
							}
							break;
						case RANDOM:
							for (final CFGNode n : cfg.getNodes()) {
								traverse(n, false);
							}
							break;
						case HYBRID:
							for (int i = 0; i < nl.length; i++) {
								traverse(nl[i], false);
							}
							break;
						default:
							break;
					}
				}
			}
		} catch (final java.lang.StackOverflowError e) {
			return;
		}
	}

	public void traverse(final CFGNode node, boolean flag) throws Exception {
	}
}
