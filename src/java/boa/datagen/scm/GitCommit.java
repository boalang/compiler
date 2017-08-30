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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
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
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.util.io.NullOutputStream;

import boa.datagen.util.FileIO;
import boa.types.Diff.ChangedFile;
import boa.types.Diff.ChangedFile.FileKind;
import boa.types.Shared.ChangeKind;

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
		ObjectId fileid = filePathGitObjectIds.get(path);
		try {
			buffer.reset();
			buffer.write(repository.open(fileid, Constants.OBJ_BLOB).getCachedBytes());
		} catch (final IOException e) {
			if (debug)
				System.err.println("Git Error getting contents for '" + path + "' at revision " + id + ": " + e.getMessage());
		}
		return buffer.toString();
	}

	@Override
	public String writeFile(final String classpathRoot, String path) {
		String name = FileIO.getFileName(path);
		File file = new File(classpathRoot, name);
		if (!file.exists()) {
			ObjectId fileid = filePathGitObjectIds.get(path);
			try {
				buffer.reset();
				buffer.write(repository.open(fileid, Constants.OBJ_BLOB).getCachedBytes());
				OutputStream fos = new FileOutputStream(file);
				buffer.writeTo(fos);
				buffer.flush();
				fos.flush();
				fos.close();
			} catch (final IOException e) {
				if (debug)
					System.err.println("Git Error write contents of '" + path + "' at revision " + id + ": " + e.getMessage());
				return null;
			}
		}
		return file.getAbsolutePath();
	}

	void getChangeFiles(RevCommit rc) {
		if (rc.getParentCount() == 0) {
			TreeWalk tw = new TreeWalk(repository);
			tw.reset();
			try {
				tw.addTree(rc.getTree());
				tw.setRecursive(true);
				while (tw.next()) {
					if (!tw.isSubtree()) {
						String path = tw.getPathString();
						ChangedFile.Builder cfb = ChangedFile.newBuilder();
						cfb.setChange(ChangeKind.ADDED);
						cfb.setName(path);
						cfb.setKind(FileKind.OTHER);
						cfb.setKey(-1);
						fileNameIndices.put(path, changedFiles.size());
						changedFiles.add(cfb);
						filePathGitObjectIds.put(path, tw.getObjectId(0));
					}
				}
			} catch (IOException e) {
				System.err.println(e.getMessage());
			}
			tw.close();
		} else {
			parentIndices = new int[rc.getParentCount()];
			for (int i = 0; i < rc.getParentCount(); i++) {
				try {
					getChangeFiles(revwalk.parseCommit(rc.getParent(i).getId()), rc);
				} catch (IOException e) {
					if (debug)
						System.err.println("Git Error parsing parent commit. " + e.getMessage());
				}
				parentIndices[i] = connector.revisionMap.get(rc.getParent(i).getName());
			}
		}
	}

	private void getChangeFiles(final RevCommit parent, final RevCommit rc) {
		final DiffFormatter df = new DiffFormatter(NullOutputStream.INSTANCE);
		df.setRepository(repository);
		df.setDiffComparator(RawTextComparator.DEFAULT);
		df.setDetectRenames(true);

		try {
			final AbstractTreeIterator parentIter = new CanonicalTreeParser(null, repository.newObjectReader(), parent.getTree());
			
			List<DiffEntry> diffs = df.scan(parentIter, new CanonicalTreeParser(null, repository.newObjectReader(), rc.getTree()));			
			for (final DiffEntry diff : diffs) {
				if (diff.getChangeType() == ChangeType.MODIFY) {
					if (diff.getNewMode().getObjectType() == Constants.OBJ_BLOB) {
						getChangeFile(parent, diff, ChangeKind.MODIFIED);
					}
				} else if (diff.getChangeType() == ChangeType.RENAME) {
					if (diff.getNewMode().getObjectType() == Constants.OBJ_BLOB) {
						getChangeFile(parent, diff, ChangeKind.RENAMED);
					}
				} else if (diff.getChangeType() == ChangeType.COPY) {
					if (diff.getNewMode().getObjectType() == Constants.OBJ_BLOB) {
						getChangeFile(parent, diff, ChangeKind.COPIED);
					}
				} else if (diff.getChangeType() == ChangeType.ADD) {
					if (diff.getNewMode().getObjectType() == Constants.OBJ_BLOB) {
						String path = diff.getNewPath();
						ChangedFile.Builder cfb = getChangeFile(path);
						cfb.setChange(ChangeKind.ADDED);
						if (cfb.getChange() == null || cfb.getChange() == ChangeKind.UNKNOWN)
							cfb.setChange(ChangeKind.ADDED);
						else if (cfb.getChange() != ChangeKind.ADDED)
							cfb.setChange(ChangeKind.MERGED);
						cfb.setName(path);
						cfb.addChanges(ChangeKind.ADDED);
						cfb.addPreviousIndices(-1);
						cfb.addPreviousVersions(-1);
						filePathGitObjectIds.put(path, diff.getNewId().toObjectId());
					}
				}
				else if (diff.getChangeType() == ChangeType.DELETE) {
					if (diff.getOldMode().getObjectType() == Constants.OBJ_BLOB) {
						String path = diff.getOldPath();
						ChangedFile.Builder cfb = getChangeFile(path);
						if (cfb.getChange() == null || cfb.getChange() == ChangeKind.UNKNOWN)
							cfb.setChange(ChangeKind.DELETED);
						else if (cfb.getChange() != ChangeKind.DELETED)
							cfb.setChange(ChangeKind.MERGED);
						cfb.setName(path);
						List<int[]> previousFiles = getPreviousFiles(parent.getName(), diff.getOldPath());
						for (int[] values : previousFiles) {
							cfb.addChanges(ChangeKind.DELETED);
							cfb.addPreviousIndices(values[0]);
							cfb.addPreviousVersions(values[1]);
						}
						filePathGitObjectIds.put(path, diff.getNewId().toObjectId());
					}
				}
			}
		} catch (final IOException e) {
			if (debug)
				System.err.println("Git Error getting commit diffs: " + e.getMessage());
		}
		df.close();
	}

	private void getChangeFile(final RevCommit parent, final DiffEntry diff, final ChangeKind kind) {
		String path = diff.getNewPath();
		ChangedFile.Builder cfb = getChangeFile(path);
		if (cfb.getChange() == null || cfb.getChange() == ChangeKind.UNKNOWN)
			cfb.setChange(kind);
		else if (cfb.getChange() != kind)
			cfb.setChange(ChangeKind.MERGED);
		cfb.setName(path);
		List<int[]> previousFiles = getPreviousFiles(parent.getName(), diff.getOldPath());
		for (int[] values : previousFiles) {
			cfb.addChanges(kind);
			cfb.addPreviousIndices(values[0]);
			cfb.addPreviousVersions(values[1]);
		}
		filePathGitObjectIds.put(path, diff.getNewId().toObjectId());
	}
}
