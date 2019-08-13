package boa.types.proto.enums;

import com.google.protobuf.ProtocolMessageEnum;

import boa.types.BoaProtoMap;

public class ElementKindProtoMap extends BoaProtoMap {

	/** {@inheritDoc} */
	@Override
	protected Class<? extends ProtocolMessageEnum> getEnumClass() {
		return boa.types.Ast.Element.ElementKind.class;
	}
	
}
