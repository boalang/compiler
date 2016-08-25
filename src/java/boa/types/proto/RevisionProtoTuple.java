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
		members.add(new BoaString());

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

		names.put("parents", counter++);
		members.add(new BoaProtoList(new BoaInt()));

		names.put("children", counter++);
		members.add(new BoaProtoList(new BoaInt()));
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
