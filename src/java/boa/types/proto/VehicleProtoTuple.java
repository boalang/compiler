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
 public class VehicleProtoTuple extends BoaProtoTuple {
	private final static List < BoaType> members = new ArrayList<BoaType>();
	private final static Map <String, Integer> names = new HashMap<String, Integer>();

	static {
		int counter = 0;

		names.put("state", counter++);
		members.add(new STATEProtoMap());

		names.put("ST_CASE", counter++);
		members.add(new BoaInt());

		names.put("VEH_NO", counter++);
		members.add(new BoaInt());

		names.put("VE_FORMS", counter++);
		members.add(new BoaInt());

		names.put("NUMOCCS", counter++);
		members.add(new BoaInt());

		names.put("DAY", counter++);
		members.add(new BoaInt());

		names.put("MONTH", counter++);
		members.add(new BoaInt());

		names.put("HOUR", counter++);
		members.add(new BoaInt());

		names.put("MINUTE", counter++);
		members.add(new BoaInt());

		names.put("HARM_EV", counter++);
		members.add(new BoaInt());

		names.put("MAN_COLL", counter++);
		members.add(new BoaInt());

		names.put("UNITTYPE", counter++);
		members.add(new BoaInt());

		names.put("HIT_RUN", counter++);
		members.add(new BoaInt());

		names.put("REG_STAT", counter++);
		members.add(new BoaInt());

		names.put("OWNER", counter++);
		members.add(new BoaInt());

		names.put("MAKE", counter++);
		members.add(new BoaInt());

		names.put("MODEL", counter++);
		members.add(new BoaInt());

		names.put("MAK_MOD", counter++);
		members.add(new BoaInt());

		names.put("BODY_TYP", counter++);
		members.add(new BoaInt());

		names.put("MOD_YEAR", counter++);
		members.add(new BoaInt());

		names.put("VIN", counter++);
		members.add(new BoaString());

		names.put("VIN_1", counter++);
		members.add(new BoaString());

		names.put("VIN_2", counter++);
		members.add(new BoaString());

		names.put("VIN_3", counter++);
		members.add(new BoaString());

		names.put("VIN_4", counter++);
		members.add(new BoaString());

		names.put("VIN_5", counter++);
		members.add(new BoaString());

		names.put("VIN_6", counter++);
		members.add(new BoaString());

		names.put("VIN_7", counter++);
		members.add(new BoaString());

		names.put("VIN_8", counter++);
		members.add(new BoaString());

		names.put("VIN_9", counter++);
		members.add(new BoaString());

		names.put("VIN_10", counter++);
		members.add(new BoaString());

		names.put("VIN_11", counter++);
		members.add(new BoaString());

		names.put("VIN_12", counter++);
		members.add(new BoaString());

		names.put("TOW_VEH", counter++);
		members.add(new BoaInt());

		names.put("J_KNIFE", counter++);
		members.add(new BoaInt());

		names.put("MCARR_I1", counter++);
		members.add(new BoaString());

		names.put("MCARR_I2", counter++);
		members.add(new BoaString());

		names.put("MCARR_ID", counter++);
		members.add(new BoaString());

		names.put("GVWR", counter++);
		members.add(new BoaInt());

		names.put("V_CONFIG", counter++);
		members.add(new BoaInt());

		names.put("CARGO_BT", counter++);
		members.add(new BoaInt());

		names.put("HAZ_INV", counter++);
		members.add(new BoaInt());

		names.put("HAZ_PLAC", counter++);
		members.add(new BoaInt());

		names.put("HAZ_ID", counter++);
		members.add(new BoaInt());

		names.put("HAZ_CNO", counter++);
		members.add(new BoaInt());

		names.put("HAZ_REL", counter++);
		members.add(new BoaInt());

		names.put("BUS_USE", counter++);
		members.add(new BoaInt());

		names.put("SPEC_USE", counter++);
		members.add(new BoaInt());

		names.put("EMER_USE", counter++);
		members.add(new BoaInt());

		names.put("TRAV_SP", counter++);
		members.add(new BoaInt());

		names.put("UNDERIDE", counter++);
		members.add(new BoaInt());

		names.put("ROLLOVER", counter++);
		members.add(new BoaInt());

		names.put("ROLINLOC", counter++);
		members.add(new BoaInt());

		names.put("IMPACT1", counter++);
		members.add(new BoaInt());

		names.put("DEFORMED", counter++);
		members.add(new BoaInt());

		names.put("TOWED", counter++);
		members.add(new BoaInt());

		names.put("M_HARM", counter++);
		members.add(new BoaInt());

		names.put("VEH_SC1", counter++);
		members.add(new BoaInt());

		names.put("VEH_SC2", counter++);
		members.add(new BoaInt());

		names.put("FIRE_EXP", counter++);
		members.add(new BoaInt());

		names.put("DR_PRES", counter++);
		members.add(new BoaInt());

		names.put("L_STATE", counter++);
		members.add(new BoaInt());

		names.put("DR_ZIP", counter++);
		members.add(new BoaInt());

		names.put("L_STATUS", counter++);
		members.add(new BoaInt());

		names.put("L_TYPE", counter++);
		members.add(new BoaInt());

		names.put("CDL_STAT", counter++);
		members.add(new BoaInt());

		names.put("L_ENDORS", counter++);
		members.add(new BoaInt());

		names.put("L_COMPL", counter++);
		members.add(new BoaInt());

		names.put("L_RESTRI", counter++);
		members.add(new BoaInt());

		names.put("DR_HGT", counter++);
		members.add(new BoaInt());

		names.put("DR_WGT", counter++);
		members.add(new BoaInt());

		names.put("PREV_ACC", counter++);
		members.add(new BoaInt());

		names.put("PREV_SUS", counter++);
		members.add(new BoaInt());

		names.put("PREV_DWI", counter++);
		members.add(new BoaInt());

		names.put("PREV_SPD", counter++);
		members.add(new BoaInt());

		names.put("PREV_OTH", counter++);
		members.add(new BoaInt());

		names.put("FIRST_MO", counter++);
		members.add(new BoaInt());

		names.put("FIRST_YR", counter++);
		members.add(new BoaInt());

		names.put("LAST_MO", counter++);
		members.add(new BoaInt());

		names.put("LAST_YR", counter++);
		members.add(new BoaInt());

		names.put("SPEEDREL", counter++);
		members.add(new BoaInt());

		names.put("DR_SF1", counter++);
		members.add(new BoaInt());

		names.put("DR_SF2", counter++);
		members.add(new BoaInt());

		names.put("DR_SF3", counter++);
		members.add(new BoaInt());

		names.put("DR_SF4", counter++);
		members.add(new BoaInt());

		names.put("VTRAFWAY", counter++);
		members.add(new BoaInt());

		names.put("VNUM_LAN", counter++);
		members.add(new BoaInt());

		names.put("VSPD_LIM", counter++);
		members.add(new BoaInt());

		names.put("VALIGN", counter++);
		members.add(new BoaInt());

		names.put("VPROFILE", counter++);
		members.add(new BoaInt());

		names.put("VPAVETYP", counter++);
		members.add(new BoaInt());

		names.put("VSURCOND", counter++);
		members.add(new BoaInt());

		names.put("VTRAFCON", counter++);
		members.add(new BoaInt());

		names.put("VTCONT_F", counter++);
		members.add(new BoaInt());

		names.put("P_CRASH1", counter++);
		members.add(new BoaInt());

		names.put("P_CRASH2", counter++);
		members.add(new BoaInt());

		names.put("P_CRASH3", counter++);
		members.add(new BoaInt());

		names.put("PCRASH4", counter++);
		members.add(new BoaInt());

		names.put("PCRASH5", counter++);
		members.add(new BoaInt());

		names.put("ACC_TYPE", counter++);
		members.add(new BoaInt());

		names.put("DEATHS", counter++);
		members.add(new BoaInt());

		names.put("DR_DRINK", counter++);
		members.add(new BoaInt());


		}

	/**
	 * Construct a VehicleProtoTuple
	 */	

	 public VehicleProtoTuple() {
		super(members, names);
	 }

	/** @{inheritDoc} */
	@Override
	public String toJavaType() {
		return "boa.types.Transport.Vehicle";
	}

 }
 