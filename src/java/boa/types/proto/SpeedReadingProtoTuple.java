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
 public class SpeedReadingProtoTuple extends BoaProtoTuple {
	private final static List < BoaType> members = new ArrayList<BoaType>();
	private final static Map <String, Integer> names = new HashMap<String, Integer>();

	static {
		int counter = 0;

		names.put("code", counter++);
		members.add(new BoaString());

		names.put("type", counter++);
		members.add(new BoaString());

		names.put("speed", counter++);
		members.add(new BoaInt());

		names.put("average", counter++);
		members.add(new BoaInt());

		names.put("reference", counter++);
		members.add(new BoaInt());

		names.put("score", counter++);
		members.add(new BoaInt());

		names.put("cvalue", counter++);
		members.add(new BoaInt());

		names.put("latlon", counter++);
		members.add(new LocationProtoTuple());

		names.put("time", counter++);
		members.add(new BoaInt());

		names.put("roadname", counter++);
		members.add(new BoaString());

		names.put("gid", counter++);
		members.add(new BoaInt());


		}

	/**
	 * Construct a SpeedReadingProtoTuple
	 */	

	 public SpeedReadingProtoTuple() {
		super(members, names);
	 }

	/** @{inheritDoc} */
	@Override
	public String toJavaType() {
		return "boa.types.NewSchema.SpeedReading";
	}

 }
 