package boa.datagen.forges.github;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map.Entry;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import boa.types.Code.CodeRepository;
import boa.types.Code.CodeRepository.RepositoryKind;
import boa.types.Toplevel.Project;
import boa.types.Toplevel.Project.ForgeKind;

public class RepoMetadata {
	private static final String ID = "id";
	private static final String NAME = "name";
	private static final String SHORT_DESCRIPTION = "shortdesc";
	private static final String HOME_PAGE = "homepage";
	private static final String SUMMARY_PAGE = "summary-page";
	private static final String CREATED_TIMESTAMP = "created_timestamp";
	private static final String DESCRIPTION = "description";
	private static final String OS = "os";
	private static final String PROGRAMMING_LANGUAGES = "programming-languages";
	private static final String DATABASES = "databases";
	private static final String LICENSES = "licenses";
	private static final String TOPICS = "topics";
	private static final String AUDIENCES = "audiences";
	private static final String ENVIRONMENTS = "environments";
	private static final String DONATION = "donation";
	private static final String MAINTAINERS = "maintainers";
	private static final String DEVELOPERS = "developers";
	private static final String TRACKERS = "trackers";
	private static final String SVN_REPO = "SVNRepository";
	private static final String GIT_REPO = "GitRepository";

	private static final String GIT_ID = "id";
	private static final String GIT_NAME = "full_name";
	private static final String GIT_SHORT_DESCRIPTION = "name";
	private static final String GIT_HOME_PAGE = "homepage";
	private static final String GIT_SUMMARY_PAGE = "html_url";
	// private static final String GIT_CREATED_TIMESTAMP = null;
	private static final String GIT_CREATE = "created_at"; // "created_at":
															// "2007-10-29T14:37:16Z",
	private static final String GIT_UPDATE = "updated_at"; // "updated_at":
															// "2015-06-15T11:40:32Z",
	private static final String GIT_PUSH = "pushed_at"; // "pushed_at":
														// "2014-02-03T19:33:59Z",
	private static final String GIT_DESCRIPTION = "description";
	private static final String GIT_OS = "os";
	private static final String GIT_PROGRAMMING_LANGUAGES = "language_list";
	private static final String GIT_DATABASES = null;
	private static final String GIT_LICENSES = null;
	private static final String GIT_TOPICS = null;
	private static final String GIT_AUDIENCES = null;
	private static final String GIT_ENVIRONMENTS = null;
	private static final String GIT_DONATION = null;
	private static final String GIT_MAINTAINERS = null;
	private static final String GIT_DEVELOPERS = null;
	private static final String GIT_TRACKERS = null;
	private static final String GIT_SVN_REPO = "svn_url";
	private static final String GIT_GIT_REPO = "clone_url";

	private static final String GIT_FORK = "fork";
	/*
	 * other git fields "size": 7954, "stargazers_count": 1856,
	 * "watchers_count": 1856, "language": "Ruby", "has_issues": true,
	 * "has_downloads": true, "has_wiki": true, "has_pages": false,
	 * "forks_count": 448, "mirror_url": null, "open_issues_count": 2, "forks":
	 * 448, "open_issues": 2, "watchers": 1856, "default_branch": "master",
	 * "network_count": 448, "subscribers_count": 60
	 */


	public String id;
	public String name;
	private String shortDescription;
	private String homepage;
	private String summaryPage;
	private long created_timestamp = -1;
	private String description;
	private String os;
	private String[] programmingLanguages;
	private int[] programmingLanguagesLOC;
	private String databases;
	private String licenses;
	private String topics;
	private String audiences;
	private String environments;
	private String donation;
	private String maintainers;
	private String developers;
	private String trackers;
	private String svnRepository;
	private String gitRepository;

	private String fork;

	public RepoMetadata(JsonObject jsonProject) {
		build(jsonProject);
	}

