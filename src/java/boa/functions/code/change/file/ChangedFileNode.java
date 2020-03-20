package boa.functions.code.change.file;

import java.util.ArrayList;
import java.util.List;

import boa.functions.code.change.RevNode;
import boa.functions.code.change.TreeObjectId;
import boa.functions.code.change.declaration.ChangedDeclNode;
import boa.functions.code.change.refactoring.RefactoringBonds;
import boa.types.Diff.ChangedFile;
import boa.types.Shared.ChangeKind;

public class ChangedFileNode {

	private RevNode r;
	private ChangedFile cf;
	private ChangedFileLocation loc;
	private TreeObjectId treeId;
	private List<ChangedFileLocation> prevLocs = new ArrayList<ChangedFileLocation>();
	
	// changes
	private List<ChangeKind> changes = new ArrayList<ChangeKind>();
	private List<ChangedDeclNode> declChanges = new ArrayList<ChangedDeclNode>(); //TODO
	private RefactoringBonds leftRefBonds = new RefactoringBonds();
	private RefactoringBonds rightRefBonds = new RefactoringBonds();

	public ChangedFileNode(ChangedFile cf, RevNode r, ChangedFileLocation loc) {
		this.cf = cf;
		this.r = r;
		this.loc = loc;
	}
	
	public ChangedFileNode(ChangedFile cf, RevNode r) {
		this.cf = cf;
		this.r = r;
		this.loc = new ChangedFileLocation(cf.getRevisionIdx(), cf.getFileIdx());
	}
	
	public ChangedDeclNode getNewDeclNode(String fqn, ChangeKind change) {
		ChangedDeclNode declNode = new ChangedDeclNode(fqn, this, declChanges.size());
		declNode.getChanges().add(change);
		declChanges.add(declNode);
		return declNode;
	}

	public ChangedFileLocation getLoc() {
		return loc;
	}
	
	public int getRevIdx() {
		return cf.getRevisionIdx();
	}
	
	public int getFileIdx() {
		return cf.getFileIdx();
	}
	
	public TreeObjectId getTreeObjectId() {
		return treeId;
	}
	
	public void setTreeObjectId(TreeObjectId treeId) {
		this.treeId = treeId;
	}
	
	public ChangedFile getChangedFile() {
		return cf;
	}

	public RevNode getRev() {
		return r;
	}

	public TreeObjectId getListId() {
		return treeId;
	}

	public RefactoringBonds getLeftRefBonds() {
		return leftRefBonds;
	}

	public RefactoringBonds getRightRefBonds() {
		return rightRefBonds;
	}

	public List<ChangedFileLocation> getPrevLocs() {
		return prevLocs;
	}
	
	public List<Integer> getRevisionParents() {
		return r.getRevision().getParentsList();
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
		ChangedFileNode other = (ChangedFileNode) obj;
		if (loc == null) {
			if (other.loc != null)
				return false;
		} else if (!loc.equals(other.loc))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return r.getRevision().getId() + " " + loc + " " + cf.getName() + " " + cf.getChange();
	}

	public List<ChangeKind> getChanges() {
		return changes;
	}

	public List<ChangedDeclNode> getDeclChanges() {
		return declChanges;
	}
	
	public int getASTChangeCount() {
		int count = 0;
		for (ChangedDeclNode declNode : declChanges)
			count += declNode.getASTChangeCount();
		return count;
	}

}