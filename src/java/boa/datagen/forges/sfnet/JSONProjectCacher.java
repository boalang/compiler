package boa.datagen.forges.sfnet;

import java.io.*;
import java.util.*;

import boa.datagen.util.FileIO;
import boa.types.Issues.IssueRepository;
import boa.types.Code.CodeRepository;
import boa.types.Code.CodeRepository.RepositoryKind;
import boa.types.Shared.Person;
import boa.types.Toplevel.Project;
import boa.types.Toplevel.Project.ForgeKind;

import com.google.protobuf.CodedInputStream;

import net.sf.json.*;

/**
 * @author hoan
 */
public class JSONProjectCacher {
	public static Project readJSONProject(File file, File cache) {
		// if a valid cache exists, use that instead of parsing
		if (cache != null && cache.exists() && file.lastModified() < cache.lastModified())
			try {
				BufferedInputStream in = new BufferedInputStream(new FileInputStream(cache));
				byte[] bytes = new byte[(int) cache.length()];
				in.read(bytes);
				in.close();
				System.err.println("using cache: " + cache.getName());
				return Project.parseFrom(CodedInputStream.newInstance(bytes, 0, bytes.length));
			} catch (IOException e) { }

		System.err.println("parsing: " + file.getName());

		String jsonTxt = FileIO.readFileContents(file);
		if (jsonTxt.isEmpty()) {
			return null;
		}
		
		JSONObject json = null;
		try {
			json = (JSONObject) JSONSerializer.toJSON(jsonTxt);
		} catch (JSONException e) { }

		if (json == null) {
			return null;
		}

		JSONObject jsonProject = json.getJSONObject("Project");
		if (jsonProject == null || jsonProject.isNullObject()) {
			return null;
		}

		Project.Builder project = Project.newBuilder();
		project.setKind(ForgeKind.SOURCEFORGE);
		if (jsonProject.has("id"))
			project.setId(jsonProject.getString("id"));
		if (jsonProject.has("name"))
			project.setName(jsonProject.getString("name"));
		if (jsonProject.has("homepage"))
			project.setHomepageUrl(jsonProject.getString("homepage"));
		if (jsonProject.has("summary-page"))
			project.setProjectUrl(jsonProject.getString("summary-page"));
		if (jsonProject.has("created_timestamp")) {
			long timestamp = jsonProject.getInt("created_timestamp");
			project.setCreatedDate(timestamp * 1000000);
		} else {
			project.setCreatedDate(-1);
		}
		if (jsonProject.has("description"))
			project.setDescription(jsonProject.getString("description"));
		if (jsonProject.has("os")) {
			JSONArray jsonOSes = jsonProject.getJSONArray("os");
			if (jsonOSes != null && jsonOSes.isArray())
				for (int i = 0; i < jsonOSes.size(); i++)
					project.addOperatingSystems(jsonOSes.getString(i));
		}
		if (jsonProject.has("programming-languages")) {
			ArrayList<String> langs = new ArrayList<String>();
			JSONArray languages = jsonProject.getJSONArray("programming-languages");
			if (languages.isArray())
				for (int i = 0; i < languages.size(); i++)
					langs.add(languages.getString(i).trim().toLowerCase());
			if (!langs.isEmpty())
				project.addAllProgrammingLanguages(langs);
		}
		if (jsonProject.has("databases")) {
			JSONArray jsonDBs = jsonProject.getJSONArray("databases");
			if (jsonDBs.isArray())
				for (int i = 0; i < jsonDBs.size(); i++)
					project.addDatabases(jsonDBs.getString(i).trim());
		}
		if (jsonProject.has("licenses")) {
			ArrayList<String> strLicenses = new ArrayList<String>();
			JSONArray licenses = jsonProject.getJSONArray("licenses");
			if (licenses.isArray())
				for (int i = 0; i < licenses.size(); i++) {
					JSONObject license = licenses.getJSONObject(i);
					if (license.has("name"))
						strLicenses.add(license.getString("name"));
				}
			if (!strLicenses.isEmpty())
				project.addAllLicenses(strLicenses);
		}
		if (jsonProject.has("topics")) {
			ArrayList<String> strTopics = new ArrayList<String>();
			JSONArray topics = jsonProject.getJSONArray("topics");
			if (topics.isArray())
				for (int i = 0; i < topics.size(); i++)
					strTopics.add(topics.getString(i).trim().toLowerCase());
			if (!strTopics.isEmpty())
				project.addAllTopics(strTopics);
		}
		if (jsonProject.has("audiences")) {
			JSONArray jsonAudiences = jsonProject.getJSONArray("audiences");
			if (jsonAudiences.isArray())
				for (int i = 0; i < jsonAudiences.size(); i++)
					project.addAudiences(jsonAudiences.getString(i).trim());
		}
		if (jsonProject.has("environments")) {
			JSONArray jsonEnvs = jsonProject.getJSONArray("environments");
			if (jsonEnvs.isArray())
				for (int i = 0; i < jsonEnvs.size(); i++)
					project.addInterfaces(jsonEnvs.getString(i).trim());
		}
		if (jsonProject.has("topics")) {
			JSONArray jsonTopics = jsonProject.getJSONArray("topics");
			if (jsonTopics.isArray())
				for (int i = 0; i < jsonTopics.size(); i++)
					project.addTopics(jsonTopics.getString(i).trim());
		}
		if (jsonProject.has("donation")) {
			JSONObject jsonDonation = jsonProject.getJSONObject("donation");
			String status = jsonDonation.getString("status");
			if (status.equals("Not Accepting"))
				project.setDonations(false);
			else if (status.equals("Accepting"))
				project.setDonations(true);
		}
		if (jsonProject.has("maintainers")) {
			ArrayList<Person> persons = new ArrayList<Person>();
			JSONArray maintainers = jsonProject.getJSONArray("maintainers");
			if (maintainers.isArray())
				for (int i = 0; i < maintainers.size(); i++) {
					JSONObject maintainer = maintainers.getJSONObject(i);
					if (maintainer.has("name")) {
						Person.Builder person = Person.newBuilder();
						person.setRealName(maintainer.getString("name"));
						person.setUsername(maintainer.getString("name"));
						person.setEmail(maintainer.getString("homepage"));
						persons.add(person.build());
					}
				}
			if (!persons.isEmpty())
				project.addAllMaintainers(persons);
		}
		if (jsonProject.has("developers")) {
			ArrayList<Person> persons = new ArrayList<Person>();
			JSONArray developers = jsonProject.getJSONArray("developers");
			if (developers.isArray())
				for (int i = 0; i < developers.size(); i++) {
					JSONObject developer = developers.getJSONObject(i);
					if (developer.has("name")) {
						Person.Builder person = Person.newBuilder();
						person.setRealName(developer.getString("name"));
						person.setUsername(developer.getString("name"));
						person.setEmail(developer.getString("homepage"));
						persons.add(person.build());
					}
				}
			if (!persons.isEmpty())
				project.addAllDevelopers(persons);
		}
		if (jsonProject.has("trackers")) {
			JSONArray trackers = jsonProject.getJSONArray("trackers");
			if (trackers.isArray())
				for (int i = 0; i < trackers.size(); i++) {
					JSONObject tracker = trackers.getJSONObject(i);
					if (tracker.has("location")) {
						IssueRepository.Builder bug = IssueRepository.newBuilder();
						bug.setUrl(tracker.getString("location"));
						bug.setKind(IssueRepository.IssueKind.OTHER);
						if (tracker.has("name")) {
							if (tracker.getString("name").equals("Bugs"))
								bug.setKind(IssueRepository.IssueKind.BUGS);
							else if (tracker.getString("name").equals("Feature Requests"))
								bug.setKind(IssueRepository.IssueKind.FEATURES);
							else if (tracker.getString("name").equals("Support Requests"))
								bug.setKind(IssueRepository.IssueKind.SUPPORT);
							else if (tracker.getString("name").equals("Patches"))
								bug.setKind(IssueRepository.IssueKind.PATCHES);
						}
						project.addIssueRepositories(bug.build());
					}
				}
		}
		if (jsonProject.has("SVNRepository")) {
			JSONObject rep = jsonProject.getJSONObject("SVNRepository");
			if (rep.has("location")) {
				CodeRepository.Builder cr = CodeRepository.newBuilder();
				cr.setUrl(rep.getString("location"));
				cr.setKind(RepositoryKind.SVN);
				project.addCodeRepositories(cr.build());
			}
		}
		if (jsonProject.has("CVSRepository")) {
			JSONObject rep = jsonProject.getJSONObject("CVSRepository");
			if (rep.has("browse")) {
				CodeRepository.Builder cr = CodeRepository.newBuilder();
				cr.setUrl(rep.getString("browse"));
				cr.setKind(RepositoryKind.CVS);
				project.addCodeRepositories(cr.build());
			}
		}
		// FIXME verify key name
		if (jsonProject.has("GitRepository")) {
			JSONObject rep = jsonProject.getJSONObject("GitRepository");
			if (rep.has("location")) {
				CodeRepository.Builder cr = CodeRepository.newBuilder();
				cr.setUrl(rep.getString("location"));
				cr.setKind(RepositoryKind.GIT);
				project.addCodeRepositories(cr.build());
			}
		}
		// FIXME verify key name
		if (jsonProject.has("BzrRepository")) {
			JSONObject rep = jsonProject.getJSONObject("BzrRepository");
			if (rep.has("location")) {
				CodeRepository.Builder cr = CodeRepository.newBuilder();
				cr.setUrl(rep.getString("location"));
				cr.setKind(RepositoryKind.BZR);
				project.addCodeRepositories(cr.build());
			}
		}
		// FIXME verify key name
		if (jsonProject.has("HgRepository")) {
			JSONObject rep = jsonProject.getJSONObject("HgRepository");
			if (rep.has("location")) {
				CodeRepository.Builder cr = CodeRepository.newBuilder();
				cr.setUrl(rep.getString("location"));
				cr.setKind(RepositoryKind.HG);
				project.addCodeRepositories(cr.build());
			}
		}

		Project prj = project.build();

		// if a cache is specified, cache the result
		if (cache != null)
			try {
				BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(cache));
				out.write(prj.toByteArray());
				out.close();
			} catch (IOException e) { }

		return prj;
	}
}
