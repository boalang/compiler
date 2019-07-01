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

import boa.types.BoaProtoList;
import boa.types.BoaProtoTuple;
import boa.types.BoaString;
import boa.types.BoaType;
import boa.types.proto.enums.ModifierKindProtoMap;
import boa.types.proto.enums.VisibilityProtoMap;

/**
 * A {@link ModifierProtoTuple}.
 * 
 * @author rdyer
 */
public class ModifierProtoTuple extends BoaProtoTuple {
	private final static List<BoaType> members = new ArrayList<BoaType>();
	private final static Map<String, Integer> names = new HashMap<String, Integer>();

	static {
		int counter = 0;

		names.put("kind", counter++);
		members.add(new ModifierKindProtoMap());

		names.put("visibility", counter++);
		members.add(new VisibilityProtoMap());

		names.put("annotation_name", counter++);
		members.add(new BoaString());

		names.put("annotation_members", counter++);
		members.add(new BoaProtoList(new BoaString()));

		names.put("annotation_values", counter++);
		members.add(new BoaProtoList(new ExpressionProtoTuple()));

		names.put("other", counter++);
		members.add(new BoaString());
	}

	/**
	 * Construct a {@link ModifierProtoTuple}.
	 */
	public ModifierProtoTuple() {
		super(members, names);
	}

	/** @{inheritDoc} */
	@Override
	public String toJavaType() {
		return "boa.types.Ast.Modifier";
	}
}
