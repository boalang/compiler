package boa.types.proto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import boa.types.BoaInt;
import boa.types.BoaProtoTuple;
import boa.types.BoaString;
import boa.types.BoaTime;
import boa.types.BoaType;

/**
 * A {@link BoaProtoTuple}.
 * 
 * @author rdyer
 * 
 */
public class BugProtoTuple extends BoaProtoTuple {
	private final static List<BoaType> members = new ArrayList<BoaType>();
	private final static Map<String, Integer> names = new HashMap<String, Integer>();

	static {
		int counter = 0;

		names.put("id", counter++);
		members.add(new BoaInt());

		names.put("reporter", counter++);
		members.add(new PersonProtoTuple());

		names.put("reported_date", counter++);
		members.add(new BoaTime());

		names.put("closed_date", counter++);
		members.add(new BoaTime());

		names.put("summary", counter++);
		members.add(new BoaString());

		names.put("description", counter++);
		members.add(new BoaString());

		names.put("status", counter++);
		members.add(new BoaInt());

		names.put("severity", counter++);
		members.add(new BoaString());
	}

	/**
	 * Construct a ProjectProtoTuple.
	 */
	public BugProtoTuple() {
		super(members, names);
	}

	/** @{inheritDoc} */
	@Override
	public String toJavaType() {
		return "boa.types.Bugs.Bug";
	}
}
