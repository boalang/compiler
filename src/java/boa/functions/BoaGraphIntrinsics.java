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

	@FunctionSpec(name = "union", returnType = "set of any", formalParameters = { "set of any","set of any" })
	public static <T> HashSet<T> union(final HashSet<T> set1, final HashSet<T> set2) {
		HashSet<T> result_set=new HashSet<T>();
		for(T i : set1) {
    			result_set.add(i);
		}
		for(T i : set2) {
    			result_set.add(i);
		}
		return result_set;
	}

	@FunctionSpec(name = "intersection", returnType = "set of any", formalParameters = { "set of any","set of any" })
	public static <T> HashSet<T> intersection(final HashSet<T> set1, final HashSet<T> set2) {
		HashSet<T> result_set=new HashSet<T>();		
		for(T i : set2) {
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

	@FunctionSpec(name = "setdifference", returnType = "set of any", formalParameters = { "set of any","set of any" })
	public static <T> HashSet<T> setDifference(final HashSet<T> set1,final HashSet<T> set2) {
		HashSet<T> result_set1=new HashSet<T>(set1);
		HashSet<T> result_set2=new HashSet<T>(set2);
		for(T i : result_set2) {
    			result_set1.remove(i);
		}
		return result_set1;
	}

	@FunctionSpec (name = "difference", returnType = "int", formalParameters = { "set of any","set of any" })
	public static <T> int difference(final HashSet<T> set1,final HashSet<T> set2) {
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
