package boa.types.proto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import boa.types.BoaProtoList;
import boa.types.BoaProtoTuple;
import boa.types.BoaString;
import boa.types.BoaType;

/**
 * A {@link VariableProtoTuple}.
 * 
 * @author rdyer
 */
public class VariableProtoTuple extends BoaProtoTuple {
	private final static List<BoaType> members = new ArrayList<BoaType>();
	private final static Map<String, Integer> names = new HashMap<String, Integer>();

	static {
		names.put("name", 0);
		members.add(new BoaString());

		names.put("variable_type", 1);
		members.add(new TypeProtoTuple());

		names.put("modifiers", 2);
		members.add(new BoaProtoList(new ModifierProtoTuple()));

		names.put("initializer", 3);
		members.add(new ExpressionProtoTuple());

		names.put("comments", 4);
		members.add(new BoaProtoList(new CommentProtoTuple()));
	}

	/**
	 * Construct a {@link VariableProtoTuple}.
	 */
	public VariableProtoTuple() {
		super(members, names);
	}

	@Override
	public String toJavaType() {
		return "boa.types.Ast.Variable";
	}
}
