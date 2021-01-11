package boa.functions.ds.type;

public class NodeDSNode extends ObjectDSNode {
	
	public NodeDSNode(String type) {
		super(type);
	}

	@Override
	public String getType() {
		return type;
	};
}
