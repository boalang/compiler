package boa.functions.ds.type;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;

import boa.functions.ds.ClassObject;
import boa.types.Ast.Variable;
import boa.types.Ast.Expression.ExpressionKind;
import boa.types.Ast.Modifier.ModifierKind;

public class DSNodeBuilder {
	public DSDatabase db;
	public ClassObject obj;

	public DSNodeBuilder(DSDatabase db, ClassObject obj) {
		this.db = db;
		this.obj = obj;
	}

	public boolean isNode() {
		for (Variable f : obj.decl.getFieldsList()) {
			String type = f.getVariableType().getName();
			if (getAllGenerics(type).stream().anyMatch(g -> g.equals(obj.decl.getName()))) {
				return true;
			}
		}
		return false;
	}

	public NodeDSNode buildNode() {
		NodeDSNode node = new NodeDSNode(obj.decl.getFullyQualifiedName());
		node.hasData = true;
		db.add(node);
		return node;
	}

	public ObjectDSNode buildObject() {
		ObjectDSNode objNode = new ObjectDSNode(obj.decl.getFullyQualifiedName());
		for (Variable f : obj.decl.getFieldsList()) {
			if (isImmutable(f)) {
				continue;
			}
			String type = f.getVariableType().getName();
			if (db.isData(type)) {
				db.updateDataType(type);
				objNode.hasData = true;
			} else if (type.endsWith("[]")) {
				int idx = type.indexOf("[]");
				updateDicDSNode(objNode, "Integer", type.substring(0, idx));
			} else if (type.indexOf('<') > 0) {
				String prefix = type.substring(0, type.indexOf('<'));
				if (isLinearList(prefix)) {

				} else if (isUnion(prefix)) {

				} else if (isDictionary(prefix)) {

				}
			} else {
				// object
				if (isNode(type)) {

				}
			}
		}
		if (objNode.isObjectDSNodeNode()) {
			db.add(objNode);
		} else if (objNode.isDataDSNode()) {
			db.updateDataType(objNode.getType());
		}
		return objNode;
	}

	private void updateDicDSNode(ObjectDSNode obj, String keyType, String valType) {
		String k = keyType + " " + valType;
		DicDSNode dicNode = null;
		if (db.allDics.containsKey(k)) {
			dicNode = db.allDics.get(k);
		} else {
			DSNode key = getDSNode(keyType);
			DSNode val = getDSNode(valType);
			dicNode = new DicDSNode(key, val);
		}
		obj.dics.add(dicNode);
	}

	private DSNode getDSNode(String type) {
		if (db.isData(type))
			return db.getDataNode();

		return null;
	}

	private boolean isLinearList(String type) {
		List<String> tokens = Arrays.asList("Queue", "Deque", "Stack", "List");
		return tokens.stream().anyMatch(token -> type.indexOf(token) > -1);
	}

	private boolean isUnion(String type) {
		List<String> tokens = Arrays.asList("Set");
		return tokens.stream().anyMatch(token -> type.indexOf(token) > -1);
	}

	private boolean isDictionary(String type) {
		List<String> tokens = Arrays.asList("Map", "Hashtable");
		return tokens.stream().anyMatch(token -> type.indexOf(token) > -1);
	}

	private boolean isNode(String type) {
		// TODO Auto-generated method stub
		return false;
	}

	public static boolean isImmutable(Variable v) {
		if (v.getModifiersList().stream().anyMatch(m -> m.getKind() == ModifierKind.FINAL))
			return true;
		String type = v.getVariableType().getName();
		// String[] options = {"Send Feedback", "Close"} consider as immuatble fields
		if (type.endsWith("[]") && v.hasInitializer() && v.getInitializer().getKind() == ExpressionKind.ARRAYINIT) {
			return true;
		}
		if (type.equals("String"))
			return true;
		return false;
	}

	public static List<String> getAllGenerics(String type) {
		List<String> res = new ArrayList<>();
		Deque<String> q = new ArrayDeque<>();
		q.addLast(type);
		while (!q.isEmpty()) {
			String cur = q.removeFirst();
			if (cur.indexOf('<') > 0) {
				for (String next : getGenerics(cur))
					q.addLast(next);
			} else {
				res.add(cur);
			}
		}
		return res;
	}

	public static List<String> getGenerics(String type) {
		int l = type.indexOf('<'), r = type.lastIndexOf('>');
		String generics = type.substring(l + 1, r);
		int commaIdx = generics.indexOf(',');
		if (commaIdx > 0) {
			String g1 = generics.substring(0, commaIdx);
			String g2 = generics.substring(commaIdx + 2);
			return Arrays.asList(g1, g2);
		}
		return Arrays.asList(generics);
	}
}
