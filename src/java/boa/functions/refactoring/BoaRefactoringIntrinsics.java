package boa.functions.refactoring;

import boa.functions.BoaAstIntrinsics;
import boa.functions.FunctionSpec;
import boa.types.Ast.ASTRoot;
import boa.types.Ast.Namespace;
import boa.types.Code.CodeRepository;
import boa.types.Code.Revision;
import boa.types.Diff.ChangedFile;
import boa.types.Shared.ChangeKind;
import gr.uom.java.xmi.UMLModel;
import org.refactoringminer.api.RefactoringType;

import static boa.functions.BoaAstIntrinsics.*;
import static boa.functions.BoaIntrinsics.*;
import static boa.functions.BoaMetricIntrinsics.getMetrics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.refactoringminer.api.Refactoring;
import org.refactoringminer.rm1.GitHistoryRefactoringMinerImpl;

import com.google.common.math.*;

public class BoaRefactoringIntrinsics {

	@FunctionSpec(name = "getrefactoringtype", returnType = "string", formalParameters = { "string" })
	public static String getRefactoringType(final String desctiption) throws Exception {
		return RefactoringType.extractFromDescription(desctiption).getDisplayName();
	}

	@FunctionSpec(name = "detectrefactoringsbyid", returnType = "array of string", formalParameters = {
			"CodeRepository", "string" })
	public static String[] detectRefactorings(final CodeRepository cr, final String id) throws Exception {
		Revision currentRevision = getRevisionById(cr, id);
		if (currentRevision != null)
			return detectRefactorings(cr, currentRevision);
		return new String[0];
	}

	@FunctionSpec(name = "getconsideredtypes", returnType = "set of string")
	public static HashSet<String> getConsideredTypes() {
		String[] types = new String[] { "Move Method", "Pull Up Attribute", "Move Attribute", "Rename Class",
				"Push Down Attribute", "Move Class", "Extract Method", "Rename Method", "Pull Up Method",
				"Inline Method", "Extract Superclass", "Change Package", "Extract Interface", "Extract And Move Method",
				"Move And Rename Class" };
		HashSet<String> typeSet = new HashSet<String>(Arrays.asList(types));
		return typeSet;
	}

	@FunctionSpec(name = "findequivalentfilepath", returnType = "string", formalParameters = { "array of string",
			"string" })
	public static String findEquivalentFilePath(String[] filePathes, String namespace) throws Exception {
		String path = namespace.replace('.', '/');
		for (String filePath : filePathes)
			if (filePath.contains(path))
				return filePath;
		// if cannot find, check parent path
		int idx = path.lastIndexOf('/');
		if (idx > -1) {
			String parentPath = path.substring(0, idx);
			return findEquivalentFilePath(filePathes, parentPath);
		}
		return null;
	}

	@FunctionSpec(name = "getfilesbefore", returnType = "array of ChangedFile", formalParameters = { "Revision",
			"array of ChangedFile" })
	public static ChangedFile[] getFilesBefore(Revision r, ChangedFile[] snapshot) {
		HashSet<String> fileNamesBefore = new HashSet<String>();
		for (ChangedFile cf : r.getFilesList()) {
			if (cf.getChange() == ChangeKind.ADDED)
				continue;
			if (cf.getChange() == ChangeKind.RENAMED)
				fileNamesBefore.add(cf.getPreviousNames(0));
			else
				fileNamesBefore.add(cf.getName());
		}
		List<ChangedFile> filesBefore = new ArrayList<ChangedFile>();
		for (ChangedFile cf : snapshot) {
			if (fileNamesBefore.isEmpty())
				break;
			if (fileNamesBefore.contains(cf.getName())) {
				filesBefore.add(cf);
				fileNamesBefore.remove(cf.getName());
			}
		}
		return filesBefore.toArray(new ChangedFile[0]);
	}

	@FunctionSpec(name = "isleafclass", returnType = "bool", formalParameters = { "array of ChangedFile", "string" })
	public static boolean isLeafClass(ChangedFile[] snapshot, String namespace) {
		String path = namespace.replace('.', '/');
		ChangedFile file = getChangedFile(snapshot, path);
		if (file != null) {
			ASTRoot root = BoaAstIntrinsics.getast(file);
			for (Namespace ns : root.getNamespacesList()) {
				String temp = ns.getName();
				if (temp.startsWith("java."))
					continue;
				if (temp.startsWith("static ")) {
					temp = temp.replace("static ", "");
					int idx = temp.lastIndexOf('.');
					if (idx != -1)
						temp = temp.substring(0, idx);
				}
				if (getChangedFile(snapshot, temp.replace('.', '/')) != null)
					return false;
			}
		}
		return true;
	}

	private static ChangedFile getChangedFile(ChangedFile[] snapshot, String path) {
		for (ChangedFile cf : snapshot) {
			if (cf.getName().contains(path))
				return cf;
		}
		return null;
	}

	@FunctionSpec(name = "getpackagebefore", returnType = "string", formalParameters = { "string" })
	public static String getPackageBefore(String description) {
		return BoaRefactoringType.getPackageBefore(description);
	}

	@FunctionSpec(name = "getinvolvedclasses", returnType = "array of string", formalParameters = { "string" })
	public static String[] getInvolvedClasses(String description) {
		return BoaRefactoringType.getBeforeClasses(description);
	}

