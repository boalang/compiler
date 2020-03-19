package boa.functions.code.change.declaration;

import boa.functions.code.change.Location;
import boa.functions.code.change.file.ChangedFileLocation;

public class ChangedDeclLocation extends Location implements Comparable<ChangedDeclLocation> {
	
	private ChangedFileLocation fileLoc;

	public ChangedDeclLocation(ChangedFileLocation loc, int idx) {
		super(idx);
		this.fileLoc = loc;
	}

	public ChangedFileLocation getFileLoc() {
		return fileLoc;
	}

	public void setFileLoc(ChangedFileLocation fl) {
		this.fileLoc = fl;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((fileLoc == null) ? 0 : fileLoc.hashCode());
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
		ChangedDeclLocation other = (ChangedDeclLocation) obj;
		if (fileLoc == null) {
			if (other.fileLoc != null)
				return false;
		} else if (!fileLoc.equals(other.fileLoc))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return this.fileLoc + " " + super.toString();
	}

	@Override
	public int compareTo(ChangedDeclLocation o) {
		int comp = this.fileLoc.compareTo(o.getFileLoc());
		return comp == 0 ? this.getIdx() - o.getIdx() : comp;
	}

}
