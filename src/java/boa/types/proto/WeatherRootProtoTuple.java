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
 public class WeatherRootProtoTuple extends BoaProtoTuple {
	private final static List < BoaType> members = new ArrayList<BoaType>();
	private final static Map <String, Integer> names = new HashMap<String, Integer>();

	static {
		int counter = 0;

		names.put("time", counter++);
		members.add(new BoaInt());

		names.put("latlon", counter++);
		members.add(new LocationProtoTuple());

		names.put("countyName", counter++);
		members.add(new BoaString());

		names.put("weather", counter++);
		members.add(new BoaProtoList(new WeatherRecordProtoTuple()));


		}

	/**
	 * Construct a WeatherRootProtoTuple
	 */	

	 public WeatherRootProtoTuple() {
		super(members, names);
	 }

	/** @{inheritDoc} */
	@Override
	public String toJavaType() {
		return "boa.types.TransportationSchema.WeatherRoot";
	}

 }
 