	@FunctionSpec(name = "getinvolvedclassesinstring", returnType = "string", formalParameters = { "string" })
	public static String getInvolvedClassesInString(String description) {
		return Arrays.asList(BoaRefactoringType.getBeforeClasses(description)).toString();
	}

	public static boolean hasOutOfMemoryError;
	public static boolean repoCloseMode = false;

	@FunctionSpec(name = "activerepoclosemode")
	public static void activeRepoCloseMode() {
		repoCloseMode = true;
	}

	@FunctionSpec(name = "detectrefactoringsbyrevision", returnType = "array of string", formalParameters = {
			"CodeRepository", "Revision" })
	public static String[] detectRefactorings(final CodeRepository cr, final Revision currentRevision)
			throws Exception {
		Map<String, String> renamedFilesHint = new HashMap<String, String>();

		if (updateRenamedFilesHintAndCheckRefactoringPossibility(currentRevision, renamedFilesHint)) {
			hasOutOfMemoryError = false;

			Set<String> fileNamesBefore = new HashSet<String>();
			Set<String> fileNamesCurrent = new HashSet<String>();
			updateFileNames(fileNamesBefore, fileNamesCurrent, currentRevision);

			// before
			Map<String, String> fileContentsBefore = new LinkedHashMap<String, String>();
			Set<String> repositoryDirectoriesBefore = new LinkedHashSet<String>();
			ChangedFile[] snapshotBefore = getSnapshotByIndex(cr, currentRevision.getParents(0));
			updateFileContents(fileContentsBefore, repositoryDirectoriesBefore, snapshotBefore, fileNamesBefore);
			// current
			Map<String, String> fileContentsCurrent = new LinkedHashMap<String, String>();
			Set<String> repositoryDirectoriesCurrent = new LinkedHashSet<String>();
			ChangedFile[] snapshotCurrent = updateSnapshotByRevision(snapshotBefore, currentRevision);
			updateFileContents(fileContentsCurrent, repositoryDirectoriesCurrent, snapshotCurrent, fileNamesCurrent);

//			System.out.println(currentRevision.getId() + " " + snapshotBefore.length + " " + snapshotCurrent.length
//					+ " " + fileContentsBefore.size() + " " + fileContentsCurrent.size() + " "
//					+ currentRevision.getFilesCount());

			// close jgit repo to avoid memory leak
			if (repoCloseMode)
				closeRepo();

			// build model and detect refactorings
			List<Refactoring> refactoringsAtRevision = new ArrayList<Refactoring>();
			try {
				UMLModel modelBefore = GitHistoryRefactoringMinerImpl.createModel(fileContentsBefore,
						repositoryDirectoriesBefore);
				UMLModel modelCurrent = GitHistoryRefactoringMinerImpl.createModel(fileContentsCurrent,
						repositoryDirectoriesCurrent);
				refactoringsAtRevision = modelBefore.diff(modelCurrent, renamedFilesHint).getRefactorings();
			} catch (OutOfMemoryError E) {
				System.out.println("OutOfMemoryError: " + currentRevision.getId());
				hasOutOfMemoryError = true;
				return new String[0];
			} catch (Exception e) {
				System.out.println("Throw Exception: " + currentRevision.getId());
				return new String[0];
			}
			refactoringsAtRevision = refactoringsAtRevision.stream().distinct().collect(Collectors.toList());
			String[] res = new String[refactoringsAtRevision.size()];
			for (int i = 0; i < res.length; i++) {
				res[i] = refactoringsAtRevision.get(i).toString();
			}
			return res;
		}

		System.out.println(currentRevision.getId() + " is not possible to contain refactorings");
		return new String[0];
	}

	private static void updateFileNames(Set<String> fileNamesBefore, Set<String> fileNamesCurrent, Revision r) {
		for (ChangedFile cf : r.getFilesList()) {
			switch (cf.getChange()) {
			case MODIFIED:
			case COPIED:
				fileNamesBefore.add(cf.getName());
				fileNamesCurrent.add(cf.getName());
				break;
			case RENAMED:
				fileNamesBefore.add(cf.getPreviousNames(0));
				fileNamesCurrent.add(cf.getName());
				break;
			case ADDED:
				fileNamesCurrent.add(cf.getName());
				break;
			case DELETED:
				fileNamesBefore.add(cf.getName());
				break;
			default:
				break;
			}
		}
	}

	public static String[] detectRefactoring(final CodeRepository cr, final Revision currentRevision) throws Exception {
		return null;
	}

	@FunctionSpec(name = "hasoutofmemoryerror", returnType = "bool")
	public static boolean hasOutOfMemoryError() throws Exception {
		return hasOutOfMemoryError;
	}

	private static void updateFileContents(Map<String, String> fileContents, Set<String> repositoryDirectories,
			ChangedFile[] snapshot, Set<String> fileNames) {
		for (ChangedFile cf : snapshot) {
			String pathString = cf.getName();
			if (isJavaFile(pathString)) {
				if (fileNames.contains(pathString)) {
					String content = getContent(cf);
					if (content != null)
						fileContents.put(pathString, getContent(cf));
				}
				int idx = pathString.lastIndexOf("/");
				if (idx != -1) {
					String directory = pathString.substring(0, idx);
					repositoryDirectories.add(directory);
					String subDirectory = new String(directory);
					while (subDirectory.contains("/")) {
						subDirectory = subDirectory.substring(0, subDirectory.lastIndexOf("/"));
						repositoryDirectories.add(subDirectory);
					}
				}
			}
		}
	}

