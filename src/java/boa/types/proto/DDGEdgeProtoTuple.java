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

import boa.types.BoaProtoTuple;
import boa.types.BoaString;
import boa.types.BoaType;
import boa.types.proto.enums.DDGEdgeLabelProtoMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A {@link DDGEdgeProtoTuple}.
 *
 * @author marafat
 */
public class DDGEdgeProtoTuple extends BoaProtoTuple{
    private final static List<BoaType> members = new ArrayList<BoaType>();
    private final static Map<String, Integer> names = new HashMap<String, Integer>();

    static {
        int counter = 0;

        names.put("src", counter++);
        members.add(new BoaString());

        names.put("dest", counter++);
        members.add(new DDGNodeProtoTuple());

        names.put("label", counter++);
        members.add(new DDGEdgeLabelProtoMap());
    }

    /**
     * Construct a {@link DDGEdgeProtoTuple}.
     */
    public DDGEdgeProtoTuple() {
        super(members, names);
    }

    /** @{inheritDoc} */
    @Override
    public String toJavaType() {
        return "boa.types.ddg.DDGEdge";
    }
}
