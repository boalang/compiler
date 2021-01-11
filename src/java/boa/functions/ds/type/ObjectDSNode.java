package boa.functions.ds.type;

import java.util.ArrayList;
import java.util.List;

public class ObjectDSNode extends DSNode {

	// fileds
	public List<LinearListDSNode> lists;
	public List<UnionDSNode> unions;
	public List<DicDSNode> dics;
	public List<ObjectDSNode> objects;
	public List<NodeDSNode> nodes;
	public boolean hasData;

	// methods
	
	
	public ObjectDSNode(String type) {
		this.type = type;
		this.lists = new ArrayList<>();
		this.unions = new ArrayList<>();
		this.dics = new ArrayList<>();
		this.objects = new ArrayList<>();
		this.nodes = new ArrayList<>();
	}

	@Override
	public String getType() {
		return type;
	}
	
	public boolean isObjectDSNodeNode() {
		if (lists.size() > 0)
			return true;
		if (unions.size() > 0)
			return true;
		if (dics.size() > 0)
			return true;
		if (objects.size() > 0)
			return true;
		if (nodes.size() > 0)
			return true;
		return false;
	}
	
	public boolean isDataDSNode() {
		return hasData;
	}
}
