package boa.types.proto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import boa.types.*;

/**
 * A {@link BoaProtoTuple}.
 * 
 * @author nmtiwari
 */
 public class CrashProtoTuple extends BoaProtoTuple {
	private final static List < BoaType> members = new ArrayList<BoaType>();
	private final static Map <String, Integer> names = new HashMap<String, Integer>();

	static {
		int counter = 0;

		names.put("state", counter++);
		members.add(new STATEProtoMap());

		names.put("ST_CASE", counter++);
		members.add(new BoaInt());

		names.put("VE_TOTAL", counter++);
		members.add(new BoaInt());

		names.put("VE_FORMS", counter++);
		members.add(new BoaInt());

		names.put("PVH_INVL", counter++);
		members.add(new BoaInt());

		names.put("PEDS", counter++);
		members.add(new BoaInt());

		names.put("PERNOTMVIT", counter++);
		members.add(new BoaInt());

		names.put("PERMVIT", counter++);
		members.add(new BoaInt());

		names.put("person_count", counter++);
		members.add(new BoaInt());

		names.put("COUNTY", counter++);
		members.add(new BoaInt());

		names.put("CITY", counter++);
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

		names.put("NHS", counter++);
		members.add(new BoaInt());

		names.put("RUR_URB", counter++);
		members.add(new BoaInt());

		names.put("FUNC_SYS", counter++);
		members.add(new BoaInt());

		names.put("RD_OWNER", counter++);
		members.add(new BoaInt());

		names.put("ROUTE", counter++);
		members.add(new BoaInt());

		names.put("TWAY_ID", counter++);
		members.add(new BoaString());

		names.put("TWAY_ID2", counter++);
		members.add(new BoaString());

		names.put("MILEPT", counter++);
		members.add(new BoaInt());

		names.put("LATITUDE", counter++);
		members.add(new BoaFloat());

		names.put("LONGITUD", counter++);
		members.add(new BoaFloat());

		names.put("SP_JUR", counter++);
		members.add(new BoaInt());

		names.put("HARM_EV", counter++);
		members.add(new BoaInt());

		names.put("MAN_COLL", counter++);
		members.add(new BoaInt());

		names.put("RELJCT1", counter++);
		members.add(new BoaInt());

		names.put("RELJCT2", counter++);
		members.add(new BoaInt());

		names.put("TYP_INT", counter++);
		members.add(new BoaInt());

		names.put("WRK_ZONE", counter++);
		members.add(new BoaInt());

		names.put("REL_ROAD", counter++);
		members.add(new BoaInt());

		names.put("LGT_COND", counter++);
		members.add(new BoaInt());

		names.put("WEATHER1", counter++);
		members.add(new BoaInt());

		names.put("WEATHER2", counter++);
		members.add(new BoaInt());

		names.put("WEATHER", counter++);
		members.add(new BoaInt());

		names.put("SCH_BUS", counter++);
		members.add(new BoaInt());

		names.put("RAIL", counter++);
		members.add(new BoaString());

		names.put("NOT_HOUR", counter++);
		members.add(new BoaInt());

		names.put("NOT_MIN", counter++);
		members.add(new BoaInt());

		names.put("ARR_HOUR", counter++);
		members.add(new BoaInt());

		names.put("ARR_MIN", counter++);
		members.add(new BoaInt());

		names.put("HOSP_HR", counter++);
		members.add(new BoaInt());

		names.put("HOSP_MN", counter++);
		members.add(new BoaInt());

		names.put("CF1", counter++);
		members.add(new BoaInt());

		names.put("CF2", counter++);
		members.add(new BoaInt());

		names.put("CF3", counter++);
		members.add(new BoaInt());

		names.put("FATALS", counter++);
		members.add(new BoaInt());

		names.put("DRUNK_DR", counter++);
		members.add(new BoaInt());

		names.put("persons", counter++);
		members.add(new PersonProtoTuple());

		names.put("vehicle", counter++);
		members.add(new VehicleProtoTuple());


		}

	/**
	 * Construct a CrashProtoTuple
	 */	

	 public CrashProtoTuple() {
		super(members, names);
	 }

	/** @{inheritDoc} */
	@Override
	public String toJavaType() {
		return "boa.types.Transport.Crash";
	}

 }
 