package boa.functions.code.change;

public class TreeObjectId {
	public int id = -1;

	public TreeObjectId(int id) {
		this.id = id;
	}

	public int getAsInt() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return Integer.toString(id);
	}
}