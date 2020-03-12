package boa.functions.code.change;

public class FileLocation implements Comparable<FileLocation> {
	
	private int revIdx;
	private int fileIdx;
	
	public FileLocation(int revIdx, int fileIdx) {
		this.revIdx = revIdx;
		this.fileIdx = fileIdx;
	}

	public int getRevIdx() {
		return revIdx;
	}

	public void setRevIdx(int revIdx) {
		this.revIdx = revIdx;
	}

	public int getFileIdx() {
		return fileIdx;
	}

	public void setFileIdx(int fileIdx) {
		this.fileIdx = fileIdx;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + fileIdx;
		result = prime * result + revIdx;
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
		FileLocation other = (FileLocation) obj;
		if (fileIdx != other.fileIdx)
			return false;
		if (revIdx != other.revIdx)
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return revIdx + " " + fileIdx;
	}

	@Override
	public int compareTo(FileLocation o) {
		int comp = this.revIdx - o.getRevIdx();
		return comp == 0 ? this.fileIdx - o.getFileIdx() : comp;
	}
	
 }
