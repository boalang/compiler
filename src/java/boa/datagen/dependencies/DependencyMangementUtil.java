package boa.datagen.dependencies;

import java.util.ArrayList;
import java.util.List;

public class DependencyMangementUtil {
	public static final List<String> repositoryLinks = new ArrayList<String>();
	
	static {
		repositoryLinks.add("http://central.maven.org/maven2/");
		repositoryLinks.add("https://oss.sonatype.org/content/repositories/releases/");
		repositoryLinks.add("http://repo.spring.io/plugins-release/");
		repositoryLinks.add("http://repo.spring.io/libs-milestone/");
		repositoryLinks.add("https://maven.atlassian.com/content/repositories/atlassian-public/");
		repositoryLinks.add("https://repository.jboss.org/nexus/content/repositories/releases/");
		repositoryLinks.add("https://maven-eu.nuxeo.org/nexus/content/repositories/public-releases/");
		repositoryLinks.add("http://maven.xwiki.org/releases/");
		repositoryLinks.add("https://repository.apache.org/content/repositories/releases/");
		repositoryLinks.add("http://clojars.org/repo/");
		repositoryLinks.add("http://repo.hortonworks.com/content/repositories/releases/");
		repositoryLinks.add("https://maven.repository.redhat.com/ga/");
		repositoryLinks.add("https://repository.cloudera.com/content/repositories/releases/");
		repositoryLinks.add("https://artifacts.alfresco.com/nexus/content/repositories/public/");
		repositoryLinks.add("http://repo.boundlessgeo.com/main/");
		repositoryLinks.add("http://dist.wso2.org/maven2/");
		repositoryLinks.add("https://maven.java.net/content/repositories/releases/");
		repositoryLinks.add("http://repo.opennms.org/maven2/");
		repositoryLinks.add("https://artifactory.cloudsoftcorp.com/artifactory/libs-release-local/");
		repositoryLinks.add("https://repository.jboss.org/nexus/content/repositories/ea/");
	}

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
