package boa.types.proto.transportation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import boa.types.BoaInt;
import boa.types.BoaProtoList;
import boa.types.BoaProtoTuple;
import boa.types.BoaString;
import boa.types.BoaType;
import boa.types.proto.enums.ForgeKindProtoMap;

public class PersonProtoTuple extends BoaProtoTuple{
	private final static List<BoaType> members = new ArrayList<BoaType>();
	private final static Map<String, Integer> names = new HashMap<String, Integer>();

	static {
		int counter = 0;

		names.put("state", counter++);
		members.add(new StateProtoMap());

		names.put("state_case", counter++);
		members.add(new BoaInt());

		names.put("AGE", counter++);
		members.add(new BoaInt());

		names.put("county", counter++);
		members.add(new BoaInt());

		names.put("city", counter++);
		members.add(new BoaInt());

		names.put("day", counter++);
		members.add(new BoaInt());

		names.put("month", counter++);
		members.add(new BoaInt());

		names.put("year", counter++);
		members.add(new BoaInt());

		names.put("day_week", counter++);
		members.add(new BoaInt());

		names.put("hour", counter++);
		members.add(new BoaInt());
		
		names.put("minute", counter++);
		members.add(new BoaInt());

		names.put("COUNTY", counter++);
		members.add(new BoaInt());
		
		names.put("persons", counter++);
		members.add(new BoaProtoList(new BoaString()));
		
		
		
//		names.put("maintainers", counter++);
//		members.add(new BoaProtoList(new PersonProtoTuple()));
//
//		names.put("developers", counter++);
//		members.add(new BoaProtoList(new PersonProtoTuple()));
//
//		names.put("code_repositories", counter++);
//		members.add(new BoaProtoList(new CodeRepositoryProtoTuple()));
//
//		names.put("issue_repositories", counter++);
//		members.add(new BoaProtoList(new IssueRepositoryProtoTuple()));

		names.put("kind", counter++);
		members.add(new ForgeKindProtoMap());
	}

	/**
	 * Construct a ProjectProtoTuple.
	 */
	public PersonProtoTuple() {
		super(members, names);
	}

	/** @{inheritDoc} */
	@Override
	public String toJavaType() {
		return "boa.types.transportation.Individual.Person";
	}
}
