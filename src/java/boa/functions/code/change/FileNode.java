package boa.functions.code.change;

import java.util.ArrayList;
import java.util.List;

import boa.functions.code.change.FileTree.TreeObjectId;
import boa.types.Diff.ChangedFile;

public class FileNode {

	private ChangedFile cf = null;
	private RevNode r = null;
	private FileLocation locId = null;
	private TreeObjectId treeId = null;
	private List<RefactoringBond> refBonds = new ArrayList<RefactoringBond>();;
	

	public FileNode(ChangedFile cf, RevNode r, int fileIdx) {
		this.cf = cf;
		this.r = r;
		this.locId = new FileLocation(cf.getRevisionIdx(), cf.getFileIdx());
	}

	public FileLocation getLocId() {
		return locId;
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((treeId == null) ? 0 : treeId.hashCode());
		result = prime * result + ((locId == null) ? 0 : locId.hashCode());
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
		FileNode other = (FileNode) obj;
		if (locId == null) {
			if (other.locId != null)
				return false;
		} else if (!locId.equals(other.locId))
			return false;
		return true;
	}

	public List<RefactoringBond> getRefBonds() {
		return refBonds;
	}


}