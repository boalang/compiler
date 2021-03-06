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
package boa.functions;

import java.util.HashMap;

import boa.runtime.BoaAbstractVisitor;

/**
 * Boa AST visitor that aggregates using a map.
 * 
 * @author rdyer
 */
public class BoaCollectingVisitor<K, V> extends BoaAbstractVisitor {
	public HashMap<K, V> map;

	public BoaCollectingVisitor<K, V> initialize(final HashMap<K, V> map) {
		initialize();
		this.map = map;
		return this;
	}
}
