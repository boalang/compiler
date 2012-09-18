package sizzle.types;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A {@link NamespaceProtoTuple}.
 * 
 * @author rdyer
 */
public class NamespaceProtoTuple extends SizzleProtoTuple {
	private final static List<SizzleType> members = new ArrayList<SizzleType>();
	private final static Map<String, Integer> names = new HashMap<String, Integer>();

	static {
		names.put("name", 0);
		members.add(new SizzleString());

		names.put("modifiers", 1);
		members.add(new SizzleProtoList(new ModifierProtoTuple()));

		names.put("declarations", 2);
		members.add(new SizzleProtoList(new DeclarationProtoTuple()));

		names.put("comments", 3);
		members.add(new SizzleProtoList(new CommentProtoTuple()));
	}

	/**
	 * Construct a {@link NamespaceProtoTuple}.
	 */
	public NamespaceProtoTuple() {
		super(members, names);
	}

	@Override
	public String toJavaType() {
		return "sizzle.types.Ast.Namespace";
	}
}
