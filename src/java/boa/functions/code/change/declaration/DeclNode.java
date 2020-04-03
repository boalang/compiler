package boa.functions.code.change.declaration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import boa.functions.code.change.ChangedASTNode;
import boa.functions.code.change.field.FieldNode;
import boa.functions.code.change.file.FileNode;
import boa.functions.code.change.method.MethodNode;

public class DeclNode extends ChangedASTNode implements Comparable<DeclNode> {

	private FileNode fn;
	private DeclLocation loc;
	
	// name-based edges
	private DeclNode firstParent;
	private DeclNode secondParent;
	
	// refactoring-based edges
	private List<DeclNode> leftRefDecls = new ArrayList<DeclNode>();

	// ast changes in 
	private HashMap<String, Integer> methodChangeMap = new HashMap<String, Integer>();
	private List<MethodNode> methodChanges = new ArrayList<MethodNode>();
	private HashMap<String, Integer> fieldChangeMap = new HashMap<String, Integer>();
	private List<FieldNode> fieldChanges = new ArrayList<FieldNode>();

	public DeclNode(String fqn, FileNode fn, DeclLocation loc) {
		super(fqn);
		this.fn = fn;
		this.loc = loc;
	}

	public DeclNode(String fqn, FileNode fn, int size) {
		super(fqn);
		this.fn = fn;
		this.loc = new DeclLocation(fn.getLoc(), size);
	}

	public MethodNode updateMethodChange(String signature) {
		if (!methodChangeMap.containsKey(signature)) {
			int idx = methodChanges.size();
			MethodNode methodNode = new MethodNode(signature, this, idx);
			methodChangeMap.put(signature, idx);
			methodChanges.add(methodNode);
			return methodNode;
		}
		return methodChanges.get(methodChangeMap.get(signature));
	}
	
	public MethodNode getMethodChange(String sig) {
		if (methodChangeMap.containsKey(sig))
			return methodChanges.get(methodChangeMap.get(sig));
		return null;
	}

	public FieldNode updateFieldChange(String signature) {
		if (!fieldChangeMap.containsKey(signature)) {
			int idx = fieldChanges.size();
			FieldNode fieldNode = new FieldNode(signature, this, idx);
			fieldChangeMap.put(signature, idx);
			fieldChanges.add(fieldNode);
			return fieldNode;
		}
		return fieldChanges.get(fieldChangeMap.get(signature));
	}
	
	public FieldNode getFieldChange(String sig) {
		if (fieldChangeMap.containsKey(sig))
			return fieldChanges.get(fieldChangeMap.get(sig));
		return null;
	}


	public FileNode getFileNode() {
		return fn;
	}

	public DeclLocation getLoc() {
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
		DeclNode other = (DeclNode) obj;
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

	public List<MethodNode> getMethodChanges() {
		return methodChanges;
	}

	public List<FieldNode> getFieldChanges() {
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

	public DeclNode getFirstParent() {
		return firstParent;
	}

	public void setFirstParent(DeclNode firstParent) {
		this.firstParent = firstParent;
	}

	public DeclNode getSecondParent() {
		return secondParent;
	}

	public void setSecondParent(DeclNode secondParent) {
		this.secondParent = secondParent;
	}

	public HashMap<String, Integer> getMethodChangeMap() {
		return methodChangeMap;
	}

	@Override
	public int compareTo(DeclNode o) {
		return this.loc.compareTo(o.getLoc());
	}

	public List<DeclNode> getLeftRefDecls() {
		return leftRefDecls;
	}

}