	@FunctionSpec(name = "updatesnapshotbyrevision", returnType = "array of ChangedFile", formalParameters = {
			"array of ChangedFile", "Revision" })
	public static ChangedFile[] updateSnapshotByRevision(ChangedFile[] snapshot, Revision r) {
		HashMap<String, ChangedFile> map = new HashMap<String, ChangedFile>();
		for (ChangedFile cf : snapshot)
			map.put(cf.getName(), cf);
		ArrayList<ChangedFile> files = new ArrayList<ChangedFile>();
		for (ChangedFile cf : r.getFilesList())
			if (isJavaFile(cf.getName()))
				files.add(cf);
		for (ChangedFile cf : files) {
			ChangeKind ck = cf.getChange();
			switch (ck) {
			case ADDED:
				map.put(cf.getName(), cf);
				break;
			case COPIED:
				map.put(cf.getName(), cf);
				break;
			case DELETED:
				map.remove(cf.getName());
				break;
			case RENAMED:
				for (String name : cf.getPreviousNamesList())
					map.remove(name);
				map.put(cf.getName(), cf);
				break;
			case MODIFIED:
				map.replace(cf.getName(), cf);
				break;
			default:
				map.put(cf.getName(), cf);
				break;
			}
		}
		return map.values().toArray(new ChangedFile[0]);
	}

	private static boolean updateRenamedFilesHintAndCheckRefactoringPossibility(Revision r,
			Map<String, String> renamedFilesHint) {
		int javaFileCount = 0;
		int adds = 0;
		int removes = 0;
		for (ChangedFile cf : r.getFilesList()) {
			if (isJavaFile(cf.getName())) {
				javaFileCount++;
				switch (cf.getChange()) {
				case RENAMED:
					renamedFilesHint.put(cf.getPreviousNames(0), cf.getName());
					break;
				case ADDED:
					adds++;
					break;
				case DELETED:
					removes++;
					break;
				default:
					break;
				}
			}
		}
		// If all files are added or deleted or non-java files, then there is no
		// refactoring.
		// If the revision has no parent, then there is no refactoring.
		return !(Math.max(adds, removes) == javaFileCount || javaFileCount == 0 || r.getParentsCount() == 0);
	}

	private static boolean isJavaFile(String path) {
		return path.endsWith(".java");
	}

	//////////////////////////////////////////////////
	// Refactoring Prediction Feature STATS //
	//////////////////////////////////////////////////
	@FunctionSpec(name = "getstats", returnType = "array of array of float", formalParameters = "array of ChangedFile")
	public static double[][] getStats(ChangedFile[] snapshot) throws Exception {
		double[][] results = new double[6][5];
		// wmc, rfc, lcom, dit, noc, cbo
		List<Double> wmc = new ArrayList<Double>();
		List<Double> rfc = new ArrayList<Double>();
		List<Double> lcom = new ArrayList<Double>();
		List<Double> dit = new ArrayList<Double>();
		List<Double> noc = new ArrayList<Double>();
		List<Double> cbo = new ArrayList<Double>();
		HashMap<String, double[]> metrics = getMetrics(snapshot);
		if (snapshot.length == 0 || metrics.size() == 0)
			return results;
		HashMap<String, List<String>> map = new HashMap<String, List<String>>();
		for (Entry<String, double[]> entry : metrics.entrySet()) {
			String fqn = entry.getKey();
			String packageName = getPackageNameFromFQN(fqn);
			if (!map.containsKey(packageName))
				map.put(packageName, new ArrayList<String>());
			map.get(packageName).add(fqn);
			double[] m = entry.getValue();
			wmc.add(m[0]);
			rfc.add(m[1]);
			lcom.add(m[2]);
			dit.add(m[3]);
			noc.add(m[4]);
			cbo.add(m[5]);
		}
		Stats wmcStats = Stats.of(wmc);
		Stats rfcStats = Stats.of(rfc);
		Stats lcomStats = Stats.of(lcom);
		Stats ditStats = Stats.of(dit);
		Stats nocStats = Stats.of(noc);
		Stats cboStats = Stats.of(cbo);
		// min, max, mean, median, std
		results[0] = new double[] { wmcStats.min(), wmcStats.max(), wmcStats.mean(), Quantiles.median().compute(wmc),
				wmcStats.populationStandardDeviation() };
		results[1] = new double[] { rfcStats.min(), rfcStats.max(), rfcStats.mean(), Quantiles.median().compute(rfc),
				rfcStats.populationStandardDeviation() };
		results[2] = new double[] { lcomStats.min(), lcomStats.max(), lcomStats.mean(),
				Quantiles.median().compute(lcom), lcomStats.populationStandardDeviation() };
		results[3] = new double[] { ditStats.min(), ditStats.max(), ditStats.mean(), Quantiles.median().compute(dit),
				ditStats.populationStandardDeviation() };
		results[4] = new double[] { nocStats.min(), nocStats.max(), nocStats.mean(), Quantiles.median().compute(noc),
				nocStats.populationStandardDeviation() };
		results[5] = new double[] { cboStats.min(), cboStats.max(), cboStats.mean(), Quantiles.median().compute(cbo),
				cboStats.populationStandardDeviation() };
		return results;
	}
	
