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

import org.apache.hadoop.mapreduce.Partitioner;

import boa.io.EmitKey;
import boa.io.EmitValue;

/**
 * A {@link Partitioner} that assigns each
 * output variable to its own reducer.
 *
 * @author rdyer
 * @author hungc
 */
public class BoaPartitioner extends Partitioner<EmitKey, EmitValue> {
	private static String[] outputVariableNames = new String[0];

	public int getPartition(final EmitKey key, final EmitValue value, final int num) {
		return getPartitionForVariable(key.getName()) % num;
	}

	public static void setVariableNames(final String[] names) {
		outputVariableNames = names;
	}

	public static String getVariableFromPartition(final int pIndex) {
		if (pIndex >= outputVariableNames.length)
			return "part-r-" + String.format("%05d", pIndex);
		return outputVariableNames[pIndex];
	}

	public static int getPartitionForVariable(final String s) {
		for (int i = 0; i < outputVariableNames.length; i++) {
			if (outputVariableNames[i].equals(s)) {
				return i;
			}
		}
		return 0;
	}
}
