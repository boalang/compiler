package sizzle.types;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A {@link SizzleProtoTuple}.
 * 
 * @author rdyer
 * 
 */
public class TypeProtoTuple extends SizzleProtoTuple {
	private final static List<SizzleType> members = new ArrayList<SizzleType>();
	private final static Map<String, Integer> names = new HashMap<String, Integer>();

	static {
		names.put("name", 0);
		members.add(new SizzleString());

		names.put("annotations", 1);
		members.add(new SizzleProtoList(new AnnotationProtoTuple()));

		names.put("methods", 3);
		members.add(new SizzleProtoList(new MethodProtoTuple()));

		names.put("fields", 4);
		members.add(new SizzleProtoList(new FieldProtoTuple()));
	}

	/**
	 * Construct a ProjectProtoTuple.
	 */
	public TypeProtoTuple() {
		super(members, names);
	}

	@Override
	public String toJavaType() {
		return "sizzle.types.Ast.Type";
	}
}
