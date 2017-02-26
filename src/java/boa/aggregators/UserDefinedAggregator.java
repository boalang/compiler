package boa.aggregators;


import boa.compiler.UserDefinedAggregators;
import boa.datagen.DefaultProperties;
import org.apache.commons.io.FileUtils;
import org.eclipse.core.internal.utils.FileUtil;

import java.io.*;
import java.nio.file.Files;

@AggregatorSpec(name = "UserDefinedAgg", type = "UserDefined", canCombine = false)
public abstract class UserDefinedAggregator extends Aggregator {

	public void store(Object object, String className) {
		File output = new File(UserDefinedAggregators.getFileName());
		final String dest= output.getAbsolutePath() + "/";
		output.mkdir();
		try {
			FileOutputStream fos = new FileOutputStream(dest + UserDefinedAggregators.getFileName() + ".model");
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(object);
			copyClassFile(className, dest);
			oos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void copyClassFile(String className, String dest) {
		className =  className.substring(4).replace(".", "$") + ".class"; //FIXME: given index 4 is an hack to avoid the "boa." from class name
		File source = new File("./compile/boa/" + className);
		File destination = new File(dest + "/" + className);
		try{
			FileUtils.copyFile(source, destination);
		}catch(IOException e) {
			e.printStackTrace();
		}
	}
}