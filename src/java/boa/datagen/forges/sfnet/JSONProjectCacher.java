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

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.protobuf.CodedInputStream;

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
		
		JsonObject json = null;
		try {
			Gson parser = new Gson();
			json = parser.fromJson(jsonTxt, JsonObject.class);
		} catch (JsonSyntaxException e) {}

		if (json == null) {
			return null;
		}

		JsonObject jsonProject = json.get("Project").getAsJsonObject();
		if (jsonProject == null || jsonProject.isJsonArray()) {
			return null;
		}

		Project.Builder project = Project.newBuilder();
		project.setKind(ForgeKind.SOURCEFORGE);
		if (jsonProject.has("id"))
			project.setId(jsonProject.get("id").getAsString());
		if (jsonProject.has("name"))
			project.setName(jsonProject.get("name").getAsString());
		if (jsonProject.has("homepage"))
			project.setHomepageUrl(jsonProject.get("homepage").getAsString());
		if (jsonProject.has("summary-page"))
			project.setProjectUrl(jsonProject.get("summary-page").getAsString());
		if (jsonProject.has("created_timestamp")) {
			long timestamp = jsonProject.get("created_timestamp").getAsLong();
			project.setCreatedDate(timestamp * 1000000);
		} else {
			project.setCreatedDate(-1);
		}
		if (jsonProject.has("description"))
			project.setDescription(jsonProject.get("description").getAsString());
		if (jsonProject.has("os")) {
			JsonArray jsonOSes = jsonProject.get("os").getAsJsonArray();
			if (jsonOSes != null && jsonOSes.isJsonArray())
				for (int i = 0; i < jsonOSes.size(); i++)
					project.addOperatingSystems(jsonOSes.get(i).getAsString());
		}
		if (jsonProject.has("programming-languages")) {
			ArrayList<String> langs = new ArrayList<String>();
			JsonArray languages = jsonProject.get("programming-languages").getAsJsonArray();
			if (languages.isJsonArray())
				for (int i = 0; i < languages.size(); i++)
					langs.add(languages.get(i).getAsString().trim().toLowerCase());
			if (!langs.isEmpty())
				project.addAllProgrammingLanguages(langs);
		}
		if (jsonProject.has("databases")) {
			JsonArray jsonDBs = jsonProject.get("databases").getAsJsonArray();
			if (jsonDBs.isJsonArray())
				for (int i = 0; i < jsonDBs.size(); i++)
					project.addDatabases(jsonDBs.get(i).getAsString().trim());
		}
		if (jsonProject.has("licenses")) {
			ArrayList<String> strLicenses = new ArrayList<String>();
			JsonArray licenses = jsonProject.get("licenses").getAsJsonArray();
			if (licenses.isJsonArray())
				for (int i = 0; i < licenses.size(); i++) {
					JsonObject license = licenses.get(i).getAsJsonObject();
					if (license.has("name"))
						strLicenses.add(license.get("name").getAsString());
				}
			if (!strLicenses.isEmpty())
				project.addAllLicenses(strLicenses);
		}
		if (jsonProject.has("topics")) {
			ArrayList<String> strTopics = new ArrayList<String>();
			JsonArray topics = jsonProject.get("topics").getAsJsonArray();
			if (topics.isJsonArray())
				for (int i = 0; i < topics.size(); i++)
					strTopics.add(topics.get(i).getAsString().trim().toLowerCase());
			if (!strTopics.isEmpty())
				project.addAllTopics(strTopics);
		}
		if (jsonProject.has("audiences")) {
			JsonArray jsonAudiences = jsonProject.get("audiences").getAsJsonArray();
			if (jsonAudiences.isJsonArray())
				for (int i = 0; i < jsonAudiences.size(); i++)
					project.addAudiences(jsonAudiences.get(i).getAsString().trim());
		}
		if (jsonProject.has("environments")) {
			JsonArray jsonEnvs = jsonProject.get("environments").getAsJsonArray();
			if (jsonEnvs.isJsonArray())
				for (int i = 0; i < jsonEnvs.size(); i++)
					project.addInterfaces(jsonEnvs.get(i).getAsString().trim());
		}
		if (jsonProject.has("topics")) {
			JsonArray jsonTopics = jsonProject.get("topics").getAsJsonArray();
			if (jsonTopics.isJsonArray())
				for (int i = 0; i < jsonTopics.size(); i++)
					project.addTopics(jsonTopics.get(i).getAsString().trim());
		}
		if (jsonProject.has("donation")) {
			JsonObject jsonDonation = jsonProject.get("donation").getAsJsonObject();
			String status = jsonDonation.get("status").getAsString();
			if (status.equals("Not Accepting"))
				project.setDonations(false);
			else if (status.equals("Accepting"))
				project.setDonations(true);
		}
		if (jsonProject.has("maintainers")) {
			ArrayList<Person> persons = new ArrayList<Person>();
			JsonArray maintainers = jsonProject.get("maintainers").getAsJsonArray();
			if (maintainers.isJsonArray())
				for (int i = 0; i < maintainers.size(); i++) {
					JsonObject maintainer = maintainers.get(i).getAsJsonObject();
					if (maintainer.has("name")) {
						Person.Builder person = Person.newBuilder();
						person.setRealName(maintainer.get("name").getAsString());
						person.setUsername(maintainer.get("name").getAsString());
						person.setEmail(maintainer.get("homepage").getAsString());
						persons.add(person.build());
					}
				}
			if (!persons.isEmpty())
				project.addAllMaintainers(persons);
		}
		if (jsonProject.has("developers")) {
			ArrayList<Person> persons = new ArrayList<Person>();
			JsonArray developers = jsonProject.get("developers").getAsJsonArray();
			if (developers.isJsonArray())
				for (int i = 0; i < developers.size(); i++) {
					JsonObject developer = developers.get(i).getAsJsonObject();
					if (developer.has("name")) {
						Person.Builder person = Person.newBuilder();
						person.setRealName(developer.get("name").getAsString());
						person.setUsername(developer.get("name").getAsString());
						person.setEmail(developer.get("homepage").getAsString());
						persons.add(person.build());
					}
				}
			if (!persons.isEmpty())
				project.addAllDevelopers(persons);
		}
		if (jsonProject.has("trackers")) {
			JsonArray trackers = jsonProject.get("trackers").getAsJsonArray();
			if (trackers.isJsonArray())
				for (int i = 0; i < trackers.size(); i++) {
					JsonObject tracker = trackers.get(i).getAsJsonObject();
					if (tracker.has("location")) {
						IssueRepository.Builder bug = IssueRepository.newBuilder();
						bug.setUrl(tracker.get("location").getAsString());
//						bug.setKind(IssueRepository.IssueKind.OTHER);
//						if (tracker.has("name")) {
//							if (tracker.getString("name").equals("Bugs"))
//								bug.setKind(IssueRepository.IssueKind.BUGS);
//							else if (tracker.getString("name").equals("Feature Requests"))
//								bug.setKind(IssueRepository.IssueKind.FEATURES);
//							else if (tracker.getString("name").equals("Support Requests"))
//								bug.setKind(IssueRepository.IssueKind.SUPPORT);
//							else if (tracker.getString("name").equals("Patches"))
//								bug.setKind(IssueRepository.IssueKind.PATCHES);
//						}
						if (tracker.has("name"))
							bug.setName(tracker.get("name").getAsString());
						project.addIssueRepositories(bug.build());
					}
				}
		}
		if (jsonProject.has("SVNRepository")) {
			JsonObject rep = jsonProject.get("SVNRepository").getAsJsonObject();
			if (rep.has("location")) {
				CodeRepository.Builder cr = CodeRepository.newBuilder();
				cr.setUrl(rep.get("location").getAsString());
				cr.setKind(RepositoryKind.SVN);
				project.addCodeRepositories(cr.build());
			}
		}
		if (jsonProject.has("CVSRepository")) {
			JsonObject rep = jsonProject.get("CVSRepository").getAsJsonObject();
			if (rep.has("browse")) {
				CodeRepository.Builder cr = CodeRepository.newBuilder();
				cr.setUrl(rep.get("browse").getAsString());
				cr.setKind(RepositoryKind.CVS);
				project.addCodeRepositories(cr.build());
			}
		}
		// FIXME verify key name
		if (jsonProject.has("GitRepository")) {
			JsonObject rep = jsonProject.get("GitRepository").getAsJsonObject();
			if (rep.has("location")) {
				CodeRepository.Builder cr = CodeRepository.newBuilder();
				cr.setUrl(rep.get("location").getAsString());
				cr.setKind(RepositoryKind.GIT);
				project.addCodeRepositories(cr.build());
			}
		}
		// FIXME verify key name
		if (jsonProject.has("BzrRepository")) {
			JsonObject rep = jsonProject.get("BzrRepository").getAsJsonObject();
			if (rep.has("location")) {
				CodeRepository.Builder cr = CodeRepository.newBuilder();
				cr.setUrl(rep.get("location").getAsString());
				cr.setKind(RepositoryKind.BZR);
				project.addCodeRepositories(cr.build());
			}
		}
		// FIXME verify key name
		if (jsonProject.has("HgRepository")) {
			JsonObject rep = jsonProject.get("HgRepository").getAsJsonObject();
			if (rep.has("location")) {
				CodeRepository.Builder cr = CodeRepository.newBuilder();
				cr.setUrl(rep.get("location").getAsString());
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
