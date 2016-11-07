package boa.dsi.dsource.git;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LsRemoteCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevSort;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.EmptyTreeIterator;
import org.eclipse.jgit.util.io.NullOutputStream;

import com.aol.cyclops.data.async.Queue;
import com.google.protobuf.GeneratedMessage;

import boa.datagen.util.DatagenUtil;
import boa.dsi.DSIProperties;
import boa.dsi.dsource.AbstractSource;
import boa.dsi.dsource.java.JavaReader;
import boa.types.Ast.ASTRoot;
//import boa.dsi.dsource.github.Githubschema.ChangeKind;
//import boa.dsi.dsource.github.Githubschema.ChangedFile;
//import boa.dsi.dsource.github.Githubschema.ChangedFile.FileKind;
//import boa.dsi.dsource.github.Githubschema.CodeRepository;
//import boa.dsi.dsource.github.Githubschema.CodeRepository.RepositoryKind;
//import boa.dsi.dsource.github.Githubschema.Person;
//import boa.dsi.dsource.github.Githubschema.Revision;
import boa.types.Code.CodeRepository;
import boa.types.Code.CodeRepository.RepositoryKind;
import boa.types.Code.Revision;
import boa.types.Diff.ChangedFile;
import boa.types.Diff.ChangedFile.FileKind;
import boa.types.Shared.ChangeKind;
import boa.types.Shared.Person;

/**
 * Created by nmtiwari on 10/26/16.
 */
public class GitReader extends AbstractSource {
	private Repository repository;
	private RevWalk revwalk;
	private List<RevCommit> revisions = new ArrayList<RevCommit>();
	private Git git;
	private HashMap<String, ObjectId> filePathGitObjectIds;
	private Map<String, Integer> revisionMap;
	private Map<String, ArrayList<ArrayList<String>>> perRevisionChangedFiles;
	private int[] parentIndices;
	private final int ADDEDFILES = 0;
	private final int CHANGEDFILES = 1;
	private final int REMOVEDFILES = 2;
	// private final String GITPARSERCLASS =
	// "boa.dsi.dsource.github.Githubschema.CodeRepository";
	private final String GITPARSERCLASS = "boa.types.code.CodeRepository";

	public GitReader(ArrayList<String> source) {
		super(source);
		this.revisionMap = new HashMap<String, Integer>();
		this.filePathGitObjectIds = new HashMap<String, ObjectId>();
		this.perRevisionChangedFiles = new HashMap<String, ArrayList<ArrayList<String>>>();
	}

	/**
	 *
	 * @param path
	 *            local path or url for the data
	 * @return returns true if the path is a valid git repository valid: any git
	 *         repository which has atleast one ref as non null;
	 */
	@Override
	public boolean isReadable(String path) {
		// check if it is legal remote url
		// if yes then check if it is clonable
		// else it is local repository then check if it is git repository
		boolean isReadable = false;
		if (DatagenUtil.isValidURL(path)) {
			isReadable = isClonable(path);
		} else if (new File(path).isDirectory()) {
			isReadable = true;
		} else {
			isReadable = false;
		}
		return isReadable;
	}

	/**
	 * 
	 * @return if at least one ref of the git repository is non null
	 */
	private boolean hasAtLeastOneReference() {
		for (Ref ref : this.repository.getAllRefs().values()) {
			if (ref.getObjectId() == null)
				continue;
			return true;
		}
		return false;
	}

