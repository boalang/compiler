package boa.functions.code.change.method;

import boa.functions.code.change.Location;
import boa.functions.code.change.declaration.ChangedDeclLocation;

public class ChangedMethodLocation extends Location implements Comparable<ChangedMethodLocation> {

	private ChangedDeclLocation declLoc;
	
	public ChangedMethodLocation(ChangedDeclLocation declLoc, int methodIdx) {
		super(methodIdx);
		this.setDeclLoc(declLoc);
	}
	
	public ChangedDeclLocation getDeclLoc() {
		return declLoc;
	}

	public void setDeclLoc(ChangedDeclLocation declLoc) {
		this.declLoc = declLoc;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((declLoc == null) ? 0 : declLoc.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ChangedMethodLocation other = (ChangedMethodLocation) obj;
		if (declLoc == null) {
			if (other.declLoc != null)
				return false;
		} else if (!declLoc.equals(other.declLoc))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return this.declLoc + " " + super.toString();
	}

	@Override
	public int compareTo(ChangedMethodLocation o) {
		int comp = this.declLoc.compareTo(o.getDeclLoc());
		return comp == 0 ? this.getIdx() - o.getIdx() : comp;
	}
	
}
