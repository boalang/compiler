package boa.types.proto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import boa.types.BoaInt;
import boa.types.BoaProtoList;
import boa.types.BoaProtoTuple;
import boa.types.BoaString;
import boa.types.BoaType;

/**
 * A {@link DeclarationProtoTuple}.
 * 
 * @author rdyer
 */
public class DeclarationProtoTuple extends BoaProtoTuple {
	private final static List<BoaType> members = new ArrayList<BoaType>();
	private final static Map<String, Integer> names = new HashMap<String, Integer>();

	static {
		names.put("name", 0);
		members.add(new BoaString());

		names.put("kind", 1);
		members.add(new BoaInt());

		names.put("modifiers", 2);
		members.add(new BoaProtoList(new ModifierProtoTuple()));

		names.put("generic_parameters", 3);
		members.add(new BoaProtoList(new TypeProtoTuple()));

		names.put("parents", 4);
		members.add(new BoaProtoList(new TypeProtoTuple()));

		names.put("methods", 5);
		members.add(new BoaProtoList(new MethodProtoTuple()));

		names.put("fields", 6);
		members.add(new BoaProtoList(new VariableProtoTuple()));

		names.put("nested_declarations", 7);
		members.add(new BoaProtoList(new DeclarationProtoTuple()));

		names.put("comments", 8);
		members.add(new BoaProtoList(new CommentProtoTuple()));
	}

	/**
	 * Construct a {@link DeclarationProtoTuple}.
	 */
	public DeclarationProtoTuple() {
		super(members, names);
	}

	@Override
	public String toJavaType() {
		return "boa.types.Ast.Declaration";
	}
}
