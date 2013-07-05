package boa.types.proto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import boa.types.BoaInt;
import boa.types.BoaProtoList;
import boa.types.BoaProtoTuple;
import boa.types.BoaString;
import boa.types.BoaTime;
import boa.types.BoaType;

/**
 * A {@link RevisionProtoTuple}.
 * 
 * @author rdyer
 */
public class RevisionProtoTuple extends BoaProtoTuple {
	private final static List<BoaType> members = new ArrayList<BoaType>();
	private final static Map<String, Integer> names = new HashMap<String, Integer>();

	static {
		int counter = 0;

		names.put("id", counter++);
		members.add(new BoaInt());

		names.put("author", counter++);
		members.add(new PersonProtoTuple());

		names.put("committer", counter++);
		members.add(new PersonProtoTuple());

		names.put("commit_date", counter++);
		members.add(new BoaTime());

		names.put("log", counter++);
		members.add(new BoaString());

		names.put("files", counter++);
		members.add(new BoaProtoList(new ChangedFileProtoTuple()));
	}

	/**
	 * Construct a {@link RevisionProtoTuple}.
	 */
	public RevisionProtoTuple() {
		super(members, names);
	}

	/** @{inheritDoc} */
	@Override
	public String toJavaType() {
		return "boa.types.Code.Revision";
	}
}
