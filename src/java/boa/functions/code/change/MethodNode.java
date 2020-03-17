package boa.functions.code.change;

import java.util.ArrayList;
import java.util.List;

import boa.types.Shared.ChangeKind;

public class MethodNode {

	private DeclarationNode declNode;
	private String signature;
	private MethodLocation loc;
	private ChangeKind change;
	private TreeObjectId treeId;
	private List<Integer> refBondIdxs = new ArrayList<Integer>();
	
	public MethodNode(DeclarationNode declNode, String sig, int mIdx, ChangeKind change) {
		this.declNode = declNode;
		this.signature = sig;
		this.change = change;
		this.loc = new MethodLocation(declNode.getLoc(), mIdx);
	}

	public DeclarationNode getDeclNode() {
		return declNode;
	}

	public String getSignature() {
		return signature;
	}

	public MethodLocation getLoc() {
		return loc;
	}

	public ChangeKind getChange() {
		return change;
	}

	public TreeObjectId getTreeId() {
		return treeId;
	}

	public List<Integer> getRefBondIdxs() {
		return refBondIdxs;
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
		return declNode + " " + loc.getIdx() + " " + signature;
	}
	
}
