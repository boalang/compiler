package boa.functions.code.change;

public class DeclarationLocation extends Location implements Comparable<DeclarationLocation> {
	
	private FileLocation fileLoc;

	public DeclarationLocation(FileLocation loc, int declIdx) {
		super(declIdx);
		this.fileLoc = loc;
	}

	public FileLocation getFileLoc() {
		return fileLoc;
	}

	public void setFileLoc(FileLocation fl) {
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
		DeclarationLocation other = (DeclarationLocation) obj;
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
	public int compareTo(DeclarationLocation o) {
		int comp = this.fileLoc.compareTo(o.getFileLoc());
		return comp == 0 ? this.idx - o.idx : comp;
	}


}
