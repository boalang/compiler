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
 public class WeatherRecordProtoTuple extends BoaProtoTuple {
	private final static List < BoaType> members = new ArrayList<BoaType>();
	private final static Map <String, Integer> names = new HashMap<String, Integer>();

	static {
		int counter = 0;

		names.put("gid", counter++);
		members.add(new BoaInt());

		names.put("tmpc", counter++);
		members.add(new BoaFloat());

		names.put("wawa", counter++);
		members.add(new BoaFloat());

		names.put("ptype", counter++);
		members.add(new BoaFloat());

		names.put("dwpc", counter++);
		members.add(new BoaFloat());

		names.put("smps", counter++);
		members.add(new BoaFloat());

		names.put("drct", counter++);
		members.add(new BoaFloat());

		names.put("vsby", counter++);
		members.add(new BoaFloat());

		names.put("roadtmpc", counter++);
		members.add(new BoaFloat());

		names.put("srad", counter++);
		members.add(new BoaFloat());

		names.put("snwd", counter++);
		members.add(new BoaFloat());

		names.put("pcpn", counter++);
		members.add(new BoaFloat());

		names.put("time", counter++);
		members.add(new BoaInt());

		names.put("latlon", counter++);
		members.add(new LocationProtoTuple());


		}

	/**
	 * Construct a WeatherRecordProtoTuple
	 */	

	 public WeatherRecordProtoTuple() {
		super(members, names);
	 }

	/** @{inheritDoc} */
	@Override
	public String toJavaType() {
		return "boa.types.NewSchema.WeatherRecord";
	}

 }
 