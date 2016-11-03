/*
 * Copyright 2015, Anthony Urso, Hridesh Rajan, Robert Dyer,
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
package boa.compiler.visitors;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.stringtemplate.v4.ST;

import boa.aggregators.AggregatorSpec;
import boa.compiler.SymbolTable;
import boa.compiler.TypeCheckException;
import boa.compiler.ast.*;
import boa.compiler.ast.expressions.*;
import boa.compiler.ast.literals.*;
import boa.compiler.ast.statements.*;
import boa.compiler.ast.types.*;
import boa.types.*;

public class CodeAnalysis {
	boolean isInsideTraversalBlock = false;
	boolean isLoopSensitive = false;
	boolean isOrderSensitive = false;
	boolean isIntersectionPresent = false;
	boolean isUnionPresent = false;
	boolean variableMonitor = false;
	String traversalNodeIdentifier="";
	HashSet<String> variableMonitored = new HashSet<String>();
	HashSet<String> variableAffectedByUnion = new HashSet<String>();
	HashSet<String> variableAffectedByIntersection = new HashSet<String>();
	HashSet<String> variableAffectedByLoop = new HashSet<String>();
	HashSet<String> aliasOfTraversalNodeIdentifier = new HashSet<String>();
	ArrayList<String> globalVariables = new ArrayList<String>();
	String lastSeenGlobalVariable = null;
	Stack<String> identifierStack = new Stack<String>();

	public void loopAnalysis(String funcName, String str) {
		//checking union merge operation
		if(str.contains("BoaGraphIntrinsics.union") || str.contains("BoaGraphIntrinsics.union1")) {
			for(String var : variableMonitored) {
				if(str.contains(var)) {
					if(isInsideTraversalBlock) {
						variableAffectedByUnion.add(identifierStack.peek().split("\\.")[0]);
						//System.out.println("Union** "+variableAffectedByUnion);
						isUnionPresent = true;
						break;
					}
				}
			}
		}
		//checking intersection merge operation
		if(str.contains("BoaGraphIntrinsics.intersection") || str.contains("BoaGraphIntrinsics.intersection1")) {
			for(String var : variableMonitored) {
				if(str.contains(var)) {
					if(isInsideTraversalBlock) {
						variableAffectedByIntersection.add(identifierStack.peek().split("\\.")[0]);
						//System.out.println("intersection** "+variableAffectedByIntersection);
						isIntersectionPresent = true;
						break;
					}
				}
			}
		}

		//checking killed variable
		if(funcName.equals("remove") || funcName.equals("removeAll")) {
			for(String var : variableAffectedByIntersection) {
				if(str.contains(var)) {
					if(isInsideTraversalBlock && isIntersectionPresent) {
						variableAffectedByLoop.add(var);
					}
				}
			}
		}
		//checking generated variable
		if(funcName.equals("union") || funcName.equals("add")) {
			for(String var : variableAffectedByUnion) {
				if(str.contains(var)) {
					if(isInsideTraversalBlock && isUnionPresent) {
						variableAffectedByLoop.add(var);
					}
				}
			}
		}
		//alias
		if(funcName.equals("clone")) {
			if(str.contains(traversalNodeIdentifier)) {
				if(isInsideTraversalBlock) {
					aliasOfTraversalNodeIdentifier.add(identifierStack.peek().split("\\.")[0]);
				}
			}
			else
				aliasOfTraversalNodeIdentifier.remove(identifierStack.peek().split("\\.")[0]);				
			for(String var : variableMonitored) {
				if(str.contains(var)) {
					if(isInsideTraversalBlock) {
						variableMonitored.add(identifierStack.peek().split("\\.")[0]);
						//System.out.println(variableMonitored);
						break;
					}
				}
			}
		}
	}

	public void loopSensitivityDetection(String str) {
		//System.out.println("loop** "+variableAffectedByLoop);
		for(String var: variableAffectedByLoop) {
			if(str.contains(var)) {
				isLoopSensitive = true;
			}
		}
	}

	public void aliasAnalysis(String lhs, String rhs) {
		// alias to traversalNodeIdentifier
		if(rhs.equals(traversalNodeIdentifier) || rhs.equals("___"+traversalNodeIdentifier)) {
			if(isInsideTraversalBlock) {
				aliasOfTraversalNodeIdentifier.add(lhs.split("\\.")[0]);
			}
		}
		else
			aliasOfTraversalNodeIdentifier.remove(lhs.split("\\.")[0]);			
		// alias to variableMonitored
		for(String var : variableMonitored) {
			if(rhs.equals(var) || rhs.equals("___"+var)) {
				if(isInsideTraversalBlock) {
					//System.out.println("lhs : "+lhs);
					variableMonitored.add(lhs.split("\\.")[0]);
					return;
				}
			}
		}
		variableMonitored.remove(lhs.split("\\.")[0]);
	}

	public void orderSensitivityDetection(String str){
		aliasOfTraversalNodeIdentifier.add(traversalNodeIdentifier);
		if(isInsideTraversalBlock) {
			if(lastSeenGlobalVariable!=null) {
				for(String var : aliasOfTraversalNodeIdentifier) {
					if(str.contains(var) && str.contains(lastSeenGlobalVariable) && !str.contains("getValue")) {
						isOrderSensitive = true;
						break;
					}
				}
			}
		}
	}

	public void monitorLastSeenGlobalVariable(String str, BoaType type) {
		if(isInsideTraversalBlock) {
			if(globalVariables.contains(str)) {
				if(type.toString().contains("stack of") || type.toString().contains("array of")) {
					lastSeenGlobalVariable = str;
				}
			}
		}
	}

	public void clear() {
		variableMonitored.clear();
		identifierStack.clear();
		aliasOfTraversalNodeIdentifier.clear();
		variableAffectedByLoop.clear();
		variableAffectedByUnion.clear();
		variableAffectedByIntersection.clear();
		isInsideTraversalBlock = false;
	}

	public void reset() {
		isLoopSensitive = false;
		isOrderSensitive = false;
		isIntersectionPresent = false;
		isUnionPresent = false;
	}
}
