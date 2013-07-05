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
		int counter = 0;

		names.put("name", counter++);
		members.add(new BoaString());

		names.put("modifiers", counter++);
		members.add(new BoaProtoList(new ModifierProtoTuple()));

		names.put("return_type", counter++);
		members.add(new TypeProtoTuple());

		names.put("generic_parameters", counter++);
		members.add(new BoaProtoList(new TypeProtoTuple()));

		names.put("arguments", counter++);
		members.add(new BoaProtoList(new VariableProtoTuple()));

		names.put("exception_types", counter++);
		members.add(new BoaProtoList(new TypeProtoTuple()));

		names.put("statements", counter++);
		members.add(new BoaProtoList(new StatementProtoTuple()));

		names.put("comments", counter++);
		members.add(new BoaProtoList(new CommentProtoTuple()));
	}

	/**
	 * Construct a {@link MethodProtoTuple}.
	 */
	public MethodProtoTuple() {
		super(members, names);
	}

	/** @{inheritDoc} */
	@Override
	public String toJavaType() {
		return "boa.types.Ast.Method";
	}
}
