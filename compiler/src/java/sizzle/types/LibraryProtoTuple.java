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
public class LibraryProtoTuple extends SizzleProtoTuple {
	private final static List<SizzleType> members = new ArrayList<SizzleType>();
	private final static Map<String, Integer> names = new HashMap<String, Integer>();

	static {
		names.put("filename", 0);
		members.add(new SizzleString());
		
		names.put("name", 1);
		members.add(new SizzleString());
		
		names.put("version", 2);
		members.add(new SizzleString());

		names.put("files", 3);
		members.add(new SizzleProtoList(new FileProtoTuple()));
	}

	/**
	 * Construct a ProjectProtoTuple.
	 */
	public LibraryProtoTuple() {
		super(members, names);
	}

	@Override
	public String toJavaType() {
		return "sizzle.types.Ast.Library";
	}
}
