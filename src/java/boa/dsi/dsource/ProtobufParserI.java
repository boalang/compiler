package boa.dsi.dsource;

import java.io.IOException;

import com.google.protobuf.GeneratedMessage;

public interface ProtobufParserI {
	GeneratedMessage parseFrom(com.google.protobuf.CodedInputStream stream) throws IOException;
}
