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

import boa.types.BoaInt;
import boa.types.BoaProtoList;
import boa.types.BoaProtoTuple;
import boa.types.BoaString;
import boa.types.BoaTime;
import boa.types.BoaType;
import boa.types.proto.enums.IssueLabelProtoMap;
import boa.types.proto.enums.IssuePriorityProtoMap;
import boa.types.proto.enums.IssueStatusProtoMap;

/**
 * A {@link IssueProtoTuple}.
 * 
 * @author rdyer
 */
public class IssueProtoTuple extends BoaProtoTuple {
	private final static List<BoaType> members = new ArrayList<BoaType>();
	private final static Map<String, Integer> names = new HashMap<String, Integer>();

	static {
		int counter = 0;

		names.put("id", counter++);
		members.add(new BoaString());

		names.put("status", counter++);
		members.add(new IssueStatusProtoMap());

		names.put("author", counter++);
		members.add(new PersonProtoTuple());

		names.put("assignee", counter++);
		members.add(new PersonProtoTuple());

		names.put("summary", counter++);
		members.add(new BoaString());

		names.put("description", counter++);
		members.add(new BoaString());

		names.put("created_date", counter++);
		members.add(new BoaTime());

		names.put("modified_date", counter++);
		members.add(new BoaTime());

		names.put("completed_date", counter++);
		members.add(new BoaTime());

		names.put("keywords", counter++);
		members.add(new BoaProtoList(new BoaString()));

		names.put("vote", counter++);
		members.add(new BoaInt());

		names.put("priority", counter++);
		members.add(new IssuePriorityProtoMap());

		names.put("files", counter++);
		members.add(new BoaProtoList(new AttachmentProtoTuple()));

		names.put("comments", counter++);
		members.add(new BoaProtoList(new IssueCommentProtoTuple()));

		/** The issues's associated milestone */
//		optional string milestone = 23;
		//optional string depends_on = 24;
		/** The issue's blocked or locked status */
//		optional string blocked = 25;
		/*
		optional string secrecy = 26;
		repeated IssueChange changes = 27;
		 */

		names.put("pullUrl", counter++);
		members.add(new BoaString());

		names.put("assignees", counter++);
		members.add(new BoaProtoList(new PersonProtoTuple()));

		names.put("number", counter++);
		members.add(new BoaInt());

		names.put("labels", counter++);
		members.add(new BoaProtoList(new IssueLabelProtoMap()));

		names.put("other_status", counter++);
		members.add(new BoaString());

		names.put("other_priority", counter++);
		members.add(new BoaString());

		names.put("other_labels", counter++);
		members.add(new BoaProtoList(new IssueLabelProtoMap()));
	}

	/**
	 * Construct a IssueProtoTuple.
	 */
	public IssueProtoTuple() {
		super(members, names);
	}

	/** @{inheritDoc} */
	@Override
	public String toJavaType() {
		return "boa.types.Issues.Issue";
	}
}
