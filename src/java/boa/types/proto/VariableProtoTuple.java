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
import boa.types.proto.enums.ChangeKindProtoMap;

/**
 * A {@link VariableProtoTuple}.
 * 
 * @author rdyer
 */
public class VariableProtoTuple extends BoaProtoTuple {
	private final static List<BoaType> members = new ArrayList<BoaType>();
	private final static Map<String, Integer> names = new HashMap<String, Integer>();

	static {
		int counter = 0;

		names.put("name", counter++);
		members.add(new BoaString());

		names.put("variable_type", counter++);
		members.add(new TypeProtoTuple());

		names.put("modifiers", counter++);
		members.add(new BoaProtoList(new ModifierProtoTuple()));

		names.put("initializer", counter++);
		members.add(new ExpressionProtoTuple());

		names.put("comments", counter++);
		members.add(new BoaProtoList(new CommentProtoTuple()));
		
		names.put("key", counter++);
		members.add(new BoaInt());
		
		names.put("declaring_type", counter++);
		members.add(new BoaInt());
		
		names.put("computed_name", counter++);
		members.add(new ExpressionProtoTuple());
		
		names.put("dollar_sign", counter++);
		members.add(new BoaBool());
		
		names.put("expressions", counter++);
		members.add(new BoaProtoList(new ExpressionProtoTuple()));
		
		names.put("change", counter++);
		members.add(new ChangeKindProtoMap());
		
		names.put("id", counter++);
		members.add(new BoaInt());
	}

	/**
	 * Construct a {@link VariableProtoTuple}.
	 */
	public VariableProtoTuple() {
		super(members, names);
	}

	/** @{inheritDoc} */
	@Override
	public String toJavaType() {
		return "boa.types.Ast.Variable";
	}
}