	public void build(JsonObject jsonProject) {
		if (jsonProject.has(GIT_ID))
			this.id = jsonProject.get(GIT_ID).getAsString();
		if (jsonProject.has(GIT_NAME))
			this.name = jsonProject.get(GIT_NAME).getAsString();
		if (jsonProject.has(GIT_SHORT_DESCRIPTION))
			this.shortDescription = jsonProject.get(GIT_SHORT_DESCRIPTION).getAsString();
		if (jsonProject.has(GIT_HOME_PAGE) && !jsonProject.get(GIT_HOME_PAGE).isJsonNull()) {
			this.homepage = jsonProject.get(GIT_HOME_PAGE).getAsString();
		}else
			this.homepage = "";
		if (jsonProject.has(GIT_SUMMARY_PAGE)) {
			this.summaryPage = jsonProject.get(GIT_SUMMARY_PAGE).getAsString();
		}
		if (jsonProject.has(GIT_CREATE)) {
			String time = jsonProject.get(GIT_CREATE).getAsString();
			this.created_timestamp = getTimeStamp(time); // project.setCreatedDate(timestamp
															// * 1000000);
		}
		if (jsonProject.has(GIT_DESCRIPTION))
			this.description = jsonProject.get(GIT_DESCRIPTION).getAsString();
		/*
		 * if (jsonProject.has("os")) { JSONArray jsonOSes =
		 * jsonProject.getJSONArray("os"); if (jsonOSes != null &&
		 * jsonOSes.isArray()) { for (int i = 0; i < jsonOSes.size(); i++)
		 * project.addOperatingSystems(jsonOSes.getString(i)); } }
		 */
		if (jsonProject.has(GIT_PROGRAMMING_LANGUAGES)) {
			JsonObject langList = jsonProject.get(GIT_PROGRAMMING_LANGUAGES).getAsJsonObject();
			int size = langList.entrySet().size();
			this.programmingLanguages = new String[size];//{ jsonProject.get(GIT_PROGRAMMING_LANGUAGES).getAsString() };
			this.programmingLanguagesLOC = new int[size];
			int i = 0;
			for (Entry<String, JsonElement> entry : langList.entrySet()) {
				programmingLanguagesLOC[i]  = entry.getValue().getAsInt();
			    programmingLanguages[i] = entry.getKey();
			    i++;
			}
		}
		/*
		 * if (jsonProject.has("databases")) { JSONArray jsonDBs =
		 * jsonProject.getJSONArray("databases"); if (jsonDBs.isArray()) for
		 * (int i = 0; i < jsonDBs.size(); i++) {
		 * project.addDatabases(jsonDBs.getString(i).trim()) ; } }
		 */
		/*
		 * if (jsonProject.has("licenses")) { ArrayList<String> strLicenses =
		 * new ArrayList<String>(); JSONArray licenses =
		 * jsonProject.getJSONArray("licenses"); if (licenses.isArray()) { for
		 * (int i = 0; i < licenses.size(); i++) { JSONObject license =
		 * licenses.getJSONObject(i); if (license.has("name"))
		 * strLicenses.add(license.getString("name")); } } if
		 * (!strLicenses.isEmpty()) project.addAllLicenses(strLicenses); }
		 */
		/*
		 * if (jsonProject.has("topics")) { ArrayList<String> strTopics = new
		 * ArrayList<String>(); JSONArray topics =
		 * jsonProject.getJSONArray("topics"); if (topics.isArray()) { for (int
		 * i = 0; i < topics.size(); i++) { String topic =
		 * topics.getString(i).trim().toLowerCase(); strTopics.add(topic); } }
		 * if (!strTopics.isEmpty()) project.addAllTopics(strTopics); }
		 */
		/*
		 * if (jsonProject.has("audiences")) { JSONArray jsonAudiences =
		 * jsonProject.getJSONArray("audiences"); if (jsonAudiences.isArray())
		 * for (int i = 0; i < jsonAudiences.size(); i++) {
		 * project.addAudiences(jsonAudiences.getString(i).trim()) ; } }
		 */
		/*
		 * if (jsonProject.has("environments")) { JSONArray jsonEnvs =
		 * jsonProject.getJSONArray("environments"); if (jsonEnvs.isArray()) for
		 * (int i = 0; i < jsonEnvs.size(); i++) {
		 * project.addInterfaces(jsonEnvs.getString(i).trim()) ; } }
		 */
		/*
		 * if (jsonProject.has("donation")) { JSONObject jsonDonation =
		 * jsonProject.getJSONObject("donation"); String status =
		 * jsonDonation.getString("status"); if (status.equals("Not Accepting"))
		 * project.setDonations(false); else if (status.equals("Accepting"))
		 * project.setDonations(true); }
		 */
		/*
		 * if (jsonProject.has("maintainers")) { ArrayList<Person> persons = new
		 * ArrayList<Person>(); JSONArray maintainers =
		 * jsonProject.getJSONArray("maintainers"); if (maintainers.isArray()) {
		 * for (int i = 0; i < maintainers.size(); i++) { JSONObject maintainer
		 * = maintainers.getJSONObject(i); if (maintainer.has("name")) {
		 * Person.Builder person = Person.newBuilder();
		 * person.setRealName(maintainer.getString("name"));
		 * person.setUsername(maintainer.getString("name"));
		 * person.setEmail(maintainer.getString("homepage"));
		 * persons.add(person.build()); } } } if (!persons.isEmpty())
		 * project.addAllMaintainers(persons); }
		 */
		/*
		 * if (jsonProject.has("developers")) { ArrayList<Person> persons = new
		 * ArrayList<Person>(); JSONArray developers =
		 * jsonProject.getJSONArray("developers"); if (developers.isArray()) {
		 * for (int i = 0; i < developers.size(); i++) { JSONObject developer =
		 * developers.getJSONObject(i); if (developer.has("name")) {
		 * Person.Builder person = Person.newBuilder();
		 * person.setRealName(developer.getString("name"));
		 * person.setUsername(developer.getString("name"));
		 * person.setEmail(developer.getString("homepage"));
		 * persons.add(person.build()); } } } if (!persons.isEmpty())
		 * project.addAllDevelopers(persons); }
		 */
		/*
		 * if (jsonProject.has("trackers")) { ArrayList<BugRepository> bugs =
		 * new ArrayList<BugRepository>(); JSONArray trackers =
		 * jsonProject.getJSONArray("trackers"); if (trackers.isArray()) { for
		 * (int i = 0; i < trackers.size(); i++) { JSONObject tracker =
		 * trackers.getJSONObject(i); if (tracker.has("name") &&
		 * tracker.getString("name").equals("Bugs")) { if
		 * (tracker.has("location")) { BugRepository.Builder bug =
		 * BugRepository.newBuilder();
		 * bug.setUrl(tracker.getString("location")); bugs.add(bug.build()); }
		 * break; } } } if (!bugs.isEmpty())
		 * project.addAllBugRepositories(bugs); }
		 */
		if (jsonProject.has(GIT_GIT_REPO)) {
			this.gitRepository = jsonProject.get(GIT_GIT_REPO).getAsString();
		}
	}

