package boa.types.proto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import boa.types.BoaProtoTuple;
import boa.types.BoaString;
import boa.types.BoaType;

/**
 * A {@link BoaProtoTuple}.
 * 
 * @author rdyer
 * 
 */
public class PersonProtoTuple extends BoaProtoTuple {
	private final static List<BoaType> members = new ArrayList<BoaType>();
	private final static Map<String, Integer> names = new HashMap<String, Integer>();

	static {
		names.put("username", 0);
		members.add(new BoaString());

		names.put("real_name", 1);
		members.add(new BoaString());

		names.put("email", 2);
		members.add(new BoaString());
	}

	/**
	 * Construct a ProjectProtoTuple.
	 */
	public PersonProtoTuple() {
		super(members, names);
	}

	@Override
	public String toJavaType() {
		return "boa.types.Shared.Person";
	}
}
