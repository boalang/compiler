package sizzle.types;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A {@link ChangedFileProtoTuple}.
 * 
 * @author rdyer
 */
public class ChangedFileProtoTuple extends SizzleProtoTuple {
	private final static List<SizzleType> members = new ArrayList<SizzleType>();
	private final static Map<String, Integer> names = new HashMap<String, Integer>();

	static {
		int counter = 0;

		names.put("change", counter++);
		members.add(new SizzleInt());

		names.put("kind", counter++);
		members.add(new SizzleInt());

		names.put("name", counter++);
		members.add(new SizzleString());
	}

	/**
	 * Construct a {@link ChangedFileProtoTuple}.
	 */
	public ChangedFileProtoTuple() {
		super(members, names);
	}

	@Override
	public String toJavaType() {
		return "sizzle.types.Diff.ChangedFile";
	}
}
