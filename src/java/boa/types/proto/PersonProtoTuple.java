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
 public class PersonProtoTuple extends BoaProtoTuple {
	private final static List < BoaType> members = new ArrayList<BoaType>();
	private final static Map <String, Integer> names = new HashMap<String, Integer>();

	static {
		int counter = 0;

		names.put("state", counter++);
		members.add(new STATEProtoMap());

		names.put("ST_CASE", counter++);
		members.add(new BoaFloat());

		names.put("VE_FORMS", counter++);
		members.add(new BoaFloat());

		names.put("VEH_NO", counter++);
		members.add(new BoaFloat());

		names.put("PER_NO", counter++);
		members.add(new BoaFloat());

		names.put("STR_VEH", counter++);
		members.add(new BoaFloat());

		names.put("COUNTY", counter++);
		members.add(new BoaFloat());

		names.put("DAY", counter++);
		members.add(new BoaFloat());

		names.put("MONTH", counter++);
		members.add(new BoaFloat());

		names.put("HOUR", counter++);
		members.add(new BoaFloat());

		names.put("MINUTE", counter++);
		members.add(new BoaFloat());

		names.put("RUR_URB", counter++);
		members.add(new BoaFloat());

		names.put("FUNC_SYS", counter++);
		members.add(new BoaFloat());

		names.put("HARM_EV", counter++);
		members.add(new BoaFloat());

		names.put("MAN_COLL", counter++);
		members.add(new BoaFloat());

		names.put("SCH_BUS", counter++);
		members.add(new BoaFloat());

		names.put("MAKE", counter++);
		members.add(new BoaFloat());

		names.put("MAK_MOD", counter++);
		members.add(new BoaFloat());

		names.put("BODY_TYP", counter++);
		members.add(new BoaFloat());

		names.put("MOD_YEAR", counter++);
		members.add(new BoaFloat());

		names.put("TOW_VEH", counter++);
		members.add(new BoaFloat());

		names.put("SPEC_USE", counter++);
		members.add(new BoaFloat());

		names.put("EMER_USE", counter++);
		members.add(new BoaFloat());

		names.put("ROLLOVER", counter++);
		members.add(new BoaFloat());

		names.put("IMPACT1", counter++);
		members.add(new BoaFloat());

		names.put("FIRE_EXP", counter++);
		members.add(new BoaFloat());

		names.put("AGE", counter++);
		members.add(new BoaFloat());

		names.put("SEX", counter++);
		members.add(new BoaFloat());

		names.put("PER_TYP", counter++);
		members.add(new BoaFloat());

		names.put("INJ_SEV", counter++);
		members.add(new BoaFloat());

		names.put("SEAT_POS", counter++);
		members.add(new BoaFloat());

		names.put("REST_USE", counter++);
		members.add(new BoaFloat());

		names.put("REST_MIS", counter++);
		members.add(new BoaFloat());

		names.put("AIR_BAG", counter++);
		members.add(new BoaFloat());

		names.put("EJECTION", counter++);
		members.add(new BoaFloat());

		names.put("EJ_PATH", counter++);
		members.add(new BoaFloat());

		names.put("EXTRICAT", counter++);
		members.add(new BoaFloat());

		names.put("DRINKING", counter++);
		members.add(new BoaFloat());

		names.put("ALC_DET", counter++);
		members.add(new BoaFloat());

		names.put("ALC_STATUS", counter++);
		members.add(new BoaFloat());

		names.put("ATST_TYP", counter++);
		members.add(new BoaFloat());

		names.put("ALC_RES", counter++);
		members.add(new BoaFloat());

		names.put("DRUGS", counter++);
		members.add(new BoaFloat());

		names.put("DRUG_DET", counter++);
		members.add(new BoaFloat());

		names.put("DSTATUS", counter++);
		members.add(new BoaFloat());

		names.put("DRUGTST1", counter++);
		members.add(new BoaFloat());

		names.put("DRUGTST2", counter++);
		members.add(new BoaFloat());

		names.put("DRUGTST3", counter++);
		members.add(new BoaFloat());

		names.put("DRUGRES1", counter++);
		members.add(new BoaFloat());

		names.put("DRUGRES2", counter++);
		members.add(new BoaFloat());

		names.put("DRUGRES3", counter++);
		members.add(new BoaFloat());

		names.put("HOSPITAL", counter++);
		members.add(new BoaFloat());

		names.put("DOA", counter++);
		members.add(new BoaFloat());

		names.put("DEATH_DA", counter++);
		members.add(new BoaFloat());

		names.put("DEATH_MO", counter++);
		members.add(new BoaFloat());

		names.put("DEATH_YR", counter++);
		members.add(new BoaFloat());

		names.put("DEATH_HR", counter++);
		members.add(new BoaFloat());

		names.put("DEATH_MN", counter++);
		members.add(new BoaFloat());

		names.put("DEATH_TM", counter++);
		members.add(new BoaFloat());

		names.put("LAG_HRS", counter++);
		members.add(new BoaFloat());

		names.put("LAG_MINS", counter++);
		members.add(new BoaFloat());

		names.put("P_SF1", counter++);
		members.add(new BoaFloat());

		names.put("P_SF2", counter++);
		members.add(new BoaFloat());

		names.put("P_SF3", counter++);
		members.add(new BoaFloat());

		names.put("WORK_INJ", counter++);
		members.add(new BoaFloat());

		names.put("HISPANIC", counter++);
		members.add(new BoaFloat());

		names.put("RACE", counter++);
		members.add(new BoaFloat());

		names.put("LOCATION", counter++);
		members.add(new BoaFloat());


		}

	/**
	 * Construct a PersonProtoTuple
	 */	

	 public PersonProtoTuple() {
		super(members, names);
	 }

	/** @{inheritDoc} */
	@Override
	public String toJavaType() {
		return "boa.types.Transport.Person";
	}

 }
 