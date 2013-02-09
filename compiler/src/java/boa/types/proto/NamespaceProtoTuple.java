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
 * A {@link NamespaceProtoTuple}.
 * 
 * @author rdyer
 */
public class NamespaceProtoTuple extends BoaProtoTuple {
	private final static List<BoaType> members = new ArrayList<BoaType>();
	private final static Map<String, Integer> names = new HashMap<String, Integer>();

	static {
		names.put("name", 0);
		members.add(new BoaString());

		names.put("modifiers", 1);
		members.add(new BoaProtoList(new ModifierProtoTuple()));

		names.put("declarations", 2);
		members.add(new BoaProtoList(new DeclarationProtoTuple()));

		names.put("comments", 3);
		members.add(new BoaProtoList(new CommentProtoTuple()));
	}

	/**
	 * Construct a {@link NamespaceProtoTuple}.
	 */
	public NamespaceProtoTuple() {
		super(members, names);
	}

	@Override
	public String toJavaType() {
		return "boa.types.Ast.Namespace";
	}
}
