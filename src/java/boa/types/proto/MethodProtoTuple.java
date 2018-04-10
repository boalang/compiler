// NOTE: This file was automatically generated - DO NOT EDIT
/*
 * Copyright 2017, Hridesh Rajan, Robert Dyer
 *                 Iowa State University of Science and Technology
 *                 and Bowling Green State University
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

/**
 * A {@link MethodProtoTuple}.
 *
 * @author rdyer
 */
public class MethodProtoTuple extends boa.types.BoaProtoTuple {
    private final static List<boa.types.BoaType> members = new ArrayList<boa.types.BoaType>();
    private final static Map<String, Integer> names = new HashMap<String, Integer>();

    static {
        int count = 0;

        names.put("name", count++);
        members.add(new boa.types.BoaString());

        names.put("modifiers", count++);
        members.add(new boa.types.BoaProtoList(new boa.types.proto.ModifierProtoTuple()));

        names.put("return_type", count++);
        members.add(new boa.types.proto.TypeProtoTuple());

        names.put("generic_parameters", count++);
        members.add(new boa.types.BoaProtoList(new boa.types.proto.TypeProtoTuple()));

        names.put("arguments", count++);
        members.add(new boa.types.BoaProtoList(new boa.types.proto.VariableProtoTuple()));

        names.put("exception_types", count++);
        members.add(new boa.types.BoaProtoList(new boa.types.proto.TypeProtoTuple()));

        names.put("statements", count++);
        members.add(new boa.types.BoaProtoList(new boa.types.proto.StatementProtoTuple()));

        names.put("structural_change_kind", count++);
        members.add(new boa.types.proto.enums.ChangeKindProtoMap());

        names.put("label_change_kind", count++);
        members.add(new boa.types.proto.enums.ChangeKindProtoMap());
    }

    /**
     * Construct a {@link MethodProtoTuple}.
     */
    public MethodProtoTuple() {
        super(members, names);
    }

    /** @{inheritDoc} */
    @Override
    public String toJavaType() {
        return "boa.types.Ast.Method";
    }
}
