package boa.functions.code.change.method;

import boa.functions.code.change.ChangedASTNode;
import boa.functions.code.change.declaration.DeclNode;

public class MethodNode extends ChangedASTNode implements Comparable<MethodNode> {

	private DeclNode declNode;
	private MethodLocation loc;
	private MethodNode firstParent;
	private MethodNode secondParent;

	public MethodNode(String sig, DeclNode declNode, MethodLocation loc) {
		super(sig);
		this.declNode = declNode;
		this.loc = loc;
	}

	public MethodNode(String sig, DeclNode declNode, int size) {
		super(sig);
		this.declNode = declNode;
		this.loc = new MethodLocation(declNode.getLoc(), size);
	}

	public DeclNode getDeclNode() {
		return declNode;
	}

	public MethodLocation getLoc() {
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
		MethodNode other = (MethodNode) obj;
		if (loc == null) {
			if (other.loc != null)
				return false;
		} else if (!loc.equals(other.loc))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return declNode + " " + loc.getIdx() + " " + signature + " " + firstChange + " " + secondChange;
	}

	public MethodNode getSecondParent() {
		return secondParent;
	}

	public void setSecondParent(MethodNode secondParent) {
		this.secondParent = secondParent;
	}

	public MethodNode getFirstParent() {
		return firstParent;
	}

	public void setFirstParent(MethodNode firstParent) {
		this.firstParent = firstParent;
	}
	
	public boolean hasFirstParent() {
		return firstParent != null;
	}
	
	public boolean hasSecondParent() {
		return secondParent != null;
	}

	@Override
	public int compareTo(MethodNode o) {
		return this.loc.compareTo(o.getLoc());
	}

}