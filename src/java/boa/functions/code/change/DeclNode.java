package boa.functions.code.change;

import java.util.ArrayList;
import java.util.List;

import boa.types.Shared.ChangeKind;

public class DeclNode extends ChangedASTNode {

	private FileNode fn;
	private DeclLocation loc;
	private List<DeclLocation> prevLocs = new ArrayList<DeclLocation>();
	
	public DeclNode(FileNode fn, String fqn, int declIdx, ChangeKind change) {
		super(fqn, change);
		this.fn = fn;
		this.loc = new DeclLocation(fn.getLoc(), declIdx); 
	}

	public FileNode getFileNode() {
		return fn;
	}

	public DeclLocation getLoc() {
		return loc;
	}

	public List<DeclLocation> getPrevLocs() {
		return prevLocs;
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
		DeclNode other = (DeclNode) obj;
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