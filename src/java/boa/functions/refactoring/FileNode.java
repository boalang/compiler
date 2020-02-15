package boa.functions.refactoring;

import boa.functions.refactoring.FileChangeLinkedList.ListObjectId;
import boa.types.Diff.ChangedFile;

public class FileNode {

	private ChangedFile cf = null;
	private Rev r = null;
	private String locId = null;
	private ListObjectId listId = null;

	public FileNode(ChangedFile cf, Rev r, int fileIdx) {
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
	
	public ListObjectId getListObjectId() {
		return listId;
	}
	
	public void setListObjectId(ListObjectId listId) {
		this.listId = listId;
	}
	
	public ChangedFile getChangedFile() {
		return cf;
	}

	public Rev getRev() {
		return r;
	}

	public ListObjectId getListId() {
		return listId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
//		result = prime * result + ((cf == null) ? 0 : cf.hashCode());
		result = prime * result + ((listId == null) ? 0 : listId.hashCode());
		result = prime * result + ((locId == null) ? 0 : locId.hashCode());
//		result = prime * result + ((r == null) ? 0 : r.hashCode());
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
//		if (cf == null) {
//			if (other.cf != null)
//				return false;
//		} else if (!cf.equals(other.cf))
//			return false;
//		if (listId == null) {
//			if (other.listId != null)
//				return false;
//		} else if (!listId.equals(other.listId))
//			return false;
		if (locId == null) {
			if (other.locId != null)
				return false;
		} else if (!locId.equals(other.locId))
			return false;
//		if (r == null) {
//			if (other.r != null)
//				return false;
//		} else if (!r.equals(other.r))
//			return false;
		return true;
	}


}