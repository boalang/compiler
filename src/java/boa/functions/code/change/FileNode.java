package boa.functions.code.change;

import boa.functions.code.change.FileTree.TreeObjectId;
import boa.types.Diff.ChangedFile;

public class FileNode {

	private ChangedFile cf = null;
	private RevNode r = null;
	private String locId = null;
	private TreeObjectId treeId = null;

	public FileNode(ChangedFile cf, RevNode r, int fileIdx) {
		this.cf = cf;
		this.r = r;
	}

	public String getLocId() {
		if (locId == null)
			locId = cf.getRevisionIdx() + " " + cf.getFileIdx();
		return locId;
	}
	
	public int getRevIdx() {
		return cf.getRevisionIdx();
	}
	
	public int getFileIdx() {
		return cf.getFileIdx();
	}
	
	public TreeObjectId getListObjectId() {
		return treeId;
	}
	
	public void setListObjectId(TreeObjectId listId) {
		this.treeId = listId;
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


}