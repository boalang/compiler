package boa.functions.code.change.method;

import java.util.ArrayList;
import java.util.List;

import boa.functions.code.change.ChangedASTNode;
import boa.functions.code.change.declaration.ChangedDeclLocation;
import boa.functions.code.change.declaration.ChangedDeclNode;
import boa.functions.code.change.file.ChangedFileNode;

public class ChangedMethodNode extends ChangedASTNode {

	private ChangedDeclNode declNode;
	private ChangedMethodLocation loc;
	private List<ChangedMethodLocation> prevLocs = new ArrayList<ChangedMethodLocation>();

	public ChangedMethodNode(String sig, ChangedDeclNode declNode, ChangedMethodLocation loc) {
		super(sig);
		this.declNode = declNode;
		this.loc = loc;
	}

	public ChangedMethodNode(String sig, ChangedDeclNode declNode, int size) {
		super(sig);
		this.declNode = declNode;
		this.loc = new ChangedMethodLocation(declNode.getLoc(), size);
	}

	public ChangedDeclNode getDeclNode() {
		return declNode;
	}

	public ChangedMethodLocation getLoc() {
		return loc;
	}

	public List<ChangedMethodLocation> getPrevLocs() {
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
		ChangedMethodNode other = (ChangedMethodNode) obj;
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