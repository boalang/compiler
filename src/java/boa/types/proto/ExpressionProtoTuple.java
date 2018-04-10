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
 * A {@link ExpressionProtoTuple}.
 *
 * @author rdyer
 */
public class ExpressionProtoTuple extends boa.types.BoaProtoTuple {
    private final static List<boa.types.BoaType> members = new ArrayList<boa.types.BoaType>();
    private final static Map<String, Integer> names = new HashMap<String, Integer>();

    static {
        int count = 0;

        names.put("kind", count++);
        members.add(new boa.types.proto.enums.ExpressionKindProtoMap());

        names.put("string_1", count++);
        members.add(new boa.types.BoaString());

        names.put("string_2", count++);
        members.add(new boa.types.BoaString());

        names.put("expression_1", count++);
        members.add(new boa.types.proto.ExpressionProtoTuple());

        names.put("expression_2", count++);
        members.add(new boa.types.proto.ExpressionProtoTuple());

        names.put("expression_3", count++);
        members.add(new boa.types.proto.ExpressionProtoTuple());

        names.put("expressions_1", count++);
        members.add(new boa.types.BoaProtoList(new boa.types.proto.ExpressionProtoTuple()));

        names.put("type_1", count++);
        members.add(new boa.types.proto.TypeProtoTuple());

        names.put("types_1", count++);
        members.add(new boa.types.BoaProtoList(new boa.types.proto.TypeProtoTuple()));

        names.put("declaration_1", count++);
        members.add(new boa.types.proto.DeclarationProtoTuple());

        names.put("bool_1", count++);
        members.add(new boa.types.BoaBool());

        names.put("int32_1", count++);
        members.add(new boa.types.BoaInt());

        names.put("variables_1", count++);
        members.add(new boa.types.BoaProtoList(new boa.types.proto.VariableProtoTuple()));

        names.put("statement_1", count++);
        members.add(new boa.types.proto.StatementProtoTuple());

        names.put("modifiers_1", count++);
        members.add(new boa.types.BoaProtoList(new boa.types.proto.ModifierProtoTuple()));

        names.put("strings_1", count++);
        members.add(new boa.types.BoaProtoList(new boa.types.BoaString()));

        names.put("modifier_1", count++);
        members.add(new boa.types.proto.ModifierProtoTuple());

        names.put("structural_change_kind", count++);
        members.add(new boa.types.proto.enums.ChangeKindProtoMap());

        names.put("label_change_kind", count++);
        members.add(new boa.types.proto.enums.ChangeKindProtoMap());
    }

    /**
     * Construct a {@link ExpressionProtoTuple}.
     */
    public ExpressionProtoTuple() {
        super(members, names);
    }

    /** @{inheritDoc} */
    @Override
    public String toJavaType() {
        return "boa.types.Ast.Expression";
    }
}
