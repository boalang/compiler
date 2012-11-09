package sizzle.types;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A {@link ASTRootProtoTuple}.
 * 
 * @author rdyer
 */
public class ASTRootProtoTuple extends SizzleProtoTuple {
	private final static List<SizzleType> members = new ArrayList<SizzleType>();
	private final static Map<String, Integer> names = new HashMap<String, Integer>();

	static {
		int counter = 0;

		names.put("namespaces", counter++);
		members.add(new SizzleProtoList(new NamespaceProtoTuple()));
	}

	/**
	 * Construct a {@link ASTRootProtoTuple}.
	 */
	public ASTRootProtoTuple() {
		super(members, names);
	}

	@Override
	public String toJavaType() {
		return "sizzle.types.Ast.ASTRoot";
	}
}
