package boa.functions;

import boa.functions.FunctionSpec;
import boa.types.BoaLocation.Location;

import com.google.protobuf.InvalidProtocolBufferException;

public class LocationInfo {
	@FunctionSpec(name = "locationinfo", returnType = "Location", formalParameters = { "string" }, typeDependencies = { "boa_location.proto" })
	public static Location locationInfo(final String location) {
		try {
			return Location.parseFrom(location.getBytes());
		} catch (final InvalidProtocolBufferException e) {
			return null;
		}
	}
}
