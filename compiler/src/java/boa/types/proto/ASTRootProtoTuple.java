package boa.types.proto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import boa.types.BoaProtoList;
import boa.types.BoaProtoTuple;
import boa.types.BoaType;

/**
 * A {@link ASTRootProtoTuple}.
 * 
 * @author rdyer
 */
public class ASTRootProtoTuple extends BoaProtoTuple {
	private final static List<BoaType> members = new ArrayList<BoaType>();
	private final static Map<String, Integer> names = new HashMap<String, Integer>();

	static {
		int counter = 0;

		names.put("namespaces", counter++);
		members.add(new BoaProtoList(new NamespaceProtoTuple()));
	}

	/**
	 * Construct a {@link ASTRootProtoTuple}.
	 */
	public ASTRootProtoTuple() {
		super(members, names);
	}

	/** @{inheritDoc} */
	@Override
	public String toJavaType() {
		return "boa.types.Ast.ASTRoot";
	}
}
