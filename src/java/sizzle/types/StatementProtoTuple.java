package sizzle.types;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A {@link StatementProtoTuple}.
 * 
 * @author rdyer
 */
public class StatementProtoTuple extends SizzleProtoTuple {
	private final static List<SizzleType> members = new ArrayList<SizzleType>();
	private final static Map<String, Integer> names = new HashMap<String, Integer>();

	static {
		names.put("kind", 0);
		members.add(new SizzleInt());

		names.put("comments", 1);
		members.add(new SizzleProtoList(new CommentProtoTuple()));

		names.put("statements", 2);
		members.add(new SizzleProtoList(new StatementProtoTuple()));

		names.put("condition", 3);
		members.add(new ExpressionProtoTuple());

		names.put("initialization", 4);
		members.add(new ExpressionProtoTuple());

		names.put("increment", 5);
		members.add(new ExpressionProtoTuple());

		names.put("variable_declaration", 6);
		members.add(new VariableProtoTuple());

		names.put("type_declaration", 7);
		members.add(new DeclarationProtoTuple());

		names.put("expression", 8);
		members.add(new ExpressionProtoTuple());
	}

	/**
	 * Construct a {@link StatementProtoTuple}.
	 */
	public StatementProtoTuple() {
		super(members, names);
	}

	@Override
	public String toJavaType() {
		return "sizzle.types.Ast.Statement";
	}
}
