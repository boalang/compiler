package boa.functions.ds.type;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class DataDSNode extends DSNode {

	private Set<String> defaultDataSet = new HashSet<>(Arrays.asList("byte", "short", "int", "long", "float", "double",
			"boolean", "char", "Byte", "Short", "Integer", "Long", "Float", "Double", "Boolean", "Character"));
	private Set<String> types;

	public DataDSNode() {
		this.types = new HashSet<>();
	}

	public boolean update(String type) {
		return types.add(type);
	}

	public boolean isData(String type) {
		return defaultDataSet.contains(type) || types.contains(type);
	}

	@Override
	public String getType() {
		return "data";
	}
}
