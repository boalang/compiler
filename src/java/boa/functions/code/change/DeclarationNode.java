package boa.functions.code.change;

import java.util.ArrayList;
import java.util.List;

public class DeclarationNode {
	
	private FileNode fn;
	private String signature;
	private DeclarationLocation loc;
	private TreeObjectId treeId;
	private List<Integer> refBondIdxs = new ArrayList<Integer>();
	
	
	public DeclarationNode(FileNode fn, String fqn, int declIdx) {
		this.fn = fn;
		this.signature = fqn;
		this.loc = new DeclarationLocation(fn.getLoc(), declIdx); 
	}

	public FileNode getFileNode() {
		return fn;
	}


	public String getSignature() {
		return signature;
	}


	public DeclarationLocation getLoc() {
		return loc;
	}


	public TreeObjectId getTreeId() {
		return treeId;
	}


	public void setTreeId(TreeObjectId treeId) {
		this.treeId = treeId;
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
		DeclarationNode other = (DeclarationNode) obj;
		if (loc == null) {
			if (other.loc != null)
				return false;
		} else if (!loc.equals(other.loc))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return fn + " " + loc.getIdx() + " " + signature;
	}
	
}
