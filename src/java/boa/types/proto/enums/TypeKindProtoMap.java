/*
 * Copyright 2014, Hridesh Rajan, Robert Dyer,
 *                 and Iowa State University of Science and Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package boa.types.proto.enums;

import com.google.protobuf.ProtocolMessageEnum;

import boa.types.BoaProtoMap;

/**
 * A {@link TypeKindProtoMap}.
 *
 * @author rdyer
 */
public class TypeKindProtoMap extends BoaProtoMap {
	/** {@inheritDoc} */
	@Override
	protected Class<? extends ProtocolMessageEnum> getEnumClass() {
		return boa.types.Ast.TypeKind.class;
	}
}