	@Override
	public List<GeneratedMessage> getData() {
		List<GeneratedMessage> result = new ArrayList<GeneratedMessage>();
		com.google.protobuf.GeneratedMessage data = null;
		for (String dataSource : this.sources) {
			if (!alreadyCloned(dataSource)) {
				try {
					clone(dataSource, getLocalPath(dataSource));
				} catch (IOException e) {
					e.printStackTrace();
				} catch (GitAPIException e) {
					e.printStackTrace();
				}
			}
			// now build the data
			try {
				this.repository = new FileRepositoryBuilder().setGitDir(new File(getLocalPath(dataSource) + "/.git"))
						.build();
				this.revwalk = new RevWalk(this.repository);
				this.git = new Git(this.repository);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			data = buildData(getLocalPath(dataSource));
		}
		result.add(data);
		return result;
	}

	private com.google.protobuf.GeneratedMessage buildData(String path) {
		this.revisions = this.getRevisions();

		CodeRepository.Builder gitBuilder = CodeRepository.newBuilder();
		gitBuilder.setUrl(path);
		gitBuilder.setKind(RepositoryKind.GIT);

		// branches
		List<String> branches = new ArrayList<String>();
		this.getBranches(branches, new ArrayList<String>());
		for (String branch : branches) {
			gitBuilder.addBranchNames(branch);
		}

		gitBuilder.addBranches(branches.size());

		List<String> tags = new ArrayList<String>();
		this.getTags(tags, new ArrayList<String>());
		for (String tag : tags) {
			gitBuilder.addTagNames(tag);
		}

		gitBuilder.addTags(tags.size());

		for (RevCommit commit : this.revisions) {
			Revision.Builder rev = Revision.newBuilder();
			rev.setId(commit.getId().name());
			Person.Builder person = Person.newBuilder();
			String email = commit.getAuthorIdent().getEmailAddress();
			String name = commit.getAuthorIdent().getName();
			person.setEmail(email);
			person.setRealName(name);
			person.setUsername(name);
			rev.setAuthor(person.build());
			rev.setCommitter(person.build());
			rev.setCommitDate(commit.getCommitTime());
			rev.setLog(commit.getFullMessage());
			rev.addParents(commit.getParentCount());
			rev.addChildren(0);
			ChangedFile.Builder file = ChangedFile.newBuilder();
			for (final String changed : perRevisionChangedFiles.get(commit.getName()).get(CHANGEDFILES)) {
				file.setChange(ChangeKind.CHANGED);
				file.setKey(changed);
				file.setName(changed);
				file.setKind(FileKind.JLS8);
				ArrayList<String> files = new ArrayList<String>();
				files.add(changed);
				JavaReader javaReader = new JavaReader(files);
				ASTRoot content = (ASTRoot) javaReader.getData().get(0);
				if (content != null) {
					file.setAst(content);
					file.setKind(javaReader.getKind());
				}
				rev.addFiles(file.build());
			}
			for (final String changed : perRevisionChangedFiles.get(commit.getName()).get(ADDEDFILES)) {
				file.setChange(ChangeKind.ADDED);
				file.setKey(changed);
				file.setName(changed);
				file.setKind(FileKind.JLS8);
				ArrayList<String> files = new ArrayList<String>();
				files.add(changed);
				JavaReader javaReader = new JavaReader(files);
				ASTRoot content = (ASTRoot) javaReader.getData().get(0);
				if (content != null) {
					file.setAst(content);
					file.setKind(javaReader.getKind());
				}
				rev.addFiles(file.build());
			}
			for (final String changed : perRevisionChangedFiles.get(commit.getName()).get(REMOVEDFILES)) {
				file.setChange(ChangeKind.REMOVED);
				file.setKey(changed);
				file.setName(changed);
				file.setKind(FileKind.JLS8);
				ArrayList<String> files = new ArrayList<String>();
				files.add(changed);
				JavaReader javaReader = new JavaReader(files);
				ASTRoot content = (ASTRoot) javaReader.getData().get(0);
				if (content != null) {
					file.setAst(content);
					file.setKind(javaReader.getKind());
				}
				rev.addFiles(file.build());
			}
			gitBuilder.addRevisions(rev.build());
		}

		return gitBuilder.build();
	}

	private boolean isClonable(String remote) {
		LsRemoteCommand lsCmd = new LsRemoteCommand(null);
		lsCmd.setRemote(remote);
		try {
			lsCmd.call();
			return true;
		} catch (GitAPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

	private String getLocalPath(String dataSource) {
		// Filter the reponame to remove .git
		String[] details = dataSource.split("/");
		String username = details[details.length - 2];
		String reponame = details[details.length - 1];
		if (reponame.endsWith(".git")) {
			reponame = reponame.substring(0, reponame.lastIndexOf("."));
		}
		return DSIProperties.CANDOIA_TRASH_PATH + "/" + DSIProperties.CANDOIA_DIR_NAME + "/" + "/" + username + "/"
				+ reponame;
	}

	private boolean alreadyCloned(String dataSource) {
		return isReadable(getLocalPath(dataSource));
	}

	private void clone(String url, String local)
			throws IOException, InvalidRemoteException, TransportException, GitAPIException {
		// prepare a new folder for the cloned repository
		File localPath = new File(local);
		if (!localPath.exists())
			localPath.mkdir();
		// then clone
		try {
			this.git = Git.cloneRepository().setURI(url).setDirectory(localPath).call();
			// Note: the call() returns an opened repository already which needs
			// to be closed to avoid file handle leaks!
			// workaround for
			// https://bugs.eclipse.org/bugs/show_bug.cgi?id=474093
			this.git.getRepository().close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (this.git != null && this.git.getRepository() != null)
				this.git.getRepository().close();
		}
	}

	private List<RevCommit> getRevisions() {
		try {
			revwalk.reset();
			revwalk.markStart(revwalk.parseCommit(repository.resolve(Constants.HEAD)));
			revwalk.sort(RevSort.TOPO, true);
			revwalk.sort(RevSort.COMMIT_TIME_DESC, true);
			revwalk.sort(RevSort.REVERSE, true);
			revisions.clear();
			revisionMap = new HashMap<String, Integer>();
			for (final RevCommit rc : revwalk) {
				this.getChangeFiles(this.revisionMap, rc);
				this.revisionMap.put(rc.getId().name(), revisions.size());
				this.revisions.add(rc);
			}
		} catch (final IOException e) {
			e.printStackTrace();
		}
		return this.revisions;
	}

	private void getTags(final List<String> names, final List<String> commits) {
		try {
			for (final Ref ref : git.tagList().call()) {
				names.add(ref.getName());
				commits.add(ref.getObjectId().getName());
			}
		} catch (final GitAPIException e) {
			e.printStackTrace();
		}
	}

	private void getBranches(final List<String> names, final List<String> commits) {
		try {
			for (final Ref ref : git.branchList().call()) {
				names.add(ref.getName());
				commits.add(ref.getObjectId().getName());
			}
		} catch (final GitAPIException e) {
			e.printStackTrace();
		}
	}

	public void getChangeFiles(Map<String, Integer> revisionMap, RevCommit rc) {
		ArrayList<String> rChangedPaths = new ArrayList<String>();
		ArrayList<String> rRemovedPaths = new ArrayList<String>();
		ArrayList<String> rAddedPaths = new ArrayList<String>();
		if (rc.getParentCount() == 0)
			getChangeFiles(null, rc, rChangedPaths, rRemovedPaths, rAddedPaths);
		else {
			int[] parentList = new int[rc.getParentCount()];
			for (int i = 0; i < rc.getParentCount(); i++) {
				try {
					getChangeFiles(revwalk.parseCommit(rc.getParent(i).getId()), rc, rChangedPaths, rRemovedPaths,
							rAddedPaths);
				} catch (IOException e) {
					e.printStackTrace();
				}
				parentList[i] = revisionMap.get(rc.getParent(i).getName());
			}
			setParentIndices(parentList);
			if (parentList.length > 1) {
				rChangedPaths.addAll(rAddedPaths);
				rChangedPaths.addAll(rRemovedPaths);
				rAddedPaths.clear();
				rRemovedPaths.clear();
			}
		}
		ArrayList<ArrayList<String>> files = new ArrayList<ArrayList<String>>();
		files.add(ADDEDFILES, rAddedPaths);
		files.add(CHANGEDFILES, rChangedPaths);
		files.add(REMOVEDFILES, rRemovedPaths);
		perRevisionChangedFiles.put(rc.getName(), files);
	}

	private void getChangeFiles(final RevCommit parent, final RevCommit rc, final ArrayList<String> rChangedPaths,
			final ArrayList<String> rRemovedPaths, final ArrayList<String> rAddedPaths) {
		final DiffFormatter df = new DiffFormatter(NullOutputStream.INSTANCE);
		df.setRepository(repository);
		df.setDiffComparator(RawTextComparator.DEFAULT);
		df.setDetectRenames(true);

		try {
			final AbstractTreeIterator parentIter;
			if (parent == null)
				parentIter = new EmptyTreeIterator();
			else
				parentIter = new CanonicalTreeParser(null, repository.newObjectReader(), parent.getTree());

			for (final DiffEntry diff : df.scan(parentIter,
					new CanonicalTreeParser(null, repository.newObjectReader(), rc.getTree()))) {
				if (diff.getChangeType() == ChangeType.MODIFY || diff.getChangeType() == ChangeType.COPY
						|| diff.getChangeType() == ChangeType.RENAME) {
					if (diff.getOldMode().getObjectType() == Constants.OBJ_BLOB
							&& diff.getNewMode().getObjectType() == Constants.OBJ_BLOB) {
						String path = diff.getNewPath();
						rChangedPaths.add(path);
						filePathGitObjectIds.put(path, diff.getNewId().toObjectId());
					}
				} else if (diff.getChangeType() == ChangeType.ADD) {
					if (diff.getNewMode().getObjectType() == Constants.OBJ_BLOB) {
						String path = diff.getNewPath();
						rAddedPaths.add(path);
						filePathGitObjectIds.put(path, diff.getNewId().toObjectId());
					}
				} else if (diff.getChangeType() == ChangeType.DELETE) {
					if (diff.getOldMode().getObjectType() == Constants.OBJ_BLOB) {
						rRemovedPaths.add(diff.getOldPath());
					}
				}
			}
		} catch (final IOException e) {
			e.printStackTrace();
		}
		df.close();
	}

	protected void setParentIndices(final int[] parentList) {
		parentIndices = parentList;
	}

	@Override
	public String getParserClassName() {
		return GITPARSERCLASS;
	}

	@Override
	public boolean getDataInQueue(Queue<GeneratedMessage> queue) {
		// TODO Auto-generated method stub
		return false;
	}

}