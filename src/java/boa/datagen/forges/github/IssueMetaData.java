package boa.datagen.forges.github;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import boa.types.Issues;
import boa.types.Issues.Issue;
import boa.types.Shared.Person;

public class IssueMetaData {

	// boa issue field names
	private final String ID = "id";
	private final String STATUS = "status";
	private final String AUTHOR = "author";
	private final String SUMMARY = "summary";
	private final String DESCRIPTION = "description";
	private final String CREATED_AT = "created_at";
	private final String MODIFIED_AT = "modified_at";
	private final String COMPLETED_AT = "completed_at";
	private final String KEYWORDS = "keywords";
	private final String COMMENTS = "comments";
	private final String MILESTONE = "milestone";
	private final String BLOCKED = "blocked";
	private final String PULLURL = "pullurl";
	private final String ASSIGNEE = "assignee";
	private final String ASSIGNEES = "assigness";
	private final String NUMBER = "number";
	private final String DATE = "date";
	private final String VOTE = "vote";
	private final String PRIORITY = "priority";
	private final String FILES = "files";
	private final String COMPONENT = "component";
	private final String RESOLUTION = "resolution";
	private final String DUPLICATED_BY = "duplicated_by";
	private final String DUPLICATE_OF = "duplicate_of";
	private final String SUBCOMPONENT = "subcomponent";
	private final String VERSION = "version";
	private final String OS = "os";
	private final String PLATFORM = "platform";
	private final String SECRACY = "secracy";
	private final String CHANGES = "changes";
	private final String LABELS = "labels";
	private final String OTHER_STATUS = "other_status";
	private final String OTHER_PRIORITY = "other_priority";
	private final String OTHER_LABELS = "other_labels";
	private final String COMMIT = "commit";

	// github issue field names
	private final String GIT_ID = "id";
	private final String GIT_STATUS = "state";
	private final String GIT_AUTHOR = "user";
	private final String GIT_SUMMARY = "title";
	private final String GIT_DESCRIPTION = "body";
	private final String GIT_CREATED_AT = "created_at";
	private final String GIT_MODIFIED_AT = "updated_at";
	private final String GIT_COMPLETED_AT = "closed_at";
	private final String GIT_COMMENTS = "comments";
	private final String GIT_MILESTONE = "milestone";
	private final String GIT_BLOCKED = "locked";
	private final String GIT_PULLURL = "pull_request";
	private final String GIT_ASSIGNEE = "assignee";
	private final String GIT_ASSIGNEES = "assigness";
	private final String GIT_NUMBER = "number";
	private final String GIT_LABELS = "labels";

	// storage fields
	public String id;
	private String status = "";
	private String author = "";
	public String summary;
	private String description = "";
	private long created_date = -1;
	private long modified_date = -1;
	private long completed_date = -1;
	private JsonArray comments;
	private String milestone = "";
	private String blocked = "";
	private String pullurl = "";
	private String assignee = "";
	private String[] assignees;
	private int number = -1;
	private String[] labels;

	public IssueMetaData(JsonObject jsonIssue) {
		build(jsonIssue);
	}

	private void build(JsonObject jsonIssue) {
		if (jsonIssue.has(GIT_ID))
			this.id = jsonIssue.get(GIT_ID).getAsString();
		if (jsonIssue.has(GIT_SUMMARY))
			this.summary = jsonIssue.get(GIT_SUMMARY).getAsString();
		if (jsonIssue.has(GIT_AUTHOR))
			this.author = jsonIssue.get(GIT_AUTHOR).getAsString();
		if (jsonIssue.has(GIT_SUMMARY))
			this.summary = jsonIssue.get(GIT_SUMMARY).getAsString();
		if (jsonIssue.has(GIT_BLOCKED))
			this.blocked = jsonIssue.get(GIT_BLOCKED).getAsString();
		if (jsonIssue.has(GIT_CREATED_AT)) {
			String time = jsonIssue.get(GIT_CREATED_AT).getAsString();
			this.created_date = getTimeStamp(time);
		}
		if (jsonIssue.has(GIT_COMPLETED_AT)) {
			String time = jsonIssue.get(GIT_COMPLETED_AT).getAsString();
			this.completed_date = getTimeStamp(time);
		}
		if (jsonIssue.has(GIT_MODIFIED_AT)) {
			String time = jsonIssue.get(GIT_MODIFIED_AT).getAsString();
			this.modified_date = getTimeStamp(time);
		}
		if (jsonIssue.has(GIT_MILESTONE))
			this.milestone = jsonIssue.get(GIT_MILESTONE).getAsString();
		if (jsonIssue.has(GIT_NUMBER))
			this.number = jsonIssue.get(GIT_NUMBER).getAsInt();
		if (jsonIssue.has(GIT_STATUS))
			this.status = jsonIssue.get(GIT_STATUS).getAsString();
		if (jsonIssue.has(GIT_DESCRIPTION))
			this.description = jsonIssue.get(GIT_DESCRIPTION).getAsString();
		if (jsonIssue.has(GIT_PULLURL))
			this.pullurl = jsonIssue.get(GIT_PULLURL).getAsString();
		if (jsonIssue.has(GIT_ASSIGNEE))
			this.assignee = jsonIssue.get(GIT_ASSIGNEE).getAsString();
		if (jsonIssue.has(GIT_ASSIGNEES)) {
			JsonArray jArray = jsonIssue.get(GIT_ASSIGNEES).getAsJsonArray();
			assignees = new String[jArray.size()];
			for (int i = 0; i < jArray.size(); i++) {
				assignees[i] = jArray.get(i).getAsString();
			}
		}
		if (jsonIssue.has(GIT_LABELS)) {
			JsonArray jArray = jsonIssue.get(GIT_LABELS).getAsJsonArray();
			labels = new String[jArray.size()];
			for (int i = 0; i < jArray.size(); i++) {
				labels[i] = jArray.get(i).getAsString();
			}
		}
		if (jsonIssue.has(GIT_COMMENTS))
			this.comments = jsonIssue.get(GIT_COMMENTS).getAsJsonArray();
	}

