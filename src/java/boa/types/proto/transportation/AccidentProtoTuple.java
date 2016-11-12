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
package boa.types.proto.transportation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import boa.types.BoaInt;
import boa.types.BoaProtoList;
import boa.types.BoaProtoTuple;
import boa.types.BoaString;
import boa.types.BoaType;
import boa.types.proto.enums.ForgeKindProtoMap;

/**
 * A {@link BoaProtoTuple}.
 * 
 * @author rdyer
 */
public class AccidentProtoTuple extends BoaProtoTuple {
	private final static List<BoaType> members = new ArrayList<BoaType>();
	private final static Map<String, Integer> names = new HashMap<String, Integer>();

	static {
		int counter = 0;

		names.put("state", counter++);
		members.add(new StateProtoMap());

		names.put("state_case", counter++);
		members.add(new BoaInt());

		names.put("ve_total", counter++);
		members.add(new BoaInt());

		names.put("ve_forms", counter++);
		members.add(new BoaInt());

		names.put("pvh_invl", counter++);
		members.add(new BoaInt());

		names.put("peds", counter++);
		members.add(new BoaInt());

		names.put("pernotmvit", counter++);
		members.add(new BoaInt());

		names.put("permvit", counter++);
		members.add(new BoaInt());

		names.put("persons", counter++);
		members.add(new BoaInt());

		names.put("county", counter++);
		members.add(new BoaInt());

		names.put("city", counter++);
		members.add(new BoaInt());

		names.put("DAY", counter++);
		members.add(new BoaInt());

		names.put("MONTH", counter++);
		members.add(new BoaInt());

		names.put("YEAR", counter++);
		members.add(new BoaInt());

		names.put("DAY_WEEK", counter++);
		members.add(new BoaInt());

		names.put("HOUR", counter++);
		members.add(new BoaInt());
		
		names.put("MINUTE", counter++);
		members.add(new BoaInt());


		names.put("nhs", counter++);
		members.add(new BoaInt());
		
		
		names.put("rur_urb", counter++);
		members.add(new BoaInt());
		
		names.put("func_sys", counter++);
		members.add(new BoaInt());
		names.put("rd_owner", counter++);
		members.add(new BoaInt());
		
		names.put("ROUTE", counter++);
		members.add(new BoaInt());
		
		names.put("LATITUDE", counter++);
		members.add(new BoaInt());
		
		names.put("LONGITUD", counter++);
		members.add(new BoaInt());
		
		names.put("COUNTY", counter++);
		members.add(new BoaInt());
		
		names.put("persons", counter++);
		members.add(new BoaProtoList(new PersonProtoTuple()));
		
		names.put("vehicle", counter++);
		members.add(new BoaProtoList(new VehicleProtoTuple()));
		
		
		
//		names.put("maintainers", counter++);
//		members.add(new BoaProtoList(new PersonProtoTuple()));
//
//		names.put("developers", counter++);
//		members.add(new BoaProtoList(new PersonProtoTuple()));
//
//		names.put("code_repositories", counter++);
//		members.add(new BoaProtoList(new CodeRepositoryProtoTuple()));
//
//		names.put("issue_repositories", counter++);
//		members.add(new BoaProtoList(new IssueRepositoryProtoTuple()));

		names.put("kind", counter++);
		members.add(new ForgeKindProtoMap());
	}

	/**
	 * Construct a ProjectProtoTuple.
	 */
	public AccidentProtoTuple() {
		super(members, names);
	}

	/** @{inheritDoc} */
	@Override
	public String toJavaType() {
		return "boa.types.transportation.Accident.Crash";
	}
}
