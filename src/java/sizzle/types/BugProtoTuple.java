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
public class BugProtoTuple extends SizzleProtoTuple {
	private final static List<SizzleType> members = new ArrayList<SizzleType>();
	private final static Map<String, Integer> names = new HashMap<String, Integer>();

	static {
		names.put("id", 0);
		members.add(new SizzleInt());

		names.put("reporter", 1);
		members.add(new PersonProtoTuple());

		names.put("reported_date", 2);
		members.add(new SizzleTime());

		names.put("closed_date", 3);
		members.add(new SizzleTime());

		names.put("summary", 4);
		members.add(new SizzleString());

		names.put("description", 5);
		members.add(new SizzleString());

		names.put("status", 6);
		members.add(new SizzleInt());

		names.put("severity", 7);
		members.add(new SizzleString());
	}

	/**
	 * Construct a ProjectProtoTuple.
	 */
	public BugProtoTuple() {
		super(members, names);
	}

	@Override
	public String toJavaType() {
		return "sizzle.types.Bugs.Bug";
	}
}
