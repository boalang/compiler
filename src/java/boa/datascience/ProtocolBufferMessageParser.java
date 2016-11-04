package boa.datascience;

import com.google.protobuf.GeneratedMessage;

public interface ProtocolBufferMessageParser<T extends GeneratedMessage> {
 public T parseFrom(byte[] buf, int offset, int len);
}
