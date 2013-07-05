package boa.types.proto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import boa.types.BoaProtoTuple;
import boa.types.BoaString;
import boa.types.BoaType;
import boa.types.proto.enums.ChangeKindProtoMap;
import boa.types.proto.enums.FileKindProtoMap;

/**
 * A {@link ChangedFileProtoTuple}.
 * 
 * @author rdyer
 */
public class ChangedFileProtoTuple extends BoaProtoTuple {
	private final static List<BoaType> members = new ArrayList<BoaType>();
	private final static Map<String, Integer> names = new HashMap<String, Integer>();

	static {
		int counter = 0;

		names.put("change", counter++);
		members.add(new ChangeKindProtoMap());

		names.put("kind", counter++);
		members.add(new FileKindProtoMap());

		names.put("name", counter++);
		members.add(new BoaString());
	}

	/**
	 * Construct a {@link ChangedFileProtoTuple}.
	 */
	public ChangedFileProtoTuple() {
		super(members, names);
	}

	@Override
	public String toJavaType() {
		return "boa.types.Diff.ChangedFile";
	}
}
