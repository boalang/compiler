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
		names.put("id", 0);
		members.add(new BoaInt());

		names.put("author", 1);
		members.add(new PersonProtoTuple());

		names.put("committer", 2);
		members.add(new PersonProtoTuple());

		names.put("commit_date", 3);
		members.add(new BoaTime());

		names.put("log", 4);
		members.add(new BoaString());

		names.put("files", 5);
		members.add(new BoaProtoList(new ChangedFileProtoTuple()));
	}

	/**
	 * Construct a {@link RevisionProtoTuple}.
	 */
	public RevisionProtoTuple() {
		super(members, names);
	}

	@Override
	public String toJavaType() {
		return "boa.types.Code.Revision";
	}
}
