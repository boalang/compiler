package boa.types.proto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import boa.types.BoaProtoList;
import boa.types.BoaProtoTuple;
import boa.types.BoaType;
import boa.types.proto.enums.StatementKindProtoMap;

/**
 * A {@link StatementProtoTuple}.
 * 
 * @author rdyer
 */
public class StatementProtoTuple extends BoaProtoTuple {
	private final static List<BoaType> members = new ArrayList<BoaType>();
	private final static Map<String, Integer> names = new HashMap<String, Integer>();

	static {
		int counter = 0;

		names.put("kind", counter++);
		members.add(new StatementKindProtoMap());

		names.put("comments", counter++);
		members.add(new BoaProtoList(new CommentProtoTuple()));

		names.put("statements", counter++);
		members.add(new BoaProtoList(new StatementProtoTuple()));

		names.put("initializations", counter++);
		members.add(new BoaProtoList(new ExpressionProtoTuple()));

		names.put("condition", counter++);
		members.add(new ExpressionProtoTuple());

		names.put("updates", counter++);
		members.add(new BoaProtoList(new ExpressionProtoTuple()));

		names.put("variable_declaration", counter++);
		members.add(new VariableProtoTuple());

		names.put("type_declaration", counter++);
		members.add(new DeclarationProtoTuple());

		names.put("expression", counter++);
		members.add(new ExpressionProtoTuple());
	}

	/**
	 * Construct a {@link StatementProtoTuple}.
	 */
	public StatementProtoTuple() {
		super(members, names);
	}

	/** @{inheritDoc} */
	@Override
	public String toJavaType() {
		return "boa.types.Ast.Statement";
	}
}
