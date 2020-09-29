package boa.datagen.treed.generic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.dltk.ast.ASTNode;
import org.eclipse.dltk.ast.ASTVisitor;

import boa.runtime.BoaAbstractVisitor;
import boa.types.Ast.Declaration;
import boa.types.Ast.Expression;
import boa.types.Ast.Method;
import boa.types.Ast.Namespace;
import boa.types.Ast.Statement;
import boa.types.Ast.Type;
import boa.types.Ast.Variable;

public class TreedBuilder implements TreedConstants {
	private int index = 1;
	private Object root;
	int type = 1;
	
	HashMap<Object, Object> treeParent=new HashMap<Object, Object>();
	HashMap<String, Integer> nodeTypes = new HashMap<>();
	// tree records children for every ast node: node -> childs
	HashMap<String, ArrayList<Object>> tree = new HashMap<String, ArrayList<Object>>();
	HashMap<String, Integer> treeHeight = new HashMap<String, Integer>(), treeDepth = new HashMap<String, Integer>();
	// treeVector[node]: represents whole subtree rooted at node, including current
	// root of the
	// subtree and all descendants. on the other hand, treeRootVector represents
	// direct childs of current node only including itself
	HashMap<String, HashMap<String, Integer>> treeVector = new HashMap<String, HashMap<String, Integer>>(),
			treeRootVector = new HashMap<String, HashMap<String, Integer>>();
	HashMap<String, Integer> propertyIndex = new HashMap<String, Integer>();

	public void clear() {
		this.nodeTypes.clear();
		this.tree.clear();
		this.treeHeight.clear();
		this.treeDepth.clear();
		this.treeVector.clear();
		this.treeParent.clear();
	}

	public TreedBuilder(Object root, int type, HashMap<String, Integer> nodeTypes) {
		super();
		this.root = root;
		this.type = type;
		this.nodeTypes = nodeTypes;
		treeDepth.put(Integer.toHexString(root.hashCode()), 0);
	}

	void preVisitCommonProperty(Object node, Object parent) {
		
		treeParent.put(node,  parent);
		
		propertyIndex.put(Integer.toHexString(node.hashCode()), index++);

		String nodeName = TreedUtils.getNodeType(node);
		if (!nodeTypes.containsKey(nodeName))
			nodeTypes.put(nodeName, type++);

		tree.put(Integer.toHexString(node.hashCode()), new ArrayList<Object>());

		if (parent == null)
			treeDepth.put(Integer.toHexString(node.hashCode()), 0);
		else
			treeDepth.put(Integer.toHexString(node.hashCode()), treeDepth.get(Integer.toHexString(parent.hashCode())) + 1);
	}

	void visitVariables(List<Variable> lst, Object parent) {
		for (Variable st : lst) {
			visit(st, parent);
		}
	}

	void visitStatements(List<Statement> lst, Object parent) {
		for (Statement st : lst) {
			visit(st, parent);
		}
	}

	void visitDeclarations(List<Declaration> lst, Object parent) {
		for (Declaration st : lst) {
			visit(st, parent);
		}
	}

	void visitMethods(List<Method> lst, Object parent) {
		for (Method st : lst) {
			visit(st, parent);
		}
	}

	void visitTypes(List<Type> lst, Object parent) {
		for (Type st : lst) {
			visit(st, parent);
		}
	}

	void visitExpressions(List<Expression> lst, Object parent) {
		for (Expression st : lst) {
			visit(st, parent);
		}
	}
	protected void visit(Namespace node, Object parent) {

		preVisitCommonProperty(node, parent);

		visitVariables(node.getVariablesList(), node);
		visitStatements(node.getStatementsList(), node);
		visitDeclarations(node.getDeclarationsList(), node);
		visitMethods(node.getMethodsList(), node);

		commonPostVisit(node, parent);
	}

