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
 * A {@link MethodProtoTuple}.
 * 
 * @author rdyer
 */
public class MethodProtoTuple extends BoaProtoTuple {
	private final static List<BoaType> members = new ArrayList<BoaType>();
	private final static Map<String, Integer> names = new HashMap<String, Integer>();

	static {
		names.put("name", 0);
		members.add(new BoaString());

		names.put("modifiers", 1);
		members.add(new BoaProtoList(new ModifierProtoTuple()));

		names.put("return_type", 2);
		members.add(new TypeProtoTuple());

		names.put("generic_parameters", 3);
		members.add(new BoaProtoList(new TypeProtoTuple()));

		names.put("arguments", 4);
		members.add(new BoaProtoList(new VariableProtoTuple()));

		names.put("exception_types", 5);
		members.add(new BoaProtoList(new TypeProtoTuple()));

		names.put("statements", 6);
		members.add(new BoaProtoList(new StatementProtoTuple()));

		names.put("comments", 7);
		members.add(new BoaProtoList(new CommentProtoTuple()));
	}

	/**
	 * Construct a {@link MethodProtoTuple}.
	 */
	public MethodProtoTuple() {
		super(members, names);
	}

	@Override
	public String toJavaType() {
		return "boa.types.Ast.Method";
	}
}
