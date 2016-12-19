package boa.datagen.forges.github;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

import boa.datagen.util.FileIO;
import boa.types.Code.CodeRepository;
import boa.types.Code.CodeRepository.RepositoryKind;
import boa.types.Toplevel.Project;
import boa.types.Toplevel.Project.ForgeKind;
import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

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
	private static final String GIT_PROGRAMMING_LANGUAGES = "language";
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

	private File metadataFile;

	public String id;
	public String name;
	private String shortDescription;
	private String homepage;
	private String summaryPage;
	private long created_timestamp = -1;
	private String description;
	private String os;
	private String[] programmingLanguages;
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

	public RepoMetadata(File file) {
		this.metadataFile = file;
	}

	public RepoMetadata() {
	}

	public boolean build() {
		String jsonTxt = "";
		try {
			BufferedInputStream in = new BufferedInputStream(new FileInputStream(metadataFile));
			byte[] bytes = new byte[(int) metadataFile.length()];
			in.read(bytes);
			in.close();
			jsonTxt = new String(bytes);
		} catch (Exception e) {
			System.err.println("Error reading file " + metadataFile.getAbsolutePath());
			return false;
		}
		if (jsonTxt.isEmpty()) {
			System.err.println("File is empty " + metadataFile.getAbsolutePath());
			return false;
		}
		// System.out.println(jsonTxt);

		JSONObject json = null;
		try {
			json = (JSONObject) JSONSerializer.toJSON(jsonTxt);
		} catch (JSONException e) {
		}
		if (json == null) {
			System.err.println("Error parsing file " + metadataFile.getAbsolutePath());
			return false;
		}
		JSONObject jsonProject = json;
		if (jsonProject.has(GIT_ID))
			this.id = jsonProject.getString(GIT_ID);
		if (jsonProject.has(GIT_NAME))
			this.name = jsonProject.getString(GIT_NAME);
		if (jsonProject.has(GIT_SHORT_DESCRIPTION))
			this.shortDescription = jsonProject.getString(GIT_SHORT_DESCRIPTION);
		if (jsonProject.has(GIT_HOME_PAGE)) {
			this.homepage = jsonProject.getString(GIT_HOME_PAGE);
		}
		if (jsonProject.has(GIT_SUMMARY_PAGE)) {
			this.summaryPage = jsonProject.getString(GIT_SUMMARY_PAGE);
		}
		if (jsonProject.has(GIT_CREATE)) {
			String time = jsonProject.getString(GIT_CREATE);
			this.created_timestamp = getTimeStamp(time); // project.setCreatedDate(timestamp
															// * 1000000);
		}
		if (jsonProject.has(GIT_DESCRIPTION))
			this.description = jsonProject.getString(GIT_DESCRIPTION);
		/*
		 * if (jsonProject.has("os")) { JSONArray jsonOSes =
		 * jsonProject.getJSONArray("os"); if (jsonOSes != null &&
		 * jsonOSes.isArray()) { for (int i = 0; i < jsonOSes.size(); i++)
		 * project.addOperatingSystems(jsonOSes.getString(i)); } }
		 */
		if (jsonProject.has(GIT_PROGRAMMING_LANGUAGES)) {
			buildProgrammingLanguages(metadataFile, id);
			if (this.programmingLanguages == null || this.programmingLanguages.length == 0)
				this.programmingLanguages = new String[] { jsonProject.getString(GIT_PROGRAMMING_LANGUAGES) };
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
			this.gitRepository = jsonProject.getString(GIT_GIT_REPO);
		}
		return true;
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

	private void buildProgrammingLanguages(File metadataFile, String id) {
		File file = new File(
				metadataFile.getParentFile().getParentFile().getAbsolutePath() + "/languages/" + id + ".json");
		if (file.exists()) {
			String content = FileIO.readFileContents(file);
			ArrayList<String> languages = getLanguages(content);
			if (languages.isEmpty())
				return;
			this.programmingLanguages = new String[languages.size()];
			for (int i = 0; i < this.programmingLanguages.length; i++)
				this.programmingLanguages[i] = languages.get(i);
		}
	}

	private static ArrayList<String> getLanguages(String content) {
		ArrayList<String> languages = new ArrayList<String>();
		int status = 0, s = 0;
		String name = null;
		for (int i = 0; i < content.length(); i++) {
			if (status == 0 && content.charAt(i) == '\"') {
				status = 1;
				s = i + 1;
			} else if (status == 1 && content.charAt(i) == '\"') {
				status = 2;
				name = content.substring(s, i);
			} else if (status == 2 && content.charAt(i) == ':') {
				status = 3;
				s = i + 1;
			} else if (status == 3 && !Character.isDigit(content.charAt(i))) {
				status = 0;
				languages.add(name);
			}
		}
		return languages;
	}

	public JSONObject toBoaMetaDataJson() {
		JSONObject jsonRepo = new JSONObject();
		jsonRepo.put(ID, id);
		jsonRepo.put(NAME, name);
		jsonRepo.put(CREATED_TIMESTAMP, created_timestamp);
		jsonRepo.put(SUMMARY_PAGE, summaryPage);
		jsonRepo.put(HOME_PAGE, homepage);
		jsonRepo.put(DESCRIPTION, description);
		if (programmingLanguages != null) {
			JSONArray langs = new JSONArray();
			for (String lang : programmingLanguages)
				langs.add(lang);
			jsonRepo.put(PROGRAMMING_LANGUAGES, langs);
		}
		if (gitRepository != null) {
			JSONObject jsonGit = new JSONObject();
			jsonGit.put("location", gitRepository);
			jsonRepo.put(GIT_REPO, jsonGit);
		}

		JSONObject jo = new JSONObject();
		jo.put("Project", jsonRepo);
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
