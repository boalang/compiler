package boa.functions.code.change;

public abstract class ASTLocation {

	protected int idx;
	protected String signature;

	public ASTLocation(int idx, String sig) {
		this.idx = idx;
		this.signature = sig;
	}
	
	public int getIdx() {
		return idx;
	}

	public void setIdx(int idx) {
		this.idx = idx;
	}

	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + idx;
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
		ASTLocation other = (ASTLocation) obj;
		if (idx != other.idx)
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return this.idx + " " + this.signature;
	};

}
