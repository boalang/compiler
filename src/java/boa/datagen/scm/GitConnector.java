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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevSort;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.TreeWalk;

/**
 * @author rdyer
 * @author josephb
 */
public class GitConnector extends AbstractConnector {
	private static final boolean debug = boa.datagen.util.Properties.getBoolean("debug", boa.datagen.DefaultProperties.DEBUG);

	private String path;

	private Repository repository;
	private Git git;
	private RevWalk revwalk;

	public GitConnector(final String path) {
		try {
			this.path = path;
			this.repository = new FileRepositoryBuilder()
								.setGitDir(new File(path + "/.git"))
								.build();
			this.git = new Git(this.repository);
			this.revwalk = new RevWalk(this.repository);
		} catch (final IOException e) {
			if (debug)
				System.err.println("Git Error connecting to " + path + ". " + e.getMessage());
		}
	}

	@Override
	public void close() {
		revwalk.close();
		repository.close();
	}

	@Override
	public void setRevisions() {
		RevWalk temprevwalk = new RevWalk(repository);
		try {
			revwalk.reset();
			Set<RevCommit> heads = getHeads();
			revwalk.markStart(heads);
			revwalk.sort(RevSort.TOPO, true);
			revwalk.sort(RevSort.COMMIT_TIME_DESC, true);
			revwalk.sort(RevSort.REVERSE, true);
			
			if (revisions == null)
				revisions = new ArrayList<AbstractCommit>(); 
			else
				revisions.clear();
			revisionMap = new HashMap<String, Integer>();
			
			for (final RevCommit rc: revwalk) {
				final GitCommit gc = new GitCommit(this, repository, temprevwalk);

				gc.setId(rc.getName());
				PersonIdent author = rc.getAuthorIdent(), committer = rc.getCommitterIdent();
				if (author != null)
					gc.setAuthor(author.getName(), null, author.getEmailAddress());
				gc.setCommitter(committer.getName(), null, committer.getEmailAddress());
				gc.setDate(new Date(((long) rc.getCommitTime()) * 1000));
				gc.setMessage(rc.getFullMessage());
				
				gc.getChangeFiles(rc);
				
				revisionMap.put(gc.id, revisions.size());
				revisions.add(gc);
			}
			
			RevCommit head = revwalk.parseCommit(repository.resolve(Constants.HEAD));
			headCommitOffset = revisionMap.get(head.getName());
			getBranches();
			getTags();
		} catch (final IOException e) {
			if (debug)
				System.err.println("Git Error getting parsing HEAD commit for " + path + ". " + e.getMessage());
		} finally {
			temprevwalk.dispose();
			temprevwalk.close();
		}
	}

	private Set<RevCommit> getHeads() {
		Set<RevCommit> heads = new HashSet<RevCommit>();
		try {
			for (final Ref ref : git.branchList().call()) {
				heads.add(revwalk.parseCommit(repository.resolve(ref.getName())));
			}
		} catch (final GitAPIException e) {
			if (debug)
				System.err.println("Git Error reading heads: " + e.getMessage());
		}catch (final IOException e) {
			if (debug)
				System.err.println("Git Error reading heads: " + e.getMessage());
		}
		return heads;
	}

	@Override
	public void getTags() {
		try {
			for (final Ref ref : git.tagList().call()) {
				Integer index = revisionMap.get(ref.getObjectId().getName());
				if (index == null)
					continue; // TODO JGit returns wrong commit id
				tagNames.add(ref.getName());
				tagIndices.add(index);
			}
		} catch (final GitAPIException e) {
			if (debug)
				System.err.println("Git Error reading tags: " + e.getMessage());
		}
	}

	@Override
	public void getBranches() {
		try {
			for (final Ref ref : git.branchList().call()) {
				Integer index = revisionMap.get(ref.getObjectId().getName());
				branchNames.add(ref.getName());
				branchIndices.add(index);
			}
		} catch (final GitAPIException e) {
			if (debug)
				System.err.println("Git Error reading branches: " + e.getMessage());
		}
	}

	public List<String> getSnapshot(String commit) {
		ArrayList<String> snapshot = new ArrayList<String>();
		TreeWalk tw = new TreeWalk(repository);
		tw.reset();
		try {
			RevCommit rc = revwalk.parseCommit(repository.resolve(commit));
			tw.addTree(rc.getTree());
			tw.setRecursive(true);
			while (tw.next()) {
				if (!tw.isSubtree()) {
					String path = tw.getPathString();
					snapshot.add(path);
				}
			}
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
		tw.close();
		return snapshot;
	}
}
