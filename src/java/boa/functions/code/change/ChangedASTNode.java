package boa.functions.code.change;

import java.util.ArrayList;
import java.util.List;

import boa.types.Shared.ChangeKind;

public abstract class ChangedASTNode {

	// code entity identifier in the scope
	protected String signature;

	// tree id
	protected TreeObjectId treeId;
	
	// changes
	protected List<ChangeKind> changes = new ArrayList<ChangeKind>();
	protected List<Integer> leftRefBondIdxs = new ArrayList<Integer>();
	protected List<Integer> rightRefBondIdxs = new ArrayList<Integer>();
	
	public ChangedASTNode(String sig) {
		this.signature = sig;
	}

	public String getSignature() {
		return signature;
	}

	public TreeObjectId getTreeId() {
		return treeId;
	}

	public void setTreeId(TreeObjectId treeId) {
		this.treeId = treeId;
	}

	public List<ChangeKind> getChanges() {
		return changes;
	}

	public List<Integer> getLeftRefBondIdxs() {
		return leftRefBondIdxs;
	}

	public List<Integer> getRightRefBondIdxs() {
		return rightRefBondIdxs;
	}

	@Override
	public abstract int hashCode();

	@Override
	public abstract boolean equals(Object obj);
	
	@Override
	public abstract String toString();

}
