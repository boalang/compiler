package boa.functions.ds.type;

public class DicDSNode extends DSNode {

	public DSNode key;
	public DSNode val;

	public DicDSNode(DSNode key, DSNode val) {
		this.key = key;
		this.val = val;
	}
	
	@Override
	public String getType() {
		return key.getType() + " " + val.getType();
	};
}
