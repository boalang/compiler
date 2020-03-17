package boa.functions.code.change;

import java.util.ArrayList;
import java.util.List;

import boa.types.Shared.ChangeKind;

public class DeclarationNode extends ChangedASTNode {

	private FileNode fn;
	private DeclarationLocation loc;
	private List<DeclarationLocation> prevLocs = new ArrayList<DeclarationLocation>();
	
	public DeclarationNode(FileNode fn, String fqn, int declIdx, ChangeKind change) {
		super(fqn, change);
		this.fn = fn;
		this.loc = new DeclarationLocation(fn.getLoc(), declIdx); 
	}

	public FileNode getFileNode() {
		return fn;
	}

	public DeclarationLocation getLoc() {
		return loc;
	}

	public List<DeclarationLocation> getPrevLocs() {
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