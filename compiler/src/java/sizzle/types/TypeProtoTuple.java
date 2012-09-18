package sizzle.types;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A {@link TypeProtoTuple}.
 * 
 * @author rdyer
 */
public class TypeProtoTuple extends SizzleProtoTuple {
	private final static List<SizzleType> members = new ArrayList<SizzleType>();
	private final static Map<String, Integer> names = new HashMap<String, Integer>();

	static {
		names.put("name", 0);
		members.add(new SizzleString());

		names.put("kind", 1);
		members.add(new TypeKindProtoMap());

		names.put("id", 3);
		members.add(new SizzleString());
	}

	/**
	 * Construct a {@link TypeProtoTuple}.
	 */
	public TypeProtoTuple() {
		super(members, names);
	}

	@Override
	public String toJavaType() {
		return "sizzle.types.Ast.Type";
	}
}
