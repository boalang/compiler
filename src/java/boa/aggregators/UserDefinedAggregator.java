package boa.aggregators;


import boa.compiler.UserDefinedAggregators;
import boa.datagen.util.FileIO;
import com.google.gson.Gson;

import java.io.*;

@AggregatorSpec(name = "UserDefinedAgg", formalParameters = { "any", "any" }, type = "UserDefined", canCombine = false)
public abstract class UserDefinedAggregator extends Aggregator {

	public void store(Object object) {
		Gson json = new Gson();
		File output = new File(UserDefinedAggregators.getFileName());
		final String dest= output.getAbsolutePath() + "/";
		output.mkdir();
		writeAsJSON(object, dest + UserDefinedAggregators.getFileName() + ".model");
	}

	private void writeAsJSON(Object object, String path) {
		Gson writer = new Gson();
		FileIO.writeFileContents(new File(path), writer.toJson(object));
	}
}