	private static String getPackageNameFromFQN(String fqn) {
		int idx = fqn.lastIndexOf('.');
		if (idx < 0)
			return fqn;
		return fqn.substring(0, idx);
	}

	private static String getFQN(String s) {
		return s.split(" ")[1];
	}

	//////////////////////////////////////////////////
	// Oracle Dataset Project Names and Commit Ids //
	//////////////////////////////////////////////////
	@FunctionSpec(name = "oracle", returnType = "map[string] of set of string")
	public static HashMap<String, HashSet<String>> oracle() {
		String[] names = new String[] { "wicketstuff/core", "spring-projects/spring-boot",
				"spring-projects/spring-boot", "spring-projects/spring-boot", "spring-projects/spring-boot",
				"spring-projects/spring-boot", "spring-projects/spring-boot", "spring-projects/spring-boot",
				"reactor/reactor", "slapperwan/gh4a", "dropwizard/metrics", "dropwizard/metrics", "cwensel/cascading",
				"rackerlabs/blueflood", "rackerlabs/blueflood", "open-keychain/open-keychain",
				"open-keychain/open-keychain", "open-keychain/open-keychain", "checkstyle/checkstyle",
				"checkstyle/checkstyle", "checkstyle/checkstyle", "checkstyle/checkstyle", "ratpack/ratpack",
				"ratpack/ratpack", "plutext/docx4j", "plutext/docx4j", "plutext/docx4j", "mockito/mockito",
				"mockito/mockito", "cucumber/cucumber-jvm", "geometer/FBReaderJ", "HubSpot/Singularity",
				"HubSpot/Singularity", "spring-projects/spring-integration", "spring-projects/spring-integration",
				"spring-projects/spring-integration", "ReactiveX/RxJava", "eclipse/jetty.project",
				"eclipse/jetty.project", "eclipse/jetty.project", "square/okhttp", "BuildCraft/BuildCraft",
				"BuildCraft/BuildCraft", "jfinal/jfinal", "redsolution/xabber-android", "codinguser/gnucash-android",
				"abarisain/dmix", "siacs/Conversations", "siacs/Conversations", "siacs/Conversations",
				"spring-projects/spring-hateoas", "tomahawk-player/tomahawk-android", "koush/AndroidAsync",
				"rstudio/rstudio", "rstudio/rstudio", "rstudio/rstudio", "rstudio/rstudio", "apache/giraph",
				"apache/giraph", "square/mortar", "spring-projects/spring-security", "spring-projects/spring-security",
				"spring-projects/spring-security", "alibaba/druid", "clojure/clojure",
				"spring-projects/spring-framework", "spring-projects/spring-framework",
				"spring-projects/spring-framework", "spring-projects/spring-framework",
				"spring-projects/spring-framework", "spring-projects/spring-framework", "Atmosphere/atmosphere",
				"google/closure-compiler", "google/closure-compiler", "google/closure-compiler",
				"google/closure-compiler", "google/closure-compiler", "nutzam/nutz", "nutzam/nutz",
				"spring-projects/spring-data-jpa", "spring-projects/spring-data-jpa", "glyptodon/guacamole-client",
				"glyptodon/guacamole-client", "square/wire", "graphhopper/graphhopper",
				"go-lang-plugin-org/go-lang-idea-plugin", "go-lang-plugin-org/go-lang-idea-plugin",
				"go-lang-plugin-org/go-lang-idea-plugin", "spring-projects/spring-roo", "AntennaPod/AntennaPod",
				"baasbox/baasbox", "Netflix/zuul", "realm/realm-java", "realm/realm-java", "jersey/jersey",
				"jersey/jersey", "jersey/jersey", "jersey/jersey", "bennidi/mbassador", "k9mail/k-9", "k9mail/k-9",
				"JoanZapata/android-iconify", "JoanZapata/android-iconify", "crashub/crash", "apache/pig", "apache/pig",
				"AsyncHttpClient/async-http-client", "Netflix/eureka", "Netflix/eureka", "Netflix/eureka",
				"Netflix/eureka", "scobal/seyren", "greenrobot/greenDAO", "skylot/jadx", "mongodb/mongo-java-driver",
				"spotify/helios", "spotify/helios", "spotify/helios", "spotify/helios", "spotify/helios",
				"brianfrankcooper/YCSB", "ignatov/intellij-erlang", "ignatov/intellij-erlang", "apache/zookeeper",
				"linkedin/rest.li", "linkedin/rest.li", "linkedin/rest.li", "linkedin/rest.li", "vaadin/vaadin",
				"vaadin/vaadin", "FasterXML/jackson-databind", "FasterXML/jackson-databind",
				"FasterXML/jackson-databind", "infinispan/infinispan", "infinispan/infinispan", "infinispan/infinispan",
				"infinispan/infinispan", "infinispan/infinispan", "infinispan/infinispan", "infinispan/infinispan",
				"spring-projects/spring-data-mongodb", "HdrHistogram/HdrHistogram", "Netflix/genie",
				"phishman3579/java-algorithms-implementation", "phishman3579/java-algorithms-implementation",
				"phishman3579/java-algorithms-implementation", "google/auto", "google/auto", "xetorthio/jedis",
				"xetorthio/jedis", "datastax/java-driver", "datastax/java-driver", "datastax/java-driver",
				"datastax/java-driver", "datastax/java-driver", "puniverse/quasar", "puniverse/quasar",
				"bitcoinj/bitcoinj", "bitcoinj/bitcoinj", "bitcoinj/bitcoinj", "bitcoinj/bitcoinj", "bitcoinj/bitcoinj",
				"bitcoinj/bitcoinj", "PhilJay/MPAndroidChart", "belaban/JGroups", "novoda/android-demos",
				"spring-projects/spring-data-neo4j", "spring-projects/spring-data-neo4j", "square/javapoet",
				"Activiti/Activiti", "Activiti/Activiti", "Activiti/Activiti", "hierynomus/sshj",
				"jeeeyul/eclipse-themes", "QuantumBadger/RedReader", "QuantumBadger/RedReader", "google/j2objc",
				"google/j2objc", "spring-projects/spring-data-rest", "google/truth", "google/truth", "apache/tomcat",
				"SlimeKnights/TinkersConstruct", "eucalyptus/eucalyptus", "apache/drill", "apache/drill",
				"apache/drill", "apache/drill", "apache/drill", "apache/drill", "apache/drill", "apache/drill",
				"crate/crate", "crate/crate", "crate/crate", "crate/crate", "crate/crate", "GoClipse/goclipse",
				"RoboBinding/RoboBinding", "oblac/jodd", "querydsl/querydsl", "querydsl/querydsl", "antlr/antlr4",
				"antlr/antlr4", "joel-costigliola/assertj-core", "thymeleaf/thymeleaf", "thymeleaf/thymeleaf",
				"facebook/facebook-android-sdk", "facebook/facebook-android-sdk", "jOOQ/jOOQ", "jOOQ/jOOQ",
				"bumptech/glide", "brettwooldridge/HikariCP", "brettwooldridge/HikariCP", "brettwooldridge/HikariCP",
				"jline/jline2", "jline/jline2", "undertow-io/undertow", "zeromq/jeromq", "AdoptOpenJDK/jitwatch",
				"Athou/commafeed", "Alluxio/alluxio", "Alluxio/alluxio", "Alluxio/alluxio", "Alluxio/alluxio",
				"Alluxio/alluxio", "Alluxio/alluxio", "jberkel/sms-backup-plus", "Graylog2/graylog2-server",
				"Graylog2/graylog2-server", "Graylog2/graylog2-server", "Graylog2/graylog2-server",
				"Graylog2/graylog2-server", "katzer/cordova-plugin-local-notifications",
				"opentripplanner/OpenTripPlanner", "opentripplanner/OpenTripPlanner",
				"jboss-developer/jboss-eap-quickstarts", "cbeust/testng", "addthis/hydra", "addthis/hydra",
				"SimonVT/schematic", "dreamhead/moco", "netty/netty", "netty/netty", "netty/netty", "netty/netty",
				"netty/netty", "selendroid/selendroid" };
		String[] ids = new String[] { "8ea46f48063c38473c12ca7c114106ca910b6e74",
				"becced5f0b7bac8200df7a5706b568687b517b90", "20d39f7af2165c67d5221f556f58820c992d2cc6",
				"84937551787072a4befac29fb48436b3187ac4c6", "1e464da2480568014a87dd0bac6febe63a76c889",
				"cb98ee25ff52bf97faebe3f45cdef0ced9b4416e", "1cfc6f64f64353bc5530a8ce8cdacfc3eba3e7b2",
				"b47634176fa48ad925f79886c6aaca225cb9af64", "669b96c8aa4ed5134617932118de563bd4c34066",
				"b8fffb706258db4c4d2f608d8e8dad9312e2230d", "2331fe19ea88a22de32f15375de8118226eaa1e6",
				"4c6ab3d77cc67c7a91155d884077520dcf1509c6", "f9d3171f5020da5c359cdda28ef05172e858c464",
				"fce2d1f07c14bbac286e16ec666fd4bf26abd43d", "c76e6e1f27a6697b3b88ad4ed710441b801afb3b",
				"c11fef6e7c80681ce69e5fdc7f4796b0b7a18e2b", "de50b3becb31c367f867382ff9cd898ba1628350",
				"49d544d558e9c7f1106b5923204b1fbec2696cf7", "2f7481ee4e20ae785298c31ec2f979752dd7eb03",
				"5a9b7249e3d092a78ac8e7d48aeeb62bf1c44e20", "a07cae0aca9f9072256b3a5fd05779e8d69b9748",
				"febbc986cb25ed460ea601c0a68c7d2597f89ee4", "2581441eda268c45306423dd4c515514d98a14a0",
				"da6167af3bdbf7663af6c20fb603aba27dd5e174", "59b8e89e61432d1d8f25cb003b62b3ac004d1b6f",
				"e29924b33ec0c0298ba4fc3f7a8c218c8e6cfa0c", "1ba361438ab4d7f6a0305428ba40ba62e2e6ff3c",
				"7f20e63a7252f33c888085134d16ee8bf45f183f", "2d036ecf1d7170b4ec7346579a1ef8904109530a",
				"0e815f3e1339f91960c7c64ab395de6dd8ff9eec", "42e0649f82779ecd48bff6448924fc7dc2534554",
				"45ada13b852af85e1ae0491267a0239d9bdf6f3f", "944aea445051891280a8ab7fbbd514c19646f1ab",
				"247232bdde24b81814a82100743f77d881aaf06b", "4cca684f368d3ff719c62d3fa4cac3cdb7828bff",
				"ec5230abc7500734d7b78a176c291378e100a927", "8ad226067434cd39ce493b336bd0659778625959",
				"1f3be625e62f44d929c01f6574678eea05754474", "13b63c194b010201c439932ece2f1bc628ebf287",
				"837d1a74bb7d694220644a2539c4440ce55462cf", "c753d2e41ba667f9b5a31451a16ecbaecdc65d80",
				"a5cdd8c4b10a738cb44819d7cc2fee5f5965d4a0", "6abc40ed4850d74ee6c155f5a28f8b34881a0284",
				"881baed894540031bd55e402933bcad28b74ca88", "faaf826e901f43d1b46105b18e655eb120f3ffef",
				"bba4af3f52064b5a2de2c9a57f9d34ba67dcdd8c", "885771d57c97bd2dd48951e8aeaaa87ceb87532b",
				"925801c14e7500313069b2bc04abd066798a881c", "e6cb12dfe414497b4317820497985c110cb81864",
				"bdc9f9a44f337ab595a3570833dc6a0558df904c", "8bdc57ba8975d851fe91edc908761aacea624766",
				"56c273ee11296288cb15320c3de781b94a1e8eb4", "1bc7905b07821f840068089343e6b77a8686d1ab",
				"9a581e07cb6381d70f3fd9bb2055e810e2a682a9", "4983f83d1bedb7b737fc56d409c1c06b04e34e4e",
				"229d1b60c03a3f8375451c68a6911660a3993777", "cb49e436b9d7ee55f2531ebc2ef1863f5c9ba9fe",
				"add1d4f07c925b8a9044cb3aa5bb4abdeaf49fc7", "03ade425dd5a65d3a713d5e7d85aa7605956fbd2",
				"72dda3404820a82d53f1a16bb2ed9ad95f745d3c", "08b1b56e2cd5ad72126f4bbeb15a47d9b104dfff",
				"64938ebcfc2fc8cd9ccd6cf31dbcd8cdd0660aca", "fcc9a34356817d93c24b5ccf3107ec234a28b136",
				"87f3f8144b7a6cb57b6e21cd3753d09ecde0d88f", "309c03055b06525c275b278542c881019424760e",
				"dd4bc630c3de70204081ab196945d6b55ab03beb", "ece12f9d370108549fffac105e4bcb7faeaaf124",
				"e083683f4fe9206609201bb39a60bbd8ee0c8a0f", "fffdd1e9e9dc887c3e8973147904d47d9fffbb47",
				"ef0eb01f93d6c485cf37692fd193833a6821272a", "31a5434ea433bdec2283797bf9415c02bb2f41c1",
				"69c229b7611ff8c6a20ff2d4da917a68c1cde64a", "ea96643364e91125f560e9508a5cbcdb776bde64",
				"b9a17665b158955ad28ef7f50cc0a8585460f053", "ba5e6d44526a2491a7004423ca2ad780c6992c46",
				"5a853a60f93e09c446d458673bc7a2f6bb26742c", "545a7d027b4c55c116dc52d9cd8121fbb09777f0",
				"6599c748ef35d38085703cf3bd41b9b5b6af5f32", "de7efe40dad0f4bb900c4fffa80ed377745532b3",
				"36d1b0717bc5836bba39985caadc2df5f2533ac4", "c13f3469e1d64ec97b11f0509e45f9c3fa8ff88a",
				"ebb483320d971ff4d9e947309668f5da1fcd3d23", "ce1f3d07976de31aed8f8189ec5e1a6453f4b580",
				"85a690e3cdbbb8447342eefdf690e22ad1b33e02", "7f80425b6a0af9bdfef12c8a873676e39e0a04a6",
				"b8929ccb4057c74ac64679216487a4abcd3ae1c3", "3d5e343df6a39ce3b41624b90974d83e9899541e",
				"0b93231025f51c7ec62fd8588985c5dc807854e4", "0bb4cca1105fc6eb86e7c4b75bfff3dbbd55f0c8",
				"c64217e2b485f3c6b997a55b1ef910c8b72779d3", "d949fe9079a82ee31aa91244aa67baaf56b7e28f",
				"b25d3f32ed2e2da86f5c746098686445c2e2a314", "9b5b10a0c254017a48651771029f4dfc0a61bcfa",
				"6cf596df183b3c3a38ed5dd9bb3b0100c6548ebb", "d57b1401f874f96a53f1ec1c0f8a6089ae66a4ce",
				"d94ca2b27c9e8a5fa9fe19483d58d2f2ef024606", "ee5aa50af6b4586fbe92cab718abfae8113a81aa",
				"fab1516773d50bf86d9cc37e2f6db13496f0ecae", "40e41d11d7847d660bba6691859b0506514bd0ac",
				"23c49d834d3859fc76a604da32d1789d2e863303", "9d44f0e06232661259681d64002dd53c7c43099d",
				"eb500cca282e39d01a9882e1d0a83186da6d1a26", "b08f28a10d050beaba6250e9e9c46efe13d9caaa",
				"2801269c7e47bd6e243612654a74cee809d20959", "92dce401344a28ff966ad4cf3dd969a676852315",
				"7a1659c12d76b510809dea1dea1f5100bcf4cd60", "f01d8610b9ceebc1de59d42f569b8af3efbe0a0f",
				"f6212a7e474f812f31ddbce6d4f7a7a0d498b751", "457a7f637ddb226acf477cae0b04c8ff16ec9a50",
				"1cacbe2ad700275bc575234ff2b32ee0d6493817", "5103ace802b2819438318dd53b5b07512aae0d25",
				"5fb36a321af7df470d4c845cb18da8f85be31c38", "d6d9dd4365387816fda6987a4ad9b679c27e72a3",
				"2d8d4164830631d3125575f055b417c5addaa22f", "8c5a20d786e66ee4c4b0d743f0f80bf681c419be",
				"dd8753cfb0f67db4dde6c5254e2df3104b635dae", "3ffd70929c08be5cf14f156189e8050969caa87e",
				"687bda5a3ea1b5daae2764653843d318c77f4590", "da39bfeb9c370abe2d86e6e327fade252434090d",
				"cc02c00b8a92ef34d1a8bcdf44a45fb01a8dea6c", "0b024834549c53512ef18bce89f60ef9225d4819",
				"3855f0ca82795f7481b34342c7d9e5644a1d42c3", "e3b84c8753a21b1b15cfc9aa90b5e0c56d290f41",
				"3fd77b419673ce6ec41e06cdc27558b1d8f4ca06", "54fa890a6af4ccf564fb481d3e1b6ad4d084de9e",
				"f61db44ca4a862f1a84450643d92f85449016cfa", "ec5ea36faa3dd74585bb339beabdba6149ed63be",
				"bd0d3bf75d31a8b5db34b8b66dfb28e5e1f492de", "b0d5315e8ba95d099f93dc2d16339033a6525b59",
				"0f9d0b0bf1cd5fb58f47f22bd6d52a9fac31c530", "cfe88fe3fbcc6b02ca55cee7b1f4ab13e249edea",
				"44dea1f292933192ea5287d9b3e14a7daaef3c0f", "da29a040ebae664274b28117b157044af0f525fa",
				"8f446b6ddf540e1b1fefca34dd10f45ba7256095", "35b6c869546a7968b6fd2f640add6eea87e03c22",
				"4184c577f4bbc57f3ac13639557cfd99cdaca3e7", "ce4f6292d6350a2c6b82d995352fdf6d07042c9c",
				"03573a655bcbb77f7a76d8e22d851cc22796b4f8", "043030723632627b0908dca6b24dae91d3dfd938",
				"e3b0d87b3ca0fd27cec39937cb3dc3a05b0cfc4e", "3224fa8ce7e0079d6ad507e17534cdf01f758876",
				"0e65ac4da70c6ca5c67bb8418e67db914218042f", "b77de40c0f3dd43a16f2491558594a61682271fc",
				"ab98bcacf6e5bf1c3a06f6bcca68f178f880ffc9", "f2385a56e6aa040ea4ff18a23ce5b63a4eeacf29",
				"4ffcb5a65e6d24c58ef75a5cd7692e875619548d", "8fc60d81fe0e46e7e5c96e71d4a93fcadc6bde4f",
				"8967e7c33c59e1336e1e3b4671293ced5697fca6", "d4b4aecbc69bbd04ba87c4e32a52cff3d129906a",
				"6c3dde45e8cbd0c1fa73072fad7610275afc6240", "9de5f0d408f861455716b8410fd53f62b360787d",
				"1edac0e92080e7c5e971b2d56c8753bf44ea8a6c", "d5134b15fe6545ec8ab5c2256006cd6fe19eac92",
				"3a0603f8f778be3219a5a0f3a7845cda65f1e172", "14abb6919a99a0d6d500198dd2e30c83b1bb6709",
				"56d4b999e8be70be237049708f019c278c356e71", "c22d40fab8dfe4c5cad9ba582caf0855ff64b324",
				"95bfa40630e34f6f369e0055d9f37f49bca60247", "2fd96c777164dd812e8b5a4294b162889601df1d",
				"1d96e1ad1dca6e2151603e10515bb04f0c2730fc", "12602650ce99f34cb530fc24266c23e39733b0bb",
				"7744a00629514b9539acac05596d64af878fe747", "a6601066ddc72ef8e71c46c5a51e1252ea0a1af5",
				"3514aaedf9624222c985cb3abb12df2d9b514b12", "f1533756133dec84ce8218202585ac85904da7c9",
				"5cdabae35f0642e9fe243afe12e4c16b3378a150", "ef2a0d63393484975854fc08ad0fd3abc7dd76b0",
				"071588a418dbc743e0f7dbfe218cd8a6c0f97421", "5a37c2aa596377cb4c9b6f916614407fd0a7d3db",
				"53036cece662f9c796d2a187b0077059c3d9088a", "a70ca1d9ad2ea07b19c5e1f9540c809d7a12d3fb",
				"ca7d0c3b33a0863bed04c77932b9ef6b1317f34e", "7c26ac669a4e17ca1d2319a5049a56424fd33104",
				"72f61ec9b85a740fd09d10ad711e275d2ec2e564", "51b8b0e1ad4be1b137d67774eab28dc0ef52cb0a",
				"2b2bb6c734d106cdd1c0f4691607be2fe11d7ebb", "fa3e6fa02dadc675f0d487a15cd842b3ac4a0c11",
				"d05d92de40542e85f9f26712d976e710be82914e", "b7cba6a700d8c5e456cdeffe9c5bf54563eab7d3",
				"200f1577d238a6d3fbcf99cb2a2585b2071214a6", "1768840bf1e69892fd2a23776817f620edfed536",
				"40f00732b9652350ac11830367fd32db67987fc7", "71820e573134be3fad3935035249cd77c4412f4e",
				"5a38d0bca0e48853c3f7c00a0f098bada64797df", "8815eb7d947be6d2a0281c15a3a60d8ba040db95",
				"ffae1691c0cd526ed1095fbabbc0855d016790d7", "3f0d9221d3f96c20db10e868cc33c2e972318ba6",
				"f8197cfe1bc3671aa6878ef9d1869b2fe8e57331", "b2bbd9941be6b132a83d27c0ae02c935e1dec5dd",
				"00aa01fb90f3210d1e3027d7f759fb1085b814bd", "c1b847acdc8cb90a1498b236b3bb5c81ca75c044",
				"711992f22ae6d6dfc43bdb4c01bf8f921d175b38", "72b5348307d86b1a118e546c24d97f1ac1895bdb",
				"563d281b61e9f8748858e911eaa810e981f1e953", "c7b6a7aa878aabd6400d2df0490e1eb2b810c8f9",
				"d5f10a4958f5e870680be906689d92d1efb42480", "5373a852a7e45715e0a6771b7cd56592994c07dd",
				"851ab757698304e9d8d4ae24ab75be619ddae31a", "b6565814805dfb2d989be25c11d4fb4cf8fb1d84",
				"722ef9156896248ef3fbe83adde0f6ff8f46856a", "e1aa31cff985e2a0c2babf4da96dc0a538d5e514",
				"09b9d989658ef5bf9333c081c92b57a7611ad207", "b395127e733b33c27f344695ebf155ecf5edeeab",
				"a9ca2efae56815dc464189b055ffe9da23766f7f", "b36ab386559d04db114db8edd87c8d4cbf850c12",
				"aed371dac5e1248880e869930c636994c3d0f8dc", "378ba37750a9cb1b19a6db434dfa59308f721ea6",
				"19d1936c3b07d97d88646aeae30de747715e3248", "e813a0be86c87366157a0201e6c61662cadee586",
				"58a4e74d28073e7c6f15d1f225ac1c2fd9aa4357", "227254cf769f3e821ed1b2ef2d88c4ec6b20adea",
				"0d4b27952751de0caab01774048c3e0ec74824ce", "cd8c4d578a609bdd6395d3a8c49bfd19ed700dea",
				"e19c6874431dc2c3046436c2ac249a0ab2ef3457", "1571049ec04b1e7e6f082ed5ec071584e7200c12",
				"1eb3b624b288a4b1a054420d3efb05b8f1d28517", "80d3ffb5aafa90992385c17e8338c2cc5def3cec",
				"d5b2bb8cd1393f1c5a5bb623e3d8906cd57e53c4", "02d3fa171d02c9d82c7bdcaeb739f47d0c0006a0",
				"3b1f4e56fea289860b31ef83ccfe96a3a003cc8b", "18a7bd1fd1a83b3b8d1b245e32f78c0b4443b7a7",
				"5b184ac783784c1ca4baf1437888c79bd9460763", "ed966510ccf8441115614e2258aea61df0ea55f5",
				"9aeefcd8120bb3b89cdb437d8c32d2ed84b8a825", "6d10621465c0e6ae81ad8d240d70a55c72caeea6",
				"0ba343846f21649e29ffc600f30a7f3e463fb24c", "b0938501f1014cf663e33b44ed5bb9b24d19a358",
				"c265bde2ace252bc1e1c65c6af93520e5994edd2", "f05e86c4d31987ff2f30330745c3eb605de4c4dc",
				"72acc2126611f0bff9b672de18b9b2f8dacdc03a", "2d98ae165ea43e9a1ac6a905d6094f077abb2e55",
				"2ef067fc70055fc4d55c75937303414ddcf07e0e", "767171c90110c4c5781e8f6d19ece1fba0d492e9",
				"51f498a96b2fa1822e392027982c20e950535fd1", "e32f161fc023d1ee153c49df312ae10b06941465",
				"334dbc7cf3432e7c17b0ed98801e61b0b591b408", "983e0e0e22ab5bd2c6ea44235518057ea45dcca9",
				"b5cf7a0252c8b0465c4dbd906717f7a12e26e6f8", "7fea4c9d5ee97d4a61ad985cadc9c5c0ab2db780",
				"664923815b5aeeba2025bfe1dc5a0cd1a02a80e2", "c1a9dd63aca8bf488f9a671aa6281538540397f8",
				"55ffa2f3353c5dc77fe6b790e5e045b76a07a772", "9d347ffb91f34933edb7b1124f6b70c3fc52e220",
				"9f422ed0f44516bea8116ed7730203e4eb316252", "d31fa31cdcc5ea2fa96116e3b1265baa180df58a",
				"8a16081a9322b4a4062baaf32edc6b6b8b4afa88", "303cb535239a6f07cbe24a033ef965e2f55758eb",
				"e4a309c160285708f917ea23238573da3b677f7f"

		};
		HashMap<String, HashSet<String>> map = new HashMap<String, HashSet<String>>();
		for (int i = 0; i < names.length; i++) {
			if (!map.containsKey(names[i]))
				map.put(names[i], new HashSet<String>());
			map.get(names[i]).add(ids[i]);
		}
		return map;
	}
}
