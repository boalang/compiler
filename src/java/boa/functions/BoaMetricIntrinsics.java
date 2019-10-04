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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Queue;

import boa.runtime.BoaAbstractVisitor;
import boa.types.Ast.*;
import boa.types.Ast.Expression.ExpressionKind;
import boa.types.Diff.ChangedFile;

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

	/////////////////////////////////////
	// Weighted Methods per Class(WMC) //
	/////////////////////////////////////

	private static class BoaWMCVisitor extends BoaCountingVisitor {
		
		private int methodCC;
		
		private BoaAbstractVisitor visitor = new BoaAbstractVisitor() {
			@Override
			public boolean preVisit(final Method node) throws Exception {
				methodCC = 0;
				for (Statement s : node.getStatementsList()) {
					// check for, do, while, if, case, catch
					switch (s.getKind()) {
						case FOR:
						case DO:
						case WHILE:
						case IF:
						case CASE:
						case CATCH:
							methodCC++;
							break;
						default:
							break;
					}
					visit(s);
				}
				methodCC++;
				return false;
			}
			
			@Override
			public boolean preVisit(final Expression node) {
				// check ||
				if (node.getKind() == ExpressionKind.LOGICAL_OR)
					methodCC++;
				return true;
			}
		};
		
		@Override
		public boolean preVisit(final Declaration node) throws Exception {
			for (Method m : node.getMethodsList()) {
				visitor.visit(m);
				count += methodCC;
			}
			return false;
		}
		
		
	}

	private static BoaWMCVisitor wmcVisitor = new BoaWMCVisitor();

	/**
	 * Compute the complexity of a class as the sum of the McCabe’s cyclomatic complexity of
	 * its methods
	 * 
	 * @param node the node to compute DIT for
	 * @return the WMC value for node
	 */
	@FunctionSpec(name = "get_metric_wmc", returnType = "int", formalParameters = { "Declaration" })
	public static long getMetricWMC(final Declaration node) throws Exception {
		wmcVisitor.initialize().visit(node);
		return wmcVisitor.count;
	}

	/////////////////////////////////////
	// Depth of Inheritance Tree (DIT) //
	/////////////////////////////////////

	private static class BoaDITVisitor extends BoaCollectingVisitor<String, Long> {
		
		private HashSet<String> fqns;
		private HashMap<String, HashSet<String>> fileNameToClassFQNMap;
		private HashMap<String, HashSet<String>> childToParentsMap;
		private HashMap<String, HashSet<String>> parentToChildsMap;
		private HashMap<String, long[]> DITNOCMap;
		
		private BoaAbstractVisitor collector = new BoaAbstractVisitor() {
			
			private String fileName;
			
			@Override
			public boolean preVisit(final ChangedFile node) throws Exception {
				fileNameToClassFQNMap.put(node.getName(), new HashSet<String>());
				fileName = node.getName();
				return true;
			}
			
			@Override
			public boolean preVisit(final Namespace node) throws Exception {
				for (Declaration d : node.getDeclarationsList())
					visit(d);
				return false;
			}
			
			@Override
			public boolean preVisit(final Declaration node) throws Exception {
				String fqn = node.getFullyQualifiedName();
				fqns.add(fqn);
				fileNameToClassFQNMap.get(fileName).add(fqn);
				for (Declaration d : node.getNestedDeclarationsList())
					visit(d);
				return false;
			}
		};
		
		private BoaAbstractVisitor visitor = new BoaAbstractVisitor() {
			
			private HashSet<String> importedPackages;
			private HashSet<String> importedClasses;
			
			private void updateMaps(String child, String parent) {
				if (!childToParentsMap.containsKey(child))
					childToParentsMap.put(child, new HashSet<String>());
				childToParentsMap.get(child).add(parent);
				if (!parentToChildsMap.containsKey(parent))
					parentToChildsMap.put(parent, new HashSet<String>());
				parentToChildsMap.get(parent).add(child);
			}
			
			@Override
			public boolean preVisit(final Namespace node) throws Exception {
				// collect imported packages and classes
				importedPackages = new HashSet<String>();
				importedClasses = new HashSet<String>();
				importedPackages.add(node.getName());
				for (String importName : node.getImportsList()) {
					// import static org.junit.Assert.*
					// import static org.mockito.Mockito.doNothing
					if (importName.startsWith("static ")) {
						String temp = importName.replace("static ", "");
						int idx = temp.lastIndexOf('.');
						temp = idx > 0 ? temp.substring(0, idx) : temp;
						importedClasses.add(temp);
					// import org.societies.android.api.useragent.*
					} else if (importName.endsWith(".*")) {
						importedPackages.add(importName.substring(0, importName.lastIndexOf(".*")));
					// import org.junit.After
					} else {
						importedClasses.add(importName);
					}
				}
				// visit type declarations
				for (Declaration d : node.getDeclarationsList())
					visit(d);
				return false;
			}
			
			@Override
			public boolean preVisit(final Declaration node) throws Exception {
				if (node.getParentsCount() > 0) {
					String child = node.getFullyQualifiedName();
					for (Type t : node.getParentsList()) {
						String parentName = t.getName();
						// implements org.red5.io.object.Output
						if (parentName.contains(".") && !child.equals(parentName)) {
							updateMaps(child, parentName);
						// extends BaseOutput
						} else {
							String parentFQN = null;
							// 1. check imported classes
							for (String importedClass : importedClasses)
								if (importedClass.contains(parentName) && fqns.contains(importedClass) && !child.equals(importedClass)) {
									parentFQN = importedClass;
									break;
								}
							// 2. check imported packages
							if (parentFQN == null) {
								for (String pkg : importedPackages) {
									String tmp = pkg + "." + parentName;
									if (fqns.contains(tmp) && !child.equals(tmp)) {
										parentFQN = tmp;
										break;
									}
								}
							}
							if (parentFQN != null)
								updateMaps(child, parentFQN);
							else
								updateMaps(child, parentName);
						}
					}
				}
				for (Declaration d : node.getNestedDeclarationsList())
					visit(d);
				return false;
			}
			
		};
		
		public void process(ChangedFile[] snapshot) throws Exception {
			fqns = new HashSet<String>();
			childToParentsMap = new HashMap<String, HashSet<String>>();
			parentToChildsMap = new HashMap<String, HashSet<String>>();
			fileNameToClassFQNMap = new HashMap<String, HashSet<String>>();
			DITNOCMap = new HashMap<String, long[]>();
			
			for (ChangedFile cf : snapshot)
				collector.visit(cf);
			for (ChangedFile cf : snapshot)
				visitor.visit(cf);
			
			for (Entry<String, HashSet<String>> entry : fileNameToClassFQNMap.entrySet()) {
				String fileName = entry.getKey();
				for (String fqn : entry.getValue()) {
					long dit = getMaxDepth(fqn);
					long noc = parentToChildsMap.containsKey(fqn) ? parentToChildsMap.get(fqn).size() : 0;
					DITNOCMap.put(fileName + " " + fqn, new long[] { dit, noc });
				}
			}
			fqns.clear();
			childToParentsMap.clear();
			parentToChildsMap.clear();
			fileNameToClassFQNMap.clear();
		}

		private long getMaxDepth(String fqn) {
			Queue<String> q = new LinkedList<String>();
			// avoid cyclic graph
			HashSet<String> visited = new HashSet<String>();
			q.offer(fqn);
			q.offer(null);
			int depth = -1;
			while (!q.isEmpty()) {
				String cur = q.poll();
				if (cur == null) {
					depth++;
					if (q.isEmpty())
						break;
					else
						q.offer(null);
				} else if (childToParentsMap.containsKey(cur)) {
					visited.add(cur);
					for (String parent : childToParentsMap.get(cur)) {
						if (!visited.contains(parent)) {
							q.offer(parent);						
						}
					}
				}
			}
			return depth;
		}
	}

	private static BoaDITVisitor ditVisitor = new BoaDITVisitor();

	/**
	 * Computes the Depth of Inheritance Tree (DIT) metric for a node.
	 * 
	 * @param decls a map mapping class name to its corresponding Declaration object
	 * @return a map mapping class full qualified name to its DIT value
	 */
	@FunctionSpec(name = "get_metric_dit_noc", returnType = "map[string] of array of int", formalParameters = { "array of ChangedFile" })
	public static HashMap<String, long[]> getMetricDITNOC(final ChangedFile[] snapshot) throws Exception {
		ditVisitor.process(snapshot);
		return ditVisitor.DITNOCMap;
	}

	////////////////////////////////
	// Number of Children (NOC) //
	////////////////////////////////

	private static class BoaNOCVisitor extends BoaCollectingVisitor<String, Long> {

		private HashMap<String, Declaration> declMap;
		private HashMap<String, HashSet<String>> parenctChildrenMap;

		private void updateMaps(Declaration node) {
			for (Type parentType : node.getParentsList()) {
				String parent = parentType.getName();
				String child = node.getFullyQualifiedName();
				if (declMap.containsKey(parent)) {
					parent = declMap.get(parent).getFullyQualifiedName();
					if (!parenctChildrenMap.containsKey(parent))
						parenctChildrenMap.put(parent, new HashSet<String>());
					parenctChildrenMap.get(parent).add(child);
				}
			}
		}

		public void process(HashMap<String, Declaration> decls) throws Exception {
			declMap = decls;
			parenctChildrenMap = new HashMap<String, HashSet<String>>();
			for (Declaration node : decls.values())
				updateMaps(node);
			for (Declaration node : decls.values()) {
				String fqn = node.getFullyQualifiedName();
				long noc = parenctChildrenMap.containsKey(fqn) ? parenctChildrenMap.get(fqn).size() : 0;
				map.put(fqn, noc);
			}
			parenctChildrenMap.clear();
		}

	}

	private static BoaNOCVisitor nocVisitor = new BoaNOCVisitor();

	/**
	 * (Partially) Computes the Number of Children (NOC) metric.
	 * 
	 * @param node the node to compute NOC for
	 * @return a map containing partial computation of the NOC metric
	 */
	@FunctionSpec(name = "get_metric_noc", returnType = "map[string] of int", formalParameters = { "map[string] of Declaration" })
	public static HashMap<String, Long> getMetricNOC(final HashMap<String, Declaration> decls) throws Exception {
		nocVisitor.initialize(new HashMap<String, Long>());
		nocVisitor.process(decls);
		return nocVisitor.map;
	}

	////////////////////////////////
	// Response For a Class (RFC) //
	////////////////////////////////

	private static class BoaRFCVisitor extends BoaCountingVisitor {
		
		private HashSet<String> methodSet;
		
		private BoaAbstractVisitor visitor = new BoaAbstractVisitor() {
			@Override
			public boolean preVisit(final Expression node) {
				if (node.getKind() == ExpressionKind.METHODCALL)
					methodSet.add(node.getMethod() + " " + node.getMethodArgsCount());
				if (node.getKind() == ExpressionKind.NEW)
					methodSet.add(node.getNewType().getName() + " " + node.getMethodArgsCount());
				return true;
			}
		};

		@Override
		public boolean preVisit(final Declaration node) throws Exception {
			methodSet = new HashSet<String>();
			for (Variable v : node.getFieldsList())
				visitor.visit(v);
			for (Method m : node.getMethodsList())
				visitor.visit(m);
			count = methodSet.size();
			methodSet.clear();
			return false;
		}

	}

	private static BoaRFCVisitor rfcVisitor = new BoaRFCVisitor();

	/**
	 * Computes the number of distinct methods and constructors invoked by a class 
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
	// Coupling Between Object (CBO) //
	////////////////////////////////////

	private static class BoaCBOVisitor extends BoaCollectingVisitor<String, Long> {

		private HashMap<String, HashSet<String>> fileNameToClassFQNMap;
		private HashSet<String> fqns;
		private HashMap<String, HashSet<String>> references;
		private HashMap<String, HashSet<String>> referenced;
		
		private BoaAbstractVisitor collector = new BoaAbstractVisitor() {
			
			private String fileName;
			
			@Override
			public boolean preVisit(final ChangedFile node) throws Exception {
				fileNameToClassFQNMap.put(node.getName(), new HashSet<String>());
				fileName = node.getName();
				return true;
			}

			@Override
			public boolean preVisit(final Declaration node) throws Exception {
				fileNameToClassFQNMap.get(fileName).add(node.getFullyQualifiedName());
				fqns.add(node.getFullyQualifiedName());
				for (Declaration d : node.getNestedDeclarationsList())
					visit(d);
				return false;
			}
		};
		
		private BoaAbstractVisitor visitor = new BoaAbstractVisitor() {
			private List<Declaration> decls;
			
			@Override
			public boolean preVisit(final Namespace node) throws Exception {
				decls = new ArrayList<Declaration>();
				for (Declaration d : node.getDeclarationsList())
					visit(d);
				
				// collect imported packages and classes
				HashSet<String> importedPackages = new HashSet<String>();
				HashSet<String> importedClasses = new HashSet<String>();
				importedPackages.add(node.getName());
				for (String importName : node.getImportsList()) {
					// import static org.junit.Assert.*
					// import static org.mockito.Mockito.doNothing
					if (importName.startsWith("static ")) {
						String temp = importName.replace("static ", "");
						int idx = temp.lastIndexOf('.');
						temp = idx > 0 ? temp.substring(0, idx) : temp;
						importedClasses.add(temp);
					// import org.societies.android.api.useragent.*
					} else if (importName.endsWith(".*")) {
						importedPackages.add(importName.substring(0, importName.lastIndexOf(".*")));
					// import org.junit.After
					} else {
						importedClasses.add(importName);
					}
				}
				
				
				for (String importedClass : importedClasses)
					if (fqns.contains(importedClass))
						for (Declaration decl : decls)
							updateMaps(decl.getFullyQualifiedName(), importedClass);
				
				// check classes under imported packages
				// TODO
				return false;
			}
			
			@Override
			public boolean preVisit(final Declaration node) throws Exception {
				decls.add(node);
				for (Declaration d : node.getNestedDeclarationsList())
					visit(d);
				return false;
			}

			private void updateMaps(String fqn, String reference) {
				if (!references.containsKey(fqn))
					references.put(fqn, new HashSet<String>());
				references.get(fqn).add(reference);
				if (!referenced.containsKey(reference))
					referenced.put(reference, new HashSet<String>());
				referenced.get(reference).add(fqn);
			}
		};
		
		public void process(ChangedFile[] snapshot) throws Exception {
			fileNameToClassFQNMap = new HashMap<String, HashSet<String>>();
			fqns = new HashSet<String>();
			references = new HashMap<String, HashSet<String>>();
			referenced = new HashMap<String, HashSet<String>>();
			for (ChangedFile cf : snapshot)
				collector.visit(cf);
			for (ChangedFile cf : snapshot)
				visitor.visit(cf);
			for (Entry<String, HashSet<String>> entry : fileNameToClassFQNMap.entrySet()) {
				String fileName = entry.getKey();
				for (String fqn : entry.getValue()) {
					HashSet<String> union = new HashSet<String>();
					if (references.containsKey(fqn))
						union.addAll(references.get(fqn));
					if (referenced.containsKey(fqn))
						union.addAll(referenced.get(fqn));
					map.put(fileName + " " + fqn, (long) union.size());
				}
			}
			fileNameToClassFQNMap.clear();
			fqns.clear();
			references.clear();
			referenced.clear();
		}
		
	}

	private static BoaCBOVisitor cboVisitor = new BoaCBOVisitor();

	/**
	 * Computes the number of classes to which a class is coupled.
	 * 
	 * @param node the node to compute CBO for
	 * @return the CBO value for node
	 */
	@FunctionSpec(name = "get_metric_cbo", returnType = "map[string] of int", formalParameters = { "array of ChangedFile" })
	public static HashMap<String, Long> getMetricCBO(final ChangedFile[] snapshot) throws Exception {
		cboVisitor.initialize(new HashMap<String, Long>());
		cboVisitor.process(snapshot);
		return cboVisitor.map;
	}

	///////////////////////////////////////////
	// Lack of Cohesion in Methods (LCOM) //
	///////////////////////////////////////////

	private static class BoaLCOMVisitor extends BoaCountingVisitor {
		
		private HashSet<String> declarationVars;
		private HashSet<String> methodVars;
		private double numAccesses;
		private double lcom;

		private BoaAbstractVisitor methodVisitor = new BoaAbstractVisitor() {
			@Override
			public boolean preVisit(final Method node) throws Exception {
				for (Statement s : node.getStatementsList())
					visit(s);
				return false;
			}
			
			@Override
			public boolean preVisit(final Variable node) {
				if (declarationVars.contains(node.getName()))
					methodVars.add(node.getName());
				return true;
			}
			
			@Override
			public boolean preVisit(final Expression node) throws Exception {
				if (node.getKind() == ExpressionKind.VARACCESS && declarationVars.contains(node.getVariable()))		
					methodVars.add(node.getVariable());
				return true;
			}
		};

		public double getLCOM() {
			return lcom;
		}

		@Override
		public boolean preVisit(final Declaration node) throws Exception {
			double fieldsCount = node.getFieldsCount();
			double methodsCount = node.getMethodsCount();
			if (fieldsCount == 0 || methodsCount < 2) {
				lcom = 0.0;
			} else {
				declarationVars = new HashSet<String>();
				methodVars = new HashSet<String>();
				numAccesses = 0;
				for (Variable v : node.getFieldsList())
					declarationVars.add(v.getName());
				for (Method m : node.getMethodsList()) {
					methodVisitor.visit(m);
					numAccesses += methodVars.size();
					methodVars.clear();
				}
				lcom = (methodsCount - numAccesses / fieldsCount) / (methodsCount - 1.0);
				declarationVars.clear();
				methodVars.clear();
			}
			return false;
		}

	}

	private static BoaLCOMVisitor lcooVisitor = new BoaLCOMVisitor();

	/**
	 * Computes the Lack of Cohesion in Methods (LCOM) metric for a node.
	 * The higher the pairs of methods in a class sharing at least a field,
	 * the higher its cohesion
	 * 
	 * @param node the node to compute LCOM for
	 * @return the LCOM value for node
	 */
	@FunctionSpec(name = "get_metric_lcom", returnType = "float", formalParameters = { "Declaration" })
	public static double getMetricLCOM(final Declaration node) throws Exception {
		lcooVisitor.initialize().visit(node);
		return lcooVisitor.getLCOM();
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
