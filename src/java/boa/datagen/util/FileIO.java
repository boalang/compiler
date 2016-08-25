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
 * @author hoan
 */
public class FileIO {
	public static void writeObjectToFile(Object object, String objectFile, boolean append) {
		try {
			ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(objectFile, append)));
			out.writeObject(object);
			out.flush();
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static Object readObjectFromFile(String objectFile) {
		try {
			ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(objectFile)));
			Object object = in.readObject();
			in.close();
			return object;
		} catch (Exception e) {
			return null;
		}
	}

	public static String readFileContents(File file) {
		try {
			BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
			byte[] bytes = new byte[(int) file.length()];
			in.read(bytes);
			in.close();
			return new String(bytes);
		} catch (Exception e) {
			return "";
		}
	}

	public static void writeFileContents(File file, String s) {
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(file));
			out.write(s);
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static final void delete(final File f) throws IOException {
		if (f.isDirectory())
			for (final File g : f.listFiles())
				delete(g);

		if (!f.delete())
			throw new IOException("unable to delete file " + f);
	}
}
