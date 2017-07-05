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
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.util.io.NullOutputStream;

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

	public GitCommit(final GitConnector cnn, final Repository repository, final RevWalk revwalk) {
		super(cnn);
		this.repository = repository;
		this.revwalk = revwalk;
	}

	@Override
	/** {@inheritDoc} */
	protected String getFileContents(final String path) {
		try {
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

	protected Map<String, Integer> fileNameIndices;

	/**
	 *
	 * @param path Name of file to search for
	 * @return the index the file occurs in fileChanges
	 */
	protected int getFileIndex(final String path) {
		Integer index = fileNameIndices.get(path);
		return index;
	}

	public void getChangeFiles(Map<String, Integer> revisionMap, RevCommit rc) {
		HashMap<String, String> rChangedPaths = new HashMap<String, String>();
		HashMap<String, String> rRemovedPaths = new HashMap<String, String>();
		HashMap<String, String> rAddedPaths = new HashMap<String, String>();
		if (rc.getParentCount() == 0) {
			TreeWalk tw = new TreeWalk(repository);
			tw.reset();
			try {
				tw.addTree(rc.getTree());
				tw.setRecursive(true);
				while (tw.next()) {
					if (!tw.isSubtree()) {
						String path = tw.getPathString();
						rAddedPaths.put(path, null);
					}
				}
			} catch (IOException e) {
				System.err.println(e.getMessage());
			}
			tw.close();
		} else {
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
				if (diff.getChangeType() == ChangeType.MODIFY) {
					if (diff.getOldMode().getObjectType() == Constants.OBJ_BLOB && diff.getNewMode().getObjectType() == Constants.OBJ_BLOB) {
						String path = diff.getNewPath();
						rChangedPaths.put(path, diff.getOldPath());
						filePathGitObjectIds.put(path, diff.getNewId().toObjectId());
					}
				} else if (diff.getChangeType() == ChangeType.RENAME) {
					
				} else if (diff.getChangeType() == ChangeType.COPY) {
					
				} else if (diff.getChangeType() == ChangeType.ADD) {
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
