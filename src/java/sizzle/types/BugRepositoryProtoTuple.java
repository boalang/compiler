package sizzle.types;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A {@link SizzleProtoTuple}.
 * 
 * @author rdyer
 * 
 */
public class BugRepositoryProtoTuple extends SizzleProtoTuple {
	private final static List<SizzleType> members = new ArrayList<SizzleType>();
	private final static Map<String, Integer> names = new HashMap<String, Integer>();

	static {
		names.put("url", 0);
		members.add(new SizzleString());

		names.put("bugs", 1);
		members.add(new SizzleProtoList(new BugProtoTuple()));
	}

	/**
	 * Construct a ProjectProtoTuple.
	 */
	public BugRepositoryProtoTuple() {
		super(members, names);
	}

	@Override
	public String toJavaType() {
		return "sizzle.types.Bugs.BugRepository";
	}
}
