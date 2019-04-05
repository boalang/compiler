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
import java.util.Map;
import java.util.Set;

import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.SequenceFile.Writer;
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

import boa.datagen.DefaultProperties;
import boa.types.Code.Revision;
import boa.types.Diff.ChangedFile;
import boa.types.Diff.ChangedFile.FileKind;
import boa.types.Shared.ChangeKind;


/**
 * @author rdyer
 * @author josephb
 */
public class GitConnector extends AbstractConnector {

	private static final int MAX_COMMITS = Integer.valueOf(DefaultProperties.MAX_COMMITS);
	private Repository repository;
	private Git git;
	private RevWalk revwalk;

	public GitConnector(final String path, String projectName) {
		this.projectName = projectName;
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

	public GitConnector(String path, String projectName, Writer astWriter, long astWriterLen, Writer commitWriter, long commitWriterLen, Writer contentWriter, long contentWriterLen) {
		this(path, projectName);
		this.astWriter = astWriter;
		this.commitWriter = commitWriter;
		this.contentWriter = contentWriter;
		this.astWriterLen = astWriterLen;
		this.commitWriterLen = commitWriterLen;
		this.contentWriterLen = contentWriterLen;
	}

	@Override
	public void close() {
		revwalk.close();
		repository.close();
	}
	
	public void countChangedFiles(List<String> commits, Map<String, Integer> counts) {
		RevWalk temprevwalk = new RevWalk(repository);
		try {
			revwalk.reset();
			Set<RevCommit> heads = getHeads();
			revwalk.markStart(heads);
			revwalk.sort(RevSort.TOPO, true);
			revwalk.sort(RevSort.COMMIT_TIME_DESC, true);
			revwalk.sort(RevSort.REVERSE, true);
			for (final RevCommit rc: revwalk) {
				final GitCommit gc = new GitCommit(this, repository, temprevwalk, projectName);
				System.out.println(rc.getName());
				commits.add(rc.getName());
				int count = gc.countChangedFiles(rc);
				counts.put(rc.getName(), count);
			}
		} catch (final IOException e) {
			if (debug)
				System.err.println("Git Error getting parsing HEAD commit for " + path + ". " + e.getMessage());
		} finally {
			temprevwalk.dispose();
			temprevwalk.close();
		}
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
			
			revisionMap = new HashMap<String, Integer>();
			
			int i = 0;
			long maxTime = 1000;
			List<RevCommit> commitList = new ArrayList<RevCommit>();
			for (RevCommit rc : revwalk) {
				commitList.add(rc);
			}
			if (commitList.size() > MAX_COMMITS) {
				System.err.println(projectName + " has " + commitList.size() + " commits " + " exceeding the maximum commit size of " + MAX_COMMITS);
//				return;
			}
				
			for (final RevCommit rc: commitList) {
				i++;
				long startTime = System.currentTimeMillis();
				
				final GitCommit gc = new GitCommit(this, repository, temprevwalk, projectName);
				
				gc.setId(rc.getName());
				try {
					PersonIdent author = rc.getAuthorIdent();
					if (author != null)
						gc.setAuthor(author.getName(), null, author.getEmailAddress());
				} catch (Exception e) {}
				try {
					PersonIdent committer = rc.getCommitterIdent();
					gc.setCommitter(committer.getName(), null, committer.getEmailAddress());
				} catch (Exception e) {
					gc.setCommitter("", null, "");
				}
				gc.setDate(new Date(((long) rc.getCommitTime()) * 1000));
				try {
					gc.setMessage(rc.getFullMessage());
				} catch (Exception e) {}
				
				gc.getChangeFiles(rc);
				gc.fileNameIndices.clear();
				
				if (commitList.size() > MAX_COMMITS) {
					revisionMap.put(gc.id, revisionKeys.size());
					
					Revision revision = gc.asProtobuf(projectName);
					revisionKeys.add(commitWriterLen);
					BytesWritable bw = new BytesWritable(revision.toByteArray());
					commitWriter.append(new LongWritable(commitWriterLen), bw);
					commitWriterLen += bw.getLength();
				} else {
					revisionMap.put(gc.id, revisions.size());
					
					revisions.add(gc);
				}
				
				if (debug) {
					long endTime = System.currentTimeMillis();
					long time = endTime - startTime;
					if (time > maxTime) {
						System.out.println(Thread.currentThread().getId() + " Max time " + (time / 1000) + " parsing metadata commit " + i + " " + rc.getName());
						maxTime = time;
					}
				}
			}
			System.out.println(Thread.currentThread().getId() + " Process metadata of all commits");
			
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

	@Override
	public List<ChangedFile> buildHeadSnapshot() {
		final List<ChangedFile> snapshot = new ArrayList<ChangedFile>();
		TreeWalk tw = new TreeWalk(repository);
		tw.reset();
		try {
			RevCommit rc = revwalk.parseCommit(repository.resolve(Constants.HEAD));
			tw.addTree(rc.getTree());
			tw.setRecursive(true);
			while (tw.next()) {
				if (!tw.isSubtree()) {
					String path = tw.getPathString();
					ChangedFile.Builder cfb = ChangedFile.newBuilder();
					cfb.setChange(ChangeKind.UNKNOWN);
					cfb.setName(path);
					cfb.setKind(FileKind.OTHER);
					cfb.setKey(0);
					cfb.setAst(false);
					if (!STORE_ASTS) {
						cfb.setCommitId(rc.getName());
						cfb.setRepoPath(new File(GH_GIT_PATH + "/" + projectName).getAbsolutePath());
					}
					GitCommit gc = new GitCommit(this, repository, revwalk, projectName);
					gc.filePathGitObjectIds.put(path, tw.getObjectId(0));
					gc.processChangeFile(cfb);
					snapshot.add(cfb.build());
				}
			}
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
		tw.close();
		
		return snapshot;
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
	
	public List<String> logCommitIds() {
		List<String> commits = new ArrayList<String>();
		RevWalk temprevwalk = new RevWalk(repository);
		try {
			revwalk.reset();
			Set<RevCommit> heads = getHeads();
			revwalk.markStart(heads);
			revwalk.sort(RevSort.TOPO, true);
			revwalk.sort(RevSort.COMMIT_TIME_DESC, true);
			revwalk.sort(RevSort.REVERSE, true);
			
			for (final RevCommit rc: revwalk)
				commits.add(rc.getName());
		} catch (final IOException e) {
			e.printStackTrace();
		} finally {
			temprevwalk.dispose();
			temprevwalk.close();
		}
		return commits;
	}
}
