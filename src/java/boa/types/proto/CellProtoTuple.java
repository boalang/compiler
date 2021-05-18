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
import boa.types.BoaInt;
import boa.types.BoaProtoList;
import boa.types.BoaProtoTuple;
import boa.types.BoaString;
import boa.types.BoaType;
import boa.types.proto.enums.CellKindProtoMap;
import boa.types.proto.enums.ChangeKindProtoMap;
import boa.types.proto.enums.StatementKindProtoMap;

/**
 * A {@link CellProtoTuple}.
 * 
 * @author sumon
 */
public class CellProtoTuple extends BoaProtoTuple {
	private final static List<BoaType> members = new ArrayList<BoaType>();
	private final static Map<String, Integer> names = new HashMap<String, Integer>();

	static {
		int counter = 0;
		
		names.put("cell_kind", counter++);
		members.add(new CellKindProtoMap());
		
		names.put("cell_id", counter++);
		members.add(new BoaInt());
		
		names.put("execution_count", counter++);
		members.add(new BoaInt());
		
		names.put("namespaces", counter++);
		members.add(new BoaProtoList(new NamespaceProtoTuple()));
		
		names.put("parse_error", counter++);
		members.add(new BoaBool());
		
		names.put("markdown_text", counter++);
		members.add(new BoaString());
		
		names.put("output_type", counter++);
		members.add(new BoaString());
		
		names.put("error_name", counter++);
		members.add(new BoaString());
		
		names.put("error_value", counter++);
		members.add(new BoaString());
		
		names.put("traceback", counter++);
		members.add(new BoaString());
		
	}

	/**
	 * Construct a {@link NamespaceProtoTuple2}.
	 */
	public CellProtoTuple() {
		super(members, names);
	}

	/** @{inheritDoc} */
	@Override
	public String toJavaType() {
		return "boa.types.Ast.Cell";
	}
}
