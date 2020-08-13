package boa.functions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;
import org.nd4j.shade.guava.collect.Sets;

import boa.runtime.BoaAbstractVisitor;
import boa.types.Ast.*;
import boa.types.Code.CodeRepository;
import boa.types.Code.Revision;
import boa.types.Diff.ChangedFile;
import boa.types.Shared.ChangeKind;

import static boa.functions.BoaIntrinsics.*;
import static boa.functions.BoaAstIntrinsics.*;

public class BoaUtilIntrinsics {

	@FunctionSpec(name = "stdout", returnType = "string", formalParameters = { "string" })
	public static String stdOut(final String s) {
		System.out.println(s);
		return s;
	}

	@FunctionSpec(name = "isjava", returnType = "bool", formalParameters = { "ChangedFile" })
	public static boolean isJavaFile(final ChangedFile cf) {
		return cf.getName().endsWith(".java");
	}

	@FunctionSpec(name = "freemem", returnType = "int")
	public static long freemem() {
		int mb = 1024 * 1024;
		long freeMem = Runtime.getRuntime().freeMemory() / mb;
		return freeMem;
	}

	@FunctionSpec(name = "split", returnType = "array of string", formalParameters = { "string" })
	public static String[] split(String s) {
		List<String> splitted = new ArrayList<>();
		for (String s1 : s.split("\\_+|\\.+|\\,+|\\[+|\\]+|\\<+|\\>+|\\s+"))
			for (String s2 : StringUtils.splitByCharacterTypeCamelCase(s1))
				splitted.add(s2);
		return splitted.stream().map(String::toLowerCase).toArray(String[]::new);
	}

	@FunctionSpec(name = "alphabet_filter", returnType = "bool", formalParameters = { "string", "string" })
	public static boolean alphabetFilter(String name, String s) {
		HashSet<Integer> exceptions = new HashSet<Integer>();
		for (char c : s.toCharArray())
			exceptions.add((int) c);
		for (char c : name.toCharArray())
			if ((c < 'A' || (c > 'Z' && c < 'a') || c > 'z') && !exceptions.contains((int) c))
				return true;
		return false;
	}

	@FunctionSpec(name = "signature", returnType = "string", formalParameters = { "Method" })
	public static String signature(Method m) {
		StringBuilder sb = new StringBuilder();

		// add modifier
//		sb.append(getModifierAsString(m.getModifiersList()));

		// add method name
		sb.append(m.getName() + "(");

		// add method arguments
		for (int i = 0; i < m.getArgumentsCount(); i++) {
			if (i > 0)
				sb.append(", ");
			Variable v = m.getArguments(i);
			sb.append(v.getName() + ": ");
			if (v.hasVariableType())
				sb.append(v.getVariableType().getName());
		}
		sb.append(")");

		// add return type
		if (m.hasReturnType()) {
			sb.append(" : " + m.getReturnType().getName());
		}

		return sb.toString();
	}

//	private static String getModifierAsString(List<Modifier> list) {
//		for (Modifier modifier : list)
//			if (modifier.hasVisibility())
//				return modifier.getVisibility().toString().toLowerCase() + " ";
//		return "";
//	}

	@FunctionSpec(name = "get_previous_file", returnType = "ChangedFile", formalParameters = { "CodeRepository",
			"Revision", "ChangedFile" })
	public static ChangedFile getPreviousFile(CodeRepository cr, Revision rev, ChangedFile cf) {
		String prevName = cf.getChange() == ChangeKind.RENAMED ? cf.getPreviousNames(0) : cf.getName();
		rev = rev.getParentsCount() == 0 ? null : getRevision(cr, rev.getParents(0));
		while (rev != null) {
			int l = 0, r = rev.getFilesCount() - 1;
			while (l <= r) {
				int mid = (l + r) / 2;
				String fileName = rev.getFiles(mid).getName();
				if (fileName.equals(prevName))
					return rev.getFiles(mid);
				else if (prevName.compareTo(fileName) > 0)
					l = mid + 1;
				else
					r = mid - 1;
			}
			// look for first-parent branch
			rev = rev.getParentsCount() == 0 ? null : getRevision(cr, rev.getParents(0));
		}
		return null;
	}

	@FunctionSpec(name = "find_inconsistent_method", returnType = "array of array of string", formalParameters = {
			"map[string] of queue of Method", "map[string] of queue of Method" })
	public static String[][] findInconsistentMethod(HashMap<String, LinkedList<Method>> prev,
			HashMap<String, LinkedList<Method>> cur) {
		List<String[]> results = new ArrayList<>();
		for (Entry<String, LinkedList<Method>> e1 : prev.entrySet())
			for (Entry<String, LinkedList<Method>> e2 : cur.entrySet())
				if (e1.getKey().equals(e2.getKey())) {
					update(e1, e2, results);
					break;
				}
		String[][] res = new String[results.size()][2];
		for (int i = 0; i < res.length; i++)
			res[i] = results.get(i);
		return res;
	}

