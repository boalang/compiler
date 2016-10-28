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
		//if(outputMapObj.get(node.getId())!=null)
		//	return (T1) outputMapObj.get(node.getId()).getClass().getMethod("clone").invoke(outputMapObj.get(node.getId()));
		//return null;
		//return ((T1)outputMapObj.get(node.getId())).getClass().getMethod("clone").invoke(((T1)outputMapObj.get(node.getId())));
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

	public final void traverse(final CFG cfg, final Traversal.TraversalKind kind, final String str) throws Exception {
		traverse(cfg, kind);
	}

	public final void dfs(final CFGNode node, java.util.HashMap<Integer,String> nodeVisitStatus, final Traversal.TraversalKind kind) throws Exception {
		traverse(node, false);
		nodeVisitStatus.put(node.getId(),"visited");
			for (CFGNode succ : node.getSuccessorsList()) {
			    if (nodeVisitStatus.get(succ.getId()).equals("unvisited")) {
				dfs(succ, nodeVisitStatus, kind);
			    }
			}
	}

	public final void postorder(final CFGNode node, java.util.HashMap<Integer,String> nodeVisitStatus) throws Exception {
		nodeVisitStatus.put(node.getId(),"visited");
		for (CFGNode succ : node.getSuccessorsList()) {
		    if (nodeVisitStatus.get(succ.getId()).equals("unvisited")) {
			postorder(succ, nodeVisitStatus);
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

	public final void reversePostorder(final CFGNode node, java.util.HashMap<Integer,String> nodeVisitStatus) throws Exception {
		nodeVisitStatus.put(node.getId(),"visited");
		for (CFGNode pred : node.getPredecessorsList()) {
		    if (nodeVisitStatus.get(pred.getId()).equals("unvisited")) {
			reversePostorder(pred, nodeVisitStatus);
		    }
		}
		traverse(node, false);
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
				boolean curFlag=true;
				boolean fixp_flag = false;
				if(curFlag) {
					boolean prevFlag=true;
					if(curFlag && prevFlag) { 
						fixp_flag = true;
					}
				}
				if(!fixp_flag) {
					for (CFGNode succ : node.getSuccessorsList()) {
						if(!stack.contains(succ))
							stack.push(succ);
					}
				}
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

	public final void traverse(final boa.graphs.cfg.CFG cfg, final Traversal.TraversalKind kind, final BoaAbstractFixP fixp) throws Exception {
		if(outputMapObj==null) {
				outputMapObj = new java.util.HashMap<Integer,T1>();
		}
		switch(kind.getNumber()) {
					case 6 :
					case 7 :
						boolean fixp_flag;
						do {
							prevOutputMapObj = new java.util.HashMap<Integer,T1>(outputMapObj);
							traverse(cfg, kind);
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
					case 9:
					case 8:
					case 5:
						prevOutputMapObj = new java.util.HashMap<Integer,T1>();
						traverseWithFixp(cfg, kind, fixp);
						break;
					default : break;
		}
	}

	public final void traverseWithFixp(final CFG cfg, final Traversal.TraversalKind kind, final BoaAbstractFixP fixp) throws Exception {
		if (preTraverse(cfg)) {			
			if(outputMapObj==null) {
				outputMapObj = new java.util.HashMap<Integer,T1>();
			}
			java.util.HashMap<Integer,String> nodeVisitStatus=new java.util.HashMap<Integer,String>();
			CFGNode[] nl = cfg.sortNodes();
			for(int i=0;i<nl.length;i++) {
				nodeVisitStatus.put(nl[i].getId(),"unvisited");
			}
			if(nl.length != 0) {
				switch(kind.getNumber()) {
					//forward
					/*case 8:
						Stack<CFGNode> stack1=new Stack<CFGNode>();
						reversePostorderStack(cfg.getEntryNode(), nodeVisitStatus, stack1);
						worklistStack(stack1, fixp, kind);
						break;*/
					//backward
					case 8:
						Queue<CFGNode> queue=new LinkedList<CFGNode>();
						postorderQueue(cfg.getEntryNode(), nodeVisitStatus, queue);
						worklist(queue, fixp, kind);
						break;
					
						/*//Forward
					case 5:
						if(cfg.getIsLoopPresent()) {
							Stack<CFGNode> stack=new Stack<CFGNode>();
							reversePostorderStack(cfg.getEntryNode(), nodeVisitStatus, stack);
							worklistStack(stack, fixp, kind);
						}
						else if(cfg.getIsBranchPresent()) {
							reversePostorder(cfg.getExitNode(), nodeVisitStatus);
						}
						else {
							for(int i=0;i<nl.length;i++) {
								traverse(nl[i], false);
							}
						}
						break;
					*/

						//backward
						case 5:
						if(cfg.getIsLoopPresent()) {
							Queue<CFGNode> queue1=new LinkedList<CFGNode>();
							postorderQueue(cfg.getEntryNode(), nodeVisitStatus, queue1);
							worklist(queue1, fixp, kind);
						}
						else if(cfg.getIsBranchPresent()) {
							postorder(cfg.getEntryNode(), nodeVisitStatus);
						}
						else {
							for(int i=nl.length-1;i>=0;i--) {
								traverse(nl[i], false);
							}
						}
						break;
						//Forward
					/*case 9:
						if(cfg.getIsLoopPresent()) {
							Stack<CFGNode> stack=new Stack<CFGNode>();
							reversePostorderStack(cfg.getEntryNode(), nodeVisitStatus, stack);
							while(!stack.isEmpty()) {
								traverse(stack.pop(), false);
							}

						}
						else if(cfg.getIsBranchPresent()) {
							reversePostorder(cfg.getExitNode(), nodeVisitStatus);
						}
						else {
							for(int i=0;i<nl.length;i++) {
								traverse(nl[i], false);
							}
						}
						break;
					*/

						//backward
						case 9:
						if(cfg.getIsLoopPresent()) {
							Queue<CFGNode> queue1=new LinkedList<CFGNode>();
							postorderQueue(cfg.getEntryNode(), nodeVisitStatus, queue1);
							while(!queue1.isEmpty()) {
								traverse(queue1.remove(), false);
							}
						}
						else if(cfg.getIsBranchPresent()) {
							postorder(cfg.getEntryNode(), nodeVisitStatus);
						}
						else {
							for(int i=nl.length-1;i>=0;i--) {
								traverse(nl[i], false);
							}
						}
						break;
					case 2:
						Stack<CFGNode> stack3=new Stack<CFGNode>();
						reversePostorderStack(cfg.getEntryNode(), nodeVisitStatus, stack3);
						worklistStack(stack3, fixp, kind);
						break;
					default:break;
				}
			}		
		}
	}

	public final void traverse(final boa.graphs.cfg.CFG cfg, final Traversal.TraversalKind kind) throws Exception {
		if (preTraverse(cfg)) {			
			if(outputMapObj==null) {
				outputMapObj = new java.util.HashMap<Integer,T1>();
			}
			java.util.HashMap<Integer,String> nodeVisitStatus=new java.util.HashMap<Integer,String>();
			CFGNode[] nl = cfg.sortNodes();
			//java.util.ArrayList<CFGNode> nl=new java.util.ArrayList<CFGNode>(java.util.Arrays.asList(cfg.sortNodes()));
			for(int i=0;i<nl.length;i++) {
				nodeVisitStatus.put(nl[i].getId(),"unvisited");
			}
			switch(kind.getNumber()) {
				case 6:
					if(nl.length!=0) {
						dfs(cfg.getExitNode(), nodeVisitStatus, kind);
						//dfs(cfg.getEntryNode(), nodeVisitStatus, kind);
					}
					break;
				case 7:
					if(nl.length!=0) {
						//reversePostorder(cfg.getExitNode(), nodeVisitStatus);
						postorder(cfg.getEntryNode(), nodeVisitStatus);
					}
					break;
				case 2:
					if(nl.length!=0) {
						dfs(cfg.getExitNode(), nodeVisitStatus, kind);
					}
					break;
				case 3:
					for(CFGNode cfgnode:nl) {
						traverse(cfgnode, false);
						if(cfgnode.getStmt()!=null) {
							traverse(cfgnode.getStmt());
						}
						if(cfgnode.getExpr()!=null) {
							traverse(cfgnode.getExpr());
						}
					}
					break;
				case 8:
					if(nl.length!=0) {
					Stack<CFGNode> stack1=new Stack<CFGNode>();
					reversePostorderStack(cfg.getEntryNode(), nodeVisitStatus, stack1);
					worklistwithoutfixp(stack1, kind);
					}
					break;
				case 4:
					for(CFGNode cfgnode:nl) {
						traverse(cfgnode, false);
					}
					break;
				case 5:
					if(nl.length!=0) {
						if(cfg.getIsBranchPresent() || cfg.getIsLoopPresent()) {
							reversePostorder(cfg.getExitNode(), nodeVisitStatus);
						}
						else {
							for(int i=0;i<nl.length;i++) {
								traverse(nl[i], false);
							}
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
