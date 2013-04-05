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
		String str = key.toString();
		int pos = str.indexOf("::");
		if (pos == -1)
			return key.hashCode() % num;
		return Integer.parseInt(str.substring(0, pos)) % num;
	}
}
