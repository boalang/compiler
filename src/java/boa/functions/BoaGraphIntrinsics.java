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
package boa.functions;


import boa.graphs.cfg.*;
import boa.types.Ast.Method;
import boa.types.Ast.Statement.StatementKind;
import boa.types.Ast.Expression.ExpressionKind;
import java.util.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import boa.runtime.*;
import boa.types.Graph.*;

/**
 * Boa functions for working with control flow graphs.
 * 
 * @author ganeshau
 * @author rramu
 *
 */
public class BoaGraphIntrinsics {

	public static HashMap<String,String> hashmap=new HashMap<String,String>();

	@FunctionSpec(name = "getcfg", returnType = "CFG", formalParameters = { "Method" })
	public static CFG getcfg(final Method method) {
		hashmap.clear();
		CFG cfg = new CFG(method);
		cfg.astToCFG();	
		return cfg;
	}
/*	
	@FunctionSpec(name = "getcfg1", returnType = "int", formalParameters = { "Method" })
	public static int getcfg1(final Method method) {
		hashmap.clear();
		CFG cfg = new CFG(method);
		cfg.astToCFG();	
		boa.types.Control.CFG new_cfg = cfg.newBuilder().build();
		boa.runtime.BoaAbstractFixP fixp = new boa.runtime.BoaAbstractFixP()
				{
					public boolean invoke1(final HashSet<String> ___curr, final HashSet<String> ___prev) throws Exception {
							if (boa.functions.BoaGraphIntrinsics.difference(___curr, ___prev) == 0l)
							{
								return true;
							}
							return false;

					}

					@Override
					public boolean invoke(Object ___curr, Object ___prev) throws Exception{
							return invoke1((HashSet<String>)___curr, (HashSet<String>)___prev);
					}
				};

		boa.runtime.BoaAbstractTraversal live = new boa.runtime.BoaAbstractTraversal<HashSet<String>>()
				{
					@Override
					public void traverse(final boa.types.Control.CFGNode node) throws Exception {
							HashSet<String> aa=new HashSet<String>();
							aa.add(String.valueOf(Math.random()));
							aa.add(String.valueOf(Math.random()));
							//if(ii!=3) {
								outputMapObj.put(node.getId(), aa);
							//}
							//ii++;
					}

				};
		try {
			live.traverse(new_cfg,Traversal.TraversalKind.BACKWARD,fixp);
		}
		catch(Exception e) {
		}
		return 1;
	}
*/
/*	@FunctionSpec(name = "getOutEdges", returnType = "array of string", formalParameters = { "Method" })
	public static String[] getOutEdges(final Method method) {
		hashmap.clear();
		CFG cfg = new CFG(method);
		cfg.astToCFG();	
		cfg.newBuilder().build();
		HashSet<String> result= new HashSet<String>();
		for(CFGNode node : cfg.getNodes()) {
			result.add(String.valueOf(node.succs.size()));
		}
		String[] resultArr = new String[result.size()];
		resultArr = result.toArray(resultArr);
		return resultArr;
	}
*/
	@FunctionSpec(name = "getedges", returnType = "string", formalParameters = { "CFG" })
	public static String getedges(final boa.types.Control.CFG cfg) {
		java.util.List<boa.types.Control.CFGEdge> edges=cfg.getEdgesList();
		String result="";
		for(int i=0;i<edges.size();i++) {
			if(edges.get(i).getLabel().getNumber()!=1) {
				result+="1";
			}
			else {
				result+="0";
			}
		}
		return result;
	}

	@FunctionSpec(name = "getcfgstring", returnType = "string", formalParameters = { "Method" })
	public static String getcfgstring(final Method method) {
		CFG cfg = new CFG(method);
		cfg.astToCFG();
		
		CFGNode[] mynodes = cfg.sortNodes(cfg);
		String result="";	
		for(CFGNode node:mynodes) {
			result+=node.newBuilder().build().toString()+"\n";
		}
		
		for (CFGNode node : mynodes) {
			for (CFGEdge edge : node.getOutEdges()) {
				CFGNode anoNode = edge.getDest();
				if (!anoNode.getInEdges().contains(edge)) {
					System.err.println("ERRORERRORERRORERRORERRORERROR");
					System.err.println(node.getId() + "-" + anoNode.getId());
				}
				result+="node " + node.getId() + " --> node "+ anoNode.getId() + "\r\n";
			}
		}

		return result;
	}

