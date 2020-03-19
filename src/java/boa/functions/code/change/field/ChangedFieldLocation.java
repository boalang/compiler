package boa.functions.code.change.field;

import boa.functions.code.change.Location;
import boa.functions.code.change.declaration.ChangedDeclLocation;

public class ChangedFieldLocation extends Location implements Comparable<ChangedFieldLocation> {

	private ChangedDeclLocation declLoc;
	
	public ChangedFieldLocation(ChangedDeclLocation declLoc, int fieldIdx) {
		super(fieldIdx);
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
		ChangedFieldLocation other = (ChangedFieldLocation) obj;
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
	public int compareTo(ChangedFieldLocation o) {
		int comp = this.declLoc.compareTo(o.getDeclLoc());
		return comp == 0 ? this.getIdx() - o.getIdx() : comp;
	}

}