	private static void update(Entry<String, LinkedList<Method>> e1, Entry<String, LinkedList<Method>> e2,
			List<String[]> results) {

		HashMap<String, Method> deleted = new HashMap<String, Method>();
		for (Method m : e1.getValue())
			deleted.put(signature(m), m);

		HashMap<String, Method> added = new HashMap<String, Method>();
		for (Method m : e2.getValue())
			added.put(signature(m), m);

		for (Iterator<Entry<String, Method>> itr = deleted.entrySet().iterator(); itr.hasNext();) {
			String sig = itr.next().getKey();
			if (added.containsKey(sig)) {
				itr.remove();
				added.remove(sig);
			}
		}

		if (deleted.size() == 0 || added.size() == 0)
			return;

		String fqn = e1.getKey();

		for (Iterator<Entry<String, Method>> itr1 = deleted.entrySet().iterator(); itr1.hasNext();) {
			Entry<String, Method> entry1 = itr1.next();
			for (Iterator<Entry<String, Method>> itr2 = added.entrySet().iterator(); itr2.hasNext();) {
				Entry<String, Method> entry2 = itr2.next();
				Method m1 = entry1.getValue();
				Method m2 = entry2.getValue();
				if (matchMethodBody(m1, m2)) {
					String[] tokens1 = split(m1.getName());
					String[] tokens2 = split(m2.getName());
					if (tokens1.length > 0 && tokens2.length > 0 && !tokens1[0].equals(tokens2[0])) {
						results.add(new String[] { fqn + " " + entry1.getKey(), fqn + " " + entry2.getKey() });
						itr1.remove();
						itr2.remove();
						break;
					}
				}

			}
		}
	}

	private static boolean matchMethodBody(Method m1, Method m2) {
		if (m1.getName().equals(m2.getName()))
			return false;
		if (m1.getStatementsCount() == 0 || m2.getStatementsCount() == 0)
			return false;
		List<Statement> s1 = m1.getStatements(0).getStatementsList();
		List<Statement> s2 = m2.getStatements(0).getStatementsList();
		if (s1.size() != s2.size() || s1.size() == 0 || s2.size() == 0)
			return false;
		for (int i = 0; i < s1.size(); i++)
			if (!prettyprint(s1.get(i)).equals(prettyprint(s2.get(i))))
				return false;
		return true;
	}

	@FunctionSpec(name = "evolution_apis", returnType = "array of array of string", formalParameters = {
			"map[string] of queue of Method", "map[string] of queue of Method" })
	public static String[][] evolutionAPIs(HashMap<String, LinkedList<Method>> prev,
			HashMap<String, LinkedList<Method>> cur) throws Exception {
		List<String[]> results = new ArrayList<>();
		for (Entry<String, LinkedList<Method>> e1 : prev.entrySet())
			for (Entry<String, LinkedList<Method>> e2 : cur.entrySet())
				if (e1.getKey().equals(e2.getKey())) {
					matchMethods(e1, e2, results);
					break;
				}
		String[][] res = new String[results.size()][2];
		for (int i = 0; i < res.length; i++)
			res[i] = results.get(i);
		return res;
	}

	private static void matchMethods(Entry<String, LinkedList<Method>> e1, Entry<String, LinkedList<Method>> e2,
			List<String[]> results) throws Exception {
		HashMap<String, Method> deleted = new HashMap<String, Method>();
		for (Method m : e1.getValue())
			deleted.put(signature(m), m);

		HashMap<String, Method> added = new HashMap<String, Method>();
		for (Method m : e2.getValue())
			added.put(signature(m), m);

		for (Iterator<Entry<String, Method>> itr = deleted.entrySet().iterator(); itr.hasNext();) {
			Entry<String, Method> before = itr.next();
			String sig = before.getKey();
			if (added.containsKey(sig)) {
				Method beforeMethod = before.getValue();
				Method afterMethod = added.get(sig);
				if (!matchMethodBody2(beforeMethod, afterMethod))
					findAPIEvolutionPairs(beforeMethod, afterMethod, results);
				itr.remove();
				added.remove(sig);
			}
		}
	}

	private static boolean matchMethodBody2(Method m1, Method m2) {
		if (m1.getStatementsCount() == 0 || m2.getStatementsCount() == 0)
			return true;
		List<Statement> s1 = m1.getStatements(0).getStatementsList();
		List<Statement> s2 = m2.getStatements(0).getStatementsList();
		if (s1.size() != s2.size())
			return false;
		if (s1.size() == 0 || s2.size() == 0)
			return true;
		for (int i = 0; i < s1.size(); i++)
			if (!prettyprint(s1.get(i)).equals(prettyprint(s2.get(i))))
				return false;
		return true;
	}

