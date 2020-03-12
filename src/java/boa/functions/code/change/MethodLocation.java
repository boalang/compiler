package boa.functions.code.change;

public class MethodLocation extends ASTLocation implements Comparable<MethodLocation> {

	private DeclarationLocation declLoc;
	
	public MethodLocation(DeclarationLocation declLoc, int methodIdx, String signature) {
		super(methodIdx, signature);
		this.setDeclLoc(declLoc);
	}
	
	public DeclarationLocation getDeclLoc() {
		return declLoc;
	}

	public void setDeclLoc(DeclarationLocation declLoc) {
		this.declLoc = declLoc;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((declLoc == null) ? 0 : declLoc.hashCode());
		result = prime * result + this.idx;
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
		MethodLocation other = (MethodLocation) obj;
		if (declLoc == null) {
			if (other.declLoc != null)
				return false;
		} else if (!declLoc.equals(other.declLoc))
			return false;
		if (this.idx != other.idx)
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
		return comp == 0 ? this.idx - o.idx : comp;
	}
	
}
