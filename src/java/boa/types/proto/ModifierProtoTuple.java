package boa.types.proto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import boa.types.BoaProtoList;
import boa.types.BoaProtoTuple;
import boa.types.BoaString;
import boa.types.BoaType;
import boa.types.proto.enums.ModifierKindProtoMap;
import boa.types.proto.enums.VisibilityProtoMap;

/**
 * A {@link ModifierProtoTuple}.
 * 
 * @author rdyer
 */
public class ModifierProtoTuple extends BoaProtoTuple {
	private final static List<BoaType> members = new ArrayList<BoaType>();
	private final static Map<String, Integer> names = new HashMap<String, Integer>();

	static {
		int counter = 0;

		names.put("kind", counter++);
		members.add(new ModifierKindProtoMap());

		names.put("visibility", counter++);
		members.add(new VisibilityProtoMap());

		names.put("annotation_name", counter++);
		members.add(new BoaString());

		names.put("annotation_members", counter++);
		members.add(new BoaProtoList(new BoaString()));

		names.put("annotation_values", counter++);
		members.add(new BoaProtoList(new ExpressionProtoTuple()));

		names.put("other", counter++);
		members.add(new BoaString());
	}

	/**
	 * Construct a {@link ModifierProtoTuple}.
	 */
	public ModifierProtoTuple() {
		super(members, names);
	}

	/** @{inheritDoc} */
	@Override
	public String toJavaType() {
		return "boa.types.Ast.Modifier";
	}
}
