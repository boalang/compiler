package boa.functions.code.change;

import java.util.ArrayList;
import java.util.List;

import boa.types.Shared.ChangeKind;

public class FieldNode extends ASTChangeNode {

	private DeclNode declNode;
	private FieldLocation loc;
	private List<FieldLocation> prevLocs = new ArrayList<FieldLocation>();

	public FieldNode(DeclNode declNode, String sig, int mIdx, ChangeKind change) {
		super(sig, change);
		this.declNode = declNode;
		this.loc = new FieldLocation(declNode.getLoc(), mIdx);
	}
	
	public DeclNode getDeclNode() {
		return declNode;
	}

	public FieldLocation getLoc() {
		return loc;
	}

	public List<FieldLocation> getPrevLocs() {
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
		FieldNode other = (FieldNode) obj;
		if (loc == null) {
			if (other.loc != null)
				return false;
		} else if (!loc.equals(other.loc))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return declNode + " " + loc.getIdx() + " " + signature;
	}

}