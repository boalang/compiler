package boa.functions.code.change.file;

import boa.functions.code.change.Location;

public class ChangedFileLocation extends Location implements Comparable<ChangedFileLocation> {
	
	private int revIdx;
	
	public ChangedFileLocation(int revIdx, int fileIdx) {
		super(fileIdx);
		this.revIdx = revIdx;
	}

	public int getRevIdx() {
		return revIdx;
	}

	public void setRevIdx(int revIdx) {
		this.revIdx = revIdx;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + revIdx;
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
		ChangedFileLocation other = (ChangedFileLocation) obj;
		if (revIdx != other.revIdx)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return revIdx + " " + super.toString();
	}

	@Override
	public int compareTo(ChangedFileLocation o) {
		int comp = this.revIdx - o.getRevIdx();
		return comp == 0 ? this.getIdx() - o.getIdx() : comp;
	}
	
 }
