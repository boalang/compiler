package boa.functions.code.change.declaration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import boa.functions.code.change.ChangedASTNode;
import boa.functions.code.change.field.ChangedFieldNode;
import boa.functions.code.change.file.ChangedFileNode;
import boa.functions.code.change.method.ChangedMethodNode;

public class ChangedDeclNode extends ChangedASTNode {

	private ChangedFileNode fn;
	private ChangedDeclLocation loc;
	private ChangedDeclNode firstParent;
	private ChangedDeclNode secondParent;

	// changes
	private HashMap<String, Integer> methodChangeMap = new HashMap<String, Integer>();
	private List<ChangedMethodNode> methodChanges = new ArrayList<ChangedMethodNode>();
	private HashMap<String, Integer> fieldChangeMap = new HashMap<String, Integer>();
	private List<ChangedFieldNode> fieldChanges = new ArrayList<ChangedFieldNode>();

	public ChangedDeclNode(String fqn, ChangedFileNode fn, ChangedDeclLocation loc) {
		super(fqn);
		this.fn = fn;
		this.loc = loc;
	}

	public ChangedDeclNode(String fqn, ChangedFileNode fn, int size) {
		super(fqn);
		this.fn = fn;
		this.loc = new ChangedDeclLocation(fn.getLoc(), size);
	}

	public ChangedMethodNode getMethodNode(String signature) {
		if (!methodChangeMap.containsKey(signature)) {
			int idx = methodChanges.size();
			ChangedMethodNode methodNode = new ChangedMethodNode(signature, this, idx);
			methodChangeMap.put(signature, idx);
			methodChanges.add(methodNode);
//			methodDB.put(methodNode.getLoc(), methodNode);
			return methodNode;
		}
		return methodChanges.get(methodChangeMap.get(signature));
	}

	public ChangedFieldNode getFieldNode(String signature) {
		if (!fieldChangeMap.containsKey(signature)) {
			int idx = fieldChanges.size();
			ChangedFieldNode fieldNode = new ChangedFieldNode(signature, this, idx);
			fieldChangeMap.put(signature, idx);
			fieldChanges.add(fieldNode);
//			fieldDB.put(fieldNode.getLoc(), fieldNode);
			return fieldNode;
		}
		return fieldChanges.get(fieldChangeMap.get(signature));
	}

	public ChangedFileNode getFileNode() {
		return fn;
	}

	public ChangedDeclLocation getLoc() {
		return loc;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((loc == null) ? 0 : loc.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ChangedDeclNode other = (ChangedDeclNode) obj;
		if (loc == null) {
			if (other.loc != null)
				return false;
		} else if (!loc.equals(other.loc))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return fn + " " + loc.getIdx() + " " + signature + " " + this.firstChange + " " + this.secondChange;
	}

	public List<ChangedMethodNode> getMethodChanges() {
		return methodChanges;
	}

	public List<ChangedFieldNode> getFieldChanges() {
		return fieldChanges;
	}

	public int getASTChangeCount() {
		return methodChanges.size() + fieldChanges.size();
	}

	public boolean hasFirstParent() {
		return firstParent != null;
	}

	public boolean hasSecondParent() {
		return secondParent != null;
	}

	public ChangedDeclNode getFirstParent() {
		return firstParent;
	}

	public void setFirstParent(ChangedDeclNode firstParent) {
		this.firstParent = firstParent;
	}

	public ChangedDeclNode getSecondParent() {
		return secondParent;
	}

	public void setSecondParent(ChangedDeclNode secondParent) {
		this.secondParent = secondParent;
	}

}