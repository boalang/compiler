package boa.functions;

import boa.BoaTup;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * Machine learning utility function
 * @author: nmtiwari
 */
public class BoaMLIntrinsics {
	/**
	 * Return a random floating point number x in the range 0.0 < x < 1.0.
	 *
	 * @return A random floating point number x in the range 0.0 < x < 1.0
	 */
	@FunctionSpec(name = "load", returnType = "tuple", formalParameters = { "string", "any"})
	public static<T> T load(String path, T dummyModel) {
		T loadedModel = null;

		try {
			FileInputStream fis = new FileInputStream(path);
			ObjectInputStream ois = new ObjectInputStream(fis);
			String obj = (String) ois.readObject();
			System.out.println(obj);
			loadedModel = (T) ois.readObject();
			ois.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return loadedModel;
	}
}
