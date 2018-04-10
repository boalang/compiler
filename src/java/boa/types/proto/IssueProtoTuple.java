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
 * A {@link IssueProtoTuple}.
 *
 * @author rdyer
 */
public class IssueProtoTuple extends boa.types.BoaProtoTuple {
    private final static List<boa.types.BoaType> members = new ArrayList<boa.types.BoaType>();
    private final static Map<String, Integer> names = new HashMap<String, Integer>();

    static {
        int count = 0;

        names.put("id", count++);
        members.add(new boa.types.BoaString());

        names.put("status", count++);
        members.add(new boa.types.proto.enums.IssueStatusProtoMap());

        names.put("author", count++);
        members.add(new boa.types.proto.PersonProtoTuple());

        names.put("assignee", count++);
        members.add(new boa.types.proto.PersonProtoTuple());

        names.put("summary", count++);
        members.add(new boa.types.BoaString());

        names.put("description", count++);
        members.add(new boa.types.BoaString());

        names.put("created_date", count++);
        members.add(new boa.types.BoaInt());

        names.put("modified_date", count++);
        members.add(new boa.types.BoaInt());

        names.put("completed_date", count++);
        members.add(new boa.types.BoaInt());

        names.put("keywords", count++);
        members.add(new boa.types.BoaProtoList(new boa.types.BoaString()));

        names.put("vote", count++);
        members.add(new boa.types.BoaInt());

        names.put("priority", count++);
        members.add(new boa.types.proto.enums.IssuePriorityProtoMap());

        names.put("files", count++);
        members.add(new boa.types.BoaProtoList(new boa.types.proto.AttachmentProtoTuple()));

        names.put("comments", count++);
        members.add(new boa.types.BoaProtoList(new boa.types.proto.IssueCommentProtoTuple()));

        names.put("milestone", count++);
        members.add(new boa.types.BoaString());

        names.put("blocked", count++);
        members.add(new boa.types.BoaString());

        names.put("pullUrl", count++);
        members.add(new boa.types.BoaString());

        names.put("assignees", count++);
        members.add(new boa.types.BoaProtoList(new boa.types.proto.PersonProtoTuple()));

        names.put("number", count++);
        members.add(new boa.types.BoaInt());

        names.put("labels", count++);
        members.add(new boa.types.BoaProtoList(new boa.types.proto.enums.IssueLabelProtoMap()));

        names.put("other_status", count++);
        members.add(new boa.types.BoaString());

        names.put("other_priority", count++);
        members.add(new boa.types.BoaString());

        names.put("other_labels", count++);
        members.add(new boa.types.BoaProtoList(new boa.types.BoaString()));
    }

    /**
     * Construct a {@link IssueProtoTuple}.
     */
    public IssueProtoTuple() {
        super(members, names);
    }

    /** @{inheritDoc} */
    @Override
    public String toJavaType() {
        return "boa.types.Issues.Issue";
    }
}
