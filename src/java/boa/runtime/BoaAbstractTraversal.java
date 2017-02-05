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
package boa.runtime;

import java.util.List;
import java.util.Queue;
import java.util.LinkedList;
import java.util.Stack;

import boa.functions.BoaAstIntrinsics;

import boa.types.Ast.*;
import boa.types.Ast.Expression.*;
import boa.types.Graph.*;
import boa.graphs.cfg.*;
import boa.types.Code.CodeRepository;
import boa.types.Code.Revision;
import boa.types.Diff.ChangedFile;
import boa.types.Shared.Person;
import boa.types.Toplevel.Project;

/**
 * Boa abstract graph traversal.
 * @author rramu
 */

public abstract class BoaAbstractTraversal<T1> {
	public java.util.HashMap<Integer,T1> outputMapObj;
	public java.util.HashMap<Integer,T1> prevOutputMapObj;
	public T1 currentResult;

	public T1 getValue(final CFGNode node) throws Exception {
		return (T1)outputMapObj.get(node.getId());
	}
	public void clear() {
		if(outputMapObj!=null)
			outputMapObj.clear();
		if(prevOutputMapObj!=null)
			prevOutputMapObj.clear();
	}

	public BoaAbstractTraversal initialize() {
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

	public final void dfsForward(final CFGNode node, java.util.HashMap<Integer,String> nodeVisitStatus) throws Exception {
		try {
			traverse(node, false);
			nodeVisitStatus.put(node.getId(),"visited");
			for (CFGNode succ : node.getSuccessorsList()) {
			    if (nodeVisitStatus.get(succ.getId()).equals("unvisited")) {
				dfsForward(succ, nodeVisitStatus);
			    }
			}
		} catch(Exception e) {
			return;
		}
	}

	public final void dfsBackward(final CFGNode node, java.util.HashMap<Integer,String> nodeVisitStatus) throws Exception {
		traverse(node, false);
		nodeVisitStatus.put(node.getId(),"visited");
		for (CFGNode pred : node.getPredecessorsList()) {
		    if (nodeVisitStatus.get(pred.getId()).equals("unvisited")) {
			dfsBackward(pred, nodeVisitStatus);
		    }
		}
	}

	public final void postorderBackward(final CFGNode node, java.util.HashMap<Integer,String> nodeVisitStatus) throws Exception {
		nodeVisitStatus.put(node.getId(),"visited");
		for (CFGNode succ : node.getSuccessorsList()) {
		    if (nodeVisitStatus.get(succ.getId()).equals("unvisited")) {
			postorderBackward(succ, nodeVisitStatus);
		    }
		}
		traverse(node, false);
	}

	public final void postorderForward(final CFGNode node, java.util.HashMap<Integer,String> nodeVisitStatus) throws Exception {
		nodeVisitStatus.put(node.getId(),"visited");
		for (CFGNode pred : node.getPredecessorsList()) {
		    if (nodeVisitStatus.get(pred.getId()).equals("unvisited")) {
			postorderForward(pred, nodeVisitStatus);
		    }
		}
		traverse(node, false);
	}

	public final void populateWithPostorder(final CFGNode node, java.util.HashMap<Integer,String> nodeVisitStatus, Queue<CFGNode> queue) throws Exception {
		nodeVisitStatus.put(node.getId(),"visited");
		for (CFGNode succ : node.getSuccessorsList()) {
		    if (nodeVisitStatus.get(succ.getId()).equals("unvisited")) {
			populateWithPostorder(succ, nodeVisitStatus, queue);
		    }
		}
		queue.offer(node);
	}

	public final void populateWithReversePostorder(final CFGNode node, java.util.HashMap<Integer,String> nodeVisitStatus, Stack<CFGNode> stack) throws Exception {
		nodeVisitStatus.put(node.getId(),"visited");
		for (CFGNode succ : node.getSuccessorsList()) {
		    if (nodeVisitStatus.get(succ.getId()).equals("unvisited")) {
			populateWithReversePostorder(succ, nodeVisitStatus, stack);
		    }
		}
		stack.push(node);
	}
	public final void worklistReversePostorderForward(Stack<CFGNode> stack, final BoaAbstractFixP fixp, final Traversal.TraversalKind kind) throws Exception {
		int nodeCount = 0;
		while(!stack.isEmpty()) {
			CFGNode node = stack.pop();
			traverse(node, true);
			nodeCount++;
			if(nodeCount > 3500) {
				return;
			}
			boolean curFlag=outputMapObj.containsKey(node.getId());
			boolean fixpFlag = false;
			if(curFlag) {
				boolean prevFlag=prevOutputMapObj.containsKey(node.getId());
				if(curFlag && prevFlag) { 
					fixpFlag = fixp.invoke((T1)outputMapObj.get(node.getId()),(T1)prevOutputMapObj.get(node.getId()));
				}
			}
			if(!fixpFlag) {
				for (CFGNode succ : node.getSuccessorsList()) {
					if(!stack.contains(succ))
						stack.push(succ);
				}
			}
			prevOutputMapObj.put(node.getId(), currentResult);
		}
	}
	public final void worklistReversePostorderBackward(Stack<CFGNode> stack, final BoaAbstractFixP fixp, final Traversal.TraversalKind kind) throws Exception {
		while(!stack.isEmpty()) {
			CFGNode node = stack.pop();
			traverse(node, true);
			boolean curFlag=outputMapObj.containsKey(node.getId());
			boolean fixpFlag = false;
			if(curFlag) {
				boolean prevFlag=prevOutputMapObj.containsKey(node.getId());
				if(curFlag && prevFlag) { 
					fixpFlag = fixp.invoke((T1)outputMapObj.get(node.getId()),(T1)prevOutputMapObj.get(node.getId()));
				}
			}
			if(!fixpFlag) {
				for (CFGNode succ : node.getPredecessorsList()) {
					if(!stack.contains(succ))
						stack.push(succ);
				}
			}
			prevOutputMapObj.put(node.getId(), currentResult);
		}
	}
	public final void worklistReversePostorderWithoutFixp(Stack<CFGNode> stack, final Traversal.TraversalKind kind) throws Exception {
		while(!stack.isEmpty()) {
			CFGNode node = stack.pop();
			traverse(node, true);
			boolean curFlag=true;
			boolean fixpFlag = true;
			if(!fixpFlag) {
				for (CFGNode succ : node.getSuccessorsList()) {
					if(!stack.contains(succ))
						stack.push(succ);
				}
			}
		}
	}

	public final void worklistPostorderWithoutFixp(Queue<CFGNode> queue, final Traversal.TraversalKind kind) throws Exception {
		while(!queue.isEmpty()) {
			CFGNode node = queue.remove();
			traverse(node, true);
			boolean curFlag=true;
			boolean fixpFlag = true;
			if(!fixpFlag) {
				for (CFGNode pred : node.getPredecessorsList()) {
					if(!queue.contains(pred))
						queue.add(pred);
				}
			}
		}
	}

	public final void worklistPostorderBackward(Queue<CFGNode> queue, final BoaAbstractFixP fixp, final Traversal.TraversalKind kind) throws Exception {
		while(!queue.isEmpty()) {
				CFGNode node = queue.remove();
				traverse(node, true);
				boolean curFlag=outputMapObj.containsKey(node.getId());
				boolean fixpFlag = false;
				if(curFlag) {
					boolean prevFlag=prevOutputMapObj.containsKey(node.getId());
					if(curFlag && prevFlag) { 
						fixpFlag = fixp.invoke((T1)outputMapObj.get(node.getId()),(T1)prevOutputMapObj.get(node.getId()));
					}
				}
				if(!fixpFlag) {
					for (CFGNode pred : node.getPredecessorsList()) {
						if(!queue.contains(pred))
							queue.add(pred);
					}
				}
				prevOutputMapObj.put(node.getId(), currentResult);
				//prevOutputMapObj = new java.util.HashMap<Integer,T1>(outputMapObj);
		}
	}

	public final void worklistPostorderForward(Queue<CFGNode> queue, final BoaAbstractFixP fixp, final Traversal.TraversalKind kind) throws Exception {
		while(!queue.isEmpty()) {
				CFGNode node = queue.remove();
				traverse(node, true);
				boolean curFlag=outputMapObj.containsKey(node.getId());
				boolean fixpFlag = false;
				if(curFlag) {
					boolean prevFlag=prevOutputMapObj.containsKey(node.getId());
					if(curFlag && prevFlag) { 
						fixpFlag = fixp.invoke((T1)outputMapObj.get(node.getId()),(T1)prevOutputMapObj.get(node.getId()));
					}
				}
				if(!fixpFlag) {
					for (CFGNode pred : node.getSuccessorsList()) {
						if(!queue.contains(pred))
							queue.add(pred);
					}
				}
				prevOutputMapObj.put(node.getId(), currentResult);
				//prevOutputMapObj = new java.util.HashMap<Integer,T1>(outputMapObj);
		}
	}

	public final void traverse(final boa.graphs.cfg.CFG cfg, final Traversal.TraversalDirection direction, final Traversal.TraversalKind kind, final BoaAbstractFixP fixp) throws Exception {
		try {
		if(outputMapObj==null) {
				outputMapObj = new java.util.HashMap<Integer,T1>();
		}	
		switch(kind.getNumber()) {
					case 1 :
					case 2 :
					case 3 :
					case 6 :
					case 7 :
						boolean fixpFlag;
						do {
							prevOutputMapObj = new java.util.HashMap<Integer,T1>(outputMapObj);
							traverse(cfg, direction, kind);
							fixpFlag=true;
							java.util.HashSet<CFGNode> nl=cfg.getNodes();
							for(CFGNode node : nl) {
								boolean curFlag=outputMapObj.containsKey(node.getId());
								boolean prevFlag=prevOutputMapObj.containsKey(node.getId());
								if(curFlag) {
									if(outputMapObj.containsKey(node.getId()) && prevOutputMapObj.containsKey(node.getId())) { 
										fixpFlag = fixpFlag && fixp.invoke((T1)outputMapObj.get(node.getId()),(T1)prevOutputMapObj.get(node.getId()));
									} else {
										fixpFlag = false; break;
									}
								}
							}
						}while(!fixpFlag);
						break;
					case 4:
					case 5:
						prevOutputMapObj = new java.util.HashMap<Integer,T1>();
						traverseWithFixp(cfg, direction, kind, fixp);
						break;
					default : break;
		}
		} catch(Exception e) {return;}

	}

	public final void traverseWithFixp(final CFG cfg, final Traversal.TraversalDirection direction, final Traversal.TraversalKind kind, final BoaAbstractFixP fixp) throws Exception {
		if (preTraverse(cfg)) {			
			if(outputMapObj==null) {
				outputMapObj = new java.util.HashMap<Integer,T1>();
			}
			if(cfg.getNodes().size() != 0) {
				java.util.HashMap<Integer,String> nodeVisitStatus=new java.util.HashMap<Integer,String>();
				for(CFGNode nl : cfg.getNodes()) {
					nodeVisitStatus.put(nl.getId(),"unvisited");
				}
				switch(kind.getNumber()) {
					case 4 :
						Queue<CFGNode> queue=new LinkedList<CFGNode>();
						populateWithPostorder(cfg.getEntryNode(), nodeVisitStatus, queue);
						switch(direction.getNumber()) {
							case 1: 
								worklistPostorderBackward(queue, fixp, kind);
								break;

							case 2 : 
								worklistPostorderForward(queue, fixp, kind);
								break;
							default : break;
						}
						break;
					case 5 :
						Stack<CFGNode> stack=new Stack<CFGNode>();
						populateWithReversePostorder(cfg.getEntryNode(), nodeVisitStatus, stack);
						switch(direction.getNumber()) {
							case 1: 
								worklistReversePostorderBackward(stack, fixp, kind);
								break;

							case 2 : 
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
				if(outputMapObj==null) {
					outputMapObj = new java.util.HashMap<Integer,T1>();
				}
				if(cfg.getNodes().size()!=0) {
					java.util.HashMap<Integer,String> nodeVisitStatus=new java.util.HashMap<Integer,String>();
					CFGNode[] nl = cfg.sortNodes();
					for(int i=0;i<nl.length;i++) {
						nodeVisitStatus.put(nl[i].getId(),"unvisited");
					}
					switch(kind.getNumber()) {
						case 1:
							if(direction.getNumber()==2) {
								dfsForward(cfg.getEntryNode(), nodeVisitStatus);
							}
							else {
								dfsBackward(cfg.getExitNode(), nodeVisitStatus);
							}
							break;
						case 2:
							postorderBackward(cfg.getEntryNode(), nodeVisitStatus);						
							break;
						case 3:
							postorderForward(cfg.getExitNode(), nodeVisitStatus);						
							break;
						case 4:
							if(direction.getNumber()==2) {
								Stack<CFGNode> stack=new Stack<CFGNode>();
								populateWithReversePostorder(cfg.getEntryNode(), nodeVisitStatus, stack);
								worklistReversePostorderWithoutFixp(stack, kind);
							}
							else {
								Queue<CFGNode> queue=new LinkedList<CFGNode>();
								populateWithPostorder(cfg.getEntryNode(), nodeVisitStatus, queue);
								worklistPostorderWithoutFixp(queue, kind);
							}
							break;
						case 5:
							if(direction.getNumber()==2) {
								Queue<CFGNode> queue=new LinkedList<CFGNode>();
								populateWithPostorder(cfg.getEntryNode(), nodeVisitStatus, queue);
								worklistPostorderWithoutFixp(queue, kind);
							}
							else {
								Stack<CFGNode> stack=new Stack<CFGNode>();
								populateWithReversePostorder(cfg.getEntryNode(), nodeVisitStatus, stack);
								worklistReversePostorderWithoutFixp(stack, kind);
							}
							break;
						case 6:
							if(direction.getNumber()==2) {
								for(int i=0;i<nl.length;i++) {
									traverse(nl[i], false);					
								}
							}
							else {
								for(int i=nl.length-1;i>=0;i--) {
									traverse(nl[i], false);					
								}
							}
							break;
						case 7:
							for(CFGNode n : cfg.getNodes()) {
								traverse(n, false);					
							}
							break;
						default:break;
					}
				}
			}
		} catch(Exception e) {return;}
	}

	public void traverse(final CFGNode node, boolean flag) throws Exception {
	}
}
