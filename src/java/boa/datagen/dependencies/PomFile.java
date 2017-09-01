package boa.datagen.dependencies;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.Stack;
import java.util.Map.Entry;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Repository;

import boa.datagen.util.FileIO;

public class PomFile {
	private String id, path;
	private PomFile parent;
	HashMap<String, String> properties = new HashMap<String, String>();
	HashMap<String, String> managedDependencies = new HashMap<String, String>();
	
	public PomFile(String path, String id, String parent, 
			Properties properties, List<Dependency> managedDependencies, List<Repository> repos,
			HashSet<String> globalRepoLinks, HashMap<String, String> globalProperties, HashMap<String, String> globalManagedDependencies,
			Stack<PomFile> parentPomFiles) {
		this.path = path;
		this.id = id;
		if (!parentPomFiles.isEmpty())
			this.parent = parentPomFiles.peek();
		if (properties != null) {
			for (Entry<Object, Object> e : properties.entrySet()) {
				this.properties.put(e.getKey().toString(), e.getValue().toString());
				globalProperties.put(e.getKey().toString(), e.getValue().toString());
			}
		}
		if (managedDependencies != null)
			for (Dependency d : managedDependencies) {
				String v = d.getVersion();
				if (v == null || v.isEmpty())
					continue;
				if (v.startsWith("$")) {
					v = v.substring(1);
					if (v.startsWith("{") && v.endsWith("}"))
						v = v.substring(1, v.length()-1).trim();
					v = getPropertyValue(v);
					if (v != null) {
						if (v.startsWith("[")) {
							v = v.substring(1, v.length() - 1);
							int index = v.indexOf(",");
							if (index > -1)
								v = v.substring(0, index);
						}
					}
				}
				this.managedDependencies.put(d.getGroupId() + ":" + d.getArtifactId(), v);
				globalManagedDependencies.put(d.getGroupId() + ":" + d.getArtifactId(), v);
			}
		if (repos != null) {
			for (int i = 0; i < repos.size(); i++) {
				String url = repos.get(i).getUrl();
				if (url != null && !url.isEmpty())
					globalRepoLinks.add(url);
			}
		}
	}

	public String getPath() {
		return path;
	}

	public Set<String> getDependencies(List<Dependency> dependencies, 
			HashSet<String> globalRepoLinks, HashMap<String, String> globalProperties, HashMap<String, String> globalManagedDependencies,
			String outPath) {
		Set<String> paths = new HashSet<String>();
		for (Dependency dep : dependencies) {
			String[] values = new String[]{dep.getGroupId(), dep.getArtifactId(), dep.getVersion()};
			for (int i = 0; i < values.length; i++) {
				String v = values[i];
				if (v != null && !v.isEmpty()) {
					v = v.trim();
					char ch = v.charAt(0);
					if (ch =='$' || ch == '@') {
						v = v.substring(1);
						if (v.startsWith("{") && v.endsWith("}"))
							v = v.substring(1, v.length()-1).trim();
						String val = getPropertyValue(v);
						if (val == null)
							v = globalProperties.get(v);
						if (v != null) {
							if (v.startsWith("[")) {
								v = v.substring(1, v.length() - 1);
								int index = v.indexOf(",");
								if (index > -1)
									v = v.substring(0, index);
							}
							values[i] = v;
						}
					}
				} else if (i == 2) {
					v = getManagedDepedency(values[0] + ":" + values[1]);
					if (v == null)
						v = globalManagedDependencies.get(values[0] + ":" + values[1]);
					if (v != null)
						values[i] = v;
				}
			}
			if (values[0] == null || values[0].contains("$") || values[0].contains("@")
					|| values[1] == null || values[1].contains("$") || values[1].contains("@") 
					|| values[2] == null || values[2].contains("$") || values[2].contains("@")) {
//				System.err.println("Cannot download pom dependency " + values[0] + ":" + values[1] + ":" + values[2]);
				continue;
			}
			values = DependencyMangementUtil.strip(values);
			values[2] = values[2].replace('+', '0');
			String name = values[1] + "-" + values[2] + ".jar";
			for (String link : globalRepoLinks) {
				link += values[0].replace('.', '/');
				link += "/" + values[1];
				link += "/" + values[2];
				link += "/" + name;
				try {
					String p = FileIO.getFile(outPath, name, link);
					if (p != null)
						paths.add(p);
					break;
				} catch (IOException ex) {
				}
			}
			if (!(new File(outPath + "/" + name).exists())) {
				String prefix = "http://central.maven.org/maven2/";
				prefix += values[0].replace('.', '/');
				prefix += "/" + values[1] + "/";
				try {
					String p = FileIO.getFile(outPath, prefix, values);
					if (p != null)
						paths.add(p);
					break;
				} catch (IOException e1) {
//					System.err.println("Cannot download pom dependency " + values[0] + ":" + values[1] + ":" + values[2]);
				}
			}
		}
		return paths;
	}

	private String getManagedDepedency(String name) {
		String v = this.managedDependencies.get(name);
		if (v != null)
			return v;
		if (this.parent != null) {
			return this.parent.getManagedDepedency(name);
		}
		return null;
	}

	private String getPropertyValue(String name) {
		String v = this.properties.get(name);
		if (v != null)
			return v;
		if (this.parent != null) {
			return this.parent.getPropertyValue(name);
		}
		return null;
	}
	
	@Override
	public String toString() {
		return this.id;
	}
}
