/*
 * Copyright 2018, Hridesh Rajan, Robert Schmidt,
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
import boa.types.BoaProtoTuple;
import boa.types.BoaType;

/**
 * A {@link PositionInfoProtoTuple}.
 *
 * @author robert2
 */
public class PositionInfoProtoTuple extends BoaProtoTuple {
	private final static List<BoaType> members = new ArrayList<BoaType>();
	private final static Map<String, Integer> names = new HashMap<String, Integer>();

	static {
		int count = 0;

		names.put("start_pos", count++);
		members.add(new BoaInt());

		names.put("length", count++);
		members.add(new BoaInt());

		names.put("start_line", count++);
		members.add(new BoaInt());

		names.put("start_col", count++);
		members.add(new BoaInt());

		names.put("end_line", count++);
		members.add(new BoaInt());

		names.put("end_col", count++);
		members.add(new BoaInt());
	}

	/**
	 * Construct a {@link PositionInfoProtoTuple}.
	 */
	public PositionInfoProtoTuple() {
		super(members, names);
	}

	/** @{inheritDoc} */
	@Override
	public String toJavaType() {
		return "boa.types.Ast.PositionInfo";
	}
}
