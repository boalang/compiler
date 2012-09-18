package sizzle.types;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A {@link RevisionProtoTuple}.
 * 
 * @author rdyer
 */
public class RevisionProtoTuple extends SizzleProtoTuple {
	private final static List<SizzleType> members = new ArrayList<SizzleType>();
	private final static Map<String, Integer> names = new HashMap<String, Integer>();

	static {
		names.put("id", 0);
		members.add(new SizzleInt());

		names.put("author", 1);
		members.add(new PersonProtoTuple());

		names.put("committer", 2);
		members.add(new PersonProtoTuple());

		names.put("commit_date", 3);
		members.add(new SizzleTime());

		names.put("log", 4);
		members.add(new SizzleString());

		names.put("files", 5);
		members.add(new SizzleProtoList(new FileProtoTuple()));
	}

	/**
	 * Construct a {@link RevisionProtoTuple}.
	 */
	public RevisionProtoTuple() {
		super(members, names);
	}

	@Override
	public String toJavaType() {
		return "sizzle.types.Code.Revision";
	}
}
