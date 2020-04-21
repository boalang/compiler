package boa.functions.code.change;

import java.util.ArrayList;
import java.util.List;

import boa.types.Shared.ChangeKind;

public abstract class ChangedASTNode {

	// code entity identifier in the scope
	protected String signature;

	// tree id
	protected int treeId = -1;

	// name-based changes
	protected ChangeKind firstChange;
	protected ChangeKind secondChange;
	
	// refactoring-based changes
	protected List<RefactoringBond> leftRefBonds = new ArrayList<RefactoringBond>();

	public ChangedASTNode(String sig) {
		this.signature = sig;
	}

	public String getSignature() {
		return signature;
	}

	public int getTreeId() {
		return treeId;
	}

	public void setTreeId(int treeId) {
		this.treeId = treeId;
	}

	public List<RefactoringBond> getLeftRefBonds() {
		return leftRefBonds;
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