	@FunctionSpec(name = "getdetails", returnType = "int", formalParameters = { "Method" })
	public static int getdetails(final Method method) {
		CFG cur_cfg = new CFG(method);
		cur_cfg.astToCFG();
		int count=0;
		java.util.HashMap<Integer,String> nodeVisitStatus=new java.util.HashMap<Integer,String>();
		CFGNode[] nl = cur_cfg.sortNodes();
		//java.util.ArrayList<CFGNode> nl=new java.util.ArrayList<CFGNode>(java.util.Arrays.asList(cfg.sortNodes()));
		for(int i=0;i<nl.length;i++) {
			nodeVisitStatus.put(nl[i].getId(),"unvisited");
		}
		Queue<CFGNode> q=new LinkedList<CFGNode>();
		if(nl.length!=0) {
			nodeVisitStatus.put(nl.length-1,"visited");
			CFGNode node = nl[nl.length-1];
			q.add(node);
			//System.out.println(node.getId());
			while(!q.isEmpty()) {
				node=q.peek();
				for(CFGNode pred : node.getInNodes()) {
					if(nodeVisitStatus.get(pred.getId()).equals("unvisited")) {
						//System.out.println(pred.getId());
						nodeVisitStatus.put(pred.getId(),"visited");
						q.add(pred);
					}
				}
				q.remove();
			}
		}

		return count;
	}

	public static final java.util.List<boa.types.Control.CFGNode> sortNodes(final java.util.List<boa.types.Control.CFGNode> nodeList) {
		java.util.List<boa.types.Control.CFGNode> nl=new java.util.ArrayList<boa.types.Control.CFGNode>();
		for(boa.types.Control.CFGNode cn:nodeList) {
			int flag=0;
			if(nl.size()>0) {
				for(int i=0;i<nl.size();i++) {
					if(nl.get(i).getId()>cn.getId()) {
						nl.add(i, cn);
						flag=1;	
						break;			
					}
				}
				if(flag==0) {
				nl.add(cn);
				}
				flag=0;
			}
			else {
				nl.add(cn);			
			}
		}
		return nl;
	}

	/*@FunctionSpec(name = "getPreds", returnType = "set of CFGNode", formalParameters = { "CFGNode" })
	public static HashSet<CFGNode> getPreds(final CFGNode node) {
		return node.getInNodes();		
		//return preds.toArray(new CFGNode[preds.size()]);
	}*/

	/*@FunctionSpec(name = "getSuccs", returnType = "set of CFGNode", formalParameters = {"CFGNode" })
	public static HashSet<CFGNode> getSuccs(final CFGNode node) {
		return node.getOutNodes();		
		//return succs.toArray(new CFGNode[succs.size()]);
	}*/

	@FunctionSpec(name = "union", returnType = "set of string", formalParameters = { "set of string","set of string" })
	public static HashSet<String> union(final HashSet<String> set1,final HashSet<String> set2) {
		HashSet<String> result_set=new HashSet<String>();
		for(String i : set1) {
    			result_set.add(i);
		}
		for(String i : set2) {
    			result_set.add(i);
		}
		return result_set;
	}

	@FunctionSpec(name = "union1", returnType = "set of set of string", formalParameters = { "set of set of string","set of set of string" })
	public static HashSet<HashSet<String>> union1(final HashSet<HashSet<String>> set1,final HashSet<HashSet<String>> set2) {
		HashSet<HashSet<String>> result_set=new HashSet<HashSet<String>>();
		for(HashSet<String> i : set1) {
    			result_set.add(i);
		}
		for(HashSet<String> i : set2) {
    			result_set.add(i);
		}
		return result_set;
	}

