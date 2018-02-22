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
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Scanner;

/**
 * @author hoan
 */
public class FileIO {
	public static void writeObjectToFile(Object object, String objectFile, boolean append) {
		ObjectOutputStream out = null;
		try {
			out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(objectFile, append)));
			out.writeObject(object);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (out != null) {
				try {
					out.flush();
					out.close();
				} catch (Exception e) {}
			}
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
	
	public static void writeFileContents(File file, String s, boolean append) {
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(file, append));
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

	public static String getFileName(String path) {
		int index = path.lastIndexOf('/');
		if (index == -1)
			index = path.lastIndexOf('\\');
		return path.substring(index + 1);
	}

	public static String getFile(String outPath, String prefix, String[] values) throws IOException {
		String lib = values[1];
		File dir = new File(outPath);
		if (dir.exists()) {
			for (File file : dir.listFiles()) {
				String name = file.getName();
				if (name.startsWith(lib) && name.endsWith(".jar"))
					return new File(dir, name).getAbsolutePath();
			}
		}
		
		InputStream is = null;
	    try {
	    	URL url = new URL(prefix);
	        is = url.openStream();
	        BufferedReader br = new BufferedReader(new InputStreamReader(is));
		    String line, last = null;
	        while ((line = br.readLine()) != null) {
	        	line = line.trim();
	        	if (line.startsWith("<a ")) {
	        		int index = "<a href=".length();
	        		char ch = line.charAt(index);
	        		if (ch == '\'' || ch == '\"')
	        			index++;
	        		String v = line;
        			if (ch == '\'' || ch == '\"')
        				v = line.substring(index, line.indexOf(ch, index));
        			else {
        				int e = line.indexOf(' ', index);
        				if (e == -1)
        					e = line.indexOf('>', index);
        				v = line.substring(index, e);
        			}
        			String[] parts = v.split("\\.");
	        		try {
	        			Integer.parseInt(parts[0]);
	        			last = v;
	        		} catch (Exception e) {
	        			if (last != null)
	        				break;
	        		}
	        	}
	        }
	        if (last != null) {
	        	if (last.endsWith("/"))
	        		last = last.substring(0, last.length() - 1);
				String name = values[1] + "-" + last + ".jar";
	        	String link = prefix + last;
				link += "/" + name;
				return getFile(outPath, name, link);
	        }
	    } finally {
	        try {
	            if (is != null) is.close();
	        } catch (IOException ioe) {}
	    }
	    return null;
	}

	public static String getFile(String outPath, String name, String link) throws IOException {
		File dir = new File(outPath), file = new File(dir, name);
		if (!file.exists()) {
			URL url = new URL(link);
			ReadableByteChannel rbc = Channels.newChannel(url.openStream());
			if (!dir.exists())
				dir.mkdirs();
			FileOutputStream fos = null;
			try {
				fos = new FileOutputStream(dir.getAbsolutePath() + "/" + name);
				fos.getChannel().transferFrom(rbc, 0, Integer.MAX_VALUE);
			} catch (Exception e) {
				return null;
			} finally {
				if (fos != null) {
					try {
						fos.flush();
						fos.close();
					} catch (Exception e) {}
				}
			}
		}
		return file.getAbsolutePath();
	}
	
	public static String normalizeEOL(String source) {
		StringBuilder sb = new StringBuilder();
		Scanner sc = new Scanner(source);
		while (sc.hasNextLine())
			sb.append(sc.nextLine() + "\n");
		sc.close();
		return sb.toString();
	}
}
