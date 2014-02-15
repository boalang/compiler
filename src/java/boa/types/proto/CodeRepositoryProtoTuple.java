package boa.types.proto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import boa.types.BoaProtoList;
import boa.types.BoaProtoTuple;
import boa.types.BoaString;
import boa.types.BoaType;
import boa.types.proto.enums.RepositoryKindProtoMap;

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
		int counter = 0;

		names.put("url", counter++);
		members.add(new BoaString());

		names.put("kind", counter++);
		members.add(new RepositoryKindProtoMap());

		names.put("revisions", counter++);
		members.add(new BoaProtoList(new RevisionProtoTuple()));
	}

	/**
	 * Construct a ProjectProtoTuple.
	 */
	public CodeRepositoryProtoTuple() {
		super(members, names);
	}

	/** @{inheritDoc} */
	@Override
	public String toJavaType() {
		return "boa.types.Code.CodeRepository";
	}
}
