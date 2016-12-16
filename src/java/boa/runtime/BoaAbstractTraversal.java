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
	public int noOfIterations = 0;
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
	public int getNoOfIterations() {
		return this.noOfIterations;
	}
	/**
	 * Initializes any visitor-specific data before starting a visit.
	 * 
	 * @return itself, to allow method chaining
	 */
	public BoaAbstractTraversal initialize() {
		return this;
	}

	protected boolean defaultPreTraverse() throws Exception {
		return true;
	}

	protected boolean preTraverse(final CFG cfg) throws Exception {
		return defaultPreTraverse();
	}

	protected boolean preTraverse(final Variable node) throws Exception {
		return defaultPreTraverse();
	}
	protected boolean preTraverse(final Statement node) throws Exception {
		return defaultPreTraverse();
	}
	protected boolean preTraverse(final Expression node) throws Exception {
		return defaultPreTraverse();
	}
	protected boolean preTraverse(final ExpressionKind node) throws Exception {
		return defaultPreTraverse();
	}
	protected boolean preTraverse(final ExpressionKind node,final String str) throws Exception {
		return defaultPreTraverse();
	}
	

	protected void defaultPostTraverse() throws Exception { }

	protected void postTraverse(final CFG cfg) throws Exception {
		defaultPostTraverse();
	}
	protected void postTraverse(final CFGNode node) throws Exception {
		defaultPostTraverse();
	}
	protected void postTraverse(final Variable node) throws Exception {
		defaultPostTraverse();
	}
	protected void postTraverse(final Statement node) throws Exception {
		defaultPostTraverse();
	}
	protected void postTraverse(final Expression node) throws Exception {
		defaultPostTraverse();
	}
	protected void postTraverse(final ExpressionKind node) throws Exception {
		defaultPostTraverse();
	}

	public final void traverse(final CFG cfg, final Traversal.TraversalDirection direction, final Traversal.TraversalKind kind, final String str) throws Exception {
		traverse(cfg, direction, kind);
	}

	public final void dfsForward(final CFGNode node, java.util.HashMap<Integer,String> nodeVisitStatus) throws Exception {
		traverse(node, false);
		nodeVisitStatus.put(node.getId(),"visited");
			for (CFGNode succ : node.getSuccessorsList()) {
			    if (nodeVisitStatus.get(succ.getId()).equals("unvisited")) {
				dfsForward(succ, nodeVisitStatus);
			    }
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

	public final void postorderQueue(final CFGNode node, java.util.HashMap<Integer,String> nodeVisitStatus, Queue<CFGNode> queue) throws Exception {
		nodeVisitStatus.put(node.getId(),"visited");
		for (CFGNode succ : node.getSuccessorsList()) {
		    if (nodeVisitStatus.get(succ.getId()).equals("unvisited")) {
			postorderQueue(succ, nodeVisitStatus, queue);
		    }
		}
		queue.offer(node);
	}

	public final void reversePostorderStack(final CFGNode node, java.util.HashMap<Integer,String> nodeVisitStatus, Stack<CFGNode> stack) throws Exception {
		nodeVisitStatus.put(node.getId(),"visited");
		for (CFGNode succ : node.getSuccessorsList()) {
		    if (nodeVisitStatus.get(succ.getId()).equals("unvisited")) {
			reversePostorderStack(succ, nodeVisitStatus, stack);
		    }
		}
		stack.push(node);
	}
	public final void worklistStack(Stack<CFGNode> stack, final BoaAbstractFixP fixp, final Traversal.TraversalKind kind) throws Exception {
		while(!stack.isEmpty()) {
				CFGNode node = stack.pop();
				traverse(node, true);
				boolean curFlag=outputMapObj.containsKey(node.getId());
				boolean fixp_flag = false;
				if(curFlag) {
					boolean prevFlag=prevOutputMapObj.containsKey(node.getId());
					if(curFlag && prevFlag) { 
						fixp_flag = fixp.invoke((T1)outputMapObj.get(node.getId()),(T1)prevOutputMapObj.get(node.getId()));
					}
				}
				if(!fixp_flag) {
					for (CFGNode succ : node.getSuccessorsList()) {
						if(!stack.contains(succ))
							stack.push(succ);
					}
				}
				prevOutputMapObj.put(node.getId(), currentResult);
				//prevOutputMapObj = new java.util.HashMap<Integer,T1>(outputMapObj);
		}
	}

	public final void worklistwithoutfixp(Stack<CFGNode> stack, final Traversal.TraversalKind kind) throws Exception {
		while(!stack.isEmpty()) {
				CFGNode node = stack.pop();
				traverse(node, true);
				//prevOutputMapObj = new java.util.HashMap<Integer,T1>(outputMapObj);
		}
	}

	public final void worklistwithoutfixpqueue(Queue<CFGNode> queue, final Traversal.TraversalKind kind) throws Exception {
		while(!queue.isEmpty()) {
				CFGNode node = queue.remove();
				traverse(node, true);
				//prevOutputMapObj = new java.util.HashMap<Integer,T1>(outputMapObj);
		}
	}

	public final void worklist(Queue<CFGNode> queue, final BoaAbstractFixP fixp, final Traversal.TraversalKind kind) throws Exception {
		while(!queue.isEmpty()) {
				CFGNode node = queue.remove();
				traverse(node, true);
				boolean curFlag=outputMapObj.containsKey(node.getId());
				boolean fixp_flag = false;
				if(curFlag) {
					boolean prevFlag=prevOutputMapObj.containsKey(node.getId());
					if(curFlag && prevFlag) { 
						fixp_flag = fixp.invoke((T1)outputMapObj.get(node.getId()),(T1)prevOutputMapObj.get(node.getId()));
					}
				}
				if(!fixp_flag) {
					for (CFGNode pred : node.getPredecessorsList()) {
						if(!queue.contains(pred))
							queue.add(pred);
					}
				}
				prevOutputMapObj.put(node.getId(), currentResult);
				//prevOutputMapObj = new java.util.HashMap<Integer,T1>(outputMapObj);
		}
	}

	public final void traverse(final boa.graphs.cfg.CFG cfg, final Traversal.TraversalDirection direction, final Traversal.TraversalKind kind, final BoaAbstractFixP fixp) throws Exception {
		if(outputMapObj==null) {
				outputMapObj = new java.util.HashMap<Integer,T1>();
		}
		switch(kind.getNumber()) {
					case 3 :
					case 4 :
					case 6 :
					case 7 :
						boolean fixp_flag;
						do {
							prevOutputMapObj = new java.util.HashMap<Integer,T1>(outputMapObj);
							traverse(cfg, direction, kind);
							fixp_flag=true;
							java.util.HashSet<CFGNode> nl=cfg.getNodes();
							for(CFGNode node : nl) {
								boolean curFlag=outputMapObj.containsKey(node.getId());
								boolean prevFlag=prevOutputMapObj.containsKey(node.getId());
								if(curFlag) {
									if(outputMapObj.containsKey(node.getId()) && prevOutputMapObj.containsKey(node.getId())) { 
										fixp_flag=fixp_flag && fixp.invoke((T1)outputMapObj.get(node.getId()),(T1)prevOutputMapObj.get(node.getId()));
									} else {
										fixp_flag = false; break;
									}
								}
							}
						}while(!fixp_flag);
						break;
					case 8:
						prevOutputMapObj = new java.util.HashMap<Integer,T1>();
						java.util.HashMap<Integer,String> nodeVisitStatus=new java.util.HashMap<Integer,String>();
						CFGNode[] nl = cfg.sortNodes();
						for(int i=0;i<nl.length;i++) {
							nodeVisitStatus.put(nl[i].getId(),"unvisited");
						}
						if(nl.length != 0) {
							if(direction.getNumber()==2) {
								Stack<CFGNode> stack1=new Stack<CFGNode>();
								reversePostorderStack(cfg.getEntryNode(), nodeVisitStatus, stack1);
								worklistStack(stack1, fixp, kind);
							}
							else {
								Queue<CFGNode> queue=new LinkedList<CFGNode>();
								postorderQueue(cfg.getEntryNode(), nodeVisitStatus, queue);
								worklist(queue, fixp, kind);
							}							
						}		
						break;
					default : break;
		}
	}

	public final void traverse(final boa.graphs.cfg.CFG cfg, final Traversal.TraversalDirection direction, final Traversal.TraversalKind kind) throws Exception {
		if (preTraverse(cfg)) {			
			if(outputMapObj==null) {
				outputMapObj = new java.util.HashMap<Integer,T1>();
			}
			java.util.HashMap<Integer,String> nodeVisitStatus=new java.util.HashMap<Integer,String>();
			CFGNode[] nl = cfg.sortNodes();
			for(int i=0;i<nl.length;i++) {
				nodeVisitStatus.put(nl[i].getId(),"unvisited");
			}
			switch(kind.getNumber()) {
				case 6:
					if(nl.length!=0) {
						if(direction.getNumber()==2) {
							dfsForward(cfg.getEntryNode(), nodeVisitStatus);
						}
						else {
							dfsBackward(cfg.getExitNode(), nodeVisitStatus);
						}
					}
					break;
				case 7:
					if(nl.length!=0) {
						if(direction.getNumber()==2) {
							postorderForward(cfg.getExitNode(), nodeVisitStatus);
						}
						else {
							postorderBackward(cfg.getEntryNode(), nodeVisitStatus);
						}						
					}
					break;
				case 3:
					if(direction.getNumber()==2) {
						for(int i=0;i<nl.length;i++) {
							traverse(nl[i], false);
							if(nl[i].getStmt()!=null) {
								traverse(nl[i].getStmt());
							}
							if(nl[i].getExpr()!=null) {
								traverse(nl[i].getExpr());
							}					
						}
					}
					else {
						for(int i=nl.length-1;i>=0;i--) {
							traverse(nl[i], false);
							if(nl[i].getStmt()!=null) {
								traverse(nl[i].getStmt());
							}
							if(nl[i].getExpr()!=null) {
								traverse(nl[i].getExpr());
							}					
						}
					}
					break;
				case 8:
					if(nl.length!=0) {
						if(direction.getNumber()==2) {
							Stack<CFGNode> stack1=new Stack<CFGNode>();
							reversePostorderStack(cfg.getEntryNode(), nodeVisitStatus, stack1);
							worklistwithoutfixp(stack1, kind);
						}
						else {
							Queue<CFGNode> queue=new LinkedList<CFGNode>();
							postorderQueue(cfg.getEntryNode(), nodeVisitStatus, queue);
							worklistwithoutfixpqueue(queue, kind);
						}
					}
					break;
				case 4:
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
				default:break;
			}		
		}
	}

	public void traverse(final CFGNode node, boolean flag) throws Exception {
	}

	public final void traverse(final Variable node) throws Exception {
		if (preTraverse(node)) {
			postTraverse(node);
		}
	}

	public final void traverse(final Statement node) throws Exception {
		if (preTraverse(node)) {
			postTraverse(node);
		}
	}

	public final void traverse(final Expression node) throws Exception {
		if (preTraverse(node)) {
			postTraverse(node);
		}
	}

	public final void traverse(final ExpressionKind node) throws Exception {
		if (preTraverse(node)) {
			postTraverse(node);
		}
	}

	public final void traverse(final ExpressionKind node,String type) throws Exception {
		if (preTraverse(node,type)) {
			postTraverse(node);
		}
	}
}
