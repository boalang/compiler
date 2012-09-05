package sizzle.functions;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Boa domain-specific functions
 * 
 * @author rdyer
 * 
 */
public class BoaIntrinsics {
	private static String[] fixingRegex = {
		"issue(s)?[\\s]+(#)?[0-9]+",
		"issue[\\s]+# [0-9]+",
		"bug[\\s]+(#)?[0-9]+",
		"bug[\\s]+# [0-9]+",
		"fix",
		"bug id=[0-9]+"
	};

	private static List<Pattern> fixingPatterns = new ArrayList<Pattern>();

	static {
		for (String s : BoaIntrinsics.fixingRegex)
			fixingPatterns.add(Pattern.compile(s));
	}

	/**
	 * Is a log message indicating it is a fixing revision?
	 * 
	 * @param log the revision's log message to mine
	 * @return true if the log indicates a fixing revision
	 */
	@FunctionSpec(name = "isfixingrevision", returnType = "bool", formalParameters = { "string" })
	public static boolean isfixingrevision(final String log) {
		for (Pattern p : fixingPatterns)
			if (p.matcher(log).matches())
				return true;

		return false;
	}

	/**
	 * Does a Revision contain a file of the specified type? This only compares based on file extension.
	 * 
	 * @param rev the Revision to examine
	 * @param ext the file extension to look for
	 * @return true if the Revision contains at least 1 file with the specified extension
	 */
	@FunctionSpec(name = "hasfiletype", returnType = "bool", formalParameters = { "Revision", "string" })
	public static boolean hasfile(final sizzle.types.Code.Revision rev, String ext) {
		for (int i = 0; i < rev.getFilesCount(); i++)
			if (rev.getFiles(i).getName().toLowerCase().endsWith("." + ext.toLowerCase()))
				return true;
		return false;
	}
}
