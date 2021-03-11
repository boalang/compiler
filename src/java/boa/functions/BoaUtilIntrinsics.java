package boa.functions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;

import boa.runtime.BoaAbstractVisitor;
import boa.types.Ast.*;
import boa.types.Code.CodeRepository;
import boa.types.Code.Revision;
import boa.types.Diff.ChangedFile;
import boa.types.Shared.ChangeKind;

import static boa.functions.BoaIntrinsics.*;
import static boa.functions.BoaAstIntrinsics.*;

public class BoaUtilIntrinsics {

//	public static void main(String[] args) {
//		String s = "camelCased<List<Integer3[seq2vec]>>_____and_UNDERSCORED<HE>[SHE]_.....__camelCasedDDDDD";
//		System.out.println(Arrays.toString(split(s)));
//		System.out.println(alphabetFilter(s, "_."));

//		System.out.println(bytesOfString(500));
//		System.out.println(calculateRecordPercent(100, bytesOfString(500)));
//	}

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

		HashSet<String> commons = new HashSet<>(deletedTypes);
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
		HashSet<String> types = new HashSet<>();
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

		HashSet<String> commons = new HashSet<>(deletedTypes);
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

	@FunctionSpec(name = "modified_classes", returnType = "array of string", formalParameters = {
			"queue of Declaration", "queue of Declaration" })
	public static String[] modifiedClasses(LinkedList<Declaration> prev, LinkedList<Declaration> cur) throws Exception {
		List<String> res = new ArrayList<>();
		for (Declaration d1 : prev)
			for (Declaration d2 : cur)
				if (d1.getFullyQualifiedName().equals(d2.getFullyQualifiedName()) && isModified(d1, d2)) {
					res.add(d1.getFullyQualifiedName());
					break;
				}
		return res.toArray(new String[0]);
	}

	private static boolean isModified(Declaration d1, Declaration d2) {
		if (d1.getFieldsCount() != d2.getFieldsCount() || d1.getMethodsCount() != d2.getMethodsCount())
			return true;

		List<List<Variable>> fieldDiffs = getFieldDiffs(d1, d2);
		if (fieldDiffs.get(0).size() + fieldDiffs.get(1).size() + fieldDiffs.get(2).size() != 0)
			return true;
		List<List<Method>> methodDiffs = getMethodDiffs(d1, d2);
		if (methodDiffs.get(0).size() + methodDiffs.get(1).size() + methodDiffs.get(2).size() != 0)
			return true;

		return false;
	}

	private static List<List<Variable>> getFieldDiffs(Declaration d1, Declaration d2) {
		List<Variable> deletedFields = new ArrayList<>(d1.getFieldsList());
		List<Variable> addedFields = new ArrayList<>(d2.getFieldsList());
		List<Variable> modifiedFieldsBefore = new ArrayList<>();
		List<Variable> modifiedFieldsAfter = new ArrayList<>();

		for (Iterator<Variable> itr1 = deletedFields.iterator(); itr1.hasNext();) {
			Variable v1 = itr1.next();
			for (Iterator<Variable> itr2 = addedFields.iterator(); itr2.hasNext();) {
				Variable v2 = itr2.next();
				if (v1.getName().equals(v2.getName())) {
					itr1.remove();
					itr2.remove();
					if (!prettyprint(v1).equals(prettyprint(v2))) {
						modifiedFieldsBefore.add(v1);
						modifiedFieldsAfter.add(v2);
					}
					break;
				}
			}
		}
		List<List<Variable>> results = new ArrayList<>();
		results.add(deletedFields);
		results.add(addedFields);
		results.add(modifiedFieldsBefore);
		results.add(modifiedFieldsAfter);
		return results;
	}

