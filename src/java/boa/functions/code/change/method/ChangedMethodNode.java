package boa.functions.code.change.method;

import boa.functions.code.change.ChangedASTNode;
import boa.functions.code.change.declaration.ChangedDeclNode;
import boa.functions.code.change.file.ChangedFileLocation;

public class ChangedMethodNode extends ChangedASTNode {

	private ChangedDeclNode declNode;
	private ChangedMethodLocation loc;
	private ChangedFileLocation firstParent;
	private ChangedFileLocation secondParent;

	public ChangedMethodNode(String sig, ChangedDeclNode declNode, ChangedMethodLocation loc) {
		super(sig);
		this.declNode = declNode;
		this.loc = loc;
	}

	public ChangedMethodNode(String sig, ChangedDeclNode declNode, int size) {
		super(sig);
		this.declNode = declNode;
		this.loc = new ChangedMethodLocation(declNode.getLoc(), size);
	}

	public ChangedDeclNode getDeclNode() {
		return declNode;
	}

	public ChangedMethodLocation getLoc() {
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
		ChangedMethodNode other = (ChangedMethodNode) obj;
		if (loc == null) {
			if (other.loc != null)
				return false;
		} else if (!loc.equals(other.loc))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return declNode + " " + loc.getIdx() + " " + signature;
	}

	public ChangedFileLocation getSecondParent() {
		return secondParent;
	}

	public void setSecondParent(ChangedFileLocation secondParent) {
		this.secondParent = secondParent;
	}

	public ChangedFileLocation getFirstParent() {
		return firstParent;
	}

	public void setFirstParent(ChangedFileLocation firstParent) {
		this.firstParent = firstParent;
	}
	
	public boolean hasFirstParent() {
		return firstParent != null;
	}
	
	public boolean hasSecondParent() {
		return secondParent != null;
	}

}