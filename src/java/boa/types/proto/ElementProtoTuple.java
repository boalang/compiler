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

import boa.types.BoaProtoList;
import boa.types.BoaProtoTuple;
import boa.types.BoaString;
import boa.types.BoaType;
import boa.types.proto.enums.ElementKindProtoMap;

/**
 * A {@link ElementProtoTuple}.
 *
 * @author robert2
 */
public class ElementProtoTuple extends BoaProtoTuple {
	private final static List<BoaType> members = new ArrayList<BoaType>();
	private final static Map<String, Integer> names = new HashMap<String, Integer>();

	static {
		int counter = 0;

		names.put("tag", counter++);
		members.add(new BoaString());

		names.put("kind", counter++);
		members.add(new ElementKindProtoMap());

		names.put("elements", counter++);
		members.add(new BoaProtoList(new ElementProtoTuple()));

		names.put("text", counter++);
		members.add(new BoaProtoList(new BoaString()));

		names.put("data", counter++);
		members.add(new BoaProtoList(new BoaString()));

		names.put("attributes", counter++);
		members.add(new BoaProtoList(new AttributeProtoTuple()));

		names.put("script", counter++);
		members.add(new NamespaceProtoTuple());

		names.put("php", counter++);
		members.add(new NamespaceProtoTuple());

		names.put("var_decl", counter++);
		members.add(new BoaProtoList(new VariableProtoTuple()));

		names.put("title", counter++);
		members.add(new BoaString());

		names.put("processing_instruction", counter++);
		members.add(new BoaProtoList(new AttributeProtoTuple()));
	}

	public ElementProtoTuple() {
		super(members, names);
	}

	/** @{inheritDoc} */
	@Override
	public String toJavaType() {
		return "boa.types.Ast.Element";
	}
}
