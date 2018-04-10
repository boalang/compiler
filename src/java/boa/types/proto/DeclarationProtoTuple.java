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
 * A {@link DeclarationProtoTuple}.
 *
 * @author rdyer
 */
public class DeclarationProtoTuple extends boa.types.BoaProtoTuple {
    private final static List<boa.types.BoaType> members = new ArrayList<boa.types.BoaType>();
    private final static Map<String, Integer> names = new HashMap<String, Integer>();

    static {
        int count = 0;

        names.put("name", count++);
        members.add(new boa.types.BoaString());

        names.put("kind", count++);
        members.add(new boa.types.proto.enums.TypeKindProtoMap());

        names.put("modifiers", count++);
        members.add(new boa.types.BoaProtoList(new boa.types.proto.ModifierProtoTuple()));

        names.put("generic_parameters", count++);
        members.add(new boa.types.BoaProtoList(new boa.types.proto.TypeProtoTuple()));

        names.put("parents", count++);
        members.add(new boa.types.BoaProtoList(new boa.types.proto.TypeProtoTuple()));

        names.put("methods", count++);
        members.add(new boa.types.BoaProtoList(new boa.types.proto.MethodProtoTuple()));

        names.put("fields", count++);
        members.add(new boa.types.BoaProtoList(new boa.types.proto.VariableProtoTuple()));

        names.put("nested_declarations", count++);
        members.add(new boa.types.BoaProtoList(new boa.types.proto.DeclarationProtoTuple()));

        names.put("structural_change_kind", count++);
        members.add(new boa.types.proto.enums.ChangeKindProtoMap());

        names.put("label_change_kind", count++);
        members.add(new boa.types.proto.enums.ChangeKindProtoMap());
    }

    /**
     * Construct a {@link DeclarationProtoTuple}.
     */
    public DeclarationProtoTuple() {
        super(members, names);
    }

    /** @{inheritDoc} */
    @Override
    public String toJavaType() {
        return "boa.types.Ast.Declaration";
    }
}
