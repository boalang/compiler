package boa.functions.code.change;

import java.util.ArrayList;
import java.util.List;

import boa.types.Shared.ChangeKind;

public abstract class ASTChangeNode {

	protected String signature;
	protected TreeObjectId treeId;
	protected List<ChangeKind> changes = new ArrayList<ChangeKind>();
	protected List<Integer> leftRefBondIdxs = new ArrayList<Integer>();
	protected List<Integer> rightRefBondIdxs = new ArrayList<Integer>();
	
	public ASTChangeNode(String sig, ChangeKind change) {
		this.signature = sig;
		this.changes.add(change);
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
