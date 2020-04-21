package boa.functions.code.change.method;

import boa.functions.code.change.Location;
import boa.functions.code.change.declaration.DeclLocation;

public class MethodLocation extends Location implements Comparable<MethodLocation> {

	private DeclLocation declLoc;
	
	public MethodLocation(DeclLocation declLoc, int methodIdx) {
		super(methodIdx);
		this.setDeclLoc(declLoc);
	}
	
	public DeclLocation getDeclLoc() {
		return declLoc;
	}

	public void setDeclLoc(DeclLocation declLoc) {
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
		MethodLocation other = (MethodLocation) obj;
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
	public int compareTo(MethodLocation o) {
		int comp = this.declLoc.compareTo(o.getDeclLoc());
		return comp == 0 ? this.getIdx() - o.getIdx() : comp;
	}
	
}