	@FunctionSpec(name = "modified_methods", returnType = "array of string", formalParameters = {
			"queue of Declaration", "queue of Declaration" })
	public static String[] modifiedMethodes(LinkedList<Declaration> prev, LinkedList<Declaration> cur)
			throws Exception {
		List<String> res = new ArrayList<>();
		for (Declaration d1 : prev)
			for (Declaration d2 : cur)
				if (d1.getFullyQualifiedName().equals(d2.getFullyQualifiedName())) {
					updateMethodSigs(d1, d2, res);
					break;
				}
		return res.toArray(new String[0]);
	}

	private static void updateMethodSigs(Declaration d1, Declaration d2, List<String> res) {
		List<List<Method>> methodDiffs = getMethodDiffs(d1, d2);
		List<Method> modifiedMethods = methodDiffs.get(3);
		for (Method m : modifiedMethods)
			res.add(d2.getFullyQualifiedName() + " " + signature(m));
	}

	private static List<List<Method>> getMethodDiffs(Declaration d1, Declaration d2) {
		List<Method> deletedMethods = new ArrayList<>(d1.getMethodsList());
		List<Method> addedMethods = new ArrayList<>(d2.getMethodsList());
		List<Method> modifiedMethodsBefore = new ArrayList<>();
		List<Method> modifiedMethodsAfter = new ArrayList<>();

		for (Iterator<Method> itr1 = deletedMethods.iterator(); itr1.hasNext();) {
			Method m1 = itr1.next();
			for (Iterator<Method> itr2 = addedMethods.iterator(); itr2.hasNext();) {
				Method m2 = itr2.next();
				if (signature(m1).equals(signature(m2))) {
					itr1.remove();
					itr2.remove();
					if (!prettyprint(m1).equals(prettyprint(m2))) {
						modifiedMethodsBefore.add(m1);
						modifiedMethodsAfter.add(m2);
					}
					break;
				}
			}
		}
		List<List<Method>> results = new ArrayList<>();
		results.add(deletedMethods);
		results.add(addedMethods);
		results.add(modifiedMethodsBefore);
		results.add(modifiedMethodsAfter);
		return results;
	}

