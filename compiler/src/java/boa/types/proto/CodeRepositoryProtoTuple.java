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
 * A {@link BoaProtoTuple}.
 * 
 * @author rdyer
 * 
 */
public class CodeRepositoryProtoTuple extends BoaProtoTuple {
	private final static List<BoaType> members = new ArrayList<BoaType>();
	private final static Map<String, Integer> names = new HashMap<String, Integer>();

	static {
		names.put("url", 0);
		members.add(new BoaString());

		names.put("kind", 1);
		members.add(new BoaInt());

		names.put("revisions", 2);
		members.add(new BoaProtoList(new RevisionProtoTuple()));
	}

	/**
	 * Construct a ProjectProtoTuple.
	 */
	public CodeRepositoryProtoTuple() {
		super(members, names);
	}

	@Override
	public String toJavaType() {
		return "boa.types.Code.CodeRepository";
	}
}
