package boa.functions.code.change.field;

import java.util.ArrayList;
import java.util.List;

import boa.functions.code.change.ChangedASTNode;
import boa.functions.code.change.declaration.ChangedDeclNode;

public class ChangedFieldNode extends ChangedASTNode {

	private ChangedDeclNode declNode;
	private ChangedFieldLocation loc;
	private List<ChangedFieldLocation> prevLocs = new ArrayList<ChangedFieldLocation>();

	public ChangedFieldNode(String sig, ChangedDeclNode declNode, ChangedFieldLocation loc) {
		super(sig);
		this.declNode = declNode;
		this.loc = loc;
	}
	
	public ChangedFieldNode(String sig, ChangedDeclNode declNode, int size) {
		super(sig);
		this.declNode = declNode;
		this.loc = new ChangedFieldLocation(declNode.getLoc(), size);
	}
	
	public ChangedDeclNode getDeclNode() {
		return declNode;
	}

	public ChangedFieldLocation getLoc() {
		return loc;
	}

	public List<ChangedFieldLocation> getPrevLocs() {
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
		ChangedFieldNode other = (ChangedFieldNode) obj;
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