package boa.datagen.dependencies;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import boa.datagen.util.FileIO;

public class GradleFile {
	private String content;

	public GradleFile(final String content) {
		this.content = content;
	}

	public Set<String> getDependencies(final String classpathRoot) {
		Set<String> paths = new HashSet<String>();
		Scanner sc = new Scanner(content);
		boolean inDependencies = false;
		HashMap<String, String> variableValue = new HashMap<String, String>();
		while (sc.hasNextLine()) {
			String l = sc.nextLine().trim();
			String line = l;
			if (line.matches("dependencies[\\s]*\\{"))
				inDependencies = true;
			int index = line.indexOf('=');
			if (index > -1) {
				String left = line.substring(0, index).trim();
				String right = line.substring(index + 1).trim();
				if (!left.isEmpty() && !right.isEmpty()) {
					char ch = right.charAt(0);
					index = right.indexOf(ch, 1);
					if (index == right.length() - 1) {
						if (ch == '\'' || ch == '\"')
							right = right.substring(1, right.length()-1);
						if (right.toUpperCase().endsWith("-" + "SNAPSHOT"))
							right = right.substring(0, right.length() - ("-" + "SNAPSHOT").length()) + ".0";
						variableValue.put(left, right);
					}
				}
			} else {
				if (inDependencies) {
					if (line.startsWith("compile ") || line.startsWith("testCompile ") || line.startsWith("classpath ") || line.startsWith("[group")) {
						if (line.startsWith("[group")) {
							index = line.indexOf(']');
							if (index == -1)
								continue;
							line = line.substring(1, index);
						} else {
							index = line.indexOf(' ');
							line = line.substring(index).trim();
						}
						int vi = 0;
						while (true) {
							vi = line.indexOf('+', vi);
							if (vi == -1)
								break;
							line = line.substring(0, vi).trim() + "+" + line.substring(vi+1).trim();
							int j = vi + 1;
							while (j < line.length()) {
								char ch = line.charAt(j);
								if (!Character.isJavaIdentifierPart(ch))
									break;
								j++;
							}
							String var = line.substring(vi+1, j);
							if (variableValue.containsKey(var)) {
								String value = variableValue.get(var);
								char ch = line.charAt(vi-1);
								if (ch == '\'' || ch == '\"')
									line = line.substring(0, vi-1) + value + ch + line.substring(j);
							}
							vi++;
						}
						String[] values = getGradleDependencyInfo(line);
						if (values == null)
							continue;
						for (int i = 0; i < values.length; i++) {
							String v = values[i];
							vi = 0;
							while (true) {
								vi = v.indexOf('$', vi);
								if (vi == -1)
									break;
								int j = vi + 1;
								while (j < v.length()) {
									char ch = v.charAt(j);
									if (ch == '$' || !Character.isJavaIdentifierPart(ch))
										break;
									j++;
								}
								String var = v.substring(vi+1, j);
								if (variableValue.containsKey(var))
									v = v.replace("$" + var, variableValue.get(var));
								vi++;
							}
							values[i] = v;
						}
						if (values.length == 2)
							values = new String[]{values[0], values[1], "null"};
						if (values[0] == null || values[0].contains("$") || values[0].contains("@")
								|| values[1] == null || values[1].contains("$") || values[1].contains("@") 
								|| values[2] == null || values[2].contains("$") || values[2].contains("@")) {
//							System.err.println("Cannot download gradle dependency " + values[0] + ":" + values[1] + ":" + values[2]);
							continue;
						}
						values = DependencyMangementUtil.strip(values);
						for (int i = 0; i < values.length; i++) {
							String v = values[i];
							if (variableValue.containsKey(v))
								values[i] = variableValue.get(v);
						}
						if (values != null && values.length == 3) {
							values[2] = values[2].replace('+', '0');
							String name = values[1] + "-" + values[2] + ".jar";
							String link = "http://central.maven.org/maven2/";
							link += values[0].replace('.', '/');
							link += "/" + values[1];
							link += "/" + values[2];
							link += "/" + name;
							try {
								String p = FileIO.getFile(classpathRoot, name, link);
								if (p != null)
									paths.add(p);
							} catch (IOException ex) {
								String prefix = "http://central.maven.org/maven2/";
								prefix += values[0].replace('.', '/');
								prefix += "/" + values[1] + "/";
								try {
									String p = FileIO.getFile(classpathRoot, prefix, values);
									if (p != null)
										paths.add(p);
								} catch (IOException e1) {
//									System.err.println("Cannot download gradle dependency " + link);
								}
							}
						}
					}
				}
			}
		}
		sc.close();
		return paths;
	}

	private static String[] getGradleDependencyInfo(String line) {
		if (line.isEmpty())
			return null;
		if (line.endsWith("{"))
			return getGradleDependencyInfo(line.substring(0, line.length()-1).trim());
		String[] values = null;
		char ch = line.charAt(0);
		if (ch == '\'' || ch == '\"') {
			line = line.substring(1, line.length() - 1);
			values = line.split(":");
		} else if (line.startsWith("group")) {
			values = line.split(",");
			for (int i = 0; i < values.length; i++) {
				String v = values[i];
				v = v.substring(v.indexOf(':') + 1).trim();
				values[i] = v;
			}
		} else if (line.length() < 2)
			return null;
		else if (!Character.isLetter(ch))
			return getGradleDependencyInfo(line.substring(1, line.length()-1).trim());
		return values;
	}

}
