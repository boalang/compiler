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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import org.apache.hadoop.io.SequenceFile;

import boa.datagen.DefaultProperties;
import boa.types.Diff.ChangedFile;
import boa.types.Shared.ChangeKind;

/**
 * @author rdyer
 */
public abstract class AbstractConnector implements AutoCloseable {
	
	protected static final boolean debug = boa.datagen.util.Properties.getBoolean("debug", boa.datagen.DefaultProperties.DEBUG);
	protected static final String classpathRoot = boa.datagen.util.Properties.getProperty("libs", boa.datagen.DefaultProperties.CLASSPATH_ROOT);
	protected static final boolean STORE_ASTS = DefaultProperties.STORE_ASTS;
	
	protected String path;
	protected List<AbstractCommit> revisions = new ArrayList<AbstractCommit>();
	protected List<Long> revisionKeys = new ArrayList<Long>();
	protected List<String> branchNames = new ArrayList<String>(), tagNames = new ArrayList<String>();
	protected List<Integer> branchIndices = new ArrayList<Integer>(), tagIndices = new ArrayList<Integer>();
	protected Map<String, Integer> revisionMap = new HashMap<String, Integer>();
	protected String projectName;
	protected int headCommitOffset = -1;
	protected SequenceFile.Writer astWriter, commitWriter, contentWriter;
	protected long astWriterLen = 1, commitWriterLen = 1, contentWriterLen = 1;

	public long getAstWriterLen() {
		return astWriterLen;
	}

	public long getCommitWriterLen() {
		return commitWriterLen;
	}

	public long getContentWriterLen() {
		return contentWriterLen;
	}

	public int getHeadCommitOffset() {
		return this.headCommitOffset;
	}

	public List<ChangedFile> buildHeadSnapshot() {
		if (!revisions.isEmpty()) {
			System.out.println("");
			return buildSnapshot(headCommitOffset);
		}
		return ((GitConnector) this).buildHeadSnapshot();
	}
	
	public List<ChangedFile> buildSnapshot(final int commitOffset) {
		final List<ChangedFile> snapshot = new ArrayList<ChangedFile>();
		getSnapshot(commitOffset, snapshot);
		return snapshot;
	}

	public void getSnapshot(int commitOffset, List<ChangedFile> snapshot) {
		Set<String> adds = new HashSet<String>(), dels = new HashSet<String>(); 
		PriorityQueue<Integer> pq = new PriorityQueue<Integer>(100, new Comparator<Integer>() {
			@Override
			public int compare(Integer i1, Integer i2) {
				return i2 - i1;
			}
		});
		Set<Integer> queuedCommitIds = new HashSet<Integer>();
		pq.offer(commitOffset);
		queuedCommitIds.add(commitOffset);
		while (!pq.isEmpty()) {
			int offset = pq.poll();
			AbstractCommit commit = revisions.get(offset);
			for (ChangedFile.Builder cf : commit.changedFiles) {
				ChangeKind ck = cf.getChange();
				switch (ck) {
					case ADDED:
						if (!adds.contains(cf.getName()) && !dels.contains(cf.getName())) {
							adds.add(cf.getName());
							snapshot.add(cf.build());
						}
						break;
					case COPIED:
						if (!adds.contains(cf.getName()) && !dels.contains(cf.getName())) {
							adds.add(cf.getName());
							snapshot.add(cf.build());
						}
						break;
					case DELETED:
						if (!adds.contains(cf.getName()) && !dels.contains(cf.getName()))
							dels.add(cf.getName());
						break;
					case MERGED:
						if (!adds.contains(cf.getName()) && !dels.contains(cf.getName())) {
							adds.add(cf.getName());
							snapshot.add(cf.build());
						}
						for (int i = 0; i < cf.getChangesCount(); i++) {
							if (cf.getChanges(i) != ChangeKind.ADDED) {
								ChangeKind pck = cf.getChanges(i);
	//							ChangedFile.Builder pcf = revisions.get(cf.getPreviousVersions(i)).changedFiles.get(cf.getPreviousIndices(i));
	//							String name = pcf.getName();
								String name = cf.getPreviousNames(i);
								if (name.isEmpty())
									name = cf.getName();
								if (!adds.contains(name) && !dels.contains(name) && (pck == ChangeKind.DELETED || pck == ChangeKind.RENAMED))
									dels.add(name);
							}
						}
						break;
					case RENAMED:
						if (!adds.contains(cf.getName()) && !dels.contains(cf.getName())) {
							adds.add(cf.getName());
							snapshot.add(cf.build());
						}
						for (int i = 0; i < cf.getChangesCount(); i++) {
	//						ChangedFile.Builder pcf = revisions.get(cf.getPreviousVersions(i)).changedFiles.get(cf.getPreviousIndices(i));
	//						String name = pcf.getName();
							String name = cf.getPreviousNames(i);
							if (!adds.contains(name) && !dels.contains(name))
								dels.add(name);
						}
						break;
					default:
						if (!adds.contains(cf.getName()) && !dels.contains(cf.getName())) {
							adds.add(cf.getName());
							snapshot.add(cf.build());
						}
						break;
				}
			}
			if (commit.parentIndices != null && commit.parentIndices.length != 0) {
				// only consider the first parent
//				for (int p : commit.parentIndices) {
//					if (!queuedCommitIds.contains(p)) {
//						pq.offer(p);
//						queuedCommitIds.add(p);
//					}
//				}
				int p = commit.parentIndices[0];
				if (!queuedCommitIds.contains(p)) {
					pq.offer(p);
					queuedCommitIds.add(p);
				}
			}
		}
	}
	
	public abstract void setRevisions();

	public List<AbstractCommit> getRevisions() {
		return revisions;
	}

	abstract void getTags();

	abstract void getBranches();

	public List<String> getBranchNames() {
		return branchNames;
	}
	
	public List<String> getTagNames() {
		return tagNames;
	}
	
	public List<Integer> getBranchIndices() {
		return branchIndices;
	}
	
	public List<Integer> getTagIndices() {
		return tagIndices;
	}
	
	public List<Object> getRevisions(final String projectName) {
		this.projectName = projectName;
		
		setRevisions();
		
		long maxTime = 1000;
		final List<Object> revs = new ArrayList<Object>();
		if (!revisions.isEmpty()) {
			for (int i = 0; i < revisions.size(); i++) {
				long startTime = System.currentTimeMillis();
				final AbstractCommit rev = revisions.get(i);
				revs.add(rev.asProtobuf(projectName));
				
				if (debug) {
					long endTime = System.currentTimeMillis();
					long time = endTime - startTime;
					if (time > maxTime) {
						System.out.println(Thread.currentThread().getId() + " Max time " + (time / 1000) + " writing to protobuf commit " + (i+1)  + " " + rev.id);
						maxTime = time;
					}
				}
			}
		}
		if (!revisionKeys.isEmpty())
			revs.addAll(revisionKeys);
		return revs;
	}

}
