/*
 * Copyright 2015, Hridesh Rajan, Robert Dyer, Hoan Nguyen
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

package boa.datagen.util;

import java.io.*;

/**
 * Loads properties and provides helper methods for using those properties.
 *
 * @author rdyer
 */
public class Properties {
	private static final java.util.Properties properties = new java.util.Properties();

	static {
		loadProperties(System.getProperty("cacher.property.file", "/cacher.properties"));
	}

	private static void loadProperties(final String path) {
		try {
			final InputStream stream = Properties.class.getResourceAsStream(path);
			if (stream == null)
				return;

			properties.load(stream);
			stream.close();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Gets a property.
	 *
	 * If the property was set by the system, get it there. Otherwise attempt
	 * to get from the properties files and fall back to the defaultVal given.
	 *
	 * @param key the property to retrieve
	 * @param defaultVal the default value if the property is not set
	 * @return the property's value
	 */
	public static String getProperty(final String key, final String defaultVal) {
		final String val = System.getProperty(key);
		if (val != null)
			return val;
		return properties.getProperty(key, defaultVal);
	}

	/**
	 * Gets a boolean property.
	 *
	 * If the property was set by the system, get it there. Otherwise attempt
	 * to get from the properties files and fall back to the defaultVal given.
	 *
	 * @param key the property to retrieve
	 * @param defaultVal the default value if the property is not set
	 * @return the property's value
	 */
	public static boolean getBoolean(final String key, final boolean defaultVal) {
		final String val = getProperty(key, "");
		if (val.equals(""))
			return defaultVal;
		return val.equalsIgnoreCase("true");
	}

	/**
	 * Helper method to create instances of objects from their string names.
	 *
	 * On an error, it stops the program.
	 *
	 * @param name the name of the class to instantiate
	 * @return the newly created instance
	 */
	public static <T> T newInstance(final String name) {
		try {
			return (T)(Class.forName(name).newInstance());
		} catch (final Exception e) {
			e.printStackTrace();
			System.exit(-1);
			return null;
		}
	}
}
