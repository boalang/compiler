package boa.datagen.util;

import org.apache.commons.validator.routines.UrlValidator;
import org.apache.log4j.Logger;

import boa.compiler.BoaCompiler;

public class DatagenUtil {
	public static Logger LOG = Logger.getLogger(BoaCompiler.class);

	public static boolean isValidURL(String path) {
		UrlValidator urlValidator = new UrlValidator();
		return path.startsWith("https://github.com/") && urlValidator.isValid(path);
	}
}