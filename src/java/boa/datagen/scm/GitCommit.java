/*
 * Copyright 2015, Hridesh Rajan, Robert Dyer, Hoan Nguyen
 *                 and Iowa State University of Science and Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package boa.datagen.scm;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.EmptyTreeIterator;
import org.eclipse.jgit.util.io.NullOutputStream;

import boa.types.Shared.Person;

/**
 * Concrete implementation of a commit for Git.
 *
 * @author rdyer
 * @author josephb
 */
public class GitCommit extends AbstractCommit {
	// the repository the commit lives in - should already be connected!
	private Repository repository;
	private RevWalk revwalk;
	private HashMap<String, ObjectId> filePathGitObjectIds = new HashMap<String, ObjectId>();

	public GitCommit(final Repository repository, GitConnector cnn) {
		super(cnn);
		this.repository = repository;
		this.revwalk = new RevWalk(repository);
	}

	@Override
	/** {@inheritDoc} */
	protected String getFileContents(final String path) {
		try {
			/*ObjectId fileid = null;
			revwalk.reset();
			tw.reset();
			try {
				tw.addTree(revwalk.lookupCommit(ObjectId.fromString(id)).getTree());
				tw.setRecursive(true);

				while (tw.next())
					if (!tw.isSubtree() && path.equals(tw.getPathString()))
						fileid = tw.getObjectId(0);
			} catch (final IOException e) {
				if (debug)
					System.err.println("Git Error getting contents for '" + path + "' at revision " + id + ": " + e.getMessage());
			}*/
			ObjectId fileid = filePathGitObjectIds.get(path);
			if (fileid == null) return "";

			try {
				buffer.reset();
				buffer.write(repository.open(fileid, Constants.OBJ_BLOB).getCachedBytes());
			} catch (final IOException e) {
				if (debug)
					System.err.println("Git Error getting contents for '" + path + "' at revision " + id + ": " + e.getMessage());
			}
			return buffer.toString();
		} catch (final Exception e) {
			if (debug)
				System.err.println("Git Error getting contents for '" + path + "' at revision " + id + ": " + e.getMessage());
			e.printStackTrace();
		}
		return "";
	}

	@SuppressWarnings("unused")
	private final static Matcher m = Pattern.compile("([^<]+)\\s+<([^>]+)>").matcher("");

	@Override
	/** {@inheritDoc} */
	protected Person parsePerson(final String s) {
		/*m.reset(s);

		if (m.find()) {
			final Person.Builder person = Person.newBuilder();
			person.setUsername(m.group(1));
			person.setRealName(m.group(1));
			person.setEmail(m.group(2));
			return person.build();
		}*/
		if (s != null) {
			final Person.Builder person = Person.newBuilder();
			person.setUsername(s);
			person.setRealName(s);
			person.setEmail(s);
			return person.build();
		}

		return null;
	}

	protected Map<String, Integer> changedFileMap;

	/**
	 *
	 * @param path Name of file to search for
	 * @return the index the file occurs in fileChanges
	 */
	protected int getFileIndex(final String path) {
		if (!changedFileMap.containsKey(path))
			return -1;
		return changedFileMap.get(path);
	}

	public void getChangeFiles(Map<String, Integer> revisionMap, RevCommit rc) {
		HashMap<String, String> rChangedPaths = new HashMap<String, String>();
		HashMap<String, String> rRemovedPaths = new HashMap<String, String>();
		HashMap<String, String> rAddedPaths = new HashMap<String, String>();
		if (rc.getParentCount() == 0)
			getChangeFiles(null, rc, rChangedPaths, rRemovedPaths, rAddedPaths);
		else {
			int[] parentList = new int[rc.getParentCount()];
			for (int i = 0; i < rc.getParentCount(); i++) {
				try {
					getChangeFiles(revwalk.parseCommit(rc.getParent(i).getId()), rc, rChangedPaths, rRemovedPaths, rAddedPaths);
				} catch (IOException e) {
					if (debug)
						System.err.println("Git Error parsing parent commit. " + e.getMessage());
				}
				parentList[i] = revisionMap.get(rc.getParent(i).getName());
			}
			setParentIndices(parentList);
			if (parentList.length > 1) {
				rChangedPaths.putAll(rAddedPaths);
				rChangedPaths.putAll(rRemovedPaths);
				for (String key : rChangedPaths.keySet())
					rChangedPaths.put(key, key);
				rAddedPaths.clear();
				rRemovedPaths.clear();
			}
		}
		setChangedPaths(rChangedPaths);
		setRemovedPaths(rRemovedPaths);
		setAddedPaths(rAddedPaths);
	}

	private void getChangeFiles(final RevCommit parent, final RevCommit rc, final HashMap<String, String> rChangedPaths, final HashMap<String, String> rRemovedPaths, final HashMap<String, String> rAddedPaths) {
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

			for (final DiffEntry diff : df.scan(parentIter, new CanonicalTreeParser(null, repository.newObjectReader(), rc.getTree()))) {
				if (diff.getChangeType() == ChangeType.MODIFY || diff.getChangeType() == ChangeType.COPY || diff.getChangeType() == ChangeType.RENAME) {
					if (diff.getOldMode().getObjectType() == Constants.OBJ_BLOB && diff.getNewMode().getObjectType() == Constants.OBJ_BLOB) {
						String path = diff.getNewPath();
						rChangedPaths.put(path, diff.getOldPath());
						filePathGitObjectIds.put(path, diff.getNewId().toObjectId());
					}
				}
				else if (diff.getChangeType() == ChangeType.ADD) {
					if (diff.getNewMode().getObjectType() == Constants.OBJ_BLOB) {
						String path = diff.getNewPath();
						rAddedPaths.put(path, null);
						filePathGitObjectIds.put(path, diff.getNewId().toObjectId());
					}
				}
				else if (diff.getChangeType() == ChangeType.DELETE) {
					if (diff.getOldMode().getObjectType() == Constants.OBJ_BLOB) {
						rRemovedPaths.put(diff.getOldPath(), diff.getOldPath());
					}
				}
			}
		} catch (final IOException e) {
			if (debug)
				System.err.println("Git Error getting commit diffs: " + e.getMessage());
		}
		df.close();
	}
}
