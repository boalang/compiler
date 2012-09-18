package sizzle.types;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A {@link ExpressionProtoTuple}.
 * 
 * @author rdyer
 */
public class ExpressionProtoTuple extends SizzleProtoTuple {
	private final static List<SizzleType> members = new ArrayList<SizzleType>();
	private final static Map<String, Integer> names = new HashMap<String, Integer>();

	static {
		names.put("kind", 0);
		members.add(new ExpressionKindProtoMap());

		names.put("expressions", 1);
		members.add(new SizzleProtoList(new ExpressionProtoTuple()));

		names.put("variable_decls", 2);
		members.add(new SizzleProtoList(new VariableProtoTuple()));

		names.put("new_type", 3);
		members.add(new TypeProtoTuple());

		names.put("generic_parameters", 4);
		members.add(new SizzleProtoList(new TypeProtoTuple()));

		names.put("is_postfix", 5);
		members.add(new SizzleBool());

		names.put("literal", 6);
		members.add(new SizzleString());

		names.put("variable", 7);
		members.add(new SizzleString());

		names.put("method", 8);
		members.add(new SizzleString());

		names.put("method_args", 9);
		members.add(new SizzleProtoList(new ExpressionProtoTuple()));
	}

	/**
	 * Construct a {@link ExpressionProtoTuple}.
	 */
	public ExpressionProtoTuple() {
		super(members, names);
	}

	@Override
	public String toJavaType() {
		return "sizzle.types.Ast.Expression";
	}
}