	@FunctionSpec(name = "intersection", returnType = "set of string", formalParameters = { "set of string","set of string" })
	public static HashSet<String> intersection(final HashSet<String> set1,final HashSet<String> set2) {
		HashSet<String> result_set=new HashSet<String>();		
		for(String i : set2) {
    			if(set1.contains(i)) {
				result_set.add(i);
			}
		}
		return result_set;
	}

	@FunctionSpec(name = "intersection1", returnType = "set of set of string", formalParameters = { "set of set of string","set of set of string" })
	public static HashSet<HashSet<String>> intersection1(final HashSet<HashSet<String>> set1,final HashSet<HashSet<String>> set2) {
		HashSet<HashSet<String>> result_set=new HashSet<HashSet<String>>();		
		for(HashSet<String> i : set2) {
    			if(set1.contains(i)) {
				result_set.add(i);
			}
		}
		return result_set;
	}

	@FunctionSpec(name = "setClone", returnType = "set of set of string", formalParameters = { "set of set of string"})
	public static HashSet<HashSet<String>> setClone(final HashSet<HashSet<String>> set1) {
		HashSet<HashSet<String>> result_set=new HashSet<HashSet<String>>(set1);
		return result_set;
	}

	@FunctionSpec(name = "setdifference", returnType = "set of string", formalParameters = { "set of string","set of string" })
	public static HashSet<String> setdifference(final HashSet<String> set1,final HashSet<String> set2) {
		HashSet<String> result_set1=new HashSet<String>(set1);
		HashSet<String> result_set2=new HashSet<String>(set2);
		for(String i : result_set2) {
    			result_set1.remove(i);
		}
		return result_set1;
	}

	@FunctionSpec(name = "difference", returnType = "int", formalParameters = { "set of string","set of string" })
	public static int difference(final HashSet<String> set1,final HashSet<String> set2) {
		if(set1.size()==set2.size()) {
			if(set1.containsAll(set2)) {
				return 0;
			}		
		}
		return 1;
	}

	@FunctionSpec(name = "equalStack", returnType = "bool", formalParameters = { "stack of int","stack of int" })
	public static boolean equalStack(final Stack<Long> set1,final Stack<Long> set2) {
		Stack<Long> set11 = (Stack)set1.clone();
		Stack<Long> set22 = (Stack)set2.clone();
		if(set11.size()==set22.size()) {
			while(!(set11.isEmpty())) {
				if(set11.pop() != set22.pop()) {
					return false;
				}
			}
			return true;		
		}
		return false;
	}

	@FunctionSpec(name = "difference2", returnType = "int", formalParameters = { "set of int","set of int" })
	public static int difference2(final HashSet<Long> set1,final HashSet<Long> set2) {
		if(set1.size()==set2.size()) {
			if(set1.containsAll(set2)) {
				return 0;
			}		
		}
		return 1;
	}

	@FunctionSpec(name = "difference1", returnType = "int", formalParameters = { "set of set of string","set of set of string" })
	public static int difference1(final HashSet<HashSet<String>> set1,final HashSet<HashSet<String>> set2) {
		if(set1.size()==set2.size()) {
			if(set1.containsAll(set2)) {
				return 0;
			}		
		}
		return 1;
	}

	@FunctionSpec(name = "getsize", returnType = "int", formalParameters = { "Method" })
	public static int getsize(final Method method) {
		CFG cfg = new CFG(method);
		cfg.astToCFG();
		return cfg.getNodes().size();
	}
	
	@FunctionSpec(name = "getstatement", returnType = "Statement", formalParameters = { "CFGNode" })
	public static boa.types.Ast.Statement getstatement(CFGNode node) {
		return node.getStmt();
	}

	@FunctionSpec(name = "getexpression", returnType = "Expression", formalParameters = { "CFGNode" })
	public static boa.types.Ast.Expression getexpression(CFGNode node) {
		return node.getExpr();
	}

	@FunctionSpec(name = "getstmtkind", returnType = "string", formalParameters = { "StatementKind" })
	public static String getstmtkind(final StatementKind kind) {
		return kind.toString();
	}

	@FunctionSpec(name = "getexprkind", returnType = "string", formalParameters = { "ExpressionKind" })
	public static String getexprkind(final ExpressionKind kind) {
		return kind.toString();
	}

