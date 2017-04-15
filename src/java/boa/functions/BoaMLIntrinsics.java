package boa.functions;

import boa.BoaTup;
import boa.datagen.util.FileIO;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.lang.model.element.Modifier;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;

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
	public static<T> T load(String path, T type) {
		Gson obj = new GsonBuilder().excludeFieldsWithModifiers(org.eclipse.jdt.core.dom.Modifier.TRANSIENT).serializeNulls().create();
		String json = FileIO.readFileContents(new File(path));
		T model = (T)obj.fromJson(json,type.getClass());
		return model;
	}
}
