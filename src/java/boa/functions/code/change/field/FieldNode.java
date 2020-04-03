package boa.functions.code.change.field;

import java.util.ArrayList;
import java.util.List;

import boa.functions.code.change.ChangedASTNode;
import boa.functions.code.change.declaration.DeclNode;

public class FieldNode extends ChangedASTNode implements Comparable<FieldNode> {

	private DeclNode declNode;
	private FieldLocation loc;
	
	// name-based edges
	private FieldNode firstParent;
	private FieldNode secondParent;
	
	// refactoring-based edges
	private List<FieldNode> beforeRefFields = new ArrayList<FieldNode>();

	public FieldNode(String sig, DeclNode declNode, FieldLocation loc) {
		super(sig);
		this.declNode = declNode;
		this.loc = loc;
	}

	public FieldNode(String sig, DeclNode declNode, int size) {
		super(sig);
		this.declNode = declNode;
		this.loc = new FieldLocation(declNode.getLoc(), size);
	}

	public DeclNode getDeclNode() {
		return declNode;
	}

	public FieldLocation getLoc() {
		return loc;
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
		return declNode + " " + loc.getIdx() + " " + signature + " " + firstChange + " " + secondChange;
	}

	public boolean hasFirstParent() {
		return firstParent != null;
	}

	public boolean hasSecondParent() {
		return secondParent != null;
	}

	public FieldNode getFirstParent() {
		return firstParent;
	}

	public void setFirstParent(FieldNode firstParent) {
		this.firstParent = firstParent;
	}

	public FieldNode getSecondParent() {
		return secondParent;
	}

	public void setSecondParent(FieldNode secondParent) {
		this.secondParent = secondParent;
	}

	@Override
	public int compareTo(FieldNode o) {
		return this.loc.compareTo(o.getLoc());
	}

	public List<FieldNode> getBeforeRefFields() {
		return beforeRefFields;
	}

}