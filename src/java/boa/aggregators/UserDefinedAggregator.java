package boa.aggregators;


import boa.compiler.UserDefinedAggregators;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

@AggregatorSpec(name = "UserDefinedAgg", type = "UserDefined", canCombine = false)
public abstract class UserDefinedAggregator extends Aggregator {

	public void store(Object object) {
		try {
			FileOutputStream fos = new FileOutputStream(UserDefinedAggregators.getFileName() + ".model");
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(object);
			oos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}