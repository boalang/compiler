package boa.functions.ds.type;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import boa.functions.ds.ClassObject;
import boa.functions.ds.ClassTrie;

public class DSDatabase {

	public ClassTrie trie;
	public List<ClassObject> classes;

	// key = type
	public Map<String, LinearListDSNode> allLists;
	public Map<String, UnionDSNode> allUnions;
	public Map<String, DicDSNode> allDics;

	// key = fully qualified name
	public Map<String, ObjectDSNode> allObjects;
	public Map<String, NodeDSNode> allNodes;

	public DataDSNode data;

	public DSDatabase(ClassTrie trie) {
		this.trie = trie;
		this.classes = trie.getAllDecls();
		this.allLists = new HashMap<>();
		this.allUnions = new HashMap<>();
		this.allDics = new HashMap<>();
		this.allObjects = new HashMap<>();
		this.allNodes = new HashMap<>();
		this.data = new DataDSNode();
	}

	public DataDSNode getDataNode() {
		return data;
	}

	public boolean isData(String type) {
		return data.isData(type);
	}

	public void updateDataType(String type) {
		data.update(type);
	}

	public boolean contains(ClassObject obj) {
		String type = obj.decl.getFullyQualifiedName();
		return allObjects.containsKey(type) || allNodes.containsKey(type) || isData(type);
	}

	public void add(NodeDSNode node) {
		allNodes.put(node.getType(), node);
	}

	public void add(ObjectDSNode objNode) {
		allObjects.put(objNode.getType(), objNode);
	}

}
