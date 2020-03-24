package boa.functions.code.change;

import java.util.ArrayList;
import java.util.List;

import boa.types.Shared.ChangeKind;

public abstract class ChangedNode {

	// code entity identifier in the scope
	protected String signature;

	// tree id
	protected TreeObjectId treeId;

	// changes
	protected ChangeKind firstChange;
	protected ChangeKind secondChange;
	protected List<Integer> leftRefBondIdxs = new ArrayList<Integer>();
	protected List<Integer> rightRefBondIdxs = new ArrayList<Integer>();

	public ChangedNode(String sig) {
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

	public List<Integer> getLeftRefBondIdxs() {
		return leftRefBondIdxs;
	}

	public List<Integer> getRightRefBondIdxs() {
		return rightRefBondIdxs;
	}

	public ChangeKind getFirstChange() {
		return firstChange;
	}

	public void setFirstChange(ChangeKind firstChange) {
		this.firstChange = firstChange;
	}

	public ChangeKind getSecondChange() {
		return secondChange;
	}

	public void setSecondChange(ChangeKind secondChange) {
		this.secondChange = secondChange;
	}

	@Override
	public abstract int hashCode();

	@Override
	public abstract boolean equals(Object obj);

	@Override
	public abstract String toString();

}
