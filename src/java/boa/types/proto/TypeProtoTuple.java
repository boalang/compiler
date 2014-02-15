package boa.types.proto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import boa.types.BoaProtoTuple;
import boa.types.BoaString;
import boa.types.BoaType;
import boa.types.proto.enums.TypeKindProtoMap;

/**
 * A {@link TypeProtoTuple}.
 * 
 * @author rdyer
 */
public class TypeProtoTuple extends BoaProtoTuple {
	private final static List<BoaType> members = new ArrayList<BoaType>();
	private final static Map<String, Integer> names = new HashMap<String, Integer>();

	static {
		int counter = 0;

		names.put("name", counter++);
		members.add(new BoaString());

		names.put("kind", counter++);
		members.add(new TypeKindProtoMap());

		names.put("id", counter++);
		members.add(new BoaString());
	}

	/**
	 * Construct a {@link TypeProtoTuple}.
	 */
	public TypeProtoTuple() {
		super(members, names);
	}

	/** @{inheritDoc} */
	@Override
	public String toJavaType() {
		return "boa.types.Ast.Type";
	}
}
