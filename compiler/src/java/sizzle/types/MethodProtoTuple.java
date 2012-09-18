package sizzle.types;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A {@link MethodProtoTuple}.
 * 
 * @author rdyer
 */
public class MethodProtoTuple extends SizzleProtoTuple {
	private final static List<SizzleType> members = new ArrayList<SizzleType>();
	private final static Map<String, Integer> names = new HashMap<String, Integer>();

	static {
		names.put("name", 0);
		members.add(new SizzleString());

		names.put("modifiers", 1);
		members.add(new SizzleProtoList(new ModifierProtoTuple()));

		names.put("return_type", 2);
		members.add(new TypeProtoTuple());

		names.put("generic_parameters", 3);
		members.add(new SizzleProtoList(new TypeProtoTuple()));

		names.put("arguments", 4);
		members.add(new SizzleProtoList(new VariableProtoTuple()));

		names.put("exception_types", 5);
		members.add(new SizzleProtoList(new TypeProtoTuple()));

		names.put("statements", 6);
		members.add(new SizzleProtoList(new StatementProtoTuple()));

		names.put("comments", 7);
		members.add(new SizzleProtoList(new CommentProtoTuple()));
	}

	/**
	 * Construct a {@link MethodProtoTuple}.
	 */
	public MethodProtoTuple() {
		super(members, names);
	}

	@Override
	public String toJavaType() {
		return "sizzle.types.Ast.Method";
	}
}
