package boa.types.proto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import boa.types.BoaBool;
import boa.types.BoaProtoList;
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
public class ProjectProtoTuple extends BoaProtoTuple {
	private final static List<BoaType> members = new ArrayList<BoaType>();
	private final static Map<String, Integer> names = new HashMap<String, Integer>();

	static {
		int counter = 0;

		names.put("name", counter++);
		members.add(new BoaString());

		names.put("project_url", counter++);
		members.add(new BoaString());

		names.put("homepage_url", counter++);
		members.add(new BoaString());

		names.put("created_date", counter++);
		members.add(new BoaTime());

		names.put("id", counter++);
		members.add(new BoaString());

		names.put("description", counter++);
		members.add(new BoaString());

		names.put("operating_systems", counter++);
		members.add(new BoaProtoList(new BoaString()));

		names.put("programming_languages", counter++);
		members.add(new BoaProtoList(new BoaString()));

		names.put("databases", counter++);
		members.add(new BoaProtoList(new BoaString()));

		names.put("licenses", counter++);
		members.add(new BoaProtoList(new BoaString()));

		names.put("interfaces", counter++);
		members.add(new BoaProtoList(new BoaString()));

		names.put("audiences", counter++);
		members.add(new BoaProtoList(new BoaString()));

		names.put("topics", counter++);
		members.add(new BoaProtoList(new BoaString()));

		names.put("donations", counter++);
		members.add(new BoaBool());

		names.put("maintainers", counter++);
		members.add(new BoaProtoList(new PersonProtoTuple()));

		names.put("developers", counter++);
		members.add(new BoaProtoList(new PersonProtoTuple()));

		names.put("code_repositories", counter++);
		members.add(new BoaProtoList(new CodeRepositoryProtoTuple()));

		names.put("bug_repositories", counter++);
		members.add(new BoaProtoList(new BugRepositoryProtoTuple()));
	}

	/**
	 * Construct a ProjectProtoTuple.
	 */
	public ProjectProtoTuple() {
		super(members, names);
	}

	/** @{inheritDoc} */
	@Override
	public String toJavaType() {
		return "boa.types.Toplevel.Project";
	}
}