	void visit(Variable node, Object parent) {
		preVisitCommonProperty(node, parent);
		
		if(node.getComputedName()!=null)
			visit(node.getComputedName(), node);
		
		if(node.getInitializer()!=null)
			visit(node.getInitializer(), node);
		
		commonPostVisit(node, parent);

	}

	void visit(Statement node, Object parent) {
		preVisitCommonProperty(node, parent);

		visitVariables(node.getVariableDeclarationsList(), node);
		visitExpressions(node.getExpressionsList(), node);
		visitExpressions(node.getConditionsList(), node);
		visitStatements(node.getStatementsList(), node);
		visitDeclarations(node.getTypeDeclarationsList(), node);
		visitMethods(node.getMethodsList(), node);

		commonPostVisit(node, parent);

	}

	void visit(Declaration node, Object parent) {
		preVisitCommonProperty(node, parent);

		visitTypes(node.getParentsList(), node);
		visitStatements(node.getStatementsList(), node);
		visitDeclarations(node.getNestedDeclarationsList(), node);
		visitMethods(node.getMethodsList(), node);

		commonPostVisit(node, parent);
	}

	void visit(Method node, Object parent) {
		preVisitCommonProperty(node, parent);

		visitVariables(node.getArgumentsList(), node);
		visitStatements(node.getStatementsList(), node);
		
		commonPostVisit(node, parent);

	}

	void visit(Type node, Object parent) {
		preVisitCommonProperty(node, parent);
		
		if(node.getComputedName()!=null)
			visit(node.getComputedName(), node);
		
		commonPostVisit(node, parent);

	}

	void visit(Expression node, Object parent) {
		preVisitCommonProperty(node, parent);

		visitVariables(node.getVariableDeclsList(), node);
		visitExpressions(node.getExpressionsList(), node);
		visitStatements(node.getStatementsList(), node);
		visitExpressions(node.getMethodArgsList(), node);
		visitMethods(node.getMethodsList(), node);
		
		commonPostVisit(node, parent);
	}


	public void commonPostVisit(Object node, Object parent) {
		buildTree(node, parent);
		buildTreeHeight(node);
		buildVector(node);
	}

	public void buildTree(Object node, Object p) {
		if (node != root) {
			ArrayList<Object> children = tree.get(Integer.toHexString(p.hashCode()));
			children.add(node);
		}
	}

	public void buildTreeHeight(Object node) {
		ArrayList<Object> children = tree.get(Integer.toHexString(node.hashCode()));
		int max = 0;
		for (Object child : children) {
			int h = treeHeight.get(Integer.toHexString(child.hashCode()));
			if (h > max)
				max = h;
		}
		treeHeight.put(Integer.toHexString(node.hashCode()), max + 1);
	}

	private void buildVector(Object node) {
		ArrayList<Object> children = tree.get(Integer.toHexString(node.hashCode()));
		HashMap<String, Integer> vector = new HashMap<String, Integer>(), rootVector = new HashMap<String, Integer>();
		char label = TreedUtils.buildLabelForVector(node, this.nodeTypes);
		String feature = "" + label;
		rootVector.put(feature, 1);
		for (Object child : children) {
			add(vector, treeVector.get(Integer.toHexString(child.hashCode())));
			HashMap<String, Integer> childRootVector = treeRootVector.get(Integer.toHexString(child.hashCode()));
			for (String cf : childRootVector.keySet()) {
				if (cf.length() < GRAM_MAX_LENGTH) {
					String rf = label + cf;
					rootVector.put(rf, childRootVector.get(cf));
				}
			}
			childRootVector.clear();
			treeRootVector.remove(child);
		}
		add(vector, rootVector);
		treeVector.put(Integer.toHexString(node.hashCode()), vector);
		treeRootVector.put(Integer.toHexString(node.hashCode()), rootVector);
	}

	private void add(HashMap<String, Integer> vector, HashMap<String, Integer> other) {
		for (String key : other.keySet()) {
			int c = other.get(key);
			if (vector.containsKey(key))
				c += vector.get(key);
			vector.put(key, c);
		}
	}
}
