package boa.functions.ds;

import java.util.*;

import boa.runtime.BoaAbstractVisitor;
import boa.types.Ast.*;
import boa.types.Diff.ChangedFile;

import static boa.functions.ds.BoaDSIntrinsics.declFilter;

public class ClassTrie {
	public ClassTrieNode root;

	public ClassTrie() {
		root = new ClassTrieNode();
	}

	public void insert(ClassObject decl) {
		String[] names = decl.decl.getFullyQualifiedName().split("\\.");
		ClassTrieNode cur = root;
		for (int i = 0; i < names.length; i++) {
			String name = names[i];
			cur.children.putIfAbsent(name, new ClassTrieNode());
			cur = cur.children.get(name);
			if (i == names.length - 1) {
				if (cur.decl != null) {
					System.out.println("Err!! duplicate fqn");
					System.out.println(cur.decl.fileName + " " + decl.decl.getFullyQualifiedName());
					System.out.println(decl.fileName + " " + decl.decl.getFullyQualifiedName());
				}
				cur.decl = decl;
			}
		}
	}

	public List<ClassObject> find(String fqn) {
		List<ClassObject> res = new LinkedList<>();
		String[] names = fqn.split("\\.");
		ClassTrieNode cur = root;
		for (int i = 0; i < names.length; i++) {
			String name = names[i];
			if (name.equals("*")) {
				res.addAll(getDecls(cur));
				break;
			}
			if ((cur = cur.children.get(name)) == null)
				break;
			if (i == names.length - 1)
				res.addAll(getDecls(cur));
		}
		return res;
	}

	public List<ClassObject> getAllDecls() {
		return getDecls(root);
	}

	public List<ClassObject> getDecls(ClassTrieNode n) {
		List<ClassObject> res = new LinkedList<>();
		Deque<ClassTrieNode> q = new ArrayDeque<>();
		q.addLast(n);
		while (!q.isEmpty()) {
			ClassTrieNode cur = q.removeFirst();
			for (Map.Entry<String, ClassTrieNode> e : cur.children.entrySet())
				q.addLast(e.getValue());
			if (cur.decl != null)
				res.add(cur.decl);
		}
		return res;
	}

	public void update(ChangedFile cf) throws Exception {
		String fileName = cf.getName();
		new BoaAbstractVisitor() {
			List<String> imports; 
			@Override
			protected boolean preVisit(Namespace ns) throws Exception {
				imports = ns.getImportsList();
				return true;
			}
			@Override
			protected boolean preVisit(Declaration d) throws Exception {
				if (declFilter(d))
					return false;
				insert(new ClassObject(fileName, imports, d));
				for (Declaration next : d.getNestedDeclarationsList())
					visit(next);
				return false;
			}
		}.visit(cf);
	}
}

class ClassTrieNode {
	ClassObject decl;
	Map<String, ClassTrieNode> children;

	public ClassTrieNode() {
		children = new HashMap<>();
	}
}