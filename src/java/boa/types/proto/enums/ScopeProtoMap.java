package boa.types.proto.enums;

import com.google.protobuf.ProtocolMessageEnum;

import boa.types.BoaProtoMap;

public class ScopeProtoMap extends BoaProtoMap {

	@Override
	protected Class<? extends ProtocolMessageEnum> getEnumClass() {
		return boa.types.Ast.Modifier.Scope.class;
	}
	
}
