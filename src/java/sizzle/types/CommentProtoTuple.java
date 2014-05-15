package sizzle.types;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A {@link CommentProtoTuple}.
 * 
 * @author rdyer
 */
public class CommentProtoTuple extends SizzleProtoTuple {
	private final static List<SizzleType> members = new ArrayList<SizzleType>();
	private final static Map<String, Integer> names = new HashMap<String, Integer>();

	static {
		names.put("kind", 0);
		members.add(new SizzleInt());

		names.put("value", 1);
		members.add(new SizzleString());

		names.put("start_line", 2);
		members.add(new SizzleInt());
	}

	/**
	 * Construct a {@link CommentProtoTuple}.
	 */
	public CommentProtoTuple() {
		super(members, names);
	}

	@Override
	public String toJavaType() {
		return "sizzle.types.Ast.Comment";
	}
}
