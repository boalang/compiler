package sizzle.types;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A {@link ModifierProtoTuple}.
 * 
 * @author rdyer
 */
public class ModifierProtoTuple extends SizzleProtoTuple {
	private final static List<SizzleType> members = new ArrayList<SizzleType>();
	private final static Map<String, Integer> names = new HashMap<String, Integer>();

	static {
		names.put("kind", 0);
		members.add(new ModifierKindProtoMap());

		names.put("visibility", 1);
		members.add(new SizzleInt());

		names.put("annotation_name", 2);
		members.add(new SizzleString());

		names.put("annotation_members", 3);
		members.add(new SizzleProtoList(new SizzleString()));

		names.put("annotation_values", 4);
		members.add(new SizzleProtoList(new ExpressionProtoTuple()));

		names.put("other", 5);
		members.add(new SizzleString());
	}

	/**
	 * Construct a {@link ModifierProtoTuple}.
	 */
	public ModifierProtoTuple() {
		super(members, names);
	}

	@Override
	public String toJavaType() {
		return "sizzle.types.Ast.Modifier";
	}
}
