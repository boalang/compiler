package sizzle.types;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A {@link DeclarationProtoTuple}.
 * 
 * @author rdyer
 */
public class DeclarationProtoTuple extends SizzleProtoTuple {
	private final static List<SizzleType> members = new ArrayList<SizzleType>();
	private final static Map<String, Integer> names = new HashMap<String, Integer>();

	static {
		names.put("name", 0);
		members.add(new SizzleString());

		names.put("kind", 1);
		members.add(new SizzleInt());

		names.put("modifiers", 2);
		members.add(new SizzleProtoList(new ModifierProtoTuple()));

		names.put("generic_parameters", 3);
		members.add(new SizzleProtoList(new TypeProtoTuple()));

		names.put("parents", 4);
		members.add(new SizzleProtoList(new TypeProtoTuple()));

		names.put("methods", 5);
		members.add(new SizzleProtoList(new MethodProtoTuple()));

		names.put("fields", 6);
		members.add(new SizzleProtoList(new VariableProtoTuple()));

		names.put("nested_declarations", 7);
		members.add(new SizzleProtoList(new DeclarationProtoTuple()));

		names.put("comments", 8);
		members.add(new SizzleProtoList(new CommentProtoTuple()));
	}

	/**
	 * Construct a {@link DeclarationProtoTuple}.
	 */
	public DeclarationProtoTuple() {
		super(members, names);
	}

	@Override
	public String toJavaType() {
		return "sizzle.types.Ast.Declaration";
	}
}
