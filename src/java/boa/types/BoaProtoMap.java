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
package boa.types;

/**
 * A {@link BoaMap}, which is actually a Protocol Buffer enum.
 * 
 * @author rdyer
 */
public class BoaProtoMap extends BoaMap {
	/**
	 * Construct a {@link BoaProtoMap}.
	 */
	public BoaProtoMap() {
		super(new BoaInt(), new BoaString());
	}

	/**
	 * Returns if this protobuf enum has the specified attribute.
	 * 
	 * @param s the attribute to check for
	 * @return true if it has the attribute s
	 */
	public boolean hasAttribute(final String s) {
		throw new RuntimeException("proto map must override hasAttribute()");
	}

	/** {@inheritDoc} */
	@Override
	public boolean assigns(BoaType obj) {
		return this.getClass() == obj.getClass();
	}

	/** {@inheritDoc} */
	@Override
	public boolean compares(BoaType obj) {
		return this.getClass() == obj.getClass();
	}
}
