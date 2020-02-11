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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
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

import boa.datagen.dependencies.GradleFile;
import boa.datagen.dependencies.PomFile;
import boa.datagen.util.FileIO;
import boa.types.Diff.ChangedFile;
import boa.types.Diff.ChangedFile.FileKind;
import boa.types.Shared.ChangeKind;

import boa.datagen.scm.GitConnector.FileLoc;

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
	Map<String, ObjectId> filePathGitObjectIds = new HashMap<String, ObjectId>();


	public GitCommit(final GitConnector cnn, 
			final Repository repository, final RevWalk revwalk, String projectName, 
			long repoKey, Map<String, List<FileLoc>> objectIdToRevisionIdx, int commitIdx) {
		super(cnn);
		this.repository = repository;
		this.revwalk = revwalk;
		this.projectName = projectName;
		this.repoKey = repoKey;
		this.objectIdToFileLoc = objectIdToRevisionIdx;
		this.commitIdx = commitIdx;
	}

	@Override
	/** {@inheritDoc} */
	protected String getFileContents(final String path) {
		ObjectId fileid = filePathGitObjectIds.get(path);
		try {
			buffer.reset();
			buffer.write(repository.open(fileid, Constants.OBJ_BLOB).getCachedBytes());
		} catch (final Throwable e) {
			if (debug)
				System.err.println("Git Error getting contents for '" + path + "' at revision " + id + ": " + e.getMessage());
		}
		return buffer.toString();
	}

	@Override
	public String writeFile(final String classpathRoot, final String path) {
		String name = FileIO.getFileName(path);
		File file = new File(classpathRoot, name);
		if (!file.exists()) {
			ObjectId fileid = filePathGitObjectIds.get(path);
			OutputStream fos = null;
			try {
				buffer.reset();
				buffer.write(repository.open(fileid, Constants.OBJ_BLOB).getCachedBytes());
				fos = new FileOutputStream(file);
				buffer.writeTo(fos);
			} catch (final IOException e) {
				if (debug)
					System.err.println("Git Error write contents of '" + path + "' at revision " + id + ": " + e.getMessage());
				return null;
			} finally {
				try {
					buffer.flush();
				} catch (Exception e) {}
				if (fos != null) {
					try {
						fos.flush();
						fos.close();
					} catch (Exception e) {}
				}
			}
		}
		return file.getAbsolutePath();
	}

	@Override
	public Set<String> getGradleDependencies(final String classpathRoot, final String path) {
		Set<String> paths = new HashSet<String>();
		String content = null;
		ObjectId fileid = filePathGitObjectIds.get(path);
		try {
			buffer.reset();
			buffer.write(repository.open(fileid, Constants.OBJ_BLOB).getCachedBytes());
			content = buffer.toString();
			buffer.flush();
		} catch (final IOException e) {
			if (debug)
				System.err.println("Git Error write contents of '" + path + "' at revision " + id + ": " + e.getMessage());
			return paths;
		}
		if (content == null)
			return paths;
		GradleFile gradle = new GradleFile(content);
		paths = gradle.getDependencies(classpathRoot);
		return paths;
	}
	
	@Override
	public Set<String> getPomDependencies(String outPath, String path,
			HashSet<String> globalRepoLinks, HashMap<String, String> globalProperties, HashMap<String, String> globalManagedDependencies,
			Stack<PomFile> parentPomFiles) {
		Set<String> paths = new HashSet<String>();
		String content = null;
		ObjectId fileid = filePathGitObjectIds.get(path);
		try {
			buffer.reset();
			buffer.write(repository.open(fileid, Constants.OBJ_BLOB).getCachedBytes());
			content = buffer.toString();
			buffer.flush();
		} catch (final IOException e) {
			if (debug)
				System.err.println("Git Error write contents of '" + path + "' at revision " + id + ": " + e.getMessage());
			return paths;
		}
		if (content == null)
			return paths;
		MavenXpp3Reader xpp3Reader = new MavenXpp3Reader();
		Model model = null;
		try {
			model = xpp3Reader.read(new ByteArrayInputStream(content.getBytes()));
		} catch (IOException e1) {
			return paths;
		} catch (XmlPullParserException e1) {
			return paths;
		}
		PomFile pf = new PomFile(path, model.getId(), model.getParent() != null ? model.getParent().getId() : null,
						model.getProperties(),
						model.getDependencyManagement() != null ? model.getDependencyManagement().getDependencies() : null,
						model.getRepositories(),
						globalRepoLinks, globalProperties, globalManagedDependencies,
						parentPomFiles);
		parentPomFiles.push(pf);
		paths = pf.getDependencies(model.getDependencies(), globalRepoLinks, globalProperties, globalManagedDependencies, outPath);
		return paths;
	}

	void updateChangedFiles(RevCommit rc) {
		if (rc.getParentCount() == 0) {
			TreeWalk tw = new TreeWalk(repository);
			tw.reset();
			try {
				tw.addTree(rc.getTree());
				tw.setRecursive(true);
				while (tw.next()) {
					if (!tw.isSubtree()) {
						String path = tw.getPathString();
						getChangeFile(path, ChangeKind.ADDED, tw.getObjectId(0).getName());
						filePathGitObjectIds.put(path, tw.getObjectId(0));
					}
				}
			} catch (IOException e) {
				if (debug)
					System.err.println(e.getMessage());
			}
			tw.close();
		} else {
			parentIndices = new int[rc.getParentCount()];
			for (int i = 0; i < rc.getParentCount(); i++) {
				int parentIndex = connector.revisionMap.get(rc.getParent(i).getName());
				// merged commit in git only store diffs between the first parent and the child
				if (i == 0)
					updateChangedFiles(rc.getParent(i), parentIndex, rc);
				parentIndices[i] = parentIndex;
			}
		}
	}

	private void updateChangedFiles(final RevCommit parent, final int parentIndex, final RevCommit child) {
		final DiffFormatter df = new DiffFormatter(NullOutputStream.INSTANCE);
		df.setRepository(repository);
		df.setDiffComparator(RawTextComparator.DEFAULT);
		df.setDetectRenames(true);
		try {
			final AbstractTreeIterator parentIter = new CanonicalTreeParser(null, repository.newObjectReader(), parent.getTree());
			final AbstractTreeIterator childIter = new CanonicalTreeParser(null, repository.newObjectReader(), child.getTree());
			List<DiffEntry> diffs = df.scan(parentIter, childIter);			
			for (final DiffEntry diff : diffs) {
				if (diff.getChangeType() == ChangeType.MODIFY) {
					if (diff.getNewMode().getObjectType() == Constants.OBJ_BLOB) {
						updateChangedFiles(child, parentIndex, diff, ChangeKind.MODIFIED);
					}
				// RENAMED file may have the same/different object id(s) for old and new
				} else if (diff.getChangeType() == ChangeType.RENAME) {
					if (diff.getNewMode().getObjectType() == Constants.OBJ_BLOB) {
						updateChangedFiles(child, parentIndex, diff, ChangeKind.RENAMED);
					}
				} else if (diff.getChangeType() == ChangeType.COPY) {
					if (diff.getNewMode().getObjectType() == Constants.OBJ_BLOB) {
						updateChangedFiles(child, parentIndex, diff, ChangeKind.COPIED);
					}
				// ADDED file should not have old path and its old object id is 0's
				} else if (diff.getChangeType() == ChangeType.ADD) {
					if (diff.getNewMode().getObjectType() == Constants.OBJ_BLOB) {
						updateChangedFiles(child, parentIndex, diff, ChangeKind.ADDED);
					}
				// DELETED file's new object id is 0's and doesn't have new path
				} else if (diff.getChangeType() == ChangeType.DELETE) {
					if (diff.getOldMode().getObjectType() == Constants.OBJ_BLOB) {
						String newPath = diff.getNewPath();
						String newObjectId = diff.getNewId().toObjectId().getName();
						String oldPath = diff.getOldPath();
						String oldObjectId = diff.getOldId().toObjectId().getName();

						// new object id for building file change linked list
						String id = "BOA_DELETED_FILE_" + objectIdToFileLoc.size();
						ChangedFile.Builder cfb = getChangeFile(oldPath, ChangeKind.DELETED, id);
						
						List<FileLoc> oldLocs = objectIdToFileLoc.get(oldObjectId);
						for (FileLoc oldLoc : oldLocs) {
							cfb.addPreviousVersions(oldLoc.revisionIdx);
							cfb.addPreviousIndices(oldLoc.locIdx);

//							if (oldLocs.size() > 1)
//								System.out.println(oldLoc.revisionIdx 
//										+ " " + oldLoc.locIdx + " " + oldLoc.kind 
//										+ " at " + commitIdx + " " + ChangeKind.DELETED 
//										+ " " + oldLocs.size() + " " + child.getParentCount());
							
						}
						// not use the BOA_DELETED_FILE object id
						filePathGitObjectIds.put(oldPath, diff.getNewId().toObjectId());
					}
				}
			}
		} catch (final IOException e) {
			if (debug)
				System.err.println("Git Error getting commit diffs: " + e.getMessage());
		}
		df.close();
	}

	private void updateChangedFiles(final RevCommit child, int parentIndex, final DiffEntry diff, final ChangeKind kind) {
		String newPath = diff.getNewPath();
		String newObjectId = diff.getNewId().toObjectId().getName();
		String oldPath = diff.getOldPath();
		String oldObjectId = diff.getOldId().toObjectId().getName();
		
		// get old loc before update the map
		List<FileLoc> oldLocs = objectIdToFileLoc.containsKey(oldObjectId) && !ObjectId.zeroId().getName().equals(oldObjectId)
				? objectIdToFileLoc.get(oldObjectId) : null;
		ChangedFile.Builder cfb = getChangeFile(newPath, kind, newObjectId);
		cfb.addChanges(kind);
		if (!oldPath.equals(newPath))
			cfb.addPreviousNames(oldPath);
		if (oldLocs != null) {
			for (FileLoc oldLoc : oldLocs) {
				cfb.addPreviousVersions(oldLoc.revisionIdx);
				cfb.addPreviousIndices(oldLoc.locIdx);

//				if (oldLocs.size() > 1)
//					System.out.println(oldLoc.revisionIdx 
//							+ " " + oldLoc.locIdx + " " + oldLoc.kind 
//							+ " at " + commitIdx + " " + kind 
//							+ " " + oldLocs.size() + " " + child.getParentCount());
			}
		}
		filePathGitObjectIds.put(newPath, diff.getNewId().toObjectId());
	}
	
	public int countChangedFiles(RevCommit rc) {
		int count = 0;
		if (rc.getParentCount() == 0) {
			TreeWalk tw = new TreeWalk(repository);
			tw.reset();
			try {
				tw.addTree(rc.getTree());
				tw.setRecursive(true);
				while (tw.next()) {
					if (!tw.isSubtree()) {
						count++;
					}
				}
			} catch (IOException e) {
				if (debug)
					System.err.println(e.getMessage());
			}
			tw.close();
		} else {
			parentIndices = new int[rc.getParentCount()];
			for (int i = 0; i < rc.getParentCount(); i++) {
				final DiffFormatter df = new DiffFormatter(NullOutputStream.INSTANCE);
				df.setRepository(repository);
				df.setDiffComparator(RawTextComparator.DEFAULT);
				df.setDetectRenames(true);

				try {
					RevCommit parent = revwalk.parseCommit(rc.getParent(i).getId());
					final AbstractTreeIterator parentIter = new CanonicalTreeParser(null, repository.newObjectReader(), parent.getTree());
					List<DiffEntry> diffs = df.scan(parentIter, new CanonicalTreeParser(null, repository.newObjectReader(), rc.getTree()));
					if (diffs.size() > count)
						count = diffs.size();
				} catch (final IOException e) {
					if (debug)
						System.err.println("Git Error getting commit diffs: " + e.getMessage());
				}
				df.close();
			}
		}
		return count;
	}
}
