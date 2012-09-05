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
public class FileProtoTuple extends SizzleProtoTuple {
	private final static List<SizzleType> members = new ArrayList<SizzleType>();
	private final static Map<String, Integer> names = new HashMap<String, Integer>();

	static {
		names.put("name", 0);
		members.add(new SizzleString());

		names.put("content", 1);
		members.add(new SizzleString());

		names.put("file_type", 2);
		members.add(new SizzleInt());

		names.put("pkg", 3);
		members.add(new PackageProtoTuple());
	}

	/**
	 * Construct a ProjectProtoTuple.
	 */
	public FileProtoTuple() {
		super(members, names);
	}

	@Override
	public String toJavaType() {
		return "sizzle.types.Ast.File";
	}
}
