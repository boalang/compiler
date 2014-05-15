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
		names.put("name", 0);
		members.add(new SizzleString());

		names.put("kind", 1);
		members.add(new SizzleInt());

		names.put("parsed", 2);
		members.add(new SizzleBool());

		names.put("namespaces", 3);
		members.add(new SizzleProtoList(new NamespaceProtoTuple()));

		names.put("content", 4);
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
