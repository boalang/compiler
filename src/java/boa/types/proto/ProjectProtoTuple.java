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
		names.put("name", 0);
		members.add(new BoaString());

		names.put("project_url", 1);
		members.add(new BoaString());

		names.put("homepage_url", 2);
		members.add(new BoaString());

		names.put("created_date", 3);
		members.add(new BoaTime());

		names.put("id", 4);
		members.add(new BoaString());

		names.put("description", 5);
		members.add(new BoaString());

		names.put("operating_systems", 6);
		members.add(new BoaProtoList(new BoaString()));

		names.put("programming_languages", 7);
		members.add(new BoaProtoList(new BoaString()));

		names.put("databases", 8);
		members.add(new BoaProtoList(new BoaString()));

		names.put("licenses", 9);
		members.add(new BoaProtoList(new BoaString()));

		names.put("interfaces", 10);
		members.add(new BoaProtoList(new BoaString()));

		names.put("audiences", 11);
		members.add(new BoaProtoList(new BoaString()));

		names.put("topics", 12);
		members.add(new BoaProtoList(new BoaString()));

		names.put("donations", 13);
		members.add(new BoaBool());

		names.put("maintainers", 14);
		members.add(new BoaProtoList(new PersonProtoTuple()));

		names.put("developers", 15);
		members.add(new BoaProtoList(new PersonProtoTuple()));

		names.put("code_repositories", 16);
		members.add(new BoaProtoList(new CodeRepositoryProtoTuple()));

		names.put("bug_repositories", 17);
		members.add(new BoaProtoList(new BugRepositoryProtoTuple()));
	}

	/**
	 * Construct a ProjectProtoTuple.
	 */
	public ProjectProtoTuple() {
		super(members, names);
	}

	@Override
	public String toJavaType() {
		return "boa.types.Toplevel.Project";
	}
}
