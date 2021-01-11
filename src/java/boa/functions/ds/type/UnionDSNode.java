package boa.functions.ds.type;

public class UnionDSNode extends DSNode {

	public DSNode element;

	public UnionDSNode(DSNode element) {
		this.element = element;
	}
	
	@Override
	public String getType() {
		return element.getType();
	};
}
