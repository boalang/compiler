package boa.functions.ds.type;

public class LinearListDSNode extends DSNode {

	public DSNode element;

	public LinearListDSNode(DSNode element) {
		this.element = element;
	}
	
	@Override
	public String getType() {
		return element.getType();
	};
}
