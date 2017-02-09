package boa.types.proto;

import com.google.protobuf.ProtocolMessageEnum;

import boa.types.BoaProtoMap;

import boa.types.*;

/**
 * A {@link ForgeKindProtoMap}.
 * 
 * @author nmtiwari
 */
public class STATEProtoMap extends BoaProtoMap {
	/** {@inheritDoc} */
	@Override
	protected Class<? extends ProtocolMessageEnum> getEnumClass() {
		return boa.types.Transport.STATE.class;
	}
}

 