	private static void findAPIEvolutionPairs(Method m1, Method m2, List<String[]> results) throws Exception {
		HashSet<String> deletedTypes = collectTypes(m1);
		HashSet<String> addedTypes = collectTypes(m2);

		HashSet<String> commons = Sets.newHashSet(deletedTypes);
		commons.retainAll(addedTypes);

		deletedTypes.removeAll(commons);
		addedTypes.removeAll(commons);

		if (deletedTypes.size() == 0 || addedTypes.size() == 0)
			return;

		for (String deletedType : deletedTypes)
			for (String addedType : addedTypes)
				results.add(new String[] { deletedType, addedType });
	}

	private static HashSet<String> collectTypes(Method m) {
		HashSet<String> types = Sets.newHashSet();
		BoaAbstractVisitor v = new BoaAbstractVisitor() {
			@Override
			protected boolean preVisit(final Type type) throws Exception {
				types.add(type.getName());
				return false;
			}
		};
		try {
			v.visit(m);
		} catch (Exception e) {
			return types;
		}
		return types;
	}

	@FunctionSpec(name = "cousage_apis", returnType = "array of array of string", formalParameters = {
			"map[string] of queue of Method", "map[string] of queue of Method" })
	public static String[][] cousageAPIs(HashMap<String, LinkedList<Method>> prev,
			HashMap<String, LinkedList<Method>> cur) throws Exception {
		List<String[]> results = new ArrayList<>();
		for (Entry<String, LinkedList<Method>> e1 : prev.entrySet())
			for (Entry<String, LinkedList<Method>> e2 : cur.entrySet())
				if (e1.getKey().equals(e2.getKey())) {
					matchMethods2(e1, e2, results);
					break;
				}
		String[][] res = new String[results.size()][2];
		for (int i = 0; i < res.length; i++)
			res[i] = results.get(i);
		return res;
	}

	private static void matchMethods2(Entry<String, LinkedList<Method>> e1, Entry<String, LinkedList<Method>> e2,
			List<String[]> results) throws Exception {
		HashMap<String, Method> deleted = new HashMap<String, Method>();
		for (Method m : e1.getValue())
			deleted.put(signature(m), m);

		HashMap<String, Method> added = new HashMap<String, Method>();
		for (Method m : e2.getValue())
			added.put(signature(m), m);

		for (Iterator<Entry<String, Method>> itr = deleted.entrySet().iterator(); itr.hasNext();) {
			Entry<String, Method> before = itr.next();
			String sig = before.getKey();
			if (added.containsKey(sig)) {
				Method beforeMethod = before.getValue();
				Method afterMethod = added.get(sig);
				if (!matchMethodBody2(beforeMethod, afterMethod))
					findAPICousagePairs(beforeMethod, afterMethod, results);
				itr.remove();
				added.remove(sig);
			}
		}
	}

	private static void findAPICousagePairs(Method m1, Method m2, List<String[]> results) {
		HashSet<String> deletedTypes = collectTypes(m1);
		HashSet<String> addedTypes = collectTypes(m2);

		HashSet<String> commons = Sets.newHashSet(deletedTypes);
		commons.retainAll(addedTypes);

		deletedTypes.removeAll(commons);
		addedTypes.removeAll(commons);

		List<String> added = new ArrayList<>(addedTypes);
		for (int i = 0; i < added.size(); i++) {
			for (int j = i + 1; j < added.size(); j++) {
				results.add(new String[] { added.get(i), added.get(j) });
			}
		}
	}

//	public static void main(String[] args) {
//		String s = "camelCased<List<Integer3[seq2vec]>>_____and_UNDERSCORED<HE>[SHE]_.....__camelCasedDDDDD";
//		System.out.println(Arrays.toString(split(s)));
//		System.out.println(alphabetFilter(s, "_."));

//		System.out.println(bytesOfString(500));
//		System.out.println(calculateRecordPercent(100, bytesOfString(500)));
//	}

	/*
	 * web: https://www.javamex.com/tutorials/memory/string_memory_usage.shtml
	 */
	public static int bytesOfString(int numOfChars) {
		return 8 * (int) (((numOfChars * 2) + 45) / 8);
	}

	public static float calculateRecordPercent(int combinerMem, int recordMem) {
		float maxRecords = (float) combinerMem * (1 << 20) / recordMem;
		MAX_RECORDS_FOR_SPILL = (int) maxRecords + 10;
		return maxRecords / (IO_SORT_MB * IO_SORT_SPILL_PERCENT * (1 << 16));
	}

	public static float IO_SORT_SPILL_PERCENT;
	public static int IO_SORT_MB;
	public static float IO_SORT_RECORD_PERCENT;
	public static int MAX_RECORDS_FOR_SPILL;

	static {
		IO_SORT_SPILL_PERCENT = 0.8f;
		IO_SORT_MB = 100;
		IO_SORT_RECORD_PERCENT = calculateRecordPercent(200, bytesOfString(500));
//		System.out.println(MAX_RECORDS_FOR_SPILL);
	};
}