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

public class PositionInfoProtoTuple extends BoaProtoTuple {
	private final static List<BoaType> members = new ArrayList<BoaType>();
	private final static Map<String, Integer> names = new HashMap<String, Integer>();

	static {
		int count = 0;

		names.put("start_pos", count++);
		members.add(new BoaInt());

		names.put("length", count++);
		members.add(new BoaInt());

		names.put("start_line", count++);
		members.add(new BoaInt());
		
		names.put("start_col", count++);
		members.add(new BoaInt());
		
		names.put("end_line", count++);
		members.add(new BoaInt());
		
		names.put("end_col", count++);
		members.add(new BoaInt());
	}
	
	/**
	 * Construct a {@link PositionInfoProtoTuple}.
	 */
	public PositionInfoProtoTuple() {
		super(members, names);
	}
	
	/** @{inheritDoc} */
	@Override
	public String toJavaType() {
		return "boa.types.Ast.PositionInfo";
	}

}
