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

	///////////////////////////////////////////
	// Lack of Cohesion in Operations (LCOO) //
	///////////////////////////////////////////

	private static class BoaLCOOVisitor extends BoaCountingVisitor {
		// TODO
	}
	private static BoaLCOOVisitor lcooVisitor = new BoaLCOOVisitor();

	/**
	 * Computes the Lack of Cohesion in Operations (LCOO) metric for a node.
	 * 
	 * @param node the node to compute LCOO for
	 * @return the LCOO value for node
	 */
	@FunctionSpec(name = "get_metric_lcoo", returnType = "int", formalParameters = { "Declaration" })
	public static long getMetricLCOO(final Declaration node) throws Exception {
		lcooVisitor.initialize().visit(node);
		return lcooVisitor.count;
	}

	/////////////////////////////////////
	// Depth of Inheritance Tree (DIT) //
	/////////////////////////////////////

	private static class BoaDITVisitor extends BoaCountingVisitor {
		// TODO
	}
	private static BoaDITVisitor ditVisitor = new BoaDITVisitor();

	/**
	 * Computes the Depth of Inheritance Tree (DIT) metric for a node.
	 * 
	 * @param node the node to compute DIT for
	 * @return the DIT value for node
	 */
	@FunctionSpec(name = "get_metric_dit", returnType = "int", formalParameters = { "Declaration" })
	public static long getMetricDIT(final Declaration node) throws Exception {
		ditVisitor.initialize().visit(node);
		return ditVisitor.count;
	}

	////////////////////////////////
	// Response For a Class (RFC) //
	////////////////////////////////

	private static class BoaRFCVisitor extends BoaCountingVisitor {
		// TODO
	}
	private static BoaRFCVisitor rfcVisitor = new BoaRFCVisitor();

	/**
	 * Computes the Response For a Class (RFC) metric for a node.
	 * 
	 * @param node the node to compute RFC for
	 * @return the RFC value for node
	 */
	@FunctionSpec(name = "get_metric_rfc", returnType = "int", formalParameters = { "Declaration" })
	public static long getMetricRFC(final Declaration node) throws Exception {
		rfcVisitor.initialize().visit(node);
		return rfcVisitor.count;
	}

	////////////////////////////////////
	// Coupling Between Classes (CBC) //
	////////////////////////////////////

	private static class BoaCBCVisitor extends BoaCountingVisitor {
		// TODO
	}
	private static BoaCBCVisitor cbcVisitor = new BoaCBCVisitor();

	/**
	 * Computes the Coupling Between Classes (CBC) metric for a node.
	 * 
	 * @param node the node to compute CBC for
	 * @return the CBC value for node
	 */
	@FunctionSpec(name = "get_metric_cbc", returnType = "int", formalParameters = { "Declaration" })
	public static long getMetricCBC(final Declaration node) throws Exception {
		cbcVisitor.initialize().visit(node);
		return cbcVisitor.count;
	}

	////////////////////////////
	// Afferent Coupling (CA) //
	////////////////////////////

	private static class BoaCAVisitor extends BoaCountingVisitor {
		// TODO
	}
	private static BoaCAVisitor caVisitor = new BoaCAVisitor();

	/**
	 * Computes the Afferent Coupling (CA) metric for a node.
	 * 
	 * @param node the node to compute CA for
	 * @return the CA value for node
	 */
	@FunctionSpec(name = "get_metric_ca", returnType = "int", formalParameters = { "Declaration" })
	public static long getMetricCA(final Declaration node) throws Exception {
		caVisitor.initialize().visit(node);
		return caVisitor.count;
	}
}
