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
 * A {@link ModifierProtoTuple}.
 * 
 * @author rdyer
 */
public class ModifierProtoTuple extends BoaProtoTuple {
	private final static List<BoaType> members = new ArrayList<BoaType>();
	private final static Map<String, Integer> names = new HashMap<String, Integer>();

	static {
		names.put("kind", 0);
		members.add(new BoaInt());

		names.put("visibility", 1);
		members.add(new BoaInt());

		names.put("annotation_name", 2);
		members.add(new BoaString());

		names.put("annotation_members", 3);
		members.add(new BoaProtoList(new BoaString()));

		names.put("annotation_values", 4);
		members.add(new BoaProtoList(new ExpressionProtoTuple()));

		names.put("other", 5);
		members.add(new BoaString());
	}

	/**
	 * Construct a {@link ModifierProtoTuple}.
	 */
	public ModifierProtoTuple() {
		super(members, names);
	}

	@Override
	public String toJavaType() {
		return "boa.types.Ast.Modifier";
	}
}