	private long getTimeStamp(String time) {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		try {
			Date date = df.parse(time);
			return date.getTime() * 1000000;
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return -1;
	}

	public JsonObject toBoaMetaDataJson() {
		JsonObject jsonRepo = new JsonObject();
		jsonRepo.addProperty(ID, id);
		jsonRepo.addProperty(NAME, name);
		jsonRepo.addProperty(CREATED_TIMESTAMP, created_timestamp);
		jsonRepo.addProperty(SUMMARY_PAGE, summaryPage);
		jsonRepo.addProperty(HOME_PAGE, homepage);
		jsonRepo.addProperty(DESCRIPTION, description);
		if (programmingLanguages != null) {
			JsonArray langs = new JsonArray();
			for (String lang : programmingLanguages)
				langs.add(lang);
			jsonRepo.add(PROGRAMMING_LANGUAGES, langs);
		}
		if (gitRepository != null) {
			JsonObject jsonGit = new JsonObject();
			jsonGit.addProperty("location", gitRepository);
			jsonRepo.add(GIT_REPO, jsonGit);
		}

		JsonObject jo = new JsonObject();
		jo.add("Project", jsonRepo);
		return jo;
	}

	public Project toBoaMetaDataProtobuf() {
		Project.Builder project = Project.newBuilder();
		project.setKind(ForgeKind.GITHUB);
		project.setId(id);
		project.setName(name);
		project.setCreatedDate(created_timestamp);
		project.setProjectUrl(summaryPage);
		project.setHomepageUrl(homepage);
		project.setDescription(description);
		if (programmingLanguages != null) {
			ArrayList<String> langs = new ArrayList<String>();
			for (String lang : programmingLanguages)
				langs.add(lang);
			if (!langs.isEmpty())
				project.addAllProgrammingLanguages(langs);
		}
		if (gitRepository != null) {
			CodeRepository.Builder cr = CodeRepository.newBuilder();
			cr.setUrl(gitRepository);
			cr.setKind(RepositoryKind.GIT);
			cr.setHead(-1);
			project.addCodeRepositories(cr.build());
		}
		Project prj = project.build();
		return prj;
	}

	public Project toBoaMetaDataProtobufWithoutJSON() {
		Project.Builder project = Project.newBuilder();
		project.setKind(ForgeKind.GITHUB);
		project.setId("local");
		project.setName("local");
		project.setCreatedDate(created_timestamp);
		project.setProjectUrl("no summary");
		project.setHomepageUrl("no homepage");
		project.setDescription("no description");
		if (programmingLanguages != null) {
			ArrayList<String> langs = new ArrayList<String>();
			for (String lang : programmingLanguages)
				langs.add(lang);
			if (!langs.isEmpty())
				project.addAllProgrammingLanguages(langs);
		}
		if (gitRepository != null) {
			CodeRepository.Builder cr = CodeRepository.newBuilder();
			cr.setUrl(gitRepository);
			cr.setKind(RepositoryKind.GIT);
			project.addCodeRepositories(cr.build());
		}
		Project prj = project.build();
		return prj;
	}
}
