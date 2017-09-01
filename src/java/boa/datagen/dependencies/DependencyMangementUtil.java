package boa.datagen.dependencies;

public class DependencyMangementUtil {

	public static String[] strip(String[] values) {
		String[] vs = new String[values.length];
		for (int i = 0; i < vs.length; i++) {
			vs[i] = strip(values[i]);
		}
		return vs;
	}

	private static String strip(String s) {
		if (s == null)
			return "null";
		char ch = s.charAt(0);
		if (Character.isLetter(ch) || Character.isDigit(ch)) {
			if (s.toUpperCase().endsWith("-" + "SNAPSHOT"))
				s = s.substring(0, s.length() - ("-" + "SNAPSHOT").length()) + ".0";
			return s;
		}
		if (ch == '(' || ch == '\'' || ch =='\"')
			return strip(s.substring(1, s.length() - 1));
		return s;
	}

}
