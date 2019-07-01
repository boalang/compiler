/*
 * Copyright 2014, Hridesh Rajan, Robert Dyer, 
 *                 and Iowa State University of Science and Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
import boa.types.proto.enums.ForgeKindProtoMap;

/**
 * A {@link BoaProtoTuple}.
 * 
 * @author rdyer
 */
public class ProjectProtoTuple extends BoaProtoTuple {
	private final static List<BoaType> members = new ArrayList<BoaType>();
	private final static Map<String, Integer> names = new HashMap<String, Integer>();

	static {
		int counter = 0;

		names.put("id", counter++);
		members.add(new BoaString());

		names.put("name", counter++);
		members.add(new BoaString());

		names.put("project_url", counter++);
		members.add(new BoaString());

		names.put("homepage_url", counter++);
		members.add(new BoaString());

		names.put("created_date", counter++);
		members.add(new BoaTime());

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

		names.put("status", counter++);
		members.add(new BoaProtoList(new BoaString()));

		names.put("translations", counter++);
		members.add(new BoaProtoList(new BoaString()));

		names.put("donations", counter++);
		members.add(new BoaBool());

		names.put("maintainers", counter++);
		members.add(new BoaProtoList(new PersonProtoTuple()));

		names.put("developers", counter++);
		members.add(new BoaProtoList(new PersonProtoTuple()));

		names.put("code_repositories", counter++);
		members.add(new BoaProtoList(new CodeRepositoryProtoTuple()));

		names.put("issue_repositories", counter++);
		members.add(new BoaProtoList(new IssueRepositoryProtoTuple()));

		names.put("kind", counter++);
		members.add(new ForgeKindProtoMap());
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
