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

import boa.types.BoaProtoMap;

/**
 * A {@link VisibilityProtoMap}.
 * 
 * @author rdyer
 */
public class VisibilityProtoMap extends BoaProtoMap {
	/**
	 * Construct a {@link VisibilityProtoMap}.
	 */
	public VisibilityProtoMap() {
	}

	/** {@inheritDoc} */
	@Override
	public String toJavaType() {
		return "boa.types.Ast.Modifier.Visibility";
	}

	/** {@inheritDoc} */
	@Override
	public boolean hasAttribute(final String s) {
		try {
			return boa.types.Ast.Modifier.Visibility.valueOf(s) != null;
		} catch (final Exception e) {
			return false;
		}
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "Visibility";
	}
}
