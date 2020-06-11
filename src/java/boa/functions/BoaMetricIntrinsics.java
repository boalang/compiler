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

import java.util.HashMap;

import boa.types.Ast.*;

/**
 * Boa domain-specific functions for computing software engineering metrics.
 * 
 * @author rdyer
 */
public class BoaMetricIntrinsics {
	////////////////////////////////
	// Number of Attributes (NOA) //
	////////////////////////////////

	private static class BoaNOAVisitor extends BoaCountingVisitor {
		@Override
		public boolean preVisit(final Declaration node) {
			if (node.getKind() == TypeKind.CLASS)
				count += node.getFieldsCount();
			return true;
		}
	}
	private static BoaNOAVisitor noaVisitor = new BoaNOAVisitor();

	/**
	 * Computes the Number of Attributes (NOA) metric for a node.
	 * 
	 * @param node the node to compute NOA for
	 * @return the NOA value for decl
	 */
	@FunctionSpec(name = "get_metric_noa", returnType = "int", formalParameters = { "Declaration" })
	public static long getMetricNOA(final Declaration node) throws Exception {
		noaVisitor.initialize().visit(node);
		return noaVisitor.count;
	}

	////////////////////////////////
	// Number of Operations (NOO) //
	////////////////////////////////

	private static class BoaNOOVisitor extends BoaCountingVisitor {
		@Override
		public boolean preVisit(final Declaration node) {
			if (node.getKind() == TypeKind.CLASS)
				count += node.getMethodsCount();
    		return true;
		}
	}
	private static BoaNOOVisitor nooVisitor = new BoaNOOVisitor();

	/**
	 * Computes the Number of Operations (NOO) metric for a node.
	 * 
	 * @param node the node to compute NOO for
	 * @return the NOO value for decl
	 */
	@FunctionSpec(name = "get_metric_noo", returnType = "int", formalParameters = { "Declaration" })
	public static long getMetricNOO(final Declaration node) throws Exception {
		nooVisitor.initialize().visit(node);
		return nooVisitor.count;
	}

	////////////////////////////////////
	// Number of Public Methods (NPM) //
	////////////////////////////////////

	private static class BoaNPMVisitor extends BoaCountingVisitor {
		@Override
		public boolean preVisit(final Method node) {
    		if (BoaModifierIntrinsics.hasModifierPublic(node))
    			count++;
    		return true;
		}
	}
	private static BoaNPMVisitor npmVisitor = new BoaNPMVisitor();

	/**
	 * Computes the Number of Public Methods (NPM) metric for a node.
	 * 
	 * @param node the node to compute NPM for
	 * @return the NPM value for decl
	 */
	@FunctionSpec(name = "get_metric_npm", returnType = "int", formalParameters = { "Declaration" })
	public static long getMetricNPM(final Declaration node) throws Exception {
		npmVisitor.initialize().visit(node);
		return npmVisitor.count;
	}

	////////////////////////////////
	// Number of Children (NOC) //
	////////////////////////////////

	private static class BoaNOCVisitor extends BoaCollectingVisitor<String,Long> {
		private String ns;
		@Override
		protected boolean preVisit(Namespace node) throws Exception {
			this.ns = node.getName();
			return super.preVisit(node);
		}
		@Override
		protected boolean preVisit(Declaration node) throws Exception {
			for (final Type t : node.getParentsList()) {
				final String key = ns + "." + t.getName();
				final long val = map.containsKey(key) ? map.get(key) : 0;
				map.put(key, val + 1);
			}
			return super.preVisit(node);
		}
	}
	private static BoaNOCVisitor nocVisitor = new BoaNOCVisitor();

	/**
	 * (Partially) Computes the Number of Children (NOC) metric.
	 * 
	 * @param node the node to compute NOC for
	 * @return a map containing partial computation of the NOC metric
	 */
	@FunctionSpec(name = "get_metric_noc", returnType = "map[string] of int", formalParameters = { "ASTRoot" })
	public static HashMap<String,Long> getMetricNOC(final ASTRoot node) throws Exception {
		nocVisitor.initialize(new HashMap<String,Long>()).visit(node);
		return nocVisitor.map;
	}
}