	@FunctionSpec(name = "maintained_map", returnType = "map[string] of string")
	public static HashMap<String, String> maintainedMap() {
		HashMap<String, String> map = new HashMap<>();
		String s = "nhaarman/ListViewAnimations	Archived\n" + "lucasr/twoway-view	Archived\n"
				+ "mcxiaoke/android-volley	Archived\n" + "stephanenicolas/robospice	Archived\n"
				+ "pedrovgs/DraggablePanel	Archived\n" + "chanjarster/weixin-java-tools	Archived\n"
				+ "M66B/XPrivacy	Archived\n" + "TonicArtos/StickyGridHeaders	Archived\n"
				+ "guardianproject/ChatSecureAndroid	Archived\n" + "lucasr/smoothie	Archived\n"
				+ "tjake/Solandra	Archived\n" + "rahatarmanahmed/CircularProgressView	Archived\n"
				+ "ai212983/android-spinnerwheel	Archived\n" + "pires/android-obd-reader	Archived\n"
				+ "nicoulaj/idea-markdown	Archived\n" + "scottyab/AESCrypt-Android	Archived\n"
				+ "JakeWharton/ActionBarSherlock	FSE\n" + "cyrilmottier/GreenDroid	FSE\n"
				+ "pakerfeldt/android-viewflow	FSE\n" + "flavienlaurent/datetimepicker	FSE\n"
				+ "sd6352051/NiftyDialogEffects	FSE\n" + "tjerkw/Android-SlideExpandableListView	FSE\n"
				+ "dmytrodanylyk/circular-progress-button	FSE\n" + "openaphid/android-flip	FSE\n"
				+ "square/dagger	FSE\n" + "square/otto	FSE\n" + "roboguice/roboguice	FSE\n"
				+ "RomainPiel/Shimmer-android	FSE\n" + "facebook/react	Active\n"
				+ "facebook/react-native	Active\n" + "nodejs/node	Active\n" + "atom/atom	Active\n"
				+ "ionic-team/ionic	Active\n" + "getlantern/lantern	Active\n" + "ReactiveX/RxJava	Active\n"
				+ "google/protobuf	Active\n" + "google/guava	Active\n" + "JetBrains/kotlin	Active\n"
				+ "PhilJay/MPAndroidChart	Active\n" + "bumptech/glide	Active\n" + "syncthing/syncthing	Active\n"
				+ "RocketChat/Rocket.Chat	Active\n" + "getsentry/sentry	Active\n" + "grpc/grpc	Active\n"
				+ "alibaba/fastjson	Active\n" + "minio/minio	Active\n" + "scala/scala	Active\n"
				+ "metabase/metabase	Active\n" + "bazelbuild/bazel	Active\n" + "openzipkin/zipkin	Active\n"
				+ "cakephp/cakephp	Active\n" + "gradle/gradle	Active\n" + "dropwizard/dropwizard	Active\n"
				+ "hankcs/HanLP	Active\n" + "zulip/zulip	Active\n" + "naver/pinpoint	Active\n"
				+ "dropwizard/metrics	Active\n" + "trello/RxLifecycle	Active\n" + "magento/magento2	Active\n"
				+ "dgraph-io/dgraph	Active\n" + "Netflix/eureka	Active\n" + "swagger-api/swagger-core	Active\n"
				+ "bookshelf/bookshelf	Active\n" + "kickstarter/android-oss	Active\n" + "sockeqwe/mosby	Active\n"
				+ "evernote/android-job	Active\n" + "ory/hydra	Active\n" + "codecentric/spring-boot-admin	Active\n"
				+ "medcl/elasticsearch-analysis-ik	Active\n" + "grpc/grpc-java	Active\n"
				+ "snowplow/snowplow	Active\n" + "davemorrissey/subsampling-scale-image-view	Active\n"
				+ "apereo/cas	Active\n" + "ben-manes/caffeine	Active\n" + "ag-grid/ag-grid	Active\n"
				+ "TooTallNate/Java-WebSocket	Active\n" + "zaproxy/zaproxy	Active\n" + "bolt/bolt	Active\n"
				+ "mesosphere/marathon	Active\n" + "rqlite/rqlite	Active\n" + "checkstyle/checkstyle	Active\n"
				+ "Alluxio/alluxio	Active\n" + "orientechnologies/orientdb	Active\n" + "socketio/engine.io	Active\n"
				+ "NLPchina/elasticsearch-sql	Active\n" + "spotify/annoy	Active\n"
				+ "wdullaer/MaterialDateTimePicker	Active\n" + "pili-engineering/PLDroidPlayer	Active\n"
				+ "TykTechnologies/tyk	Active\n" + "basho/riak	Active\n" + "Tencent/GT	Active\n"
				+ "Studio-42/elFinder	Active\n" + "tsuru/tsuru	Active\n" + "mapbox/mapbox-gl-native	Active\n"
				+ "owncloud/android	Active\n" + "vavr-io/vavr	Active\n" + "Ereza/CustomActivityOnCrash	Active\n"
				+ "termux/termux-app	Active\n" + "dlew/joda-time-android	Active\n"
				+ "graphql-java/graphql-java	Active\n" + "cryptomator/cryptomator	Active\n"
				+ "mongodb/mongo-java-driver	Active\n" + "junit-team/junit5	Active\n" + "spotify/helios	Active\n"
				+ "tarantool/tarantool	Active\n" + "nutzam/nutz	Active\n" + "pwittchen/ReactiveNetwork	Active\n"
				+ "azkaban/azkaban	Active\n" + "infobyte/faraday	Active\n" + "conan-io/conan	Active\n"
				+ "MediaBrowser/Emby	Active\n" + "LWJGL/lwjgl3	Active\n" + "ionic-team/ionic-native	Active\n"
				+ "btraceio/btrace	Active\n" + "TwidereProject/Twidere-Android	Active\n"
				+ "Netflix/archaius	Active\n" + "Polidea/RxAndroidBle	Active\n" + "WebGoat/WebGoat	Active\n"
				+ "igniterealtime/Openfire	Active\n" + "tdebatty/java-string-similarity	Active\n"
				+ "M66B/NetGuard	Active\n" + "codeclimate/codeclimate	Active\n" + "cloudfoundry/bosh	Active";
		for (String row : s.split("\n")) {
			String[] arr = row.split("\\s+");
			map.put(arr[0], arr[1]);
		}
		return map;
	}
	
