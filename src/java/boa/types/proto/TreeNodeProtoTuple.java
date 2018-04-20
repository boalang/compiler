/*
 * Copyright 2018, Robert Dyer, Mohd Arafat
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

import boa.types.BoaInt;
import boa.types.BoaProtoList;
import boa.types.BoaProtoTuple;
import boa.types.BoaType;
import boa.types.proto.enums.TreeNodeTypeProtoMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A {@link TreeNodeProtoTuple}.
 *
 * @author marafat
 */
public class TreeNodeProtoTuple extends BoaProtoTuple {
    private final static List<BoaType> members = new ArrayList<BoaType>();
    private final static Map<String, Integer> names = new HashMap<String, Integer>();

    static {
        int counter = 0;

        names.put("kind", counter++);
        members.add(new TreeNodeTypeProtoMap());

        names.put("id", counter++);
        members.add(new BoaInt());

        names.put("stmt", counter++);
        members.add(new StatementProtoTuple());

        names.put("expr", counter++);
        members.add(new ExpressionProtoTuple());

        names.put("children", counter++);
        members.add(new BoaProtoList(new TreeNodeProtoTuple()));
    }

    /**
     * Construct a {@link TreeNodeProtoTuple}.
     */
    public TreeNodeProtoTuple() {
        super(members, names);
    }

    /** @{inheritDoc} */
    @Override
    public String toJavaType() {
        return "boa.graphs.trees.TreeNode";
    }
}
