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
public class MethodProtoTuple extends SizzleProtoTuple {
	private final static List<SizzleType> members = new ArrayList<SizzleType>();
	private final static Map<String, Integer> names = new HashMap<String, Integer>();

	static {
		names.put("name", 0);
		members.add(new SizzleString());

		names.put("annotations", 1);
		members.add(new SizzleProtoList(new AnnotationProtoTuple()));

		names.put("return_type", 2);
		members.add(new SizzleString());

		names.put("arg_types", 3);
		members.add(new SizzleProtoList(new SizzleString()));

		names.put("arg_names", 4);
		members.add(new SizzleProtoList(new SizzleString()));

		names.put("exceptions", 5);
		members.add(new SizzleProtoList(new SizzleString()));
	}

	/**
	 * Construct a ProjectProtoTuple.
	 */
	public MethodProtoTuple() {
		super(members, names);
	}

	@Override
	public String toJavaType() {
		return "sizzle.types.Ast.Method";
	}
}
