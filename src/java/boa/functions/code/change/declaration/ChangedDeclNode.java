package boa.functions.code.change.declaration;

import java.util.ArrayList;
import java.util.List;

import boa.functions.code.change.ChangedASTNode;
import boa.functions.code.change.field.ChangedFieldNode;
import boa.functions.code.change.file.ChangedFileNode;
import boa.functions.code.change.method.ChangedMethodNode;
import boa.types.Shared.ChangeKind;

public class ChangedDeclNode extends ChangedASTNode {

	private ChangedFileNode fn;
	private ChangedDeclLocation loc;
	private List<ChangedDeclLocation> prevLocs = new ArrayList<ChangedDeclLocation>();

	// changes
	private List<ChangedMethodNode> methodChanges = new ArrayList<ChangedMethodNode>();
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

	public ChangedMethodNode getNewMethodNode(String signature, ChangeKind change) {
		ChangedMethodNode methodNode = new ChangedMethodNode(signature, this, methodChanges.size());
		methodNode.getChanges().add(change);
		return methodNode;
	}

	public ChangedFieldNode getNewFieldNode(String signature, ChangeKind change) {
		ChangedFieldNode fieldNode = new ChangedFieldNode(signature, this, fieldChanges.size());
		fieldNode.getChanges().add(change);
		return fieldNode;
	}

	public ChangedFileNode getFileNode() {
		return fn;
	}

	public ChangedDeclLocation getLoc() {
		return loc;
	}

	public List<ChangedDeclLocation> getPrevLocs() {
		return prevLocs;
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
		return fn + " " + loc.getIdx() + " " + signature;
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

}