	@FunctionSpec(name = "org_map", returnType = "map[string] of string")
	public static HashMap<String, String> orgMap() {
		HashMap<String, String> map = new HashMap<>();
		String s = "project,class\n" + 
				"Netflix/SimianArmy,project\n" + 
				"apache/sqoop,project\n" + 
				"apache/mahout,project\n" + 
				"apache/flume,project\n" + 
				"github/maven-plugins,project\n" + 
				"jenkinsci/github-plugin,project\n" + 
				"jenkinsci/violations-plugin,project\n" + 
				"twitter/hbc,project\n" + 
				"apache/tez,project\n" + 
				"twitter/hraven,project\n" + 
				"jenkinsci/mesos-plugin,project\n" + 
				"jenkinsci/docker-plugin,project\n" + 
				"airbnb/plog,project\n" + 
				"google/openrtb,project\n" + 
				"twitter/elephant-bird,project\n" + 
				"facebook/stetho,project\n" + 
				"rstoyanchev/dispatch-test,notproject\n" + 
				"rewbs/ljcu.findbugs.ext,notproject\n" + 
				"loboweissmann/groovy-grails-na-pratica,notproject\n" + 
				"kevin-ww/text.classfication,notproject\n" + 
				"knighthunter09/XPEDIA,notproject\n" + 
				"mohitsehgal/MapsApp2,notproject\n" + 
				"Robbi-Blechdose/StarTrekMod,notproject";
		for (String row : s.split("\n")) {
			String[] arr = row.split(",");
			map.put(arr[0], arr[1]);
		}
		return map;
	}
	
	@FunctionSpec(name = "util_map", returnType = "map[string] of string")
	public static HashMap<String, String> utilMap() {
		HashMap<String, String> map = new HashMap<>();
		String s = "samirahmed/Iris-Voice-Automation,project\n" + 
				"aamattos/GMF-Tooling-Visual-Editor,project\n" + 
				"steveliles/dsl4xml,project\n" + 
				"verdigris/HappyNewYear,project\n" + 
				"sonatype/maven-guide-en,project\n" + 
				"generators-io-projects/generators,project\n" + 
				"bingoohuang/buka,project\n" + 
				"jlagerweij/swagger-springweb-maven-plugin,project\n" + 
				"liulhdarks/darks-codec,project\n" + 
				"safaci2000/google-voice-java,project\n" + 
				"v3l0c1r4pt0r/HistoriaPojazdu,project\n" + 
				"lucasr/dspec,project\n" + 
				"Kalbintion/Kdkbot,project\n" + 
				"jjbunn/MultipathODL,project\n" + 
				"PiDyGB/android-slidinglayout,project\n" + 
				"lazymaniac/LexSem,project\n" + 
				"eetac/android-logging-log4j,project\n" + 
				"rstoyanchev/dispatch-test,notproject\n" + 
				"rewbs/ljcu.findbugs.ext,notproject\n" + 
				"loboweissmann/groovy-grails-na-pratica,notproject\n" + 
				"kevin-ww/text.classfication,notproject\n" + 
				"knighthunter09/XPEDIA,notproject\n" + 
				"mohitsehgal/MapsApp2,notproject\n" + 
				"Robbi-Blechdose/StarTrekMod,notproject";
		for (String row : s.split("\n")) {
			String[] arr = row.split(",");
			map.put(arr[0], arr[1]);
		}
		return map;
	}

}