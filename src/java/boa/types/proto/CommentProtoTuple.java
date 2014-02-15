package boa.types.proto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import boa.types.BoaInt;
import boa.types.BoaProtoTuple;
import boa.types.BoaString;
import boa.types.BoaType;
import boa.types.proto.enums.CommentKindProtoMap;

/**
 * A {@link CommentProtoTuple}.
 * 
 * @author rdyer
 */
public class CommentProtoTuple extends BoaProtoTuple {
	private final static List<BoaType> members = new ArrayList<BoaType>();
	private final static Map<String, Integer> names = new HashMap<String, Integer>();

	static {
		int count = 0;

		names.put("kind", count++);
		members.add(new CommentKindProtoMap());

		names.put("value", count++);
		members.add(new BoaString());

		names.put("start_line", count++);
		members.add(new BoaInt());
	}

	/**
	 * Construct a {@link CommentProtoTuple}.
	 */
	public CommentProtoTuple() {
		super(members, names);
	}

	/** @{inheritDoc} */
	@Override
	public String toJavaType() {
		return "boa.types.Ast.Comment";
	}
}
