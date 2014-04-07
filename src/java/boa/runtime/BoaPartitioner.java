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
package boa.runtime;

import org.apache.hadoop.mapreduce.Partitioner;

import boa.io.EmitKey;
import boa.io.EmitValue;

/**
 * A {@link Partitioner} that takes several merged jobs and partitions
 * keys from each job to its own reducer.
 * 
 * @author rdyer
 */
public class BoaPartitioner extends Partitioner<EmitKey, EmitValue> {
	public int getPartition(final EmitKey key, final EmitValue value, final int num) {
		return key.getId() % num;
	}
}