	@FunctionSpec(name = "getnodekind", returnType = "string", formalParameters = { "CFGNodeType" })
	public static String getnodekind(final boa.types.Control.CFGNode.CFGNodeType kind) {
		return kind.toString();
	}

	@FunctionSpec(name = "getvariabledefined", returnType = "set of string", formalParameters = { "CFGNode" })
	public static HashSet<String> getvariabledefined(final boa.types.Control.CFGNode node) {
		HashSet<String> vardef= new HashSet<String>();
		if(node.getExpression()!=null) {
			if(node.getExpression().getKind().toString().equals("VARDECL") || node.getExpression().getKind().toString().equals("ASSIGN")) {
			vardef.add(String.valueOf(node.getId()));
			}		
		}
		return vardef;
	}

	@FunctionSpec(name = "getvariablekilled", returnType = "set of string", formalParameters = {"CFG", "CFGNode" })
	public static HashSet<String> getvariablekilled(final boa.types.Control.CFG cfg,final boa.types.Control.CFGNode node) {
		HashSet<String> varkilled= new HashSet<String>();
		String vardef="";
		if(node.getExpression()!=null) {
			if(node.getExpression().getKind().toString().equals("VARDECL")) {
				vardef=node.getExpression().getVariableDeclsList().get(0).getName();
			}
			else if(node.getExpression().getKind().toString().equals("ASSIGN")) {
				vardef=node.getExpression().getExpressionsList().get(0).getVariable();
			}
			else {
				return varkilled;
			}
			for(boa.types.Control.CFGNode tnode:cfg.getNodesList()) {
				if(tnode.getExpression()!=null && tnode.getId()!=node.getId()) {
					if(tnode.getExpression().getKind().toString().equals("VARDECL")) {
						if(tnode.getExpression().getVariableDeclsList().get(0).getName().equals(vardef)) {
							varkilled.add(String.valueOf(tnode.getId()));
						}			
					}
					else if(tnode.getExpression().getKind().toString().equals("ASSIGN")) {
						if(tnode.getExpression().getExpressionsList().get(0).getVariable().equals(vardef)) {
							varkilled.add(String.valueOf(tnode.getId()));
						}					
					}
				}
			}		
		}
		
		return varkilled;
	}

	@FunctionSpec(name = "getvariabledef", returnType = "set of string", formalParameters = { "CFGNode" })
	public static HashSet<String> getvariabledef(final boa.types.Control.CFGNode node) {
		HashSet<String> vardef= new HashSet<String>();
		if(node.getExpression()!=null) {
			if(node.getExpression().getKind().toString().equals("VARDECL")) {
				vardef.add(node.getExpression().getVariableDeclsList().get(0).getName());			
			}
			if(node.getExpression().getKind().toString().equals("ASSIGN")) {
				vardef.add(node.getExpression().getExpressionsList().get(0).getVariable());
			}		
		}
		return vardef;
	}

	@FunctionSpec(name = "getvariableused", returnType = "set of string", formalParameters = { "CFGNode" })
	public static HashSet<String> getvariableused(final boa.types.Control.CFGNode node) {
		HashSet<String> varused=new HashSet<String>();
		if(node.getExpression()!=null) {
			traverseExpr(varused,node.getExpression());
		}
		return varused;
	}

	public static void traverseExpr(HashSet<String> varused, final boa.types.Ast.Expression expr) {		
		if(expr.getVariable()!=null) {
			varused.add(expr.getVariable());			
		}
		for(boa.types.Ast.Expression exprs:expr.getExpressionsList()) {
			traverseExpr(varused, exprs);
		}
		for(boa.types.Ast.Variable vardecls:expr.getVariableDeclsList()) {
			traverseVarDecls(varused, vardecls);
		}
		for(boa.types.Ast.Expression methodexpr:expr.getMethodArgsList()) {
			traverseExpr(varused, methodexpr);
		}
	}

	public static void traverseVarDecls(HashSet<String> varused, final boa.types.Ast.Variable vardecls) {		
		if(vardecls.getInitializer()!=null) {
			traverseExpr(varused, vardecls.getInitializer());			
		}
	}
}
