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
import boa.graphs.cdg.*;

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

	protected boolean preTraverse(final CDG cdg) throws Exception {
		return defaultPreTraverse();
	}

	public final void traverse(final CFG cfg, final Traversal.TraversalDirection direction, final Traversal.TraversalKind kind, final String str) throws Exception {
		traverse(cfg, direction, kind);
	}

	public final void dfsForward(final CDGNode node, java.util.Set<Integer> visitedNodes) throws Exception {
		final Stack<CDGNode> s = new Stack<CDGNode>();
		s.push(node);
		while (!s.isEmpty()) {
			final CDGNode n = s.pop();
			if (!visitedNodes.contains(n.getId())) {
				traverse(n, false);
				visitedNodes.add(n.getId());
				for (int i = n.getSuccessors().size() - 1; i >= 0; i--) {
					s.push(n.getSuccessors().get(i));
				}
			}
		}
	}

	public final void dfsForward(final CFGNode node, java.util.Set<Integer> visitedNodes) throws Exception {
		final Stack<CFGNode> s = new Stack<CFGNode>();
		s.push(node);
		while (!s.isEmpty()) {
			final CFGNode n = s.pop();
			if (!visitedNodes.contains(n.getId())) {
				traverse(n, false);
				visitedNodes.add(n.getId());
				for (int i = n.getSuccessorsList().size() - 1; i >= 0; i--) {
					s.push(n.getSuccessorsList().get(i));
				}
			}
		}
	}

	public final void dfsBackward(final CFGNode node, java.util.Set<Integer> visitedNodes) throws Exception {
		final Stack<CFGNode> s = new Stack<CFGNode>();
		s.push(node);
		while (!s.isEmpty()) {
			final CFGNode n = s.pop();
			if (!visitedNodes.contains(n.getId())) {
				traverse(n, false);
				visitedNodes.add(n.getId());
				for (int i = n.getPredecessorsList().size() - 1; i >= 0; i--) {
					s.push(n.getPredecessorsList().get(i));
				}
			}
		}
	}

	public final void postorderBackward(final CFGNode node, java.util.Set<Integer> visitedNodes) throws Exception {
		// FIXME remove recursion
		visitedNodes.add(node.getId());
		for (final CFGNode succ : node.getSuccessorsList()) {
			if (!visitedNodes.contains(succ.getId())) {
				postorderBackward(succ, visitedNodes);
			}
		}
		traverse(node, false);
	}

	public final void postorderForward(final CFGNode node, java.util.Set<Integer> visitedNodes) throws Exception {
		// FIXME remove recursion
		visitedNodes.add(node.getId());
		for (final CFGNode pred : node.getPredecessorsList()) {
			if (!visitedNodes.contains(pred.getId())) {
				postorderForward(pred, visitedNodes);
			}
		}
		traverse(node, false);
	}

	public final void populateWithPostorder(final CFGNode node, java.util.Set<Integer> visitedNodes, Queue<CFGNode> queue) throws Exception {
		// FIXME remove recursion
		visitedNodes.add(node.getId());
		for (final CFGNode succ : node.getSuccessorsList()) {
			if (!visitedNodes.contains(succ.getId())) {
				populateWithPostorder(succ, visitedNodes, queue);
			}
		}
		queue.offer(node);
	}

	public final void populateWithReversePostorder(final CFGNode node, java.util.Set<Integer> visitedNodes, Stack<CFGNode> stack) throws Exception {
		// FIXME remove recursion
		visitedNodes.add(node.getId());
		for (final CFGNode succ : node.getSuccessorsList()) {
			if (!visitedNodes.contains(succ.getId())) {
				populateWithReversePostorder(succ, visitedNodes, stack);
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
			// FIXME why is this here?
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
					final java.util.Set<Integer> visitedNodes = new java.util.HashSet<Integer>();
					final CFGNode[] nl = cfg.sortNodes();
					if (nl.length != 0) {
						if (this.isFlowSensitive) {
							if (this.isLoopSensitive) {
								switch (direction) {
									case BACKWARD:
										if (cfg.getIsLoopPresent()) {
											final Queue<CFGNode> queue = new LinkedList<CFGNode>();
											populateWithPostorder(cfg.getEntryNode(), visitedNodes, queue);
											worklistPostorderBackward(queue, fixp, kind);
										} else if (cfg.getIsBranchPresent()) {
											postorderBackward(cfg.getEntryNode(), visitedNodes);
										} else {
											for (int i = nl.length - 1; i >= 0; i--) {
												traverse(nl[i], false);
											}
										}
										break;
									case FORWARD:
										if (cfg.getIsLoopPresent()) {
											final Stack<CFGNode> stack = new Stack<CFGNode>();
											populateWithReversePostorder(cfg.getEntryNode(), visitedNodes, stack);
											worklistReversePostorderForward(stack, fixp, kind);
										} else if (cfg.getIsBranchPresent()) {
											postorderForward(cfg.getExitNode(), visitedNodes);
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
											populateWithPostorder(cfg.getEntryNode(), visitedNodes, queue);
											while (!queue.isEmpty()) {
												traverse(queue.remove(), false);
											}
										} else if (cfg.getIsBranchPresent()) {
											postorderBackward(cfg.getEntryNode(), visitedNodes);
										} else {
											for (int i = nl.length - 1; i >= 0; i--) {
												traverse(nl[i], false);
											}
										}
										break;
									case FORWARD:
										if (cfg.getIsLoopPresent()) {
											final Stack<CFGNode> stack = new Stack<CFGNode>();
											populateWithReversePostorder(cfg.getEntryNode(), visitedNodes, stack);
											while (!stack.isEmpty()) {
												traverse(stack.pop(), false);
											}
										} else if (cfg.getIsBranchPresent()) {
											postorderForward(cfg.getExitNode(), visitedNodes);
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
				final java.util.Set<Integer> visitedNodes = new java.util.HashSet<Integer>();
				switch (kind) {
					case WORKLIST_POSTORDER:
						Queue<CFGNode> queue = new LinkedList<CFGNode>();
						populateWithPostorder(cfg.getEntryNode(), visitedNodes, queue);
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
						populateWithReversePostorder(cfg.getEntryNode(), visitedNodes, stack);
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
				if (outputMapObj == null) {
					outputMapObj = new java.util.HashMap<Integer, T1>();
				}
				if (cfg.getNodes().size() != 0) {
					final java.util.Set<Integer> visitedNodes = new java.util.HashSet<Integer>();
					final CFGNode[] nl = cfg.sortNodes();
					switch (kind) {
						case DFS:
							switch (direction) {
								case FORWARD:
									dfsForward(cfg.getEntryNode(), visitedNodes);
									break;
								default:
								case BACKWARD:
									dfsBackward(cfg.getExitNode(), visitedNodes);
									break;
							}
							break;
						case POSTORDER:
							postorderBackward(cfg.getEntryNode(), visitedNodes);
							break;
						case REVERSEPOSTORDER:
							postorderForward(cfg.getExitNode(), visitedNodes);
							break;
						case WORKLIST_POSTORDER:
							switch (direction) {
								case FORWARD:
									Stack<CFGNode> stack = new Stack<CFGNode>();
									populateWithReversePostorder(cfg.getEntryNode(), visitedNodes, stack);
									worklistReversePostorderWithoutFixp(stack, kind);
									break;
								default:
								case BACKWARD:
									Queue<CFGNode> queue = new LinkedList<CFGNode>();
									populateWithPostorder(cfg.getEntryNode(), visitedNodes, queue);
									worklistPostorderWithoutFixp(queue, kind);
									break;
							}
							break;
						case WORKLIST_REVERSEPOSTORDER:
							switch (direction) {
								case FORWARD:
									final Queue<CFGNode> queue = new LinkedList<CFGNode>();
									populateWithPostorder(cfg.getEntryNode(), visitedNodes, queue);
									worklistPostorderWithoutFixp(queue, kind);
									break;
								default:
								case BACKWARD:
									final Stack<CFGNode> stack = new Stack<CFGNode>();
									populateWithReversePostorder(cfg.getEntryNode(), visitedNodes, stack);
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

	public final void traverse(final boa.graphs.cdg.CDG cdg, final Traversal.TraversalDirection direction, final Traversal.TraversalKind kind) throws Exception {
		try {
			if (preTraverse(cdg)) {
				if (outputMapObj == null) {
					outputMapObj = new java.util.HashMap<Integer, T1>();
				}
				if (cdg.getNodes().size() != 0) {
					final java.util.Set<Integer> visitedNodes = new java.util.HashSet<Integer>();
					//final CDGNode[] nl = cdg.sortNodes();
					switch (kind) {
						case DFS:
							switch (direction) {
								case FORWARD:
									dfsForward(cdg.getEntryNode(), visitedNodes);
									break;
								default:
								//case BACKWARD:
									//dfsBackward(cdg.getExitNode(), visitedNodes);
									break;
							}
							break;
/*
						case POSTORDER:
							postorderBackward(cdg.getEntryNode(), visitedNodes);
							break;
						case REVERSEPOSTORDER:
							postorderForward(cdg.getExitNode(), visitedNodes);
							break;
						case WORKLIST_POSTORDER:
							switch (direction) {
								case FORWARD:
									Stack<CDGNode> stack = new Stack<CDGNode>();
									populateWithReversePostorder(cdg.getEntryNode(), visitedNodes, stack);
									worklistReversePostorderWithoutFixp(stack, kind);
									break;
								default:
								case BACKWARD:
									Queue<CDGNode> queue = new LinkedList<CDGNode>();
									populateWithPostorder(cdg.getEntryNode(), visitedNodes, queue);
									worklistPostorderWithoutFixp(queue, kind);
									break;
							}
							break;
						case WORKLIST_REVERSEPOSTORDER:
							switch (direction) {
								case FORWARD:
									final Queue<CDGNode> queue = new LinkedList<CDGNode>();
									populateWithPostorder(cdg.getEntryNode(), visitedNodes, queue);
									worklistPostorderWithoutFixp(queue, kind);
									break;
								default:
								case BACKWARD:
									final Stack<CDGNode> stack = new Stack<CDGNode>();
									populateWithReversePostorder(cdg.getEntryNode(), visitedNodes, stack);
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
							for (final CDGNode n : cdg.getNodes()) {
								traverse(n, false);
							}
							break;
						case HYBRID:
							for (int i = 0; i < nl.length; i++) {
								traverse(nl[i], false);
							}
							break;
*/
						default:
							break;
					}
				}
			}
		} catch (final java.lang.StackOverflowError e) {
			return;
		}
	}

	public void traverse(final CDGNode node, boolean flag) throws Exception {
	}
}
