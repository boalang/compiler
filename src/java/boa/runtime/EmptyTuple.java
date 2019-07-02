/*
 * Copyright 2019, Robert Dyer, Che Shian Hung
 *                 and Bowling Green State University
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
package boa.runtime;

import boa.output.Output.Value;

/**
 * An empty tuple, containing no values.
 *
 * @author rdyer
 * @author hungc
 */
public class EmptyTuple extends Tuple {
	public EmptyTuple() {}

	public EmptyTuple(final EmptyTuple tmp) {}

	public EmptyTuple clone() {
		return this;
	}

	public Value toValue() {
        return null;
    }
}