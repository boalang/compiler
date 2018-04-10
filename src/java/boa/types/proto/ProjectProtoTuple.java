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
 * A {@link ProjectProtoTuple}.
 *
 * @author rdyer
 */
public class ProjectProtoTuple extends boa.types.BoaProtoTuple {
    private final static List<boa.types.BoaType> members = new ArrayList<boa.types.BoaType>();
    private final static Map<String, Integer> names = new HashMap<String, Integer>();

    static {
        int count = 0;

        names.put("id", count++);
        members.add(new boa.types.BoaString());

        names.put("name", count++);
        members.add(new boa.types.BoaString());

        names.put("project_url", count++);
        members.add(new boa.types.BoaString());

        names.put("homepage_url", count++);
        members.add(new boa.types.BoaString());

        names.put("created_date", count++);
        members.add(new boa.types.BoaInt());

        names.put("description", count++);
        members.add(new boa.types.BoaString());

        names.put("operating_systems", count++);
        members.add(new boa.types.BoaProtoList(new boa.types.BoaString()));

        names.put("programming_languages", count++);
        members.add(new boa.types.BoaProtoList(new boa.types.BoaString()));

        names.put("databases", count++);
        members.add(new boa.types.BoaProtoList(new boa.types.BoaString()));

        names.put("licenses", count++);
        members.add(new boa.types.BoaProtoList(new boa.types.BoaString()));

        names.put("interfaces", count++);
        members.add(new boa.types.BoaProtoList(new boa.types.BoaString()));

        names.put("audiences", count++);
        members.add(new boa.types.BoaProtoList(new boa.types.BoaString()));

        names.put("topics", count++);
        members.add(new boa.types.BoaProtoList(new boa.types.BoaString()));

        names.put("status", count++);
        members.add(new boa.types.BoaProtoList(new boa.types.BoaString()));

        names.put("translations", count++);
        members.add(new boa.types.BoaProtoList(new boa.types.BoaString()));

        names.put("donations", count++);
        members.add(new boa.types.BoaBool());

        names.put("maintainers", count++);
        members.add(new boa.types.BoaProtoList(new boa.types.proto.PersonProtoTuple()));

        names.put("developers", count++);
        members.add(new boa.types.BoaProtoList(new boa.types.proto.PersonProtoTuple()));

        names.put("code_repositories", count++);
        members.add(new boa.types.BoaProtoList(new boa.types.proto.CodeRepositoryProtoTuple()));

        names.put("issue_repositories", count++);
        members.add(new boa.types.BoaProtoList(new boa.types.proto.IssueRepositoryProtoTuple()));

        names.put("kind", count++);
        members.add(new boa.types.proto.enums.ForgeKindProtoMap());

        names.put("forked", count++);
        members.add(new boa.types.BoaBool());

        names.put("forks", count++);
        members.add(new boa.types.BoaInt());

        names.put("stars", count++);
        members.add(new boa.types.BoaInt());
    }

    /**
     * Construct a {@link ProjectProtoTuple}.
     */
    public ProjectProtoTuple() {
        super(members, names);
    }

    /** @{inheritDoc} */
    @Override
    public String toJavaType() {
        return "boa.types.Toplevel.Project";
    }
}
