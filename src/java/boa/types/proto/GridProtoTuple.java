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
 public class GridProtoTuple extends BoaProtoTuple {
	private final static List < BoaType> members = new ArrayList<BoaType>();
	private final static Map <String, Integer> names = new HashMap<String, Integer>();

	static {
		int counter = 0;

		names.put("id", counter++);
		members.add(new BoaInt());

		names.put("gidlocation", counter++);
		members.add(new LocationProtoTuple());

		names.put("weatherRoot", counter++);
		members.add(new WeatherRootProtoTuple());

		names.put("speedRoot", counter++);
		members.add(new SpeedRootProtoTuple());


		}

	/**
	 * Construct a GridProtoTuple
	 */	

	 public GridProtoTuple() {
		super(members, names);
	 }

	/** @{inheritDoc} */
	@Override
	public String toJavaType() {
		return "boa.types.NewSchema.Grid";
	}

 }
 