	private long getTimeStamp(String time) {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		try {
			Date date = df.parse(time);
			return date.getTime() * 1000;
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return -1;
	}

	public Issue toBoaMetaDataProtobuf() {
		Issue.Builder ib = Issue.newBuilder();
		ib.setId(this.id);
		Person.Builder person = Person.newBuilder();
		person.setUsername(this.author);
		ib.setAuthor(person.build());
		ib.setNumber(this.number);
		ib.setSummary(this.summary);
		ib.setDescription(this.description);
		ib.setCreatedDate(this.created_date);
		ib.setModifiedDate(this.modified_date);
		if (!this.blocked.equals(""))
			ib.setBlocked(this.blocked);
		if (!this.milestone.equals(""))
			ib.setMilestone(this.milestone);
		if (!this.pullurl.equals(""))
			ib.setPullUrl(this.pullurl);
		if (this.completed_date > -1)
			ib.setCompletedDate(this.completed_date);
		if (!this.assignee.equals("")) {
			person = Person.newBuilder();
			person.setUsername(this.assignee);
			ib.setAssignee(person.build());
		}
		if (this.assignees != null) {
			for (int i = 0; i < this.assignees.length; i++) {
				person = Person.newBuilder();
				person.setUsername(this.assignees[i]);
				ib.addAssignees(person.build());
			}
		}
		// set Issue status
		if (this.status.equals("NEW")) {
			ib.setStatus(Issue.IssueStatus.NEW);
		} else if (this.status.equals("OPEN")) {
			ib.setStatus(Issue.IssueStatus.OPEN);
		} else if (this.status.equals("ON_HOLD")) {
			ib.setStatus(Issue.IssueStatus.ON_HOLD);
		} else if (this.status.equals("RESOLVED")) {
			ib.setStatus(Issue.IssueStatus.RESOLVED);
		} else if (this.status.equals("DUPLICATE")) {
			ib.setStatus(Issue.IssueStatus.DUPLICATE);
		} else if (this.status.equals("INVALID")) {
			ib.setStatus(Issue.IssueStatus.INVALID);
		} else if (this.status.equals("WONT_FIX")) {
			ib.setStatus(Issue.IssueStatus.WONT_FIX);
		} else if (this.status.equals("CLOSED")) {
			ib.setStatus(Issue.IssueStatus.CLOSED);
		} else if (this.status.equals("FIXED")) {
			ib.setStatus(Issue.IssueStatus.FIXED);
		} else if (this.status.equals("NOT_APPLICABLE")) {
			ib.setStatus(Issue.IssueStatus.NOT_APPLICABLE);
		} else if (this.status.equals("NOT_REPRODUCIBLE")) {
			ib.setStatus(Issue.IssueStatus.NOT_REPRODUCIBLE);
		} else if (this.status.equals("EXTERNAL")) {
			ib.setStatus(Issue.IssueStatus.EXTERNAL);
		} else if (this.status.equals("ANSWERED")) {
			ib.setStatus(Issue.IssueStatus.ANSWERED);
		} else {
			ib.setStatus(Issue.IssueStatus.OTHER_STATUS);
			ib.setOtherStatus(this.status);
		}
		// set issue labels
		for (int i = 0; i < this.labels.length; i++) {
			String label = this.labels[i];
			if (label.equals("BUG")) {
				ib.addLabels(Issue.IssueLabel.BUG);
			} else if (label.equals("ENHANCEMENT")) {
				ib.addLabels(Issue.IssueLabel.ENHANCEMENT);
			} else if (label.equals("PROPOSAL")) {
				ib.addLabels(Issue.IssueLabel.PROPOSAL);
			} else if (label.equals("TASK")) {
				ib.addLabels(Issue.IssueLabel.TASK);
			} else if (label.equals("FEATURE")) {
				ib.addLabels(Issue.IssueLabel.FEATURE);
			} else if (label.equals("SUPPORT")) {
				ib.addLabels(Issue.IssueLabel.SUPPORT);
			} else if (label.equals("DISCUSSION")) {
				ib.addLabels(Issue.IssueLabel.DISCUSSION);
			} else if (label.equals("DOCUMENTATION")) {
				ib.addLabels(Issue.IssueLabel.DOCUMENTATION);
			} else if (label.equals("SUGGESTION")) {
				ib.addLabels(Issue.IssueLabel.SUGGESTION);
			} else if (label.equals("QUESTION")) {
				ib.addLabels(Issue.IssueLabel.QUESTION);
			} else if (label.equals("TEST")) {
				ib.addLabels(Issue.IssueLabel.TEST);
			} else {
				ib.addLabels(Issue.IssueLabel.OTHER_LABEL);
				ib.addOtherLabels(label);
			}
		}
		// add comments
		for (int i = 0; i < comments.size(); i++) {
			Issues.IssueComment.Builder cb = Issues.IssueComment.newBuilder();
			JsonObject comment = comments.get(i).getAsJsonObject();
			cb.setId(comment.get(GIT_ID).getAsString());
			person = Person.newBuilder();
			person.setUsername(comment.get(GIT_AUTHOR).getAsString());
			cb.setAuthor(person.build());
			cb.setDescription(comment.get(GIT_DESCRIPTION).getAsString());
			cb.setDate(getTimeStamp(comment.get(GIT_CREATED_AT).getAsString()));
		}
		return ib.build();
	}
}
